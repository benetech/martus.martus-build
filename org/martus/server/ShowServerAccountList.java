package org.martus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.martus.common.Database;
import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.FileDatabase.MissingAccountMapException;
import org.martus.common.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.MartusUtilities.FileVerificationException;

public class ShowServerAccountList 
{
	public static void main(String[] args)
		throws FileDatabase.MissingAccountMapException, MartusUtilities.FileVerificationException, CryptoInitializationException, MissingAccountMapSignatureException
	{
		File dataDir = null;
		File keyPairFile = null;
		
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--keypair"))
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--packet-directory="))
			{
				dataDir = new File(args[i].substring(args[i].indexOf("=")+1));
			}
		}
		
		if(dataDir == null || keyPairFile == null )
		{
			System.err.println("\nUsage: ShowServerAccountList --packet-directory=<directory> --keypair=<pathToKeyPairFile>");
			System.exit(2);
		}
		
		if(!keyPairFile.exists() || !keyPairFile.isFile() )
		{
			System.err.println("Error: " + keyPairFile + " is not a valid keypair file.");
			System.exit(2);
		}
		
		if(!dataDir.exists() || !dataDir.isDirectory() )
		{
			System.err.println("Error: " + dataDir + " is not a valid data directory.");
			System.exit(2);
		}
		
		MartusCrypto security = null;
		
		System.out.print("Enter server passphrase:");
		System.out.flush();
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

		new ShowServerAccountList(dataDir, security);
		System.exit(0);
	}
	
	ShowServerAccountList(File dataDirectory, MartusCrypto security) throws CryptoInitializationException, MissingAccountMapException, MissingAccountMapSignatureException, FileVerificationException
	{		
		fileDatabase = new ServerFileDatabase(dataDirectory, security);
		fileDatabase.initialize();
		fileDatabase.visitAllAccounts(new AccountVisitor());
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

	class AccountVisitor implements Database.AccountVisitor 
	{
		public void visit(String accountString)
		{
			File accountDir = fileDatabase.getAbsoluteAccountDirectory(accountString);
			File bucket = accountDir.getParentFile();
			String publicCode = "";
			try
			{
				publicCode = MartusUtilities.formatPublicCode(MartusUtilities.computePublicCode(accountString));
			}
			catch(Exception e)
			{
				publicCode = "ERROR: " + e;
			}
			
			System.out.println(publicCode + "=" + bucket.getName() + "/" + accountDir.getName());
		}
	}

	FileDatabase fileDatabase;
}
