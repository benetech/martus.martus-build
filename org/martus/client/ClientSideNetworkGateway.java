package org.martus.client;

import java.util.Vector;

import org.martus.common.*;

public class ClientSideNetworkGateway 
{
	public ClientSideNetworkGateway(NetworkInterface serverToUse)
	{
		server = serverToUse;
	}
	
	public NetworkResponse getServerInfo()
	{
		Vector parameters = new Vector();
		return new NetworkResponse(server.getServerInfo(parameters));
	}
	
	public NetworkResponse getUploadRights(MartusCrypto signer, String tryMagicWord) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(tryMagicWord);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.getUploadRights(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getSealedBulletinIds(MartusCrypto signer, String authorAccountId, Vector retrieveTags) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(retrieveTags);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.getSealedBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}
					
	public NetworkResponse getDraftBulletinIds(MartusCrypto signer, String authorAccountId, Vector retrieveTags) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(retrieveTags);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.getDraftBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}
					
	public NetworkResponse getFieldOfficeAccountIds(MartusCrypto signer, String hqAccountId) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(hqAccountId);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.getFieldOfficeAccountIds(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse putBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int chunkSize, int totalSize, String data) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(totalSize));
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(chunkSize));
		parameters.add(data);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.putBulletinChunk(signer.getPublicKeyString(), parameters, signature));
	}
					
	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int maxChunkSize) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.getBulletinChunk(signer.getPublicKeyString(), parameters, signature));
	}
					
	public NetworkResponse getPacket(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					String packetLocalId) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(packetLocalId);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.getPacket(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse deleteServerDraftBulletins(MartusCrypto signer, 
					String authorAccountId, String[] bulletinLocalIds) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(new Integer(bulletinLocalIds.length));
		for (int i = 0; i < bulletinLocalIds.length; i++)
		{
			parameters.add(bulletinLocalIds[i]);
		}
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.deleteDraftBulletins(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse	putContactInfo(MartusCrypto signer, String authorAccountId, Vector parameters) throws 
			MartusCrypto.MartusSignatureException
	{
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(server.putContactInfo(signer.getPublicKeyString(), parameters, signature));
	}

	final static String defaultReservedString = "";
		
	NetworkInterface server;
}
