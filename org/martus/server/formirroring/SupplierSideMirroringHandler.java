package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;

public class SupplierSideMirroringHandler implements MirroringInterface, NetworkInterfaceConstants
{
	public SupplierSideMirroringHandler(ServerSupplierInterface supplierToUse, MartusCrypto verifierToUse)
	{
		supplier = supplierToUse;
		verifier = verifierToUse;
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
			result.add(INVALID_DATA);
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
				result.add(RESULT_OK);
				return result;
			}
			case cmdListAccountsForMirroring:
			{
				Vector accounts = supplier.listAccountsForMirroring();
	
				result.add(OK);
				result.add(accounts);
				return result;
			}
			case cmdListBulletinsForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				Vector infos = supplier.listBulletinsForMirroring(authorAccountId);
				
				result.add(OK);
				result.add(infos);
				return result;
			}
			case cmdGetBulletinChunkForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				String bulletinLocalId = (String)parameters.get(2);
				int offset = ((Integer)parameters.get(3)).intValue();
				int maxChunkSize = ((Integer)parameters.get(4)).intValue();

				Vector data = getBulletinChunk(authorAccountId, bulletinLocalId, offset, maxChunkSize);
				String resultTag = (String)data.remove(0);
				
				result.add(resultTag);
				result.add(data);
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

	Vector getBulletinChunk(String authorAccountId, String bulletinLocalId, int offset, int maxChunkSize)
	{
		return supplier.getBulletinChunkWithoutVerifyingCaller(authorAccountId, bulletinLocalId, 
								offset, maxChunkSize);
	}
	
	
	int extractCommand(Object possibleCommand)
	{
		String cmdString = (String)possibleCommand;
		if(cmdString.equals(CMD_PING_FOR_MIRRORING))
			return cmdPing;

		if(cmdString.equals(CMD_LIST_ACCOUNTS_FOR_MIRRORING))
			return cmdListAccountsForMirroring;
		
		if(cmdString.equals(CMD_LIST_BULLETINS_FOR_MIRRORING))
			return cmdListBulletinsForMirroring;
		
		if(cmdString.equals(CMD_GET_BULLETIN_CHUNK_FOR_MIRRORING))
			return cmdGetBulletinChunkForMirroring;

		return cmdUnknown;
	}
	
	boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return supplier.isAuthorizedForMirroring(callerAccountId);
	}

	public static class UnknownCommandException extends Exception {}

	final static int cmdUnknown = 0;
	final static int cmdPing = 1;
	final static int cmdListAccountsForMirroring = 2;
	final static int cmdListBulletinsForMirroring = 3;
	final static int cmdGetBulletinChunkForMirroring = 4;
	
	ServerSupplierInterface supplier;
	MartusCrypto verifier;
}
