package org.martus.server.formirroring;

import java.util.Vector;

public interface ServerSupplierInterface
{
	public Vector getPublicInfo();
	public boolean isAuthorizedForMirroring(String callerAccountId);
	public Vector listAccountsForMirroring();
	public Vector listBulletinsForMirroring(String authorAccountId);
	public String getBulletinUploadRecord(String authorAccountId, String bulletinLocalId);
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
			int chunkOffset, int maxChunkSize);
	public void log(String message);
}
