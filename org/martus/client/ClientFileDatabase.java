package org.martus.client;

import java.io.File;

import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;

public class ClientFileDatabase extends FileDatabase 
{
	public ClientFileDatabase(File directory, MartusCrypto security)
	{
		super(directory, security);
	}

	public boolean mustEncryptLocalData()
	{
		return true;
	}
}
