package org.martus.server;

import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.NetworkInterfaceConstants;

class FakeServerSupplier implements ServerSupplierInterface
{
	FakeServerSupplier() throws Exception
	{
		db = new MockServerDatabase();
		security = new MockMartusSecurity();
		
		returnZipData = Base64.encode("zip data".getBytes("UTF-8"));
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
	
	public Database getDatabase()
	{
		return db;
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return callerAccountId.equals(authorizedCaller);
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

	MockServerDatabase db;
	MartusCrypto security;
	
	String gotAccount;
	String gotLocalId;
	int gotChunkOffset;
	int gotMaxChunkSize;
}
