package org.martus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;

public class ChangeServerPassphrase
{
	public static void main(String[] args) throws Exception
	{
			System.out.println("ChangeServerPassphrase:\nThis program will replace your keypair.dat file."
				+ "\nWe strongly recommend that you make sure you have a backup copy before running this program. "
				+ "\nAlso, after successfully changing the password, we strongly recommend that you create a backup of the new keypair.dat file.\n");
			File dataDirectory = MartusServer.getDataDirectory();
			String keyPairFilename = MartusServer.getKeypairFilename();
			File keyPairFile = new File(dataDirectory, keyPairFilename);
			if(!keyPairFile.exists())
			{
				System.out.println("There is no server account.");
				System.out.print("Create an account first by running the Martus Server.");
				System.out.flush();
				System.exit(1);
			}

			System.out.print("Enter current passphrase:");
			System.out.flush();
			
			InputStreamReader rawReader = new InputStreamReader(System.in);	
			BufferedReader reader = new BufferedReader(rawReader);
			try
			{
				String oldPassphrase = reader.readLine();
				
				MartusCrypto security = loadCurrentMartusSecurity(keyPairFile, oldPassphrase);
				
				System.out.print("Enter new passphrase:");
				System.out.flush();
				String newPassphrase1 = reader.readLine();
				System.out.print("Re-enter the new passphrase:");
				System.out.flush();
				String newPassphrase2 = reader.readLine();
				
				if( newPassphrase1.equals(newPassphrase2) )
				{
					System.out.println("Updating passphrase...");
					System.out.flush();
					updateMartusPassphrase(keyPairFile, newPassphrase1, security);
				}
				else
				{
					throw new NewPasswordsNotSame();
				}
			}
			catch(Exception e)
			{
				System.out.println("ChangeServerPassphrase.main: " + e);
				System.exit(3);
			}
			System.out.println("Server passphrase updated.");
			System.out.flush();			
	}
	
	private static void updateMartusPassphrase(File keyPairFile, String newPassphrase, MartusCrypto security)
		throws FileNotFoundException, IOException
	{
		FileOutputStream out = new FileOutputStream(keyPairFile);
		security.writeKeyPair(out, newPassphrase);
	}
	
	private static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, String passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}
	
	public static class NewPasswordsNotSame extends Exception {}
}