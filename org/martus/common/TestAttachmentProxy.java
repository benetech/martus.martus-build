package org.martus.common;

import java.io.File;
import java.util.Arrays;

import org.martus.common.*;

public class TestAttachmentProxy extends TestCaseEnhanced
{
	public TestAttachmentProxy(String name)
	{
		super(name);
	}

	public void testFileProxy() throws Exception
	{
		File file = File.createTempFile("$$$TestAttachmentProxy", null);
		file.deleteOnExit();
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln("This is some text");
		writer.close();
		
		MartusCrypto security = new MockMartusSecurity();
		byte[] sessionKeyBytes = security.createSessionKey();
		
		AttachmentProxy a = new AttachmentProxy(file);
		assertEquals(file.getName(), a.getLabel());
		assertEquals("file", file, a.getFile());
		assertNull("not null key?", a.getSessionKeyBytes());
		
		UniversalId uid = UniversalId.createDummyUniversalId();
		assertNull("already has a uid?", a.getUniversalId());
		a.setUniversalIdAndSessionKey(uid, sessionKeyBytes);
		assertEquals("wrong uid?", uid, a.getUniversalId());
		assertEquals("wrong key?", true, Arrays.equals(sessionKeyBytes, a.getSessionKeyBytes()));
		assertNull("still has file?", a.getFile());
	}
	
	public void testUidProxy() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		String label = "label";
		AttachmentProxy a = new AttachmentProxy(uid, label, null);
		assertEquals("wrong uid?", uid, a.getUniversalId());
		assertEquals("wrong label?", label, a.getLabel());
		assertNull("has file?", a.getFile());
		
	}
	
	public void testStringProxy() throws Exception
	{
		String label = "label";
		AttachmentProxy a = new AttachmentProxy(label);
		assertEquals(label, a.getLabel());
		assertNull("file", a.getFile());
	}
}
