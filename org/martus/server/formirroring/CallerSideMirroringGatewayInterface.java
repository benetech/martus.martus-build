package org.martus.server.formirroring;

import org.martus.common.BulletinRetrieverGatewayInterface;
import org.martus.common.MartusCrypto;
import org.martus.common.NetworkResponse;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.MartusSignatureException;

public interface CallerSideMirroringGatewayInterface extends BulletinRetrieverGatewayInterface
{
	public NetworkResponse ping() throws MartusSignatureException;
	public NetworkResponse listBulletinsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException;
	public NetworkResponse listAccountsForMirroring(MartusCrypto signer) throws MartusSignatureException;
	public NetworkResponse getBulletinUploadRecord(MartusCrypto signer, UniversalId uid) throws MartusSignatureException;
}
