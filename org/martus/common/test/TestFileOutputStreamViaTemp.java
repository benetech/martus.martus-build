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
import java.io.FileInputStream;
import java.util.Arrays;

import org.martus.util.FileOutputStreamViaTemp;

public class TestFileOutputStreamViaTemp extends TestCaseEnhanced
{
	public TestFileOutputStreamViaTemp(String name)
	{
		super(name);
	}


	public void testWhenFileExists() throws Exception
	{
		File destFile = createTempFile();
		FileOutputStreamViaTemp out = new FileOutputStreamViaTemp(destFile);
		byte[] sampleData = {1,2,3,4,5};
		out.write(sampleData);
		out.close();
		assertTrue("Didn't create dest?", destFile.exists());
		assertEquals("Wrong length?", sampleData.length, destFile.length());
		
		byte[] gotData = new byte[(int)destFile.length()];
		FileInputStream in = new FileInputStream(destFile);
		in.read(gotData);
		assertTrue("Wrong data?", Arrays.equals(sampleData, gotData));
		assertEquals("more data?", -1, in.read());
		in.close();
		
		destFile.delete();
	}

	public void testWhenFileExistsReadOnly() throws Exception
	{
		File destFile = createTempFile();
		destFile.setReadOnly();
		FileOutputStreamViaTemp out = new FileOutputStreamViaTemp(destFile);
		byte[] sampleData = {1,2,3,4,5};
		out.write(sampleData);
		out.close();
		assertTrue("Didn't create dest?", destFile.exists());
		assertEquals("Wrong length?", sampleData.length, destFile.length());
		
		byte[] gotData = new byte[(int)destFile.length()];
		FileInputStream in = new FileInputStream(destFile);
		in.read(gotData);
		assertTrue("Wrong data?", Arrays.equals(sampleData, gotData));
		assertEquals("more data?", -1, in.read());
		in.close();
		
		destFile.delete();
	}

	public void testWhenFileDoesntExist() throws Exception
	{
		File destFile = createTempFile();
		destFile.delete();
		FileOutputStreamViaTemp out = new FileOutputStreamViaTemp(destFile);
		assertFalse("Already created dest?", destFile.exists());
		byte[] sampleData = {1,2,3,4,5};
		out.write(sampleData);
		out.close();
		assertTrue("Didn't create dest?", destFile.exists());
		assertEquals("Wrong length?", sampleData.length, destFile.length());
		
		byte[] gotData = new byte[(int)destFile.length()];
		FileInputStream in = new FileInputStream(destFile);
		in.read(gotData);
		assertTrue("Wrong data?", Arrays.equals(sampleData, gotData));
		assertEquals("more data?", -1, in.read());
		in.close();
		
		destFile.delete();
	}
}
