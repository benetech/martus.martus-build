package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayInputStreamWithSeek extends InputStreamWithSeek 
{
	public ByteArrayInputStreamWithSeek(byte[] bytesToUse)
	{
		bytes = bytesToUse;
		try
		{
			inputStream = openStream();
		}
		catch(IOException impossible)
		{
			inputStream = null;
		}
	}
	
	InputStream openStream() throws IOException 
	{
		return new ByteArrayInputStream(bytes);
	}
	
	byte[] bytes;
}
