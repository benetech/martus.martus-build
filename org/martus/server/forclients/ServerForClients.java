package org.martus.server.forclients;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.NetworkInterfaceXmlRpcConstants;
import org.martus.server.core.MartusSecureWebServer;

public class ServerForClients implements ServerForNonSSLClientsInterface, ServerForClientsInterface
{
	public ServerForClients(MartusServer coreServerToUse)
	{
		coreServer = coreServerToUse;

	}
	
	public void handleNonSSL()
	{
		ServerSideNetworkHandlerForNonSSL nonSSLServerHandler = new ServerSideNetworkHandlerForNonSSL(this);
		MartusXmlRpcServer.createNonSSLXmlRpcServer(nonSSLServerHandler, NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_NON_SSL);
	}
	
	public void handleSSL(int port)
	{
		ServerSideNetworkHandler serverHandler = new ServerSideNetworkHandler(this);
		MartusSecureWebServer.security = getSecurity();
		MartusXmlRpcServer.createSSLXmlRpcServer(serverHandler, port);
	}
	
	public void allowUploads(String clientId)
	{
		coreServer.allowUploads(clientId);
	}
	
	public int getNumberActiveClients()
	{
		return coreServer.getNumberActiveClients();
	}

	// BEGIN SSL interface
	public String deleteDraftBulletins(String accountId, String[] localIds)
	{
		return coreServer.deleteDraftBulletins(accountId, localIds);
	}

	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		return coreServer.downloadFieldDataPacket(authorAccountId, bulletinLocalId, packetLocalId, myAccountId, signature);
	}

	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature)
	{
		return coreServer.downloadFieldOfficeBulletinChunk(authorAccountId, bulletinLocalId, hqAccountId, chunkOffset, maxChunkSize, signature);
	}

	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize)
	{
		return coreServer.getBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize);
	}

	public Vector getNews(String myAccountId, String versionLabel, String versionBuildDate)
	{
		return coreServer.getNews(myAccountId, versionLabel, versionBuildDate);
	}

	public Vector getPacket(String myAccountId, String authorAccountId, String bulletinLocalId, String packetLocalId)
	{
		return coreServer.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
	}

	public Vector getServerCompliance()
	{
		return coreServer.getServerCompliance();
	}

	public Vector listMySealedBulletinIds(String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listMySealedBulletinIds(authorAccountId, retrieveTags);
	}

	public String putBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int chunkSize, int totalSize, int chunkOffset, String data)
	{
		return coreServer.putBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, chunkOffset, chunkSize, totalSize, data);
	}

	public String putContactInfo(String myAccountId, Vector parameters)
	{
		return coreServer.putContactInfo(myAccountId, parameters);
	}

	public Vector legacyDownloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		return coreServer.legacyDownloadAuthorizedPacket(authorAccountId, packetLocalId, myAccountId, signature);
	}

	public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listFieldOfficeDraftBulletinIds(hqAccountId, authorAccountId, retrieveTags);
	}

	public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId, retrieveTags);
	}

	public Vector listMyDraftBulletinIds(String authorAccountId, Vector retrieveTags)
	{
		return coreServer.listMyDraftBulletinIds(authorAccountId, retrieveTags);
	}

	public MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}
	
	public String getPublicCode(String clientId)
	{
		return coreServer.getPublicCode(clientId); 
	}
	
	public synchronized void logging(String message)
	{
		coreServer.logging(message);
	}
	
	public synchronized void clientConnectionStart()
	{
		coreServer.clientConnectionStart();
	}
	
	public synchronized void clientConnectionExit()
	{
		coreServer.clientConnectionExit();
	}

	// begin NON-SSL interface (sort of)
	public String authenticateServer(String tokenToSign)
	{
		return coreServer.authenticateServer(tokenToSign);
	}

	public String ping()
	{
		return coreServer.ping();
	}
	
	public Vector getServerInformation()
	{
		return coreServer.getServerInformation();
	}
	
	public String requestUploadRights(String clientId, String tryMagicWord)
	{
		return coreServer.requestUploadRights(clientId, tryMagicWord);
	}
	
	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		return coreServer.listFieldOfficeAccounts(hqAccountId);
	}
	
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		return coreServer.uploadBulletinChunk(authorAccountId, bulletinLocalId, totalSize, chunkOffset, chunkSize, data, signature);
	}
	
	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature)
	{
		return coreServer.downloadMyBulletinChunk(authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize, signature);
	}

	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		return coreServer.uploadBulletin(authorAccountId, bulletinLocalId, data);
	}
	
	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		return coreServer.downloadBulletin(authorAccountId, bulletinLocalId);
	}

	public Vector legacyDownloadPacket(String clientId, String packetId)
	{
		return coreServer.legacyDownloadPacket(clientId, packetId);
	}

	public Vector legacyListFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId)
	{
		return coreServer.legacyListFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
	}

	public Vector legacyListMySealedBulletinIds(String clientId)
	{
		return coreServer.legacyListMySealedBulletinIds(clientId);
	}

	MartusServer coreServer;

}
