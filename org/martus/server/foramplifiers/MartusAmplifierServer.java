package org.martus.server.foramplifiers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.martus.common.AmplifierNetworkInterface;
import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.BulletinZipUtilities;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.UniversalId;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.Database.RecordHiddenException;
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
import org.martus.server.core.MartusSecureWebServer;
import org.martus.server.core.ServerConstants;
import org.martus.server.core.ServerFileDatabase;
import org.martus.server.core.XmlRpcThread;

public class MartusAmplifierServer implements NetworkInterfaceConstants
{

	public static void main(String[] args)
	{
		System.out.println("MartusAmplifierServer");
		
		File dataDirectory = getDefaultDataDirectory();
		
		MartusAmplifierServer server = null;

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
			
			if(args[arg].startsWith("--server-name="))
			{
				servername = args[arg].substring(args[arg].indexOf("=")+1);
			}
		}
		
		System.out.println("Initializing...this will take a few seconds...");
		try
		{
			server = new MartusAmplifierServer(dataDirectory);
		} 
		catch(CryptoInitializationException e) 
		{
			System.out.println("Crypto Initialization Exception" + e);
			System.exit(1);			
		}
		
		server.setServerName(servername);
		
		
		System.out.println("Version " + ServerConstants.internalVersionForScriptCompatibility);
		
		String versionInfo = MartusUtilities.getVersionDate();
		System.out.println("Build Date " + versionInfo);

		System.out.print("Enter passphrase: ");
		System.out.flush();

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

			String accountId = server.getAccountId();
			System.out.println("Server Account: " + accountId);
			System.out.println();

			System.out.print("Server Public Code: ");
			String publicCode = MartusCrypto.computePublicCode(accountId);
			System.out.println(MartusCrypto.formatPublicCode(publicCode));
			System.out.println();
		}
		catch(IOException e)
		{
			System.out.println("MartusAmplifierServer.main: " + e);
			System.exit(3);
		}
		catch (InvalidBase64Exception e)
		{
			System.out.println("MartusAmplifierServer.main: " + e);
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
				
		System.out.println("Setting up sockets (this may take up to a minute or longer)...");
		server.createAmplifierXmlRpcServer();
		System.out.println("Waiting for connection...");
	}


	MartusAmplifierServer(File dir) throws MartusCrypto.CryptoInitializationException
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

		amplifierHandler = new ServerSideAmplifierHandler(this);
		keyPairFile = new File(startupConfigDirectory, getKeypairFilename());
		
		shutdownFile = new File(triggerDirectory, MARTUSSHUTDOWNFILENAME);
		
		Timer shutdownRequestTimer = new Timer(true);
 		TimerTask shutdownRequestTaskMonitor = new ShutdownRequestMonitor();
 		shutdownRequestTimer.schedule(shutdownRequestTaskMonitor, IMMEDIATELY, shutdownRequestIntervalMillis);
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
	
	AmplifierNetworkInterface getAmplifierHandler()
	{
		return amplifierHandler;
	}

	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return false;
	}
	
	boolean hasAccount()
	{
		return keyPairFile.exists();
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
	
	public void createAmplifierXmlRpcServer()
	{
		int port = AmplifierInterfaceXmlRpcConstants.MARTUS_PORT_FOR_AMPLIFIER;
		createAmplifierXmlRpcServerOnPort(port);
	}

	public void createAmplifierXmlRpcServerOnPort(int port)
	{
		if(MartusSecureWebServer.security == null)
			MartusSecureWebServer.security = security;

		MartusAmplifierXmlRpcServer.createSSLXmlRpcServer(getAmplifierHandler(), port);
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
			byte[] sigBytes = security.createSignatureOfStream(in);
			
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
				//System.out.println("MartusAmplifierServer.download: " + e);
				result.add(NetworkInterfaceConstants.SERVER_ERROR);
			}
		}
		return result;
	}

	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int maxChunkSize) 
	{
		if(serverMaxLogging)
		{
			StringBuffer logMsg = new StringBuffer();
			logMsg.append("getBulletinChunk request by " + getClientAliasForLogging(myAccountId));
			logMsg.append("  " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logMsg.append("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
			logging(logMsg.toString());
		}
	
		if( isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );

		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
			return returnSingleResponseAndLog( " returning NOT_FOUND", NetworkInterfaceConstants.NOT_FOUND );

		Vector result = getBulletinChunkWithoutVerifyingCaller(
					authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize);
		
		if(serverMaxLogging)
			logging("  exit: " + result.get(0));
		return result;
	}

	public String authenticateServer(String tokenToSign)
	{
		if(serverMaxLogging)
			logging("authenticateServer");
		try 
		{
			InputStream in = new ByteArrayInputStream(Base64.decode(tokenToSign));
			byte[] sig = security.createSignatureOfStream(in);
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

	public String getPublicCode(String clientId) 
	{
		String formattedCode = "";
		try 
		{
			String publicCode = MartusCrypto.computePublicCode(clientId);
			formattedCode = MartusCrypto.formatPublicCode(publicCode);
		} 
		catch(InvalidBase64Exception e) 
		{
		}
		return formattedCode;
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
	
	public static File getDefaultDataDirectory()
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
		File file = new File(dataDirectory);
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
	
	private Vector buildBulletinChunkResponse(DatabaseKey headerKey, int chunkOffset, int maxChunkSize) throws
			FileTooLargeException,
			InvalidPacketException, 
			WrongPacketTypeException, 
			SignatureVerificationException, 
			DecryptionException, 
			NoKeyPairException, 
			CryptoException, 
			FileVerificationException, 
			IOException, 
			RecordHiddenException 
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
			CryptoException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException,
			NoKeyPairException,
			MartusUtilities.FileVerificationException, IOException, RecordHiddenException
	{
		File tempFile = getDatabase().getOutgoingInterimFile(headerKey);
		File tempFileSignature = MartusUtilities.getSignatureFileFromFile(tempFile);
		if(tempFile.exists() && tempFileSignature.exists())
		{
			if(verifyBulletinInterimFile(tempFile, tempFileSignature, security.getPublicKeyString()))
				return tempFile;
		}
		MartusUtilities.deleteInterimFileAndSignature(tempFile);
		BulletinZipUtilities.exportPublicBulletinPacketsFromDatabaseToZipFile(getDatabase(), headerKey, tempFile, security);
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
			return security.isValidSignatureOfStream(signerPublicKey, in, Base64.decode(signature));
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
	
	protected String getCurrentClientIp()
	{
		String ip;
		Thread currThread = Thread.currentThread();
		if( XmlRpcThread.class.getName() == currThread.getClass().getName() )
		{
			ip = ((XmlRpcThread) Thread.currentThread()).getClientAddress();
		}
		else
		{
			ip = Integer.toHexString(currThread.hashCode());
		}

		return ip;
	}

	public synchronized void logging(String message)
	{
		if(serverLogging)
		{
			Timestamp stamp = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat formatDate = new SimpleDateFormat("EE MM/dd HH:mm:ss z");
			String threadId = getCurrentClientIp();
			
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

	public void serverExit(int exitCode) throws Exception
	{
		System.exit(exitCode);
	}
	
	private class ShutdownRequestMonitor extends TimerTask
	{
		public void run()
		{
			if( isShutdownRequested() && getNumberActiveClients() == 0 )
			{
				logging("Shutdown request received.");

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
	

	Database database;

	ServerSideAmplifierHandler amplifierHandler;
	
	public MartusCrypto security;
	File keyPairFile;
	String serverName;
	public File dataDirectory;

	public File shutdownFile;
	public File triggerDirectory;
	public File startupConfigDirectory;

	private int activeClientsCounter;
	private static boolean serverLogging;
	private static boolean serverMaxLogging;
	public static boolean serverSSLLogging;

	
	private static final String KEYPAIRFILENAME = "keypair.dat";
	private static final String MARTUSSHUTDOWNFILENAME = "amp-exit";
	
	private static final String ADMINTRIGGERDIRECTORY = "adminTriggers";
	private static final String ADMINSTARTUPCONFIGDIRECTORY = "deleteOnStartup";
	
	private final long IMMEDIATELY = 0;
	private static final long shutdownRequestIntervalMillis = 1000;

}
