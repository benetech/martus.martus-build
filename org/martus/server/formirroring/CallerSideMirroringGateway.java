package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkResponse;
import org.martus.common.MartusCrypto.MartusSignatureException;

public class CallerSideMirroringGateway implements CallerSideMirroringGatewayInterface
{
	public CallerSideMirroringGateway(MirroringInterface handlerToUse)
	{
		handler = handlerToUse;
	}
	
	public NetworkResponse ping(MartusCrypto signer) throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_PING_FOR_MIRRORING);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse listAccountsForMirroring(MartusCrypto signer) throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_LIST_ACCOUNTS_FOR_MIRRORING);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse listBulletinsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_LIST_BULLETINS_FOR_MIRRORING);
		parameters.add(authorAccountId);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int maxChunkSize) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_GET_BULLETIN_CHUNK_FOR_MIRRORING);
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}
					
	MirroringInterface handler;
}

