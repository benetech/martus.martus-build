package org.martus.client;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.client.ClientSideNetworkHandlerUsingXmlRpc.SSLSocketSetupException;
import org.martus.common.Base64;
import org.martus.common.ByteArrayInputStreamWithSeek;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.FileDatabase;
import org.martus.common.FileInputStreamWithSeek;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceForNonSSL;
import org.martus.common.NetworkInterfaceXmlRpcConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.WrongAccountException;


public class MartusApp
{
	public class SaveConfigInfoException extends Exception {}
	public class LoadConfigInfoException extends Exception {}

	public static class MartusAppInitializationException extends Exception 
	{
		MartusAppInitializationException(String message)
		{
			super(message);
		}
	}

	public MartusApp() throws MartusAppInitializationException
	{
		this(null, determineDataDirectory());
	}
	
	public MartusApp(MartusCrypto cryptoToUse, File dataDirectoryToUse) throws MartusAppInitializationException
	{
		try
		{
			if(cryptoToUse == null)
				cryptoToUse = new MartusSecurity();
				
			dataDirectory = dataDirectoryToUse.getPath() + "/";
			security = cryptoToUse;
			localization = new MartusLocalization();
			store = new BulletinStore(dataDirectoryToUse);
			store.setSignatureGenerator(cryptoToUse);
			store.setEncryptPublicData(true);
			configInfo = new ConfigInfo();

			currentUserName = "";
			maxNewFolders = MAXFOLDERS;
		}
		catch(FileDatabase.MissingAccountMapException e)
		{
			throw new MartusAppInitializationException("ErrorMissingAccountMap");
		}
		catch(MartusCrypto.CryptoInitializationException e)
		{
			throw new MartusAppInitializationException("ErrorCryptoInitialization");
		}

		File languageFlag = new File(getDataDirectory(),"lang.es");
		if(languageFlag.exists())
		{
			languageFlag.delete();
			setCurrentLanguage("es");
			setCurrentDateFormatCode(MartusLocalization.DMY_SLASH.getCode());
		}
		else
		{
			CurrentUiState previouslySavedState = new CurrentUiState();
			previouslySavedState.load(getUiStateFile());
			String previouslySavedStateLanguage = previouslySavedState.getCurrentLanguage();
			if(previouslySavedStateLanguage == "")
				setCurrentLanguage(MartusLocalization.getDefaultUiLanguage());
			else
				setCurrentLanguage(previouslySavedStateLanguage);

			String previouslySavedStateDateFormat = previouslySavedState.getCurrentDateFormat();
			if(previouslySavedStateDateFormat == "")
				setCurrentDateFormatCode(MartusLocalization.getDefaultDateFormatCode());
			else
				setCurrentDateFormatCode(previouslySavedStateDateFormat);
		}
	}

	public void enableUploadLogging()
	{
		logUploads = true;
	}
	
	public void setServerInfo(String serverName, String serverKey)
	{
		configInfo.setServerName(serverName);
		configInfo.setServerPublicKey(serverKey);
		try
		{
			saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			System.out.println("MartusApp.setServerInfo: Unable to Save ConfigInfo" + e);
		}
		
		invalidateCurrentHandlerAndGateway();
	}

	public void setHQKey(String hqKey) throws 
		SaveConfigInfoException
	{
		configInfo.setHQKey(hqKey);
		saveConfigInfo();
	}
	
	public String getHQKey()
	{
		return configInfo.getHQKey();	
	}
	
	public void clearHQKey() throws 
		SaveConfigInfoException
	{
		configInfo.clearHQKey();
		saveConfigInfo();
	}

	public ConfigInfo getConfigInfo()
	{
		return configInfo;
	}

	public void saveConfigInfo() throws SaveConfigInfoException
	{
		String fileName = getConfigInfoFilename();

		try
		{
			ByteArrayOutputStream encryptedConfigOutputStream = new ByteArrayOutputStream();
			configInfo.save(encryptedConfigOutputStream);
			byte[] encryptedConfigInfo = encryptedConfigOutputStream.toByteArray();

			ByteArrayInputStream encryptedConfigInputStream = new ByteArrayInputStream(encryptedConfigInfo);
			FileOutputStream configFileOutputStream = new FileOutputStream(fileName);
			security.encrypt(encryptedConfigInputStream, configFileOutputStream);

			configFileOutputStream.close();
			encryptedConfigInputStream.close();
			encryptedConfigOutputStream.close();


			FileInputStream in = new FileInputStream(fileName);
			byte[] signature = security.createSignature(in);
			in.close();

			FileOutputStream out = new FileOutputStream(getConfigInfoSignatureFilename());
			out.write(signature);
			out.close();
		}
		catch (Exception e)
		{
			System.out.println("saveConfigInfo :" + e);
			throw new SaveConfigInfoException();
		}

	}

	public void loadConfigInfo() throws LoadConfigInfoException
	{
		configInfo.clear();

		String fileName = getConfigInfoFilename();
		File sigFile = new File(getConfigInfoSignatureFilename());
		File dataFile = new File(fileName);

		if(!dataFile.exists())
		{
			//System.out.println("MartusApp.loadConfigInfo: config file doesn't exist");
			return;
		}

		try
		{
			byte[] signature =	new byte[(int)sigFile.length()];
			FileInputStream inSignature = new FileInputStream(sigFile);
			inSignature.read(signature);
			inSignature.close();

			FileInputStream inData = new FileInputStream(dataFile);
			boolean verified = security.isSignatureValid(security.getPublicKeyString(), inData, signature);
			inData.close();
			if(!verified)
				throw new LoadConfigInfoException();

			InputStreamWithSeek encryptedConfigFileInputStream = new FileInputStreamWithSeek(new File(fileName));
			ByteArrayOutputStream plainTextConfigOutputStream = new ByteArrayOutputStream();
			security.decrypt(encryptedConfigFileInputStream, plainTextConfigOutputStream);

			byte[] plainTextConfigInfo = plainTextConfigOutputStream.toByteArray();
			ByteArrayInputStream plainTextConfigInputStream = new ByteArrayInputStream(plainTextConfigInfo);
			configInfo = ConfigInfo.load(plainTextConfigInputStream);

			plainTextConfigInputStream.close();
			plainTextConfigOutputStream.close();
			encryptedConfigFileInputStream.close();
		}
		catch (Exception e)
		{
			//System.out.println("Loadconfiginfo: " + e);
			throw new LoadConfigInfoException();
		}
	}
	
	public String getDataDirectory()
	{
		return dataDirectory;
	}

	public String getConfigInfoFilename()
	{
		return getDataDirectory() + "MartusConfig.dat";
	}

	public String getConfigInfoSignatureFilename()
	{
		return getDataDirectory() + "MartusConfig.sig";
	}

	public File getUploadInfoFile()
	{
		return new File(getDataDirectory() + "MartusUploadInfo.dat");
	}

	public File getUiStateFile()
	{
		return new File(getDataDirectory() + "UiState.dat");
	}

	public String getUploadLogFilename()
	{
		return  getDataDirectory() + "MartusUploadLog.txt";
	}

	public String getHelpFilename()
	{
		String helpFile = "MartusHelp-" + getCurrentLanguage() + ".txt";
		return helpFile;
	}
	
	public String getEnglishHelpFilename()
	{
		return("MartusHelp-en.txt");
	}

	public static String getTranslationsDirectory()
	{
		return determineDataDirectory().getPath();
	}

	public File getKeyPairFile()
	{
		return new File(getDataDirectory() + "MartusKeyPair.dat");
	}
	
	public static File getBackupFile(File original)
	{
		return new File(original.getPath() + ".bak");
	}

	public String getUserName()
	{
		return currentUserName;
	}

	public void loadFolders()
	{
		store.loadFolders();
	}

	public BulletinStore getStore()
	{
		return store;
	}

	public Bulletin createBulletin()
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGAUTHOR, configInfo.getAuthor());
		b.set(Bulletin.TAGORGANIZATION, configInfo.getOrganization());
		b.set(Bulletin.TAGPUBLICINFO, configInfo.getTemplateDetails());
		b.setDraft();
		b.setAllPrivate(true);
		return b;
	}

	public void setHQKeyInBulletin(Bulletin b)
	{
		//System.out.println("App.setHQKeyInBulletin Setting HQ:" + getHQKey());
		b.setHQPublicKey(getHQKey());
	}

	public BulletinFolder getFolderSent()
	{
		return store.getFolderSent();
	}

	public BulletinFolder getFolderDiscarded()
	{
		return store.getFolderDiscarded();
	}

	public BulletinFolder getFolderOutbox()
	{
		return store.getFolderOutbox();
	}
	
	public BulletinFolder getFolderDraftOutbox()
	{
		return store.getFolderDraftOutbox();
	}
	
	public BulletinFolder createFolderRetrieved()
	{
		String folderName = getNameOfFolderRetrieved();
		return createOrFindFolder(folderName);
	}

	public BulletinFolder createFolderRetrievedFieldOffice()
	{
		String folderName = getNameOfFolderRetrievedFieldOffice();
		return createOrFindFolder(folderName);
	}

	public String getNameOfFolderRetrieved()
	{
		return store.getNameOfFolderRetrieved();
	}
	
	public String getNameOfFolderRetrievedFieldOffice()
	{
		return store.getNameOfFolderRetrievedFieldOffice();
	}
	
	public BulletinFolder createOrFindFolder(String name)
	{
		return store.createOrFindFolder(name);
	}

	public void setMaxNewFolders(int numFolders)
	{
		maxNewFolders = numFolders;
	}
	
	public BulletinFolder createUniqueFolder() 
	{
		BulletinFolder newFolder = null;
		String uniqueFolderName = null;
		int folderIndex = 0;
		String originalFolderName = getFieldLabel("defaultFolderName");
		while (newFolder == null && folderIndex < maxNewFolders)
		{
			uniqueFolderName = originalFolderName;
			if (folderIndex > 0)
				uniqueFolderName += folderIndex;
			newFolder = store.createFolder(uniqueFolderName);
			++folderIndex;
		}
		if(newFolder != null)
			store.saveFolders();
		return newFolder;
	}
	
	public int quarantineUnreadableBulletins()
	{
		return store.quarantineUnreadableBulletins();
	}
	
	public int repairOrphans()
	{
		Set orphans = store.getSetOfOrphanedBulletinUniversalIds();
		int foundOrphanCount = orphans.size();
		if(foundOrphanCount == 0)
			return 0;
		
		String name = store.getOrphanFolderName();
		BulletinFolder orphanFolder = store.createOrFindFolder(name);

		Iterator it = orphans.iterator();
		while(it.hasNext())
		{
			UniversalId uid = (UniversalId)it.next();
			store.addBulletinToFolder(uid, orphanFolder);
		}
		
		store.saveFolders();
		return foundOrphanCount;
	}
	

	public Vector findBulletinInAllVisibleFolders(Bulletin b)
	{
		return store.findBulletinInAllVisibleFolders(b);
	}

	public boolean shouldShowDraftUploadReminder()
	{
		if(getFolderDraftOutbox().getBulletinCount() == 0)
			return false;
		return true;
	}
	
	private boolean isSealedFolderOutboxEmpty()
	{
		if(getFolderOutbox().getBulletinCount() == 0)
			return true;
		return false;
	}
	
	public boolean shouldShowSealedUploadReminderOnExit()
	{
		if(isSealedFolderOutboxEmpty())
			return false;
		return true;
	}

	public Date getUploadInfoElement(int index)
	{
		File file = getUploadInfoFile();
		if (!file.canRead())
			return null;
		Date date = null;
		try
		{
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
			for(int i = 0 ; i < index ; ++i)
			{
				stream.readObject();
			}
			date = (Date)stream.readObject();
			stream.close();
		}
		catch (Exception e)
		{
			System.out.println("Error reading from getUploadInfoElement " + index + ":" + e);
		}
		return date;

	}

	public Date getLastUploadedTime()
	{
		return(getUploadInfoElement(0));
	}

	public Date getLastUploadRemindedTime()
	{
		return(getUploadInfoElement(1));
	}


	public void setUploadInfoElements(Date uploaded, Date reminded)
	{
		File file = getUploadInfoFile();
		file.delete();
		try
		{
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
			stream.writeObject(uploaded);
			stream.writeObject(reminded);
			stream.close();
		}
		catch (Exception e)
		{
			System.out.println("Error writing to setUploadInfoElements:" + e);
		}

	}

	public void setLastUploadedTime(Date uploaded)
	{
		Date reminded = getLastUploadRemindedTime();
		setUploadInfoElements(uploaded, reminded);
	}

	public void setLastUploadRemindedTime(Date reminded)
	{
		Date uploaded = getLastUploadedTime();
		setUploadInfoElements(uploaded, reminded);
	}
	
	public void resetLastUploadedTime()
	{
		setLastUploadedTime(new Date());
	}

	public void resetLastUploadRemindedTime()
	{
		setLastUploadRemindedTime(new Date());
	}

	public void search(String searchFor, String startDate, String endDate)
	{
		SearchParser parser = new SearchParser(this);
		SearchTreeNode searchNode = parser.parse(searchFor);

		String name = store.getSearchFolderName();
		BulletinFolder results = store.findFolder(name);
		if(results == null)
			results = store.createFolder(name);

		results.removeAll();

		Vector uids = store.getAllBulletinUids();
		for(int i = 0; i < uids.size(); ++i)
		{
			UniversalId uid = (UniversalId)uids.get(i);
			Bulletin b = store.findBulletinByUniversalId(uid);
			if(b.matches(searchNode, startDate, endDate))
				store.addBulletinToFolder(b.getUniversalId(), results);
		}
		store.saveFolders();
	}

	public boolean isNonSSLServerAvailable(String serverName)
	{
		if(serverName.length() == 0)
			return false;

		int port = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_NON_SSL;
		NetworkInterfaceForNonSSL server = new ClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverName, port);
		return isNonSSLServerAvailable(server);
	}
	
	public boolean isSSLServerAvailable()
	{
		if(currentNetworkInterfaceHandler == null && getServerName().length() == 0)
			return false;
			
		return isSSLServerAvailable(getCurrentNetworkInterfaceGateway());
	}

	public boolean isSSLServerAvailable(String serverName)
	{
		if(serverName.length() == 0)
			return false;

		int port = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_SSL;
		NetworkInterface server;
		try 
		{
			server = new ClientSideNetworkHandlerUsingXmlRpc(serverName, port);
		} 
		catch (SSLSocketSetupException e) 
		{
			//TODO propagate to UI and needs a test.
			e.printStackTrace();
			return false;
		}
		return isSSLServerAvailable(new ClientSideNetworkGateway(server));
	}
	
	public boolean isSignedIn()
	{
		return security.hasKeyPair();	
	}
	
	public String getServerPublicCode(String serverName) throws
		ServerNotAvailableException,
		PublicInformationInvalidException
	{
		try
		{
			return MartusUtilities.computePublicCode(getServerPublicKey(serverName));
		}
		catch(Base64.InvalidBase64Exception e)
		{
			throw new PublicInformationInvalidException();
		}
	}
	
	public String getServerPublicKey(String serverName) throws
		ServerNotAvailableException,
		PublicInformationInvalidException
	{
		int port = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_NON_SSL;
		NetworkInterfaceForNonSSL server = new ClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverName, port);
		return getServerPublicKey(server);
	}
	
	public class ServerNotAvailableException extends Exception {}
	public class PublicInformationInvalidException extends Exception {}
	
	public String getServerPublicKey(NetworkInterfaceForNonSSL server) throws
		ServerNotAvailableException,
		PublicInformationInvalidException
	{
		if(server.ping() == null)
			throw new ServerNotAvailableException();
			
		Vector serverInformation = server.getServerInformation();
		if(serverInformation == null)
			throw new ServerNotAvailableException();
			
		if(serverInformation.size() != 3)
			throw new PublicInformationInvalidException();
		
		String accountId = (String)serverInformation.get(1);
		String sig = (String)serverInformation.get(2);
		validatePublicInfo(accountId, sig);
		return accountId;
	}

	public void validatePublicInfo(String accountId, String sig) throws 
		PublicInformationInvalidException 
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(accountId));
			if(!security.isSignatureValid(accountId, in, Base64.decode(sig)))
				throw new PublicInformationInvalidException();
		
		}
		catch(Exception e)
		{
			//System.out.println("MartusApp.getServerPublicCode: " + e);
			throw new PublicInformationInvalidException();
		}
	}

	public boolean requestServerUploadRights(String magicWord)
	{
		try
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getUploadRights(security, magicWord);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return true;
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			System.out.println("MartusApp.requestServerUploadRights: " + e);
		}
			
		return false;
	}
	
	public String uploadBulletin(Bulletin b, UiProgressMeter progressMeter) throws
			InvalidPacketException
	{
		String result = null;
		File tempFile = null;
		try
		{
			tempFile = File.createTempFile("$$$MartusUploadBulletin", null);
			DatabaseKey headerKey = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());
			Database db = store.getDatabase();
			MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, tempFile, security);
			FileInputStream inputStream = new FileInputStream(tempFile);
			int totalSize = MartusUtilities.getCappedFileLength(tempFile);
			int offset = 0;
			byte[] rawBytes = new byte[serverChunkSize];

			while(true)
			{
				if(progressMeter != null)
				{	
					String message;
					if(b.isDraft())
						message = getFieldLabel("UploadingDraftBulletin");
					else
						message = getFieldLabel("UploadingSealedBulletin");
					progressMeter.updateProgressMeter(message, offset, totalSize);
				}
				int chunkSize = inputStream.read(rawBytes);
				if(chunkSize <= 0)
					break;
				byte[] chunkBytes = new byte[chunkSize];
				System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);

				String authorId = getAccountId();
				String bulletinLocalId = b.getLocalId();
				String encoded = Base64.encode(chunkBytes);
				
				NetworkResponse response = getCurrentNetworkInterfaceGateway().putBulletinChunk(security, 
									authorId, bulletinLocalId, offset, chunkSize, totalSize, encoded);
				result = response.getResultCode();
				if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
					break;
				offset += chunkSize;
			}
			inputStream.close();
		}
		catch(InvalidPacketException e)
		{
			System.out.println("MartusApp.uploadBulletin: " + e);
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("MartusApp.uploadBulletin: " + e);
		}
		
		if(tempFile != null)
			tempFile.delete();
			
		return result;
	}
	
	public String backgroundUpload(UiProgressMeter progressMeter)
	{
		if(getFolderOutbox().getBulletinCount() > 0)
			return backgroundUploadOneSealedBulletin(progressMeter);
			
		if(getFolderDraftOutbox().getBulletinCount() > 0)
			return backgroundUploadOneDraftBulletin(progressMeter);
		if(getConfigInfo().shouldContactInfoBeSentToServer())
			sendContactInfoToServer();
		if(progressMeter != null)
			progressMeter.setStatusMessageAndHideMeter(getFieldLabel("StatusReady"));
		return null;
	}

	private void sendContactInfoToServer()
	{
		ConfigInfo info = getConfigInfo();
		String result = "";
		try 
		{
			result = putContactInfoOnServer(info.getContactInfo(security));
		} 
		catch (MartusSignatureException e) 
		{
			System.out.println("MartusApp:putContactInfoOnServer :" + e);
			return;
		} 
		if(!result.equals(NetworkInterfaceConstants.OK))
		{
			System.out.println("MartusApp:putContactInfoOnServer Failed:" + result);
			return;
		}
		System.out.println("Contact info successfully sent to server");	

		try 
		{
			info.setSendContactInfoToServer(false);
			saveConfigInfo();
		} 
		catch (SaveConfigInfoException e) 
		{
			System.out.println("MartusApp:putContactInfoOnServer Failed to save configinfo locally:" + e);
		}
	}		

	String backgroundUploadOneSealedBulletin(UiProgressMeter progressMeter) 
	{
		if(!isSSLServerAvailable())
		{
			if(progressMeter != null)
				progressMeter.setStatusMessageAndHideMeter(getFieldLabel("NoServerAvailableProgressMessage"));
			return null;
		}
		
		BulletinFolder outbox = getFolderOutbox();
		Bulletin b = outbox.getBulletinSorted(0);
		try
		{
			String result = uploadBulletin(b, progressMeter);
			
			if(result != null)
			{
				if(result.equals(NetworkInterfaceConstants.OK) || result.equals(NetworkInterfaceConstants.DUPLICATE))
				{
					outbox.remove(b.getUniversalId());
					store.moveBulletin(b, outbox, getFolderSent());
					store.saveFolders();
					resetLastUploadedTime();
					if(logUploads)
					{
						try
						{
							File file = new File(getUploadLogFilename());
							UnicodeWriter log = new UnicodeWriter(file, UnicodeWriter.APPEND);
							log.writeln(b.getLocalId());
							log.writeln(configInfo.getServerName());
							log.writeln(b.get(b.TAGTITLE));
							log.close();
							log = null;
						}
						catch(Exception e)
						{
							System.out.println("MartusApp.backgroundUpload: " + e);
						}
					}
				}
				return result;
			}
		}
		catch (InvalidPacketException e)
		{
			System.out.println("MartusApp.backgroundUploadOneSealedBulletin: ");
			System.out.println("  InvalidPacket. Moving from outbox to damaged");
			BulletinFolder damaged = createOrFindFolder(store.getNameOfFolderDamaged());
			store.moveBulletin(b, outbox, damaged);
			store.saveFolders();
		}
		
		if(progressMeter != null)
			progressMeter.setStatusMessageAndHideMeter(getFieldLabel("UploadFailedProgressMessage"));
		return null;
	}

	String backgroundUploadOneDraftBulletin(UiProgressMeter progressMeter) 
	{
		if(!isSSLServerAvailable())
		{
			if(progressMeter != null)
				progressMeter.setStatusMessageAndHideMeter(getFieldLabel("NoServerAvailableProgressMessage"));
			return null;
		}
		
		BulletinFolder draftOutbox = getFolderDraftOutbox();
		Bulletin b = draftOutbox.getBulletinSorted(0);
		try
		{
			String result = uploadBulletin(b, progressMeter);
			
			if(result != null)
			{
				if(result.equals(NetworkInterfaceConstants.OK))
				{
					draftOutbox.remove(b.getUniversalId());
					store.saveFolders();
				}
				return result;
			}
		}
		catch (InvalidPacketException e)
		{
			System.out.println("MartusApp.backgroundUploadOneDraftBulletin: ");
			System.out.println("  InvalidPacket. Removing from draftoutbox");
			draftOutbox.remove(b.getUniversalId());
			store.saveFolders();
		}

		if(progressMeter != null)
			progressMeter.setStatusMessageAndHideMeter(getFieldLabel("UploadFailedProgressMessage"));
		return null;
	}

	public static class ServerErrorException extends Exception 
	{
		public ServerErrorException(String message)
		{
			super(message);
		}
		
		public ServerErrorException()
		{
			this("");
		}
	}
	
	public Vector getMyServerBulletinSummaries() throws ServerErrorException
	{
		String resultCode = "?";
		try 
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getSealedBulletinIds(security, getAccountId());
			resultCode = response.getResultCode();
			if(resultCode.equals(NetworkInterfaceConstants.OK))
				return response.getResultVector();
		} 
		catch (MartusSignatureException e) 
		{
			System.out.println("MartusApp.getMyServerBulletinSummaries: " + e);
			resultCode = NetworkInterfaceConstants.SIG_ERROR;
		}
		throw new ServerErrorException(resultCode);
	}
	
	public Vector getMyDraftServerBulletinSummaries() throws ServerErrorException
	{
		String resultCode = "?";
		try 
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getDraftBulletinIds(security, getAccountId());
			resultCode = response.getResultCode();
			if(resultCode.equals(NetworkInterfaceConstants.OK))
				return response.getResultVector();
		}
		catch (MartusSignatureException e) 
		{
			System.out.println("MartusApp.getMyDraftServerBulletinSummaries: " + e);
			resultCode = NetworkInterfaceConstants.SIG_ERROR;
		}
		throw new ServerErrorException(resultCode);
	}

	public BulletinSummary createSummaryFromString(String accountId, String pair)
		throws ServerErrorException 
	{
		int at = pair.indexOf("=");
		if(at < 0)
			throw new ServerErrorException("MartusApp.createSummaryFromString: " + pair);
			
		String bulletinLocalId = pair.substring(0, at);
		String summary = pair.substring(at + 1);
		String author = "";
		if(FieldDataPacket.isValidLocalId(summary))
		{
			String packetlocalId = summary;
			try 
			{
				FieldDataPacket fdp = retrieveFieldDataPacketFromServer(accountId, bulletinLocalId, packetlocalId);
				summary = fdp.get(Bulletin.TAGTITLE);
				author = fdp.get(Bulletin.TAGAUTHOR);
			} 
			catch(Exception e) 
			{
				System.out.println("MartusApp.getSummaries: " + e);
				e.printStackTrace();
				throw new ServerErrorException();
			}
		}
		else
		{
			if(summary.length() > 0)
				summary = summary.substring(1);
		}
		BulletinSummary bulletinSummary = new BulletinSummary(accountId, bulletinLocalId, summary, author);
		return bulletinSummary;
	}
	
	public Vector getFieldOfficeAccounts() throws ServerErrorException
	{
		try
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getFieldOfficeAccountIds(security, getAccountId());
			String resultCode = response.getResultCode();
			if(!resultCode.equals(NetworkInterfaceConstants.OK))
				throw new ServerErrorException(resultCode);
			return response.getResultVector();
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			System.out.println("MartusApp.getFieldOfficeAccounts: " + e);
			throw new ServerErrorException();
		}
	}

	public FieldDataPacket retrieveFieldDataPacketFromServer(String authorAccountId, String bulletinLocalId, String dataPacketLocalId) throws Exception
	{
		NetworkResponse response = getCurrentNetworkInterfaceGateway().getPacket(security, authorAccountId, bulletinLocalId, dataPacketLocalId);
		String resultCode = response.getResultCode();
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
			throw new ServerErrorException(resultCode);

		String xml = (String)response.getResultVector().get(0);
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, dataPacketLocalId);
		FieldDataPacket fdp = new FieldDataPacket(uid , Bulletin.getStandardFieldNames());
		byte[] xmlBytes = xml.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in =  new ByteArrayInputStreamWithSeek(xmlBytes);
		fdp.loadFromXml(in, security);
		return fdp;
	}

	public void retrieveOneBulletin(UniversalId uid, BulletinFolder retrievedFolder, UiProgressMeter progressMeter) throws
		Exception
	{
		File tempFile = File.createTempFile("$$$MartusApp", null);
		tempFile.deleteOnExit();
		FileOutputStream outputStream = new FileOutputStream(tempFile);

		int masterTotalSize = 0;
		int totalSize = 0;
		int chunkOffset = 0;
		String lastResponse = "";
		if(progressMeter != null)
			progressMeter.updateProgressMeter(getFieldLabel("ChunkProgressStatusMessage"), 0, 1);	
		while(!lastResponse.equals(NetworkInterfaceConstants.OK))
		{
			
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getBulletinChunk(security, 
								uid.getAccountId(), uid.getLocalId(), chunkOffset, serverChunkSize);
								
			lastResponse = response.getResultCode();
			if(!lastResponse.equals(NetworkInterfaceConstants.OK) &&
				!lastResponse.equals(NetworkInterfaceConstants.CHUNK_OK))
			{
				//System.out.println((String)result.get(0));
				throw new ServerErrorException();
			}
			
			Vector result = response.getResultVector();
			totalSize = ((Integer)result.get(0)).intValue();
			if(masterTotalSize == 0)
				masterTotalSize = totalSize;
				
			if(totalSize != masterTotalSize)
				throw new ServerErrorException("totalSize not consistent");
			if(totalSize < 0)
				throw new ServerErrorException("totalSize negative");
				
			int chunkSize = ((Integer)result.get(1)).intValue();
			if(chunkSize < 0 || chunkSize > totalSize - chunkOffset)
				throw new ServerErrorException("chunkSize out of range");
			
			// TODO: validate that length of data == chunkSize that was returned
			String data = (String)result.get(2);
			StringReader reader = new StringReader(data);

			Base64.decode(reader, outputStream);
			chunkOffset += chunkSize;
			if(progressMeter != null)
				progressMeter.updateProgressMeter(getFieldLabel("ChunkProgressStatusMessage"), chunkOffset, masterTotalSize);	
		}
		if(progressMeter != null)
			progressMeter.updateProgressMeter(getFieldLabel("ChunkProgressStatusMessage"), chunkOffset, masterTotalSize);	

		outputStream.close();

		if(tempFile.length() != masterTotalSize)
			throw new ServerErrorException("totalSize didn't match data length");
			
		store.importZipFileBulletin(tempFile, retrievedFolder, true);

		tempFile.delete();
	}

	public String deleteServerDraftBulletins(Vector uidList) throws 
		MartusSignatureException,
		WrongAccountException
	{
		String[] localIds = new String[uidList.size()];
		for (int i = 0; i < localIds.length; i++)
		{
			UniversalId uid = (UniversalId)uidList.get(i);
			if(!uid.getAccountId().equals(getAccountId()))
				throw new WrongAccountException();
				
			localIds[i] = uid.getLocalId();
		}
		NetworkResponse response = getCurrentNetworkInterfaceGateway().deleteServerDraftBulletins(getSecurity(), getAccountId(), localIds);
		return response.getResultCode();
	}
	
	public String putContactInfoOnServer(Vector info)  throws 
			MartusCrypto.MartusSignatureException
	{
		NetworkResponse response = getCurrentNetworkInterfaceGateway().putContactInfo(getSecurity(), getAccountId(), info);
		return response.getResultCode();
	}


	public static class AccountAlreadyExistsException extends Exception {}
	public static class CannotCreateAccountFileException extends IOException {}

	public void createAccount(String userName, String userPassPhrase) throws
					AccountAlreadyExistsException,
					CannotCreateAccountFileException,
					IOException
	{
		createAccountInternal(getKeyPairFile(), userName, userPassPhrase);
	}

	public void writeKeyPairFile(String userName, String userPassPhrase) throws 
		IOException, 
		CannotCreateAccountFileException
	{
		writeKeyPairFileWithBackup(getKeyPairFile(), userName, userPassPhrase);
	}

	public boolean doesAccountExist()
	{
		return getKeyPairFile().exists();
	}
	
	public void exportPublicInfo(File exportFile) throws 
		IOException,
		Base64.InvalidBase64Exception,
		MartusCrypto.MartusSignatureException
	{
			String publicKeyString = security.getPublicKeyString();
			byte[] publicKeyBytes = Base64.decode(publicKeyString);
			ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
			byte[] sigBytes = security.createSignature(in);
			
			UnicodeWriter writer = new UnicodeWriter(exportFile);
			writer.writeln(publicKeyString);
			writer.writeln(Base64.encode(sigBytes));
			writer.close();
	}
	
	public String extractPublicInfo(File file) throws
		IOException,
		Base64.InvalidBase64Exception,
		PublicInformationInvalidException
	{
		UnicodeReader reader = new UnicodeReader(file);
		String publicKey = reader.readLine();
		String signature = reader.readLine();
		reader.close();
		validatePublicInfo(publicKey, signature);
		return publicKey;
	}		

	public File getPublicInfoFile(String fileName)
	{
		fileName = toFileName(fileName);
		String completeFileName = fileName + PUBLIC_INFO_EXTENSION;
		return(new File(getDataDirectory(), completeFileName));
	}

	public boolean attemptSignIn(String userName, String userPassPhrase)
	{
		return attemptSignInInternal(getKeyPairFile(), userName, userPassPhrase);
	}
	
	public String getFieldLabel(String fieldName)
	{
		return localization.getLabel(getCurrentLanguage(), "field", fieldName, "");
	}

	public String getFolderLabel(String folderName)
	{
		if(BulletinFolder.isNameLocalized(folderName))
			return localization.getLabel(getCurrentLanguage(), "folder", folderName, "");
		return folderName;
	}

	public String getLanguageName(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "language", code, "Unknown");
	}

	public String getWindowTitle(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "wintitle", code, "???");
	}

	public String getButtonLabel(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "button", code, "???");
	}

	public String getMenuLabel(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "menu", code, "???");
	}

	public String getMonthLabel(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "month", code, "???");
	}

	public String getMessageLabel(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "message", code, "???");
	}

	public String getStatusLabel(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "status", code, "???");
	}

	public String getKeyword(String code)
	{
		return localization.getLabel(getCurrentLanguage(), "keyword", code, "???");
	}

	public String[] getMonthLabels()
	{
		final String[] tags = {"jan","feb","mar","apr","may","jun",
							"jul","aug","sep","oct","nov","dec"};

		String[] labels = new String[tags.length];
		for(int i = 0; i < labels.length; ++i)
		{
			labels[i] = getMonthLabel(tags[i]);
		}

		return labels;
	}

	public ChoiceItem[] getUiLanguages()
	{
		return localization.getUiLanguages(getTranslationsDirectory());
	}
	
	public String getCurrentLanguage()
	{
		return currentLanguage;
	}

	public void setCurrentLanguage(String languageCode)
	{
		localization.loadTranslationFile(languageCode);
		currentLanguage = languageCode;
	}

	public String getCurrentDateFormatCode()
	{
		return currentDateFormat;
	}

	public void setCurrentDateFormatCode(String code)
	{
		currentDateFormat = code;
	}

	public ChoiceItem[] getLanguageNameChoices(String[] languageCodes)
	{
		if(languageCodes == null)
			return null;
		ChoiceItem[] tempChoicesArray = new ChoiceItem[languageCodes.length];
		for(int i = 0; i < languageCodes.length; i++)
		{
			tempChoicesArray[i] = 
				new ChoiceItem(languageCodes[i], getLanguageName(languageCodes[i]));	
		}
		return tempChoicesArray;
	}
	
	
	public String convertStoredToDisplay(String storedDate)
	{
		DateFormat dfStored = Bulletin.getStoredDateFormat();
		DateFormat dfDisplay = new SimpleDateFormat(getCurrentDateFormatCode());
		String result = "";
		try
		{
			Date d = dfStored.parse(storedDate);
			result = dfDisplay.format(d);
		}
		catch(ParseException e)
		{
			// unparsable dates simply become blank strings,
			// so we don't want to do anything for this exception
			//System.out.println(e);
		}

		return result;
	}

	public static Point center(Dimension inner, Rectangle outer)
	{
		int x = (outer.width - inner.width) / 2;
		int y = (outer.height - inner.height) / 2;
		return new Point(x, y);
	}

	public String getAccountId()
	{
		return store.getAccountId();
	}

	public void createAccountInternal(File keyPairFile, String userName, String userPassPhrase) throws
					AccountAlreadyExistsException,
					CannotCreateAccountFileException,
					IOException
	{
		if(keyPairFile.exists())
			throw(new AccountAlreadyExistsException());
		security.clearKeyPair();
		security.createKeyPair();
		try 
		{
			writeKeyPairFileWithBackup(keyPairFile, userName, userPassPhrase);
			currentUserName = userName;
		} 
		catch(IOException e) 
		{
			security.clearKeyPair();
			throw(e);
		} 
	}

	protected void writeKeyPairFileWithBackup(File keyPairFile, String userName, String userPassPhrase) throws 
		IOException, 
		CannotCreateAccountFileException 
	{
		writeKeyPairFileInternal(keyPairFile, userName, userPassPhrase);
		try
		{
			writeKeyPairFileInternal(getBackupFile(keyPairFile), userName, userPassPhrase);			
		}
		catch (Exception e)
		{
			System.out.println("MartusApp.writeKeyPairFileWithBackup: " + e);
		}
		
	}

	protected void writeKeyPairFileInternal(File keyPairFile, String userName, String userPassPhrase) throws 
		IOException, 
		CannotCreateAccountFileException 
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(keyPairFile);
			security.writeKeyPair(outputStream, getCombinedPassPhrase(userName, userPassPhrase));
			outputStream.close();
		}
		catch(FileNotFoundException e)
		{
			throw(new CannotCreateAccountFileException());
		}
		
	}

	public boolean attemptSignInInternal(File keyPairFile, String userName, String userPassPhrase)
	{
		FileInputStream inputStream = null;
		security.clearKeyPair();
		currentUserName = "";

		try
		{
			inputStream = new FileInputStream(keyPairFile);
		}
		catch(IOException e)
		{
			return false;
		}

		boolean worked = true;
		try
		{
			security.readKeyPair(inputStream, getCombinedPassPhrase(userName, userPassPhrase));
		}
		catch(Exception e)
		{
			worked = false;
		}

		try
		{
			inputStream.close();
		}
		catch(IOException e)
		{
			worked = false;
		}

		if(worked)
			currentUserName = userName;
					
		return worked;
	}

	public String getCombinedPassPhrase(String userName, String userPassPhrase)
	{
		return(userPassPhrase + ":" + userName);
	}

	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public void setSSLNetworkInterfaceHandlerForTesting(NetworkInterface server)
	{
		currentNetworkInterfaceHandler = server;
	}

	private boolean isNonSSLServerAvailable(NetworkInterfaceForNonSSL server)
	{
		String result = server.ping();
		if(result == null)
			return false;

		if(result.indexOf("MartusServer") != 0)
			return false;

		return true;
	}
	
	private boolean isSSLServerAvailable(ClientSideNetworkGateway server)
	{
		NetworkResponse response = server.getServerInfo();
		if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
			return false;

		try
		{
			String version = (String)response.getResultVector().get(0);
			if(version.indexOf("MartusServer") == 0)
				return true;
		}
		catch(Exception e)
		{
			//System.out.println("MartusApp.isSSLServerAvailable: " + e);
		}

		return false;
	}
	
	public ClientSideNetworkGateway getCurrentNetworkInterfaceGateway()
	{
		if(currentNetworkInterfaceGateway == null)
		{
			currentNetworkInterfaceGateway = new ClientSideNetworkGateway(getCurrentNetworkInterfaceHandler());
		}
		
		return currentNetworkInterfaceGateway;
	}
	
	private NetworkInterface getCurrentNetworkInterfaceHandler()
	{
		if(currentNetworkInterfaceHandler == null)
		{
			currentNetworkInterfaceHandler = createXmlRpcNetworkInterfaceHandler();
		}

		return currentNetworkInterfaceHandler;
	}

	private NetworkInterface createXmlRpcNetworkInterfaceHandler() 
	{
		String ourServer = getServerName();
		int ourPort = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_SSL;
		try 
		{
			ClientSideNetworkHandlerUsingXmlRpc handler = new ClientSideNetworkHandlerUsingXmlRpc(ourServer, ourPort);
			handler.getSimpleX509TrustManager().setExpectedPublicKey(getConfigInfo().getServerPublicKey());
			return handler;
		} 
		catch (SSLSocketSetupException e) 
		{
			//TODO propagate to UI and needs a test.
			e.printStackTrace();
			return null;
		}
	}
	
	private void invalidateCurrentHandlerAndGateway()
	{
		currentNetworkInterfaceHandler = null;
		currentNetworkInterfaceGateway = null;
	}
	
	private String getServerName()
	{
		return configInfo.getServerName();
	}

	private static File determineDataDirectory()
	{
		String dir;
		if(System.getProperty("os.name").indexOf("Windows") >= 0)
		{
			dir = "C:/Martus/";
		}
		else
		{
			String userHomeDir = System.getProperty("user.home");
			dir = userHomeDir + "/.Martus/";
		}
		File file = new File(dir);
		if(!file.exists())
		{
			file.mkdirs();
		}
		
		return file;
	}

	static public String toFileName(String text)
	{
		final int maxLength = 20;
		final int minLength = 3;

		if(text.length() > maxLength)
			text = text.substring(0, maxLength);

		char[] chars = text.toCharArray();
		for(int i = 0; i < chars.length; ++i)
		{
			if(!isCharOkInFileName(chars[i]))
				chars[i] = ' ';
		}

		text = new String(chars).trim();
		if(text.length() < minLength)
			text = "Martus-" + text;

		return text;
	}

	static private boolean isCharOkInFileName(char c)
	{
		if(Character.isLetterOrDigit(c))
			return true;
		return false;
	}

	private String createSignature(String stringToSign)
		throws UnsupportedEncodingException, MartusSignatureException 
	{
		MartusCrypto security = getSecurity();
		return MartusUtilities.createSignature(stringToSign, security);
	}

	String dataDirectory;
	private MartusLocalization localization;
	public BulletinStore store;
	private ConfigInfo configInfo;
	public NetworkInterface currentNetworkInterfaceHandler;
	public ClientSideNetworkGateway currentNetworkInterfaceGateway;
	private boolean logUploads;
	public MartusCrypto security;
	private String currentUserName;
	private int maxNewFolders;

	public static final String PUBLIC_INFO_EXTENSION = ".mpi";
	public static final String AUTHENTICATE_SERVER_FAILED = "Failed to Authenticate Server";
	private final int MAXFOLDERS = 50;
	public int serverChunkSize = NetworkInterfaceConstants.MAX_CHUNK_SIZE;
	private String currentLanguage;
	private String currentDateFormat;
}

