/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

package org.martus.common.test;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.martus.common.FieldSpec;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinSaver;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.MockDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;


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
			tempFile1 = createTempFileWithData(sampleBytes1);
			tempFile2 = createTempFileWithData(sampleBytes2);
			tempFile3 = createTempFileWithData(sampleBytes3);
			tempFile4 = createTempFileWithData(sampleBytes4);
			tempFile5 = createTempFileWithData(sampleBytes5);
			tempFile6 = createTempFileWithData(sampleBytes6);
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
		db = new MockClientDatabase();
    }

    public void tearDown() throws Exception
    {
    }

    public void testBasics()
    {
		Bulletin b = new Bulletin(security);
		assertEquals(false, b.isStandardField("Nope"));
		assertEquals(false, b.isStandardField("Location"));
		assertEquals(true, b.isStandardField("location"));
		assertEquals(false, b.isStandardField("LOCATION"));
		assertEquals(false, b.isStandardField(Bulletin.TAGPRIVATEINFO));

		assertEquals(false, b.isPrivateField("LOCATION"));
		assertEquals(true, b.isPrivateField(Bulletin.TAGPRIVATEINFO));

		b = new Bulletin(security);
		assertNotEquals("", b.getLocalId());

		assertEquals(security, b.getSignatureGenerator());

		assertEquals("account not initialized correctly?", security.getPublicKeyString(), b.getAccount());
		assertEquals("field data account?", security.getPublicKeyString(), b.getFieldDataPacket().getAccountId());

	}
	
	public void testUnknownTags()
	{
		Bulletin b = new Bulletin(security);
		assertFalse("already has unknown?", b.hasUnknownTags());
		
		b.getBulletinHeaderPacket().setHasUnknownTags(true);
		assertTrue("header unknown", b.hasUnknownTags());
		b.getBulletinHeaderPacket().setHasUnknownTags(false);
		
		b.getFieldDataPacket().setHasUnknownTags(true);
		assertTrue("fdp unknown", b.hasUnknownTags());
		b.getFieldDataPacket().setHasUnknownTags(false);
		
		b.getPrivateFieldDataPacket().setHasUnknownTags(true);
		assertTrue("private fdp unknown", b.hasUnknownTags());
		b.getPrivateFieldDataPacket().setHasUnknownTags(false);

		assertFalse("not back to normal?", b.hasUnknownTags());
	}
	
	public void testHasUnknownCustomField()
	{
		Bulletin noUnknown = new Bulletin(security);
		assertFalse("has unknown custom?", noUnknown.hasUnknownCustomField());

		BulletinForTesting.shouldCreateUnknownStuffInCustomField = true;
		Bulletin withUnknown = new BulletinForTesting(security);
		assertTrue("no unknown custom?", withUnknown.hasUnknownCustomField());
		BulletinForTesting.clearShoulds();
	}

	public void testAllPrivate()
	{
		Bulletin b = new Bulletin(security);
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
		Bulletin b = new Bulletin(security);
		assertNotNull("Id was Null?", b.getLocalId());
		assertEquals("Id was empty?", false, b.getLocalId().length()==0);
	}

	public void testStatus()
	{
		Bulletin b = new Bulletin(security);
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
		Bulletin b = new Bulletin(security);
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
		Bulletin b = new Bulletin(security);
		assertEquals("", b.get("NoSuchField"));
		b.set("NoSuchField", "hello");
		assertEquals("", b.get("NoSuchField"));

		assertEquals("", b.get("author"));
		b.set("author", "hello");
		assertEquals("hello", b.get("author"));
		assertEquals("", b.get("Author"));
		assertEquals("", b.get("AUTHOR"));

		b.set("location", "94404");
		assertEquals("94404", b.get("location"));
		b.set("author", "goodbye");
		assertEquals("goodbye", b.get("author"));

		b.set(Bulletin.TAGPRIVATEINFO, "secret");
		assertEquals("secret", b.get(Bulletin.TAGPRIVATEINFO));
	}

	public void testClear()
	{
		String publicInfo = "public info";
		String privateInfo = "private info";

		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGPUBLICINFO, publicInfo);
		b.set(Bulletin.TAGPRIVATEINFO, privateInfo);
		assertEquals("public info not set?", publicInfo, b.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info not set?", privateInfo, b.get(Bulletin.TAGPRIVATEINFO));
		b.clear();
		assertEquals("public info not cleared?", "", b.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info not cleared?", "", b.get(Bulletin.TAGPRIVATEINFO));
	}

	public void testGetFieldType()
	{
		FieldSpec[] standardPublicFields = FieldSpec.getDefaultPublicFieldSpecs();
		for (int i = 0; i < standardPublicFields.length; i++)
		{
			FieldSpec spec = standardPublicFields[i];
			if(spec.getTag().equals("summary"))
				assertEquals(spec.getTag(), FieldSpec.TYPE_MULTILINE, spec.getType());
			else if(spec.getTag().equals("publicinfo"))
				assertEquals(spec.getTag(), FieldSpec.TYPE_MULTILINE, spec.getType());
			else if(spec.getTag().equals("eventdate"))
				assertEquals(spec.getTag(), FieldSpec.TYPE_DATERANGE, spec.getType());
			else if(spec.getTag().equals("entrydate"))
				assertEquals(spec.getTag(), FieldSpec.TYPE_DATE, spec.getType());
			else if(spec.getTag().equals("language"))
				assertEquals(spec.getTag(), FieldSpec.TYPE_CHOICE, spec.getType());
			else
				assertEquals(spec.getTag(), FieldSpec.TYPE_NORMAL, spec.getType());
		}

		FieldSpec[] standardPrivateFields = FieldSpec.getDefaultPrivateFieldSpecs();
		for (int i = 0; i < standardPrivateFields.length; i++)
		{
			FieldSpec spec = standardPrivateFields[i];
			if(spec.getTag().equals("privateinfo"))
				assertEquals(spec.getTag(), FieldSpec.TYPE_MULTILINE, spec.getType());
			else
				assertEquals(spec.getTag(), FieldSpec.TYPE_NORMAL, spec.getType());
		}
	}

	public void testEncryptPublicData() throws Exception
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
		Bulletin b = new Bulletin(security);
		b.setSealed();
		b.setAllPrivate(false);
		BulletinSaver.saveToClientDatabase(b, db, true, security);
		assertEquals("Didn't Encrypt or Encyrpted too many packets.", 1, db.encryptWasCalled);
	}

	public void testGetStatus() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		b1.setSealed();
		assertEquals("Not Sealed Status?", BulletinConstants.STATUSSEALED, b1.get(Bulletin.TAGSTATUS));
		b1.setDraft();
		assertEquals("Not Draft Status?", BulletinConstants.STATUSDRAFT, b1.get(Bulletin.TAGSTATUS));
	}

	public void testPullFrom() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		b1.setSealed();
		BulletinSaver.saveToClientDatabase(b1, db, true, security);
		Bulletin b2 = new Bulletin(security);
		b2.createDraftCopyOf(b1, db);
		assertEquals("Not a draft?", Bulletin.STATUSDRAFT, b2.getStatus());
		assertEquals("signer", b1.getSignatureGenerator(), b2.getSignatureGenerator());
		assertEquals("id unchanged", false, b2.getLocalId().equals(b1.getLocalId()));
		assertEquals("public info", b1.get(Bulletin.TAGPUBLICINFO), b2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info", b1.get(Bulletin.TAGPRIVATEINFO), b2.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("wrong status?", Bulletin.STATUSDRAFT, b2.getStatus());
		assertEquals("wrong private?", b1.isAllPrivate(), b2.isAllPrivate());

		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		b1.addPublicAttachment(a1);

		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b1.addPrivateAttachment(a2);

		b2.createDraftCopyOf(b1, db);
		assertEquals("public attachment count", 1, b2.getPublicAttachments().length);
		assertEquals("private attachment count", 1, b2.getPrivateAttachments().length);
		AttachmentProxy clonedPublicAttachment = b2.getPublicAttachments()[0];
		assertEquals("public attachment1 data", a1, clonedPublicAttachment);
		AttachmentProxy clonedPrivateAttachment = b2.getPrivateAttachments()[0];
		assertEquals("private attachment data", a2, clonedPrivateAttachment);
		b2.createDraftCopyOf(b1, db);
		assertEquals("again public attachment count", 1, b2.getPublicAttachments().length);
		assertEquals("again private attachment count", 1, b2.getPrivateAttachments().length);
		assertEquals("again public attachment1 data", a1, clonedPublicAttachment);
		assertEquals("again private attachment data", a2, clonedPrivateAttachment);

		b1.setAllPrivate(false);
		b2.createDraftCopyOf(b1, db);
		assertEquals("didn't pull private false?", b1.isAllPrivate(), b2.isAllPrivate());

		b1.setAllPrivate(true);
		b2.createDraftCopyOf(b1, db);
		assertEquals("didn't pull private true?", b1.isAllPrivate(), b2.isAllPrivate());

		BulletinSaver.saveToClientDatabase(b1,db,false,security);

		b2.createDraftCopyOf(b1,db);
		clonedPublicAttachment = b2.getPublicAttachments()[0];
		clonedPrivateAttachment = b2.getPrivateAttachments()[0];
		assertNotEquals("didn't clone the public attachment?", a1.getUniversalId().getLocalId(), clonedPublicAttachment.getUniversalId().getLocalId());
		assertNotEquals("didn't clone the private attachment?", a2.getUniversalId().getLocalId(), clonedPrivateAttachment.getUniversalId().getLocalId());
		assertEquals("Public attachment label not the same?", a1.getLabel(), clonedPublicAttachment.getLabel());
		assertEquals("Private attachment label not the same?", a2.getLabel(), clonedPrivateAttachment.getLabel());
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

	public void testDateRangeCompatibility() throws Exception
	{
		String sampleDateRange = "2003-04-07,2003-05-17";
		DateFormat df = Bulletin.getStoredDateFormat();
		Date d = df.parse(sampleDateRange);
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		assertEquals(2003, year);
		assertEquals(3, month);
		assertEquals(7, day);
	}
		

	public void testGetToday()
	{
		DateFormat df = Bulletin.getStoredDateFormat();
		String result = df.format(new Date());
		assertEquals(result, Bulletin.getToday());
	}

	public void testAddAttachment() throws Exception
	{
		Bulletin b = new Bulletin(security);
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

	public void testGetAndSetHQPublicKey()
	{
		Bulletin original = new Bulletin(security);
		assertEquals("HQKey already set?", "", original.getHQPublicKey());
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		String key = "12345";
		original.setHQPublicKey(key);
		assertEquals("HQKey not set?", key, original.getHQPublicKey());
		assertEquals("HQKey not set in public?", key, original.getFieldDataPacket().getHQPublicKey());
		assertEquals("HQKey not set in private?", key, original.getPrivateFieldDataPacket().getHQPublicKey());
	}

	static final String samplePublic = "some public text";
	static final String samplePrivate = "a bit of private text";

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

	static MockDatabase db;
	static MartusSecurity security;
}
