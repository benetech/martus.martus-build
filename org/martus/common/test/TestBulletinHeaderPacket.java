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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.martus.common.MartusXml;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.util.Base64;
import org.martus.util.ByteArrayInputStreamWithSeek;

public class TestBulletinHeaderPacket extends TestCaseEnhanced
{
	public TestBulletinHeaderPacket(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		if(security == null)
		{
			int SHORTEST_LEGAL_KEY_SIZE = 512;
			security = new MartusSecurity();
			security.createKeyPair(SHORTEST_LEGAL_KEY_SIZE);
		}
		if(bhp == null)
			bhp = new BulletinHeaderPacket(security.getPublicKeyString());
	}

	public void testCreateUniversalId()
	{
		String sampleAccount = "an account";
		UniversalId uid = BulletinHeaderPacket.createUniversalId(sampleAccount);
		assertEquals("account", sampleAccount, uid.getAccountId());
		assertStartsWith("prefix", "B-", uid.getLocalId());
	}

	public void testPrefix()
	{
		assertEquals("not legal?", true, BulletinHeaderPacket.isValidLocalId("B-12345"));
		assertEquals("was legal?", false, BulletinHeaderPacket.isValidLocalId("F-12345"));
	}

	public void testConstructorWithId()
	{
		final String accountId = "some account id";
		final String packetId = "some local id";
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, packetId);
		BulletinHeaderPacket p = new BulletinHeaderPacket(uid);
		assertEquals("accountId?", accountId, p.getAccountId());
		assertEquals("packetId?", packetId, p.getLocalId());
	}

	public void testGetFieldDataPacketId()
	{
		String sampleId = "this is a valid id. really.";
		assertNull("data not null?", bhp.getFieldDataPacketId());
		bhp.setFieldDataPacketId(sampleId);
		assertEquals(sampleId, bhp.getFieldDataPacketId());

		String privateId = "private data id";
		assertNull("private data not null?", bhp.getPrivateFieldDataPacketId());
		bhp.setPrivateFieldDataPacketId(privateId);
		assertEquals(privateId, bhp.getPrivateFieldDataPacketId());
	}

	public void testAddAndGetAttachments()
	{
		assertEquals("count before adding public", 0, bhp.getPublicAttachmentIds().length);
		assertEquals("count before adding private", 0, bhp.getPrivateAttachmentIds().length);

		bhp.addPublicAttachmentLocalId(attachmentId1);
		String[] list1 = bhp.getPublicAttachmentIds();
		assertEquals("count after adding 1", 1, list1.length);
		assertEquals("list1 missing a1?", attachmentId1, list1[0]);
		assertEquals("private count after adding public", 0, bhp.getPrivateAttachmentIds().length);

		bhp.addPublicAttachmentLocalId(attachmentId2);
		String[] list2 = bhp.getPublicAttachmentIds();
		assertEquals("count after adding 2", 2, list2.length);
		assertEquals("list2 a1 in wrong position?", 1, Arrays.binarySearch(list2, attachmentId1));
		assertEquals("list2 a2 in wrong position?", 0, Arrays.binarySearch(list2, attachmentId2));

		bhp.addPublicAttachmentLocalId(attachmentId2);
		assertEquals("count after dupe", 2, bhp.getPublicAttachmentIds().length);
		assertEquals("private count after adding multiple publics", 0, bhp.getPrivateAttachmentIds().length);

		bhp.addPrivateAttachmentLocalId(attachmentId3);
		assertEquals("private count after adding 1 private", 1, bhp.getPrivateAttachmentIds().length);
		assertEquals("public count after adding private", 2, bhp.getPublicAttachmentIds().length);

		bhp.addPrivateAttachmentLocalId(attachmentId4);
		String[] list3 = bhp.getPublicAttachmentIds();
		String[] list4 = bhp.getPrivateAttachmentIds();
		assertEquals("private count after adding 2 private", 2, bhp.getPrivateAttachmentIds().length);
		assertEquals("public count after adding multiple privates", 2, bhp.getPublicAttachmentIds().length);
		assertEquals("public list3 a1 in wrong position?", 1, Arrays.binarySearch(list3, attachmentId1));
		assertEquals("public list3 a2 in wrong position?", 0, Arrays.binarySearch(list3, attachmentId2));
		assertEquals("private list4 a3 in wrong position?", 1, Arrays.binarySearch(list4, attachmentId3));
		assertEquals("private list4 a4 in wrong position?", 0, Arrays.binarySearch(list4, attachmentId4));

		bhp.clearAttachments();
		assertEquals("private count after clear", 0, bhp.getPrivateAttachmentIds().length);
		assertEquals("public count after clear", 0, bhp.getPublicAttachmentIds().length);

	}

	public void testStatus()
	{
		assertEquals("not empty to start?", "", bhp.getStatus());
		bhp.setStatus("abc");
		assertEquals("not set right?", "abc", bhp.getStatus());
	}

	public void testAllPrivate() throws Exception
	{
		UniversalId dummyUid = UniversalId.createDummyUniversalId();
		BulletinHeaderPacket unknownPrivacy = new BulletinHeaderPacket(dummyUid);
		assertEquals("knows privacy?", false, unknownPrivacy.hasAllPrivateFlag());
		unknownPrivacy.setAllPrivate(true);

		bhp.setAllPrivate(false);
		assertEquals("doesn't know privacy after set false?", true, bhp.hasAllPrivateFlag());
		assertEquals("private?", false, bhp.isAllPrivate());

		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		bhp.writeXml(out1, security);
		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(out1.toByteArray());
		BulletinHeaderPacket loadedBhp1 = new BulletinHeaderPacket(UniversalId.createDummyUniversalId());
		loadedBhp1.loadFromXml(in1, security);
		assertEquals("doesn't know privacy after loaded false?", true, loadedBhp1.hasAllPrivateFlag());
		assertEquals("private after load?", false, loadedBhp1.isAllPrivate());

		bhp.setAllPrivate(true);
		assertEquals("doesn't know privacy after set true?", true, bhp.hasAllPrivateFlag());
		assertEquals("not private?", true, bhp.isAllPrivate());

		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		bhp.writeXml(out2, security);
		ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(out2.toByteArray());
		BulletinHeaderPacket loadedBhp2 = new BulletinHeaderPacket(UniversalId.createDummyUniversalId());
		loadedBhp2.loadFromXml(in2, security);
		assertEquals("doesn't know privacy after loaded true?", true, loadedBhp2.hasAllPrivateFlag());
		assertEquals("not private after load?", true, loadedBhp2.isAllPrivate());

		String result2 = new String(out2.toByteArray(), "UTF-8");
		int startTagStart = result2.indexOf(MartusXml.AllPrivateElementName) - 1;
		int endTagEnd = result2.indexOf("/" + MartusXml.AllPrivateElementName) + MartusXml.AllPrivateElementName.length() + 1;
		String withoutTag = result2.substring(0, startTagStart) + result2.substring(endTagEnd);
		ByteArrayInputStreamWithSeek in3 = new ByteArrayInputStreamWithSeek(withoutTag.getBytes("UTF-8"));
		BulletinHeaderPacket loadedBhp3 = new BulletinHeaderPacket(UniversalId.createDummyUniversalId());
		loadedBhp3.setAllPrivate(true);
		loadedBhp3.loadFromXml(in3, null);
		assertEquals("knows privacy after loaded without tag?", false, loadedBhp3.hasAllPrivateFlag());
		assertEquals("not private after load without tag?", true, loadedBhp3.isAllPrivate());
	}

	public void testWriteXml() throws Exception
	{
		String dataId = "this data id";
		String privateId = "this data id";
		bhp.updateLastSavedTime();
		bhp.setFieldDataPacketId(dataId);
		bhp.setPrivateFieldDataPacketId(privateId);
		bhp.setFieldDataSignature(sampleSig1);
		bhp.setPrivateFieldDataSignature(sampleSig2);
		bhp.addPublicAttachmentLocalId(attachmentId1);
		bhp.addPublicAttachmentLocalId(attachmentId2);
		bhp.addPrivateAttachmentLocalId(attachmentId3);
		bhp.addPrivateAttachmentLocalId(attachmentId4);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(bhp.getLocalId(), result);

		assertContains(MartusXml.getTagStart(MartusXml.DataPacketIdElementName), result);
		assertContains(dataId, result);
		assertContains(MartusXml.getTagEnd(MartusXml.DataPacketIdElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.DataPacketSigElementName), result);
		assertContains("missing data sig?", Base64.encode(sampleSig1), result);
		assertContains(MartusXml.getTagEnd(MartusXml.DataPacketSigElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.PrivateDataPacketIdElementName), result);
		assertContains(privateId, result);
		assertContains(MartusXml.getTagEnd(MartusXml.PrivateDataPacketIdElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.PrivateDataPacketSigElementName), result);
		assertContains("missing private data sig?", Base64.encode(sampleSig2), result);
		assertContains(MartusXml.getTagEnd(MartusXml.PrivateDataPacketSigElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.PublicAttachmentIdElementName), result);
		assertContains(attachmentId1, result);
		assertContains(MartusXml.getTagEnd(MartusXml.PublicAttachmentIdElementName), result);

		assertContains(attachmentId2, result);

		assertContains(MartusXml.getTagStart(MartusXml.PrivateAttachmentIdElementName), result);
		assertContains(attachmentId3, result);
		assertContains(MartusXml.getTagEnd(MartusXml.PrivateAttachmentIdElementName), result);

		assertContains(attachmentId4, result);

		assertContains(Long.toString(bhp.getLastSavedTime()), result);

		assertNotContains(MartusXml.getTagStart(MartusXml.HQPublicKeyElementName), result);
	}

	public void testWriteXmlWithHQKeySet() throws Exception
	{
		String dataId = "this data id";
		String privateId = "this data id";
		String hqKey = "hqkey123";
		bhp.setHQPublicKey(hqKey);
		bhp.setFieldDataPacketId(dataId);
		bhp.setPrivateFieldDataPacketId(privateId);
		bhp.setFieldDataSignature(sampleSig1);
		bhp.setPrivateFieldDataSignature(sampleSig2);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.HQPublicKeyElementName), result);
	}

	public void testWriteXmlWithNoFieldData() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(bhp.getLocalId(), result);
	}

	public void testLoadXml() throws Exception
	{
		String dataId = "some id";
		String privateId = "private id";
		String sampleStatus = "draft or whatever";
		bhp.updateLastSavedTime();
		bhp.setHQPublicKey("");
		bhp.setStatus(sampleStatus);
		bhp.setFieldDataPacketId(dataId);
		bhp.setPrivateFieldDataPacketId(privateId);
		bhp.setFieldDataSignature(sampleSig1);
		bhp.setPrivateFieldDataSignature(sampleSig2);
		bhp.addPublicAttachmentLocalId(attachmentId1);
		bhp.addPublicAttachmentLocalId(attachmentId2);
		bhp.addPrivateAttachmentLocalId(attachmentId3);
		bhp.addPrivateAttachmentLocalId(attachmentId4);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket("");
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);

		assertEquals("time", bhp.getLastSavedTime(), loaded.getLastSavedTime());
		assertEquals("id", bhp.getLocalId(), loaded.getLocalId());
		assertEquals("data id", bhp.getFieldDataPacketId(), loaded.getFieldDataPacketId());
		assertEquals("private id", bhp.getPrivateFieldDataPacketId(), loaded.getPrivateFieldDataPacketId());
		assertEquals("status", sampleStatus, loaded.getStatus());
		assertEquals("data sig", true, Arrays.equals(sampleSig1, bhp.getFieldDataSignature()));
		assertEquals("private data sig", true, Arrays.equals(sampleSig2, bhp.getPrivateFieldDataSignature()));
		assertEquals("hqKey", "", loaded.getHQPublicKey());

		String[] list = loaded.getPublicAttachmentIds();
		assertEquals("public count", 2, list.length);
		assertEquals("public attachments wrong?", true, Arrays.equals(bhp.getPublicAttachmentIds(), list));
		String[] list2 = loaded.getPrivateAttachmentIds();
		assertEquals("private count", 2, list2.length);
		assertEquals("private attachments wrong?", true, Arrays.equals(bhp.getPrivateAttachmentIds(), list2));
	}

	public void testLoadXmlWithHQKey() throws Exception
	{
		String hqKey = "sdjflksj";
		bhp.setHQPublicKey(hqKey);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket("");
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);

		assertEquals("hqKey", bhp.getHQPublicKey(), loaded.getHQPublicKey());
	}

	byte[] sampleSig1 = {1,6,38,0};
	byte[] sampleSig2 = {7, 9, 9};
	final String attachmentId1 = "second alphabetically";
	final String attachmentId2 = "alphabetically first";
	final String attachmentId3 = "alphabetically after 4";
	final String attachmentId4 = "4 first";

	static BulletinHeaderPacket bhp;
	static MartusSecurity security;
}
