package org.martus.meta;

import java.io.File;
import java.util.HashMap;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;
import org.martus.server.core.ServerFileDatabase;

public class TestDatabaseHiddenRecords extends TestCaseEnhanced
{
	public TestDatabaseHiddenRecords(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		tempDirectory = createTempFile();
		tempDirectory.delete();
		tempDirectory.mkdir(); 
		fileDatabase = new ServerFileDatabase(tempDirectory, MockMartusSecurity.createServer());
		fileDatabase.initialize();
		mockDatabase = new MockServerDatabase();
	}

	protected void tearDown() throws Exception
	{
		fileDatabase.deleteAllData();
		tempDirectory.delete();
	}

	public void testBasics() throws Exception
	{
		verifyBasics(mockDatabase);
		verifyBasics(fileDatabase);
	}

	private void verifyBasics(Database db) throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		assertFalse("already hidden?", db.isHidden(uid));
		db.hide(uid);
		assertTrue("not hidden?", db.isHidden(uid));
		db.hide(uid);
		assertTrue("not hidden after two hides?", db.isHidden(uid));
		db.deleteAllData();
		assertTrue("not hidden after deleteAllData?", db.isHidden(uid));
	}
	
	public void testWriteHiddenRecordToServerDatabase() throws Exception
	{
		verifyWriteHiddenRecord(mockDatabase);
		verifyWriteHiddenRecord(fileDatabase);
	}

	private void verifyWriteHiddenRecord(Database db) throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		db.hide(uid);
		DatabaseKey draftKey = DatabaseKey.createDraftKey(uid);
		try
		{
			db.writeRecord(draftKey, "draft");
			fail("Should have thrown for draft!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		try
		{
			db.writeRecord(sealedKey, "sealed");
			fail("Should have thrown for sealed!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		
		db.deleteAllData();
	}
	
	public void testImportHiddenRecordToServerDatabase() throws Exception
	{
		verifyImportHiddenRecord(mockDatabase);
		verifyImportHiddenRecord(fileDatabase);
	}

	private void verifyImportHiddenRecord(Database db)
		throws Exception
	{
		UniversalId visibleUid = UniversalId.createDummyUniversalId();
		UniversalId hiddenUid = UniversalId.createDummyUniversalId();
		db.hide(hiddenUid);
		
		DatabaseKey visibleKey = DatabaseKey.createSealedKey(visibleUid);
		DatabaseKey hiddenKey = DatabaseKey.createSealedKey(hiddenUid);
		HashMap entries = new HashMap();
		entries.put(visibleKey, null);
		entries.put(hiddenKey, null);
		try
		{
			db.importFiles(entries);
			fail("Should have thrown!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		
		db.deleteAllData();
	}
	
	// TODO: need to test BulletinZipUtilities.importBulletinPacketsFromZipFileToDatabase

	File tempDirectory;	
	Database fileDatabase;
	Database mockDatabase;
}
