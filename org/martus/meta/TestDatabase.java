package org.martus.meta;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

import org.martus.client.ClientFileDatabase;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;
import org.martus.common.MockDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.Database.PacketVisitor;
import org.martus.server.ServerFileDatabase;



public class TestDatabase extends TestCaseEnhanced
{
	public TestDatabase(String name) throws Exception
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		mockDb = new MockDatabase();

		goodDir1 = createTempFile();
		goodDir1.delete();
		goodDir1.mkdir();
		clientFileDb = new ClientFileDatabase(goodDir1);
		
		goodDir2 = createTempFile();
		goodDir2.delete();
		goodDir2.mkdir();
		serverFileDb = new ServerFileDatabase(goodDir2);
		
		largeBytes = largeString.getBytes("UTF-8");

		security = new MockMartusSecurity();
	}

	public void tearDown()
	{
		mockDb.deleteAllData();
		clientFileDb.deleteAllData();
		serverFileDb.deleteAllData();
		assertTrue("cleanup failed1?", goodDir1.delete());
		assertTrue("cleanup failed2?", goodDir2.delete());
	}
	
	public void TRACE(String text)
	{
		//System.out.println(text);
	}

	////////// File
	public void testEmptyDatabase() throws Exception
	{
		TRACE("testSmallWriteRecord");
		internalTestEmptyDatabase(mockDb);
		internalTestEmptyDatabase(clientFileDb);
		internalTestEmptyDatabase(serverFileDb);
	}
	
	public void testSmallWriteRecord() throws Exception
	{
		TRACE("testSmallWriteRecord");
		internalTestSmallWriteRecord(mockDb);
		internalTestSmallWriteRecord(clientFileDb);
	}

	public void testLargeWriteRecord() throws Exception
	{
		TRACE("testLargeWriteRecord");
		internalTestLargeWriteRecord(mockDb);
		internalTestLargeWriteRecord(clientFileDb);
	}

	public void testLargeRecordInputStream() throws Exception
	{
		TRACE("testLargeRecordInputStream");
		internalTestLargeRecordInputStream(mockDb);
		internalTestLargeRecordInputStream(clientFileDb);
	}

	public void testReplaceWriteRecord() throws Exception
	{
		TRACE("testReplaceWriteRecord");
		internalTestReplaceWriteRecord(mockDb);
		internalTestReplaceWriteRecord(clientFileDb);
	}

	public void testDiscard() throws Exception
	{
		TRACE("testDiscard");
		internalTestDiscard(mockDb);
		internalTestDiscard(clientFileDb);
	}

	public void testDoesRecordExist() throws Exception
	{
		TRACE("testDoesRecordExist");
		internalTestDoesRecordExist(mockDb);
		internalTestDoesRecordExist(clientFileDb);
	}

	public void testVisitAllRecords() throws Exception
	{
		TRACE("testVisitAllRecords");

		internalTestVisitAllRecords(mockDb);
		internalTestVisitAllRecords(clientFileDb);
	}

	public void testDeleteAllData() throws Exception
	{
		TRACE("testDeleteAllData");
		internalTestDeleteAllData(mockDb);
		internalTestDeleteAllData(clientFileDb);
	}

	public void testSmallWriteRecordFromStream() throws Exception
	{
		TRACE("testSmallWriteRecordFromStream");
		internalTestSmallWriteRecordFromStream(mockDb);
		internalTestSmallWriteRecordFromStream(clientFileDb);
	}

	public void testLargeWriteRecordFromStream() throws Exception
	{
		TRACE("testLargeWriteRecordFromStream");
		internalTestLargeWriteRecordFromStream(mockDb);
		internalTestLargeWriteRecordFromStream(clientFileDb);
	}

	public void testBadStream()
	{
		TRACE("testBadKey");
		internalTestBadStream(mockDb);
		internalTestBadStream(clientFileDb);
	}

	public void testInternalTestGetIncomingInterimFile() throws Exception
	{
		TRACE("testInternalTestGetIncomingInterimFile");
		internalTestGetIncomingInterimFile(mockDb);
		internalTestGetIncomingInterimFile(clientFileDb);
	}
	
	public void testGetOutgoingInterimFile() throws Exception
	{
		TRACE("testBuildInterimFileFromBulletinPackets");
		internalTestGetOutgoingInterimFile(mockDb);
		internalTestGetOutgoingInterimFile(clientFileDb);
	}

	public void testDraftsServer() throws Exception
	{
		TRACE("testDraftsServer");
		internalTestDrafts(mockDb);
		// this test is NOT VALID for ClientFileDatabase
		//internalTestDrafts(clientFileDb);
		internalTestDrafts(serverFileDb);
	}
	/////////////////////////////////////////////////////////////////////

	private void internalTestEmptyDatabase(Database db) throws Exception
	{
		assertNull("found non-existant String record?", db.readRecord(smallKey, security));
		assertNull("found non-existant Stream record?", db.openInputStream(smallKey, security));
	}
	
	private void internalTestSmallWriteRecord(Database db) throws Exception
	{
		try
		{
			db.writeRecord(null, smallString);
			fail(db.toString()+"should have thrown for null key");
		}
		catch(IOException ignoreExpectedException)
		{
		}

		try
		{
			db.writeRecord(smallKey, (String)null);
			fail(db.toString()+"should have thrown for null string");
		}
		catch(IOException ignoreExpectedException)
		{
		}

		db.writeRecord(smallKey, smallString);
		String gotBack = db.readRecord(smallKey, security);
		assertNotNull(db.toString()+"read failed", gotBack);
		assertEquals(db.toString()+"wrong data?", smallString, gotBack);
	}

	private void internalTestLargeWriteRecord(Database db) throws Exception
	{
		db.writeRecord(largeKey, largeString);
		String gotBackLarge1 = db.readRecord(largeKey, security);
		assertNotNull(db.toString()+"read large1", gotBackLarge1);
		assertEquals(db.toString()+"large string1", largeString, gotBackLarge1);
	}

	private void internalTestLargeRecordInputStream(Database db) throws Exception
	{
		db.writeRecord(largeKey, largeString);
		InputStream in = db.openInputStream(largeKey, security);
		assertNotNull(db.toString()+"no input stream?", in);
		assertEquals(db.toString()+"wrong length?", largeBytes.length, in.available());
		byte[] got = new byte[largeBytes.length];
		in.read(got);
		in.close();
		assertEquals(db.toString()+"bad data", true, Arrays.equals(largeBytes, got));
	}
	
	private void internalTestReplaceWriteRecord(Database db) throws Exception
	{
		db.writeRecord(largeKey, largeString);
		db.writeRecord(smallKey, smallString);
		String gotBack = db.readRecord(smallKey, security);
		assertNotNull(db.toString()+"read2 failed", gotBack);
		assertEquals(db.toString()+"wrong data2?", smallString, gotBack);
	}

	private void internalTestDiscard(Database db) throws Exception
	{
		db.writeRecord(smallKey, smallString);
		db.writeRecord(largeKey, largeString);

		db.discardRecord(smallKey);
		assertNull(db.toString()+"discard failed", db.readRecord(smallKey, security));

		String gotBackLarge3 = db.readRecord(largeKey, security);
		assertNotNull(db.toString()+"read large3", gotBackLarge3);
		assertEquals(db.toString()+"large string3", largeString, gotBackLarge3);
	}

	private void internalTestDoesRecordExist(Database db) throws Exception
	{
		assertEquals(db.toString()+"database not empty", false, db.doesRecordExist(smallKey));
		db.writeRecord(smallKey, smallString);
		assertEquals(db.toString()+"record doesn't exist", true, db.doesRecordExist(smallKey));
		db.discardRecord(smallKey);
		assertEquals(db.toString()+"record didn't discard", false, db.doesRecordExist(smallKey));
	}

	class PacketCounter implements Database.PacketVisitor
	{
		PacketCounter(Database dbToUse)
		{
			db = dbToUse;
		}
		
		public void visit(DatabaseKey key)
		{
			++count;
			assertTrue(db.toString()+"bad key?", db.doesRecordExist(key));
		}
		
		Database db;
		int count = 0;
	}
	
	private void internalTestVisitAllRecords(Database db) throws Exception
	{
		PacketCounter counter = new PacketCounter(db);
		db.visitAllRecords(counter);
		assertEquals(db.toString()+"not empty?", 0, counter.count);
		
		db.writeRecord(smallKey, smallString);
		db.writeRecord(largeKey, largeString);
		db.visitAllRecords(counter);
		assertEquals(db.toString()+"count wrong?", 2, counter.count);
	}

	private void internalTestDeleteAllData(Database db) throws Exception
	{
		db.writeRecord(smallKey, smallString);
		assertNotNull(db.toString()+"didn't write", db.readRecord(smallKey, security));
		db.deleteAllData();
		assertNull(db.toString()+"didn't delete all", db.readRecord(smallKey, security));
	}

	private void internalTestSmallWriteRecordFromStream(Database db) throws Exception
	{
		byte[] bytes = smallString.getBytes();
		InputStream stream = new ByteArrayInputStream(bytes);
		try 
		{
			db.writeRecord(null, stream);
			fail(db.toString()+"should have thrown for null key");
		} 
		catch(IOException ignoreExpectedException) 
		{
		}

		try 
		{
			db.writeRecord(smallKey, (InputStream)null);
			fail(db.toString()+"should have thrown for null input stream");
		} 
		catch(IOException ignoreExpectedException) 
		{
		}

		db.writeRecord(smallKey, stream);
		String gotBack = db.readRecord(smallKey, security);
		assertNotNull(db.toString()+"read failed", gotBack);
		assertEquals(db.toString()+"wrong data?", smallString, gotBack);
	}

	private void internalTestLargeWriteRecordFromStream(Database db) throws Exception
	{
		InputStream stream = new ByteArrayInputStream(largeBytes);
		db.writeRecord(largeKey, stream);
		String gotBackLarge = db.readRecord(largeKey, security);
		assertNotNull(db.toString()+"read failed", gotBackLarge);
		assertEquals(db.toString()+"wrong data?", largeString, gotBackLarge);
	}

	private void internalTestBadStream(Database db)
	{
		class BadInputStream extends InputStream
		{
			public int available() throws IOException
				{ throw(new IOException("Fake error")); }
			public void close() throws IOException
				{ throw(new IOException("Fake error")); }
			public void mark(int limit)
				{  }
			public boolean markSupported()
				{ return false; }
			public int read() throws IOException
				{ throw(new IOException("Fake error")); }
			public int read(byte[] b) throws IOException
				{ throw(new IOException("Fake error")); }
			public int read(byte[] b, int offset, int len) throws IOException
				{ throw(new IOException("Fake error")); }
			public long skip(long n) throws IOException
				{ throw(new IOException("Fake error")); }
		}
		InputStream badStream = new BadInputStream();
		try
		{
			db.writeRecord(smallKey, badStream);
			fail(db.toString()+"should have thrown");
		}
		catch(IOException ignoreExpectedException)
		{
		}
		//TODO deside whether to try to recover an old record after a failed write.
		//assertEquals("kept partial", null, db.readRecord(smallKey));
		//db.writeRecord(smallKey, smallString);
		//assertEquals("write bad stream2", false, db.writeRecord(smallKey, badStream));
		//assertEquals("discarded old", smallString, db.readRecord(smallKey));
	}

	private void internalTestGetIncomingInterimFile(Database db) throws Exception
	{
		File interim = db.getIncomingInterimFile(smallKey);
		assertNotNull(db.toString()+"file is null?", interim);
		assertEquals(db.toString()+"interim file exists?", false, interim.exists());
		UnicodeWriter writer = new UnicodeWriter(interim);
		writer.write("hello");
		writer.close();
		long fileSize = interim.length();
		assertNotEquals(db.toString()+"Zero length?", 0, fileSize);
		
		File interimSame = db.getIncomingInterimFile(smallKey);
		assertEquals(db.toString()+"Not the same file?", interim, interimSame);
		assertEquals(db.toString()+"interimSame size not the same?", fileSize, interimSame.length());
	}
	
	private void internalTestGetOutgoingInterimFile(Database db) throws Exception
	{
		File interim = db.getOutgoingInterimFile(smallKey);
		assertNotNull(db.toString()+"file is null?", interim);
		assertEquals(db.toString()+"interim file exists?", false, interim.exists());
		UnicodeWriter writer = new UnicodeWriter(interim);
		writer.write("hello");
		writer.close();
		long fileSize = interim.length();
		assertNotEquals(db.toString()+"Zero length?", 0, fileSize);
		
		File interimSame = db.getOutgoingInterimFile(smallKey);
		assertEquals(db.toString()+"Not the same file?", interim, interimSame);
		assertEquals(db.toString()+"interimSame size not the same?", fileSize, interimSame.length());
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
	}


	static String buildLargeString()
	{
		String result = "";
		for(int i = 0; i < 200; ++i)
			result += "The length of this string must not эт divide into blocksize!!!";
		return result;
	}

	MockMartusSecurity security;
	DatabaseKey smallKey = new DatabaseKey(UniversalId.createFromAccountAndPrefix("small account", "x"));
	DatabaseKey largeKey = new DatabaseKey(UniversalId.createFromAccountAndPrefix("large account", "x"));
	String smallString = "How are you doing?";
	String smallString2 = "Just another string 123";
	String largeString = buildLargeString();
	byte[] largeBytes;
	File goodDir1;
	File goodDir2;
	MockDatabase mockDb;
	FileDatabase clientFileDb;
	ServerFileDatabase serverFileDb;
}
