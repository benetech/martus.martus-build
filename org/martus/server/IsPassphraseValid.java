package org.martus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;

public class IsPassphraseValid
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
		
		if(keyPairFile == null)
		{
				System.err.println("Error: Incorrect argument.\nIsPassphraseValid --keypair=/path/keypair.dat [--no-prompt]" );
				System.err.flush();
				System.exit(2);
		}
		
		if(!keyPairFile.isFile() || !keyPairFile.exists() )
		{
			System.err.println("Error: " + keyPairFile.getAbsolutePath() + " is not a file" );
			System.err.flush();
			System.exit(3);
		}
		
		if(prompt)
		{
			System.out.print("Enter passphrase: ");
			System.out.flush();
		}

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String passphrase = null;
		try
		{
			passphrase = stdin.readLine();
		}
		catch (IOException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(3);
		}

		try
		{
			MartusSecurity security = (MartusSecurity) loadCurrentMartusSecurity(keyPairFile, passphrase);
			String publicCode = MartusUtilities.computePublicCode(security.getPublicKeyString());
			System.out.println("Public Code: " + MartusUtilities.formatPublicCode(publicCode));
			System.exit(0);
		}
		catch (AuthorizationFailedException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(1);
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(3);
		}
	}

	private static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, String passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto crypto = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		crypto.readKeyPair(in, passphrase);
		in.close();
		return crypto;
	}
}