package org.martus.client;

import java.io.File;

import org.martus.common.FileDatabase;

public class ClientFileDatabase extends FileDatabase 
{
	public ClientFileDatabase(File directory)
		throws MissingAccountMapException 
	{
		super(directory);
	}

}
