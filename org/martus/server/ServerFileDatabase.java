package org.martus.server;

import java.io.File;
import java.io.IOException;

import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;

public class ServerFileDatabase extends FileDatabase 
{
	public ServerFileDatabase(File directory)
		throws MissingAccountMapException 
	{
		super(directory);
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
