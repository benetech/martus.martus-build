package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;



public class TestFileDatabase extends TestCaseEnhanced
{
	public TestFileDatabase(String name) throws Exception
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		dir = File.createTempFile("$$$MartusTestFileDatabaseSetup", null);
		dir.delete();
		dir.mkdir();
		db = new FileDatabase(dir);
		security = new MockMartusSecurity();
	}
	
	public void tearDown()
	{
		db.deleteAllData();
		assertTrue("Either a test failed or a file was left open.", dir.delete());
	}
	
	public void testBasics() throws Exception
	{
		assertEquals("count not 0?", 0, getRecordCount());
	}

/*
 * This test was an attempt to figure out why the sync() call was working in the 
 * unit tests, but failing in the app itself (under Win2K, anyway). Unfortunately, 
 * the test passed, so it didn't really tell us anything. At some point, when the 
 * sync() stuff has been fixed, this should be deleted. kbs. 2002-09-03
	public void testStupidSyncProblem() throws Exception
	{
		File dir = new File("c:/martus/packets/abed/a0000000/pb00");
		dir.deleteOnExit();
		dir.mkdirs();
		File file = new File(dir, "$$$MartusTest.tmp");
		file.deleteOnExit();
		
		FileOutputStream rawOut = new FileOutputStream(file);
		BufferedOutputStream out = (new BufferedOutputStream(rawOut));
		for(int i=0;i<800;++i)
			out.write(0);
		out.flush();
		rawOut.flush();
		rawOut.getFD().sync();
		out.close();
		file.delete();
	}
*/
	
	public void testConstructorWhenNoMapExists() throws Exception
	{
		db.getAccountDirectory("some stupid account");
		db.accountMapFile.delete();
		try
		{
			new FileDatabase(dir);
			fail("Should have thrown");
		}
		catch(FileDatabase.MissingAccountMapException ignoreExpectedException)
		{
		}
	}

	public void testWriteAndReadStrings() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);
		assertEquals("count not one?", 1, getRecordCount());
		
		assertEquals("read1", sampleString1, db.readRecord(shortKey, security));

		db.writeRecord(shortKey, sampleString2);
		assertEquals("count not still one?", 1, getRecordCount());

		assertEquals("read2", sampleString2, db.readRecord(shortKey, security));
	}
	
	public void testWriteAndReadStreams() throws Exception
	{
		ByteArrayInputStream streamToWrite1 = new ByteArrayInputStream(sampleBytes1);
		db.writeRecord(shortKey, streamToWrite1);
		streamToWrite1.close();
		assertEquals("count not one?", 1, getRecordCount());
		
		InputStream in1 = db.openInputStream(shortKey, security);
		assertNotNull("null stream?", in1);
		byte[] bytes1 = new byte[in1.available()];
		in1.read(bytes1);
		in1.close();
		assertTrue("wrong bytes?", Arrays.equals(sampleBytes1, bytes1));

		ByteArrayInputStream streamToWrite2 = new ByteArrayInputStream(sampleBytes2);
		db.writeRecord(shortKey, streamToWrite2);
		streamToWrite2.close();
		assertEquals("count not still one?", 1, getRecordCount());

		InputStream in2 = db.openInputStream(shortKey, security);
		assertNotNull("null stream?", in2);
		byte[] bytes2 = new byte[in2.available()];
		in2.read(bytes2);
		in2.close();
		assertTrue("wrong bytes?", Arrays.equals(sampleBytes2, bytes2));
	}
	
	public void testReadEncryptedStream() throws Exception
	{
		db.writeRecordEncrypted(shortKey, sampleString1, security);
		InputStream in1 = db.openInputStream(shortKey, security);
		assertNotNull("null stream?", in1);
		byte[] bytes1 = new byte[in1.available()];
		in1.read(bytes1);
		in1.close();
		String got = new String(bytes1, "UTF-8");
		assertEquals("wrong data?", sampleString1, got);
	}
	
	public void testDiscardRecord() throws Exception
	{
		assertEquals("count not 0?", 0, getRecordCount());
		assertEquals("already exists?", false, db.doesRecordExist(shortKey));

		db.writeRecord(shortKey, sampleString1);
		assertEquals("count not one?", 1, getRecordCount());
		assertEquals("wasn't created?", true, db.doesRecordExist(shortKey));
		
		db.discardRecord(shortKey);
		assertEquals("count not back to 0?", 0, getRecordCount());
		assertEquals("wasn't discarded?", false, db.doesRecordExist(shortKey));
	}
	
	public void testDeleteAllData() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);
		db.deleteAllData();
		assertEquals("count not 0?", 0, getRecordCount());
		assertEquals("not zero files?", 0, db.absoluteBaseDir.list().length);
	}
	
	public void testInterimFileNames() throws Exception
	{
		File interimIn = db.getIncomingInterimFile(shortKey);
		assertEndsWith(".in", interimIn.getName());
		File interimOut = db.getOutgoingInterimFile(shortKey);
		assertEndsWith(".out", interimOut.getName());
	}
	
	public void testPersistence() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);
		db.writeRecord(shortKey, sampleString1);
		ByteArrayInputStream streamToWrite1 = new ByteArrayInputStream(sampleBytes1);
		db.writeRecord(otherKey, streamToWrite1);
		streamToWrite1.close();
		assertEquals("count not two?", 2, getRecordCount());
		db.discardRecord(otherKey);

		db = new FileDatabase(dir);
		assertEquals("count not back to one?", 1, getRecordCount());
		
		assertTrue("missing short?", db.doesRecordExist(shortKey));
	}
	
	public void testWriteAndReadRecordEncrypted() throws Exception
	{
		try 
		{
			db.writeRecordEncrypted(null, sampleString1, security);
			fail("should have thrown for null key");
		} 
		catch(IOException ignoreExpectedException) 
		{
		}
		
		try 
		{
			db.writeRecordEncrypted(shortKey, null, security);
			fail("should have thrown for null string");
		} 
		catch(IOException ignoreExpectedException) 
		{
		}
		
		try 
		{
			db.writeRecordEncrypted(shortKey, sampleString1, null);
			fail("should have thrown for null crypto");
		} 
		catch(IOException ignoreExpectedException) 
		{
		}
		
		db.writeRecordEncrypted(shortKey, sampleString1, security);
		File file = db.getFileForRecord(shortKey);

		InputStream in1 = new FileInputStream(file);
		assertNotNull("null stream?", in1);
		byte[] bytes1 = new byte[in1.available()];
		in1.read(bytes1);
		in1.close();
		assertEquals("Not Encrypted?", false, Arrays.equals(sampleString1.getBytes(), bytes1));
		
		String result = db.readRecord(shortKey, security);
		assertEquals("got wrong data?", sampleString1, result);
	}
	
	public void testSupportMarkReset() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);
		InputStream in = db.openInputStream(shortKey, security);
		assertTrue("Mark Not Supported?", in.markSupported());
		in.mark(9999999);
		in.read();
		in.reset();
		in.close();
	}
	
	public void testHashFunction()
	{
		String s1 = "abcdefg";
		String s2 = "bcdefga";
		int hash1 = FileDatabase.getHashValue(s1);
		int hash2 = FileDatabase.getHashValue(s2);
		int hash3 = FileDatabase.getHashValue(s1);
		assertNotEquals("same?", new Integer(hash1), new Integer(hash2));
		assertEquals("not same?", new Integer(hash1), new Integer(hash3));
		
		Random rand = new Random(12345);
		StringBuffer buffer = new StringBuffer();
		for(int x = 0; x < 24; ++x)
		{
			char newChar = (char)('A' + rand.nextInt(26));
			buffer.append(newChar);
		}
		int[] count = new int[256];
		for(int i = 0; i < 25600; ++i)
		{
			int changeAt = rand.nextInt(buffer.length());
			char newChar = (char)('A' + rand.nextInt(26));
			buffer.setCharAt(changeAt, newChar);
			int hash = FileDatabase.getHashValue(new String(buffer));
			++count[hash&0xFF];
		}
		for(int bucket = 0; bucket < count.length; ++bucket)
		{
			assertTrue("too many in bucket?", count[bucket] < 250);
		}
		
	}
	
	public void testGetFileForRecord() throws Exception
	{
		File file = db.getFileForRecord(shortKey);
		assertEquals("filename", shortKey.getLocalId(), file.getName());
		String path = file.getPath();
		assertStartsWith("no dir?", dir.getPath(), path);
		
		int hash = FileDatabase.getHashValue(shortKey.getLocalId()) & 0xFF;
		String hashString = Integer.toHexString(hash + 0xb00);
		assertContains("no hash stuff?", "p" + hashString, path);

	}
	
	public void testGetAccountDirectory() throws Exception
	{
		Vector accountDirs = new Vector();
		
		String baseDir = dir.getPath().replace('\\', '/');
		for(int i = 0; i < 20; ++i)
		{
			String a1 = "account" + i;
			UniversalId uid1 = UniversalId.createFromAccountAndPrefix(a1, "x");
			DatabaseKey key1 = new DatabaseKey(uid1);
			int accountHash1 = FileDatabase.getHashValue(uid1.getAccountId()) & 0xFF;
			String accountHashString1 = Integer.toHexString(accountHash1 + 0xb00);
			String expectedAccountBucket = baseDir + "/a" + accountHashString1;
			String gotDir1 = db.getAccountDirectory(key1.getAccountId()).getPath().replace('\\', '/');
			assertContains("wrong base?", baseDir, gotDir1);
			assertContains("wrong full path?", expectedAccountBucket, gotDir1);
			
			String gotDirString = db.getFolderForAccount(key1.getAccountId());
			assertStartsWith("bad folder?", "a" + accountHashString1 + File.separator, gotDirString);
			
			assertEquals("bad reverse lookup?", key1.getAccountId(), db.getAccountString(new File(gotDir1)));
			assertNotContains("already used this accountdir?", gotDir1, accountDirs);
			accountDirs.add(gotDir1);
		}
	}
	
	public void testGetFolderForAccountUpdateAccountMapWhenNeeded() throws Exception
	{
		db.deleteAllData();
		File mapFile = db.accountMapFile;
		
		assertEquals("account file already exists?", 0, mapFile.length());

		String accountId = "some silly account";
		db.getFolderForAccount(accountId);
		assertNotEquals("account file not updated?", 0, mapFile.length());
		long lastLength = mapFile.length();
		long lastModified = mapFile.lastModified();
		Thread.sleep(2000);

		db.getFolderForAccount(accountId);
		assertEquals("account file grew?", lastLength, mapFile.length());
		assertEquals("account file touched?", lastModified, mapFile.lastModified());

		String accountId2 = "another silly account";
		db.getFolderForAccount(accountId2);
		assertNotEquals("account file not updated again?", lastLength, mapFile.length());
		assertNotEquals("account file not touched again?", lastModified, mapFile.lastModified());
	}
	
	public void testAddParsedAccountEntry()
	{
		HashMap map = new HashMap();
		String dirSeparator = File.separator;
		String relativeDir1 = "bucket1"+ dirSeparator +"a1";
		String absoluteDir2 = "C:" + dirSeparator + "Martus" + dirSeparator;
		String relativeDir2 = "bucket2"+ dirSeparator +"a1";
		String absoluteDir3 = dirSeparator + "home" + dirSeparator;
		String relativeDir3 = "bucket3"+ dirSeparator +"a1";
		String account1 = "account1";
		String account2 = "account2";
		String account3 = "account3";
		db.addParsedAccountEntry(map,relativeDir1+"="+account1);
		assertEquals("relative Failed to be added to map?", relativeDir1, map.get(account1));
		db.addParsedAccountEntry(map,absoluteDir2+relativeDir2+"="+account2);
		assertEquals("absoluteDir Failed to be added to map?", relativeDir2, map.get(account2));
		db.addParsedAccountEntry(map,absoluteDir3+relativeDir3+"="+account3);
		assertEquals("absoluteDir2 Failed to be added to map?", relativeDir3, map.get(account3));
	}
	
	public void testWriteUpdatesAccountMapWhenNeeded() throws Exception
	{
		db.deleteAllData();
		File mapFile = db.accountMapFile;
		String accountId = "accountForTesting";
		UniversalId uid = UniversalId.createFromAccountAndPrefix(accountId, "x");
		DatabaseKey key = new DatabaseKey(uid);

		db.writeRecord(key, "Some record text");
		long lastLength = mapFile.length();
		long lastModified = mapFile.lastModified();
		Thread.sleep(2000);

		db.writeRecord(key, "Other record text");
		assertEquals("account file grew?", lastLength, mapFile.length());
		assertEquals("account file touched?", lastModified, mapFile.lastModified());
	}
	
	public void testVisitAllAccounts() throws Exception
	{
		class AccountCollector implements Database.AccountVisitor
		{
			public void visit(String accountString, File accountDir)
			{
				list.add(accountString);
			}
			Vector list = new Vector();
		}
		
		db.writeRecord(shortKey, sampleString1);
		db.writeRecord(otherKey, sampleString2);
		
		AccountCollector ac = new AccountCollector();
		db.visitAllAccounts(ac);
		assertEquals("count?", 2, ac.list.size());
		assertContains("missing 1?", shortKey.getAccountId(), ac.list);
		assertContains("missing 2?", otherKey.getAccountId(), ac.list);
		
	}
	
	public void testVisitAllPacketsForAccount() throws Exception
	{
		class PacketCollector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				list.add(key);
			}
			Vector list = new Vector();
		}
		
		db.writeRecord(shortKey, sampleString1);
		db.writeRecord(shortKey2, sampleString2);
		
		PacketCollector ac = new PacketCollector();
		db.visitAllPacketsForAccount(ac, accountString1);
		assertEquals("count?", 2, ac.list.size());
		assertContains("missing 1?", shortKey, ac.list);
		assertContains("missing 2?", shortKey2, ac.list);
		
	}
	
	int getRecordCount()
	{
		class PacketCounter implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				++count;
			}
			
			int count;
		}
		
		PacketCounter counter = new PacketCounter();
		db.visitAllRecords(counter);
		return counter.count;
	}



	MockMartusSecurity security;
	FileDatabase db;
	File dir;
	String accountString1 = "acct1";
	DatabaseKey shortKey = new DatabaseKey(UniversalId.createFromAccountAndPrefix(accountString1 , "x"));
	DatabaseKey shortKey2 = new DatabaseKey(UniversalId.createFromAccountAndPrefix(accountString1 , "x"));
	DatabaseKey otherKey = new DatabaseKey(UniversalId.createFromAccountAndPrefix("acct2", "x"));
	String sampleString1 = "This is just a little bit of data as a sample";
	String sampleString2 = "Here is a somewhat different sample string";
	byte[] sampleBytes1 = {127,44,17,0,27,99};
	byte[] sampleBytes2 = {88,0,127,56,21,101};
}
