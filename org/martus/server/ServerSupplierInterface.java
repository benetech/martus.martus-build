package org.martus.server;

import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.MartusCrypto;

public interface ServerSupplierInterface
{
	public Database getDatabase();
	public MartusCrypto getSecurity();
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
			int chunkOffset, int maxChunkSize);
}
