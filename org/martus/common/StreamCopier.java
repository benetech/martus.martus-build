package org.martus.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopier implements StreamFilter
{
	public void copyStream(InputStream in, OutputStream out) throws IOException
	{
		if(in == null)
			throw new IOException("Null InputStream");
		int got;
		byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
		while( (got=in.read(bytes)) >= 0)
			out.write(bytes, 0, got);
	}
}
