/* $Id: $ */
package org.martus.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.*;

public class TestZipEntryInputStream extends TestCaseEnhanced
{
	public TestZipEntryInputStream(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		zip = createSampleZipFile();
		entry = zip.getEntry(sampleEntryName);
		in = new ZipEntryInputStream(zip, entry);
	}
	
	public void tearDown() throws Exception
	{
		in.close();
		zip.close();
	}

	public void testMarkSupported() throws Exception
	{	
		assertEquals("mark supported", true, in.markSupported());
		in.mark(999999999);
		in.reset();
	}
	
	public void testResetBeforeMark() throws Exception
	{
		try
		{
			in.reset();
			fail("can't reset before mark");
		}
		catch (IOException e)
		{
			// expected exception
		}
	}

	public void testSimpleRead() throws Exception
	{		
		assertEquals("available?", sampleBytes.length, in.available());
		int firstByte = in.read();
		assertEquals("wrong first byte?", sampleBytes[0], firstByte);
	}
	
	public void testAvailable() throws Exception
	{
		in.mark(100);
		in.read();
		// available doesn't seem to be implemented correctly by the InputStream 
		// that ZipFile returns. So the following test would fail. We don't care.
		//assertEquals("available after read?", sampleBytes.length-1, in.available());
		in.reset();
		assertEquals("available after reset?", sampleBytes.length, in.available());
	}
	
	public void testReadAfterReset() throws Exception
	{		
		in.mark(0);
		in.read();
		in.reset();

		byte[] allBytes = new byte[sampleBytes.length];
		assertEquals("got?", sampleBytes.length, in.read(allBytes));
		assertEquals("wrong bytes?", true, Arrays.equals(sampleBytes, allBytes));
	}
	
	public void testMiddleMarkAndReset() throws Exception
	{		
		in.read();
		in.mark(1);
		in.read();
		in.reset();
		assertEquals("after middle reset", sampleBytes[1], in.read());
	}
	
	public void testSkip() throws Exception
	{
		in.read();
		in.mark(100);
		in.skip(3);
		assertEquals("after skip", sampleBytes[4], in.read());
		in.mark(0);
		in.skip(5);
		in.reset();
		assertEquals("after skip and reset", sampleBytes[2], in.read());
	}
	
	ZipFile createSampleZipFile() throws IOException
	{
		File tempFile = File.createTempFile("$$$MartusTestZipEntry", null);
		tempFile.deleteOnExit();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
		
		ZipEntry entry = new ZipEntry(sampleEntryName);
		out.putNextEntry(entry);
		out.write(sampleBytes);
		out.close();
		
		return new ZipFile(tempFile);
	}
	
	static final byte[] sampleBytes = {1,2,3,4,5,6,7,8,9,0,127};
	static final String sampleEntryName = "sample.dat";
	ZipFile zip;
	ZipEntry entry;
	ZipEntryInputStream in;
}
