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
		if(server.serverSSLLogging)
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
		if(server.serverSSLLogging)
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
		if(server.serverSSLLogging)
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

		Vector legacyResult = null;
		if(myAccountId.equals(authorAccountId))
			legacyResult = legacyListMyBulletinSummaries(myAccountId);
		else
			legacyResult = legacyListFieldOfficeBulletinSummaries(myAccountId, authorAccountId);

		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);

		result.add(resultCode);
		result.add(legacyResult);
		
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
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

		Vector legacyResult = null;
		if(myAccountId.equals(authorAccountId))
			legacyResult = server.listMyDraftBulletinIds(authorAccountId);
		else
			legacyResult = server.listFieldOfficeDraftBulletinIds(myAccountId, authorAccountId);
			
		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);

		result.add(resultCode);
		result.add(legacyResult);
		
		server.decrementActiveClientsCounter();
		
		return result;
	}

	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
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
		if(server.serverSSLLogging)
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
		if(server.serverSSLLogging)
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
		if(server.serverSSLLogging)
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
		if(server.serverSSLLogging)
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
		if(server.serverSSLLogging)
			server.logging("putContactInfo");
		server.incrementActiveClientsCounter();
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			if(server.serverSSLLogging)
				server.logging("Signature Error");
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
		String response;
		
		if(server.serverSSLLogging)
			server.logging("SSL-Ping");
		server.incrementActiveClientsCounter();
		response = server.ping();
		server.decrementActiveClientsCounter();
		return response;
	}

	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{
		String response;
		
		if(server.serverSSLLogging)
			server.logging("SSL-requestUploadRights");
		server.incrementActiveClientsCounter();
		response = legacyRequestUploadRights(authorAccountId, tryMagicWord);
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
		String response;
		if(server.serverSSLLogging)
			server.logging("SSL-uploadBulletinChunk");
		server.incrementActiveClientsCounter();
		response = server.uploadBulletinChunk(authorAccountId, bulletinLocalId,
						totalSize, chunkOffset, chunkSize, data, signature);
		server.decrementActiveClientsCounter();
		return response;
	}

	public Vector downloadMyBulletinChunk(String authorAccountId,String bulletinLocalId,
		int chunkOffset, int maxChunkSize, String signature)
	{
		Vector response;
		if(server.serverSSLLogging)
			server.logging("SSL-downloadMyBulletinChunk");
		server.incrementActiveClientsCounter();
		response = server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize, signature);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature)
	{
		Vector response;
		if(server.serverSSLLogging)
			server.logging("SSL-downloadFieldOfficeBulletinChunk");
		server.incrementActiveClientsCounter();
		response = server.downloadFieldOfficeBulletinChunk(authorAccountId, bulletinLocalId, hqAccountId, 
					chunkOffset, maxChunkSize, signature);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector downloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		Vector response;
		if(server.serverSSLLogging)
			server.logging("SSL-downloadAuthorizedPacket");
		server.incrementActiveClientsCounter();
		response = server.legacyDownloadAuthorizedPacket(authorAccountId, packetLocalId, myAccountId, signature);
		server.decrementActiveClientsCounter();
		return response;
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		Vector response;
		if(server.serverSSLLogging)
			server.logging("SSL-listMyBulletinSummaries");
		server.incrementActiveClientsCounter();
		response = legacyListMyBulletinSummaries(authorAccountId);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector legacyListMyBulletinSummaries(String authorAccountId)
	{
		return server.listMySealedBulletinIds(authorAccountId);
	}

	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		Vector response;
		if(server.serverSSLLogging)
			server.logging("SSL-downloadFieldDataPacket");
		server.incrementActiveClientsCounter();
		response = server.downloadFieldDataPacket(authorAccountId, bulletinLocalId, packetLocalId, myAccountId, signature);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		Vector response;
		if(server.serverSSLLogging)
			server.logging("SSL-listFieldOfficeBulletinSummaries");
		server.incrementActiveClientsCounter();
		response = legacyListFieldOfficeBulletinSummaries(hqAccountId, authorAccountId);
		server.decrementActiveClientsCounter();
		return response;
	}
	
	public Vector legacyListFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		return server.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
	}
	
	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		Vector response;
		if(server.serverSSLLogging)
			server.logging("SSL-listFieldOfficeAccounts");
		server.incrementActiveClientsCounter();
		response = legacyListFieldOfficeAccounts(hqAccountId);
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
