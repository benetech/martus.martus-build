package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.server.core.MartusXmlRpcServer;
import org.martus.server.forclients.MartusServer;

public class ServerForMirroring implements ServerSupplierInterface
{
	public ServerForMirroring(MartusServer coreServerToUse)
	{
		coreServer = coreServerToUse;
	}

	public void addListeners()
	{
		int port = MirroringInterface.MARTUS_PORT_FOR_MIRRORING;
		SupplierSideMirroringHandler supplierHandler = new SupplierSideMirroringHandler(this, getSecurity());
		MartusXmlRpcServer.createSSLXmlRpcServer(supplierHandler, MirroringInterface.DEST_OBJECT_NAME, port);
	}

	// Begin ServerSupplierInterface
	public Vector getPublicInfo()
	{
		try
		{
			Vector result = new Vector();
			result.add(getSecurity().getPublicKeyString());
			result.add(MartusUtilities.getSignatureOfPublicKey(getSecurity()));
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Vector();
		}
	}
	
	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return false;
	}

	public Vector listAccountsForMirroring()
	{
		class Collector implements Database.AccountVisitor
		{
			public void visit(String accountId)
			{
				accounts.add(accountId);
			}
			
			Vector accounts = new Vector();
		}

		Collector collector = new Collector();		
		getDatabase().visitAllAccounts(collector);
		return collector.accounts;
	}

	public Vector listBulletinsForMirroring(String authorAccountId)
	{
		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					InputStreamWithSeek in = getDatabase().openInputStream(key, null);
					byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, getSecurity());
					in.close();
					String sigString = Base64.encode(sigBytes);
					Vector info = new Vector();
					info.add(key.getLocalId());
					info.add(sigString);
					infos.add(info);
				}
				catch (Exception e)
				{
					// TODO: Log this so the MSPA knows there's a problem
					// (but in a way that won't print during unit tests)
					//e.printStackTrace();
				}
			}
			
			Vector infos = new Vector();
		}

		Collector collector = new Collector();		
		getDatabase().visitAllRecordsForAccount(collector, authorAccountId);
		return collector.infos;
	}
	
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize)
	{
		return coreServer.getBulletinChunkWithoutVerifyingCaller(authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize);
	}
	//End ServerSupplierInterface

	MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}

	Database getDatabase()
	{
		return coreServer.getDatabase();
	}


	MartusServer coreServer;
}
