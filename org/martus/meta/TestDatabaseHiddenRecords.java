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
		verifyBasics(mockDatabase);
		verifyBasics(fileDatabase);
	}

	private void verifyBasics(Database db) throws Exception
	{
		assertFalse("draft already hidden?", db.isHidden(draftUid));
		assertFalse("sealed already hidden?", db.isHidden(sealedUid));
		db.hide(draftUid);
		db.hide(sealedUid);
		assertTrue("draft not hidden?", db.isHidden(draftUid));
		assertTrue("sealed not hidden?", db.isHidden(sealedUid));
		db.hide(draftUid);
		db.hide(sealedUid);
		assertTrue("draft not hidden after two hides?", db.isHidden(draftUid));
		assertTrue("sealed not hidden after two hides?", db.isHidden(sealedUid));
		db.deleteAllData();
		assertTrue("draft not hidden after deleteAllData?", db.isHidden(draftUid));
		assertTrue("sealed not hidden after deleteAllData?", db.isHidden(sealedUid));
	}
	
	public void testWriteHiddenRecordToServerDatabase() throws Exception
	{
		verifyWriteHiddenRecord(mockDatabase);
		verifyWriteHiddenRecord(fileDatabase);
	}

	private void verifyWriteHiddenRecord(Database db) throws Exception
	{
		db.hide(draftUid);
		DatabaseKey draftKey = DatabaseKey.createDraftKey(draftUid);
		try
		{
			db.writeRecord(draftKey, "draft");
			fail("Should have thrown for draft!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		
		db.hide(sealedUid);
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(sealedUid);
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
	
	public void testOpenInputStreamHidden() throws Exception
	{
		verifyOpenInputStreamHidden(mockDatabase, draftKey);
		verifyOpenInputStreamHidden(mockDatabase, sealedKey);
		verifyOpenInputStreamHidden(fileDatabase, draftKey);
		verifyOpenInputStreamHidden(fileDatabase, sealedKey);
	}
	
	void verifyOpenInputStreamHidden(Database db, DatabaseKey key) throws Exception
	{
		writeAndHideRecord(db, key);
		assertNull("opened stream for hidden?", db.openInputStream(key, security));
		db.deleteAllData();
	}

	public void testReadRecordHidden() throws Exception
	{
		verifyReadRecordHidden(mockDatabase, draftKey);
		verifyReadRecordHidden(mockDatabase, sealedKey);
		verifyReadRecordHidden(fileDatabase, draftKey);
		verifyReadRecordHidden(fileDatabase, sealedKey);
	}
	
	void verifyReadRecordHidden(Database db, DatabaseKey key) throws Exception
	{
		writeAndHideRecord(db, key);
		assertNull("able to read hidden?", db.readRecord(key, security));
		db.deleteAllData();
	}
	
	public void testDiscardRecordHidden() throws Exception
	{
		verifyDiscardRecordHidden(mockDatabase, draftKey);
		verifyDiscardRecordHidden(mockDatabase, sealedKey);
		verifyDiscardRecordHidden(fileDatabase, draftKey);
		verifyDiscardRecordHidden(fileDatabase, sealedKey);
	}
	
	void verifyDiscardRecordHidden(Database db, DatabaseKey key) throws Exception
	{
		writeAndHideRecord(db, key);
		db.discardRecord(key);
		db.discardRecord(key);
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
