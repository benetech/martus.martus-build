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
		String strResponse = server.ping();
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public Vector getServerInformation()
	{
		server.incrementActiveClientsCounter();
		Vector vecResponse = server.getServerInformation();
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{
		server.incrementActiveClientsCounter();
		String strResponse = server.requestUploadRights(authorAccountId, tryMagicWord);
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		server.incrementActiveClientsCounter();
		String strResponse = server.uploadBulletin(authorAccountId, bulletinLocalId, data);
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId,
		int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		server.incrementActiveClientsCounter();
		String strResponse = server.uploadBulletinChunk(authorAccountId, bulletinLocalId,
						totalSize, chunkOffset, chunkSize, data, signature);
		server.decrementActiveClientsCounter();
		return strResponse;
	}

	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		server.incrementActiveClientsCounter();
		Vector vecResponse = server.downloadBulletin(authorAccountId, bulletinLocalId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector downloadMyBulletinChunk(String authorAccountId,String bulletinLocalId,
		int chunkOffset, int maxChunkSize, String signature)
	{
		server.incrementActiveClientsCounter();
		Vector vecResponse = server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize, signature);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		server.incrementActiveClientsCounter();
		Vector vecResponse = server.legacyListMySealedBulletinIds(authorAccountId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		server.incrementActiveClientsCounter();
		Vector vecResponse = server.legacyListFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		server.incrementActiveClientsCounter();
		Vector vecResponse = server.listFieldOfficeAccounts(hqAccountId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public Vector downloadPacket(String authorAccountId, String packetLocalId)
	{
		server.incrementActiveClientsCounter();
		Vector vecResponse = server.legacyDownloadPacket(authorAccountId, packetLocalId);
		server.decrementActiveClientsCounter();
		return vecResponse;
	}

	public String authenticateServer(String tokenToSign)
	{
		server.incrementActiveClientsCounter();
		String strResponse = server.authenticateServer(tokenToSign);
		server.decrementActiveClientsCounter();
		return strResponse;
	}
	
	MartusServer server;
}