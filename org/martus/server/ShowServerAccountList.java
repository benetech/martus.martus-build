package org.martus.server;

import java.io.File;

import org.martus.common.Database;
import org.martus.common.FileDatabase;
import org.martus.common.MartusUtilities;

public class ShowServerAccountList 
{
	

	public static void main(String[] args) throws FileDatabase.MissingAccountMapException
	{
		File dataDirectory = MartusServer.getDefaultDataDirectory();		
		
		FileDatabase fileDatabase = new ServerFileDatabase(new File(dataDirectory, "packets"));
		fileDatabase.visitAllAccounts(new AccountVisitor());
	}

	static class AccountVisitor implements Database.AccountVisitor 
	{
		public void visit(String accountString, File accountDir)
		{
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

}
