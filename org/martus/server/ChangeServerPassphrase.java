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
				String passphrase = reader.readLine();
				
				MartusCrypto security = getCurrentMartusSecurity(keyPairFile, passphrase);
				
				System.out.print("Enter new passphrase:");
				System.out.flush();
				String newPassphrase = reader.readLine();
				System.out.print("Re-enter the new passphrase:");
				System.out.flush();
				
				if( newPassphrase.equals(reader.readLine()) )
				{
					updateMartusPassphrase(keyPairFile, security, newPassphrase);
				}
				else
				{
					throw new Exception();
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
	
	private static void updateMartusPassphrase(File keyPairFile, MartusCrypto security, String newPassphrase)
		throws FileNotFoundException, IOException
	{
		System.out.println("Updating passphrase...");
		System.out.flush();
		FileOutputStream out = new FileOutputStream(keyPairFile);
		security.writeKeyPair(out, newPassphrase);
	}
	
	private static MartusCrypto getCurrentMartusSecurity(File keyPairFile, String passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}
}