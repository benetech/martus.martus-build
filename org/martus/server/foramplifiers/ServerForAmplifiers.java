package org.martus.server.foramplifiers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Vector;

import org.martus.common.AmplifierNetworkInterface;
import org.martus.common.MartusUtilities;
import org.martus.common.XmlRpcThread;
import org.martus.common.MartusUtilities.FileTooLargeException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.server.core.MartusSecureWebServer;
import org.martus.server.forclients.MartusServer;
import org.martus.util.Base64;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.Base64.InvalidBase64Exception;

public class ServerForAmplifiers implements NetworkInterfaceConstants
{
/*
	public static void main(String[] args)
	{
		System.out.println("MartusAmplifierServer");
		
		File dataDirectory = getDefaultDataDirectory();
		
		ServerForAmplifiers server = null;

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
			server = new ServerForAmplifiers(dataDirectory);
		} 
		catch(CryptoInitializationException e) 
		{
			System.out.println("Crypto Initialization Exception" + e);
			System.exit(1);			
		}
		
		server.setServerName(servername);
		
		
		System.out.println("Version " + ServerConstants.marketingVersionNumber);
		
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
*/

	public ServerForAmplifiers(MartusServer coreServerToUse) throws MartusCrypto.CryptoInitializationException
	{
		coreServer = coreServerToUse;
		
		amplifierHandler = new ServerSideAmplifierHandler(this);
	}
	
	public Database getDatabase()
	{
		return coreServer.getDatabase();
	}
	
	AmplifierNetworkInterface getAmplifierHandler()
	{
		return amplifierHandler;
	}

	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return false;
	}
	
	public String getAccountId()
	{
		return getSecurity().getPublicKeyString();
	}
	
	public void loadConfigurationFiles() throws IOException
	{
		
	}
	
	public void createAmplifierXmlRpcServer()
	{
		int port = AmplifierInterfaceXmlRpcConstants.MARTUS_PORT_FOR_AMPLIFIER;
		createAmplifierXmlRpcServerOnPort(port);
	}

	public void createAmplifierXmlRpcServerOnPort(int port)
	{
		if(MartusSecureWebServer.security == null)
			MartusSecureWebServer.security = getSecurity();

		log("Opening port " + port + " for amplifiers...");
		MartusAmplifierXmlRpcServer.createSSLXmlRpcServer(getAmplifierHandler(), port);
	}

	public Vector getServerInformation()
	{
		log("getServerInformation");
			
		if( coreServer.isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
				
		Vector result = new Vector();
		try
		{
			String publicKeyString = getSecurity().getPublicKeyString();
			byte[] publicKeyBytes = Base64.decode(publicKeyString);
			ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
			byte[] sigBytes = getSecurity().createSignatureOfStream(in);
			
			result.add(NetworkInterfaceConstants.OK);
			result.add(publicKeyString);
			result.add(Base64.encode(sigBytes));
			log("getServerInformation : Exit OK");
		}
		catch(Exception e)
		{
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			result.add(e.toString());
			log("getServerInformation SERVER ERROR" + e);			
		}
		return result;
	}


	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		log("downloadBulletin " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			
		if( coreServer.isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );

		Vector result = new Vector();
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = DatabaseKey.createSealedKey(uid);
		if(!getDatabase().doesRecordExist(headerKey))
		{
			log("downloadBulletin NOT_FOUND");
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
				log("downloadBulletin : Exit OK");
			}
			catch(Exception e)
			{
				log("downloadBulletin SERVER_ERROR " + e);
				//System.out.println("MartusAmplifierServer.download: " + e);
				result.add(NetworkInterfaceConstants.SERVER_ERROR);
			}
		}
		return result;
	}

	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int maxChunkSize) 
	{
		{
			StringBuffer logMsg = new StringBuffer();
			logMsg.append("getBulletinChunk request by " + getClientAliasForLogging(myAccountId));
			logMsg.append("  " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logMsg.append("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
			log(logMsg.toString());
		}
	
		if( coreServer.isShutdownRequested() )
			return returnSingleResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );

		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
			return returnSingleResponseAndLog( " returning NOT_FOUND", NetworkInterfaceConstants.NOT_FOUND );

		Vector result = getBulletinChunkWithoutVerifyingCaller(
					authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize);
		
		log("  exit: " + result.get(0));
		return result;
	}

	public String authenticateServer(String tokenToSign)
	{
		log("authenticateServer");
		try 
		{
			InputStream in = new ByteArrayInputStream(Base64.decode(tokenToSign));
			byte[] sig = getSecurity().createSignatureOfStream(in);
			return Base64.encode(sig);
		} 
		catch(MartusSignatureException e) 
		{
			log("SERVER_ERROR: " + e);
			return NetworkInterfaceConstants.SERVER_ERROR;
		} 
		catch(InvalidBase64Exception e) 
		{
			log("INVALID_DATA: " + e);
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
		getSecurity().readKeyPair(in, passphrase);
	}
	
	private Vector returnSingleResponseAndLog( String message, String responseCode )
	{
		if( message.length() > 0 )
			log( message.toString());
		
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
		log("entering createInterimBulletinFile");
		File tempFile = createInterimBulletinFile(headerKey);
		log("createInterimBulletinFile done");
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
		log("downloadBulletinChunk : Exit " + result.get(0));
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
			if(verifyBulletinInterimFile(tempFile, tempFileSignature, getSecurity().getPublicKeyString()))
				return tempFile;
		}
		MartusUtilities.deleteInterimFileAndSignature(tempFile);
		BulletinZipUtilities.exportPublicBulletinPacketsFromDatabaseToZipFile(getDatabase(), headerKey, tempFile, getSecurity());
		tempFileSignature = MartusUtilities.createSignatureFileFromFile(tempFile, getSecurity());
		if(!verifyBulletinInterimFile(tempFile, tempFileSignature, getSecurity().getPublicKeyString()))
			throw new MartusUtilities.FileVerificationException();
		log("    Total file size =" + tempFile.length());
		
		return tempFile;
	}

	public boolean verifyBulletinInterimFile(File bulletinZipFile, File bulletinSignatureFile, String accountId)
	{
			try 
			{
				MartusUtilities.verifyFileAndSignature(bulletinZipFile, bulletinSignatureFile, getSecurity(), accountId);
				return true;
			} 
			catch (MartusUtilities.FileVerificationException e) 
			{
				log("    verifyBulletinInterimFile: " + e);
			}
		return false;	
	}
	
	private String getClientAliasForLogging(String clientId)
	{
		return getDatabase().getFolderForAccount(clientId);
	}
	
	public synchronized void clientConnectionStart()
	{
		//logging("start");
	}
	
	public synchronized void clientConnectionExit()
	{
		//logging("exit");
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

	public synchronized void log(String message)
	{
		coreServer.log(message);
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
		InputStreamWithSeek in = db.openInputStream(key, getSecurity());
		bhp.loadFromXml(in, getSecurity());
		in.close();
		return bhp;
	}

	public MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}

	MartusServer coreServer;

	ServerSideAmplifierHandler amplifierHandler;
}
