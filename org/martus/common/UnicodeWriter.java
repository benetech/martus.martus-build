/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class UnicodeWriter extends OutputStreamWriter
{
	public static final int CREATE = 0;
	public static final int APPEND = 1;
	public final static String NEWLINE = System.getProperty("line.separator");

	public UnicodeWriter(File file) throws IOException
	{
		this(file, CREATE);
	}

	public UnicodeWriter(File file, int mode) throws IOException
	{
		this(new FileOutputStream(file.getPath(), (mode==APPEND)));
	}

	public UnicodeWriter(OutputStream outputStream) throws IOException
	{
		super(outputStream, "UTF8");
	}

	public void writeln(String text) throws IOException
	{
		write(text + NEWLINE);
	}
}
