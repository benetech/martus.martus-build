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

package org.martus.client.swingui;

import org.martus.client.core.ChoiceItem;

public class DateUtilities
{
	public static String getMdyOrder(String format)
	{
		String result = "";
		format = format.toLowerCase();
		for(int i = 0; i < format.length(); ++i)
		{
			char c = format.charAt(i);
			if( (c == 'm' || c == 'd' || c == 'y') && (result.indexOf(c) < 0) )
				result += c;
		}

		return result;
	}

	public static String getDefaultDateFormatCode()
	{
		return DateUtilities.MDY_SLASH.getCode();
	}

	public static ChoiceItem[] getDateFormats()
	{
		return new ChoiceItem[]
		{
			DateUtilities.MDY_SLASH,
			DateUtilities.DMY_SLASH,
			DateUtilities.DMY_DOT
		};
	}


	public static ChoiceItem DMY_SLASH = new ChoiceItem("dd/MM/yyyy", "dd/mm/yyyy");
	public static ChoiceItem MDY_SLASH = new ChoiceItem("MM/dd/yyyy", "mm/dd/yyyy");
	public static ChoiceItem DMY_DOT = new ChoiceItem("dd.MM.yyyy", "dd.mm.yyyy");
}
