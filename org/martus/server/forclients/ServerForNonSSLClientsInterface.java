package org.martus.server.forclients;

import java.util.Vector;

public interface ServerForNonSSLClientsInterface
{
	public String getPublicCode(String clientId);
	public void logging(String message);
	public void clientConnectionStart();
	public void clientConnectionExit();
	public String authenticateServer(String tokenToSign);
	public String ping();
	public Vector getServerInformation();
	public String requestUploadRights(String clientId, String tryMagicWord);
	public Vector listFieldOfficeAccounts(String hqAccountId);
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature);
	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature);
	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data);
	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId);
	public Vector legacyDownloadPacket(String clientId, String packetId);
	public Vector legacyListFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId);
	public Vector legacyListMySealedBulletinIds(String clientId);
}
