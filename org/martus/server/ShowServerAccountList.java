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
		new ShowServerAccountList();
	}
	
	ShowServerAccountList() throws CryptoInitializationException, MissingAccountMapException, FileVerificationException
	{
		File dataDirectory = MartusServer.getDefaultDataDirectory();		
		
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
