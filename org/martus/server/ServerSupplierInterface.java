package org.martus.server;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MockServerDatabase;

public interface ServerSupplierInterface
{
	public MockServerDatabase getDatabase();
	public MartusCrypto getSecurity();
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
			int chunkOffset, int maxChunkSize);
}
