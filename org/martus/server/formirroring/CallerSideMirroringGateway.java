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
	
	public NetworkResponse ping() throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_PING);
		return new NetworkResponse(handler.request("anonymous", parameters, "unsigned"));
	}

	public NetworkResponse listAccountsForMirroring(MartusCrypto signer) throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_ACCOUNTS);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse listBulletinsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_SEALED_BULLETINS);
		parameters.add(authorAccountId);
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int maxChunkSize) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORINT_GET_BULLETIN_CHUNK);
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}
					
	MirroringInterface handler;
}

