package org.martus.server.formirroring;

import java.util.Vector;

public interface ServerSupplierInterface
{
	public boolean isAuthorizedForMirroring(String callerAccountId);
	public Vector listAccountsForMirroring();
	public Vector listBulletinsForMirroring(String authorAccountId);
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
			int chunkOffset, int maxChunkSize);

}
