/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

package org.martus.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class UnicodeStringWriter extends UnicodeWriter 
{
	public static UnicodeStringWriter create() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		return new UnicodeStringWriter(out);
	}
	
	public String toString()
	{
		try 
		{
			return outputBuffer.toString("UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			return "unavailable";
		}
	}
	
	private UnicodeStringWriter(ByteArrayOutputStream out) throws IOException
	{
		super(out);
		outputBuffer = out;
	}
	
	ByteArrayOutputStream outputBuffer;
}
