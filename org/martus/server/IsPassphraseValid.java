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
	public static void main(String[] args)// throws Exception
	{
			File dataDirectory = null;
			if( args.length == 1 )
			{
				if( !args[0].startsWith("--data-directory") )
				{
					System.err.println("could not find keypair");
					System.exit(2);
				}
				dataDirectory = new File(args[0].substring(args[0].indexOf("=")+1));
			}
			else
			{
				System.err.println("could not find keypair");
				System.exit(2);
			}

			String keyPairFilename = MartusServer.getKeypairFilename();
			File keyPairFile = new File(dataDirectory, keyPairFilename);

			BufferedReader stdin = new BufferedReader( new InputStreamReader(System.in));
			String passphrase = null;
			try
			{
				passphrase = stdin.readLine();
			}
			catch (IOException e)
			{
				System.err.println("error: " + e);
				System.exit(2);
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
				System.err.println(e.toString());
				System.exit(2);
			}

			System.out.flush();
			System.exit(0);
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