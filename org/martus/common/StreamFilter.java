package org.martus.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamFilter
{
	public void copyStream(InputStream in, OutputStream out) throws IOException;
}
