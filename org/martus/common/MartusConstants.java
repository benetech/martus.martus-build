package org.martus.common;

public interface MartusConstants 
{
	// Somewhat surprisingly, a 32k buffer didn't seem to be any 
	// faster than a 1k buffer.
	public final static int streamBufferCopySize = 1024;
}
