package org.martus.server.formirroring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.UniversalId;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.server.forclients.MartusServerUtilities;

public class MirroringRetriever
{
	public MirroringRetriever(Database databaseToUse, CallerSideMirroringGatewayInterface gatewayToUse, MartusCrypto securityToUse)
	{
		db = databaseToUse;
		gateway = gatewayToUse;
		security = securityToUse;
		
		uidsToRetrieve = new Vector();
		accountsToRetrieve = new Vector();
	}
	
	public void tick()
	{
		UniversalId uid = getNextUidToRetrieve();
		if(uid == null)
			return;
			
		try
		{
			File zip = retrieveOneBulletin(uid);
			BulletinHeaderPacket bhp = MartusServerUtilities.saveZipFileToDatabase(db, uid.getAccountId(), zip, security);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
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

			NetworkResponse response = gateway.listBulletinsForMirroring(security, nextAccountId);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				Vector infos = response.getResultVector();
				for(int i=0; i < infos.size(); ++i)
				{
					Vector info = (Vector)infos.get(i);
					String localId = (String)info.get(0);
					UniversalId uid = UniversalId.createFromAccountAndLocalId(nextAccountId, localId);
					uidsToRetrieve.add(uid);
				}
			}
		}
		catch (Throwable e)
		{
			// TODO: Better error handling
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
			NetworkResponse response = gateway.listAccountsForMirroring(security);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				accountsToRetrieve.addAll(response.getResultVector());
			}
		}
		catch (Throwable e)
		{
			// TODO: Better error handling
			e.printStackTrace();
			System.out.println("MirroringRetriever.getNextAccountToRetrieve: " + e);
		}
		return null;
	}
	
	File retrieveOneBulletin(UniversalId uid) throws InvalidBase64Exception, IOException, MartusSignatureException, ServerErrorException
	{
		File tempFile = File.createTempFile("$$$MirroringRetriever", null);
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);

		int chunkSize = NetworkInterfaceConstants.MAX_CHUNK_SIZE;
		int totalLength = MartusUtilities.retrieveBulletinZipToStream(uid, out, chunkSize, gateway, security, null, null);

		out.close();

		if(tempFile.length() != totalLength)
		{
			System.out.println("file=" + tempFile.length() + ", returned=" + totalLength);
			throw new ServerErrorException("totalSize didn't match data length");
		}

		return tempFile;
	}

	Database db;	
	CallerSideMirroringGatewayInterface gateway;
	MartusCrypto security;
	
	Vector uidsToRetrieve;
	Vector accountsToRetrieve;
}
