package org.martus.meta;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;
import org.martus.common.Database.RecordHiddenException;
import org.martus.server.core.ServerFileDatabase;

public class TestDatabaseHiddenRecords extends TestCaseEnhanced
{
	public TestDatabaseHiddenRecords(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		security = MockMartusSecurity.createServer(); 
		tempDirectory = createTempFile();
		tempDirectory.delete();
		tempDirectory.mkdir(); 
		fileDatabase = new ServerFileDatabase(tempDirectory, security);
		fileDatabase.initialize();
		mockDatabase = new MockServerDatabase();

		draftUid = UniversalId.createFromAccountAndPrefix("bogus account", "G");
		draftKey = DatabaseKey.createDraftKey(draftUid);

		sealedUid = UniversalId.createFromAccountAndPrefix("bogus account", "G");
		sealedKey = DatabaseKey.createSealedKey(sealedUid);
		
		assertNotEquals("duplicate uids?", draftUid, sealedUid);
	}

	protected void tearDown() throws Exception
	{
		fileDatabase.deleteAllData();
		tempDirectory.delete();
	}

	public void testBasics() throws Exception
	{
		verifyBasics(mockDatabase, draftUid);
		verifyBasics(mockDatabase, sealedUid);
		verifyBasics(fileDatabase, draftUid);
		verifyBasics(fileDatabase, sealedUid);
	}

	private void verifyBasics(Database db, UniversalId uid) throws Exception
	{
		assertFalse("already hidden?", db.isHidden(uid));
		db.hide(uid);
		assertTrue("not hidden?", db.isHidden(uid));
		db.hide(uid);
		assertTrue("not hidden after two hides?", db.isHidden(uid));
		db.deleteAllData();
		assertTrue("not hidden after deleteAllData?", db.isHidden(uid));
	}
	
	public void testHiddenFromReading() throws Exception
	{
		verifyHiddenFromReading(mockDatabase, draftKey);
		verifyHiddenFromReading(mockDatabase, sealedKey);
		verifyHiddenFromReading(fileDatabase, draftKey);
		verifyHiddenFromReading(fileDatabase, sealedKey);
	}
	
	void verifyHiddenFromReading(Database db, DatabaseKey key) throws Exception
	{
		writeAndHideRecord(db, key);
		assertNull("opened stream for hidden?", db.openInputStream(key, security));
		assertNull("able to read hidden?", db.readRecord(key, security));
		assertFalse("hidden exists?", db.doesRecordExist(key));
		try
		{
			db.getRecordSize(key);
			fail("Should have thrown for getRecordSize");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.isInQuarantine(key);
			fail("Should have thrown for isInQuarantine");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.getOutgoingInterimFile(key);
			fail("Should have thrown for getOutgoingInterimFile");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		db.deleteAllData();
	}

	public void testHiddenFromWriting() throws Exception
	{
		verifyHiddenFromWriting(mockDatabase, draftKey);
		verifyHiddenFromWriting(mockDatabase, sealedKey);
		verifyHiddenFromWriting(fileDatabase, draftKey);
		verifyHiddenFromWriting(fileDatabase, sealedKey);
	}

	public void testHiddenFromOverwriting() throws Exception
	{
		verifyHiddenFromOverwriting(mockDatabase, draftKey);
		verifyHiddenFromOverwriting(mockDatabase, sealedKey);
		verifyHiddenFromOverwriting(fileDatabase, draftKey);
		verifyHiddenFromOverwriting(fileDatabase, sealedKey);
	}
	
	private void verifyHiddenFromOverwriting(Database db, DatabaseKey key)  throws Exception
	{
		db.writeRecord(key, "some sample data");
		verifyHiddenFromWriting(db, key);
	}

	private void verifyHiddenFromWriting(Database db, DatabaseKey key) throws Exception
	{
		db.hide(key.getUniversalId());
		try
		{
			db.writeRecord(key, "sample data");
			fail("Should have thrown for writeRecord!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.moveRecordToQuarantine(key);
			fail("Should have thrown for moveRecordToQuarantine!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.getIncomingInterimFile(key);
			fail("Should have thrown for getIncomingInterimFile");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		db.discardRecord(key);
		db.discardRecord(key);
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
		db.hide(draftUid);
		db.hide(sealedUid);
		
		DatabaseKey visibleKey = DatabaseKey.createSealedKey(visibleUid);
		DatabaseKey hiddenDraftKey = DatabaseKey.createSealedKey(draftUid);
		DatabaseKey hiddenSealedKey = DatabaseKey.createSealedKey(sealedUid);
		HashMap entries = new HashMap();
		entries.put(visibleKey, null);
		entries.put(draftKey, null);
		entries.put(sealedKey, null);
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

	private void writeAndHideRecord(Database db, DatabaseKey key)
		throws IOException, RecordHiddenException
	{
		db.writeRecord(key, "test");
		db.hide(key.getUniversalId());
	}
	
	MockMartusSecurity security;
	File tempDirectory;	
	Database fileDatabase;
	Database mockDatabase;
	
	UniversalId draftUid;
	DatabaseKey draftKey;

	UniversalId sealedUid;
	DatabaseKey sealedKey;
}
