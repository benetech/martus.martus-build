package org.martus.server;

import java.io.File;

import org.martus.common.*;

public class TestServerFileDatabase extends TestCaseEnhanced 
{
	public TestServerFileDatabase(String name) 
	{
		super(name);
	}
	
	public void testBasics() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey key = new DatabaseKey(uid);
		File dir = File.createTempFile("$$$MartusTestServerFileDatabase", null);
		dir.delete();
		dir.mkdir();
		ServerFileDatabase db = new ServerFileDatabase(dir);
				
		key.setSealed();
		File sealedFile = db.getFileForRecord(key);
		File sealedBucket = sealedFile.getParentFile();
		String sealedBucketName = sealedBucket.getName();
		assertStartsWith("Wrong sealed bucket name", "pb", sealedBucketName);
		
		key.setDraft();
		File draftFile = db.getFileForRecord(key);
		File draftBucket = draftFile.getParentFile();
		String draftBucketName = draftBucket.getName();
		assertStartsWith("Wrong draft bucket name", "dpb", draftBucketName);
	}
}
