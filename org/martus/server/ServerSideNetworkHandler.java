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
		
		String version = server.ping();
		Vector data = new Vector();
		data.add(version);
		
		Vector result = new Vector();
		result.add(OK);
		result.add(data);
		return result;
	}

	public Vector getUploadRights(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("getUploadRights");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
			return result;
		}
			
		int index = 0;
		String tryMagicWord = (String)parameters.get(index++);
		
		String legacyResult = requestUploadRights(myAccountId, tryMagicWord);
		result.add(legacyResult);
		return result;
	}

	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("getSealedBulletinIds");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
			return result;
		}
			
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);

		Vector legacyResult = null;
		if(myAccountId.equals(authorAccountId))
			legacyResult = listMyBulletinSummaries(myAccountId);
		else
			legacyResult = listFieldOfficeBulletinSummaries(myAccountId, authorAccountId);

		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);

		result.add(resultCode);
		result.add(legacyResult);
		return result;
	}

	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("getDraftBulletinIds");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
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
		return result;
	}

	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("getFieldOfficeAccountIds");
		
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
			return result;
		}
			
		int index = 0;
		String hqAccountId = (String)parameters.get(index++);

		Vector legacyResult = listFieldOfficeAccounts(hqAccountId);
		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);
		
		result.add(resultCode);
		result.add(legacyResult);
		return result;
	}

	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("putBulletinChunk");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
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
		return result;
	}

	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("getBulletinChunk");
			
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
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
		return result;
	}

	public Vector getPacket(String myAccountId, Vector parameters, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("getPacket");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(SIG_ERROR);
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
		return result;
	}

	// begin legacy!
	public String ping()
	{
		if(server.serverSSLLogging)
			server.logging("SSL-Ping");
		return server.ping();
	}

	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-requestUploadRights");
		return server.requestUploadRights(authorAccountId, tryMagicWord);
	}

	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId,
		int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-uploadBulletinChunk");
		return server.uploadBulletinChunk(authorAccountId, bulletinLocalId,
						totalSize, chunkOffset, chunkSize, data, signature);
	}

	public Vector downloadMyBulletinChunk(String authorAccountId,String bulletinLocalId,
		int chunkOffset, int maxChunkSize, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-downloadMyBulletinChunk");
		return server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize, signature);
	}
	
	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-downloadFieldOfficeBulletinChunk");
		return server.downloadFieldOfficeBulletinChunk(authorAccountId, bulletinLocalId, hqAccountId, 
					chunkOffset, maxChunkSize, signature);
	}
	
	public Vector downloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-downloadAuthorizedPacket");
		return server.legacyDownloadAuthorizedPacket(authorAccountId, packetLocalId, myAccountId, signature);
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-listMyBulletinSummaries");
		return server.listMySealedBulletinIds(authorAccountId);
	}

	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-downloadFieldDataPacket");
		return server.downloadFieldDataPacket(authorAccountId, bulletinLocalId, packetLocalId, myAccountId, signature);
	}
	
	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-listFieldOfficeBulletinSummaries");
		return server.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId);
	}
	
	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		if(server.serverSSLLogging)
			server.logging("SSL-listFieldOfficeAccounts");
		return server.listFieldOfficeAccounts(hqAccountId);
	}
	
	private boolean isSignatureOk(String myAccountId, Vector parameters, String signature, MartusCrypto verifier)
	{
		return MartusUtilities.verifySignature(parameters, verifier, myAccountId, signature);
	}

	final static String defaultReservedResponse = "";

	MartusServer server;

}
