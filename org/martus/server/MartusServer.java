package org.martus.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.AttachmentPacket;
import org.martus.common.Base64;
import org.martus.common.BulletinConstants;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceForNonSSL;
import org.martus.common.NetworkInterfaceXmlRpcConstants;
import org.martus.common.Packet;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.ZipEntryInputStream;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.CryptoException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusCrypto.NoKeyPairException;
import org.martus.common.MartusUtilities.FileTooLargeException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;
import org.martus.common.Packet.WrongPacketTypeException;

public class MartusServer
{

	public static void main(String[] args)
	{
		System.out.println("MartusServer");

		File dataDirectory = getDataDirectory();		
		
		Database diskDatabase = null;
		try
		{
			diskDatabase = new ServerFileDatabase(new File(dataDirectory, "packets"));
		}
		catch(FileDatabase.MissingAccountMapException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map File");
			System.exit(7);
		}
		
		MartusServer server = null;
		boolean secureMode = false;
		for(int arg = 0; arg < args.length; ++arg)
		{
			if (args[arg].indexOf("logging")>=0)
			{
				serverLogging = true;
				if (args[arg].indexOf("max")>=0)
				{
					serverMaxLogging = true;
					serverSSLLogging = true;
					System.out.println("Server Error Logging set to Max");
				}
				else
					System.out.println("Server Error Logging Enabled");
			}
			
			if(args[arg].equals("secure"))
				secureMode = true;
		}
		
		if(secureMode)
			System.out.println("Running in SECURE mode");
		else
			System.out.println("***RUNNING IN INSECURE MODE***");
		
		System.out.println("Initializing...this will take a few seconds...");
		try
		{
			server = new MartusServer(diskDatabase, dataDirectory);
		} 
		catch(CryptoInitializationException e) 
		{
			System.out.println("Crypto Initialization Exception" + e);
			System.exit(1);			
		}

		String versionInfo = MartusUtilities.getVersionDate(server.getClass());
		System.out.println("Version " + versionInfo);

		System.out.print("Enter passphrase: ");
		System.out.flush();

		File waitingFile = new File(dataDirectory, "waiting");
		waitingFile.delete();
		writeSyncFile(waitingFile);

		InputStreamReader rawReader = new InputStreamReader(System.in);	
		BufferedReader reader = new BufferedReader(rawReader);
		try
		{
			String passphrase = reader.readLine();
			if(server.hasAccount())
			{
				server.loadAccount(passphrase);
			}
			else
			{
				System.out.println("***** Key pair file not found *****");
				System.out.print("Create a new account now (y/N)?");
				System.out.flush();
				String response = reader.readLine();
				if(response.toLowerCase().startsWith("y"))
				{
					System.out.println("Creating account (this will take a while)...");
					server.createAccount(passphrase);
					System.out.println("Account created");
				}
				else
				{
					System.out.println("Account not created.");
					System.exit(2);
				}
			}

			String accountId = server.getAccountId();
			System.out.println("Server Account: " + accountId);
			System.out.println();

			System.out.print("Server Public Code: ");
			String publicCode = MartusUtilities.computePublicCode(accountId);
			System.out.println(MartusUtilities.formatPublicCode(publicCode));
			System.out.println();
		}
		catch(Exception e)
		{
			System.out.println("MartusServer.main: " + e);
			System.exit(3);
		}
		
		File runningFile = new File(dataDirectory, "running");
		runningFile.delete();
		if(secureMode)
		{
			File magicWordsFile = new File(dataDirectory, "magicwords.txt");
			if(!magicWordsFile.delete())
			{
				System.out.println("Unable to delete magicwords");
				System.exit(4);
			}
			File keyPairFile = new File(dataDirectory, "keypair.dat");
			if(!keyPairFile.delete())
			{
				System.out.println("Unable to delete keypair");
				System.exit(5);
			}
		}
				
		System.out.println();

		System.out.println(server.clientsThatCanUpload.size() + " clients currently allowed to upload");

		server.createNonSSLXmlRpcServer();
		server.createSSLXmlRpcServer();
		writeSyncFile(runningFile);
		System.out.println("Waiting for connection...");
	}


	public static void writeSyncFile(File syncFile) 
	{
		try 
		{
			FileOutputStream out = new FileOutputStream(syncFile);
			out.write(0);
			out.close();
		} 
		catch(Exception e) 
		{
			System.out.println("MartusServer.main: " + e);
			System.exit(6);
		}
	}


	MartusServer(Database databaseToUse, File dataDirectory) throws 
					MartusCrypto.CryptoInitializationException
	{
		database = databaseToUse;
		security = new MartusSecurity();
		
		nonSSLServerHandler = new ServerSideNetworkHandlerForNonSSL(this);
		serverHandler = new ServerSideNetworkHandler(this);
		

		clientsThatCanUpload = new Vector();
		allowUploadFile = new File(dataDirectory, "uploadsok.txt");
		magicWordsFile = new File(dataDirectory, "magicwords.txt");
		keyPairFile = new File(dataDirectory, "keypair.dat");

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(allowUploadFile));
			loadCanUploadList(reader);
			reader.close();
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
		}
		catch(IOException e)
		{
			// TODO: Log this so the administrator knows
			System.out.println("MartusServer constructor: " + e);
		}
		
		try
		{
			UnicodeReader reader = new UnicodeReader(magicWordsFile);
			String line = reader.readLine();
			if(line != null)
				setMagicWord(line);
			reader.close();
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
		}
		catch(IOException e)
		{
			// TODO: Log this so the administrator knows
			System.out.println("MartusServer constructor: " + e);
		}
	}
	
	public Database getDatabase()
	{
		return database;
	}
	
	NetworkInterfaceForNonSSL getNonSSLServerHandler()
	{
		return nonSSLServerHandler;
	}
	
	NetworkInterface getServerHandler()
	{
		return serverHandler;
	}
	
	boolean hasAccount()
	{
		return keyPairFile.exists();
	}
	
	void createAccount(String passphrase) throws IOException
	{
		security.createKeyPair();
		FileOutputStream out = new FileOutputStream(keyPairFile);
		writeKeyPair(out, passphrase);
		out.close();
	}
	
	void loadAccount(String passphrase) throws Exception
	{
		FileInputStream in = new FileInputStream(keyPairFile);
		readKeyPair(in, passphrase);
		in.close();
	}
	
	public String getAccountId()
	{
		return security.getPublicKeyString();
	}
	
	public void setMagicWord(String newMagicWord)
	{
		magicWord = newMagicWord;
	}
	
	public void createNonSSLXmlRpcServer()
	{
		XmlRpcServer.createNonSSLXmlRpcServer(getNonSSLServerHandler(), NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_NON_SSL);
	}

	public void createSSLXmlRpcServer()
	{
		int port = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_SSL;
		createSSLXmlRpcServerOnPort(port);
	}

	public void createSSLXmlRpcServerOnPort(int port) 
	{
		MartusSecureWebServer.security = security;
		XmlRpcServer.createSSLXmlRpcServer(getServerHandler(), port);
	}

	public String ping()
	{
		if(serverMaxLogging)
			logging("ping request");		
		return NetworkInterfaceConstants.VERSION;
	}

	public Vector getServerInformation()
	{
		if(serverMaxLogging)
			logging("getServerInformation");
		Vector result = new Vector();
		try
		{
			String publicKeyString = security.getPublicKeyString();
			byte[] publicKeyBytes = Base64.decode(publicKeyString);
			ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
			byte[] sigBytes = security.createSignature(in);
			
			result.add(NetworkInterfaceConstants.OK);
			result.add(publicKeyString);
			result.add(Base64.encode(sigBytes));
			if(serverMaxLogging)
				logging("getServerInformation : Exit OK");
		}
		catch(Exception e)
		{
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			result.add(e.toString());
			logging("getServerInformation SERVER ERROR" + e);			
		}
		return result;
	}
	
	public String requestUploadRights(String clientId, String tryMagicWord)
	{
		if(!tryMagicWord.equals(magicWord))
		{
			logging("requestUploadRights: Rejected " + getPublicCode(clientId) + "magicWord=" + magicWord + " tryMagicWord=" +tryMagicWord);
			return NetworkInterfaceConstants.REJECTED;
		}
		if(serverMaxLogging)
			logging("requestUploadRights granted to :" + clientId);			
		allowUploads(clientId);
		return NetworkInterfaceConstants.OK;
	}
	
	
	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		if(serverMaxLogging)
			logging("uploadBulletin " + getFolderFromClientId(authorAccountId) + " " + bulletinLocalId);

		if(!canClientUpload(authorAccountId))
		{
			logging("uploadBulletin REJECTED (!canClientUpload)");
			return NetworkInterfaceConstants.REJECTED;
		}
		
		File tempFile = null;
		try 
		{
			tempFile = Base64.decodeToTempFile(data);
		} 
		catch(Exception e)
		{
			//System.out.println("MartusServer.uploadBulletin: " + e);
			logging("uploadBulletin INVALID_DATA " + e);
			return NetworkInterfaceConstants.INVALID_DATA;
		}
		String result = saveUploadedBulletinZipFile(authorAccountId, tempFile);
		tempFile.delete();

		if(serverMaxLogging)
			logging("uploadBulletin : Exit " + result);
		return result;
	}


	
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		if(serverMaxLogging)
		{
			logging("uploadBulletinChunk");
		}
		
		String signedString = authorAccountId + "," + bulletinLocalId + "," +
					Integer.toString(totalSize) + "," + Integer.toString(chunkOffset) + "," +
					Integer.toString(chunkSize) + "," + data;
		if(!isSignatureCorrect(signedString, signature, authorAccountId))
		{
			logging("  returning SIG_ERROR");
			return NetworkInterfaceConstants.SIG_ERROR;
		}
		
		String result = putBulletinChunk(authorAccountId, authorAccountId, bulletinLocalId,
									chunkOffset, chunkSize, totalSize, data);
		return result;
	}


	public String putBulletinChunk(String uploaderAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int chunkSize, int totalSize, String data) 
	{
		if(serverMaxLogging)
		{
			logging("putBulletinChunk");
			logging("  " + getFolderFromClientId(authorAccountId) + " " + bulletinLocalId);
			logging("  Total Size=" + totalSize + ", Offset=" + chunkOffset);
			if(chunkSize != NetworkInterfaceConstants.MAX_CHUNK_SIZE)
				logging("Last Chunk = " + chunkSize);
		}
		
		if(!canClientUpload(authorAccountId))
		{
			logging("putBulletinChunk REJECTED (!canClientUpload)");
			return NetworkInterfaceConstants.REJECTED;
		}
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey key = new DatabaseKey(uid);
		File interimZipFile;
		try 
		{
			interimZipFile = getDatabase().getIncomingInterimFile(key);
		} 
		catch (IOException e) 
		{
			logging("putBulletinChunk Error creating interim file." + e.getMessage());
			return NetworkInterfaceConstants.SERVER_ERROR;
		}
		
		if(chunkSize > NetworkInterfaceConstants.MAX_CHUNK_SIZE)
		{
			interimZipFile.delete();
			logging("putBulletinChunk INVALID_DATA (> MAX_CHUNK_SIZE)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}			
		
		if(chunkOffset == 0)
		{
			if(serverMaxLogging && interimZipFile.exists())
				logging("putBulletinChunk : restarting at zero");
			interimZipFile.delete();
		}
		
		double oldFileLength = interimZipFile.length();
		if(oldFileLength != chunkOffset)
		{
			interimZipFile.delete();
			logging("putBulletinChunk INVALID_DATA (!= file length)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}
		
		if(oldFileLength + chunkSize > totalSize)
		{
			interimZipFile.delete();
			logging("putBulletinChunk INVALID_DATA (> totalSize)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}			
		
		StringReader reader = null;
		FileOutputStream out = null;
		try 
		{
			reader = new StringReader(data);
			out = new FileOutputStream(interimZipFile.getPath(), true);
			Base64.decode(reader, out);
			out.close();
			reader.close();
		} 
		catch(Exception e)
		{
			try 
			{
				if(out != null)
					out.close();
			} 
			catch (IOException nothingWeCanDo) 
			{
			}
			if(reader != null)
				reader.close();
			interimZipFile.delete();
			logging("putBulletinChunk INVALID_DATA " + e);
			return NetworkInterfaceConstants.INVALID_DATA;
		}
		
		String result = NetworkInterfaceConstants.CHUNK_OK;
		double newFileLength = interimZipFile.length();
		if(chunkSize != newFileLength - oldFileLength)
		{
			interimZipFile.delete();
			logging("putBulletinChunk INVALID_DATA (chunkSize != actual dataSize)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}			
		
		if(newFileLength >= totalSize)
		{
			if(serverMaxLogging)
				logging("entering saveUploadedBulletinZipFile");
			try 
			{
				result = saveUploadedBulletinZipFile(authorAccountId, interimZipFile);
			} catch (Exception e) 
			{
				if(serverMaxLogging)
					logging("Exception =" + e);
				e.printStackTrace();
			}
			if(serverMaxLogging)
				logging("returned from saveUploadedBulletinZipFile result =" + result);
			interimZipFile.delete();
		}
		
		if(serverMaxLogging)
			logging("putBulletinChunk : Exit " + result);
		return result;
	}


	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		if(serverMaxLogging)
			logging("downloadBulletin " + getFolderFromClientId(authorAccountId) + " " + bulletinLocalId);
		Vector result = new Vector();
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = new DatabaseKey(uid);
		if(!getDatabase().doesRecordExist(headerKey))
		{
			logging("downloadBulletin NOT_FOUND");
			result.add(NetworkInterfaceConstants.NOT_FOUND);
		}
		else
		{
			try
			{
				File tempFile = createInterimBulletinFile(headerKey);
				//TODO: if file is bigger than one chunk, should return an error here!
				
				StringWriter writer = new StringWriter();
				FileInputStream in = new FileInputStream(tempFile);
				Base64.encode(in, writer);
				in.close();
				String zipString = writer.toString();
	
				tempFile.delete();
				result.add(NetworkInterfaceConstants.OK);
				result.add(zipString);
				if(serverMaxLogging)
					logging("downloadBulletin : Exit OK");
			}
			catch(Exception e)
			{
				logging("downloadBulletin SERVER_ERROR " + e);
				//System.out.println("MartusServer.download: " + e);
				result.add(NetworkInterfaceConstants.SERVER_ERROR);
			}
		}

		return result;
	}

	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature)
	{
		if(serverMaxLogging)
		{
			logging("downloadFieldOfficeBulletinChunk ");
			logging("  " + getFolderFromClientId(authorAccountId) + " " + bulletinLocalId);
			logging("  Offset=" + chunkOffset + ", Max=" + maxChunkSize + "HQ: " + getFolderFromClientId(hqAccountId));
		}
		Vector result = new Vector();
		
		String signedString = authorAccountId + "," + bulletinLocalId + "," + hqAccountId + "," + 
					Integer.toString(chunkOffset) + "," +
					Integer.toString(maxChunkSize);
		if(!isSignatureCorrect(signedString, signature, hqAccountId))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);
			return result;
		}

		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = new DatabaseKey(uid);
		if(!getDatabase().doesRecordExist(headerKey))
		{
			logging("  NOT_FOUND");
			result.add(NetworkInterfaceConstants.NOT_FOUND);
			return result;
		}
		
		try
		{
			String bulletinHqAccountId = getBulletinHQAccountId(headerKey);
			if(!bulletinHqAccountId.equals(hqAccountId))
			{
				logging("  WRONG HQ (NOTYOURBULLETIN)");
				result.add(NetworkInterfaceConstants.NOTYOURBULLETIN);
				return result;
			}

			result = buildBulletinChunkResponse(headerKey, chunkOffset, maxChunkSize);
		}
		catch(Exception e)
		{
			logging("  SERVER_ERROR " + e);
			//System.out.println("MartusServer.download: " + e);
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
		}
		
		return result;
	}

	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature)
	{
		if(serverMaxLogging)
		{
			logging("downloadMyBulletinChunk ");
			logging("  " + getFolderFromClientId(authorAccountId) + " " + bulletinLocalId);
			logging("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
		}
		
		String signedString = authorAccountId + "," + bulletinLocalId + "," +
					Integer.toString(chunkOffset) + "," +
					Integer.toString(maxChunkSize);

		boolean gotValidSignature = isSignatureCorrect(signedString, signature, authorAccountId);

		// TODO: This is only needed to support the Guatemala clients. It should be removed
		// after all those have been updated to newer software!
		if(!gotValidSignature)
		{
			logging("  initial sig verify failed (will try legacy sig)");

			String legacySignedString = authorAccountId + "," + bulletinLocalId + "," +
						"0" + "," + Integer.toString(chunkOffset) + "," +
						Integer.toString(maxChunkSize);
			gotValidSignature = isSignatureCorrect(legacySignedString, signature, authorAccountId);
		}
		
		// TODO: This is only needed to support the Guatemala HQ's. It should be removed
		// after all those have been updated to newer software!
		if(!gotValidSignature)
		{
			logging("  legacy client sig verify also failed (will try legacy hq)");

			try
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
				DatabaseKey headerKey = new DatabaseKey(uid);
				String bulletinHqAccountId = getBulletinHQAccountId(headerKey);
				gotValidSignature = isSignatureCorrect(signedString, signature, bulletinHqAccountId);
			}
			catch(Exception e)
			{
				logging("  checking legacy hq sig threw: " + e);
			}
		}
		
		// TODO: This is only needed to support the Guatemala clients. It should be removed
		// after all those have been updated to newer software!
		if(!gotValidSignature)
		{
			logging("  legacy hq verify failed (will try legacy chunk sig)");

			try
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
				DatabaseKey headerKey = new DatabaseKey(uid);
				File tempFile = createInterimBulletinFile(headerKey);
				int totalLength = MartusUtilities.getCappedFileLength(tempFile);
				String legacySignedString = authorAccountId + "," + bulletinLocalId + "," +
							Integer.toString(totalLength) + "," + Integer.toString(chunkOffset) + "," +
							Integer.toString(maxChunkSize);
				gotValidSignature = isSignatureCorrect(legacySignedString, signature, authorAccountId);
			}
			catch(Exception e)
			{
				logging("  checking legacy client chunk sig threw: " + e);
			}
		}
		
		if(!gotValidSignature)
		{
			logging("  all attempts to verify sig have failed!");
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.SIG_ERROR);
			return result;
		}

		Vector result = getBulletinChunk(authorAccountId, authorAccountId, bulletinLocalId, 
							chunkOffset, maxChunkSize);

		if(serverMaxLogging)
			logging("downloadMyBulletinChunk: Exit");
		return result;
	}


	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int maxChunkSize) 
	{
		if(serverMaxLogging)
		{
			logging("getBulletinChunk request by " + getFolderFromClientId(myAccountId));
			logging("  " + getFolderFromClientId(authorAccountId) + " " + bulletinLocalId);
			logging("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
		}
		
		Vector result = new Vector();
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = new DatabaseKey(uid);
		headerKey.setSealed();
		if(!getDatabase().doesRecordExist(headerKey))
			headerKey.setDraft();

		if(!getDatabase().doesRecordExist(headerKey))
		{
			logging("  NOT_FOUND");
			result.add(NetworkInterfaceConstants.NOT_FOUND);
			return result;
		}
		
		try
		{
			result = buildBulletinChunkResponse(headerKey, chunkOffset, maxChunkSize);
		}
		catch(Exception e)
		{
			logging("  SERVER_ERROR " + e);
			//System.out.println("MartusServer.download: " + e);
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
		}
		
		if(serverMaxLogging)
			logging("  exit: " + result.get(0));
		return result;
	}

	public Vector listMySealedBulletinIds(String clientId)
	{
		if(serverMaxLogging)
			logging("listMySealedBulletinIds " + getFolderFromClientId(clientId));
		SummaryCollector summaryCollector = new MySealedSummaryCollector(getDatabase(), clientId);
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("listMySealedBulletinIds : Exit");
		return summaries;
	}
	
	public Vector listMyDraftBulletinIds(String authorAccountId)
	{
		if(serverMaxLogging)
			logging("listMyDraftBulletinIds " + getFolderFromClientId(authorAccountId));
		SummaryCollector summaryCollector = new MyDraftSummaryCollector(getDatabase(), authorAccountId);
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("listMyDraftBulletinIds : Exit");
		return summaries;
	}

	public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId)
	{
		if(serverMaxLogging)
			logging("listFieldOfficeSealedBulletinIds " + getFolderFromClientId(hqAccountId));
		SummaryCollector summaryCollector = new FieldOfficeSealedSummaryCollector(getDatabase(), hqAccountId, authorAccountId);
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("listFieldOfficeSealedBulletinIds : Exit");
		return summaries;	
	}

	public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId)
	{
		if(serverMaxLogging)
			logging("listFieldOfficeDraftBulletinIds " + getFolderFromClientId(hqAccountId));
		SummaryCollector summaryCollector = new FieldOfficeDraftSummaryCollector(getDatabase(), hqAccountId, authorAccountId);
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("listFieldOfficeDraftBulletinIds : Exit");
		return summaries;
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{

		class FieldOfficeAccountCollector implements Database.PacketVisitor
		{
			FieldOfficeAccountCollector(String hqAccountIdToUse)
			{
				hqAccountId = hqAccountIdToUse;
				accounts = new Vector();
				accounts.add(NetworkInterfaceConstants.OK);
			}
			
			public void visit(DatabaseKey key)
			{
				if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
					return;
				try
				{
					BulletinHeaderPacket bhp = loadBulletinHeaderPacket(getDatabase(), key);
					if(bhp.getHQPublicKey().equals(hqAccountId))
					{
						String packetAccountId = bhp.getAccountId();
						if(!accounts.contains(packetAccountId))
							accounts.add(packetAccountId);
					}
				}
				catch(Exception e)
				{
					logging("FieldOfficeAccountCollector:Visit " + e);
					accounts.set(0, NetworkInterfaceConstants.SERVER_ERROR);
				}
			}
			
			public Vector getAccounts()
			{
				return accounts;
			}
			String hqAccountId;
			Vector accounts;
		}	

		if(serverMaxLogging)
			logging("listFieldOfficeAccounts " + getFolderFromClientId(hqAccountId));

		FieldOfficeAccountCollector visitor = new FieldOfficeAccountCollector(hqAccountId);
		getDatabase().visitAllRecords(visitor);
	
		if(serverMaxLogging)
			logging("listFieldOfficeAccounts : Exit");
		return visitor.getAccounts();	
	}

	public Vector legacyDownloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		//TODO reject requests for other accounts public packets
		if(serverMaxLogging)
			logging("downloadAuthorizedPacket: " + getFolderFromClientId(myAccountId));
	
		String signedString = authorAccountId + "," + packetLocalId + "," + myAccountId;
		if(!isSignatureCorrect(signedString, signature, myAccountId))
		{
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.SIG_ERROR);
			return result;
		}
		
		return	legacyDownloadPacket(authorAccountId, packetLocalId);
	}

	public Vector legacyDownloadPacket(String clientId, String packetId)
	{
		if(serverMaxLogging)
			logging("legacyDLPacket " + getFolderFromClientId(clientId) + ": " + packetId);
		Vector result = new Vector();
		
		if(AttachmentPacket.isValidLocalId(packetId))
		{
			logging("legacyDLPacket invalid for attachments");
			result.add(NetworkInterfaceConstants.INVALID_DATA);
			return result;
		}

		Database db = getDatabase();
		UniversalId uid = UniversalId.createFromAccountAndLocalId(clientId, packetId);
		DatabaseKey key = new DatabaseKey(uid);
		if(db.doesRecordExist(key))
		{
			try
			{
				String packetXml = db.readRecord(key, security);
				result.add(NetworkInterfaceConstants.OK);
				result.add(packetXml);
			}
			catch(Exception e)
			{
				//TODO: Make sure this has a test!
				logging("legacyDLPacket " + e);
				result.clear();
				result.add(NetworkInterfaceConstants.SERVER_ERROR);
			}
		}
		else
		{
			result.add(NetworkInterfaceConstants.NOT_FOUND);
			logging("legacyDLPacket NotFound: " + getFolderFromClientId(clientId) + " : " + packetId);
		}
		return result;
	}
	
	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		if(serverMaxLogging)
		{
			logging("downloadFieldOfficeDataPacket: " + getFolderFromClientId(authorAccountId) + "  " + bulletinLocalId);
			logging("  packet " + packetLocalId + " requested by: " + getFolderFromClientId(myAccountId));
		}
	
		Vector result = new Vector();

		String signedString = authorAccountId + "," + bulletinLocalId + "," + packetLocalId + "," + myAccountId;
		if(!isSignatureCorrect(signedString, signature, myAccountId))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);
			return result;
		}
		
		result = getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
		
		if(serverMaxLogging)
			logging("downloadFieldDataPacket: Exit");
		return result;
	}


	public Vector getPacket(String myAccountId, String authorAccountId, String bulletinLocalId,
		String packetLocalId) 
	{
		Vector result = new Vector();
		
		if(!FieldDataPacket.isValidLocalId(packetLocalId))
		{
			logging("  attempt to download non-fielddatapacket: " + packetLocalId);
			result.add(NetworkInterfaceConstants.INVALID_DATA);
			return result;
		}
		
		Database db = getDatabase();
		
		UniversalId headerUid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = new DatabaseKey(headerUid);
		headerKey.setSealed();
		
		if(!db.doesRecordExist(headerKey))
			headerKey.setDraft();
		
		if(!db.doesRecordExist(headerKey))
		{
			logging("  header packet not found");
			result.add(NetworkInterfaceConstants.NOT_FOUND);
			return result;
		}
		
		UniversalId dataPacketUid = UniversalId.createFromAccountAndLocalId(authorAccountId, packetLocalId);
		DatabaseKey dataPacketKey = new DatabaseKey(dataPacketUid);
		if(headerKey.isDraft())
			dataPacketKey.setDraft();
		else
			dataPacketKey.setSealed();
			
		if(!db.doesRecordExist(dataPacketKey))
		{
			logging("  data packet not found");
			result.add(NetworkInterfaceConstants.NOT_FOUND);
			return result;
		}
		
		try
		{
			if(!myAccountId.equals(authorAccountId) && 
					!myAccountId.equals(getBulletinHQAccountId(headerKey)) )
			{
				logging("  neither author nor HQ account");
				result.add(NetworkInterfaceConstants.NOTYOURBULLETIN);
				return result;
			}
			
			String packetXml = db.readRecord(dataPacketKey, security);
		
			result.add(NetworkInterfaceConstants.OK);
			result.add(packetXml);
			return result;
		}
		catch(Exception e)
		{
			//TODO: Make sure this has a test!
			logging("  error loading " + e);
			result.clear();
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			return result;
		}
	}

	public String authenticateServer(String tokenToSign)
	{
		if(serverMaxLogging)
			logging("authenticateServer");
		try 
		{
			InputStream in = new ByteArrayInputStream(Base64.decode(tokenToSign));
			byte[] sig = security.createSignature(in);
			return Base64.encode(sig);
		} 
		catch(MartusSignatureException e) 
		{
			if(serverMaxLogging)
				logging("SERVER_ERROR: " + e);
			return NetworkInterfaceConstants.SERVER_ERROR;
		} 
		catch(InvalidBase64Exception e) 
		{
			if(serverMaxLogging)
				logging("INVALID_DATA: " + e);
			return NetworkInterfaceConstants.INVALID_DATA;
		}
	}
	
	// end MartusServerInterface interface

	public boolean canClientUpload(String clientId)
	{
		return clientsThatCanUpload.contains(clientId);
	}

	public synchronized void allowUploads(String clientId)
	{
		if(serverMaxLogging)
			logging("allowUploads " + getFolderFromClientId(clientId) + " : " + getPublicCode(clientId));
		clientsThatCanUpload.add(clientId);
		
		try
		{
			UnicodeWriter writer = new UnicodeWriter(allowUploadFile);
			for(int i = 0; i < clientsThatCanUpload.size(); ++i)
			{
				writer.writeln((String)clientsThatCanUpload.get(i));
			}
			writer.close();
			if(serverMaxLogging)
				logging("allowUploads : Exit OK");
		}
		catch(IOException e)
		{
			logging("allowUploads " + e);
			//System.out.println("MartusServer.allowUploads: " + e);
		}
	}

	public String getPublicCode(String clientId) 
	{
		String formattedCode = "";
		try 
		{
			String publicCode = MartusUtilities.computePublicCode(clientId);
			formattedCode = MartusUtilities.formatPublicCode(publicCode);
		} 
		catch(InvalidBase64Exception e) 
		{
		}
		return formattedCode;
	}

	public synchronized void loadCanUploadList(BufferedReader canUploadInput)
	{
		if(serverMaxLogging)
			logging("loadCanUploadList");
		try
		{
			while(true)
			{
				String currentLine = canUploadInput.readLine();
				if(currentLine == null)
					break;
				if(currentLine.length() == 0)
					continue;
				clientsThatCanUpload.add(currentLine);
			}
			if(serverMaxLogging)
				logging("loadCanUploadList : Exit OK");
		}
		catch(IOException e)
		{
			logging("loadCanUploadList -- Error loading can-upload list: " + e);
		}
	}

	public static boolean keyBelongsToClient(DatabaseKey key, String clientId)
	{
		return clientId.equals(key.getAccountId());
	}

	public static DatabaseKey[] getAllPacketKeys(BulletinHeaderPacket bhp)
	{
		String accountId = bhp.getAccountId();
		String[] publicAttachmentIds = bhp.getPublicAttachmentIds();
		String[] privateAttachmentIds = bhp.getPrivateAttachmentIds();
		
		int corePacketCount = 3;
		int publicAttachmentCount = publicAttachmentIds.length;
		int privateAttachmentCount = privateAttachmentIds.length;
		int totalPacketCount = corePacketCount + publicAttachmentCount + privateAttachmentCount;
		DatabaseKey[] keys = new DatabaseKey[totalPacketCount];
		int next = 0;
		
		UniversalId dataUid = UniversalId.createFromAccountAndLocalId(accountId, bhp.getFieldDataPacketId());
		UniversalId privateDataUid = UniversalId.createFromAccountAndLocalId(accountId, bhp.getPrivateFieldDataPacketId());

		keys[next++] = new DatabaseKey(dataUid);
		keys[next++] = new DatabaseKey(privateDataUid);
		for(int i=0; i < publicAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, publicAttachmentIds[i]);
			keys[next++] = new DatabaseKey(uid);
		}
		for(int i=0; i < privateAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, privateAttachmentIds[i]);
			keys[next++] = new DatabaseKey(uid);
		}
		keys[next++] = new DatabaseKey(bhp.getUniversalId());
		
		boolean isDraft = (bhp.getStatus().equals(BulletinConstants.STATUSDRAFT));
		for(int i=0; i < keys.length; ++i)
		{
			if(isDraft)
				keys[i].setDraft();
			else
				keys[i].setSealed();
		}
		return keys;
	}
	
	void readKeyPair(InputStream in, String passphrase) throws 
		IOException,
		MartusCrypto.AuthorizationFailedException,
		MartusCrypto.InvalidKeyPairFileVersionException
	{
		security.readKeyPair(in, passphrase);
	}
	
	void writeKeyPair(OutputStream out, String passphrase) throws 
		IOException
	{
		security.writeKeyPair(out, passphrase);
	}
	
	public static File getDataDirectory()
	{
		String dataDirectory = null;
		if(System.getProperty("os.name").indexOf("Windows") >= 0)
		{
			dataDirectory = "C:/MartusServer/";
		}
		else
		{
			String userHomeDir = System.getProperty("user.home");
			dataDirectory = userHomeDir + "/MartusServer/";
		}
		File file = new File(dataDirectory);
		if(!file.exists())
		{
			file.mkdirs();
		}
		
		return file;
	}
	
	class DuplicatePacketException extends Exception
	{
		DuplicatePacketException(String message)
		{
			super(message);
		}
	}
	
	class SealedPacketExistsException extends Exception
	{
		SealedPacketExistsException(String message)
		{
			super(message);
		}
	}

	private String saveUploadedBulletinZipFile(String authorAccountId, File zipFile) 
	{
		String result = NetworkInterfaceConstants.OK;
		
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(zipFile);
			saveZipPacketsInDatabase(authorAccountId, zip);
		}
		catch (DuplicatePacketException e)
		{
			logging("saveUpload DUPLICATE: " + e.getMessage());
			result =  NetworkInterfaceConstants.DUPLICATE;
		}
		catch (SealedPacketExistsException e)
		{
			logging("saveUpload SEALED_EXISTS: " + e.getMessage());
			result =  NetworkInterfaceConstants.SEALED_EXISTS;
		}
		catch (Packet.SignatureVerificationException e)
		{
			logging("saveUpload SIG_ERROR: " + e);
			result =  NetworkInterfaceConstants.SIG_ERROR;
		}
		catch (Packet.WrongAccountException e)
		{
			logging("saveUpload NOTYOURBULLETIN: ");
			result =  NetworkInterfaceConstants.NOTYOURBULLETIN;
		}
		catch (Exception e)
		{
			logging("saveUpload INVALID_DATA: " + e);
			result =  NetworkInterfaceConstants.INVALID_DATA;
		}
		
		if(zip != null)
		{
			try
			{
				zip.close();
			}
			catch(IOException nothingWeCanDoAboutIt)
			{
				logging("saveUpload error closing zip file: nothingWeCanDoAboutIt");
			}
		}

		return result;
	}

	private void saveZipPacketsInDatabase(String authorAccountId, ZipFile zip)
		throws IOException, 
		DuplicatePacketException,
		SealedPacketExistsException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		Packet.WrongAccountException,
		MartusCrypto.DecryptionException
	{
		validateZipFilePackets(authorAccountId, zip);
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		deleteDraftBulletinPackets(header.getUniversalId());
		
		Database db = getDatabase();
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			InputStream in = new BufferedInputStream(zip.getInputStream(entry));
		
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, entry.getName());
			DatabaseKey key = createKeyWithHeaderStatus(header, uid);
			db.writeRecord(key, in);
		}
	}

	private void validateZipFilePackets(String authorAccountId, ZipFile zip) throws 
		IOException, 
		DuplicatePacketException,
		SealedPacketExistsException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		Packet.WrongAccountException,
		MartusCrypto.DecryptionException
	{
		//TODO validate Header Packet matches other packets
		Database db = getDatabase();
		Enumeration entries = zip.entries();
		if(!entries.hasMoreElements())
		{
			throw new Packet.InvalidPacketException("Empty zip file");
		}

		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, entry.getName());
			DatabaseKey trySealedKey = new DatabaseKey(uid);
			trySealedKey.setSealed();
			if(db.doesRecordExist(trySealedKey))
			{
				DatabaseKey newKey = createKeyWithHeaderStatus(header, uid);
				if(newKey.isDraft())
					throw new SealedPacketExistsException(entry.getName());
				else
					throw new DuplicatePacketException(entry.getName());
			}
		
			InputStream in = new ZipEntryInputStream(zip, entry);
			Packet.validateXml(in, authorAccountId, entry.getName(), null, security);
		}
	}

	private void deleteDraftBulletinPackets(UniversalId uid) throws
		IOException
	{
		DatabaseKey headerKey = new DatabaseKey(uid);
		headerKey.setDraft();
		Database db = getDatabase();
		if(!db.doesRecordExist(headerKey))
			return;
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(uid);
		try 
		{
			InputStream in = db.openInputStream(headerKey, security);
			bhp.loadFromXml(in, security);
		} 
		catch (Exception e) 
		{
			throw new IOException(e.toString());
		}
		
		String accountId = bhp.getAccountId();
		deleteDraftPacket(accountId, bhp.getLocalId());
		deleteDraftPacket(accountId, bhp.getFieldDataPacketId());
		deleteDraftPacket(accountId, bhp.getPrivateFieldDataPacketId());
		
		String[] publicAttachmentIds = bhp.getPublicAttachmentIds();
		for(int i = 0; i < publicAttachmentIds.length; ++i)
		{
			deleteDraftPacket(accountId, publicAttachmentIds[i]);
		}

		String[] privateAttachmentIds = bhp.getPrivateAttachmentIds();
		for(int i = 0; i < privateAttachmentIds.length; ++i)
		{
			deleteDraftPacket(accountId, privateAttachmentIds[i]);
		}
	}

	private void deleteDraftPacket(String accountId, String localId)
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
		DatabaseKey key = new DatabaseKey(uid);
		key.setDraft();
		getDatabase().discardRecord(key);
	}

	private DatabaseKey createKeyWithHeaderStatus(BulletinHeaderPacket header, UniversalId uid) 
	{
		DatabaseKey key = new DatabaseKey(uid);
		if(header.getStatus().equals(BulletinConstants.STATUSDRAFT))
			key.setDraft();
		else
			key.setSealed();
		return key;
	}

	private String getBulletinHQAccountId(DatabaseKey headerKey) throws
			IOException,
			CryptoException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException
	{
		BulletinHeaderPacket bhp = loadBulletinHeaderPacket(getDatabase(), headerKey);
		return bhp.getHQPublicKey();
	}
	
	private Vector buildBulletinChunkResponse(DatabaseKey headerKey, int chunkOffset, int maxChunkSize) throws
			IOException,
			CryptoException,
			UnsupportedEncodingException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException,
			NoKeyPairException,
			FileNotFoundException,
			FileTooLargeException 
	{
		Vector result = new Vector();
		if(serverMaxLogging)
			logging("entering createInterimBulletinFile");
		File tempFile = createInterimBulletinFile(headerKey);
		if(serverMaxLogging)
			logging("createInterimBulletinFile done");
		int totalLength = MartusUtilities.getCappedFileLength(tempFile);
		
		int chunkSize = totalLength - chunkOffset;
		if(chunkSize > maxChunkSize)
			chunkSize = maxChunkSize;
			
		byte[] rawData = new byte[chunkSize];
		
		StringWriter writer = new StringWriter();
		FileInputStream in = new FileInputStream(tempFile);
		in.skip(chunkOffset);
		in.read(rawData);
		in.close();
		
		String zipString = Base64.encode(rawData);
		
		int endPosition = chunkOffset + chunkSize;
		if(endPosition >= totalLength)
		{
			tempFile.delete();
			result.add(NetworkInterfaceConstants.OK);
		}
		else
		{
			result.add(NetworkInterfaceConstants.CHUNK_OK);
		}
		result.add(new Integer(totalLength));
		result.add(new Integer(chunkSize));
		result.add(zipString);
		if(serverMaxLogging)
			logging("downloadBulletinChunk : Exit " + result.get(0));
		return result;
	}

	private File createInterimBulletinFile(DatabaseKey headerKey) throws
			IOException,
			CryptoException,
			UnsupportedEncodingException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException,
			NoKeyPairException,
			FileNotFoundException 
	{
		File tempFile = getDatabase().getOutgoingInterimFile(headerKey);
		if(tempFile.exists())
			return tempFile;

		String headerXml = getDatabase().readRecord(headerKey, security);
		byte[] headerBytes = headerXml.getBytes("UTF-8");
		
		ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
		BulletinHeaderPacket bhp = new BulletinHeaderPacket("");
		
		MartusCrypto doNotCheckSigDuringDownload = null;
		bhp.loadFromXml(headerIn, doNotCheckSigDuringDownload);
		
		DatabaseKey[] packetKeys = getAllPacketKeys(bhp);
		
		exportPacketsToZipFile(headerKey.getAccountId(), packetKeys, tempFile);
		return tempFile;
	}


	private void exportPacketsToZipFile(String clientId, DatabaseKey[] packetKeys, File tempFile) throws 
			FileNotFoundException, 
			IOException, 
			UnsupportedEncodingException 
	{
		Database db = getDatabase();
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		extractPacketsToZipStream(clientId, packetKeys, outputStream);
	}


	public void extractPacketsToZipStream(String clientId, DatabaseKey[] packetKeys, OutputStream outputStream) throws 
		IOException, 
		UnsupportedEncodingException 
	{
		ZipOutputStream zipOut = new ZipOutputStream(outputStream);
		
		Database db = getDatabase();
		try 
		{
			for(int i = 0; i < packetKeys.length; ++i)
			{
				DatabaseKey key = packetKeys[i];
				ZipEntry entry = new ZipEntry(key.getLocalId());
				zipOut.putNextEntry(entry);

				InputStream in = db.openInputStream(key, security);

				int got;
				byte[] bytes = new byte[1024];
				while( (got=in.read(bytes)) >= 0)
					zipOut.write(bytes, 0, got);
					
				in.close();
			}
		} 
		catch(CryptoException e) 
		{
			throw new IOException("CryptoException " + e);
		}
		finally
		{
			zipOut.close();
		}
	}
	
	private boolean isSignatureCorrect(String signedString, String signature, String signerPublicKey)
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(signedString.getBytes("UTF-8"));
			return security.isSignatureValid(signerPublicKey, in, Base64.decode(signature));
		}
		catch(Exception e)
		{
			logging("  isSigCorrect exception: " + e);
			return false;
		}
	}
	
	private String getFolderFromClientId(String clientId)
	{
		return getDatabase().getFolderForAccount(clientId);
	}

	public synchronized void logging(String message)
	{
		if(serverLogging)
		{
			Timestamp stamp = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat formatDate = new SimpleDateFormat("EE MM/dd HH:mm:ss z:");
			System.out.println(formatDate.format(stamp) + " " + message);
		}
	}

	BulletinHeaderPacket loadBulletinHeaderPacket(Database db, DatabaseKey key)
		throws
			IOException,
			CryptoException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(key.getAccountId());
		InputStream in = db.openInputStream(key, security);
		bhp.loadFromXml(in, security);
		in.close();
		return bhp;
	}

	abstract class SummaryCollector implements Database.PacketVisitor
	{
		SummaryCollector(Database dbToUse, String accountIdToUse)
		{
			db = dbToUse;
			authorAccountId = accountIdToUse;
		}
		
		public void visit(DatabaseKey key)
		{
			// TODO: this should only be for maxmaxmaxlogging
//				if(serverMaxLogging)
//				{
//					logging("listMyBulletinSummaries:visit " + 
//						getFolderFromClientId(key.getAccountId()) +  " " +
//						key.getLocalId());
//				}
			if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
			{
				//this would fire for every non-header packet
				//logging("listMyBulletinSummaries:visit  Error:isValidLocalId Key=" + key.getLocalId() );					
				return;
			}
				
			addSummaryIfAppropriate(key);
			return;
		}

		abstract public void addSummaryIfAppropriate(DatabaseKey key);
		
		public Vector getSummaries()
		{
			if(summaries == null)
			{
				summaries = new Vector();
				summaries.add(NetworkInterfaceConstants.OK);
				db.visitAllRecords(this);
			}
			return summaries;	
		}

		void addToSummary(BulletinHeaderPacket bhp) 
		{
			String summary = bhp.getLocalId() + "=";
			summary  += bhp.getFieldDataPacketId();
		
			summaries.add(summary);
		}

		Database db;
		String authorAccountId;
		Vector summaries;
	}
	
	class MySealedSummaryCollector extends SummaryCollector
	{
		public MySealedSummaryCollector(Database dbToUse, String accountIdToUse) 
		{
			super(dbToUse, accountIdToUse);
		}

		public void addSummaryIfAppropriate(DatabaseKey key) 
		{
			if(!keyBelongsToClient(key, authorAccountId))
				return;

			if(!key.isSealed())
				return;
				
			try
			{
				addToSummary(loadBulletinHeaderPacket(db, key));
			}
			catch(Exception e)
			{
				logging("visit " + e);
				e.printStackTrace();
				//System.out.println("MartusServer.listMyBulletinSummaries: " + e);
			}
		}
	}

	class MyDraftSummaryCollector extends SummaryCollector
	{
		public MyDraftSummaryCollector(Database dbToUse, String accountIdToUse) 
		{
			super(dbToUse, accountIdToUse);
		}

		public void addSummaryIfAppropriate(DatabaseKey key) 
		{
			if(!keyBelongsToClient(key, authorAccountId))
				return;

			if(!key.isDraft())
				return;

			try
			{
				addToSummary(loadBulletinHeaderPacket(db, key));
			}
			catch(Exception e)
			{
				logging("visit " + e);
				e.printStackTrace();
				//System.out.println("MartusServer.listMyBulletinSummaries: " + e);
			}
		}
	}


	class FieldOfficeSealedSummaryCollector extends SummaryCollector
	{
		public FieldOfficeSealedSummaryCollector(Database dbToUse, String hqAccountIdToUse, String authorAccountIdToUse) 
		{
			super(dbToUse, authorAccountIdToUse);
			hqAccountId = hqAccountIdToUse;

		}

		public void addSummaryIfAppropriate(DatabaseKey key) 
		{
			if(!keyBelongsToClient(key, authorAccountId))
				return;
			if(!key.isSealed())
				return;
			
			try
			{
				BulletinHeaderPacket bhp = loadBulletinHeaderPacket(db, key);
				if(bhp.getHQPublicKey().equals(hqAccountId))
					addToSummary(bhp);
			}
			catch(Exception e)
			{
				logging("visit " + e);
				e.printStackTrace();
				//System.out.println("MartusServer.FieldOfficeSealedSummaryCollectors: " + e);
			}
		}
		String hqAccountId;
	}

	class FieldOfficeDraftSummaryCollector extends SummaryCollector
	{
		public FieldOfficeDraftSummaryCollector(Database dbToUse, String hqAccountIdToUse, String authorAccountIdToUse) 
		{
			super(dbToUse, authorAccountIdToUse);
			hqAccountId = hqAccountIdToUse;

		}

		public void addSummaryIfAppropriate(DatabaseKey key) 
		{
			if(!keyBelongsToClient(key, authorAccountId))
				return;
			if(!key.isDraft())
				return;
			
			try
			{
				BulletinHeaderPacket bhp = loadBulletinHeaderPacket(db, key);
				if(bhp.getHQPublicKey().equals(hqAccountId))
					addToSummary(bhp);
			}
			catch(Exception e)
			{
				logging("visit " + e);
				e.printStackTrace();
				//System.out.println("MartusServer.FieldOfficeDraftSummaryCollectors: " + e);
			}
		}
		String hqAccountId;
	}


	Database database;
	ServerSideNetworkHandlerForNonSSL nonSSLServerHandler;
	ServerSideNetworkHandler serverHandler;
	public Vector clientsThatCanUpload;
	public MartusCrypto security;
	File keyPairFile;
	public File allowUploadFile;
	public File magicWordsFile;
	private String magicWord;
	private static boolean serverLogging;
	private static boolean serverMaxLogging;
	public static boolean serverSSLLogging;
}
