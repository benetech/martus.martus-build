package org.martus.server.tools;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.server.forclients.MartusServerUtilities;
import org.martus.server.formirroring.CallerSideMirroringGateway;
import org.martus.server.formirroring.MirroringUtilities;
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
		loadKeyPair();
		Vector publicInfo = retrievePublicInfo();
		writePublicInfo(publicInfo);
		System.out.println("Success");
		System.exit(0);
	}

	private void createGateway()
	{
		try
		{
			gateway = MirroringUtilities.createRealMirroringGateway(ip, port, publicCode);
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
			NetworkResponse response = gateway.ping(security);
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
	
	void loadKeyPair()
	{
		File keyPairFile = new File(keyPairFileName);
		if(!keyPairFile.exists())
		{
			System.out.println("Error missing keypair");
			System.exit(3);
		}
		
		if(prompt)
		{
			System.out.print("Enter server passphrase:");
			System.out.flush();
		}

		try
		{
			UnicodeReader reader = new UnicodeReader(System.in);
			String passphrase = reader.readLine();
			security = MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase);
		}
		catch (AuthorizationFailedException e)
		{
			System.err.println("Error probably bad passphrase: " + e + "\n");
			System.exit(1);
		}
		catch(Exception e)
		{
			System.err.println("Error loading keypair: " + e + "\n");
			System.exit(3);
		} 
	}
	
	void processArgs(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			String value = args[i].substring(args[i].indexOf("=")+1);
			if(args[i].startsWith("--no-prompt"))
				prompt = false;
			
			if(args[i].startsWith("--ip"))
				ip = value;
			
			if(args[i].startsWith("--port") && value != null)
			{
				port = new Integer(value).intValue();
			}
			
			if(args[i].startsWith("--public-code"))
				publicCode = MartusUtilities.removeNonDigits(value);
			
			if(args[i].startsWith("--output-file"))
				outputFileName = value;

			if(args[i].startsWith("--keypair"))
				keyPairFileName = value;
		}

		if(ip == null || port == 0 || publicCode == null || 
			outputFileName == null || keyPairFileName == null)
		{
			System.err.println("Incorrect arguments: RetrievePublicKey [--no-prompt] --ip=1.2.3.4 --port=5 --public-code=6.7.8.1.2 --output-file=pubkey.txt --keypair-file=keypair.dat\n");
			System.exit(2);
		}
		
	}

	boolean prompt = true;
	String ip;
	int port;
	String publicCode;
	String outputFileName;
	String keyPairFileName;

	MartusCrypto security; 
	CallerSideMirroringGateway gateway; 
}
