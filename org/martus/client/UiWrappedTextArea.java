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

package org.martus.client;

import java.awt.Window;

import javax.swing.JTextArea;

public class UiWrappedTextArea extends JTextArea
{
	public UiWrappedTextArea(Window mainWindow, String message)
	{
		this(mainWindow, message, 80);	
	}


	public UiWrappedTextArea(Window mainWindow, String message, int maxChars)
	{
		super(message);
		int messageLength = message.length();
		int cols = messageLength;
		int rows = 1;
		if(cols > maxChars)
			cols = maxChars;

		int startOfParagraph = 0;
		int endOfParagraph = message.indexOf("\n");
		
		if(endOfParagraph == -1 && messageLength > maxChars)
			rows = messageLength / maxChars;
		
		while(endOfParagraph >= 0 )
		{
			int paragraphLength = endOfParagraph - startOfParagraph;
			rows += (paragraphLength / cols) + 1;

			startOfParagraph = endOfParagraph + 1;
			endOfParagraph = message.indexOf("\n", startOfParagraph);
		}
		
		// cushion for safety
		if(messageLength > maxChars)
			++rows;

		setRows(rows);
		setColumns(cols);
		setEditable(false);
		setFocusable(true);
		setWrapStyleWord(true);
		setLineWrap(true);
		setBackground(mainWindow.getBackground());
		setForeground(mainWindow.getForeground());
	}

}
