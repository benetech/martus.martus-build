package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.MartusCrypto;

public interface ServerSupplierInterface
{
	public Database getDatabase();
	public MartusCrypto getSecurity();
	public boolean isAuthorizedForMirroring(String callerAccountId);
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
			int chunkOffset, int maxChunkSize);

}
