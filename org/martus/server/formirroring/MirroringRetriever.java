package org.martus.server.formirroring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.UniversalId;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.server.core.LoggerInterface;
import org.martus.server.forclients.MartusServerUtilities;

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
			
		try
		{
			String publicCode = MartusUtilities.getPublicCode(uid.getAccountId());
			log("Get bulletin: " + publicCode + "->" + uid.getLocalId());
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

			String publicCode = MartusUtilities.getPublicCode(nextAccountId);
			//log("Mirroring: List bulletins: " + publicCode);
			NetworkResponse response = gateway.listBulletinsForMirroring(security, nextAccountId);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				Vector infos = response.getResultVector();
				for(int i=0; i < infos.size(); ++i)
				{
					Vector info = (Vector)infos.get(i);
					String localId = (String)info.get(0);
					UniversalId uid = UniversalId.createFromAccountAndLocalId(nextAccountId, localId);
					DatabaseKey key = new DatabaseKey(uid);
					if(!db.doesRecordExist(key))
						uidsToRetrieve.add(uid);
				}
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

	String getNextAccountToRetrieve()
	{
		if(accountsToRetrieve.size() > 0)
			return (String)accountsToRetrieve.remove(0);

		try
		{
			log("Getting list of accounts");
			NetworkResponse response = gateway.listAccountsForMirroring(security);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				accountsToRetrieve.addAll(response.getResultVector());
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
	
	void retrieveOneBulletin(File destFile, UniversalId uid) throws InvalidBase64Exception, IOException, MartusSignatureException, ServerErrorException
	{
		FileOutputStream out = new FileOutputStream(destFile);

		int chunkSize = NetworkInterfaceConstants.MAX_CHUNK_SIZE;
		int totalLength = MartusUtilities.retrieveBulletinZipToStream(uid, out, chunkSize, gateway, security, null, null);

		out.close();

		if(destFile.length() != totalLength)
		{
			System.out.println("file=" + destFile.length() + ", returned=" + totalLength);
			throw new ServerErrorException("totalSize didn't match data length");
		}
	}
	
	void log(String message)
	{
		logger.log("Mirror " + ip + ": " + message);
	}

	Database db;	
	CallerSideMirroringGatewayInterface gateway;
	String ip;
	LoggerInterface logger;
	MartusCrypto security;
	
	Vector uidsToRetrieve;
	Vector accountsToRetrieve;
}
