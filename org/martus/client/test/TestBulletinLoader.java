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

import java.io.File;

import org.martus.client.core.Bulletin;
import org.martus.client.core.BulletinZipImporter;
import org.martus.client.core.BulletinLoader;
import org.martus.client.core.BulletinSaver;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.Bulletin.DamagedBulletinException;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusXml;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockDatabase;
import org.martus.common.TestCaseEnhanced;

public class TestBulletinLoader extends TestCaseEnhanced
{

	public TestBulletinLoader(String name)
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

	public void testDetectFieldPacketWithWrongSig() throws Exception
	{
		Database db = store.getDatabase();

		Bulletin original = store.createEmptyBulletin();
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		original.set(Bulletin.TAGPRIVATEINFO, "private info");
		original.setSealed();
		original.save();

		Bulletin loaded = BulletinLoader.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not valid?", true, loaded.isValid());

		FieldDataPacket fdp = loaded.getFieldDataPacket();
		fdp.set(Bulletin.TAGPUBLICINFO, "different public!");
		boolean encryptPublicData = store.mustEncryptPublicData();
		fdp.writeXmlToDatabase(db, encryptPublicData, security);

		loaded = BulletinLoader.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not invalid?", false, loaded.isValid());
		assertEquals("private messed up?", original.get(Bulletin.TAGPRIVATEINFO), loaded.get(Bulletin.TAGPRIVATEINFO));
	}

	public void testDetectPrivateFieldPacketWithWrongSig() throws Exception
	{
		Database db = store.getDatabase();

		Bulletin original = store.createEmptyBulletin();
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		original.set(Bulletin.TAGPRIVATEINFO, "private info");
		original.setSealed();
		original.save();

		Bulletin loaded = BulletinLoader.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not valid?", true, loaded.isValid());

		FieldDataPacket fdp = loaded.getPrivateFieldDataPacket();
		fdp.set(Bulletin.TAGPRIVATEINFO, "different private!");
		boolean encryptPublicData = store.mustEncryptPublicData();
		fdp.writeXmlToDatabase(db, encryptPublicData, security);

		loaded = BulletinLoader.loadFromDatabase(store, new DatabaseKey(original.getUniversalId()));
		assertEquals("not invalid?", false, loaded.isValid());
		assertEquals("public messed up?", original.get(Bulletin.TAGPUBLICINFO), loaded.get(Bulletin.TAGPUBLICINFO));
	}

	public void testLoadFromDatabase() throws Exception
	{
		assertEquals(0, db.getAllKeys().size());

		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGPUBLICINFO, "public info");
		b.set(Bulletin.TAGPRIVATEINFO, "private info");
		b.setSealed();
		BulletinSaver.saveToDatabase(b, db, store.mustEncryptPublicData(), security);
		assertEquals("saved 1", 3, db.getAllKeys().size());

		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		Bulletin loaded = store.createEmptyBulletin();
		loaded = BulletinLoader.loadFromDatabase(store, key);
		assertEquals("id", b.getLocalId(), loaded.getLocalId());
		assertEquals("public info", b.get(Bulletin.TAGPUBLICINFO), loaded.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info", b.get(Bulletin.TAGPRIVATEINFO), loaded.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("status", b.getStatus(), loaded.getStatus());
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
		BulletinZipImporter.loadFromFile(loaded2, tempFile, security);
		assertEquals("Loaded Keys not the same?", original.getFieldDataPacket().getHQPublicKey(), loaded2.getFieldDataPacket().getHQPublicKey());
	}

	public void testLoadFromDatabaseEncrypted() throws Exception
	{
		assertEquals(0, db.getAllKeys().size());

		Bulletin b = store.createEmptyBulletin();
		b.setAllPrivate(true);
		BulletinSaver.saveToDatabase(b, db, store.mustEncryptPublicData(), security);
		assertEquals("saved 1", 3, db.getAllKeys().size());

		DatabaseKey key = new DatabaseKey(b.getUniversalId());
		Bulletin loaded = store.createEmptyBulletin();
		loaded = BulletinLoader.loadFromDatabase(store, key);
		assertEquals("id", b.getLocalId(), loaded.getLocalId());

		assertEquals("not private?", b.isAllPrivate(), loaded.isAllPrivate());
	}

	public void testLoadFromDatabaseDamaged() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGPUBLICINFO, samplePublic);
		b.set(Bulletin.TAGPRIVATEINFO, samplePrivate);
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
		final int positionAfterHeaderSig = packetContents.indexOf(MartusXml.packetStartCommentEnd);
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
		DatabaseKey headerKey = new DatabaseKey(b.getBulletinHeaderPacket().getUniversalId());
		Bulletin stillValid = BulletinLoader.loadFromDatabase(store, headerKey, security);
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
				BulletinLoader.loadFromDatabase(store, headerKey, security);
			}
			catch (DamagedBulletinException ignoreExpectedException)
			{
			}
			return;
		}

		Bulletin invalid = BulletinLoader.loadFromDatabase(store, headerKey, security);
		assertEquals(label + " not invalid?", false, invalid.isValid());
		assertEquals(label + " wrong uid?", b.getUniversalId(), invalid.getUniversalId());
		assertEquals(label + " wrong fdp account?", b.getAccount(), invalid.getFieldDataPacket().getAccountId());
		assertEquals(label + " wrong private fdp account?", b.getAccount(), invalid.getPrivateFieldDataPacket().getAccountId());
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		assertEquals(label + " wrong fdp localId?", bhp.getFieldDataPacketId(), invalid.getFieldDataPacket().getLocalId());
		assertEquals(label + " wrong private fdp localId?", bhp.getPrivateFieldDataPacketId(), invalid.getPrivateFieldDataPacket().getLocalId());
		assertEquals(label + " public info", expectedPublic, invalid.get(Bulletin.TAGPUBLICINFO));
		assertEquals(label + " private info", expectedPrivate, invalid.get(Bulletin.TAGPRIVATEINFO));
		assertEquals(label + " hq key", "", invalid.getHQPublicKey());
	}

	static final String samplePublic = "some public text for loading";
	static final String samplePrivate = "a bit of private text for loading";

	MockMartusApp app;
	static MockDatabase db;
	static BulletinStore store;
	static MartusSecurity security;
}
