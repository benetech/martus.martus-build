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
import org.martus.common.MartusUtilities.FileVerificationException;

public class FileSignerAndVerifier
{
	public static void main(String[] args) throws Exception
	{
			System.out.println("FileSignerAndVerifier:\nUse this program to create a signature file of a specified file"
								+ " or to verify a file against it's signature file.");
			File keyPairFile = null;
			File fileForOperation = null;
			
			if( args.length != 2 )
			{
				System.err.println("\nUsage:\n FileSignerAndVerifier --keypair=<pathOfKeyFile> --file=<pathToFileToSignOrVerify>");
				System.exit(2);
			}
			
			for (int i = 0; i < args.length; i++)
			{
				if(args[i].startsWith("--keypair"))
				{
					keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
				}
				
				if(args[i].startsWith("--file"))
				{
					fileForOperation = new File(args[i].substring(args[i].indexOf("=")+1));
				}
			}
			
			if(keyPairFile == null || fileForOperation == null || !keyPairFile.isFile())
			{
				System.err.println("\nUsage:\n FileSignerAndVerifier --keypair=<pathOfKeyFile> --file=<pathToFileToSignOrVerify>");
				System.exit(2);
			}

			System.out.print("Enter server passphrase:");
			System.out.flush();
			
			MartusCrypto security = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try
			{
				String passphrase = reader.readLine();
				security = loadCurrentMartusSecurity(keyPairFile, passphrase);
			}
			catch(Exception e)
			{
				System.err.println("FileSignerAndVerifier.main: " + e);
				System.exit(3);
			}
			
			File signatureFile = MartusUtilities.getSignatureFileFromFile(fileForOperation);
			
			if(signatureFile.exists())
			{
				try
				{
					MartusUtilities.verifyFileAndSignature(fileForOperation, signatureFile, security, security.getPublicKeyString());
				}
				catch(FileVerificationException e)
				{
					System.err.println("File " + fileForOperation.getAbsolutePath()
										+ " did not verify against signature file "
										+ signatureFile.getAbsolutePath() + ".");
					System.exit(3);
				}
				
				System.out.println("File " + fileForOperation.getAbsolutePath()
									+ " verified successfully against signature file "
									+ signatureFile.getAbsolutePath() + ".");
			}
			else
			{
				signatureFile = MartusUtilities.createSignatureFileFromFile(fileForOperation, security);
				System.out.println("Signature file created at " + signatureFile.getAbsolutePath());
			}
			System.exit(0);
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
}