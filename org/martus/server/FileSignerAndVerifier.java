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
			System.out.println("FileSignerVerifier:\nUse this program to create a signature file of a specified file with the server key"
								+ "or to verify a server file against it's signature file.");
			boolean isSigningOperation = true;
			File fileToSign = null;
			File fileToVerify = null;
			File fileSignature = null;
			File keyPairFile = null;
			if( args.length == 3 )
			{
				if( args[0].compareToIgnoreCase("-sign") != 0 ) usage();
				fileToSign = new File(args[1]);
				keyPairFile = new File(args[2]);
				if(!fileToSign.exists() || !keyPairFile.exists()) usage();
			}
			else if(args.length == 4 )
			{
				if( args[0].compareToIgnoreCase("-verify") != 0 ) usage();
				fileToVerify = new File(args[1]);
				fileSignature = new File(args[2]);
				keyPairFile = new File(args[3]);
				if(!fileToVerify.exists() || !fileSignature.exists() || !keyPairFile.exists()) usage();
				isSigningOperation = false;
			}
			else
			{
				usage();
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
				System.out.println("FileSignerVerifier.main: " + e);
				System.exit(3);
			}
			
			if(isSigningOperation)
			{
				File signatureFile = MartusUtilities.createSignatureFileFromFile(fileToSign, security);

				System.out.println("Signature file created at " + signatureFile.getAbsolutePath());
			}
			else
			{
				try
				{
					MartusUtilities.verifyFileAndSignature(fileToVerify, fileSignature, security, security.getPublicKeyString());
				}
				catch(FileVerificationException e)
				{
					System.out.println("File " + fileToVerify.getAbsolutePath()
										+ " did not verify against signature file "
										+ fileSignature.getAbsolutePath() + ".");
					throw new FileVerificationException();
				}
				
				System.out.println("File " + fileToVerify.getAbsolutePath()
									+ " verified successfully against signature file "
									+ fileSignature.getAbsolutePath() + ".");
			}
			System.out.flush();
	}
	
	private static void usage()
	{
		System.out.println("\nUsage:\n FileSignerVerifier [-sign <pathOfFileToSign> || -verify <pathOfFileToVerify> <pathOfSignatureFile>] <pathOfKeyFile>");
		System.exit(1);
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