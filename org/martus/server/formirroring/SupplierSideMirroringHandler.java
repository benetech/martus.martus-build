package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.Base64.InvalidBase64Exception;

public class SupplierSideMirroringHandler implements MirroringInterface, NetworkInterfaceConstants
{
	public SupplierSideMirroringHandler(ServerSupplierInterface supplierToUse, MartusCrypto verifierToUse)
	{
		supplier = supplierToUse;
		verifier = verifierToUse;
	}
	
	public Vector request(String callerAccountId, Vector parameters, String signature)
	{
		try
		{
			if(!isSignatureAcceptable(callerAccountId, parameters, signature))
			{
				log("request: bad sig");
				Vector result = new Vector();
				result.add(SIG_ERROR);		
				return result;
			}

			return executeCommand(callerAccountId, parameters);
		}
		catch (RuntimeException e)
		{
			Vector result = new Vector();
			result.add(INVALID_DATA);
			return result;
		}

	}

	private boolean isSignatureAcceptable(String callerAccountId, Vector parameters, String signature)
	{
		int cmd = extractCommand(parameters.get(0));
		if(cmd == cmdPing)
			return true;
		return MartusUtilities.verifySignature(parameters, verifier, callerAccountId, signature);
	}
	
	Vector executeCommand(String callerAccountId, Vector parameters)
	{
		Vector result = new Vector();
		int cmd = extractCommand(parameters.get(0));

		if(!isAuthorized(cmd, callerAccountId))
		{
			log("request: not authorized");
			result.add(NOT_AUTHORIZED);
			return result;
		}

		switch(cmd)
		{
			case cmdPing:
			{
				log("ping");
				result.add(RESULT_OK);
				result.add(supplier.getPublicInfo());
				return result;
			}
			case cmdListAccountsForMirroring:
			{
				log("listAccounts");
				Vector accounts = supplier.listAccountsForMirroring();
	
				result.add(OK);
				result.add(accounts);
				return result;
			}
			case cmdListBulletinsForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				String publicCode;
				try
				{
					publicCode = MartusUtilities.computePublicCode(authorAccountId);
				}
				catch (InvalidBase64Exception e)
				{
					log("listBulletins: Bad account:" + authorAccountId);
					result.add(INVALID_DATA);
					return result;
				}
				log("listBulletins: " + MartusUtilities.formatPublicCode(publicCode));
				Vector infos = supplier.listBulletinsForMirroring(authorAccountId);
				
				result.add(OK);
				result.add(infos);
				return result;
			}
			case cmdGetBulletinUploadRecordForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				String bulletinLocalId = (String)parameters.get(2);
				log("getBulletinUploadRecord: " + bulletinLocalId);
				String bur = supplier.getBulletinUploadRecord(authorAccountId, bulletinLocalId);
				if(bur == null)
				{
					result.add(NOT_FOUND);
				}
				else
				{
					Vector burs = new Vector();
					burs.add(bur);
					
					result.add(OK);
					result.add(burs);
				}
				return result;
			}
			case cmdGetBulletinChunkForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				String bulletinLocalId = (String)parameters.get(2);
				log("getBulletinChunk: " + bulletinLocalId);
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
				log("request: Unknown command");
				result = new Vector();
				result.add(UNKNOWN_COMMAND);
			}
		}
		
		return result;
	}

	boolean isAuthorized(int cmd, String callerAccountId)
	{
		if(cmd == cmdPing)
			return true;
			
		return isAuthorizedForMirroring(callerAccountId);
	}

	Vector getBulletinChunk(String authorAccountId, String bulletinLocalId, int offset, int maxChunkSize)
	{
		return supplier.getBulletinChunkWithoutVerifyingCaller(authorAccountId, bulletinLocalId, 
								offset, maxChunkSize);
	}
	
	
	int extractCommand(Object possibleCommand)
	{
		String cmdString = (String)possibleCommand;
		if(cmdString.equals(CMD_MIRRORING_PING))
			return cmdPing;

		if(cmdString.equals(CMD_MIRRORING_LIST_ACCOUNTS))
			return cmdListAccountsForMirroring;
		
		if(cmdString.equals(CMD_MIRRORING_LIST_SEALED_BULLETINS))
			return cmdListBulletinsForMirroring;
		
		if(cmdString.equals(CMD_MIRRORING_GET_BULLETIN_UPLOAD_RECORD))
			return cmdGetBulletinUploadRecordForMirroring;

		if(cmdString.equals(CMD_MIRRORING_GET_BULLETIN_CHUNK))
			return cmdGetBulletinChunkForMirroring;

		return cmdUnknown;
	}
	
	boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return supplier.isAuthorizedForMirroring(callerAccountId);
	}
	
	void log(String message)
	{
		supplier.log("Mirror handler: " + message);
	}

	public static class UnknownCommandException extends Exception {}

	final static int cmdUnknown = 0;
	final static int cmdPing = 1;
	final static int cmdListAccountsForMirroring = 2;
	final static int cmdListBulletinsForMirroring = 3;
	final static int cmdGetBulletinUploadRecordForMirroring = 4;
	final static int cmdGetBulletinChunkForMirroring = 5;
	
	ServerSupplierInterface supplier;
	MartusCrypto verifier;
}
