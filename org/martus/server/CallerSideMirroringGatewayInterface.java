package org.martus.server;

import org.martus.common.BulletinRetrieverGatewayInterface;
import org.martus.common.MartusCrypto;
import org.martus.common.NetworkResponse;
import org.martus.common.MartusCrypto.MartusSignatureException;

public interface CallerSideMirroringGatewayInterface extends BulletinRetrieverGatewayInterface
{
	public NetworkResponse listBulletinsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException;
	public NetworkResponse listAccountsForMirroring(MartusCrypto signer) throws MartusSignatureException;
}
