package org.martus.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.server.forclients.MartusServerUtilities;
import org.martus.server.forclients.MartusServerUtilities.MartusSignatureFileAlreadyExistsException;
import org.martus.server.forclients.MartusServerUtilities.MartusSignatureFileDoesntExistsException;

public class ServerFileSignerAndVerifier
{
	public static void main(String[] args) throws Exception
	{
			System.out.println("ServerFileSignerAndVerifier:\nUse this program to create a signature file of a specified file"
								+ " or to verify a file against it's signature file.");
			File keyPairFile = null;
			File fileForOperation = null;
			File signatureFile = null;
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
					signatureFile = new File(args[i].substring(args[i].indexOf("=")+1));
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
			finally
			{
				reader.close();
			}
			
			try
			{
				if(isSigningOperation)
				{
					signFile(fileForOperation, security);
				}
				else
				{
					verifyFile(fileForOperation, signatureFile, security);
				}
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
				System.exit(3);
			}
			System.exit(0);
	}

	public static void signFile(File fileForOperation, MartusCrypto security)
		throws IOException, MartusSignatureException, InterruptedException, MartusSignatureFileAlreadyExistsException
	{
		File signatureFile;
		signatureFile = MartusServerUtilities.createSignatureFileFromFileOnServer(fileForOperation, security);
		System.out.println("Signature file created at " + signatureFile.getAbsolutePath());
	}
	
	public static void verifyFile(File fileForOperation, File signatureFile, MartusCrypto security)
		throws IOException, ParseException, MartusSignatureFileDoesntExistsException, FileVerificationException
	{
		if(signatureFile == null)
		{
			signatureFile = MartusServerUtilities.getLatestSignatureFileFromFile(fileForOperation);
		}
		MartusServerUtilities.verifyFileAndSignatureOnServer(fileForOperation, signatureFile, security, security.getPublicKeyString());
		System.out.println("File " + fileForOperation.getAbsolutePath()
							+ " verified successfully against signature file "
							+ signatureFile.getAbsolutePath() + ".");
	}
}