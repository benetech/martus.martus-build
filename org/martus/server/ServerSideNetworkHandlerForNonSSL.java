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
		server.incrementActiveClientsCounter();
		strResponse = server.ping();
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public Vector getServerInformation()
	{
		server.incrementActiveClientsCounter();
		vecResponse = server.getServerInformation();
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{
		server.incrementActiveClientsCounter();
		strResponse = server.requestUploadRights(authorAccountId, tryMagicWord);
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		server.incrementActiveClientsCounter();
		strResponse = server.uploadBulletin(authorAccountId, bulletinLocalId, data);
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId,
		int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		server.incrementActiveClientsCounter();
		strResponse = server.uploadBulletinChunk(authorAccountId, bulletinLocalId,
						totalSize, chunkOffset, chunkSize, data, signature);
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		server.incrementActiveClientsCounter();
		vecResponse = server.downloadBulletin(authorAccountId, bulletinLocalId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector downloadMyBulletinChunk(String authorAccountId,String bulletinLocalId,
		int chunkOffset, int maxChunkSize, String signature)
	{
		server.incrementActiveClientsCounter();
		vecResponse = server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize, signature);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		server.incrementActiveClientsCounter();
		vecResponse = server.listMySealedBulletinIds(authorAccountId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		server.incrementActiveClientsCounter();
		vecResponse = server.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		server.incrementActiveClientsCounter();
		vecResponse = server.listFieldOfficeAccounts(hqAccountId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector downloadPacket(String authorAccountId, String packetLocalId)
	{
		server.incrementActiveClientsCounter();
		vecResponse = server.legacyDownloadPacket(authorAccountId, packetLocalId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public String authenticateServer(String tokenToSign)
	{
		server.incrementActiveClientsCounter();
		strResponse = server.authenticateServer(tokenToSign);
		server.decrementActiveClientsCounter();
		return strResponse;
	}
	
	MartusServer server;
	String strResponse;
	Vector vecResponse;
}