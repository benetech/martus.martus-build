package org.martus.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusCrypto.CryptoException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;

public class SupplierSideMirroringHandler implements MirroringInterface
{
	SupplierSideMirroringHandler(Database supplierDatabase, MartusCrypto verifierToUse)
	{
		db = supplierDatabase;
		verifier = verifierToUse;
		authorizedCallers = new Vector();
	}
	
	public void clearAllAuthorizedCallers()
	{
		authorizedCallers.clear();
	}
	
	public void addAuthorizedCaller(String authorizedAccountId)
	{
		authorizedCallers.add(authorizedAccountId);
	}
	
	public Vector request(String callerAccountId, Vector parameters, String signature)
	{
		if(!MartusUtilities.verifySignature(parameters, verifier, callerAccountId, signature))
		{
			Vector result = new Vector();
			result.add(SIG_ERROR);		
			return result;
		}

		if(!isAuthorizedForMirroring(callerAccountId))
		{
			Vector result = new Vector();
			result.add(NOT_AUTHORIZED);
			return result;
		}

		Vector result = new Vector();
		try
		{
			return executeCommand(callerAccountId, parameters);
		}
		catch (RuntimeException e)
		{
			result = new Vector();
			result.add(BAD_PARAMETER);
			return result;
		}

	}

	Vector executeCommand(String callerAccountId, Vector parameters)
	{
		Vector result = new Vector();

		int cmd = extractCommand(parameters.get(0));
		switch(cmd)
		{
			case cmdPing:
			{
				result.add(OK);
				return result;
			}
			case cmdListAccountsForMirroring:
			{
				Vector accounts = listAccountsForMirroring(callerAccountId);
	
				result.add(OK);
				result.add(accounts);
				return result;
			}
			case cmdListBulletinsForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				Vector infos = listBulletinsForMirroring(callerAccountId, authorAccountId);
				
				result.add(OK);
				result.add(infos);
				return result;
			}
			default:
			{
				result = new Vector();
				result.add(UNKNOWN_COMMAND);
			}
		}
		
		return result;
	}

	Vector listAccountsForMirroring(String callerAccountId)
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
		db.visitAllAccounts(collector);
		return collector.accounts;
	}
	
	Vector listBulletinsForMirroring(String callerAccountId, String authorAccountId)
	{
		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					InputStreamWithSeek in = db.openInputStream(key, null);
					byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, verifier);
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
		db.visitAllRecordsForAccount(collector, authorAccountId);
		return collector.infos;
	}
	
	int extractCommand(Object possibleCommand)
	{
		String cmdString = (String)possibleCommand;
		if(cmdString.equals(CMD_PING))
			return cmdPing;

		if(cmdString.equals(CMD_LIST_ACCOUNTS_FOR_MIRRORING))
			return cmdListAccountsForMirroring;
		
		if(cmdString.equals(CMD_LIST_BULLETINS_FOR_MIRRORING))
			return cmdListBulletinsForMirroring;
		
		return cmdUnknown;
	}
	
	boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return authorizedCallers.contains(callerAccountId);
	}

	public static class UnknownCommandException extends Exception {}

	final static int cmdUnknown = 0;
	final static int cmdPing = 1;
	final static int cmdListAccountsForMirroring = 2;
	final static int cmdListBulletinsForMirroring = 3;
	
	Database db;
	MartusCrypto verifier;
	
	Vector authorizedCallers;
}
