/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.test;

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

import org.martus.client.core.Bulletin;
import org.martus.client.core.BulletinLoader;
import org.martus.client.core.BulletinSaver;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.SearchParser;
import org.martus.client.core.SearchTreeNode;
import org.martus.common.AttachmentPacket;
import org.martus.common.AttachmentProxy;
import org.martus.common.BulletinConstants;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
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
		assertEquals(false, b.isStandardField(Bulletin.TAGPRIVATEINFO));

		assertEquals(false, b.isPrivateField("LOCATION"));
		assertEquals(true, b.isPrivateField(Bulletin.TAGPRIVATEINFO));

		b = store.createEmptyBulletin();
		assertNotEquals("", b.getLocalId());

		assertEquals(store, b.getStore());

		assertEquals("account not initialized correctly?", store.getAccountId(), b.getAccount());
		assertEquals("field data account?", store.getAccountId(), b.getFieldDataPacket().getAccountId());

	}

	public void testAllPrivate()
	{
		Bulletin b = store.createEmptyBulletin();
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

		b.set(Bulletin.TAGPRIVATEINFO, "secret");
		assertEquals("secret", b.get(Bulletin.TAGPRIVATEINFO));
	}

	public void testClear()
	{
		String publicInfo = "public info";
		String privateInfo = "private info";

		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGPUBLICINFO, publicInfo);
		b.set(Bulletin.TAGPRIVATEINFO, privateInfo);
		assertEquals("public info not set?", publicInfo, b.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info not set?", privateInfo, b.get(Bulletin.TAGPRIVATEINFO));
		b.clear();
		assertEquals("public info not cleared?", "", b.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info not cleared?", "", b.get(Bulletin.TAGPRIVATEINFO));
	}

	public void testMatches()
	{
		Bulletin b = store.createEmptyBulletin();
		b.set("author", "hello");
		b.set("summary", "summary");
		b.set("title", "Josée");
		b.set(Bulletin.TAGEVENTDATE, "2002-04-04");
		b.set(Bulletin.TAGENTRYDATE, "2002-10-15");

		b.save();
		String beginDate ="1900-01-01";
		String endDate = "2099-12-31";

		assertEquals("hello", true, b.matches(new SearchTreeNode("hello"), beginDate, endDate));
		// field names should not be searched
		assertEquals("author", false, b.matches(new SearchTreeNode("author"), beginDate, endDate));
		// id should not be searched
		assertEquals("getLocalId()", false, b.matches(new SearchTreeNode(b.getLocalId()), beginDate, endDate));

		assertEquals("HELLO", true, b.matches(new SearchTreeNode("HELLO"), beginDate, endDate));
		assertEquals("Josée", true, b.matches(new SearchTreeNode("Josée"), beginDate, endDate));
		assertEquals("josée", true, b.matches(new SearchTreeNode("josée"), beginDate, endDate));
		assertEquals("josÉe", true, b.matches(new SearchTreeNode("josÉe"), beginDate, endDate));
		assertEquals("josee", false, b.matches(new SearchTreeNode("josee"), beginDate, endDate));
		assertEquals("Blank must match", true, b.matches(new SearchTreeNode(""), beginDate, endDate));

		SearchParser parser = new SearchParser(app);
		assertEquals("right false and", false, b.matches(parser.parse("hello and goodbye"), beginDate, endDate));
		assertEquals("left false and", false, b.matches(parser.parse("goodbye and hello"), beginDate, endDate));
		assertEquals("true and", true, b.matches(parser.parse("Hello and Summary"), beginDate, endDate));

		assertEquals("false or", false, b.matches(parser.parse("swinging and swaying"), beginDate, endDate));
		assertEquals("left true or", true, b.matches(parser.parse("hello or goodbye"), beginDate, endDate));
		assertEquals("right true or", true, b.matches(parser.parse("goodbye or hello"), beginDate, endDate));
		assertEquals("both true or", true, b.matches(parser.parse("hello or summary"), beginDate, endDate));
		assertEquals("Blank didn't match", true, b.matches(parser.parse(""), beginDate, endDate));
	}

	public void testDateMatches()
	{
		Bulletin b = store.createEmptyBulletin();
		b.set("author", "Dave");
		b.set("summary", "summary");
		b.set("title", "cool day");
		b.set(Bulletin.TAGEVENTDATE, "2002-04-04");
		b.set(Bulletin.TAGENTRYDATE, "2002-10-15");

		b.save();
		String outOfRangeBeginDate ="2003-01-01";
		String outOfRangeEndDate = "2006-12-31";
		String bothInRangeBeginDate ="2002-01-01";
		String bothInRangeEndDate = "2002-12-31";
		String eventInRangeBeginDate ="2002-01-01";
		String eventInRangeEndDate = "2002-04-04";
		String entryInRangeBeginDate ="2002-10-15";
		String entryInRangeEndDate = "2002-10-16";

		assertEquals("out of range", false, b.matches(new SearchTreeNode(""), outOfRangeBeginDate, outOfRangeEndDate));
		assertEquals("both event and entry in range", true, b.matches(new SearchTreeNode(""), bothInRangeBeginDate, bothInRangeEndDate));
		assertEquals("event only in range", true, b.matches(new SearchTreeNode(""), eventInRangeBeginDate, eventInRangeEndDate));
		assertEquals("entry only in range", true, b.matches(new SearchTreeNode(""), entryInRangeBeginDate, entryInRangeEndDate));

		SearchParser parser = new SearchParser(app);
		assertEquals("parser out of range", false, b.matches(parser.parse(""), outOfRangeBeginDate, outOfRangeEndDate));
		assertEquals("parser both event and entry in range", true, b.matches(parser.parse(""), bothInRangeBeginDate, bothInRangeEndDate));
		assertEquals("parser event only in range", true, b.matches(parser.parse(""), eventInRangeBeginDate, eventInRangeEndDate));
		assertEquals("parser entry only in range", true, b.matches(parser.parse(""), entryInRangeBeginDate, entryInRangeEndDate));

		assertEquals("both event and entry in range but string doesn't match", false, b.matches(new SearchTreeNode("hello"), bothInRangeBeginDate, bothInRangeEndDate));
		assertEquals("parser both event and entry in range but string doesn't match", false, b.matches(parser.parse("hi"), bothInRangeBeginDate, bothInRangeEndDate));

		assertEquals("both event and entry in range and string matchs", true, b.matches(new SearchTreeNode("Dave"), bothInRangeBeginDate, bothInRangeEndDate));
		assertEquals("parser both event and entry in range and string matchs", true, b.matches(parser.parse("Dave"), bothInRangeBeginDate, bothInRangeEndDate));

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
		assertEquals("Ecrypted?", true, store.mustEncryptPublicData());
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
		Bulletin b2 = BulletinLoader.loadFromDatabase(store, new DatabaseKey(b.getUniversalId()));
		long loadedTime = b2.getLastSavedTime();
		assertEquals("Didn't keep time saved?", firstSavedTime, loadedTime);
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
		BulletinSaver.saveToDatabase(original, db, store.mustEncryptPublicData());
		UniversalId uid = original.getUniversalId();

		original = BulletinLoader.loadFromDatabase(store, new DatabaseKey(uid));
		AttachmentProxy[] originalAttachments = original.getPublicAttachments();
		assertEquals("not one attachment?", 1, originalAttachments.length);
		DatabaseKey key2 = new DatabaseKey(originalAttachments[0].getUniversalId());
		assertEquals("public attachment wasn't saved?", true,  store.getDatabase().doesRecordExist(key2));

		AttachmentProxy[] originalPrivateAttachments = original.getPrivateAttachments();
		assertEquals("not one attachment in private?", 1, originalPrivateAttachments.length);
		DatabaseKey keyPrivate = new DatabaseKey(originalPrivateAttachments[0].getUniversalId());
		assertEquals("private attachment wasn't saved?", true,  store.getDatabase().doesRecordExist(keyPrivate));

		File tmpFile = File.createTempFile("$$$MartusTestBullSaveFileAtta1", null);
		tmpFile.deleteOnExit();
		MockBulletin.saveToFile(db, original, tmpFile);
		assertTrue("unreasonable file size?", tmpFile.length() > 20);

		ZipFile zip = new ZipFile(tmpFile);
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

	public void testSaveToFile() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGPUBLICINFO, "public info");
		b.set(Bulletin.TAGPRIVATEINFO, "private info");

		File tempFile = File.createTempFile("$$$MartusTest", null);
		tempFile.deleteOnExit();
		MockBulletin.saveToFile(db, b, tempFile);
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
		assertEquals("data wrong?", b.get(Bulletin.TAGPUBLICINFO), data.get(Bulletin.TAGPUBLICINFO));

		assertEquals("no private data?", true, entries.hasMoreElements());
		ZipEntry privateDataEntry = (ZipEntry)entries.nextElement();
		assertNotNull("null data?", privateDataEntry);
		InputStreamWithSeek privateDataIn = new ZipEntryInputStream(zip, privateDataEntry);
		FieldDataPacket privateData = new FieldDataPacket(uid, Bulletin.getPrivateFieldNames());
		privateData.loadFromXml(privateDataIn, security);
		assertEquals("data wrong?", b.get(Bulletin.TAGPRIVATEINFO), privateData.get(Bulletin.TAGPRIVATEINFO));

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
		b.set(Bulletin.TAGPUBLICINFO, "public info");
		b.set(Bulletin.TAGPRIVATEINFO, "private info");
		b.setSealed();

		File tempFile = File.createTempFile("$$$MartusTest", null);
		tempFile.deleteOnExit();
		MockBulletin.saveToFile(db, b, tempFile);

		Bulletin loaded = store.createEmptyBulletin();
		loaded.loadFromFile(tempFile, security);
		assertEquals("wrong id?", b.getLocalId(), loaded.getLocalId());
		assertEquals("public info", b.get(Bulletin.TAGPUBLICINFO), loaded.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info", b.get(Bulletin.TAGPRIVATEINFO), loaded.get(Bulletin.TAGPRIVATEINFO));
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

	public void testGetStatus() throws Exception
	{
		Bulletin b1 = store.createEmptyBulletin();
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		b1.setSealed();
		assertEquals("Not Sealed Status?", BulletinConstants.STATUSSEALED, b1.get(Bulletin.TAGSTATUS));
		b1.setDraft();
		assertEquals("Not Draft Status?", BulletinConstants.STATUSDRAFT, b1.get(Bulletin.TAGSTATUS));
	}

	public void testPullFrom() throws Exception
	{
		Bulletin b1 = store.createEmptyBulletin();
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		b1.setSealed();
		b1.save();
		Bulletin b2 = store.createEmptyBulletin();
		b2.pullDataFrom(b1);
		assertEquals("store unchanged", store, b2.getStore());
		assertEquals("id unchanged", false, b2.getLocalId().equals(b1.getLocalId()));
		assertEquals("public info", b1.get(Bulletin.TAGPUBLICINFO), b2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info", b1.get(Bulletin.TAGPRIVATEINFO), b2.get(Bulletin.TAGPRIVATEINFO));
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
		assertEquals("a not in abc?", true, MartusUtilities.isStringInArray(abc, a));
		assertEquals("b not in abc?", true, MartusUtilities.isStringInArray(abc, b));
		assertEquals("c not in abc?", true, MartusUtilities.isStringInArray(abc, c));
		assertEquals("x in abc?", false, MartusUtilities.isStringInArray(abc, "xyz"));
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

		BulletinSaver.extractAttachmentToFile(db, list[0], security, destFile1);
		assertTrue("didn't create?", destFile1.exists());
	}

	public void testGetAndSetHQPublicKey()
	{
		Bulletin original = store.createEmptyBulletin();
		assertEquals("HQKey already set?", "", original.getHQPublicKey());
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		String key = "12345";
		original.setHQPublicKey(key);
		assertEquals("HQKey not set?", key, original.getHQPublicKey());
		assertEquals("HQKey not set in public?", key, original.getFieldDataPacket().getHQPublicKey());
		assertEquals("HQKey not set in private?", key, original.getPrivateFieldDataPacket().getHQPublicKey());
	}

	public void testLoadAndSaveWithHQPublicKey() throws Exception
	{
		Bulletin original = store.createEmptyBulletin();
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		String key = security.getPublicKeyString();
		original.setHQPublicKey(key);
		original.save();
		DatabaseKey dbKey = new DatabaseKey(original.getUniversalId());
		Bulletin loaded = BulletinLoader.loadFromDatabase(store, dbKey);
		assertEquals("Keys not the same?", original.getFieldDataPacket().getHQPublicKey(), loaded.getFieldDataPacket().getHQPublicKey());

		File tempFile = createTempFile();
		MockBulletin.saveToFile(db, original, tempFile);
		Bulletin loaded2 = store.createEmptyBulletin();
		loaded2.loadFromFile(tempFile, security);
		assertEquals("Loaded Keys not the same?", original.getFieldDataPacket().getHQPublicKey(), loaded2.getFieldDataPacket().getHQPublicKey());
	}

	public void testExportAndImportZipBetweenAccounts() throws Exception
	{
		Bulletin original = store.createEmptyBulletin();
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		original.set(Bulletin.TAGPRIVATEINFO, "private info");
		File tempFile = createTempFile();

		MartusSecurity otherSecurity = new MartusSecurity();
		otherSecurity.createKeyPair(512);

		original.setDraft();
		original.setAllPrivate(true);
		MockBulletin.saveToFile(db, original, tempFile);
		Bulletin loaded2 = store.createEmptyBulletin();
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("draft private could get public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("draft private could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setDraft();
		original.setAllPrivate(false);
		MockBulletin.saveToFile(db,original, tempFile);
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("draft public could get encrypted public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("draft public could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setSealed();
		original.setAllPrivate(true);
		MockBulletin.saveToFile(db,original, tempFile);
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("sealed private could get encrypted public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("sealed private could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setSealed();
		original.setAllPrivate(false);
		MockBulletin.saveToFile(db,original, tempFile);
		loaded2.loadFromFile(tempFile, otherSecurity);
		assertEquals("sealed public couldn't get encrypted public?", original.get(Bulletin.TAGPUBLICINFO), loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("sealed public could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));
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
