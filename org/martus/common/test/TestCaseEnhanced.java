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
import java.util.Vector;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.martus.util.UnicodeWriter;

public class TestCaseEnhanced extends TestCase
{
	public TestCaseEnhanced(String name)
	{
		super(name);
	}

	public File createTempFile() throws IOException
	{
		final String tempFileName = "$$$" + getName();
		return createTempFileFromName(tempFileName);
	}

	public File createTempFileFromName(String name) throws IOException
	{
		File file = File.createTempFile(name, null);
		file.deleteOnExit();
		return file;
	}

	public File createTempFileWithData(String contents) throws IOException
	{
		File file = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(contents);
		writer.flush();
		writer.close();
		return file;
	}

	protected File createTempFileWithData(byte[] data) throws Exception
	{
		File tempFile = createTempFile();
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(data);
		out.close();
		return tempFile;
	}
	
	protected File createTempDirectory() throws IOException
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdirs();
		return dir;
	}


	public static void assertFalse(String message, boolean actual)
	{
		if(actual)
			throw new AssertionFailedError(message + " expected false ");
	}

	public static void assertNotEquals(long expected, long actual)
	{
		if(actual == expected)
			throw new AssertionFailedError("Expected anything other than " + expected);
	}

	public static void assertNotEquals(String message, long expected, long actual)
	{
		if(actual == expected)
			throw new AssertionFailedError(message + "Expected anything other than " + expected);
	}

	public static void assertNotEquals(String expected, String actual)
	{
		if(expected.equals(actual))
			throw new AssertionFailedError("Expected anything other than " + expected);
	}

	public static void assertNotEquals(String message, Object expected, Object actual)
	{
		if(expected.equals(actual))
			throw new AssertionFailedError(message + ": Expected anything other than " + expected);
	}

	public static void assertContains(String expected, String container)
	{
		assertNotNull(expected);
		if (container.indexOf(expected) == -1)
			throw new AssertionFailedError("<" + expected + ">" + " not found in " + "<" + container + ">");
	}

	public static void assertContains(String message, String expected, String container)
	{
		assertNotNull(expected);
		if (container.indexOf(expected) == -1)
			throw new AssertionFailedError(message + ": " + "<" + expected + ">" +
											" not found in " + "<" + container + ">");
	}

	public static void assertNotContains(String unexpected, String container)
	{
		assertNotNull(unexpected);
		if (container.indexOf(unexpected) != -1)
			throw new AssertionFailedError("<" + unexpected + ">" + " WAS found in " + "<" + container + ">");
	}

	public static void assertNotContains(String message, String unexpected, String container)
	{
		assertNotNull(unexpected);
		if (container.indexOf(unexpected) != -1)
			throw new AssertionFailedError(message + ": " + "<" + unexpected + ">" +
											" WAS found in " + "<" + container + ">");
	}

	public static void assertContains(Object unexpected, Vector container)
	{
		assertNotNull(unexpected);
		if (!container.contains(unexpected))
			throw new AssertionFailedError("<" + unexpected + ">" + " not found in " + "<" + container + ">");
	}

	public static void assertContains(String message, Object unexpected, Vector container)
	{
		assertNotNull(unexpected);
		if (!container.contains(unexpected))
			throw new AssertionFailedError(message + ": " + "<" + unexpected + ">" +
											" not found in " + "<" + container + ">");
	}

	public static void assertNotContains(Object unexpected, Vector container)
	{
		assertNotNull(unexpected);
		if (container.contains(unexpected))
			throw new AssertionFailedError("<" + unexpected + ">" + " WAS found in " + "<" + container + ">");
	}

	public static void assertNotContains(String message, Object unexpected, Vector container)
	{
		assertNotNull(unexpected);
		if (container.contains(unexpected))
			throw new AssertionFailedError(message + ": " + "<" + unexpected + ">" +
											" WAS found in " + "<" + container + ">");
	}

	public static void assertStartsWith(String expected, String container)
	{
		assertNotNull(expected);
		if (!container.startsWith(expected))
			throw new AssertionFailedError("<" + expected + ">" + " not at start of " + "<" + container + ">");
	}

	public static void assertStartsWith(String label, String expected, String container)
	{
		assertNotNull(expected);
		if (!container.startsWith(expected))
			throw new AssertionFailedError(label + ": <" + expected + ">" + " not at start of " + "<" + container + ">");
	}

	public static void assertEndsWith(String expected, String container)
	{
		assertNotNull(expected);
		if (!container.endsWith(expected))
			throw new AssertionFailedError("<" + expected + ">" + " not at end of " + "<" + container + ">");
	}

	public static void assertEndsWith(String label, String expected, String container)
	{
		assertNotNull(expected);
		if (!container.endsWith(expected))
			throw new AssertionFailedError(label + ": <" + expected + ">" + " not at end of " + "<" + container + ">");
	}

	public void TRACE_BEGIN(String method)
	{
		if(VERBOSE)
		{
			System.out.print(getClass() + "." + method + ": ");
			methodStartedAt = System.currentTimeMillis();
		}
	}

	public void TRACE_END()
	{
		if(VERBOSE)
			System.out.println(System.currentTimeMillis() - methodStartedAt);
	}

	public final static String BAD_FILENAME = "<>//\\..??**::||";

	private long methodStartedAt;
	public boolean VERBOSE = false;

}
