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

package org.martus.server.formirroring;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.server.core.LoggerInterface;
import org.martus.server.core.MartusXmlRpcServer;
import org.martus.server.forclients.MartusServer;
import org.martus.server.forclients.MartusServerUtilities;
import org.martus.server.formirroring.CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException;
import org.martus.util.Base64;
import org.martus.util.InputStreamWithSeek;

public class ServerForMirroring implements ServerSupplierInterface
{
	public ServerForMirroring(MartusServer coreServerToUse, LoggerInterface loggerToUse) throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException  
	{
		coreServer = coreServerToUse;
		logger = loggerToUse;
	}

	public void log(String message)
	{
		logger.log(message);
	}
	
	public File getMirrorConfigFile()
	{
		return new File(coreServer.getStartupConfigDirectory(), MIRRORCONFIGFILENAME);
	}
	
	public void verifyConfigurationFiles()
	{
		// nothing to do yet
	}
	
	public void loadConfigurationFiles() throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		if(getMirrorConfigFile().exists())
		{
			long oneSecondOfMillis = 1000;
			long oneMinuteOfMillis = 60 * oneSecondOfMillis;

			mirroringIntervalMillis = oneSecondOfMillis;
			inactiveSleepMillis = oneMinuteOfMillis;
		}
		log("MirroringInterval (millis): " + mirroringIntervalMillis);
		log("InactiveSleep (millis): " + inactiveSleepMillis);

		authorizedCallers = new Vector();
		loadServersWhoAreAuthorizedToCallUs();
		log("Authorized " + authorizedCallers.size() + " Mirrors to call us");
	}

	public void deleteConfigurationFiles()
	{
		getMirrorConfigFile().delete();
	}

	public void addListeners() throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		log("Initializing ServerForMirroring");
		
		int port = MirroringInterface.MARTUS_PORT_FOR_MIRRORING;
		log("Opening port " + port + " for mirroring...");
		SupplierSideMirroringHandler supplierHandler = new SupplierSideMirroringHandler(this, getSecurity());
		MartusXmlRpcServer.createSSLXmlRpcServer(supplierHandler, MirroringInterface.DEST_OBJECT_NAME, port);

		MartusUtilities.startTimer(new MirroringTask(retrieversWeWillCall), mirroringIntervalMillis);
		log("Mirroring port opened and mirroring task scheduled");
	}

	// Begin ServerSupplierInterface
	public Vector getPublicInfo()
	{
		try
		{
			Vector result = new Vector();
			result.add(getSecurity().getPublicKeyString());
			result.add(getSecurity().getSignatureOfPublicKey());
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Vector();
		}
	}
	
	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return authorizedCallers.contains(callerAccountId);
	}

	public Vector listAccountsForMirroring()
	{
		class Collector implements Database.AccountVisitor
		{
			public void visit(String accountId)
			{
				accounts.add(accountId);
			}
			
			Vector accounts = new Vector();
		}

		Collector collector = new Collector();		
		getDatabase().visitAllAccounts(collector);
		return collector.accounts;
	}

	public Vector listBulletinsForMirroring(String authorAccountId)
	{
		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					if(key.isDraft())
						return;
					if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
						return;
					InputStreamWithSeek in = getDatabase().openInputStream(key, null);
					byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, getSecurity());
					in.close();
					String sigString = Base64.encode(sigBytes);
					Vector info = new Vector();
					info.add(key.getLocalId());
					info.add(sigString);
					infos.add(info);
				}
				catch (Exception e)
				{
					log("Error: listBulletins " + e);
				}
			}
			
			Vector infos = new Vector();
		}

		Collector collector = new Collector();		
		getDatabase().visitAllRecordsForAccount(collector, authorAccountId);
		return collector.infos;
	}
	
	public String getBulletinUploadRecord(String authorAccountId, String bulletinLocalId)
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = new DatabaseKey(uid);
		DatabaseKey burKey = MartusServerUtilities.getBurKey(headerKey);
		try
		{
			String bur = getDatabase().readRecord(burKey, getSecurity());
			return bur;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize)
	{
		return coreServer.getBulletinChunkWithoutVerifyingCaller(authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize);
	}
	//End ServerSupplierInterface

	MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}

	Database getDatabase()
	{
		return coreServer.getDatabase();
	}
	
	boolean isSecureMode()
	{
		return coreServer.isSecureMode();
	}

	void loadServersWhoAreAuthorizedToCallUs() throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		authorizedCallers.clear();

		File authorizedCallersDir = getAuthorizedCallersDirectory();
		File[] callersFiles = authorizedCallersDir.listFiles();
		if(callersFiles == null)
			return;
		for (int i = 0; i < callersFiles.length; i++)
		{
			File callerFile = callersFiles[i];
			Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(callerFile, getSecurity());
			String accountId = (String)publicInfo.get(0);
			addAuthorizedCaller(accountId);
			if(isSecureMode())
			{
				callerFile.delete();
				if(callerFile.exists())
					throw new IOException("delete failed: " + callerFile);
			}
			log("Authorized to call us: " + callerFile.getName());
		}
	}

	File getAuthorizedCallersDirectory()
	{
		return new File(coreServer.getStartupConfigDirectory(), "mirrorsWhoCallUs");
	}
	
	void addAuthorizedCaller(String publicKey)
	{
		authorizedCallers.add(publicKey);
	}
	
	File getMirrorsWeWillCallDirectory()
	{
		return new File(coreServer.getStartupConfigDirectory(), "mirrorsWhoWeCall");		
	}
	
	public void createGatewaysWeWillCall() throws 
			IOException, InvalidPublicKeyFileException, PublicInformationInvalidException, SSLSocketSetupException
	{
		retrieversWeWillCall = new Vector();

		File toCallDir = getMirrorsWeWillCallDirectory();
		File[] toCallFiles = toCallDir.listFiles();
		if(toCallFiles == null)
			return;
		for (int i = 0; i < toCallFiles.length; i++)
		{
			File toCallFile = toCallFiles[i];
			retrieversWeWillCall.add(createRetrieverToCall(toCallFile));
			if(isSecureMode())
			{
				toCallFile.delete();
				if(toCallFile.exists())
					throw new IOException("delete failed: " + toCallFile);
			}
			log("We will call: " + toCallFile.getName());
		}
		log("Configured to call " + retrieversWeWillCall.size() + " Mirrors");
	}
	
	MirroringRetriever createRetrieverToCall(File publicKeyFile) throws
			IOException, 
			InvalidPublicKeyFileException, 
			PublicInformationInvalidException, 
			SSLSocketSetupException
	{
		String ip = extractIpFromFileName(publicKeyFile.getName());
		CallerSideMirroringGateway gateway = createGatewayToCall(ip, publicKeyFile);
		MirroringRetriever retriever = new MirroringRetriever(getDatabase(), gateway, ip, logger, getSecurity());
		return retriever;
	}
	
	CallerSideMirroringGateway createGatewayToCall(String ip, File publicKeyFile) throws 
			IOException, 
			InvalidPublicKeyFileException, 
			PublicInformationInvalidException, 
			SSLSocketSetupException
	{
		int port = MirroringInterface.MARTUS_PORT_FOR_MIRRORING;
		Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(publicKeyFile, getSecurity());
		String publicKey = (String)publicInfo.get(0);

		CallerSideMirroringGatewayForXmlRpc xmlRpcGateway = new CallerSideMirroringGatewayForXmlRpc(ip, port); 
		xmlRpcGateway.setExpectedPublicKey(publicKey);
		return new CallerSideMirroringGateway(xmlRpcGateway);
	}

	static String extractIpFromFileName(String fileName) throws 
		InvalidPublicKeyFileException 
	{
		final String ipStartString = "ip=";
		int ipStart = fileName.indexOf(ipStartString);
		if(ipStart < 0)
			throw new InvalidPublicKeyFileException();
		ipStart += ipStartString.length();
		int ipEnd = ipStart;
		for(int i=0; i < 3; ++i)
		{
			ipEnd = fileName.indexOf(".", ipEnd+1);
			if(ipEnd < 0)
				throw new InvalidPublicKeyFileException();
		}
		++ipEnd;
		while(ipEnd < fileName.length() && Character.isDigit(fileName.charAt(ipEnd)))
			++ipEnd;
		String ip = fileName.substring(ipStart, ipEnd);
		return ip;
	}
	
	private class MirroringTask extends TimerTask
	{
		MirroringTask(Vector retrieversToUse)
		{
			retrievers = retrieversToUse;
		}
		
		public void run()
		{
			protectedRun();
		}
		
		synchronized void protectedRun()
		{
			if(retrievers.size() == 0)
				return;
			++nextRetriever;
			if(nextRetriever >= retrievers.size())
				nextRetriever = 0;
				
			MirroringRetriever thisRetriever = (MirroringRetriever)retrievers.get(nextRetriever);
			thisRetriever.tick();
		}

		int nextRetriever;
		Vector retrievers;
	}
	
	MartusServer coreServer;
	LoggerInterface logger;
	Vector authorizedCallers;
	MirroringRetriever retriever;
	Vector retrieversWeWillCall;

	static final String MIRRORCONFIGFILENAME = "mirrorConfig.txt";	
	static long mirroringIntervalMillis = 10 * 1000;	// TODO: Probably 60 seconds
	static long inactiveSleepMillis = 60 * 60 * 1000;
}
