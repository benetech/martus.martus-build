package org.martus.server;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkResponse;
import org.martus.common.MartusCrypto.MartusSignatureException;

class CallerSideMirroringGateway implements CallerSideMirroringGatewayInterface
{
	CallerSideMirroringGateway(MirroringInterface handlerToUse)
	{
		handler = handlerToUse;
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

