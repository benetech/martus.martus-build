package org.martus.server;

import java.io.File;

import org.martus.common.Database;
import org.martus.common.FileDatabase;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.FileDatabase.MissingAccountMapException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusUtilities.FileVerificationException;

public class ShowServerAccountList 
{
	public static void main(String[] args)
		throws FileDatabase.MissingAccountMapException, MartusUtilities.FileVerificationException, CryptoInitializationException
	{
		File dataDir = null;
		
		if( args.length == 0 )
		{
			dataDir = MartusServer.getDefaultDataDirectory();
		}
		else if( args[0].startsWith("--packet-directory=") )
		{
			dataDir = new File(new File (args[0].substring(args[0].indexOf("=")+1)).getParent());
		}
		
		if(!dataDir.exists() || !dataDir.isDirectory() )
		{
			System.err.println("Error: " + dataDir + " is not a valid data directory.");
			System.exit(2);
		}

		new ShowServerAccountList(dataDir);
		System.exit(0);
	}
	
	ShowServerAccountList(File dataDirectory) throws CryptoInitializationException, MissingAccountMapException, FileVerificationException
	{		
		fileDatabase = new ServerFileDatabase(new File(dataDirectory, "packets"), new MartusSecurity());
		fileDatabase.visitAllAccounts(new AccountVisitor());
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
