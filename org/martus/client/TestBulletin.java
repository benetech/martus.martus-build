package org.martus.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.client.Bulletin.DamagedBulletinException;
import org.martus.common.AttachmentPacket;
import org.martus.common.AttachmentProxy;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockDatabase;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;
import org.martus.common.ZipEntryInputStream;


public class TestBulletin extends TestCaseEnhanced
{
    public TestBulletin(String name) throws Exception
	{
        super(name);
    }

    public void setUp() throws Exception
    {
    	if(tempFile1 == null)
    	{
			tempFile1 = createSampleFile(sampleBytes1);
			tempFile2 = createSampleFile(sampleBytes2);
			tempFile3 = createSampleFile(sampleBytes3);
			tempFile4 = createSampleFile(sampleBytes4);
			tempFile5 = createSampleFile(sampleBytes5);
			tempFile6 = createSampleFile(sampleBytes6);
    	}
		proxy1 = new AttachmentProxy(tempFile1);
		proxy2 = new AttachmentProxy(tempFile2);
		proxy3 = new AttachmentProxy(tempFile3);
		proxy4 = new AttachmentProxy(tempFile4);
		proxy5 = new AttachmentProxy(tempFile5);
		proxy6 = new AttachmentProxy(tempFile6);

		if(security == null)
		{
			security = new MartusSecurity();
			security.createKeyPair(512);
		}
		app = MockMartusApp.create(security);
		if(store == null)
		{
			db = new MockClientDatabase();
			store = new BulletinStore(db);
			store.setSignatureGenerator(app.getSecurity());
			app.store = store;
		}
		store.deleteAllData();
    }
    
    public void tearDown() throws Exception
    {
    	app.deleteAllFiles();
    }

	private File createSampleFile(byte[] data) throws Exception
	{
		File tempFile = File.createTempFile("$$$MartusTest", null);
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(data);
		out.close();
		return tempFile;
	}

    public void testBasics()
    {
		Bulletin b = new Bulletin((BulletinStore)null);
		assertEquals(false, b.isStandardField("Nope"));
		assertEquals(true, b.isStandardField("Location"));
		assertEquals(true, b.isStandardField("location"));
		assertEquals(true, b.isStandardField("LOCATION"));
		assertEquals(false, b.isStandardField(b.TAGPRIVATEINFO));
		
		assertEquals(false, b.isPrivateField("LOCATION"));
		assertEquals(true, b.isPrivateField(b.TAGPRIVATEINFO));

		b = store.createEmptyBulletin();
		assertNotEquals("", b.getLocalId());

		assertEquals(store, b.getStore());
		
		assertEquals("not already all private?", true, b.isAllPrivate());
		b.setAllPrivate(false);
		assertEquals("still all private?", false, b.isAllPrivate());
		b.setAllPrivate(true);
		assertEquals("not all private?", true, b.isAllPrivate());
		
		BulletinHeaderPacket header = b.getBulletinHeaderPacket();
		assertNotNull("No header?", header);
		FieldDataPacket data = b.getFieldDataPacket();
		assertNotNull("No data packet?", data);
		assertEquals("data id", header.getFieldDataPacketId(), data.getLocalId());
		FieldDataPacket privateData = b.getPrivateFieldDataPacket();
		assertNotNull("No private data packet?", privateData);
		assertEquals("private data id", header.getPrivateFieldDataPacketId(), privateData.getLocalId());
		assertEquals("not really private?", true, privateData.isEncrypted());
		
		assertEquals("account not initialized correctly?", store.getAccountId(), b.getAccount());
		assertEquals("field data account?", store.getAccountId(), b.getFieldDataPacket().getAccountId());
		
	}
	
	public void testId()
	{
		Bulletin b = new Bulletin((BulletinStore)null);
		assertNotNull("Id was Null?", b.getLocalId());		
		assertEquals("Id was empty?", false, b.getLocalId().length()==0);
	}

	public void testStatus()
	{
		Bulletin b = store.createEmptyBulletin();
		assertEquals(Bulletin.STATUSDRAFT, b.getStatus());
		assertEquals("Should start as draft", true, b.isDraft());
		b.setDraft();
		assertEquals(Bulletin.STATUSDRAFT, b.getStatus());
		assertEquals("Should be draft", true, b.isDraft());
		assertEquals("Not yet sealed", false, b.isSealed());
		b.setSealed();
		assertEquals(Bulletin.STATUSSEALED, b.getStatus());
		assertEquals("No longer draft", false, b.isDraft());
		assertEquals("Now sealed", true, b.isSealed());
	}

	public void testEmpty()
	{
		Bulletin b = store.createEmptyBulletin();
		String today = Bulletin.getToday();
		assertEquals(today, b.get("entrydate"));

		GregorianCalendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.MONTH, 0);
		cal.set(GregorianCalendar.DATE, 1);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String result = df.format(cal.getTime());
		assertEquals(result, b.get("eventdate"));

		assertEquals(Bulletin.STATUSDRAFT, b.getStatus());
	}

	public void testGetSet()
	{

		Bulletin b = store.createEmptyBulletin();
		assertEquals("", b.get("NoSuchField"));
		b.set("NoSuchField", "hello");
		assertEquals("", b.get("NoSuchField"));

		assertEquals("", b.get("Author"));
		b.set("Author", "hello");
		assertEquals("hello", b.get("author"));
		assertEquals("hello", b.get("Author"));
		assertEquals("hello", b.get("AUTHOR"));

		b.set("Location", "94404");
		assertEquals("94404", b.get("Location"));
		b.set("author", "goodbye");
		assertEquals("goodbye", b.get("AUTHOR"));
		
		b.set(b.TAGPRIVATEINFO, "secret");
		assertEquals("secret", b.get(b.TAGPRIVATEINFO));
	}

	public void testClear()
	{
		String publicInfo = "public info";
		String privateInfo = "private info";
		
		Bulletin b = store.createEmptyBulletin();
		b.set(b.TAGPUBLICINFO, publicInfo);
		b.set(b.TAGPRIVATEINFO, privateInfo);
		assertEquals("public info not set?", publicInfo, b.get(b.TAGPUBLICINFO));
		assertEquals("private info not set?", privateInfo, b.get(b.TAGPRIVATEINFO));
		b.clear();
		assertEquals("public info not cleared?", "", b.get(b.TAGPUBLICINFO));
		assertEquals("private info not cleared?", "", b.get(b.TAGPRIVATEINFO));
	}

	public void testMatches()
	{
		Bulletin b = store.createEmptyBulletin();
		b.set("author", "hello");
		b.set("summary", "summary");
		b.set("title", "Josée");
		b.save();

		assertEquals("hello", true, b.matches(new SearchTreeNode("hello")));
		// field names should not be searched
		assertEquals("author", false, b.matches(new SearchTreeNode("author")));
		// id should not be searched
		assertEquals("getLocalId()", false, b.matches(new SearchTreeNode(b.getLocalId())));

		assertEquals("HELLO", true, b.matches(new SearchTreeNode("HELLO")));
		assertEquals("Josée", true, b.matches(new SearchTreeNode("Josée")));
		assertEquals("josée", true, b.matches(new SearchTreeNode("josée")));
		assertEquals("josÉe", true, b.matches(new SearchTreeNode("josÉe")));
		assertEquals("josee", false, b.matches(new SearchTreeNode("josee")));

		SearchParser parser = new SearchParser(app);
		assertEquals("right false and", false, b.matches(parser.parse("hello and goodbye")));
		assertEquals("left false and", false, b.matches(parser.parse("goodbye and hello")));
		assertEquals("true and", true, b.matches(parser.parse("Hello and Summary")));

		assertEquals("false or", false, b.matches(parser.parse("swinging and swaying")));
		assertEquals("left true or", true, b.matches(parser.parse("hello or goodbye")));
		assertEquals("right true or", true, b.matches(parser.parse("goodbye or hello")));
		assertEquals("both true or", true, b.matches(parser.parse("hello or summary")));
	}

	public void testGetStandardFieldNames()
	{
		List names = Arrays.asList(Bulletin.getStandardFieldNames());
		assertEquals(true, names.contains("author"));
		assertEquals(false, names.contains("privateinfo"));
		assertEquals(false, names.contains("nope"));
		assertEquals(true, names.contains("language"));
		assertEquals(true, names.contains("organization"));
		
		List privateNames = Arrays.asList(Bulletin.getPrivateFieldNames());
		assertEquals(true, privateNames.contains("privateinfo"));
		assertEquals(false, privateNames.contains("nope"));
	}

	public void testGetFieldType()
	{
		assertEquals(Bulletin.NORMAL, Bulletin.getFieldType("author"));
		assertEquals(Bulletin.MULTILINE, Bulletin.getFieldType("summary"));
		assertEquals(Bulletin.MULTILINE, Bulletin.getFieldType("publicinfo"));
		assertEquals(Bulletin.MULTILINE, Bulletin.getFieldType("privateinfo"));
		assertEquals(Bulletin.DATE, Bulletin.getFieldType("eventdate"));
		assertEquals(Bulletin.DATE, Bulletin.getFieldType("entrydate"));
		assertEquals(Bulletin.CHOICE, Bulletin.getFieldType("language"));

	}
	
	public void testEncryptPublicData()
	{

		class MyMockDatabase extends MockClientDatabase
		{
			public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter)
			{
				++encryptWasCalled;
			}
			public int encryptWasCalled;
		}
		
		MyMockDatabase db = new MyMockDatabase();
		BulletinStore store = new BulletinStore(db);
		store.setSignatureGenerator(security);
		Bulletin b = store.createEmptyBulletin();
		b.setSealed();
		b.setAllPrivate(false);
		assertEquals("Ecrypted?", false, b.mustEncryptPublicData());
		store.setEncryptPublicData(true);
		assertEquals("Not Ecrypted?", true, b.mustEncryptPublicData());
		b.save();
		assertEquals("Didn't Encrypt or Encyrpted too many packets.", 1, db.encryptWasCalled);
	}

	public void testIsFieldEncrypted()
	{
		assertEquals(false, Bulletin.isFieldEncrypted("author"));
		assertEquals(true, Bulletin.isFieldEncrypted("privateinfo"));
	}

	public void testSave()
	{
		int oldCount = store.getBulletinCount();
		Bulletin b = store.createEmptyBulletin();
		b.set("author", "testsave");
		b.save();
		assertEquals(oldCount+1, store.getBulletinCount());
		b = store.findBulletinByUniversalId(b.getUniversalId());
		assertEquals("testsave", b.get("author"));
		boolean empty = (b.getLocalId().length() == 0);
		assertEquals("Saved ID must be non-empty\n", false, empty);

		Bulletin b2 = store.createEmptyBulletin();
		b2.save();
		assertNotEquals("Saved ID must be unique\n", b.getLocalId(), b2.getLocalId());
		
		b2.save();
	}
	
	public void testLastSavedTime() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		long createdTime = b.getLastSavedTime();
		assertEquals("time already set?", BulletinHeaderPacket.TIME_UNKNOWN, createdTime);
		
		Thread.sleep(200);
		b.save();
		long firstSavedTime = b.getLastSavedTime();
		assertNotEquals("Didn't update time saved?", createdTime, firstSavedTime);
		long delta2 = Math.abs(firstSavedTime - System.currentTimeMillis());
		assertTrue("time wrong?", delta2 < 1000);
		
		Thread.sleep(200);
		Bulletin b2 = Bulletin.loadFromDatabase(store, new DatabaseKey(b.getUniversalId()));
		long loadedTime = b2.getLastSavedTime();
		assertEquals("Didn't keep time saved?", firstSavedTime, loadedTime);
	}
	
	public void testRemoveBulletinFromDatabase() throws Exception
	{
		MockDatabase db = new MockClientDatabase();
		assertEquals(0, db.getAllKeys().size());
		Bulletin b = store.createEmptyBulletin();
		b.addPublicAttachment(new AttachmentProxy(tempFile1));
		b.set(b.TAGPUBLICINFO, "public info");
		b.set(b.TAGPRIVATEINFO, "private info");
		b.setSealed();
		b.save();
		b.saveToDatabase(db);
		assertTrue("didn't write?", db.getAllKeys().size() > 0);
		
		b.removeBulletinFromDatabase(db, security);
		assertEquals(0, db.getAllKeys().size());
	}

	public void testSaveToDatabase() throws Exception
	{
		assertEquals(0, db.getAllKeys().size());

		Bulletin b = store.createEmptyBulletin();
		b.set("summary", "New bulletin");
		b.saveToDatabase(db);
		DatabaseKey headerKey1 = new DatabaseKey(b.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey1 = new DatabaseKey(b.getFieldDataPacket().getUniversalId());
		assertEquals("saved 1", 3, db.getAllKeys().size());
		assertEquals("saved 1 header key", true,db.doesRecordExist(headerKey1));
		assertEquals("saved 1 data key", true,db.doesRecordExist(dataKey1));

		// re-saving the same bulletin replaces the old one
		b.saveToDatabase(db);
		assertEquals("resaved 1", 3, db.getAllKeys().size());
		assertEquals("resaved 1 header key", true,db.doesRecordExist(headerKey1));
		assertEquals("resaved 1 data key", true,db.doesRecordExist(dataKey1));

		// saving a bulletin with the same id replaces the old one
		Bulletin b2 = new Bulletin(b);
		b2.set("summary", "Replaced bulletin");
		b2.saveToDatabase(db);
		assertEquals("resaved 2", 3, db.getAllKeys().size());
		DatabaseKey headerKey2 = new DatabaseKey(b2.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey2 = new DatabaseKey(b2.getFieldDataPacket().getUniversalId());
		assertEquals("resaved 2 header key", true,db.doesRecordExist(headerKey2));
		assertEquals("resaved 2 data key", true,db.doesRecordExist(dataKey2));
		
		Bulletin b3 = Bulletin.loadFromDatabase(store, headerKey2);
 		assertEquals("id", b2.getLocalId(), b3.getLocalId());
		assertEquals("summary", b2.get("summary"), b3.get("summary"));
		
		// unsaved bulletin changes should not be in the store
		b.set("summary", "not saved yet");
		Bulletin b4 = Bulletin.loadFromDatabase(store, headerKey2);
		assertEquals("id", b2.getLocalId(), b4.getLocalId());
		assertEquals("summary", b2.get("summary"), b4.get("summary"));

		// saving a new bulletin with a non-empty id should retain that id
		b = store.createEmptyBulletin();
		b.saveToDatabase(db);
		assertEquals("saved another", 6, db.getAllKeys().size());
		assertEquals("old header key", true, db.doesRecordExist(headerKey2));
		assertEquals("old data key", true, db.doesRecordExist(dataKey2));
		DatabaseKey newHeaderKey = new DatabaseKey(b.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey newDataKey = new DatabaseKey(b.getFieldDataPacket().getUniversalId());
		assertEquals("new header key", true, db.doesRecordExist(newHeaderKey));
		assertEquals("new data key", true, db.doesRecordExist(newDataKey));
	}
	
	public void testSaveToDatabaseWithPendingAttachment() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		b.addPublicAttachment(a);
		String[] attachmentIds = b.getBulletinHeaderPacket().getPublicAttachmentIds();
		assertEquals("one attachment", 1, attachmentIds.length);
		b.saveToDatabase(db);
		assertEquals("saved", 4, db.getAllKeys().size());

		Bulletin got = Bulletin.loadFromDatabase(store, new DatabaseKey(b.getUniversalId()));
		assertEquals("id", b.getLocalId(), got.getLocalId());
		assertEquals("attachment count", b.getPublicAttachments().length, got.getPublicAttachments().length);
	}
	
	public void testSaveToDatabaseWithAttachment() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		b.saveToDatabase(db);
		assertEquals("saved", 5, db.getAllKeys().size());

		Bulletin got1 = Bulletin.loadFromDatabase(store, key);
		verifyLoadedBulletin("First load", b, got1);
	}
	
	public void testSaveToDatabaseAllPrivate() throws Exception
	{
		Bulletin somePublicDraft = store.createEmptyBulletin();
		somePublicDraft.setAllPrivate(false);
		somePublicDraft.setDraft();
		somePublicDraft.saveToDatabase(db);
		assertEquals("public draft was not encrypted?", true, somePublicDraft.getFieldDataPacket().isEncrypted());
		
		Bulletin allPrivateDraft = store.createEmptyBulletin();
		allPrivateDraft.setAllPrivate(true);
		allPrivateDraft.setDraft();
		allPrivateDraft.saveToDatabase(db);
		assertEquals("private draft was not encrypted?", true, allPrivateDraft.getFieldDataPacket().isEncrypted());
		
		Bulletin somePublicSealed = store.createEmptyBulletin();
		somePublicSealed.setAllPrivate(false);
		somePublicSealed.setSealed();
		somePublicSealed.saveToDatabase(db);
		assertEquals("public sealed was encrypted?", false, somePublicSealed.getFieldDataPacket().isEncrypted());

		Bulletin allPrivateSealed = store.createEmptyBulletin();
		allPrivateSealed.setAllPrivate(true);
		allPrivateSealed.setSealed();
		allPrivateSealed.saveToDatabase(db);
		assertEquals("private sealed was encrypted?", true, allPrivateSealed.getFieldDataPacket().isEncrypted());
	}
	
	
	public void testReSaveToDatabaseWithAttachments() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		b.saveToDatabase(db);
		assertEquals("saved", 5, db.getAllKeys().size());
		Bulletin got1 = Bulletin.loadFromDatabase(store, key);
		got1.saveToDatabase(db);
		assertEquals("resaved", 5, db.getAllKeys().size());
		
		Bulletin got2 = Bulletin.loadFromDatabase(store, key);
		verifyLoadedBulletin("Reload after save", got1, got2);
	}
	
	public void testReSaveToDatabaseAddAttachments() throws Exception
	{		
		Bulletin b = store.createEmptyBulletin();
		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		b.addPrivateAttachment(proxy4);
		b.addPrivateAttachment(proxy5);
		b.saveToDatabase(db);
		Bulletin got1 = Bulletin.loadFromDatabase(store, key);

		got1.clear();
		got1.addPublicAttachment(proxy1);
		got1.addPublicAttachment(proxy2);
		got1.addPublicAttachment(proxy3);
		got1.addPrivateAttachment(proxy4);
		got1.addPrivateAttachment(proxy5);
		got1.addPrivateAttachment(proxy6);
		got1.saveToDatabase(db);
		assertEquals("resaved", 9, db.getAllKeys().size());

		Bulletin got3 = Bulletin.loadFromDatabase(store, key);
		verifyLoadedBulletin("Reload after save", got1, got3);

		String[] publicAttachmentIds = got3.getBulletinHeaderPacket().getPublicAttachmentIds();
		assertEquals("wrong public attachment count in bhp?", 3, publicAttachmentIds.length);
		String[] privateAttachmentIds = got3.getBulletinHeaderPacket().getPrivateAttachmentIds();
		assertEquals("wrong private attachment count in bhp?", 3, privateAttachmentIds.length);
	}
	
	public void testReSaveToDatabaseRemoveAttachment() throws Exception
	{		
		Bulletin b = store.createEmptyBulletin();
		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		b.addPrivateAttachment(proxy3);
		b.addPrivateAttachment(proxy4);
		b.saveToDatabase(db);
		assertEquals("saved key count", 7, db.getAllKeys().size());
		Bulletin got1 = Bulletin.loadFromDatabase(store, key);
		AttachmentProxy keep = got1.getPublicAttachments()[1];
		AttachmentProxy keepPrivate = got1.getPrivateAttachments()[1];

		got1.clear();
		got1.addPublicAttachment(keep);
		got1.addPrivateAttachment(keepPrivate);
		got1.saveToDatabase(db);
		assertEquals("resaved modified", 5, db.getAllKeys().size());

		Bulletin got3 = Bulletin.loadFromDatabase(store, key);
		verifyLoadedBulletin("Reload after save", got1, got3);

		String[] publicAttachmentIds = got3.getBulletinHeaderPacket().getPublicAttachmentIds();
		assertEquals("wrong public attachment count in bhp?", 1, publicAttachmentIds.length);
		String[] privateAttachmentIds = got3.getBulletinHeaderPacket().getPrivateAttachmentIds();
		assertEquals("wrong private attachment count in bhp?", 1, privateAttachmentIds.length);
	}

	protected void verifyLoadedBulletin(String tag, Bulletin original, Bulletin got) throws Exception
	{
		assertEquals(tag + " id", original.getUniversalId(), got.getUniversalId());
		AttachmentProxy[] originalAttachments = got.getPublicAttachments();
		assertEquals(tag + " wrong public attachment count?", original.getPublicAttachments().length, originalAttachments.length);
		verifyAttachments(tag + "public", got, originalAttachments);

		AttachmentProxy[] originalPrivateAttachments = got.getPrivateAttachments();
		assertEquals(tag + " wrong private attachment count?", original.getPrivateAttachments().length, originalPrivateAttachments.length);
		verifyAttachments(tag + "private", got, originalPrivateAttachments);
	}

	protected void verifyAttachments(String tag, Bulletin got, AttachmentProxy[] originalAttachments) throws Exception
	{
		for(int i=0; i < originalAttachments.length; ++i)
		{
			AttachmentProxy gotA = originalAttachments[i];
			String localId = gotA.getUniversalId().getLocalId();
			DatabaseKey key1 = new DatabaseKey(gotA.getUniversalId());
			assertEquals(tag + i + " missing original record?", true,  store.getDatabase().doesRecordExist(key1));
		
			File tempFile = File.createTempFile("$$$MartusTestBullSvAtt", null);
			tempFile.deleteOnExit();
			got.extractAttachmentToFile(gotA, security, tempFile);
			FileInputStream in = new FileInputStream(tempFile);
			byte[] gotBytes = new byte[in.available()];
			in.read(gotBytes);
			in.close();
			byte[] expectedBytes = null;
			if(localId.equals(proxy1.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes1;
			else if(localId.equals(proxy2.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes2;
			else if(localId.equals(proxy3.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes3;
			else if(localId.equals(proxy4.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes4;
			else if(localId.equals(proxy5.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes5;
			else if(localId.equals(proxy6.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes6;
				
		
			assertEquals(tag + i + "got wrong data length?", expectedBytes.length, gotBytes.length);
			assertEquals(tag + i + "got bad data?", true, Arrays.equals(gotBytes, expectedBytes));
			tempFile.delete();
		}
	}
	
	public void testSaveToFileWithAttachment() throws Exception
	{
		UniversalId dummyUid = UniversalId.createDummyUniversalId();
		
		Bulletin original = store.createEmptyBulletin();
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.setSealed();
		original.saveToDatabase(db);
		UniversalId uid = original.getUniversalId();

		original = Bulletin.loadFromDatabase(store, new DatabaseKey(uid));
		AttachmentProxy[] originalAttachments = original.getPublicAttachments();
		assertEquals("not one attachment?", 1, originalAttachments.length);
		DatabaseKey key2 = new DatabaseKey(originalAttachments[0].getUniversalId());
		assertEquals("public attachment wasn't saved?", true,  store.getDatabase().doesRecordExist(key2));
	
		AttachmentProxy[] originalPrivateAttachments = original.getPrivateAttachments();
		assertEquals("not one attachment in private?", 1, originalPrivateAttachments.length);
		DatabaseKey keyPrivate = new DatabaseKey(originalPrivateAttachments[0].getUniversalId());
		assertEquals("private attachment wasn't saved?", true,  store.getDatabase().doesRecordExist(keyPrivate));

		File tempFile = File.createTempFile("$$$MartusTestBullSaveFileAtt1", null);
		tempFile.deleteOnExit();
		MockBulletin.saveToFile(original, tempFile);
		assertTrue("unreasonable file size?", tempFile.length() > 20);

		ZipFile zip = new ZipFile(tempFile);
		Enumeration entries = zip.entries();
		
		ZipEntry dataEntry = (ZipEntry)entries.nextElement();
		assertStartsWith("data id wrong?", "F", dataEntry.getName());
		FieldDataPacket fdp = new FieldDataPacket(dummyUid, Bulletin.getStandardFieldNames());
		fdp.loadFromXml(new ZipEntryInputStream(zip, dataEntry), security);
		assertEquals("fdp id?", original.getFieldDataPacket().getUniversalId(), fdp.getUniversalId());
		
		ZipEntry privateEntry = (ZipEntry)entries.nextElement();
		assertStartsWith("private id wrong?", "F", privateEntry.getName());
		FieldDataPacket pdp = new FieldDataPacket(dummyUid, Bulletin.getPrivateFieldNames());
		pdp.loadFromXml(new ZipEntryInputStream(zip, privateEntry), security);
		assertEquals("pdp id?", original.getPrivateFieldDataPacket().getUniversalId(), pdp.getUniversalId());
		
		ZipEntry attachmentEntry = (ZipEntry)entries.nextElement();
		verifyAttachmentInZipFile("public", a, sampleBytes1, zip, attachmentEntry);

		ZipEntry attachmentPrivateEntry = (ZipEntry)entries.nextElement();
		verifyAttachmentInZipFile("private", aPrivate, sampleBytes2, zip, attachmentPrivateEntry);
		
		ZipEntry headerEntry = (ZipEntry)entries.nextElement();
		assertStartsWith("header id wrong?", "B", headerEntry.getName());
		BulletinHeaderPacket bhp = new BulletinHeaderPacket("dummy");
		bhp.loadFromXml(new ZipEntryInputStream(zip, headerEntry), security);
		assertEquals("bhp id?", original.getUniversalId(), bhp.getUniversalId());
		
		assertEquals("too many entries?", false, entries.hasMoreElements());
	}

	public void verifyAttachmentInZipFile(String label, AttachmentProxy a, byte[] bytes, ZipFile zip, ZipEntry attachmentEntry) throws Exception
	{
		assertStartsWith(label + " attachment id wrong?", "A", attachmentEntry.getName());
		ZipEntryInputStream in = new ZipEntryInputStream(zip, attachmentEntry);
		
		File tempRawFile = File.createTempFile("$$$MartusTestBullSaveFileAtt2", null);
		tempRawFile.deleteOnExit();
		AttachmentPacket.exportRawFileFromXml(in, a.getSessionKeyBytes(), security, tempRawFile);
		assertEquals(label + " wrong size2?", bytes.length, tempRawFile.length());
		
		byte[] raw = new byte[bytes.length];
		FileInputStream inRaw = new FileInputStream(tempRawFile);
		assertEquals(label + " read count?", raw.length, inRaw.read(raw));
		inRaw.close();
		assertEquals(label + " wrong bytes?", true, Arrays.equals(bytes, raw));
	}
	
	public void testLoadFromDatabase() throws Exception
	{
		assertEquals(0, db.getAllKeys().size());

		Bulletin b = store.createEmptyBulletin();
		b.set(b.TAGPUBLICINFO, "public info");
		b.set(b.TAGPRIVATEINFO, "private info");
		b.setSealed();
		b.saveToDatabase(db);
		assertEquals("saved 1", 3, db.getAllKeys().size());
		
		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		Bulletin loaded = store.createEmptyBulletin();
		assertEquals("fromdb", false, loaded.isFromDatabase());
		assertNull("db", loaded.getDatabase());
		loaded = Bulletin.loadFromDatabase(store, key);
		assertEquals("id", b.getLocalId(), loaded.getLocalId());
		assertEquals("public info", b.get(b.TAGPUBLICINFO), loaded.get(b.TAGPUBLICINFO));
		assertEquals("private info", b.get(b.TAGPRIVATEINFO), loaded.get(b.TAGPRIVATEINFO));
		assertEquals("fromdb", true, loaded.isFromDatabase());
		assertEquals("db", db, loaded.getDatabase());
		assertEquals("status", b.getStatus(), loaded.getStatus());
	}
	
	public void testLoadFromDatabaseEncrypted() throws Exception
	{
		assertEquals(0, db.getAllKeys().size());

		Bulletin b = store.createEmptyBulletin();
		b.setAllPrivate(true);
		b.saveToDatabase(db);
		assertEquals("saved 1", 3, db.getAllKeys().size());
		
		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		Bulletin loaded = store.createEmptyBulletin();
		assertEquals("fromdb", false, loaded.isFromDatabase());
		assertNull("db", loaded.getDatabase());
		loaded = Bulletin.loadFromDatabase(store, key);
		assertEquals("id", b.getLocalId(), loaded.getLocalId());

		assertEquals("not private?", b.isAllPrivate(), loaded.isAllPrivate());
	}
	
	public void testLoadFromDatabaseDamaged() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(b.TAGPUBLICINFO, samplePublic);
		b.set(b.TAGPRIVATEINFO, samplePrivate);
		b.setHQPublicKey(b.getAccount());
		saveAndVerifyValid("freshly created", b);
		
		DatabaseKey headerKey = new DatabaseKey(b.getBulletinHeaderPacket().getUniversalId());
		verifyVariousTypesOfDamage("bad header", b, false, headerKey, "", "");

		DatabaseKey dataKey = new DatabaseKey(b.getFieldDataPacket().getUniversalId());
		verifyVariousTypesOfDamage("bad field data", b, true, dataKey, "", samplePrivate);
		
		DatabaseKey privateDataKey = new DatabaseKey(b.getPrivateFieldDataPacket().getUniversalId());
		verifyVariousTypesOfDamage("bad private field data", b, true, privateDataKey, samplePublic, "");
	}

	void verifyVariousTypesOfDamage(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey, 
				String expectedPublic, String expectedPrivate) throws Exception 
	{
		verifyCorruptByRemovingOneCharAfterHeaderComment(label + " remove one char after header comment", 
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByDamagingHeaderComment(label + "damage header comment", 
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByDamagingSigComment(label + "damage sig comment", 
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByRemovingOneSigChar(label + "remove one sig char", 
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByModifyingOneSigChar(label + "modify one sig char", 
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByRemovingOneCharAfterHeaderComment(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey, 
				String expectedPublic, String expectedPrivate) throws Exception 
	{
		saveAndVerifyValid(label, b);
		String packetContents = db.readRecord(packetKey, security);
		final int positionAfterHeaderSig = packetContents.indexOf("-->") + 20;
		int removeCharAt = positionAfterHeaderSig;
		db.writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByModifyingOneSigChar(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey, 
				String expectedPublic, String expectedPrivate) throws Exception 
	{
		saveAndVerifyValid(label, b);
		String packetContents = db.readRecord(packetKey, security);
		final int positionInsideSig = packetContents.indexOf("<!--sig=") + 20;
		int modifyCharAt = positionInsideSig;
		char charToModify = packetContents.charAt(modifyCharAt);
		if(charToModify == '2')
			charToModify = '3';
		else
			charToModify = '2';
		String newPacketContents = packetContents.substring(0,modifyCharAt) + charToModify + packetContents.substring(modifyCharAt+1);
		db.writeRecord(packetKey, newPacketContents);
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByRemovingOneSigChar(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey, 
				String expectedPublic, String expectedPrivate) throws Exception 
	{
		saveAndVerifyValid(label, b);
		String packetContents = db.readRecord(packetKey, security);
		final int positionInsideSig = packetContents.indexOf("<!--sig=") + 20;
		int removeCharAt = positionInsideSig;
		db.writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByDamagingHeaderComment(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey, 
				String expectedPublic, String expectedPrivate) throws Exception 
	{
		saveAndVerifyValid(label, b);
		String packetContents = db.readRecord(packetKey, security);
		final int positionAfterHeaderSig = packetContents.indexOf(";;");
		int removeCharAt = positionAfterHeaderSig;
		db.writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByDamagingSigComment(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey, 
				String expectedPublic, String expectedPrivate) throws Exception 
	{
		saveAndVerifyValid(label, b);
		String packetContents = db.readRecord(packetKey, security);
		final int positionAfterHeaderSig = packetContents.indexOf("<!--sig=");
		int removeCharAt = positionAfterHeaderSig;
		db.writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void saveAndVerifyValid(String label, Bulletin b) throws Exception
	{
		b.save();
		Database db = b.getStore().getDatabase();
		DatabaseKey headerKey = new DatabaseKey(b.getBulletinHeaderPacket().getUniversalId());
		Bulletin stillValid = Bulletin.loadFromDatabase(store, headerKey, security);
		assertEquals(label + " not valid after save?", true, stillValid.isValid());
	}

	void verifyBulletinIsInvalid(String label, Bulletin b, boolean headerIsValid, 
				String expectedPublic, String expectedPrivate) throws Exception
	{
		DatabaseKey headerKey = new DatabaseKey(b.getBulletinHeaderPacket().getUniversalId());

		if(!headerIsValid)
		{
			try 
			{
				Bulletin.loadFromDatabase(b.getStore(), headerKey, security);
			} 
			catch (DamagedBulletinException ignoreExpectedException) 
			{
			}
			return;
		}

		Bulletin invalid = Bulletin.loadFromDatabase(b.getStore(), headerKey, security);
		assertEquals(label + " not invalid?", false, invalid.isValid());
		assertEquals(label + " wrong uid?", b.getUniversalId(), invalid.getUniversalId());
		assertEquals(label + " wrong fdp account?", b.getAccount(), invalid.getFieldDataPacket().getAccountId());
		assertEquals(label + " wrong private fdp account?", b.getAccount(), invalid.getPrivateFieldDataPacket().getAccountId());
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		assertEquals(label + " wrong fdp localId?", bhp.getFieldDataPacketId(), invalid.getFieldDataPacket().getLocalId());
		assertEquals(label + " wrong private fdp localId?", bhp.getPrivateFieldDataPacketId(), invalid.getPrivateFieldDataPacket().getLocalId());
		assertEquals(label + " public info", expectedPublic, invalid.get(b.TAGPUBLICINFO));
		assertEquals(label + " private info", expectedPrivate, invalid.get(b.TAGPRIVATEINFO));
		assertEquals(label + " hq key", "", invalid.getHQPublicKey());
	}
	
	public void testSaveToFile() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(b.TAGPUBLICINFO, "public info");
		b.set(b.TAGPRIVATEINFO, "private info");

		File tempFile = File.createTempFile("$$$MartusTest", null);
		tempFile.deleteOnExit();
		MockBulletin.saveToFile(b, tempFile);
		assertTrue("unreasonable file size?", tempFile.length() > 20);
		
		ZipFile zip = new ZipFile(tempFile);
		Enumeration entries = zip.entries();
		
		UniversalId uid = UniversalId.createDummyUniversalId();

		assertEquals("no data?", true, entries.hasMoreElements());
		ZipEntry dataEntry = (ZipEntry)entries.nextElement();
		assertNotNull("null data?", dataEntry);
		InputStreamWithSeek dataIn = new ZipEntryInputStream(zip, dataEntry);
		FieldDataPacket data = new FieldDataPacket(uid, Bulletin.getStandardFieldNames());
		data.loadFromXml(dataIn, security);
		assertEquals("data wrong?", b.get(b.TAGPUBLICINFO), data.get(b.TAGPUBLICINFO));

		assertEquals("no private data?", true, entries.hasMoreElements());
		ZipEntry privateDataEntry = (ZipEntry)entries.nextElement();
		assertNotNull("null data?", privateDataEntry);
		InputStreamWithSeek privateDataIn = new ZipEntryInputStream(zip, privateDataEntry);
		FieldDataPacket privateData = new FieldDataPacket(uid, Bulletin.getPrivateFieldNames());
		privateData.loadFromXml(privateDataIn, security);
		assertEquals("data wrong?", b.get(b.TAGPRIVATEINFO), privateData.get(b.TAGPRIVATEINFO));

		assertEquals("no header?", true, entries.hasMoreElements());
		ZipEntry headerEntry = (ZipEntry)entries.nextElement();
		assertNotNull("null header?", headerEntry);
		assertEquals("wrong header name?", b.getLocalId(), headerEntry.getName());
		InputStreamWithSeek headerIn = new ZipEntryInputStream(zip, headerEntry);
		BulletinHeaderPacket header = new BulletinHeaderPacket("");
		header.loadFromXml(headerIn, security);
		headerIn.close();
		assertEquals("header id wrong?", b.getLocalId(), header.getLocalId());
		assertEquals("wrong data name?", header.getFieldDataPacketId(), dataEntry.getName());
		assertEquals("wrong privatedata name?", header.getPrivateFieldDataPacketId(), privateDataEntry.getName());
		
		assertEquals("too many entries?", false, entries.hasMoreElements());
	}
	
	public void testLoadFromFile() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(b.TAGPUBLICINFO, "public info");
		b.set(b.TAGPRIVATEINFO, "private info");
		b.setSealed();

		File tempFile = File.createTempFile("$$$MartusTest", null);
		tempFile.deleteOnExit();
		MockBulletin.saveToFile(b, tempFile);
		
		Bulletin loaded = store.createEmptyBulletin();
		loaded.loadFromFile(tempFile, security);
		assertEquals("wrong id?", b.getLocalId(), loaded.getLocalId());
		assertEquals("public info", b.get(b.TAGPUBLICINFO), loaded.get(b.TAGPUBLICINFO));
		assertEquals("private info", b.get(b.TAGPRIVATEINFO), loaded.get(b.TAGPRIVATEINFO));
		assertEquals("status", b.getStatus(), loaded.getStatus());
	}
	
	public void testLoadFromFileEmpty() throws Exception
	{
		verifyLoadFromFileInvalid(MODE_EMPTY_FILE, "MODE_EMPTY_FILE");
		verifyLoadFromFileInvalid(MODE_MISNAMED_HEADER, "MODE_MISNAMED_HEADER");
		verifyLoadFromFileInvalid(MODE_INVALID_HEADER, "MODE_INVALID_HEADER");
		verifyLoadFromFileInvalid(MODE_MISSING_DATA, "MODE_MISSING_DATA");
		verifyLoadFromFileInvalid(MODE_INVALID_DATA, "MODE_INVALID_DATA");
	}

	public void verifyLoadFromFileInvalid(int mode, String label) throws Exception
	{
		MartusCrypto signer = store.getSignatureGenerator();
		
		File tempFile = File.createTempFile("$$$MartusTest", null);
		tempFile.deleteOnExit();
		if(mode != MODE_EMPTY_FILE)
		{
			Bulletin b = store.createEmptyBulletin();
			BulletinHeaderPacket headerPacket = b.getBulletinHeaderPacket();
	
			ByteArrayOutputStream headerOut = new ByteArrayOutputStream();
			headerPacket.writeXml(headerOut, signer);
			byte[] headerBytes = headerOut.toByteArray();

			String badData = "Not a valid data packet";
			byte[] dataBytes = badData.getBytes();

			FileOutputStream outputStream = new FileOutputStream(tempFile);
			ZipOutputStream zipOut = new ZipOutputStream(outputStream);

			ZipEntry headerEntry = new ZipEntry(headerPacket.getLocalId());
			if(mode != MODE_MISSING_DATA)
			{
				ZipEntry dataEntry = new ZipEntry(headerPacket.getFieldDataPacketId());
				zipOut.putNextEntry(dataEntry);
				zipOut.write(dataBytes);
			}

			if(mode == MODE_MISNAMED_HEADER)
				headerEntry = new ZipEntry("misnamed header");
			zipOut.putNextEntry(headerEntry);

			if(mode != MODE_INVALID_HEADER)
				zipOut.write(headerBytes);
				
			zipOut.close();	
		}
		
		Bulletin loaded = store.createEmptyBulletin();
		try
		{
			loaded.loadFromFile(tempFile, security);
			fail("should have thrown: " + label);
		}
		catch(IOException e)
		{
			//expected exception
		}
	}

	public void testCopyConstructor() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set("author", "someone");
		b.save();
		
		Bulletin copy = new Bulletin(b);
		assertEquals("store", b.getStore(), copy.getStore());
		assertEquals("id", b.getLocalId(), copy.getLocalId());
		assertEquals("account", b.getAccount(), copy.getAccount());
		assertEquals("author", b.get("author"), copy.get("author"));
		assertEquals("fdp id", b.getFieldDataPacket().getLocalId(), copy.getFieldDataPacket().getLocalId());
		assertEquals("fdp account", b.getFieldDataPacket().getAccountId(), copy.getFieldDataPacket().getAccountId());
		assertEquals("header fdp id", b.getBulletinHeaderPacket().getFieldDataPacketId(),copy.getBulletinHeaderPacket().getFieldDataPacketId());
	}

	public void testPullFrom() throws Exception
	{
		Bulletin b1 = store.createEmptyBulletin();
		b1.set(b1.TAGPUBLICINFO, "public info");
		b1.set(b1.TAGPRIVATEINFO, "private info");
		b1.setSealed();
		b1.save();
		Bulletin b2 = store.createEmptyBulletin();
		b2.pullDataFrom(b1);
		assertEquals("store unchanged", store, b2.getStore());
		assertEquals("id unchanged", false, b2.getLocalId().equals(b1.getLocalId()));
		assertEquals("public info", b1.get(b1.TAGPUBLICINFO), b2.get(b1.TAGPUBLICINFO));
		assertEquals("private info", b1.get(b1.TAGPRIVATEINFO), b2.get(b1.TAGPRIVATEINFO));
		assertEquals("wrong status?", b1.getStatus(), b2.getStatus());
		assertEquals("wrong private?", b1.isAllPrivate(), b2.isAllPrivate());

		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b1.addPublicAttachment(a1);
		b1.addPrivateAttachment(a2);
		b2.pullDataFrom(b1);
		assertEquals("public attachment count", 1, b2.getPublicAttachments().length);
		assertEquals("private attachment count", 1, b2.getPrivateAttachments().length);
		assertEquals("public attachment data", a1, b2.getPublicAttachments()[0]);
		assertEquals("private attachment data", a2, b2.getPrivateAttachments()[0]);
		b2.pullDataFrom(b1);
		assertEquals("again public attachment count", 1, b2.getPublicAttachments().length);
		assertEquals("again private attachment count", 1, b2.getPrivateAttachments().length);
		assertEquals("again public attachment data", a1, b2.getPublicAttachments()[0]);
		assertEquals("again private attachment data", a2, b2.getPrivateAttachments()[0]);
		
		b1.setAllPrivate(false);
		b2.pullDataFrom(b1);
		assertEquals("didn't pull private false?", b1.isAllPrivate(), b2.isAllPrivate());

		b1.setAllPrivate(true);
		b2.pullDataFrom(b1);
		assertEquals("didn't pull private true?", b1.isAllPrivate(), b2.isAllPrivate());
	}
	
	public void testIsStringInArray()
	{
		String a = "abc";
		String b = "bcde";
		String c = "cdefg";
		String[] abc = new String[] {a,b,c};
		assertEquals("a not in abc?", true, Bulletin.isStringInArray(abc, a));
		assertEquals("b not in abc?", true, Bulletin.isStringInArray(abc, b));
		assertEquals("c not in abc?", true, Bulletin.isStringInArray(abc, c));
		assertEquals("x in abc?", false, Bulletin.isStringInArray(abc, "xyz"));
	}

	public void testStoredDateFormat()
	{
		DateFormat df = Bulletin.getStoredDateFormat();
		assertEquals(false, df.isLenient());
		Date d;
		try
		{
			d = df.parse("2003-07-02");
			Calendar cal = new GregorianCalendar();
			cal.setTime(d);
			assertEquals(2003, cal.get(Calendar.YEAR));
			assertEquals(7-1, cal.get(Calendar.MONTH));
			assertEquals(2, cal.get(Calendar.DATE));
		}
		catch(ParseException e)
		{
			assertTrue(false);
		}
	}

	public void testGetToday()
	{
		DateFormat df = Bulletin.getStoredDateFormat();
		String result = df.format(new Date());
		assertEquals(result, Bulletin.getToday());
	}

	public void testAddAttachment() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		assertEquals("no attachments yet", 0, b.getPublicAttachments().length);
		assertEquals("no private attachments yet", 0, b.getPrivateAttachments().length);

		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		AttachmentProxy a3 = new AttachmentProxy(tempFile1);
		AttachmentProxy a4 = new AttachmentProxy(tempFile2);
		AttachmentProxy a5 = new AttachmentProxy(tempFile3);
		AttachmentProxy a6 = new AttachmentProxy(tempFile3);
		b.addPublicAttachment(a1);
		assertEquals("added one", 1, b.getPublicAttachments().length);
		b.addPublicAttachment(a2);
		assertEquals("added another", 2, b.getPublicAttachments().length);
		b.addPublicAttachment(a3);
		assertEquals("added third", 3, b.getPublicAttachments().length);

		b.addPrivateAttachment(a4);
		assertEquals("added 4", 1, b.getPrivateAttachments().length);
		b.addPrivateAttachment(a5);
		assertEquals("added 5", 2, b.getPrivateAttachments().length);
		b.addPrivateAttachment(a6);
		assertEquals("added 6", 3, b.getPrivateAttachments().length);

		AttachmentProxy[] v = b.getPublicAttachments();
		assertEquals("a1 label", tempFile1.getName(), v[0].getLabel());
		assertEquals("a2 label", tempFile2.getName(), v[1].getLabel());
		assertEquals("a3 label", tempFile1.getName(), v[2].getLabel());

		AttachmentProxy[] vp = b.getPrivateAttachments();
		assertEquals("a4 label", tempFile2.getName(), vp[0].getLabel());
		assertEquals("a5 label", tempFile3.getName(), vp[1].getLabel());
		assertEquals("a6 label", tempFile3.getName(), vp[2].getLabel());
	}
	
	public void testExtractAttachment() throws Exception
	{
		store.deleteAllData();
		Bulletin original = store.createEmptyBulletin();
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		original.addPublicAttachment(a1);
		original.addPublicAttachment(a2);
		original.save();
		assertEquals("wrong record count", 5, db.getRecordCount());
		
		Bulletin loaded = store.findBulletinByUniversalId(original.getUniversalId());
		assertNotNull("not saved?", loaded);
		AttachmentProxy[] list = loaded.getPublicAttachments();
		assertEquals("count wrong?", 2, list.length);
		
		File destFile1 = File.createTempFile("$$$MartusTestBulletinExt", null);
		destFile1.deleteOnExit();
		destFile1.delete();

		loaded.extractAttachmentToFile(list[0], security, destFile1);
		assertTrue("didn't create?", destFile1.exists());
	}
	
	public void testDetectFieldPacketWithWrongSig() throws Exception
	{
		Database db = store.getDatabase();
		
		Bulletin original = store.createEmptyBulletin();
		original.set(original.TAGPUBLICINFO, "public info");
		original.set(original.TAGPRIVATEINFO, "private info");
		original.setSealed();
		original.save();

		Bulletin loaded = Bulletin.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not valid?", true, loaded.isValid());
		
		FieldDataPacket fdp = loaded.getFieldDataPacket();
		fdp.set(original.TAGPUBLICINFO, "different public!");
		loaded.writePacketToDatabase(fdp, db, security);
		
		loaded = Bulletin.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not invalid?", false, loaded.isValid());
		assertEquals("private messed up?", original.get(original.TAGPRIVATEINFO), loaded.get(loaded.TAGPRIVATEINFO));
	}
	
	public void testDetectPrivateFieldPacketWithWrongSig() throws Exception
	{
		Database db = store.getDatabase();
		
		Bulletin original = store.createEmptyBulletin();
		original.set(original.TAGPUBLICINFO, "public info");
		original.set(original.TAGPRIVATEINFO, "private info");
		original.setSealed();
		original.save();

		Bulletin loaded = Bulletin.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not valid?", true, loaded.isValid());
		
		FieldDataPacket pdp = loaded.getPrivateFieldDataPacket();
		pdp.set(original.TAGPRIVATEINFO, "different private!");
		loaded.writePacketToDatabase(pdp, db, security);
		
		loaded = Bulletin.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not invalid?", false, loaded.isValid());
		assertEquals("public messed up?", original.get(original.TAGPUBLICINFO), loaded.get(loaded.TAGPUBLICINFO));
	}
	
	public void testGetAndSetHQPublicKey()
	{
		Bulletin original = store.createEmptyBulletin();
		assertEquals("HQKey already set?", "", original.getHQPublicKey());
		original.set(original.TAGPUBLICINFO, "public info");
		String key = "12345";
		original.setHQPublicKey(key);
		assertEquals("HQKey not set?", key, original.getHQPublicKey());
		assertEquals("HQKey not set in public?", key, original.getFieldDataPacket().getHQPublicKey());
		assertEquals("HQKey not set in private?", key, original.getPrivateFieldDataPacket().getHQPublicKey());
	}
	
	public void testLoadAndSaveWithHQPublicKey() throws Exception
	{
		Bulletin original = store.createEmptyBulletin();
		original.set(original.TAGPUBLICINFO, "public info");
		String key = security.getPublicKeyString();
		original.setHQPublicKey(key);
		original.save();
		DatabaseKey dbKey = new DatabaseKey(original.getUniversalId());
		Bulletin loaded = Bulletin.loadFromDatabase(store, dbKey);
		assertEquals("Keys not the same?", original.getFieldDataPacket().getHQPublicKey(), loaded.getFieldDataPacket().getHQPublicKey());
		
		File tempFile = createTempFile();
		MockBulletin.saveToFile(original, tempFile);
		Bulletin loaded2 = store.createEmptyBulletin();
		loaded2.loadFromFile(tempFile, security);
		assertEquals("Loaded Keys not the same?", original.getFieldDataPacket().getHQPublicKey(), loaded2.getFieldDataPacket().getHQPublicKey());
	}
	
	public void testExportAndImportZipBetweenAccounts() throws Exception
	{
		Bulletin original = store.createEmptyBulletin();
		original.set(original.TAGPUBLICINFO, "public info");
		original.set(original.TAGPRIVATEINFO, "private info");
		String key = security.getPublicKeyString();
		File tempFile = createTempFile();
		
		MartusSecurity otherSecurity = new MartusSecurity();
		otherSecurity.createKeyPair(512);

		original.setDraft();
		original.setAllPrivate(true);
		MockBulletin.saveToFile(original, tempFile);
		Bulletin loaded2 = store.createEmptyBulletin();
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("draft private could get public?", "", loaded2.get(loaded2.TAGPUBLICINFO));
		assertEquals("draft private could get private?", "", loaded2.get(loaded2.TAGPRIVATEINFO));
		
		original.setDraft();
		original.setAllPrivate(false);
		MockBulletin.saveToFile(original, tempFile);
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("draft public could get encrypted public?", "", loaded2.get(loaded2.TAGPUBLICINFO));
		assertEquals("draft public could get private?", "", loaded2.get(loaded2.TAGPRIVATEINFO));
		
		original.setSealed();
		original.setAllPrivate(true);
		MockBulletin.saveToFile(original, tempFile);
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("sealed private could get encrypted public?", "", loaded2.get(loaded2.TAGPUBLICINFO));
		assertEquals("sealed private could get private?", "", loaded2.get(loaded2.TAGPRIVATEINFO));

		original.setSealed();
		original.setAllPrivate(false);
		MockBulletin.saveToFile(original, tempFile);
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("sealed public couldn't get encrypted public?", original.get(original.TAGPUBLICINFO), loaded2.get(loaded2.TAGPUBLICINFO));
		assertEquals("sealed public could get private?", "", loaded2.get(loaded2.TAGPRIVATEINFO));
	}
	
	static final String samplePublic = "some public text";
	static final String samplePrivate = "a bit of private text";
	
	static final int MODE_EMPTY_FILE = 0;
	static final int MODE_INVALID_HEADER = 1;
	static final int MODE_MISNAMED_HEADER = 2;
	static final int MODE_MISSING_DATA = 3;
	static final int MODE_INVALID_DATA = 4;

	static final String sampleLabel = "label for an attachment";
	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
	static final byte[] sampleBytes3 = {6,5,0,4,7,5,5,4,4,0};
	static final byte[] sampleBytes4 = {12,34,56};
	static final byte[] sampleBytes5 = {9,8,7,6,5};
	static final byte[] sampleBytes6 = {1,3,5,7,9,11,13};
	static File tempFile1;
	static File tempFile2;
	static File tempFile3;
	static File tempFile4;
	static File tempFile5;
	static File tempFile6;
	static AttachmentProxy proxy1;
	static AttachmentProxy proxy2;
	static AttachmentProxy proxy3;
	static AttachmentProxy proxy4;
	static AttachmentProxy proxy5;
	static AttachmentProxy proxy6;
	
	MockMartusApp app;
	static MockDatabase db;
	static BulletinStore store;
	static MartusSecurity security;
}
