package org.martus.server;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;


public class ServerSideNetworkHandler implements NetworkInterface, NetworkInterfaceConstants
{

	public ServerSideNetworkHandler(MartusServer serverToUse)
	{
		server = serverToUse;
	}

	// begin ServerInterface	
	public Vector getServerInfo(Vector reservedForFuture)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("getServerInfo");
		
		server.incrementActiveClientsCounter();
		
		String version = server.ping();
		Vector data = new Vector();
		data.add(version);
		
		Vector result = new Vector();
		result.add(OK);
		result.add(data);
		
		server.decrementActiveClientsCounter();
		return result;
	}

	public Vector getUploadRights(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("getUploadRights");
			
		server.incrementActiveClientsCounter();

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			server.decrementActiveClientsCounter();
			result.add(SIG_ERROR);
			return result;
		}
			
		int index = 0;
		String tryMagicWord = (String)parameters.get(index++);
		
		String legacyResult = legacyRequestUploadRights(myAccountId, tryMagicWord);
		result.add(legacyResult);
		
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("getSealedBulletinIds");
			
		server.incrementActiveClientsCounter();

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
			server.decrementActiveClientsCounter();
			return result;
		}
			
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);
		Vector retrieveTags = new Vector();
		if(index < parameters.size())
			retrieveTags = (Vector)parameters.get(index++);
		
		if(myAccountId.equals(authorAccountId))
			result = server.listMySealedBulletinIds(authorAccountId, retrieveTags);
		else
			result = server.listFieldOfficeSealedBulletinIds(myAccountId, authorAccountId, retrieveTags);

		server.decrementActiveClientsCounter();
		return result;
	}

	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("getDraftBulletinIds");
			
		server.incrementActiveClientsCounter();

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);			
			server.decrementActiveClientsCounter();			
			return result;
		}
			
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);
		Vector retrieveTags = new Vector();
		if(index < parameters.size())
			retrieveTags = (Vector)parameters.get(index++);

		if(myAccountId.equals(authorAccountId))
			result = server.listMyDraftBulletinIds(authorAccountId, retrieveTags);
		else
			result = server.listFieldOfficeDraftBulletinIds(myAccountId, authorAccountId, retrieveTags);
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("getFieldOfficeAccountIds");
			
		server.incrementActiveClientsCounter();
		
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);			
			server.decrementActiveClientsCounter();			
			return result;
		}
			
		int index = 0;
		String hqAccountId = (String)parameters.get(index++);

		Vector legacyResult = legacyListFieldOfficeAccounts(hqAccountId);
		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);
		
		result.add(resultCode);
		result.add(legacyResult);
		
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("putBulletinChunk");
			
		server.incrementActiveClientsCounter();

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);			
			server.decrementActiveClientsCounter();			
			return result;
		}
			
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);
		String bulletinLocalId= (String)parameters.get(index++);
		int chunkOffset = ((Integer)parameters.get(index++)).intValue();
		int chunkSize = ((Integer)parameters.get(index++)).intValue();
		int totalSize = ((Integer)parameters.get(index++)).intValue();
		String data = (String)parameters.get(index++);

		String legacyResult = server.putBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, 
					chunkSize, totalSize, chunkOffset, data);
		result.add(legacyResult);
		
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("getBulletinChunk");
			
		server.incrementActiveClientsCounter();
			
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);			
			server.decrementActiveClientsCounter();			
			return result;
		}
			
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);
		String bulletinLocalId= (String)parameters.get(index++);
		int chunkOffset = ((Integer)parameters.get(index++)).intValue();
		int maxChunkSize = ((Integer)parameters.get(index++)).intValue();

		Vector legacyResult = server.getBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, 
				chunkOffset, maxChunkSize);
		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);
				
		result.add(resultCode);
		result.add(legacyResult);
		
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector getPacket(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("getPacket");
			
		server.incrementActiveClientsCounter();

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);			
			server.decrementActiveClientsCounter();			
			return result;
		}
			
	
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);
		String bulletinLocalId= (String)parameters.get(index++);
		String packetLocalId= (String)parameters.get(index++);

		Vector legacyResult = server.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);
				
		result.add(resultCode);
		result.add(legacyResult);
		
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector deleteDraftBulletins(String myAccountId, Vector parameters, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("deleteDraftBulletins");
			
		server.incrementActiveClientsCounter();

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);			
			server.decrementActiveClientsCounter();			
			return result;
		}

		int idCount = ((Integer)parameters.get(0)).intValue();
		String[] idList = new String[idCount];
		for (int i = 0; i < idList.length; i++)
		{
			idList[i] = (String)parameters.get(1+i);
		}

		result.add(server.deleteDraftBulletins(myAccountId, idList));
		
		server.decrementActiveClientsCounter();
		
		return result;
	}
	
	public Vector putContactInfo(String myAccountId, Vector parameters, String signature) 
	{
		server.incrementActiveClientsCounter();
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			if(MartusServer.serverSSLLogging)
				server.logging("putContactInfo:Signature Error");
			result.add(SIG_ERROR);
			server.decrementActiveClientsCounter();
			return result;
		}
		result.add(server.putContactInfo(myAccountId, parameters));
		server.decrementActiveClientsCounter();
		return result;
	}


	// begin legacy!
	public String ping()
	{		
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-Ping");
		server.incrementActiveClientsCounter();
		String response = server.ping();
		server.decrementActiveClientsCounter();
		return response;
	}

	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{		
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-requestUploadRights");
		server.incrementActiveClientsCounter();
		String response = legacyRequestUploadRights(authorAccountId, tryMagicWord);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public String legacyRequestUploadRights(String authorAccountId, String tryMagicWord)
	{
		return server.requestUploadRights(authorAccountId, tryMagicWord);
	}

	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId,
		int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-uploadBulletinChunk");
		server.incrementActiveClientsCounter();
		String response = server.uploadBulletinChunk(authorAccountId, bulletinLocalId,
						totalSize, chunkOffset, chunkSize, data, signature);
		server.decrementActiveClientsCounter();
		return response;
	}

	public Vector downloadMyBulletinChunk(String authorAccountId,String bulletinLocalId,
		int chunkOffset, int maxChunkSize, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-downloadMyBulletinChunk");
		server.incrementActiveClientsCounter();
		Vector response = server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize, signature);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-downloadFieldOfficeBulletinChunk");
		server.incrementActiveClientsCounter();
		Vector response = server.downloadFieldOfficeBulletinChunk(authorAccountId, bulletinLocalId, hqAccountId, 
					chunkOffset, maxChunkSize, signature);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector downloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-downloadAuthorizedPacket");
		server.incrementActiveClientsCounter();
		Vector response = server.legacyDownloadAuthorizedPacket(authorAccountId, packetLocalId, myAccountId, signature);
		server.decrementActiveClientsCounter();
		return response;
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-listMyBulletinSummaries");
		server.incrementActiveClientsCounter();
		Vector response = server.legacyListMySealedBulletinIds(authorAccountId);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-downloadFieldDataPacket");
		server.incrementActiveClientsCounter();
		Vector response = server.downloadFieldDataPacket(authorAccountId, bulletinLocalId, packetLocalId, myAccountId, signature);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-listFieldOfficeBulletinSummaries");
		server.incrementActiveClientsCounter();
		Vector response = server.legacyListFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		if(MartusServer.serverSSLLogging)
			server.logging("SSL-listFieldOfficeAccounts");
		server.incrementActiveClientsCounter();
		Vector response = legacyListFieldOfficeAccounts(hqAccountId);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector legacyListFieldOfficeAccounts(String hqAccountId)
	{
		return server.listFieldOfficeAccounts(hqAccountId);
	}
	
	private boolean isSignatureOk(String myAccountId, Vector parameters, String signature, MartusCrypto verifier)
	{
		return MartusUtilities.verifySignature(parameters, verifier, myAccountId, signature);
	}

	final static String defaultReservedResponse = "";

	MartusServer server;

}
