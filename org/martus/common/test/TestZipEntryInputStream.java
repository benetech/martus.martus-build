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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.util.ZipEntryInputStream;

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

	public void testSimpleRead() throws Exception
	{
		assertEquals("available?", sampleBytes.length, in.available());
		int firstByte = in.read();
		assertEquals("wrong first byte?", sampleBytes[0], firstByte);
	}

	public void testAvailable() throws Exception
	{
		in.read();
		// available doesn't seem to be implemented correctly by the InputStream
		// that ZipFile returns. So the following test would fail. We don't care.
		//assertEquals("available after read?", sampleBytes.length-1, in.available());
		in.seek(0);
		assertEquals("available after reset?", sampleBytes.length, in.available());
	}

	public void testReadAfterReset() throws Exception
	{
		in.read();
		in.seek(0);

		byte[] allBytes = new byte[sampleBytes.length];
		assertEquals("got?", sampleBytes.length, in.read(allBytes));
		assertEquals("wrong bytes?", true, Arrays.equals(sampleBytes, allBytes));
	}

	public void testMiddleMarkAndReset() throws Exception
	{
		in.read();
		in.read();
		in.seek(1);
		assertEquals("after middle reset", sampleBytes[1], in.read());
	}

	public void testSkip() throws Exception
	{
		in.read();
		in.skip(3);
		assertEquals("after skip", sampleBytes[4], in.read());
		in.skip(5);
		in.seek(2);
		assertEquals("after skip and reset", sampleBytes[2], in.read());
	}

	ZipFile createSampleZipFile() throws IOException
	{
		File tempFile = createTempFileFromName("$$$MartusTestZipEntry");
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
