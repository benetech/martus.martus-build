/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002,2003, Beneficent
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

package org.martus.server.forclients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
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
	
	public synchronized void log(String message)
	{
		coreServer.log(message);
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
		try
		{
			File allowUploadFileSignature = MartusServerUtilities.getLatestSignatureFileFromFile(getAllowUploadFile());
			MartusCrypto security = getSecurity();
			MartusServerUtilities.verifyFileAndSignatureOnServer(getAllowUploadFile(), allowUploadFileSignature, security, security.getPublicKeyString());
		}
		catch(FileVerificationException e)
		{
			e.printStackTrace();
			System.out.println(UPLOADSOKFILENAME + " did not verify against signature file");
			System.exit(7);
		}
		catch(Exception e)
		{
			if(getAllowUploadFile().exists())
			{
				e.printStackTrace();
				System.out.println("Unable to verify " + UPLOADSOKFILENAME + " against a signature file");
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
			log("client BANNED : ");
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
	
	public void handleNonSSL(int[] ports)
	{
		ServerSideNetworkHandlerForNonSSL nonSSLServerHandler = new ServerSideNetworkHandlerForNonSSL(this);
		for(int i=0; i < ports.length; ++i)
			MartusXmlRpcServer.createNonSSLXmlRpcServer(nonSSLServerHandler, ports[i]);
	}
	
	public void handleSSL(int[] ports)
	{
		ServerSideNetworkHandler serverHandler = new ServerSideNetworkHandler(this);
		MartusSecureWebServer.security = getSecurity();
		for(int i=0; i < ports.length; ++i)
			MartusXmlRpcServer.createSSLXmlRpcServer(serverHandler, "MartusServer", ports[i]);
	}
	
	




	// BEGIN SSL interface
	public String deleteDraftBulletins(String accountId, String[] localIds)
	{
		return coreServer.deleteDraftBulletins(accountId, localIds);
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
			log("loadBannedClients: " + e);
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
		log("allowUploads " + coreServer.getClientAliasForLogging(clientId) + " : " + getPublicCode(clientId));
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
			MartusServerUtilities.createSignatureFileFromFileOnServer(getAllowUploadFile(), security);
			log("allowUploads : Exit OK");
		}
		catch(Exception e)
		{
			log("allowUploads " + e);
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
		log("loadCanUploadList");

		try
		{
			coreServer.loadListFromFile(canUploadInput, clientsThatCanUpload);
		}
		catch (IOException e)
		{
			log("loadCanUploadList -- Error loading can-upload list: " + e);
		}
		
		log("loadCanUploadList : Exit OK");
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
