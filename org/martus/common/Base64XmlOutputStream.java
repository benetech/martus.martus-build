package org.martus.common;

import java.io.IOException;
import java.io.OutputStream;

public class Base64XmlOutputStream extends OutputStream 
{
	public Base64XmlOutputStream(XmlWriterFilter destination) 
	{
		dest = destination;
		buffer = new byte[Base64.BYTESPERLINE];
		offset = 0;
	}

	public void write(int b) throws IOException 
	{
		buffer[offset++] = (byte)b;
		if(offset >= buffer.length)
			flush();
	}
	
	public void flush() throws IOException
	{
		flushBuffer();
	}

	public void close() throws IOException 
	{
		flush();
	}

	private void flushBuffer() throws IOException
	{
		String thisLine = Base64.encode(buffer, 0, offset);
		writeLine(thisLine);
		offset = 0;
	}

	private void writeLine(String thisLine) throws IOException
	{
		dest.writeDirect(thisLine);
		dest.writeDirect("\n");
	}
	
	XmlWriterFilter dest;
	byte[] buffer;
	int offset;
}
