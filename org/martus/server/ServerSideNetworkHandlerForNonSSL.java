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
		server.clientConnectionStart();
		String strResponse = server.ping();
		server.clientConnectionExit();
		return strResponse;
	}

	public Vector getServerInformation()
	{
		server.clientConnectionStart();
		Vector vecResponse = server.getServerInformation();
		server.clientConnectionExit();
		return vecResponse;
	}

	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		String strResponse = server.requestUploadRights(authorAccountId, tryMagicWord);
		server.clientConnectionExit();
		return strResponse;
	}

	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		String strResponse = server.uploadBulletin(authorAccountId, bulletinLocalId, data);
		server.clientConnectionExit();
		return strResponse;
	}

	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId,
		int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		String strResponse = server.uploadBulletinChunk(authorAccountId, bulletinLocalId,
						totalSize, chunkOffset, chunkSize, data, signature);
		server.clientConnectionExit();
		return strResponse;
	}

	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		Vector vecResponse = server.downloadBulletin(authorAccountId, bulletinLocalId);
		server.clientConnectionExit();
		return vecResponse;
	}

	public Vector downloadMyBulletinChunk(String authorAccountId,String bulletinLocalId,
		int chunkOffset, int maxChunkSize, String signature)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		Vector vecResponse = server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize, signature);
		server.clientConnectionExit();
		return vecResponse;
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		Vector vecResponse = server.legacyListMySealedBulletinIds(authorAccountId);
		server.clientConnectionExit();
		return vecResponse;
	}

	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(hqAccountId));
		Vector vecResponse = server.legacyListFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
		server.clientConnectionExit();
		return vecResponse;
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(hqAccountId));
		Vector vecResponse = server.listFieldOfficeAccounts(hqAccountId);
		server.clientConnectionExit();
		return vecResponse;
	}

	public Vector downloadPacket(String authorAccountId, String packetLocalId)
	{
		server.clientConnectionStart();
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		Vector vecResponse = server.legacyDownloadPacket(authorAccountId, packetLocalId);
		server.clientConnectionExit();
		return vecResponse;
	}

	public String authenticateServer(String tokenToSign)
	{
		server.clientConnectionStart();
		String strResponse = server.authenticateServer(tokenToSign);
		server.clientConnectionExit();
		return strResponse;
	}
	
	MartusServer server;
}