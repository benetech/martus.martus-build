package org.martus.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class TestUnicodeFileWriter extends TestCaseEnhanced
{
    public TestUnicodeFileWriter(String name) 
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
			UnicodeWriter bad = new UnicodeWriter(badFile);
			assertTrue("bad file", false);
		}
		catch(IOException e)
		{
			// exception was expected
		}

		try
		{
			UnicodeWriter bad = new UnicodeWriter((File)null);
			assertTrue("bad file", false);
		}
		catch(Exception e)
		{
			// exception was expected
		}

		File tempFile = File.createTempFile("testmartus", null);
		tempFile.deleteOnExit();
		UnicodeWriter writer = new UnicodeWriter(tempFile);
		writer.write("hello");
		writer.close();

		UnicodeWriter appender = new UnicodeWriter(tempFile, UnicodeWriter.APPEND);
		appender.write("second");
		appender.close();

		assertEquals("File still open?", true, tempFile.delete());
	}

	public void testStreamConstructor() throws Exception
	{
		try
		{
			UnicodeWriter shouldFail = new UnicodeWriter((OutputStream)null);
			fail("should not have been able to create");
		}
		catch(Exception e)
		{
			// expected exception
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		UnicodeWriter writer = new UnicodeWriter(outputStream);
		writer.write("test");
		writer.close();
	}

    public void testBasics() throws Exception
    {
		File file = File.createTempFile("$$test", null);
		file.delete();
		assertTrue("Delete should work", !file.exists());

		UnicodeWriter writer = new UnicodeWriter(file);
		String text = "Test String";
		writer.write(text);
		writer.close();

		try
		{
			writer.write("Test String");
			assertTrue("Can't write to closed writer", false);
		}
		catch(IOException e)
		{
			// expected an exception
		}

		assertTrue("Should exist", file.exists());
		assertEquals(text.length(), file.length());
		file.delete();
	}

	public void testWritingUnicodeCharacters() throws Exception
	{
		String text2 = "í";
		int len2 = 0;
		try
		{
			len2 = text2.getBytes("UTF8").length;
		}
		catch(Exception e)
		{
			assertTrue("UTF8 not supported", false);
		}
		assertTrue("Unicode length should be different", len2 != text2.length());

		File file = File.createTempFile("testmartus", null);
		file.deleteOnExit();
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.write(text2);
		writer.close();
		assertEquals(len2, file.length());
    }

    public void testAppend() throws Exception
    {
		File file = File.createTempFile("$$test", null);
		file.deleteOnExit();
		String text = "Testing";
		int len = text.length();

		UnicodeWriter create = new UnicodeWriter(file);
		create.write(text);
		create.close();
		assertTrue("created?", file.exists());
		assertEquals("create length", len, file.length());

		UnicodeWriter overwrite = new UnicodeWriter(file);
		overwrite.write(text);
		overwrite.close();
		assertTrue("still there?", file.exists());
		assertEquals("create length", len, file.length());

		UnicodeWriter append = new UnicodeWriter(file, UnicodeWriter.APPEND);
		append.write(text);
		append.close();
		assertTrue("still there?", file.exists());
		assertEquals("create length", len * 2, file.length());
	}

	public void testWriteln() throws Exception
	{
		File file = File.createTempFile("$$test", null);
		file.deleteOnExit();
		String text = "Testing";
		int len = text.length();

		UnicodeWriter create = new UnicodeWriter(file);
		create.writeln(text);
		create.close();
		assertTrue("created?", file.exists());
		assertEquals("create length", len + create.NEWLINE.length(), file.length());
	}

}
