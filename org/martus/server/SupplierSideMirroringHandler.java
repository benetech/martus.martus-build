package org.martus.server;

import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;

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

		Vector result = new Vector();
		try
		{
			return executeCommand(callerAccountId, parameters);
		}
		catch (NotAuthorizedException e)
		{
			result = new Vector();
			result.add(NOT_AUTHORIZED);
			return result;
		}
	}

	Vector executeCommand(String callerAccountId, Vector parameters)
		throws NotAuthorizedException
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
			case cmdListAccountsForBackup:
			{
				Vector accounts = getAccountsForBackup(callerAccountId);
	
				result.add(OK);
				result.add(accounts);
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

	Vector getAccountsForBackup(String callerAccountId) throws NotAuthorizedException
	{
		if(!isAuthorizedToBackup(callerAccountId))
			throw new NotAuthorizedException();

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
	
	int extractCommand(Object possibleCommand)
	{
		try
		{
			String cmdString = (String)possibleCommand;
			if(cmdString.equals(CMD_PING))
				return cmdPing;

			if(cmdString.equals(CMD_LIST_ACCOUNTS_FOR_BACKUP))
				return cmdListAccountsForBackup;
			
		}
		catch (RuntimeException e)
		{
			//e.printStackTrace();
		}
		
		return cmdUnknown;
	}
	
	boolean isAuthorizedToBackup(String callerAccountId)
	{
		return authorizedCallers.contains(callerAccountId);
	}

	public static class NotAuthorizedException extends Exception {}
	public static class UnknownCommandException extends Exception {}

	final static int cmdUnknown = 0;
	final static int cmdPing = 1;
	final static int cmdListAccountsForBackup = 2;
	
	Database db;
	MartusCrypto verifier;
	
	Vector authorizedCallers;
}
