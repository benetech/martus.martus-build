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
			if( args.length == 0 || !args[0].startsWith("--keypair") )
			{
					System.err.println("Error: Incorrect argument.\nIsPassphraseValid --keypair=/path/keypair.dat" );
					System.err.flush();
					System.exit(2);
			}

			File keyPairFile = new File(args[0].substring(args[0].indexOf("=")+1));
			
			if(!keyPairFile.isFile() || !keyPairFile.exists() )
			{
				System.err.println("Error: " + keyPairFile.getAbsolutePath() + " is not a file" );
				System.err.flush();
				System.exit(3);
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
				System.out.println(MartusUtilities.formatPublicCode(publicCode));
				System.exit(0);
			}
			catch(AuthorizationFailedException e)
			{
				System.err.println("Error: " + e.toString() );
				System.err.flush();
				System.exit(3);
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