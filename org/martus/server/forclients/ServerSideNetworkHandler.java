package org.martus.server.forclients;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;


public class ServerSideNetworkHandler implements NetworkInterface, NetworkInterfaceConstants
{

	public ServerSideNetworkHandler(ServerForClientsInterface serverToUse)
	{
		server = serverToUse;
	}

	// begin ServerInterface	
	public Vector getServerInfo(Vector reservedForFuture)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("getServerInfo");
		
		String version = server.ping();
		Vector data = new Vector();
		data.add(version);
		
		Vector result = new Vector();
		result.add(OK);
		result.add(data);
		
		if(MartusServer.serverSSLLogging)
			server.logging("getServerInfo: exit");
			
		server.clientConnectionExit();
		return result;
	}

	public Vector getUploadRights(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("getUploadRights");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			server.clientConnectionExit();
			result.add(SIG_ERROR);
			return result;
		}

		int index = 0;
		String tryMagicWord = (String)parameters.get(index++);
		
		String legacyResult = legacyRequestUploadRights(myAccountId, tryMagicWord);
		result.add(legacyResult);
		
		server.clientConnectionExit();
		return result;
	}

	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("getSealedBulletinIds");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(SIG_ERROR);
			server.clientConnectionExit();
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

		server.clientConnectionExit();
		return result;
	}

	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("getDraftBulletinIds");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(SIG_ERROR);			
			server.clientConnectionExit();			
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

		server.clientConnectionExit();
		return result;
	}

	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("getFieldOfficeAccountIds");
		
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(SIG_ERROR);			
			server.clientConnectionExit();			
			return result;
		}
			
		int index = 0;
		String hqAccountId = (String)parameters.get(index++);

		Vector legacyResult = legacyListFieldOfficeAccounts(hqAccountId);
		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);
		
		result.add(resultCode);
		result.add(legacyResult);
		
		server.clientConnectionExit();
		
		return result;
	}

	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("putBulletinChunk");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(SIG_ERROR);			
			server.clientConnectionExit();			
			return result;
		}
			
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);
		String bulletinLocalId= (String)parameters.get(index++);
		int totalSize = ((Integer)parameters.get(index++)).intValue();
		int chunkOffset = ((Integer)parameters.get(index++)).intValue();
		int chunkSize = ((Integer)parameters.get(index++)).intValue();
		String data = (String)parameters.get(index++);

		String legacyResult = server.putBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, 
					totalSize, chunkOffset, chunkSize, data);
		result.add(legacyResult);
		
		server.clientConnectionExit();
		
		return result;
	}

	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("getBulletinChunk");
			
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(SIG_ERROR);			
			server.clientConnectionExit();			
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
		
		server.clientConnectionExit();
		
		return result;
	}

	public Vector getPacket(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("getPacket");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(SIG_ERROR);			
			server.clientConnectionExit();			
			return result;
		}
			
	
		int index = 0;
		String authorAccountId = (String)parameters.get(index++);
		String bulletinLocalId= (String)parameters.get(index++);
		String packetLocalId= (String)parameters.get(index++);

		if(MartusServer.serverSSLLogging)
			server.logging("getPacketId " + packetLocalId + " for bulletinId " + bulletinLocalId);

		Vector legacyResult = server.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
		String resultCode = (String)legacyResult.get(0);
		legacyResult.remove(0);
				
		result.add(resultCode);
		result.add(legacyResult);
		
		server.clientConnectionExit();
		
		return result;
	}

	public Vector deleteDraftBulletins(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		if(MartusServer.serverSSLLogging)
			server.logging("deleteDraftBulletins");

		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(SIG_ERROR);			
			server.clientConnectionExit();			
			return result;
		}

		int idCount = ((Integer)parameters.get(0)).intValue();
		String[] idList = new String[idCount];
		for (int i = 0; i < idList.length; i++)
		{
			idList[i] = (String)parameters.get(1+i);
		}

		result.add(server.deleteDraftBulletins(myAccountId, idList));
		
		server.clientConnectionExit();
		return result;
	}
	
	public Vector putContactInfo(String myAccountId, Vector parameters, String signature) 
	{
		server.clientConnectionStart();
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			if(MartusServer.serverSSLLogging)
				server.logging("putContactInfo:Signature Error");
			result.add(SIG_ERROR);
			server.clientConnectionExit();
			return result;
		}
		result.add(server.putContactInfo(myAccountId, parameters));
		server.clientConnectionExit();
		return result;
	}

	public Vector getNews(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		Vector result = new Vector();

		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			if(MartusServer.serverSSLLogging)
				server.logging("getNews:Signature Error");
			result.add(SIG_ERROR);
			server.clientConnectionExit();
			return result;
		}
		
		String versionLabel = "";
		String versionBuildDate = "";
		
		if(parameters.size() >= 2)
		{
			int index = 0;
			versionLabel = (String)parameters.get(index++);
			versionBuildDate = (String)parameters.get(index++);
		}

		result = server.getNews(myAccountId, versionLabel, versionBuildDate);
		server.clientConnectionExit();
		return result;
	}

	public Vector getServerCompliance(String myAccountId, Vector parameters, String signature)
	{
		server.clientConnectionStart();
		Vector result = new Vector();

		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			if(MartusServer.serverSSLLogging)
				server.logging("getServerCompliance:Signature Error");
			result.add(SIG_ERROR);
			server.clientConnectionExit();
			return result;
		}
		
		result = server.getServerCompliance();
		server.clientConnectionExit();
		return result;
	}

	// begin legacy!
	
	public String legacyRequestUploadRights(String authorAccountId, String tryMagicWord)
	{
		server.logging("request for client " + server.getPublicCode(authorAccountId));
		return server.requestUploadRights(authorAccountId, tryMagicWord);
	}

	public Vector legacyListFieldOfficeAccounts(String hqAccountId)
	{
		server.logging("request for client " + server.getPublicCode(hqAccountId));
		return server.listFieldOfficeAccounts(hqAccountId);
	}
	
	private boolean isSignatureOk(String myAccountId, Vector parameters, String signature, MartusCrypto verifier)
	{
		server.logging("request for client " + server.getPublicCode(myAccountId));
		return MartusUtilities.verifySignature(parameters, verifier, myAccountId, signature);
	}

	final static String defaultReservedResponse = "";

	ServerForClientsInterface server;
}
