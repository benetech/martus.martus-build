package org.martus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;

public class CreateKeyPair
{
	public static void main(String[] args)
	{
		File keyPairFile = null;
		boolean prompt = true;
		
		for (int i = 0; i < args.length; i++)
		{
			if( args[i].startsWith("--keypair") )
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if( args[i].startsWith("--no-prompt") )
			{
				prompt = false;
			}
		}
		
		if( keyPairFile == null)
		{
			System.err.println("CreateKeyPair.java --keypair=<pathToKeypair> [--no-prompt]\nThis program will create a keypair.dat file.");
			System.err.flush();
			System.exit(2);
		}
		
		if(prompt)
		{
			System.out.print("Enter passphrase: ");
			System.out.flush();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			String passphrase = reader.readLine();
			
			MartusSecurity security = new MartusSecurity();
			security.createKeyPair();
			FileOutputStream out = new FileOutputStream(keyPairFile);
			security.writeKeyPair(out, passphrase);
			out.close();
			
			String publicCode = MartusUtilities.computePublicCode(security.getPublicKeyString());
			System.out.println("Public Code: " + MartusUtilities.formatPublicCode(publicCode));
			System.exit(0);
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			System.exit(3);
		}
	}
}
