package org.martus.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.server.forclients.MartusServerUtilities;

public class ServerFileSignerAndVerifier
{
	public static void main(String[] args) throws Exception
	{
			System.out.println("ServerFileSignerAndVerifier:\nUse this program to create a signature file of a specified file"
								+ " or to verify a file against it's signature file.");
			File keyPairFile = null;
			File fileForOperation = null;
			File fileSignature = null;
			boolean isSigningOperation = true;
			
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
				
				if(args[i].startsWith("--verify"))
				{
					isSigningOperation = false;
				}
				
				if(args[i].startsWith("--signature"))
				{
					fileSignature = new File(args[i].substring(args[i].indexOf("=")+1));
				}
			}
			
			if(keyPairFile == null || fileForOperation == null )
			{
				System.err.println("\nUsage:\n FileSignerAndVerifier [--sign | --verify --signature=<pathOfFileSignature>] --keypair=<pathOfKeyFile> --file=<pathToFileToSignOrVerify>");
				System.exit(2);
			}
			
			if(!keyPairFile.isFile() || !keyPairFile.exists())
			{
				System.err.println("Error: " + keyPairFile.getAbsolutePath() + " is not a file" );
				System.err.flush();
				System.exit(3);
			}
			
			if(!fileForOperation.isFile() || !fileForOperation.exists())
			{
				System.err.println("Error: " + fileForOperation.getAbsolutePath() + " is not a file" );
				System.err.flush();
				System.exit(3);
			}

			System.out.print("Enter server passphrase:");
			System.out.flush();
			
			MartusCrypto security = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try
			{
				String passphrase = reader.readLine();
				security = MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase);
			}
			catch(Exception e)
			{
				System.err.println("FileSignerAndVerifier.main: " + e);
				System.exit(3);
			}
			
			if(isSigningOperation)
			{
				fileSignature = MartusServerUtilities.createSignatureFileFromFileOnServer(fileForOperation, security);
				System.out.println("Signature file created at " + fileSignature.getAbsolutePath());
			}
			else
			{
				if(fileSignature == null)
				{
					fileSignature = MartusServerUtilities.getLatestSignatureFileFromFile(fileForOperation);
					if(fileSignature == null)
					{
						System.err.println("Error: unable to locate a signature file for " +  fileForOperation.getAbsolutePath());
						System.err.println("You need to indicate the path to the signature file on the command-line.");
						System.err.flush();
						System.exit(3);
					}
				}
				
				try
				{
					MartusServerUtilities.verifyFileAndSignatureOnServer(fileForOperation, fileSignature, security, security.getPublicKeyString());
				}
				catch (FileVerificationException e)
				{
					System.err.println("File " + fileForOperation.getAbsolutePath()
										+ " did not verify against signature file "
										+ fileSignature.getAbsolutePath() + ".");
					System.exit(3);
				}
				System.out.println("File " + fileForOperation.getAbsolutePath()
									+ " verified successfully against signature file "
									+ fileSignature.getAbsolutePath() + ".");
			}
			System.exit(0);
	}
}