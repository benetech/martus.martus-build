package org.martus.common;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class TestCaseEnhanced extends TestCase
{
	public TestCaseEnhanced(String name)
	{
		super(name);
	}
	
	public File createTempFile() throws IOException
	{
		final String tempFileName = "$$$" + getName();
		File file = File.createTempFile(tempFileName, null);
		file.deleteOnExit();
		return file;
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
	
	public final static String BAD_FILENAME = "<>//\\..??**::||";

}
