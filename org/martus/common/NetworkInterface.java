package org.martus.common;

import java.util.Vector;

public interface NetworkInterface
{
	public Vector getServerInfo(Vector reservedForFuture);
	public Vector getUploadRights(String myAccountId, Vector parameters, String signature);
	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature);
	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature);
	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature);
	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature);
	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature);
	public Vector getPacket(String myAccountId, Vector parameters, String signature);

	// TODO: Delete everything below this line after all clients have been upgraded to 2002-08-28 or later
	public String ping();
	public String requestUploadRights(String authorAccountId, String tryMagicWord);
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature);
	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature);
	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature);
	public Vector listMyBulletinSummaries(String authorAccountId);
	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature);
	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId);
	public Vector listFieldOfficeAccounts(String hqAccountId);

	// TODO: Delete this after all clients have been upgraded to 2002-08-24 or later
	public Vector downloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature);
}
