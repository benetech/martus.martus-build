package org.martus.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class UnicodeWriter extends OutputStreamWriter
{
	public static final int CREATE = 0;
	public static final int APPEND = 1;
	public final static String NEWLINE = System.getProperty("line.separator");

	public UnicodeWriter(File file) throws IOException
	{
		this(file, CREATE);
	}

	public UnicodeWriter(File file, int mode) throws IOException
	{
		this(new FileOutputStream(file.getPath(), (mode==APPEND)));
	}

	public UnicodeWriter(OutputStream outputStream) throws IOException
	{
		super(outputStream, "UTF8");
	}

	public void writeln(String text) throws IOException
	{
		write(text + NEWLINE);
	}
}
