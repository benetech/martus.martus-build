package org.martus.common;

import java.io.*;

public class UnicodeReader extends BufferedReader
{
	public UnicodeReader(File file) throws IOException
	{
		this(new FileInputStream(file.getPath()));
	}

	public UnicodeReader(InputStream inputStream) throws IOException
	{
		super(new InputStreamReader(inputStream, "UTF8"));
	}

	public String readAll(int maxLines) throws IOException
	{
		String all = "";
		for(int i = 0; i < maxLines; ++i)
		{
			String line = readLine();
			if(line == null)
				break;
			all += line + NEWLINE;
		}

		return all;
	}

	final String NEWLINE = System.getProperty("line.separator");
}
