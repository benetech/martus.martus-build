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
		String signature = sign(parameters, signer);
		return new NetworkResponse(server.getUploadRights(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getSealedBulletinIds(MartusCrypto signer, String authorAccountId) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		String signature = sign(parameters, signer);
		return new NetworkResponse(server.getSealedBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}
					
	public NetworkResponse getDraftBulletinIds(MartusCrypto signer, String authorAccountId) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		String signature = sign(parameters, signer);
		return new NetworkResponse(server.getDraftBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}
					
	public NetworkResponse getFieldOfficeAccountIds(MartusCrypto signer, String hqAccountId) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(hqAccountId);
		String signature = sign(parameters, signer);
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
		String signature = sign(parameters, signer);
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
		String signature = sign(parameters, signer);
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
		String signature = sign(parameters, signer);
		return new NetworkResponse(server.getPacket(signer.getPublicKeyString(), parameters, signature));
	}
	
	public static String sign(Vector dataToSign, MartusCrypto signer) throws 
			MartusCrypto.MartusSignatureException
	{
		try
		{
			signer.signatureInitializeSign();
			for(int element = 0; element < dataToSign.size(); ++element)
			{
				String thisElement = dataToSign.get(element).toString();
				byte[] bytesToSign = thisElement.getBytes("UTF-8");
				//TODO: might want to optimize this for speed
				for(int b = 0; b < bytesToSign.length; ++b)
					signer.signatureDigestByte(bytesToSign[b]);
				signer.signatureDigestByte((byte)0);
			}
			return Base64.encode(signer.signatureGet());
		}
		catch(Exception e)
		{
			// TODO: Needs tests!
			System.out.println("ServerProxy.sign: " + e);
			throw new MartusCrypto.MartusSignatureException();
		}
	}

	public static boolean verifySignature(Vector dataToSign, MartusCrypto verifier, String signedBy, String sig)
	{
		try
		{
			verifier.signatureInitializeVerify(signedBy);
			for(int element = 0; element < dataToSign.size(); ++element)
			{
				String thisElement = dataToSign.get(element).toString();
				byte[] bytesToSign = thisElement.getBytes("UTF-8");
				//TODO: might want to optimize this for speed
				for(int b = 0; b < bytesToSign.length; ++b)
					verifier.signatureDigestByte(bytesToSign[b]);
				verifier.signatureDigestByte((byte)0);
			}
			byte[] sigBytes = Base64.decode(sig);
			return verifier.signatureIsValid(sigBytes);
		}
		catch(Exception e)
		{
			return false;
		}
	}

	final static String defaultReservedString = "";
		
	NetworkInterface server;
}
