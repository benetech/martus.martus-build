package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Arrays;



public class TestFieldDataPacket extends TestCaseEnhanced
{
	public TestFieldDataPacket(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		if(security == null)
		{
			security = new MartusSecurity();
			security.createKeyPair(SHORTEST_LEGAL_KEY_SIZE);
		}	
		if(securityHQ == null)
		{
			securityHQ = new MartusSecurity();
			securityHQ.createKeyPair(SHORTEST_LEGAL_KEY_SIZE);
		}	
		UniversalId uid = FieldDataPacket.createUniversalId(security.getPublicKeyString());
		fdp = new FieldDataPacket(uid, fieldTags);
	}

	public void testBasics()
	{
		assertEquals("getFieldCount", fieldTags.length, fdp.getFieldCount());
		assertEquals("nope", false, fdp.fieldExists("Nope"));
		assertEquals("plain tag", true, fdp.fieldExists(bTag));
		assertEquals("lower", true, fdp.fieldExists(bTag.toLowerCase()));
		assertEquals("upper", true, fdp.fieldExists(bTag.toUpperCase()));
		
		assertEquals("tag list", true, Arrays.equals(fieldTags, fdp.getFieldTags()));
		assertEquals("HQ Key not ''?", "", fdp.getHQPublicKey());
		String hqKey = "12345";
		fdp.setHQPublicKey(hqKey);
		assertEquals("HQ Key not the same?", hqKey, fdp.getHQPublicKey());
		fdp.clearAll();
		assertEquals("HQ Key not cleared?", "", fdp.getHQPublicKey());
	}
	
	public void testIsEmpty()
	{
		assertEquals("didn't start out empty?", true, fdp.isEmpty());
		fdp.set(fieldTags[0], "blah");
		assertEquals("still empty after field?", false, fdp.isEmpty());
		fdp.clearAll();
		assertEquals("didn't return to empty after field?", true, fdp.isEmpty());

		UniversalId uid = UniversalId.createDummyUniversalId();
		AttachmentProxy a = new AttachmentProxy(uid, "label", null);
		fdp.addAttachment(a);
		assertEquals("still empty after attachment?", false, fdp.isEmpty());
		fdp.clearAll();
		assertEquals("didn't return to empty after attachment?", true, fdp.isEmpty());
	}
	
	public void testCreateUniversalId()
	{
		String sampleAccount = "an account";
		UniversalId uid = FieldDataPacket.createUniversalId(sampleAccount);
		assertEquals("account", sampleAccount, uid.getAccountId());
		assertStartsWith("prefix", "F-", uid.getLocalId());
	}
	
	public void testIsEncrypted()
	{
		assertEquals("already encrypted?", false, fdp.isEncrypted());
		fdp.setEncrypted(true);
		assertEquals("not encrypted?", true, fdp.isEncrypted());
		fdp.setEncrypted(false);
		assertEquals("still encrypted?", false, fdp.isEncrypted());
	}
	
	public void testIsPublicData()
	{
		assertEquals("already not Public?", true, fdp.isPublicData());
		fdp.setEncrypted(true);
		assertEquals("still Public?", false, fdp.isPublicData());
		fdp.setEncrypted(false);
		assertEquals("not Back to Public?", true, fdp.isPublicData());
	}
		
	
	public void testConstructorWithUniversalId()
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(fdp.getAccountId(), fdp.getLocalId());
		FieldDataPacket p = new FieldDataPacket(uid, fieldTags);
		assertEquals("account", fdp.getAccountId(), p.getAccountId());
		assertEquals("packet", fdp.getLocalId(), p.getLocalId());
		// check some other fields here
	}

	public void testGetSet()
	{
		assertEquals("", fdp.get("NoSuchField"));
		fdp.set("NoSuchField", "hello");
		assertEquals("", fdp.get("NoSuchField"));

		assertEquals("", fdp.get(aTag));
		fdp.set(aTag, "hello");
		assertEquals("hello", fdp.get(aTag));
		assertEquals("hello", fdp.get(aTag));
		assertEquals("hello", fdp.get(aTag));

		fdp.set(aTag.toUpperCase(), "another");
		assertEquals("another", fdp.get(aTag));

		fdp.set(bTag, "94404");
		assertEquals("94404", fdp.get(bTag));
		assertEquals("after setting other field", "another", fdp.get(aTag));
		fdp.set(aTag, "goodbye");
		assertEquals("goodbye", fdp.get(aTag));
	}
	
	public void testClear()
	{
		fdp.set(aTag, "hello");
		assertEquals("hello", fdp.get(aTag));
		fdp.clearAll();
		assertEquals("",fdp.get(aTag));
	}
	
	public void testAttachments()
	{
		String label1 = "Label 1";
		assertEquals("not none?", 0, fdp.getAttachments().length);
		fdp.addAttachment(new AttachmentProxy(label1));
		AttachmentProxy[] v = fdp.getAttachments();
		assertEquals("not one?", 1, v.length);
		assertEquals("wrong label", label1, v[0].getLabel());
		
	}
	
	public void testLoadFromXmlSimple() throws Exception
	{
		String account = "asbid";
		String id = "1234567";
		String data1 = "data 1í";
		String simpleFieldDataPacket = 
			"<FieldDataPacket>\n" + 
			"<" + MartusXml.PacketIdElementName + ">" + id + 
			"</" + MartusXml.PacketIdElementName + ">\n" + 
			"<" + MartusXml.AccountElementName + ">" + account + 
			"</" + MartusXml.AccountElementName + ">\n" + 
			"<" + MartusXml.EncryptedFlagElementName + ">" +
			"</" + MartusXml.EncryptedFlagElementName + ">" +
			"<" + MartusXml.FieldElementPrefix + aTag + ">" + data1 + 
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		fdp.loadFromXml(in, (MartusCrypto)null);
		assertEquals("account", account, fdp.getAccountId());
		assertEquals("id", id, fdp.getLocalId());
		assertEquals("aTag", data1, fdp.get(aTag));
		assertEquals("encrypted", true, fdp.isEncrypted());
	}
	
	public void testLoadFromXmlWithSpaces() throws Exception
	{
		String id = "1234567";
		String data1 = "  simple  ";
		String data2 = "This has  \nsome";
		String data3 = "plain\n  spaces";
		String simpleFieldDataPacket = 
			"<FieldDataPacket>\n" + 
			"<" + MartusXml.PacketIdElementName + ">" + id + 
			"</" + MartusXml.PacketIdElementName + ">\n" + 
			"<" + MartusXml.FieldElementPrefix + aTag + ">" + 
			data1 + 
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + bTag + ">" + 
			data2 + 
			"</" + MartusXml.FieldElementPrefix + bTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + cTag + ">" + 
			data3 + 
			"</" + MartusXml.FieldElementPrefix + cTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		fdp.loadFromXml(in, (MartusCrypto)null);
		assertEquals("id", id, fdp.getLocalId());
		assertEquals("aTag", data1, fdp.get(aTag));
		assertEquals("bTag", data2, fdp.get(bTag));
		assertEquals("cTag", data3, fdp.get(cTag));
	}
	
	public void testLoadFromXmlWithNewlines() throws Exception
	{
		String id = "1234567";
		String data1 = "leading\n    spaces";
		String data2 = "trailing newlines\n\n\n\n";
		String data3cr = "crlf\r\npairs\r\n";
		String data3 = "crlf\npairs\n";
		String simpleFieldDataPacket = 
			"<FieldDataPacket>\n" + 
			"<" + MartusXml.PacketIdElementName + ">" + id + 
			"</" + MartusXml.PacketIdElementName + ">\n" + 
			"<" + MartusXml.FieldElementPrefix + aTag + ">" + 
			data1 + 
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + bTag + ">" + 
			data2 +
			"</" + MartusXml.FieldElementPrefix + bTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + cTag + ">" + 
			data3cr + 
			"</" + MartusXml.FieldElementPrefix + cTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		fdp.loadFromXml(in, (MartusCrypto)null);
		assertEquals("id", id, fdp.getLocalId());
		assertEquals("aTag", data1, fdp.get(aTag));
		assertEquals("bTag", data2, fdp.get(bTag));
		assertEquals("cTag", data3, fdp.get(cTag));
	}

	public void testLoadFromXmlWithAmps() throws Exception
	{
		String id = "1234567";
		String data1amp = "&lt;tag&gt;";
		String data1 = "<tag>";
		String data2amp = "&amp;&amp;";
		String data2 = "&&";
		String data3 = "'\"'\"\\";
		String simpleFieldDataPacket = 
			"<FieldDataPacket>\n" + 
			"<" + MartusXml.PacketIdElementName + ">" + id + 
			"</" + MartusXml.PacketIdElementName + ">\n" + 
			"<" + MartusXml.FieldElementPrefix + aTag + ">" + 
			data1amp + 
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + bTag + ">" + 
			data2amp + 
			"</" + MartusXml.FieldElementPrefix + bTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + cTag + ">" + 
			data3 + 
			"</" + MartusXml.FieldElementPrefix + cTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		fdp.loadFromXml(in, (MartusCrypto)null);
		assertEquals("id", id, fdp.getLocalId());
		assertEquals("aTag", data1, fdp.get(aTag));
		assertEquals("bTag", data2, fdp.get(bTag));
		assertEquals("cTag", data3, fdp.get(cTag));
	}

	public void testWriteXml() throws Exception
	{
		String data1 = "data 1";
		String data2base = "data 2";
		fdp.set(aTag, data1);
		fdp.set(bTag, data2base + "&<>");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();

		assertContains(MartusXml.getTagStart(MartusXml.FieldDataPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.FieldDataPacketElementName), result);
		assertContains(fdp.getLocalId(), result);
		assertNotContains("encrypted?", MartusXml.EncryptedFlagElementName, result);

		assertContains(aTag.toLowerCase(), result);
		assertContains(bTag.toLowerCase(), result);
		assertContains(data1, result);
		assertContains(data2base + xmlAmp + xmlLt + xmlGt, result);
			
	}
	
	public void testWriteAndLoadXml() throws Exception
	{
		String account = fdp.getAccountId();
		String data1 = "  some  \n\n whitespace \n\n";
		String data2 = "<&>";
		String data3 = "";
		UniversalId uid1 = UniversalId.createFromAccountAndPrefix(account, "A");
		UniversalId uid2 = UniversalId.createFromAccountAndPrefix(account, "A");
		AttachmentProxy attach1 = new AttachmentProxy(new File("attachment 1"));
		AttachmentProxy attach2 = new AttachmentProxy(new File("attachmenté 2"));
		attach1.setUniversalIdAndSessionKey(uid1, security.createSessionKey());
		attach2.setUniversalIdAndSessionKey(uid2, security.createSessionKey());
		fdp.set(aTag, data1);
		fdp.set(bTag, data2);
		fdp.set(cTag, data3);
		fdp.addAttachment(attach1);
		fdp.addAttachment(attach2);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();
		
		int attachmentUidAt = result.indexOf(MartusXml.AttachmentLocalIdElementName);
		int attachmentKeyAt = result.indexOf(MartusXml.AttachmentKeyElementName);
		int attachmentLabelAt = result.indexOf(MartusXml.AttachmentLabelElementName);
		assertNotContains(MartusXml.getTagStart(MartusXml.HQSessionKeyElementName), result);
		
		assertTrue("uid after label?", attachmentUidAt < attachmentLabelAt);
		assertTrue("key after label?", attachmentKeyAt < attachmentLabelAt);

		StringReader reader = new StringReader(result);

		UniversalId uid = UniversalId.createDummyUniversalId();
		FieldDataPacket got = new FieldDataPacket(uid, fieldTags);

		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		got.loadFromXml(in, security);
		
		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("c", fdp.get(cTag), got.get(cTag));

		AttachmentProxy[] attachments = got.getAttachments();
		assertEquals("Attachment count", 2, attachments.length);
		AttachmentProxy got1 = attachments[0];
		AttachmentProxy got2 = attachments[1];
		assertEquals("A1 label incorrect?", attach1.getLabel(), got1.getLabel());
		assertEquals("A2 label incorrect?", attach2.getLabel(), got2.getLabel());
		assertEquals("A1 uid incorrect?", uid1, got1.getUniversalId());
		assertEquals("A2 uid incorrect?", uid2, got2.getUniversalId());
		assertNotNull("A1 key null?", got1.getSessionKeyBytes());
		assertNotNull("A2 key null?", got2.getSessionKeyBytes());
		assertEquals("A1 key incorrect?", true, Arrays.equals(attach1.getSessionKeyBytes(), got1.getSessionKeyBytes()));
		assertEquals("A2 key incorrect?", true, Arrays.equals(attach2.getSessionKeyBytes(), got2.getSessionKeyBytes()));
		
	}

	public void testWriteAndLoadXmlEncrypted() throws Exception
	{
		fdp.setEncrypted(true);
		
		String data1 = "data 1";
		String data2base = "data 2";
		fdp.set(aTag, data1);
		fdp.set(bTag, data2base + "&<>");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();

		assertContains(MartusXml.getTagStart(MartusXml.FieldDataPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.FieldDataPacketElementName), result);
		assertContains(fdp.getLocalId(), result);
		assertContains("not encrypted?", MartusXml.EncryptedFlagElementName, result);
		assertNotContains(MartusXml.getTagStart(MartusXml.HQSessionKeyElementName), result);

		assertNotContains("encrypted data visible1?", aTag.toLowerCase(), result);
		assertNotContains("encrypted data visible2?", bTag.toLowerCase(), result);
		assertNotContains("encrypted data visible3?", data1, result);
		assertNotContains("encrypted data visible4?", data2base + xmlAmp + xmlLt + xmlGt, result);
		
		UniversalId uid = UniversalId.createFromAccountAndPrefix("other acct", "");
		FieldDataPacket got = new FieldDataPacket(uid, fdp.getFieldTags());
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		got.loadFromXml(in, security);

		assertEquals("account", fdp.getAccountId(), got.getAccountId());
		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("encrypted", fdp.isEncrypted(), got.isEncrypted());
			
	}
	

	public void testWriteAndLoadXmlEncryptedWithHQ() throws Exception
	{
		fdp.setEncrypted(true);
		fdp.setHQPublicKey(securityHQ.getPublicKeyString());
		
		String data1 = "data 1";
		String data2base = "data 2";
		fdp.set(aTag, data1);
		fdp.set(bTag, data2base + "&<>");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();

		assertContains(MartusXml.getTagStart(MartusXml.HQSessionKeyElementName), result);
		
		UniversalId uid = UniversalId.createFromAccountAndPrefix("other acct", "");
		FieldDataPacket got = new FieldDataPacket(uid, fdp.getFieldTags());
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		got.loadFromXml(in, securityHQ);

		assertEquals("account", fdp.getAccountId(), got.getAccountId());
		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("encrypted", fdp.isEncrypted(), got.isEncrypted());
		
		ByteArrayInputStream in2 = new ByteArrayInputStream(bytes);
		got.loadFromXml(in2, security);
		assertEquals("account", fdp.getAccountId(), got.getAccountId());

		MartusSecurity otherSecurity = new MartusSecurity();
		otherSecurity.createKeyPair(SHORTEST_LEGAL_KEY_SIZE);
		try
		{
			ByteArrayInputStream in3 = new ByteArrayInputStream(bytes);
			got.loadFromXml(in3, otherSecurity);
			fail("Should have thrown decrption exception");
		}
		catch (MartusCrypto.DecryptionException expectedException)
		{
		}
	}

//	public void testAttachments()
//	{
//		String id1 = "235235";
//		String id2 = "29836982";
//		String label1 = "This is a test";
//		String label2 = "Why not do something?";
//		
//		AttachmentInfo a1 = new AttachmentInfo(id1, label1);
//		AttachmentInfo a2 = new AttachmentInfo(id2, label2);
//		
//	}
		
	void verifyLoadException(byte[] input, Class expectedExceptionClass)
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		try
		{
			UniversalId uid = UniversalId.createDummyUniversalId();
			FieldDataPacket loaded = new FieldDataPacket(uid, fieldTags);
			loaded.loadFromXml(inputStream, security);
			fail("Should have thrown " + expectedExceptionClass.getName());
		}
		catch(Exception e)
		{
			assertEquals("Wrong exception type?", expectedExceptionClass, e.getClass());
		}
	}
	
	String line1 = "This";
	String line2 = "is";
	String line3 = "data";
	String line4 = "for b";
	
	FieldDataPacket fdp;
	String xmlAmp = "&amp;";
	String xmlLt = "&lt;";
	String xmlGt = "&gt;";
	String aTag = "aMonte";
	String bTag = "Blue";
	String cTag = "cSharp";
	String aData = "data for a";
	String bData = line1 + "\n" + line2 + "\r\n" + line3 + "\n" + line4;
	String cData = "after b";
	String[] fieldTags = {aTag, bTag, cTag};

	int SHORTEST_LEGAL_KEY_SIZE = 512;
	static MartusSecurity security;
	static MartusSecurity securityHQ;
}
