package org.martus.server.formirroring;

import java.util.Iterator;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterfaceConstants;

class FakeServerSupplier implements ServerSupplierInterface
{
	FakeServerSupplier() throws Exception
	{
		accountsToMirror = new Vector();
		bulletinsToMirror = new Vector();
		security = MockMartusSecurity.createServer();
		
		returnZipData = Base64.encode("zip data".getBytes("UTF-8"));
	}

	void addAccountToMirror(String accountId)
	{
		accountsToMirror.add(accountId);
	}
	
	void addBulletinToMirror(DatabaseKey key, String sig)
	{
		Vector data = new Vector();
		data.add(key.getAccountId());
		data.add(key.getLocalId());
		data.add(sig);
		bulletinsToMirror.add(data);
	}
	
	int getChunkSize()
	{
		try
		{
			return Base64.decode(returnZipData).length;
		}
		catch(Exception nothingWeCanDo)
		{
			return 0;
		}
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public Vector getPublicInfo()
	{
		try
		{
			Vector result = new Vector();
			result.add(security.getPublicKeyString());
			result.add(MartusUtilities.getSignatureOfPublicKey(security));
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return callerAccountId.equals(authorizedCaller);
	}

	public Vector listAccountsForMirroring()
	{
		return accountsToMirror;
	}

	public Vector listBulletinsForMirroring(String authorAccountId)
	{
		Vector bulletins = new Vector();
		for (Iterator b = bulletinsToMirror.iterator(); b.hasNext();)
		{
			Vector data = (Vector)b.next();
			if(authorAccountId.equals(data.get(0)))
			{
				Vector info = new Vector();
				info.add(data.get(1));
				info.add(data.get(2));
				bulletins.add(info);
			}
		}
		return bulletins;
	}
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
			int chunkOffset, int maxChunkSize)
	{
		gotAccount = authorAccountId;
		gotLocalId = bulletinLocalId;
		gotChunkOffset = chunkOffset;
		gotMaxChunkSize = maxChunkSize;

		int totalLen = getChunkSize();
		if(returnResultTag == NetworkInterfaceConstants.CHUNK_OK)
			totalLen *= 3;

		Vector result = new Vector();
		result.add(returnResultTag);
		result.add(new Integer(totalLen));
		result.add(new Integer(getChunkSize()));
		result.add(returnZipData);
		return result;
	}

	String authorizedCaller;
	String returnZipData;
	String returnResultTag = NetworkInterfaceConstants.CHUNK_OK;

	MartusCrypto security;
	Vector accountsToMirror;
	Vector bulletinsToMirror;
	
	String gotAccount;
	String gotLocalId;
	int gotChunkOffset;
	int gotMaxChunkSize;
}
