package org.martus.server.foramplifiers;

import java.util.Vector;

import org.martus.common.AmplifierNetworkInterface;
import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.ByteArrayInputStreamWithSeek;
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
			}
	
			public void visit(String accountString)
			{
				PublicDataCollector collector = new PublicDataCollector();
				Database db = server.getDatabase();
				db.visitAllRecordsForAccount(collector, accountString);
				
				if(collector.infos.size() > 0 && ! accounts.contains(accountString))
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

		result.add(NetworkInterfaceConstants.OK);
		result.add(visitor.getAccounts());

		return result;
	}
	
	public Vector getAccountUniversalIds(String myAccountId, Vector parameters, String signature)
	{
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.security))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);
			return result;
		}
		
		PublicDataCollector collector = new PublicDataCollector();
		String parameter = (String) parameters.get(0);
		Database db = server.getDatabase();
		db.visitAllRecordsForAccount(collector, parameter);
		
		result.add(NetworkInterfaceConstants.OK);
		result.add(collector.infos);
		
		return result;
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
	
	class PublicDataCollector implements Database.PacketVisitor
	{
		public void visit(DatabaseKey key)
		{
			try
			{
				if(! key.getLocalId().startsWith("B-") )
				{
					return;
				}
				
				InputStreamWithSeek in = server.getDatabase().openInputStream(key, null);
				byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, server.security);
				in.close();
				String sigString = Base64.encode(sigBytes);
				
				String headerXml = server.getDatabase().readRecord(key, server.security);
				byte[] headerBytes = headerXml.getBytes("UTF-8");
				
				ByteArrayInputStreamWithSeek headerIn = new ByteArrayInputStreamWithSeek(headerBytes);
				BulletinHeaderPacket bhp = new BulletinHeaderPacket("");
				bhp.loadFromXml(headerIn, null);
				if(! bhp.isAllPrivate())
				{
					Vector info = new Vector();
					info.add(key.getLocalId());
					info.add(sigString);
					infos.add(info);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		Vector infos = new Vector();
	}

	private boolean isSignatureOk(String myAccountId, Vector parameters, String signature, MartusCrypto verifier)
	{
		return MartusUtilities.verifySignature(parameters, verifier, myAccountId, signature);
	}
	
	MartusAmplifierServer server;
}
