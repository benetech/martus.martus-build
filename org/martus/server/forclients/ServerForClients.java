package org.martus.server.forclients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceXmlRpcConstants;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.server.core.MartusSecureWebServer;
import org.martus.server.core.MartusXmlRpcServer;

public class ServerForClients implements ServerForNonSSLClientsInterface, ServerForClientsInterface
{
	public ServerForClients(MartusServer coreServerToUse)
	{
		coreServer = coreServerToUse;
		clientsBanned = new Vector();
		magicWords = new Vector();
		clientsThatCanUpload = new Vector();

	}
	
	public MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}
	
	public String getPublicCode(String clientId)
	{
		return coreServer.getPublicCode(clientId); 
	}
	
	public synchronized void logging(String message)
	{
		coreServer.logging(message);
	}
	
	void displayClientStatistics()
	{
		System.out.println();
		System.out.println(clientsThatCanUpload.size() + " client(s) currently allowed to upload");
		System.out.println(clientsBanned.size() + " client(s) are currently banned");
		System.out.println(magicWords.size() + " active magic word(s)");
	}

	public void verifyConfigurationFiles()
	{
		File allowUploadFileSignature = MartusUtilities.getSignatureFileFromFile(getAllowUploadFile());
		if(getAllowUploadFile().exists() || allowUploadFileSignature.exists())
		{
			try
			{
				MartusCrypto security = getSecurity();
				MartusUtilities.verifyFileAndSignature(getAllowUploadFile(), allowUploadFileSignature, security, security.getPublicKeyString());
			}
			catch(FileVerificationException e)
			{
				e.printStackTrace();
				System.out.println(UPLOADSOKFILENAME + " did not verify against signature file");
				System.exit(7);
			}
		}
	}

	public void loadConfigurationFiles() throws IOException
	{
		loadBannedClients();
		loadCanUploadFile();
		loadMagicWordsFile();
	}

	void prepareToShutdown()
	{
		clearCanUploadList();
	}

	public boolean isClientBanned(String clientId)
	{
		if(clientsBanned.contains(clientId))
		{
			logging("client BANNED : ");
			return true;
		}
		return false;
	}

	public boolean canExitNow()
	{
		return (getNumberActiveClients() == 0);
	}
	
	synchronized int getNumberActiveClients()
	{
		return activeClientsCounter;
	}
	
	
	public synchronized void clientConnectionStart()
	{
		activeClientsCounter++;
	}
	
	public synchronized void clientConnectionExit()
	{
		activeClientsCounter--;
	}
	
	public void handleNonSSL()
	{
		ServerSideNetworkHandlerForNonSSL nonSSLServerHandler = new ServerSideNetworkHandlerForNonSSL(this);
		MartusXmlRpcServer.createNonSSLXmlRpcServer(nonSSLServerHandler, NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_NON_SSL);
	}
	
	public void handleSSL(int port)
	{
		ServerSideNetworkHandler serverHandler = new ServerSideNetworkHandler(this);
		MartusSecureWebServer.security = getSecurity();
		MartusXmlRpcServer.createSSLXmlRpcServer(serverHandler, "MartusServer", port);
	}
	
	




	// BEGIN SSL interface
	public String deleteDraftBulletins(String accountId, String[] localIds)
	{
		return coreServer.deleteDraftBulletins(accountId, localIds);
	}

	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		return coreServer.downloadFieldDataPacket(authorAccountId, bulletinLocalId, packetLocalId, myAccountId, signature);
	}

	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize)
	{
		return coreServer.getBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize);
	}

	public Vector getNews(String myAccountId, String versionLabel, String versionBuildDate)
	{
		return coreServer.getNews(myAccountId, versionLabel, versionBuildDate);
	}

	public Vector getPacket(String myAccountId, String authorAccountId, String bulletinLocalId, String packetLocalId)
	{
		return coreServer.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
	}

	public Vector getServerCompliance()
	{
		return coreServer.getServerCompliance();
	}

	public Vector listMySealedBulletinIds(String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listMySealedBulletinIds(authorAccountId, retrieveTags);
	}

	public String putBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data)
	{
		return coreServer.putBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, totalSize, chunkOffset, chunkSize, data);
	}

	public String putContactInfo(String myAccountId, Vector parameters)
	{
		return coreServer.putContactInfo(myAccountId, parameters);
	}

	public Vector legacyDownloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		return coreServer.legacyDownloadAuthorizedPacket(authorAccountId, packetLocalId, myAccountId, signature);
	}

	public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listFieldOfficeDraftBulletinIds(hqAccountId, authorAccountId, retrieveTags);
	}

	public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId, retrieveTags);
	}

	public Vector listMyDraftBulletinIds(String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listMyDraftBulletinIds(authorAccountId, retrieveTags);
	}

	// begin NON-SSL interface (sort of)
	public String authenticateServer(String tokenToSign)
	{
		return coreServer.authenticateServer(tokenToSign);
	}

	public String ping()
	{
		return coreServer.ping();
	}
	
	public Vector getServerInformation()
	{
		return coreServer.getServerInformation();
	}
	
	public String requestUploadRights(String clientId, String tryMagicWord)
	{
		return coreServer.requestUploadRights(clientId, tryMagicWord);
	}
	
	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		return coreServer.listFieldOfficeAccounts(hqAccountId);
	}
	
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		return coreServer.uploadBulletinChunk(authorAccountId, bulletinLocalId, totalSize, chunkOffset, chunkSize, data, signature);
	}
	
	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		return coreServer.uploadBulletin(authorAccountId, bulletinLocalId, data);
	}
	
	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		return coreServer.downloadBulletin(authorAccountId, bulletinLocalId);
	}

	public Vector legacyDownloadPacket(String clientId, String packetId)
	{
		return coreServer.legacyDownloadPacket(clientId, packetId);
	}

	public Vector legacyListFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId)
	{
		return coreServer.legacyListFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
	}

	File getBannedFile()
	{
		return new File(coreServer.getStartupConfigDirectory(), BANNEDCLIENTSFILENAME);
	}

	public synchronized void loadBannedClients()
	{
		loadBannedClients(getBannedFile());
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
				coreServer.loadListFromFile(reader, clientsBanned);
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

	void deleteBannedFile()
	{
		if(getBannedFile().exists())
		{
			if(!getBannedFile().delete())
			{
				System.out.println("Unable to delete " + getBannedFile().getAbsolutePath() );
				System.exit(5);
			}
		}
	}

	public boolean isValidMagicWord(String tryMagicWord)
	{
		return (magicWords.contains(normalizeMagicWord(tryMagicWord)));
	}
	
	public void addMagicWord(String newMagicWord)
	{
		if( !magicWords.contains(newMagicWord) )
			magicWords.add(newMagicWord);
	}
	
	public File getMagicWordsFile()
	{
		return new File(coreServer.getStartupConfigDirectory(), MAGICWORDSFILENAME);
	}

	void loadMagicWordsFile()
	{
		try
		{
			UnicodeReader reader = new UnicodeReader(getMagicWordsFile());
			String line = null;
			while( (line = reader.readLine()) != null)
				addMagicWord(normalizeMagicWord(line));
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

	void deleteMagicWordsFile()
	{
		if(!getMagicWordsFile().delete())
		{
			System.out.println("Unable to delete magicwords");
			System.exit(4);
		}
	}

	static String normalizeMagicWord(String original)
	{
		return original.toLowerCase().trim().replaceAll("\\s", "");
	}

	public boolean canClientUpload(String clientId)
	{
		return clientsThatCanUpload.contains(clientId);
	}
	
	public void clearCanUploadList()
	{
		clientsThatCanUpload.clear();
	}

	public synchronized void allowUploads(String clientId)
	{
		logging("allowUploads " + coreServer.getClientAliasForLogging(clientId) + " : " + getPublicCode(clientId));
		clientsThatCanUpload.add(clientId);
		
		try
		{
			UnicodeWriter writer = new UnicodeWriter(getAllowUploadFile());
			for(int i = 0; i < clientsThatCanUpload.size(); ++i)
			{
				writer.writeln((String)clientsThatCanUpload.get(i));
			}
			writer.close();
			MartusCrypto security = getSecurity();
			MartusUtilities.createSignatureFileFromFile(getAllowUploadFile(), security);
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

	public File getAllowUploadFile()
	{
		return new File(coreServer.dataDirectory, UPLOADSOKFILENAME);
	}

	void loadCanUploadFile()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(getAllowUploadFile()));
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
	}
	
	public synchronized void loadCanUploadList(BufferedReader canUploadInput)
	{
		logging("loadCanUploadList");

		try
		{
			coreServer.loadListFromFile(canUploadInput, clientsThatCanUpload);
		}
		catch (IOException e)
		{
			logging("loadCanUploadList -- Error loading can-upload list: " + e);
		}
		
		logging("loadCanUploadList : Exit OK");
	}
	


	MartusServer coreServer;
	private int activeClientsCounter;
	Vector magicWords;
	public Vector clientsThatCanUpload;
	public Vector clientsBanned;
	private long bannedClientsFileLastModified;

	private static final String BANNEDCLIENTSFILENAME = "banned.txt";
	private static final String MAGICWORDSFILENAME = "magicwords.txt";
	private static final String UPLOADSOKFILENAME = "uploadsok.txt";
}
