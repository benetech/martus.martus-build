package org.martus.server.core;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.server.forclients.MartusServerUtilities;
import org.martus.server.forclients.MartusServerUtilities.MartusSignatureFileDoesntExistsException;

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
		{
			File accountMapFile = super.getAccountMapFile();
			File sigFile;

			try
			{
				sigFile = MartusServerUtilities.getLatestSignatureFileFromFile(accountMapFile);
				MartusServerUtilities.verifyFileAndSignatureOnServer(accountMapFile, sigFile, security, security.getPublicKeyString());
			}
			catch (IOException e)
			{
				throw new FileVerificationException();
			}
			catch (ParseException e)
			{
				throw new FileVerificationException();
			}
			catch (MartusSignatureFileDoesntExistsException e)
			{
				throw new MissingAccountMapSignatureException();
			}
		}
	}
	
	public void signAccountMap() throws IOException, MartusCrypto.MartusSignatureException
	{
		File accountMapFile = super.getAccountMapFile();
		try
		{
			MartusServerUtilities.createSignatureFileFromFileOnServer(accountMapFile, security);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new MartusSignatureException();
		}
	}
	
	public void deleteSignaturesForFile(File origFile)
	{
		MartusServerUtilities.deleteSignaturesForFile(origFile);
	}
	
	public String getTimeStamp(DatabaseKey key) throws IOException, TooManyAccountsException
	{
		File file = getFileForRecord(key);
		long lastModifiedMillisSince1970 = file.lastModified();
		return MartusServerUtilities.getFormattedTimeStamp(lastModifiedMillisSince1970);
	}
	
	private static final String draftPrefix = "d" + defaultBucketPrefix;
}
