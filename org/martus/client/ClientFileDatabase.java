package org.martus.client;

import java.io.File;

import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities.FileVerificationException;

public class ClientFileDatabase extends FileDatabase 
{
	public ClientFileDatabase(File directory, MartusCrypto security)
		throws MissingAccountMapException, FileVerificationException 
	{
		super(directory, security);
	}

}
