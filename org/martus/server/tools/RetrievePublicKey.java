package org.martus.server.tools;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.UnicodeWriter;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.server.formirroring.CallerSideMirroringGateway;
import org.martus.server.formirroring.CallerSideMirroringGatewayForXmlRpc;
import org.martus.server.formirroring.MirroringInterface;
import org.martus.server.formirroring.CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException;

public class RetrievePublicKey
{
	public static void main(String[] args)
	{
		new RetrievePublicKey(args);
	}
	
	RetrievePublicKey(String[] args)
	{
		processArgs(args);
		createGateway();
		Vector publicInfo = retrievePublicInfo();
		writePublicInfo(publicInfo);
		System.out.println("Success");
		System.exit(0);
	}

	private void createGateway()
	{
		try
		{
			gateway = RetrievePublicKey.createRealMirroringGateway(ip, port, publicCode);
		}
		catch (SSLSocketSetupException e)
		{
			e.printStackTrace();
			System.out.println("Error setting up socket");
			System.exit(3);
		}
	}

	void writePublicInfo(Vector publicInfo)
	{
		String publicKey = (String)publicInfo.get(0);
		String sig = (String)publicInfo.get(1);
		File outputFile = new File(outputFileName);
		try
		{
			UnicodeWriter writer = new UnicodeWriter(outputFile); 
			MartusUtilities.writeServerPublicKey(writer, publicKey, sig);
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Error writing output file");
			System.exit(3);
		}
		
	}
	
	Vector retrievePublicInfo()
	{ 
		try
		{
			NetworkResponse response = gateway.ping();
			String resultCode = response.getResultCode();
			if(!NetworkInterfaceConstants.OK.equals(resultCode))
			{
				System.out.println("Error response from server: " + resultCode);
				System.exit(3);
				return null;
			}
			return response.getResultVector();
		}
		catch (MartusSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Error signing request");
			System.exit(3);
		}
		return null;
	}
	
	void processArgs(String[] args)
	{
		port = MirroringInterface.MARTUS_PORT_FOR_MIRRORING;

		for (int i = 0; i < args.length; i++)
		{
			String value = args[i].substring(args[i].indexOf("=")+1);
			
			if(args[i].startsWith("--ip"))
				ip = value;
			
			if(args[i].startsWith("--port") && value != null)
				port = new Integer(value).intValue();
			
			if(args[i].startsWith("--public-code"))
				publicCode = MartusUtilities.removeNonDigits(value);
			
			if(args[i].startsWith("--output-file"))
				outputFileName = value;

		}

		if(ip == null || publicCode == null || outputFileName == null)
		{
			System.err.println("Incorrect arguments: RetrievePublicKey --ip=1.2.3.4 [--port=5] --public-code=6.7.8.1.2 --output-file=pubkey.txt\n");
			System.exit(2);
		}
		
	}

	public static CallerSideMirroringGateway createRealMirroringGateway(String ip, int port, String publicCode) throws CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException
	{
		CallerSideMirroringGatewayForXmlRpc xmlRpcGateway = new CallerSideMirroringGatewayForXmlRpc(ip, port); 
		xmlRpcGateway.setExpectedPublicCode(publicCode);
		return new CallerSideMirroringGateway(xmlRpcGateway);
	}

	String ip;
	int port;
	String publicCode;
	String outputFileName;

	CallerSideMirroringGateway gateway; 
}
