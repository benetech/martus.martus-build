package org.martus.common;

import java.util.Vector;

public interface NetworkInterfaceForNonSSL
{
	public String ping();
	public Vector getServerInformation();
	public String requestUploadRights(String authorAccountId, String tryMagicWord);
	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data);
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature);
	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId);
	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature);
	public Vector listMyBulletinSummaries(String authorAccountId);
	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId);
	public Vector listFieldOfficeAccounts(String hqAccountId);
	public Vector downloadPacket(String authorAccountId, String packetLocalId);
	public String authenticateServer(String tokenToSign);
}
