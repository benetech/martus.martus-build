package org.martus.server;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;

public class SupplierSideMirroringHandler implements MirroringInterface
{
	SupplierSideMirroringHandler(MartusCrypto verifierToUse)
	{
		verifier = verifierToUse;
	}
	
	public Vector request(String callerAccountId, Vector parameters, String signature)
	{
		Vector result = new Vector();
		if(!MartusUtilities.verifySignature(parameters, verifier, callerAccountId, signature))
		{
			result.add(SIG_ERROR);		
			return result;
		}

		String cmd = extractCommand(parameters.get(0));
		if(cmd.equals(CMD_PING))
		{
			result.add(OK);
			return result;
		}
		else if(cmd.equals(CMD_GET_ACCOUNTS))
		{
			result.add(OK);
			result.add(new Vector());
		}
		else
		{
			result.add(UNKNOWN_COMMAND);
		}

		return result;
	}
	
	String extractCommand(Object possibleCommand)
	{
		try
		{
			return (String)possibleCommand;
		}
		catch (RuntimeException e)
		{
			return "";
		}
	}
	

	MartusCrypto verifier;
}
