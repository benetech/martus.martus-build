package org.martus.server.forclients;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.martus.common.AttachmentPacket;
import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.FileDatabase;
import org.martus.common.InputStreamWithSeek;
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
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.CryptoException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusCrypto.NoKeyPairException;
import org.martus.common.MartusUtilities.FileTooLargeException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;
import org.martus.common.Packet.WrongPacketTypeException;
import org.martus.server.formirroring.MirroringInterface;
import org.martus.server.formirroring.MirroringRetriever;
import org.martus.server.formirroring.ServerSupplierInterface;
import org.martus.server.formirroring.SupplierSideMirroringHandler;
import org.martus.server.core.*;
import org.martus.server.core.ServerFileDatabase;

public class MartusServer implements NetworkInterfaceConstants, ServerSupplierInterface
{

	public static void main(String[] args)
	{
		System.out.println("MartusServer");
		
		File dataDirectory = getDefaultDataDirectory();
		
		MartusServer server = null;
		boolean secureMode = false;
		String servername = null;
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

			if(args[arg].startsWith("--server-name="))
			{
				servername = args[arg].substring(args[arg].indexOf("=")+1);
			}
		}
		
		if(secureMode)
			System.out.println("Running in SECURE mode");
		else
			System.out.println("***RUNNING IN INSECURE MODE***");
		
		System.out.println("Initializing...this will take a few seconds...");
		try
		{
			server = new MartusServer(dataDirectory);
		} 
		catch(CryptoInitializationException e) 
		{
			System.out.println("Crypto Initialization Exception" + e);
			System.exit(1);			
		}
		
		server.setServerName(servername);
		
		
		System.out.println("Version " + ServerConstants.version);
		
		String versionInfo = MartusUtilities.getVersionDate();
		System.out.println("Build Date " + versionInfo);

		System.out.print("Enter passphrase: ");
		System.out.flush();

		File waitingFile = new File(server.triggerDirectory, "waiting");
		waitingFile.delete();
		writeSyncFile(waitingFile);

		InputStreamReader rawReader = new InputStreamReader(System.in);	
		BufferedReader reader = new BufferedReader(rawReader);
		try
		{
			String passphrase = reader.readLine();
			if(server.hasAccount())
			{
				try
				{
					server.loadAccount(passphrase);
				}
				catch (Exception e)
				{
					System.out.println("Invalid password: " + e);
					System.exit(73);
				}
			}
			else
			{
				System.out.println("***** Key pair file not found *****");
				System.exit(2);
			}
			
			System.out.println("Passphrase correct.");			
			server.initialize();

			String accountId = server.getAccountId();
			System.out.println("Server Account: " + accountId);
			System.out.println();

			System.out.print("Server Public Code: ");
			String publicCode = MartusUtilities.computePublicCode(accountId);
			System.out.println(MartusUtilities.formatPublicCode(publicCode));
			System.out.println();
		}
		catch(IOException e)
		{
			System.out.println("MartusServer.main: " + e);
			System.exit(3);
		}
		catch (InvalidBase64Exception e)
		{
			System.out.println("MartusServer.main: " + e);
			System.exit(3);
		}

		Database diskDatabase = new ServerFileDatabase(new File(dataDirectory, "packets"), server.getSecurity());
		try
		{
			diskDatabase.initialize();
		}
		catch(FileDatabase.MissingAccountMapException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map File");
			System.exit(7);
		}
		catch(FileDatabase.MissingAccountMapSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map Signature File");
			System.exit(7);
		}
		catch(FileVerificationException e)
		{
			e.printStackTrace();
			System.out.println("Account Map did not verify against signature file");
			System.exit(7);
		}
		
		server.setDatabase(diskDatabase);
		
		File runningFile = new File(server.triggerDirectory, "running");
		runningFile.delete();
		if(secureMode)
		{
			File magicWordsFile = new File(server.startupConfigDirectory, MAGICWORDSFILENAME);
			if(!magicWordsFile.delete())
			{
				System.out.println("Unable to delete magicwords");
				System.exit(4);
			}
			File keyPairFile = new File(server.startupConfigDirectory, getKeypairFilename());
			if(!keyPairFile.delete())
			{
				System.out.println("Unable to delete keypair");
				System.exit(5);
			}
			File bannedFile = new File(server.startupConfigDirectory, BANNEDCLIENTSFILENAME);
			if(bannedFile.exists())
			{
				if(!bannedFile.delete())
				{
					System.out.println("Unable to delete " + bannedFile.getAbsolutePath() );
					System.exit(5);
				}
			}
		}
				
		System.out.println();

		System.out.println(server.clientsThatCanUpload.size() + " client(s) currently allowed to upload");
		System.out.println(server.clientsBanned.size() + " client(s) are currently banned");
		System.out.println(server.magicWords.size() + " active magic word(s)");

		System.out.println("Setting up sockets (this may take up to a minute or longer)...");
		server.createNonSSLXmlRpcServer();
		server.createSSLXmlRpcServer();
		server.createMirroringSupplierXmlRpcServer();
		writeSyncFile(runningFile);
		System.out.println("Waiting for connection...");
	}


	MartusServer(File dir) throws 
					MartusCrypto.CryptoInitializationException
	{
		security = new MartusSecurity();
		
		dataDirectory = dir;
		
		triggerDirectory = new File(dataDirectory, ADMINTRIGGERDIRECTORY);
		if(!triggerDirectory.exists())
		{
			triggerDirectory.mkdirs();
		}

		startupConfigDirectory = new File(dataDirectory,ADMINSTARTUPCONFIGDIRECTORY);
		if(!startupConfigDirectory.exists())
		{
			startupConfigDirectory.mkdirs();
		}
		
		nonSSLServerHandler = new ServerSideNetworkHandlerForNonSSL(this);
		serverHandler = new ServerSideNetworkHandler(this);
		supplierHandler = new SupplierSideMirroringHandler(this);

		clientsThatCanUpload = new Vector();
		clientsBanned = new Vector();
		magicWords = new Vector();
		failedUploadRequestCounter = 0;
		
		allowUploadFile = new File(dataDirectory, UPLOADSOKFILENAME);
		magicWordsFile = new File(startupConfigDirectory, MAGICWORDSFILENAME);
		keyPairFile = new File(startupConfigDirectory, getKeypairFilename());
		bannedClientsFile = new File(startupConfigDirectory, BANNEDCLIENTSFILENAME);
		
		shutdownFile = new File(triggerDirectory, MARTUSSHUTDOWNFILENAME);
		
		loadBannedClients();
		
		Timer shutdownRequestTimer = new Timer(true);
 		TimerTask shutdownRequestTaskMonitor = new ShutdownRequestMonitor();
 		shutdownRequestTimer.schedule(shutdownRequestTaskMonitor, IMMEDIATELY, shutdownRequestIntervalMillis);

		Timer mirroringTimer = new Timer(true);
 		TimerTask mirroringTask = new MirroringTask();
 		mirroringTimer.schedule(mirroringTask, IMMEDIATELY, mirroringIntervalMillis);
 		
 		Timer failedUploadRequestsTimer = new Timer(true);
 		TimerTask uploadRequestTask = new UploadRequestsMonitor();
 		failedUploadRequestsTimer.schedule(uploadRequestTask, IMMEDIATELY, getUploadRequestTimerInterval());
	}
	
	public void initialize()
	{
		verifyConfigurationFiles();
		loadConfigurationFiles();
	}
	
	public void verifyConfigurationFiles()
	{
		File allowUploadFileSignature = MartusUtilities.getSignatureFileFromFile(allowUploadFile);
		if(allowUploadFile.exists() || allowUploadFileSignature.exists())
		{
			try
			{
				MartusUtilities.verifyFileAndSignature(allowUploadFile, allowUploadFileSignature, security, security.getPublicKeyString());
			}
			catch(FileVerificationException e)
			{
				e.printStackTrace();
				System.out.println(UPLOADSOKFILENAME + " did not verify against signature file");
				System.exit(7);
			}
		}
	}

	public void loadConfigurationFiles()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(allowUploadFile));
			loadCanUploadList(reader);
			reader.close();
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			;
		}
		catch(IOException e)
		{
			// TODO: Log this so the administrator knows
			System.out.println("MartusServer constructor: " + e);
		}
		
		try
		{
			UnicodeReader reader = new UnicodeReader(magicWordsFile);
			String line = null;
			while( (line = reader.readLine()) != null)
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
	
	public void setDatabase(Database databaseToUse)
	{
		database = databaseToUse;
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	NetworkInterfaceForNonSSL getNonSSLServerHandler()
	{
		return nonSSLServerHandler;
	}
	
	NetworkInterface getServerHandler()
	{
		return serverHandler;
	}
	
	MirroringInterface getMirroringSupplierHandler()
	{
		return supplierHandler;
	}

	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return false;
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
		if( !magicWords.contains(newMagicWord) )
			magicWords.add(newMagicWord);
	}
	
	public void createNonSSLXmlRpcServer()
	{
		MartusXmlRpcServer.createNonSSLXmlRpcServer(getNonSSLServerHandler(), NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_NON_SSL);
	}

	public void createSSLXmlRpcServer()
	{
		int port = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_SSL;
		createSSLXmlRpcServerOnPort(port);
	}

	public void createSSLXmlRpcServerOnPort(int port) 
	{
		MartusSecureWebServer.security = security;
		MartusXmlRpcServer.createSSLXmlRpcServer(getServerHandler(), port);
	}
	
	public void createMirroringSupplierXmlRpcServer()
	{
		int port = MirroringInterface.MARTUS_PORT_FOR_MIRRORING;
		createMirroringSupplierXmlRpcServer(port);
	}

	public void createMirroringSupplierXmlRpcServer(int port)
	{
		MartusXmlRpcServer.createSSLXmlRpcServer(getMirroringSupplierHandler(), port);
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
			
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
				
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
		boolean uploadGranted = false;

		if(magicWords.contains(tryMagicWord))
			uploadGranted = true;
			
		if(!areUploadRequestsCurrentlyAllowed())
		{
			if(!uploadGranted)
				incrementFailedUploadRequests();
			return NetworkInterfaceConstants.SERVER_ERROR;
		}

		if( isClientBanned(clientId) )
			return NetworkInterfaceConstants.REJECTED;
			
		if( isShutdownRequested() )
			return NetworkInterfaceConstants.SERVER_DOWN;
			
		if(tryMagicWord.length() == 0 && clientsThatCanUpload.contains(clientId))
			return NetworkInterfaceConstants.OK;
		
		if(!uploadGranted)
		{
			logging("requestUploadRights: Rejected " + getPublicCode(clientId) + " magicWords=" + magicWords.toString() + " tryMagicWord=" +tryMagicWord);
			incrementFailedUploadRequests();
			return NetworkInterfaceConstants.REJECTED;
		}
		if(serverMaxLogging)
			logging("requestUploadRights granted to :" + clientId + " with magicword=" + tryMagicWord);
			
		allowUploads(clientId);
		return NetworkInterfaceConstants.OK;
	}
	
	
	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		if(serverMaxLogging)
			logging("uploadBulletin " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);

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
		
		if(isClientBanned(authorAccountId) )
			return NetworkInterfaceConstants.REJECTED;
		
		if( isShutdownRequested() )
			return NetworkInterfaceConstants.SERVER_DOWN;
		
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
			logging("  " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logging("  Total Size=" + totalSize + ", Offset=" + chunkOffset);
			if(chunkSize != NetworkInterfaceConstants.MAX_CHUNK_SIZE)
				logging("Last Chunk = " + chunkSize);
		}
		
		if(isClientBanned(authorAccountId) || !canClientUpload(authorAccountId))
		{
			logging("putBulletinChunk REJECTED");
			return NetworkInterfaceConstants.REJECTED;
		}
		
		if( isShutdownRequested() )
		{
			logging(" returning SERVER_DOWN");
			return NetworkInterfaceConstants.SERVER_DOWN;
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
			logging("downloadBulletin " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );

		Vector result = new Vector();
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = DatabaseKey.createSealedKey(uid);
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
	
				MartusUtilities.deleteInterimFileAndSignature(tempFile);
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
			logging("  " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logging("  Offset=" + chunkOffset + ", Max=" + maxChunkSize + " HQ: " + getClientAliasForLogging(hqAccountId));
		}
		
		if(isClientBanned(hqAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
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
			logging("  " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logging("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
		}
		
		if(isClientBanned(authorAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
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
			return returnSingleResponseAndLog("  all attempts to verify sig have failed!", NetworkInterfaceConstants.SIG_ERROR);
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
			logging("getBulletinChunk request by " + getClientAliasForLogging(myAccountId));
			logging("  " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logging("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
		}
		
		if(isClientBanned(myAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );

		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
			return returnSingleResponseAndLog( " returning NOT_FOUND", NetworkInterfaceConstants.NOT_FOUND );

		if(!myAccountId.equals(authorAccountId))
		{
			try 
			{
				String hqAccountId = getBulletinHQAccountId(headerKey);
				if(!myAccountId.equals(hqAccountId))
					return returnSingleResponseAndLog( " returning NOTYOURBULLETIN", NetworkInterfaceConstants.NOTYOURBULLETIN );
			} 
			catch (SignatureVerificationException e) 
			{
					return returnSingleResponseAndLog( " returning SIG ERROR", NetworkInterfaceConstants.SIG_ERROR );
			} 
			catch (Exception e) 
			{
				return returnSingleResponseAndLog( " returning SERVER_ERROR :" + e, NetworkInterfaceConstants.SERVER_ERROR );
			} 
		}

		Vector result = getBulletinChunkWithoutVerifyingCaller(
					authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize);
		
		if(serverMaxLogging)
			logging("  exit: " + result.get(0));
		return result;
	}


	public Vector legacyListMySealedBulletinIds(String clientId)
	{
		if(serverMaxLogging)
			logging("legacylistMySealedBulletinIds " + getClientAliasForLogging(clientId));
		
		if(isClientBanned(clientId) )
			return returnSingleResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		SummaryCollector summaryCollector = new MySealedSummaryCollector(getDatabase(), clientId, new Vector());
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("legacylistMySealedBulletinIds : Exit");
		return summaries;
	}
	
	public Vector listMySealedBulletinIds(String clientId, Vector retrieveTags)
	{
		if(serverMaxLogging)
			logging("listMySealedBulletinIds " + getClientAliasForLogging(clientId));
		
		if(isClientBanned(clientId) )
			return returnSingleResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		SummaryCollector summaryCollector = new MySealedSummaryCollector(getDatabase(), clientId, retrieveTags);
		Vector summaries = summaryCollector.getSummaries();
		String resultCode = (String)summaries.get(0);
		summaries.remove(0);

		Vector result = new Vector();
		result.add(resultCode);
		result.add(summaries);
		if(serverMaxLogging)
			logging("listMySealedBulletinIds : Exit");
		return result;
	}

	public Vector legacyListMyDraftBulletinIds(String authorAccountId)
	{
		if(serverMaxLogging)
			logging("legacyListMyDraftBulletinIds " + getClientAliasForLogging(authorAccountId));
			
		if(isClientBanned(authorAccountId) )
			return returnSingleResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		SummaryCollector summaryCollector = new MyDraftSummaryCollector(getDatabase(), authorAccountId, new Vector());
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("legacyListMyDraftBulletinIds : Exit");
		return summaries;
	}

	public Vector listMyDraftBulletinIds(String authorAccountId, Vector retrieveTags)
	{
		if(serverMaxLogging)
			logging("listMyDraftBulletinIds " + getClientAliasForLogging(authorAccountId));
			
		if(isClientBanned(authorAccountId) )
			return returnSingleResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		SummaryCollector summaryCollector = new MyDraftSummaryCollector(getDatabase(), authorAccountId, retrieveTags);
		Vector summaries = summaryCollector.getSummaries();

		String resultCode = (String)summaries.get(0);
		summaries.remove(0);
		Vector result = new Vector();
		result.add(resultCode);
		result.add(summaries);

		if(serverMaxLogging)
			logging("listMyDraftBulletinIds : Exit");
		return result;
	}

	public Vector legacyListFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId)
	{
		if(serverMaxLogging)
			logging("legacylistFieldOfficeSealedBulletinIds " + getClientAliasForLogging(hqAccountId));
			
		if(isClientBanned(hqAccountId) )
			return returnSingleResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		SummaryCollector summaryCollector = new FieldOfficeSealedSummaryCollector(getDatabase(), hqAccountId, authorAccountId, new Vector());
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("legacylistFieldOfficeSealedBulletinIds : Exit");
		return summaries;	
	}

	public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		if(serverMaxLogging)
			logging("listFieldOfficeSealedBulletinIds " + getClientAliasForLogging(hqAccountId));
			
		if(isClientBanned(hqAccountId) )
			return returnSingleResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		SummaryCollector summaryCollector = new FieldOfficeSealedSummaryCollector(getDatabase(), hqAccountId, authorAccountId, retrieveTags);
		Vector summaries = summaryCollector.getSummaries();

		String resultCode = (String)summaries.get(0);
		summaries.remove(0);

		Vector result = new Vector();
		result.add(resultCode);
		result.add(summaries);

		if(serverMaxLogging)
			logging("listFieldOfficeSealedBulletinIds : Exit");
		return result;	
	}

	public Vector legacyListFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId)
	{
		if(serverMaxLogging)
			logging("legacyListFieldOfficeDraftBulletinIds " + getClientAliasForLogging(hqAccountId));

		if(isClientBanned(hqAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
			
		SummaryCollector summaryCollector = new FieldOfficeDraftSummaryCollector(getDatabase(), hqAccountId, authorAccountId, new Vector());
		Vector summaries = summaryCollector.getSummaries();
		if(serverMaxLogging)
			logging("legacyListFieldOfficeDraftBulletinIds : Exit");
		return summaries;
	}

	public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		if(serverMaxLogging)
			logging("listFieldOfficeDraftBulletinIds " + getClientAliasForLogging(hqAccountId));

		if(isClientBanned(hqAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
			
		SummaryCollector summaryCollector = new FieldOfficeDraftSummaryCollector(getDatabase(), hqAccountId, authorAccountId, retrieveTags);
		Vector summaries = summaryCollector.getSummaries();

		String resultCode = (String)summaries.get(0);
		summaries.remove(0);
		Vector result = new Vector();
		result.add(resultCode);
		result.add(summaries);

		if(serverMaxLogging)
			logging("listFieldOfficeDraftBulletinIds : Exit");
		return result;
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
			logging("listFieldOfficeAccounts " + getClientAliasForLogging(hqAccountId));
			
		if(isClientBanned(hqAccountId) )
			return returnSingleResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog("  returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN);

		FieldOfficeAccountCollector visitor = new FieldOfficeAccountCollector(hqAccountId);
		getDatabase().visitAllRecords(visitor);
	
		if(serverMaxLogging)
			logging("listFieldOfficeAccounts : Exit");
		return visitor.getAccounts();	
	}
	
	public String deleteDraftBulletins(String accountId, String[] localIds)
	{
		if(isClientBanned(accountId) )
			return REJECTED;
		
		if( isShutdownRequested() )
			return SERVER_DOWN;
			
		String result = OK;
		for (int i = 0; i < localIds.length; i++)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localIds[i]);
			try
			{
				DatabaseKey key = DatabaseKey.createDraftKey(uid);
				BulletinHeaderPacket bhp = new BulletinHeaderPacket(uid);
				InputStreamWithSeek in = getDatabase().openInputStream(key, security);
				bhp.loadFromXml(in, null, security);
				in.close();

				MartusUtilities.deleteBulletinFromDatabase(bhp, getDatabase(), security);			
			}
			catch (Exception e)
			{
				logging("deleteDraftBulletins: " + e);
				result = INCOMPLETE;
			}
		}
		return result;
	}
	
	public String putContactInfo(String accountId, Vector contactInfo)
	{
		if(serverMaxLogging)
			logging("putContactInfo " + getClientAliasForLogging(accountId));
		if(isClientBanned(accountId) )
			return NetworkInterfaceConstants.REJECTED;
		
		if( isShutdownRequested() )
			return NetworkInterfaceConstants.SERVER_DOWN;
			
		String result = NetworkInterfaceConstants.INVALID_DATA;
		if(contactInfo == null)
			return result;
		if(contactInfo.size() <= 3)
			return result;
		String publicKey = (String)contactInfo.get(0);
		if(!publicKey.equals(accountId))
			return result;
		int contentSize = ((Integer)(contactInfo.get(1))).intValue();
		if(contentSize + 3 != contactInfo.size())
			return result;

		String signature = (String)contactInfo.get(contactInfo.size()-1);
		contactInfo.remove(contactInfo.size()-1);
		if(!MartusUtilities.verifySignature(contactInfo, security, publicKey, signature))
			return NetworkInterfaceConstants.SIG_ERROR;
		contactInfo.add(signature);

		try 
		{
			File contactInfoFile = getContactInfoFileForAccount(accountId);
			contactInfoFile.getParentFile().mkdirs();
			FileOutputStream contactFileOutputStream = new FileOutputStream(contactInfoFile);
			DataOutputStream out = new DataOutputStream(contactFileOutputStream);
			out.writeUTF((String)contactInfo.get(0));
			out.writeInt(((Integer)(contactInfo.get(1))).intValue());
			for(int i = 2; i<contactInfo.size(); ++i)
			{
				out.writeUTF((String)contactInfo.get(i));
			}
			out.close();
		} 
		catch (IOException e) 
		{
			logging("putContactInfo Error" + e);
			return NetworkInterfaceConstants.SERVER_ERROR;
		}
		return NetworkInterfaceConstants.OK;
	}

	public Vector getNews(String accountId)
	{
		Vector result = new Vector();
		Vector items = new Vector();
		if(isClientBanned(accountId))
		{
			final String bannedText = "Your account has been blocked from accessing this server. " + 
					"Please contact the Server Policy Administrator for more information.";
			items.add(bannedText);
		}

		result.add(OK);
		result.add(items);
		return result;
	}

	public File getContactInfoFileForAccount(String accountId) throws
		IOException
	{
		return getDatabase().getContactInfoFile(accountId);
	}

	public Vector legacyDownloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		//TODO reject requests for other accounts public packets
		if(serverMaxLogging)
			logging("downloadAuthorizedPacket: " + getClientAliasForLogging(myAccountId));
			
		if(isClientBanned(authorAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
	
		String signedString = authorAccountId + "," + packetLocalId + "," + myAccountId;
		if(!isSignatureCorrect(signedString, signature, myAccountId))
			return returnSingleResponseAndLog("", NetworkInterfaceConstants.SIG_ERROR);
		
		return	legacyDownloadPacket(authorAccountId, packetLocalId);
	}

	public Vector legacyDownloadPacket(String clientId, String packetId)
	{
		if(serverMaxLogging)
			logging("legacyDLPacket " + getClientAliasForLogging(clientId) + ": " + packetId);
		Vector result = new Vector();
		
		if(AttachmentPacket.isValidLocalId(packetId))
		{
			return returnSingleResponseAndLog("legacyDLPacket invalid for attachments", NetworkInterfaceConstants.INVALID_DATA);
		}

		Database db = getDatabase();
		UniversalId uid = UniversalId.createFromAccountAndLocalId(clientId, packetId);
		DatabaseKey key = DatabaseKey.createSealedKey(uid);
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
			logging("legacyDLPacket NotFound: " + getClientAliasForLogging(clientId) + " : " + packetId);
		}
		return result;
	}
	
	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		if(serverMaxLogging)
		{
			logging("downloadFieldOfficeDataPacket: " + getClientAliasForLogging(authorAccountId) + "  " + bulletinLocalId);
			logging("  packet " + packetLocalId + " requested by: " + getClientAliasForLogging(myAccountId));
		}
		
		if(isClientBanned(myAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
	
		Vector result = new Vector();

		String signedString = authorAccountId + "," + bulletinLocalId + "," + packetLocalId + "," + myAccountId;
		if(!isSignatureCorrect(signedString, signature, myAccountId))
		{
			return returnSingleResponseAndLog("", NetworkInterfaceConstants.SIG_ERROR);
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
		
		if(isClientBanned(myAccountId) )
			return returnSingleResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		if(!FieldDataPacket.isValidLocalId(packetLocalId))
		{
			return returnSingleResponseAndLog( "  attempt to download non-fielddatapacket: " + packetLocalId, NetworkInterfaceConstants.INVALID_DATA );
		}
		
		Database db = getDatabase();
		
		UniversalId headerUid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = DatabaseKey.createSealedKey(headerUid);
		
		if(!db.doesRecordExist(headerKey))
			headerKey.setDraft();
		
		if(!db.doesRecordExist(headerKey))
		{
			return returnSingleResponseAndLog( "  header packet not found", NetworkInterfaceConstants.NOT_FOUND );
		}
		
		UniversalId dataPacketUid = UniversalId.createFromAccountAndLocalId(authorAccountId, packetLocalId);
		DatabaseKey dataPacketKey = new DatabaseKey(dataPacketUid);
		if(headerKey.isDraft())
			dataPacketKey.setDraft();
		else
			dataPacketKey.setSealed();
			
		if(!db.doesRecordExist(dataPacketKey))
		{
			return returnSingleResponseAndLog( "  data packet not found", NetworkInterfaceConstants.NOT_FOUND );
		}
		
		try
		{
			if(!myAccountId.equals(authorAccountId) && 
					!myAccountId.equals(getBulletinHQAccountId(headerKey)) )
			{
				return returnSingleResponseAndLog( "  neither author nor HQ account", NetworkInterfaceConstants.NOTYOURBULLETIN );
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
	
	public boolean isClientBanned(String clientId)
	{
		return clientsBanned.contains(clientId);
	}

	public synchronized void allowUploads(String clientId)
	{
		if(serverMaxLogging)
			logging("allowUploads " + getClientAliasForLogging(clientId) + " : " + getPublicCode(clientId));
		clientsThatCanUpload.add(clientId);
		
		try
		{
			UnicodeWriter writer = new UnicodeWriter(allowUploadFile);
			for(int i = 0; i < clientsThatCanUpload.size(); ++i)
			{
				writer.writeln((String)clientsThatCanUpload.get(i));
			}
			writer.close();
			MartusUtilities.createSignatureFileFromFile(allowUploadFile, security);
			if(serverMaxLogging)
				logging("allowUploads : Exit OK");
		}
		catch(IOException e)
		{
			logging("allowUploads " + e);
			//System.out.println("MartusServer.allowUploads: " + e);
		}
		catch(MartusSignatureException e)
		{
			logging("allowUploads: " + e);
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
	
	public synchronized void loadListFromFile(BufferedReader readerInput, Vector result)
		throws IOException
	{
		try
		{
			while(true)
			{
				String currentLine = readerInput.readLine();
				if(currentLine == null)
					break;
				if(currentLine.length() == 0)
					continue;
					
				if( result.contains(currentLine) )
					continue;

				result.add(currentLine);
				//System.out.println("MartusServer.loadListFromFile: " + currentLine);
			}
		}
		catch(IOException e)
		{
			throw new IOException(e.getMessage());
		}
	}

	public synchronized void loadCanUploadList(BufferedReader canUploadInput)
	{
		if(serverMaxLogging)
			logging("loadCanUploadList");

		try
		{
			loadListFromFile(canUploadInput, clientsThatCanUpload);
		}
		catch (IOException e)
		{
			logging("loadCanUploadList -- Error loading can-upload list: " + e);
		}
		
		if(serverMaxLogging)
				logging("loadCanUploadList : Exit OK");
	}
	
	public synchronized void loadBannedClients()
	{
// Too much logging!
//		if(serverMaxLogging)
//			logging("loadBannedClients()");

		loadBannedClients(bannedClientsFile);
	}

	public void loadBannedClients(File bannedClientsFile)
	{
		try
		{
			long lastModified = bannedClientsFile.lastModified();
			if( lastModified != bannedClientsFileLastModified )
			{
				clientsBanned.clear();
				bannedClientsFileLastModified = lastModified;
				UnicodeReader reader = new UnicodeReader(bannedClientsFile);
				loadListFromFile(reader, clientsBanned);
				reader.close();
			}
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			clientsBanned.clear();
		}
		catch (IOException e)
		{
			logging("loadBannedClients: " + e);
		}
	}

	public static boolean keyBelongsToClient(DatabaseKey key, String clientId)
	{
		return clientId.equals(key.getAccountId());
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
	
	public static String getDefaultDataDirectoryPath()
	{
		String dataDirectory = null;
		if(System.getProperty("os.name").indexOf("Windows") >= 0)
		{
			dataDirectory = "C:/MartusServer/";
		}
		else
		{
			dataDirectory = "/var/MartusServer/";
		}
		return dataDirectory;
	}
	
	public static File getDefaultDataDirectory()
	{
		File file = new File(MartusServer.getDefaultDataDirectoryPath());
		if(!file.exists())
		{
			file.mkdirs();
		}
		
		return file;
	}
	
	public static String getKeypairFilename()
	{
		return KEYPAIRFILENAME;
	}
	
	private Vector returnSingleResponseAndLog( String message, String responseCode )
	{
		if( message.length() > 0 )
			logging( message.toString());
		
		Vector response = new Vector();
		response.add( responseCode );
		
		return response;
		
	}
	
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
				int chunkOffset, int maxChunkSize)
	{
		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
			return returnSingleResponseAndLog("getBulletinChunkWithoutVerifyingCaller:  NOT_FOUND ", NetworkInterfaceConstants.NOT_FOUND);
		
		try
		{
			return buildBulletinChunkResponse(headerKey, chunkOffset, maxChunkSize);
		}
		catch(Exception e)
		{
			return returnSingleResponseAndLog("getBulletinChunkWithoutVerifyingCaller:  SERVER_ERROR " + e, NetworkInterfaceConstants.SERVER_ERROR);
		}
	}


	public DatabaseKey findHeaderKeyInDatabase(String authorAccountId,String bulletinLocalId) 
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = new DatabaseKey(uid);
		headerKey.setSealed();
		if(getDatabase().doesRecordExist(headerKey))
			return headerKey;

		headerKey.setDraft();
		if(getDatabase().doesRecordExist(headerKey))
			return headerKey;

		return null;
	}

	private String saveUploadedBulletinZipFile(String authorAccountId, File zipFile) 
	{
		String result = NetworkInterfaceConstants.OK;
		
		try
		{
			MartusServerUtilities.saveZipFileToDatabase(getDatabase(), authorAccountId, zipFile, security);
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
		
		return result;
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
			FileTooLargeException,
			MartusUtilities.FileVerificationException 
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
		
		FileInputStream in = new FileInputStream(tempFile);
		in.skip(chunkOffset);
		in.read(rawData);
		in.close();
		
		String zipString = Base64.encode(rawData);
		
		int endPosition = chunkOffset + chunkSize;
		if(endPosition >= totalLength)
		{
			MartusUtilities.deleteInterimFileAndSignature(tempFile);
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

	public File createInterimBulletinFile(DatabaseKey headerKey) throws
			IOException,
			CryptoException,
			UnsupportedEncodingException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException,
			NoKeyPairException,
			FileNotFoundException,
			MartusUtilities.FileVerificationException
	{
		File tempFile = getDatabase().getOutgoingInterimFile(headerKey);
		File tempFileSignature = MartusUtilities.getSignatureFileFromFile(tempFile);
		if(tempFile.exists() && tempFileSignature.exists())
		{
			if(verifyBulletinInterimFile(tempFile, tempFileSignature, security.getPublicKeyString()))
				return tempFile;
		}
		MartusUtilities.deleteInterimFileAndSignature(tempFile);
		MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile(getDatabase(), headerKey, tempFile, security);
		tempFileSignature = MartusUtilities.createSignatureFileFromFile(tempFile, security);
		if(!verifyBulletinInterimFile(tempFile, tempFileSignature, security.getPublicKeyString()))
			throw new MartusUtilities.FileVerificationException();
		if(serverMaxLogging)
			logging("    Total file size =" + tempFile.length());
		
		return tempFile;
	}

	public boolean verifyBulletinInterimFile(File bulletinZipFile, File bulletinSignatureFile, String accountId)
	{
			try 
			{
				MartusUtilities.verifyFileAndSignature(bulletinZipFile, bulletinSignatureFile, security, accountId);
				return true;
			} 
			catch (MartusUtilities.FileVerificationException e) 
			{
				logging("    verifyBulletinInterimFile: " + e);
			}
		return false;	
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
	
	private String getClientAliasForLogging(String clientId)
	{
		return getDatabase().getFolderForAccount(clientId);
	}
	
	public boolean isShutdownRequested()
	{
		return(shutdownFile.exists());
	}
	
	public synchronized void clientConnectionStart()
	{
		//logging("start");
		incrementActiveClientsCounter();
	}
	
	public synchronized void clientConnectionExit()
	{
		//logging("exit");
		decrementActiveClientsCounter();
	}
	
	public synchronized int getNumberActiveClients()
	{
		return activeClientsCounter;
	}
	
	public synchronized void incrementActiveClientsCounter()
	{
		activeClientsCounter++;
	}
	
	public synchronized void decrementActiveClientsCounter()
	{
		activeClientsCounter--;
	}
	
	public synchronized void incrementFailedUploadRequests()
	{
		failedUploadRequestCounter++;
	}
	
	public synchronized void subtractMaxFailedUploadAttemptsFromCounter()
	{
		failedUploadRequestCounter -= getMaxFailedUploadAllowedAttempts();
		if(failedUploadRequestCounter < 0) failedUploadRequestCounter = 0;
	}
	
	public synchronized int getNumFailedUploadRequest()
	{
		return failedUploadRequestCounter;
	}
	
	public int getMaxFailedUploadAllowedAttempts()
	{
		return MAX_FAILED_UPLOAD_ATTEMPTS;
	}
	
	public long getUploadRequestTimerInterval()
	{
		return magicWordsGuessIntervalMillis;
	}
	
	synchronized boolean areUploadRequestsCurrentlyAllowed()
	{
		return (failedUploadRequestCounter < getMaxFailedUploadAllowedAttempts());
	}

	public synchronized void logging(String message)
	{
		if(serverLogging)
		{
			Thread currThread = Thread.currentThread();
			Timestamp stamp = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat formatDate = new SimpleDateFormat("EE MM/dd HH:mm:ss z");
			String threadId = null;
			
			if( XmlRpcThread.class.getName() == currThread.getClass().getName() )
			{
				threadId = ((XmlRpcThread) Thread.currentThread()).getClientAddress();
			}
			else
			{
				threadId = Integer.toHexString(currThread.hashCode());
			}
			
			String logEntry = formatDate.format(stamp) + " " + getServerName() + ": " + threadId + ": " + message;
			System.out.println(logEntry);
		}
	}
	
	public void setServerName(String servername)
	{
		serverName = servername;
	}
	
	String getServerName()
	{
		if(serverName == null)
			return "host/address";
		return serverName;
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
		InputStreamWithSeek in = db.openInputStream(key, security);
		bhp.loadFromXml(in, security);
		in.close();
		return bhp;
	}

	public static class DuplicatePacketException extends Exception
	{
		public DuplicatePacketException(String message)
		{
			super(message);
		}
	}
	
	public static class SealedPacketExistsException extends Exception
	{
		public SealedPacketExistsException(String message)
		{
			super(message);
		}
	}

	public void serverExit(int exitCode) throws Exception
	{
		System.exit(exitCode);
	}

	abstract class SummaryCollector implements Database.PacketVisitor
	{
		SummaryCollector(Database dbToUse, String accountIdToUse, Vector retrieveTagsToUse)
		{
			db = dbToUse;
			authorAccountId = accountIdToUse;
			retrieveTags = retrieveTagsToUse;
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
			if(retrieveTags.contains(NetworkInterfaceConstants.TAG_BULLETIN_SIZE))
			{
				int size = MartusUtilities.getBulletinSize(database, bhp);
				summary += "=" + size;
			}
			summaries.add(summary);
		}

		Database db;
		String authorAccountId;
		Vector summaries;
		Vector retrieveTags;
	}
	
	class MySealedSummaryCollector extends SummaryCollector
	{
		public MySealedSummaryCollector(Database dbToUse, String accountIdToUse, Vector retrieveTags) 
		{
			super(dbToUse, accountIdToUse, retrieveTags);
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
		public MyDraftSummaryCollector(Database dbToUse, String accountIdToUse, Vector retrieveTagsToUse) 
		{
			super(dbToUse, accountIdToUse, retrieveTagsToUse);
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
		public FieldOfficeSealedSummaryCollector(Database dbToUse, String hqAccountIdToUse, String authorAccountIdToUse, Vector retrieveTagsToUse) 
		{
			super(dbToUse, authorAccountIdToUse, retrieveTagsToUse);
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
		public FieldOfficeDraftSummaryCollector(Database dbToUse, String hqAccountIdToUse, String authorAccountIdToUse, Vector retrieveTagsToUse) 
		{
			super(dbToUse, authorAccountIdToUse, retrieveTagsToUse);
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


	private class BannedClientsMonitor extends TimerTask
	{
		public void run()
		{
			loadBannedClients();
		}
	}
	
	private class UploadRequestsMonitor extends TimerTask
	{
		public void run()
		{
			subtractMaxFailedUploadAttemptsFromCounter();
		}
	}
	
	private class ShutdownRequestMonitor extends TimerTask
	{
		public void run()
		{
			if( isShutdownRequested() && getNumberActiveClients() == 0 )
			{
				logging("Shutdown request received.");
				
				clientsThatCanUpload.clear();				
				shutdownFile.delete();
				logging("Server has exited.");
				try
				{
					serverExit(0);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private class MirroringTask extends TimerTask
	{
		public void run()
		{
			if(mirrorRetriever != null)
				mirrorRetriever.tick();
		}
	}
	

	Database database;
	ServerSideNetworkHandlerForNonSSL nonSSLServerHandler;
	ServerSideNetworkHandler serverHandler;
	SupplierSideMirroringHandler supplierHandler;
	MirroringRetriever mirrorRetriever;
	
	public Vector clientsThatCanUpload;
	public Vector clientsBanned;
	public MartusCrypto security;
	File keyPairFile;
	String serverName;
	public File dataDirectory;
	public File allowUploadFile;
	public File magicWordsFile;
	public File bannedClientsFile;
	public File shutdownFile;
	public File triggerDirectory;
	public File startupConfigDirectory;
	private Vector magicWords;
	private long bannedClientsFileLastModified;
	private int activeClientsCounter;
	private static boolean serverLogging;
	private static boolean serverMaxLogging;
	public static boolean serverSSLLogging;
	static int failedUploadRequestCounter;
	
	private static final String KEYPAIRFILENAME = "keypair.dat";
	private static final String MAGICWORDSFILENAME = "magicwords.txt";
	private static final String UPLOADSOKFILENAME = "uploadsok.txt";
	private static final String BANNEDCLIENTSFILENAME = "banned.txt";
	private static final String MARTUSSHUTDOWNFILENAME = "exit";
	
	private static final String ADMINTRIGGERDIRECTORY = "adminTriggers";
	private static final String ADMINSTARTUPCONFIGDIRECTORY = "deleteOnStartup";
	
	private final long IMMEDIATELY = 0;
	private final int MAX_FAILED_UPLOAD_ATTEMPTS = 100;
	private static final long magicWordsGuessIntervalMillis = 60 * 1000;
	private static final long bannedCheckIntervalMillis = 60 * 1000;
	private static final long shutdownRequestIntervalMillis = 1000;
	private static final long mirroringIntervalMillis = 1 * 1000;	// TODO: Probably 60 seconds
}
