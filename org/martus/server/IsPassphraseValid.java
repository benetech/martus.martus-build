package org.martus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;

public class IsPassphraseValid
{
	public static void main(String[] args)
	{
			if( args.length == 0 || !args[0].startsWith("--file") ) error("incorrect argument");

			File keyPairFile = new File(args[0].substring(args[0].indexOf("=")+1));
			
			if(!keyPairFile.isFile() || !keyPairFile.exists() ) error(keyPairFile.toString() + " is not a file");

			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			String passphrase = null;
			try
			{
				passphrase = stdin.readLine();
			}
			catch (IOException e)
			{
				error(e.toString());
			}

			try
			{
				loadCurrentMartusSecurity(keyPairFile, passphrase);
			}
			catch(AuthorizationFailedException e)
			{
				//System.err.println("failed: " + e);
				System.exit(1);
			}
			catch(Exception e)
			{
				error(e.toString());
			}
			System.exit(0);
	}
	
	private static void error(String msg)
	{
		System.err.println(msg);
		System.err.flush();
		System.exit(2);
	}

	private static void loadCurrentMartusSecurity(File keyPairFile, String passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
	}
}