package org.martus.server.core;

import java.io.File;
import java.io.IOException;

import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.server.forclients.MartusServerUtilities;

public class ServerFileDatabase extends FileDatabase 
{
	public ServerFileDatabase(File directory, MartusCrypto security)
	{
		super(directory, security);
	}

	protected String getBucketPrefix(DatabaseKey key) 
	{
		if(key.isDraft())
			return draftPrefix;
		return super.getBucketPrefix(key);
	}

	public boolean isDraftPacketBucket(String folderName)
	{
		return folderName.startsWith(draftPrefix);
	}
	
	public synchronized void loadAccountMap() throws FileVerificationException, MissingAccountMapSignatureException
	{
		super.loadAccountMap();
		if(isAccountMapExpected())
			super.verifyAccountMap();
	}
	
	public String getTimeStamp(DatabaseKey key) throws IOException, TooManyAccountsException
	{
		File file = getFileForRecord(key);
		long lastModifiedMillisSince1970 = file.lastModified();
		return MartusServerUtilities.getFormattedTimeStamp(lastModifiedMillisSince1970);
	}
	
	private static final String draftPrefix = "d" + defaultBucketPrefix;
}
