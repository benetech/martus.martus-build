package org.martus.common;

import java.io.IOException;
import java.io.InputStream;

public abstract class InputStreamWithSeek extends InputStream
{
	public int available() throws IOException
	{
		return inputStream.available();
	}
	
	public void close() throws IOException
	{
		inputStream.close();
	}

	public int read() throws IOException
	{
		int got = inputStream.read();
		return got;
	}
	
	public long skip(long n) throws IOException
	{
		return inputStream.skip(n);
	}
	
	public void seek(long offset) throws IOException
	{
		inputStream.close();
		inputStream = openStream();
		inputStream.skip(offset);
	}
	
	abstract InputStream openStream() throws IOException;
	
	InputStream inputStream;

}
