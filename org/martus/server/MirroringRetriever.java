package org.martus.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.UniversalId;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.ServerErrorException;

public class MirroringRetriever
{
	MirroringRetriever(Database databaseToUse, CallerSideMirroringGatewayInterface gatewayToUse, MartusCrypto securityToUse)
	{
		gateway = gatewayToUse;
		security = securityToUse;
		
		uidsToRetrieve = new Vector();
		accountsToRetrieve = new Vector();
	}
	
	public void tick()
	{
		//UniversalId uid = getNextUidToRetrieve();
	}
	
	UniversalId getNextUidToRetrieve()
	{
		if(uidsToRetrieve.size() > 0)
			return (UniversalId)uidsToRetrieve.remove(0);

		try
		{
			String nextAccountId = getNextAccountToRetrieve();
			if(nextAccountId != null)
			{
				NetworkResponse response = gateway.listBulletinsForMirroring(security, nextAccountId);
				if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				{
					uidsToRetrieve.addAll(response.getResultVector());
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
	
	CallerSideMirroringGatewayInterface gateway;
	MartusCrypto security;
	
	Vector uidsToRetrieve;
	Vector accountsToRetrieve;
}
