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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.Base64.InvalidBase64Exception;

public class MirroringRetriever
{
	public MirroringRetriever(Database databaseToUse, CallerSideMirroringGatewayInterface gatewayToUse, 
						String ipToUse, LoggerInterface loggerToUse, MartusCrypto securityToUse)
	{
		db = databaseToUse;
		gateway = gatewayToUse;
		ip = ipToUse;
		logger = loggerToUse;
		security = securityToUse;
		
		uidsToRetrieve = new Vector();
		accountsToRetrieve = new Vector();
	}
	
	static class MissingBulletinUploadRecordException extends Exception {}
	
	public void tick()
	{
		UniversalId uid = getNextUidToRetrieve();
		if(uid == null)
			return;
			
		shouldSleepNextCycle = false;
			
		try
		{
			String publicCode = MartusCrypto.getFormattedPublicCode(uid.getAccountId());
			log("Getting bulletin: " + publicCode + "->" + uid.getLocalId());
			String bur = retrieveBurFromMirror(uid);
			File zip = File.createTempFile("$$$MirroringRetriever", null);
			try
			{
				zip.deleteOnExit();
				retrieveOneBulletin(zip, uid);
				BulletinHeaderPacket bhp = MartusServerUtilities.saveZipFileToDatabase(db, uid.getAccountId(), zip, security);
				MartusServerUtilities.writeSpecificBurToDatabase(db, bhp, bur);
			}
			finally
			{
				zip.delete();
			}
		}
		catch(ServerErrorException e)
		{
			log("Supplier server error: " + e);
		}
		catch(ServerNotAvailableException e)
		{
			// TODO: Notify once per hour that something is wrong
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	static class ServerNotAvailableException extends Exception {}

	private String retrieveBurFromMirror(UniversalId uid)
		throws MartusSignatureException, MissingBulletinUploadRecordException, ServerNotAvailableException
	{
		NetworkResponse response = gateway.getBulletinUploadRecord(security, uid);
		String resultCode = response.getResultCode();
		if(resultCode.equals(NetworkInterfaceConstants.NO_SERVER))
		{
			throw new ServerNotAvailableException();
		}
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
		{
			throw new MissingBulletinUploadRecordException();
		}
		String bur = (String)response.getResultVector().get(0);
		return bur;
	}
	
	UniversalId getNextUidToRetrieve()
	{
		try
		{
			if(uidsToRetrieve.size() > 0)
			{
				return (UniversalId)uidsToRetrieve.remove(0);
			}

			String nextAccountId = getNextAccountToRetrieve();
			if(nextAccountId == null)
				return null;

			String publicCode = MartusCrypto.getFormattedPublicCode(nextAccountId);
			//log("listBulletins: " + publicCode);
			NetworkResponse response = gateway.listBulletinsForMirroring(security, nextAccountId);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				Vector infos = response.getResultVector();
				uidsToRetrieve = listOnlyPacketsThatWeWant(nextAccountId, infos);
				if(infos.size()>0 || uidsToRetrieve.size()>0)
					log("listBulletins: " + publicCode + 
						" -> " + infos.size() + " -> " + uidsToRetrieve.size());
			}
		}
		catch (Exception e)
		{
			// TODO: Better error handling
			e.printStackTrace();
			System.out.println("MirroringRetriever.getNextUidToRetrieve: " + e);
		}

		return null;
	}

	Vector listOnlyPacketsThatWeWant(String accountId, Vector infos)
	{
		Vector uids = new Vector();
		for(int i=0; i < infos.size(); ++i)
		{
			Vector info = (Vector)infos.get(i);
			String localId = (String)info.get(0);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
			DatabaseKey key = new DatabaseKey(uid);
			if(!db.doesRecordExist(key) && !db.isHidden(key))
				uids.add(uid);
		}
		return uids;
	}

	String getNextAccountToRetrieve()
	{
		if(accountsToRetrieve.size() > 0)
			return (String)accountsToRetrieve.remove(0);

		if(isSleeping())
			return null;

		if(shouldSleepNextCycle)
		{
			//log("Sleeping for " + ServerForMirroring.inactiveSleepMillis / 1000 / 60 + " minutes");
			sleepUntil = System.currentTimeMillis() + ServerForMirroring.inactiveSleepMillis;
			shouldSleepNextCycle = false;
			return null;
		}

		shouldSleepNextCycle = true;

		try
		{
			log("Getting list of accounts");
			NetworkResponse response = gateway.listAccountsForMirroring(security);
			String resultCode = response.getResultCode();
			if(resultCode.equals(NetworkInterfaceConstants.OK))
			{
				accountsToRetrieve.addAll(response.getResultVector());
				log("Account count:" + accountsToRetrieve.size());
			}
			else if(!resultCode.equals(NetworkInterfaceConstants.NO_SERVER))
			{
				log("ERROR returned by " + ip + ": " + resultCode);
			}
		}
		catch (Exception e)
		{
			// TODO: Better error handling
			e.printStackTrace();
			log("getNextAccountToRetrieve: " + e);
		}
		return null;
	}

	private boolean isSleeping()
	{
		return System.currentTimeMillis() < sleepUntil;
	}
	
	void retrieveOneBulletin(File destFile, UniversalId uid) throws InvalidBase64Exception, IOException, MartusSignatureException, ServerErrorException
	{
		FileOutputStream out = new FileOutputStream(destFile);

		int chunkSize = MIRRORING_MAX_CHUNK_SIZE;
		ProgressMeterInterface nullProgressMeter = null;
		int totalLength = BulletinZipUtilities.retrieveBulletinZipToStream(uid, out, chunkSize, gateway, security, nullProgressMeter);

		out.close();

		if(destFile.length() != totalLength)
		{
			System.out.println("file=" + destFile.length() + ", returned=" + totalLength);
			throw new ServerErrorException("totalSize didn't match data length");
		}
	}
	
	void log(String message)
	{
		logger.log("Mirror calling " + ip + ": " + message);
	}
	
	Database db;	
	CallerSideMirroringGatewayInterface gateway;
	String ip;
	LoggerInterface logger;
	MartusCrypto security;
	
	Vector uidsToRetrieve;
	Vector accountsToRetrieve;

	public boolean shouldSleepNextCycle;
	public long sleepUntil;
	
	static final int MIRRORING_MAX_CHUNK_SIZE = 1024 * 1024;
}
