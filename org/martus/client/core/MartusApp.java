/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.client.core.ClientSideNetworkHandlerUsingXmlRpc.SSLSocketSetupException;
import org.martus.client.core.Exceptions.ServerCallFailedException;
import org.martus.client.core.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusConstants;
import org.martus.common.MartusUtilities;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.network.NetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkInterfaceForNonSSL;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.search.BulletinSearcher;
import org.martus.common.search.SearchParser;
import org.martus.common.search.SearchTreeNode;
import org.martus.util.Base64;
import org.martus.util.ByteArrayInputStreamWithSeek;
import org.martus.util.FileInputStreamWithSeek;
import org.martus.util.InputStreamWithSeek;


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

	public MartusApp(Localization localizationToUse) throws MartusAppInitializationException
	{
		this(null, determineDataDirectory(), localizationToUse);
	}

	public MartusApp(MartusCrypto cryptoToUse, File dataDirectoryToUse, Localization localizationToUse) throws MartusAppInitializationException
	{
		localization = localizationToUse;
		try
		{
			if(cryptoToUse == null)
				cryptoToUse = new MartusSecurity();

			dataDirectory = dataDirectoryToUse.getPath() + "/";
			security = cryptoToUse;
			store = new BulletinStore(dataDirectoryToUse, cryptoToUse);
			configInfo = new ConfigInfo();

			currentUserName = "";
			maxNewFolders = MAXFOLDERS;
		}
		catch(MartusCrypto.CryptoInitializationException e)
		{
			throw new MartusAppInitializationException("ErrorCryptoInitialization");
		}

		initializeCurrentLanguage(localizationToUse);
	}

	private void initializeCurrentLanguage(Localization localization)
	{
		File languageFlag = new File(getDataDirectory(),"lang.es");
		if(languageFlag.exists())
		{
			languageFlag.delete();
			localization.setCurrentLanguageCode("es");
			localization.setCurrentDateFormatCode(DateUtilities.DMY_SLASH.getCode());
		}
		else
		{
			CurrentUiState previouslySavedState = new CurrentUiState();
			previouslySavedState.load(getUiStateFile());
			String previouslySavedStateLanguage = previouslySavedState.getCurrentLanguage();
			if(previouslySavedStateLanguage == "")
				localization.setCurrentLanguageCode(Localization.ENGLISH);
			else
				localization.setCurrentLanguageCode(previouslySavedStateLanguage);
		
			String previouslySavedStateDateFormat = previouslySavedState.getCurrentDateFormat();
			if(previouslySavedStateDateFormat == "")
				localization.setCurrentDateFormatCode(DateUtilities.getDefaultDateFormatCode());
			else
				localization.setCurrentDateFormatCode(previouslySavedStateDateFormat);
		}
	}
	
	public void enableUploadLogging()
	{
		logUploads = true;
	}

	public void setServerInfo(String serverName, String serverKey, String serverCompliance)
	{
		configInfo.setServerName(serverName);
		configInfo.setServerPublicKey(serverKey);
		configInfo.setServerCompliance(serverCompliance);
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
			byte[] signature = security.createSignatureOfStream(in);
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
			boolean verified = security.isValidSignatureOfStream(security.getPublicKeyString(), inData, signature);
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

	public void doAfterSigninInitalization() throws MartusAppInitializationException
	{
			try
			{
				store.doAfterSigninInitalization();
			}
			catch (MissingAccountMapException e)
			{
				throw new MartusAppInitializationException("ErrorMissingAccountMap");
			}
			catch (FileVerificationException handlingPostponedException)
			{
				throw new MartusAppInitializationException("ErrorAccountMapVerification");
			}
			catch (MissingAccountMapSignatureException handlingPostponedException)
			{
				throw new MartusAppInitializationException("ErrorMissingAccountMapSignature");
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

	public File getDefaultDetailsFile()
	{
		return new File(getDataDirectory(), "DefaultDetails" + DEFAULT_DETAILS_EXTENSION);
	}

	public String getUploadLogFilename()
	{
		return  getDataDirectory() + "MartusUploadLog.txt";
	}

	public String getHelpFilename(String languageCode)
	{
		String helpFile = "MartusHelp-" + languageCode + ".txt";
		return helpFile;
	}

	public String getEnglishHelpFilename()
	{
		return("MartusHelp-en.txt");
	}

	public String getHelpTOCFilename(String languageCode)
	{
		String helpFile = "MartusHelpTOC-" + languageCode + ".txt";
		return helpFile;
	}

	public static File getTranslationsDirectory()
	{
		return determineDataDirectory();
	}

	public File getKeyPairFile()
	{
		return new File(getDataDirectory() + KEYPAIR_FILENAME);
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
		b.set(Bulletin.TAGLANGUAGE, getCurrentLanguage());
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
		String folderName = getNameOfFolderRetrievedSealed();
		return createOrFindFolder(folderName);
	}

	public BulletinFolder createFolderRetrievedFieldOffice()
	{
		String folderName = getNameOfFolderRetrievedFieldOfficeSealed();
		return createOrFindFolder(folderName);
	}

	public String getNameOfFolderRetrievedSealed()
	{
		return store.getNameOfFolderRetrievedSealed();
	}

	public String getNameOfFolderRetrievedDraft()
	{
		return store.getNameOfFolderRetrievedDraft();
	}

	public String getNameOfFolderRetrievedFieldOfficeSealed()
	{
		return store.getNameOfFolderRetrievedFieldOfficeSealed();
	}

	public String getNameOfFolderRetrievedFieldOfficeDraft()
	{
		return store.getNameOfFolderRetrievedFieldOfficeDraft();
	}

	public BulletinFolder createOrFindFolder(String name)
	{
		return store.createOrFindFolder(name);
	}

	public void setMaxNewFolders(int numFolders)
	{
		maxNewFolders = numFolders;
	}

	public BulletinFolder createUniqueFolder(String originalFolderName)
	{
		BulletinFolder newFolder = null;
		String uniqueFolderName = null;
		int folderIndex = 0;
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

	public boolean deleteAllBulletinsAndUserFolders()
	{
		try
		{
			store.deleteAllData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
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

	public BulletinFolder discardBulletinsFromFolder(BulletinFolder folderToDiscardFrom, Bulletin[] bulletinsToDiscard) throws IOException 
	{
		BulletinFolder draftOutBox = getFolderDraftOutbox();
		BulletinFolder discardedFolder = getFolderDiscarded();
		for (int i = 0; i < bulletinsToDiscard.length; i++)
		{
			Bulletin b = bulletinsToDiscard[i];
			draftOutBox.getStore().discardBulletin(draftOutBox, b);
			folderToDiscardFrom.getStore().discardBulletin(folderToDiscardFrom, b);
		}
		return discardedFolder;
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

	public void search(String searchFor, String startDate, String endDate, String andKeyword, String orKeyword)
	{
		SearchParser parser = new SearchParser(andKeyword, orKeyword);
		SearchTreeNode searchNode = parser.parse(searchFor);
		BulletinSearcher matcher = new BulletinSearcher(searchNode, startDate, endDate);

		BulletinFolder searchFolder = createOrFindFolder(store.getSearchFolderName());
		searchFolder.removeAll();

		Vector uids = store.getAllBulletinUids();
		for(int i = 0; i < uids.size(); ++i)
		{
			UniversalId uid = (UniversalId)uids.get(i);
			Bulletin b = store.findBulletinByUniversalId(uid);
			if(matcher.doesMatch(b))
				store.addBulletinToFolder(b.getUniversalId(), searchFolder);
		}
		store.saveFolders();
	}

	public boolean isNonSSLServerAvailable(String serverName)
	{
		if(serverName.length() == 0)
			return false;

		NetworkInterfaceForNonSSL server = new ClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverName);
		return isNonSSLServerAvailable(server);
	}

	public boolean isSSLServerAvailable()
	{
		if(currentNetworkInterfaceHandler == null && getServerName().length() == 0)
			return false;

		return isSSLServerAvailable(getCurrentNetworkInterfaceGateway());
	}

	public ClientSideNetworkGateway buildGateway(String serverName, String serverPublicKey)
	{
		NetworkInterface server = buildNetworkInterface(serverName, serverPublicKey);
		if(server == null)
			return null;
		
		return new ClientSideNetworkGateway(server);
	}

	NetworkInterface buildNetworkInterface(String serverName, String serverPublicKey)
	{
		if(serverName.length() == 0)
			return null;
	
		try
		{
			int[] ports = NetworkInterfaceXmlRpcConstants.defaultSSLPorts;
			ClientSideNetworkHandlerUsingXmlRpc handler = new ClientSideNetworkHandlerUsingXmlRpc(serverName, ports);
			handler.getSimpleX509TrustManager().setExpectedPublicKey(serverPublicKey);
			return handler;
		}
		catch (SSLSocketSetupException e)
		{
			//TODO propagate to UI and needs a test.
			e.printStackTrace();
			return null;
		}
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
			return MartusCrypto.computePublicCode(getServerPublicKey(serverName));
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
		NetworkInterfaceForNonSSL server = new ClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverName);
		return getServerPublicKey(server);
	}

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
		MartusUtilities.validatePublicInfo(accountId, sig, security);
		return accountId;
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

	public Vector getNewsFromServer()
	{
		if(!isSSLServerAvailable())
			return new Vector();

		try
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getNews(security);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return response.getResultVector();
		}
		catch (MartusSignatureException e)
		{
			System.out.println("MartusApp.sendContactInfoToServer :" + e);
		}
		return new Vector();
	}

	public String getServerCompliance(ClientSideNetworkGateway gateway) 
		throws ServerCallFailedException, ServerNotAvailableException
	{
		if(!isSSLServerAvailable(gateway))
			throw new ServerNotAvailableException();
		try
		{
			NetworkResponse response = gateway.getServerCompliance(security);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return (String)response.getResultVector().get(0);
		}
		catch (Exception e)
		{
			//System.out.println("MartusApp.getServerCompliance :" + e);
			throw new ServerCallFailedException();
		}		
		throw new ServerCallFailedException();
	}

	void moveBulletinToDamaged(BulletinFolder outbox, UniversalId uid)
	{
		System.out.println("Moving bulletin to damaged");
		BulletinFolder damaged = createOrFindFolder(store.getNameOfFolderDamaged());
		Bulletin b = store.findBulletinByUniversalId(uid);
		store.moveBulletin(b, outbox, damaged);
		store.saveFolders();
	}

	public static class DamagedBulletinException extends Exception
	{
		public DamagedBulletinException(String message)
		{
			super(message);
		}
	}

	public Vector getMyServerBulletinSummaries() throws ServerErrorException
	{
		if(!isSSLServerAvailable())
			throw new ServerErrorException("No server");

		String resultCode = "?";
		try
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getSealedBulletinIds(security, getAccountId(), MartusUtilities.getRetrieveBulletinSummaryTags());
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
		if(!isSSLServerAvailable())
			throw new ServerErrorException("No server");

		String resultCode = "?";
		try
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getDraftBulletinIds(security, getAccountId(), MartusUtilities.getRetrieveBulletinSummaryTags());
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

	public BulletinSummary retrieveSummaryFromString(String accountId, String parameters)
		throws ServerErrorException
	{
		FieldDataPacket fdp = null;
		String args[] = parameters.split(MartusConstants.regexEqualsDelimeter, -1);
		if(args.length != 3)
			throw new ServerErrorException("MartusApp.createSummaryFromString: " + parameters);
		String bulletinLocalId= args[0];
		String packetlocalId = args[1];
		int size = Integer.parseInt(args[2]);

		if(!FieldDataPacket.isValidLocalId(packetlocalId))
			throw new ServerErrorException();

		UniversalId uId = UniversalId.createFromAccountAndLocalId(accountId, bulletinLocalId);
		Bulletin bulletin = store.findBulletinByUniversalId(uId);
		if (bulletin != null)
			fdp = bulletin.getFieldDataPacket();

		try
		{
			if(fdp == null)
				fdp = retrieveFieldDataPacketFromServer(accountId, bulletinLocalId, packetlocalId);
		}
		catch(Exception e)
		{
			//System.out.println("MartusApp.createSummaryFromString: " + e);
			//e.printStackTrace();
			throw new ServerErrorException();
		}
		BulletinSummary bulletinSummary = new BulletinSummary(accountId, bulletinLocalId, fdp, size);
		return bulletinSummary;
	}

	public Vector downloadFieldOfficeAccountIds() throws ServerErrorException
	{
		if(!isSSLServerAvailable())
			throw new ServerErrorException();

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

	public void retrieveOneBulletinToFolder(UniversalId uid, BulletinFolder retrievedFolder, ProgressMeterInterface progressMeter) throws
		Exception
	{
		File tempFile = File.createTempFile("$$$MartusApp", null);
		tempFile.deleteOnExit();
		FileOutputStream outputStream = new FileOutputStream(tempFile);

		int masterTotalSize = BulletinZipUtilities.retrieveBulletinZipToStream(uid, outputStream,
						serverChunkSize, getCurrentNetworkInterfaceGateway(),  security,
						progressMeter);

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
		MartusUtilities.exportClientPublicKey(security, exportFile);
	}

	public String extractPublicInfo(File file) throws
		IOException,
		Base64.InvalidBase64Exception,
		PublicInformationInvalidException
	{
		Vector importedPublicKeyInfo = MartusUtilities.importClientPublicKeyFromFile(file);
		String publicKey = (String) importedPublicKeyInfo.get(0);
		String signature = (String) importedPublicKeyInfo.get(1);
		MartusUtilities.validatePublicInfo(publicKey, signature, security);
		return publicKey;
	}

	public File getPublicInfoFile(String fileName)
	{
		fileName = MartusUtilities.toFileName(fileName);
		String completeFileName = fileName + PUBLIC_INFO_EXTENSION;
		return(new File(getDataDirectory(), completeFileName));
	}

	public boolean attemptSignIn(String userName, String userPassPhrase)
	{
		return attemptSignInInternal(getKeyPairFile(), userName, userPassPhrase);
	}

	private String getCurrentLanguage()
	{
		return localization.getCurrentLanguageCode();
	}



	public String getAccountId()
	{
		return store.getAccountId();
	}
	
	public boolean isOurBulletin(Bulletin b)
	{
		return getAccountId().equals(b.getAccount());	
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

	public boolean isSSLServerAvailable(ClientSideNetworkGateway server)
	{
		try
		{
			NetworkResponse response = server.getServerInfo();
			if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return false;

			String version = (String)response.getResultVector().get(0);
			if(version.indexOf("MartusServer") == 0)
				return true;
		}
		catch(Exception notInterestingBecauseTheServerMightJustBeDown)
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
		String ourServerPublicKey = getConfigInfo().getServerPublicKey();
		return buildNetworkInterface(ourServer,ourServerPublicKey);
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

	protected String dataDirectory;
	private Localization localization;
	public BulletinStore store;
	private ConfigInfo configInfo;
	public NetworkInterface currentNetworkInterfaceHandler;
	public ClientSideNetworkGateway currentNetworkInterfaceGateway;
	boolean logUploads;
	public MartusCrypto security;
	private String currentUserName;
	private int maxNewFolders;

	public static final String PUBLIC_INFO_EXTENSION = ".mpi";
	public static final String DEFAULT_DETAILS_EXTENSION = ".txt";
	public static final String AUTHENTICATE_SERVER_FAILED = "Failed to Authenticate Server";
	public static final String KEYPAIR_FILENAME = "MartusKeyPair.dat";

	private final int MAXFOLDERS = 50;
	public int serverChunkSize = NetworkInterfaceConstants.MAX_CHUNK_SIZE;
}

