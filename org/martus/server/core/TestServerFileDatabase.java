package org.martus.server.core;

import java.io.File;
import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;

public class TestServerFileDatabase extends TestCaseEnhanced 
{
	public TestServerFileDatabase(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		security = new MockMartusSecurity();

		mockDb = new MockServerDatabase();

		File goodDir2 = createTempFile();
		goodDir2.delete();
		goodDir2.mkdir();
		serverFileDb = new ServerFileDatabase(goodDir2, security);
		serverFileDb.initialize();
	}
	
	public void testBasics() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey key = new DatabaseKey(uid);
		File dir = File.createTempFile("$$$MartusTestServerFileDatabase", null);
		dir.delete();
		dir.mkdir();
		ServerFileDatabase db = new ServerFileDatabase(dir, security);
		db.initialize();
				
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
		
		db.deleteAllData();
		dir.delete();
	}
	
	public void testDraftsServer() throws Exception
	{
		internalTestDrafts(mockDb);
		internalTestDrafts(serverFileDb);
	}
	
	private void internalTestDrafts(Database db) throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey draftKey = new DatabaseKey(uid);
		draftKey.setDraft();
		DatabaseKey sealedKey = new DatabaseKey(uid);
		sealedKey.setSealed();
		
		db.writeRecord(draftKey, smallString);
		db.writeRecord(sealedKey, smallString2);
		
		assertEquals(db.toString()+"draft wrong?", smallString, db.readRecord(draftKey, security));
		assertEquals(db.toString()+"sealed wrong?", smallString2, db.readRecord(sealedKey, security));
		
		class Counter implements Database.PacketVisitor
		{
			Counter(Database databaseToUse, Vector expected)
			{
				db = databaseToUse;
				expectedKeys = expected;
			}
			
			public void visit(DatabaseKey key)
			{
				assertContains(db.toString()+"wrong key?", key, expectedKeys);
				expectedKeys.remove(key);
			}
			
			Database db;
			Vector expectedKeys;
		}
		
		Vector allKeys = new Vector();
		allKeys.add(draftKey);
		allKeys.add(sealedKey);
		Counter counter = new Counter(db, allKeys);
		db.visitAllRecords(counter);
		assertEquals(db.toString()+"Not all keys visited?", 0, counter.expectedKeys.size());
		
		db.deleteAllData();
	}

	String smallString = "How are you doing?";
	String smallString2 = "Just another string 123";

	MockMartusSecurity security;
	MockServerDatabase mockDb;
	ServerFileDatabase serverFileDb;
}
