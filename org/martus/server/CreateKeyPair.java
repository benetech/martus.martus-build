package org.martus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusCrypto.CryptoInitializationException;

public class CreateKeyPair
{
	public static void main(String[] args)
	{
		if( args.length != 1 || !args[0].startsWith("--keypair"))
		{
			System.err.println("CreateKeyPair.java --keypair=<pathToKeypair>\nThis program will create a keypair.dat file.");
			System.err.flush();
			System.exit(2);
		}
		File keyPairFile = new File(args[0].substring(args[0].indexOf("=")+1));
		
		System.out.print("Enter passphrase: ");
		System.out.flush();

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
			System.out.print("Public Code: " + MartusUtilities.formatPublicCode(publicCode));
			System.exit(0);
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			System.exit(3);
		}
	}
}
