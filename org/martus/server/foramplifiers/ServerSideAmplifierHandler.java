package org.martus.server.foramplifiers;

import java.util.Vector;

import org.martus.common.AmplifierNetworkInterface;
import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;

public class ServerSideAmplifierHandler implements AmplifierNetworkInterface
{
	
	public ServerSideAmplifierHandler(MartusAmplifierServer serverToUse)
	{
		server = serverToUse;
	}
	
	/// Begin Interface //
	
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature)
	{
		class AccountVisitor implements Database.AccountVisitor
		{
			AccountVisitor()
			{
				accounts = new Vector();
				accounts.add(NetworkInterfaceConstants.OK);
	}
	
			public void visit(String accountString)
			{
				if(!accounts.contains(accountString))
					accounts.add(accountString);
			}
	
			public Vector getAccounts()
	{
				return accounts;
			}
			Vector accounts;
		}
		
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);
			return result;
		}
		
		AccountVisitor visitor = new AccountVisitor();
		server.getDatabase().visitAllAccounts(visitor);

		return visitor.getAccounts();
	}
	
	public Vector getAccountUniversalIds(String myAccountId, Vector parameters, String signature)
	{
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);
			return result;
		}
		
		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					InputStreamWithSeek in = server.getDatabase().openInputStream(key, null);
					byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, server.security);
					in.close();
					String sigString = Base64.encode(sigBytes);
					Vector info = new Vector();
					info.add(key.getLocalId());
					info.add(sigString);
					infos.add(info);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			Vector infos = new Vector();
		}

		Collector collector = new Collector();		
		server.getDatabase().visitAllRecordsForAccount(collector, (String) parameters.get(1));
		return collector.infos;
	}
	
	public Vector getAmplifierBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);
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
		
		return result;
	}
	
	/// End Interface //

	private boolean isSignatureOk(String myAccountId, Vector parameters, String signature, MartusCrypto verifier)
	{
		return MartusUtilities.verifySignature(parameters, verifier, myAccountId, signature);
	}
	
	MartusAmplifierServer server;
}
