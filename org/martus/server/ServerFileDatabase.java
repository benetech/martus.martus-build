package org.martus.server;

import java.io.File;

import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities.FileVerificationException;

public class ServerFileDatabase extends FileDatabase 
{
	public ServerFileDatabase(File directory, MartusCrypto security)
		throws MissingAccountMapException, FileVerificationException 
	{
		super(directory, security);
	}

	protected String getBucketPrefix(DatabaseKey key) 
	{
		String bucketPrefix = defaultBucketPrefix;
		if(key.isDraft())
			return draftPrefix;
		return super.getBucketPrefix(key);
	}

	public boolean isDraftPacketBucket(String folderName)
	{
		return folderName.startsWith(draftPrefix);
	}
	
	private static final String draftPrefix = "d" + defaultBucketPrefix;
}
