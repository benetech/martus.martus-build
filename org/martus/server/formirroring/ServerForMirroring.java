package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.server.forclients.MartusServer;
import org.martus.server.forclients.MartusXmlRpcServer;

public class ServerForMirroring implements ServerSupplierInterface
{
	public ServerForMirroring(MartusServer coreServerToUse)
	{
		coreServer = coreServerToUse;
		int port = MirroringInterface.MARTUS_PORT_FOR_MIRRORING;
		SupplierSideMirroringHandler supplierHandler = new SupplierSideMirroringHandler(this);
		MartusXmlRpcServer.createSSLXmlRpcServer(supplierHandler, port);
	}

	// Begin ServerSupplierInterface
	public Database getDatabase()
	{
		return coreServer.getDatabase();
	}

	public MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}

	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return false;
	}

	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize)
	{
		return coreServer.getBulletinChunkWithoutVerifyingCaller(authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize);
	}
	//End ServerSupplierInterface

	MartusServer coreServer;
}
