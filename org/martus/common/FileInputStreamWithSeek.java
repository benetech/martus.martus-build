package org.martus.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInputStreamWithSeek extends InputStreamWithSeek
{
	public FileInputStreamWithSeek(File fileToUse) throws IOException
	{
		file = fileToUse;
		inputStream = openStream();
	}
	
	InputStream openStream() throws IOException
	{
		return new FileInputStream(file);
	}

	File file;
}
