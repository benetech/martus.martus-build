package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

public class TestPacket extends TestCaseEnhanced
{
    public TestPacket(String name)
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
    }
    
	public void testBasics()
	{
		Packet packet = new Packet();
	}
	
	public void testWriteXmlToStream() throws Exception
	{
		Packet packet = new Packet();
		try
		{
			packet.writeXml((OutputStream)null, null);
			fail("Should have been an exception");
		}
		catch(Exception e)
		{
			//Expected Exception	
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		packet.writeXml(out, security);
		out.close();
		byte[] bytes = out.toByteArray();

		String result = new String(bytes, "UTF-8");
		assertStartsWith(MartusXml.packetStartComment, result);
		assertContains(packet.getLocalId(), result);
		assertContains(packet.getAccountId(), result);
		assertContains(MartusXml.packetSignatureStart, result);
		assertContains(MartusXml.packetSignatureEnd, result);
		//System.out.println(result);
		
		String newLine = "\n";
		int sigCommentIndex = result.indexOf(MartusXml.packetSignatureStart);
		int sigCommentEndLen = MartusXml.packetSignatureEnd.length();
		int sigCommentEndIndex = bytes.length - MartusXml.packetSignatureEnd.length() - newLine.length();
		int sigCommentLen = sigCommentEndIndex - sigCommentIndex;

		String sigComment = new String(bytes, sigCommentIndex, sigCommentLen + sigCommentEndLen, "UTF-8");
		//System.out.println(sigComment);
		assertStartsWith("bad sig start?", MartusXml.packetSignatureStart, sigComment);
		assertEndsWith("bad sig end?", MartusXml.packetSignatureEnd, sigComment);
		int sigIndex = MartusXml.packetSignatureStart.length();
		int sigEndIndex = sigComment.length() - MartusXml.packetSignatureEnd.length();
		String sig = sigComment.substring(sigIndex, sigEndIndex);
		//System.out.println(sig);
	}
	
	public void testWriteXmlToWriter() throws Exception
	{
		Packet packet = new Packet();
		try
		{
			packet.writeXml((Writer)null, null);
			fail("Should have been an exception");
		}
		catch(Exception e)
		{
			//Expected Exception	
		}
		StringWriter writer = new StringWriter();
		packet.writeXml(writer, security);
		String result = writer.toString();
		assertStartsWith(MartusXml.packetStartComment, result);
		assertContains(packet.getLocalId(), result);
		assertContains(packet.getAccountId(), result);
	}
	
	public void testWriteAndLoadUtf8() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		String utf8Data = "ßÑñú";
		bhp.setFieldDataPacketId(utf8Data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		bhp.setFieldDataPacketId("");
		byte[] bytes = out.toByteArray();
		String s8859 = new String(bytes);
		String sUtf8 = new String(bytes, "UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		bhp.loadFromXml(in, null, security);
		
		assertEquals("utf-8 damaged?", utf8Data, bhp.getFieldDataPacketId());
	}
	
	public void testLoadMoreSpecificPacketType() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		bhp.setFieldDataPacketId("none");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] sig = bhp.writeXml(out, security);
		
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(out.toByteArray());
		Packet.validateXml(in, security.getPublicKeyString(), bhp.getLocalId(), sig, security);
		try
		{
			ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(out.toByteArray());
			Packet.validateXml(in2, security.getPublicKeyString(), "123444", null, security);
			fail("Didn't throw for bad localid?");
		}
		catch (Packet.InvalidPacketException expectedException)
		{
		}

		try
		{
			sig[sig.length/2] ^= 0xFF;
			ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(out.toByteArray());
			Packet.validateXml(in2, security.getPublicKeyString(), bhp.getLocalId(), sig, security);
			fail("Didn't throw for bad sig?");
		}
		catch (Packet.SignatureVerificationException expectedException)
		{
		}
	}

	public void testCorruptedXML() throws Exception
	{
		class CorruptedBhp extends BulletinHeaderPacket
		{
			public CorruptedBhp(String accountString)
			{
				super(createUniversalId(accountString));
			}

			protected void internalWriteXml(XmlWriterFilter dest) throws IOException
			{
				dest.writeDirect("<");
				super.internalWriteXml(dest);
			}
		}
		
		CorruptedBhp bhp = new CorruptedBhp(security.getPublicKeyString());
		bhp.setFieldDataPacketId("none");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		try
		{
			ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(out.toByteArray());
			Packet.validateXml(in, security.getPublicKeyString(), bhp.getLocalId(), null, security);
			fail("Didn't throw for bad xml?");
		}
		catch(Packet.InvalidPacketException expectedException)
		{
		}
	}
		
	public void testVerifyPacketWithNonPacketData() throws Exception
	{
		byte[] invalidBytes = {1,2,3};
		ByteArrayInputStreamWithSeek inInvalid = new ByteArrayInputStreamWithSeek(invalidBytes);
		try
		{
			Packet.verifyPacketSignature(inInvalid, security);
			fail("invalidBytes should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException e)
		{
			// expected exception
		}
	}
			
	public void testVerifyGoodPacket() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		bhp.setPrivateFieldDataPacketId("Josée");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] bytes = out.toByteArray();
		ByteArrayInputStreamWithSeek in0 = new ByteArrayInputStreamWithSeek(bytes);
		Packet.verifyPacketSignature(in0, security);
		assertEquals("UTF", "Josée",bhp.getPrivateFieldDataPacketId());
	}
	
	public void testVerifyGoodPacketWithAnotherAccount() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] bytes = out.toByteArray();
		ByteArrayInputStreamWithSeek in0 = new ByteArrayInputStreamWithSeek(bytes);
		security.createKeyPair(SHORTEST_LEGAL_KEY_SIZE);
		Packet.verifyPacketSignature(in0, security);
	}	

	public void testVerifyPacketWithCorruptedStartComment() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] bytes = out.toByteArray();
		bytes[5] ^= 0xFF;

		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
		try
		{
			Packet.verifyPacketSignature(in1, security);
			fail("verifyPacketSignature Should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}
	
	public void testVerifyPacketWithCorruptedSignatureComment() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] bytes = out.toByteArray();
		String xml = new String(bytes, "UTF-8");
		assertEquals("unicode in the sample?", bytes.length, xml.length());
		int sigStart = xml.indexOf(MartusXml.packetSignatureStart);

		int corruptSigStartAt = sigStart + 1;
		try
		{
			bytes[corruptSigStartAt] ^= 0xFF;
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted sigstart should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
			bytes[corruptSigStartAt] ^= 0xFF;
		}
		
		int corruptSigEndAt = bytes.length - 2;
		try
		{
			bytes[corruptSigEndAt] ^= 0xFF;
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted sigend should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}
	
	public void testVerifyPacketWithCorruptedData() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] bytes = out.toByteArray();

		int corruptDataAt = MartusXml.packetStartComment.length() + 5;
		try
		{
			bytes[corruptDataAt] ^= 0xFF;
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted data should have thrown SignatureVerificationException");
		}
		catch(Packet.SignatureVerificationException ignoreThisExpectedException)
		{
		}
	}
	
	public void testVerifyPacketWithCorruptedSignature() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] bytes = out.toByteArray();

		int corruptSigAt = bytes.length - MartusXml.packetSignatureEnd.length() - 10;
		try
		{
			bytes[corruptSigAt] = ' ';
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted data should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}
	
	public void testVerifyPacketWithNoAccountTag() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] oldBytes = out.toByteArray();
		String oldXml = new String(oldBytes,"UTF-8");
		
		//String newXml =	oldXml.replaceFirst(MartusXml.AccountElementName,"xxy");
		// rewrite above line in java 1.3 compatible form:
		String newXml = oldXml; // first assume no match found
		int idx = oldXml.indexOf(MartusXml.AccountElementName);
		if (idx >= 0)
			newXml = oldXml.substring(0, idx) + "xxy" + oldXml.substring(idx+MartusXml.AccountElementName.length());

		byte[] newBytes = newXml.getBytes("UTF-8");

		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(newBytes);
		try
		{
			Packet.verifyPacketSignature(in1, security);
			fail("verifyPacketSignature Should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}

	public void testVerifyPacketWithBadAccountElement() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security.getPublicKeyString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		
		byte[] oldBytes = out.toByteArray();
		String tagEnd = MartusXml.getTagEnd(MartusXml.AccountElementName);
		String oldXml = new String(oldBytes,"UTF-8");
		String newXml =	oldXml.replaceFirst(tagEnd, "\n" + tagEnd);
		byte[] newBytes = newXml.getBytes("UTF-8");

		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(newBytes);
		try
		{
			Packet.verifyPacketSignature(in1, security);
			fail("verifyPacketSignature Should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException e)
		{
			// expected exception
		}
	}

	public void testLoadFromEmptyStream() throws Exception
	{
		Class expected = new Packet.InvalidPacketException("a").getClass();
		verifyLoadException(new byte[0], expected);
	}
		
	public void testLoadFromWrongPacketType() throws Exception
	{
		SimplePacketSubtype packet = new SimplePacketSubtype();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		packet.writeXml(out, security);

		byte[] wrongType = out.toByteArray();
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(wrongType);
		try
		{
			AnotherSimplePacketSubtype loaded = new AnotherSimplePacketSubtype();
			loaded.loadFromXmlInternal(inputStream, null, security);
			fail("Should have thrown WrongPacketTypeException");
		}
		catch(Packet.WrongPacketTypeException e)
		{
			// expected exception
		}
	}
		
	public void testLoadFromInvalidPacket() throws Exception
	{
		Class expected = new Packet.InvalidPacketException("a").getClass();
		verifyLoadException(new byte[] {1,2,3}, expected);
		
		String xmlError = "<" + MartusXml.PacketElementName + ">" + 
					"</a></" + MartusXml.PacketElementName + ">";
		byte[] xmlErrorBytes = xmlError.getBytes();
		verifyLoadException(xmlErrorBytes, expected);
	}
	
	void verifyLoadException(byte[] input, Class expectedExceptionClass)
	{
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(input);
		try
		{
			Packet.validateXml(inputStream, security.getPublicKeyString(), "", null, security);
			fail("Should have thrown " + expectedExceptionClass.getName());
		}
		catch(Exception e)
		{
			assertEquals("Wrong exception type?", expectedExceptionClass, e.getClass());
		}
	}

	class SimplePacketSubtype extends Packet
	{
		SimplePacketSubtype()
		{
			super(UniversalId.createFromAccountAndPrefix(security.getPublicKeyString(), ""));
		}
		
		protected String getPacketRootElementName()
		{
			return "BogusPacket";
		}
		
	}
	
	class AnotherSimplePacketSubtype extends Packet
	{
		AnotherSimplePacketSubtype()
		{
			super(UniversalId.createFromAccountAndPrefix(security.getPublicKeyString(), ""));
		}
		
		protected String getPacketRootElementName()
		{
			return "AnotherBogusPacket";
		}
		
	}
	
	static MartusSecurity security;
	int SHORTEST_LEGAL_KEY_SIZE = 512;

}
