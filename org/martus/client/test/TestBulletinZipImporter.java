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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.client.core.BulletinStore;
import org.martus.client.core.BulletinZipImporter;
import org.martus.common.AttachmentPacket;
import org.martus.common.AttachmentProxy;
import org.martus.common.Bulletin;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.BulletinSaver;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;
import org.martus.common.ZipEntryInputStream;

public class TestBulletinZipImporter extends TestCaseEnhanced
{

	public TestBulletinZipImporter(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		if(security == null)
		{
			security = new MartusSecurity();
			security.createKeyPair(512);
		}
		if(store == null)
		{
			db = new MockClientDatabase();
			store = new BulletinStore(db);
			security = new MockMartusSecurity();
			store.setSignatureGenerator(security);
		}
		store.deleteAllData();
	}

	public void testLoadFromFile() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGPUBLICINFO, "public info");
		b.set(Bulletin.TAGPRIVATEINFO, "private info");
		b.setSealed();

		File tempFile = File.createTempFile("$$$MartusTest", null);
		tempFile.deleteOnExit();
		MockBulletin.saveToFile(db, b, tempFile, store.getSignatureVerifier());

		Bulletin loaded = store.createEmptyBulletin();
		BulletinZipImporter.loadFromFile(loaded, tempFile, security);
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
			BulletinZipImporter.loadFromFile(loaded, tempFile, security);
			fail("should have thrown: " + label);
		}
		catch(IOException e)
		{
			//expected exception
		}
	}

	public void testSaveToFileWithAttachment() throws Exception
	{
		File tempFile1 = createTempFile(sampleBytes1);
		File tempFile2 = createTempFile(sampleBytes2);
		UniversalId dummyUid = UniversalId.createDummyUniversalId();

		Bulletin original = store.createEmptyBulletin();
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.setSealed();
		BulletinSaver.saveToDatabase(original, db, store.mustEncryptPublicData(), security);
		UniversalId uid = original.getUniversalId();

		original = store.loadFromDatabase(new DatabaseKey(uid));
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
		MockBulletin.saveToFile(db, original, tmpFile, store.getSignatureVerifier());
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
		MockBulletin.saveToFile(db, b, tempFile, store.getSignatureVerifier());
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
		MockBulletin.saveToFile(db, original, tempFile, store.getSignatureVerifier());
		Bulletin loaded2 = store.createEmptyBulletin();
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("draft private could get public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("draft private could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setDraft();
		original.setAllPrivate(false);
		MockBulletin.saveToFile(db,original, tempFile, store.getSignatureVerifier());
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("draft public could get encrypted public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("draft public could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setSealed();
		original.setAllPrivate(true);
		MockBulletin.saveToFile(db,original, tempFile, store.getSignatureVerifier());
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("sealed private could get encrypted public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("sealed private could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setSealed();
		original.setAllPrivate(false);
		MockBulletin.saveToFile(db,original, tempFile, store.getSignatureVerifier());
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("sealed public couldn't get encrypted public?", original.get(Bulletin.TAGPUBLICINFO), loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("sealed public could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));
	}

	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};

	static final int MODE_EMPTY_FILE = 0;
	static final int MODE_INVALID_HEADER = 1;
	static final int MODE_MISNAMED_HEADER = 2;
	static final int MODE_MISSING_DATA = 3;
	static final int MODE_INVALID_DATA = 4;

	static MockDatabase db;
	static BulletinStore store;
	static MartusSecurity security;
}
