package org.martus.server;

import java.util.Vector;

import org.martus.common.*;


public class ServerSideNetworkHandlerForNonSSL implements NetworkInterfaceForNonSSL
{

	public ServerSideNetworkHandlerForNonSSL(MartusServer serverToUse)
	{
		server = serverToUse;
	}

	public String ping()
	{
		return server.ping();
	}

	public Vector getServerInformation()
	{
		return server.getServerInformation();
	}

	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{
		return server.requestUploadRights(authorAccountId, tryMagicWord);
	}

	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		return server.uploadBulletin(authorAccountId, bulletinLocalId, data);
	}

	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId,
		int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		return server.uploadBulletinChunk(authorAccountId, bulletinLocalId,
						totalSize, chunkOffset, chunkSize, data, signature);
	}

	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		return server.downloadBulletin(authorAccountId, bulletinLocalId);
	}

	public Vector downloadMyBulletinChunk(String authorAccountId,String bulletinLocalId,
		int chunkOffset, int maxChunkSize, String signature)
	{
		return server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize, signature);
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		return server.listMySealedBulletinIds(authorAccountId);
	}

	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		return server.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		return server.listFieldOfficeAccounts(hqAccountId);
	}

	public Vector downloadPacket(String authorAccountId, String packetLocalId)
	{
		return server.legacyDownloadPacket(authorAccountId, packetLocalId);
	}

	public String authenticateServer(String tokenToSign)
	{
		return server.authenticateServer(tokenToSign);
	}
	
	MartusServer server;
}