package org.martus.common;

import junit.framework.*;
import java.util.*;
import java.text.*;
import java.io.*;
import java.io.File;

import org.martus.common.*;

public class TestUnicodeFileReader extends TestCaseEnhanced
{
    public TestUnicodeFileReader(String name) 
    {
        super(name);
    }

    public void setUp()
    {
    }

	public void testConstructor() throws Exception
	{
		File badFile = new File(BAD_FILENAME);
		try
		{
			UnicodeReader bad = new UnicodeReader(badFile);
			assertTrue("bad file", false);
		}
		catch(IOException e)
		{
			// exception was expected
		}

		try
		{
			UnicodeReader bad = new UnicodeReader((File)null);
			assertTrue("null file", false);
		}
		catch(Exception e)
		{
			// exception was expected
		}

		File file = File.createTempFile("$$$MartusTestUnicodeFileReader", null);
		file.deleteOnExit();
		createSampleFile(file);
		UnicodeReader reader = new UnicodeReader(file);

		assertEquals("Can read line 1 from open reader", text, reader.readLine());
		assertEquals("Can read line 2 from open reader", text2, reader.readLine());
		assertEquals("Null at EOF", null, reader.readLine());
		reader.close();
		try
		{
			reader.readLine();
			assertTrue("no exception from closed file read", false);
		}
		catch(Exception e)
		{
			// exception was expected
		}
		assertEquals("File still open?", true, file.delete());
	}

	public void testStreamConstructor() throws Exception
	{
		try
		{
			UnicodeReader shouldFail = new UnicodeReader((InputStream)null);
			fail("should not have been able to create");
		}
		catch(Exception e)
		{
			// expected exception
		}

		byte[] bytes = {'a', 'b', 'c', 'd'};
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		UnicodeReader reader = new UnicodeReader(inputStream);
		String got = reader.readLine();
		reader.close();
	}

    public void testReadAll() throws Exception
    {
		File file = File.createTempFile("$$test", null);
		createSampleFile(file);
		UnicodeReader reader = new UnicodeReader(file);

		String result = reader.readAll(100);
		assertEquals(text + NEWLINE + text2 + NEWLINE, result);
		reader.close();

		file.delete();
	}

	void createSampleFile(File file) throws Exception
	{
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.write(text + NEWLINE + text2 + NEWLINE);
		writer.close();
	}

	final String NEWLINE = System.getProperty("line.separator");
	final String text = "Test String";
	final String text2 = "í";
}
