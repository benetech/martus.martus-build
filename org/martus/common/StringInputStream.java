package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class StringInputStream extends ByteArrayInputStream 
{
	public StringInputStream(String source) throws UnsupportedEncodingException
	{
		super(source.getBytes("UTF-8"));
	}
}
