/* $Id: $ */
package org.martus.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipEntryInputStream extends InputStreamWithReset
{
	public ZipEntryInputStream(ZipFile zipToUse, ZipEntry entryToUse) throws IOException
	{
		zip = zipToUse;
		entry = entryToUse;
		openAndResetStream();
	}
	
	InputStream openStream() throws IOException
	{
		return zip.getInputStream(entry);
	}

	ZipFile zip;
	ZipEntry entry;
}
