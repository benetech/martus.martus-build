package org.martus.common;

import java.util.Vector;

import org.martus.common.*;

public class NetworkResponse 
{
	public NetworkResponse(Vector rawServerReturnData)
	{
		if(rawServerReturnData == null)
		{
			resultCode = NetworkInterfaceConstants.NO_SERVER;
		}
		else
		{
			resultCode = (String)rawServerReturnData.get(0);
			if(rawServerReturnData.size() >= 2)
				resultVector = (Vector)rawServerReturnData.get(1);
		}
	}
	
	public String getResultCode()
	{
		return resultCode;
	}
	
	public Vector getResultVector()
	{
		return resultVector;
	}
	
	String resultCode;
	Vector resultVector;
}
