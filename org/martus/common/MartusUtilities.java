package org.martus.common;

import java.io.File;

public class MartusUtilities 
{
	public static class FileTooLargeException extends Exception {}
	
	public static int getCappedFileLength(File file) throws FileTooLargeException
	{
		long rawLength = file.length();
		if(rawLength >= Integer.MAX_VALUE || rawLength < 0)
			throw new FileTooLargeException();
			
		return (int)rawLength;
	}
}
