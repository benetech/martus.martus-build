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

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

public class UiMultilineTextEditor extends UiTextField
{
	public UiMultilineTextEditor(UiLocalization localizationToUse)
	{
		super(localizationToUse);
		editor = new UiTextArea(5, UiConstants.textFieldColumns);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);
		editor.setFont(new Font("SansSerif", Font.PLAIN, UiConstants.defaultFontSize));

		widget = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		supportContextMenu();
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public JTextComponent getEditor()
	{
		return editor;
	}

	public String getText()
	{
		return editor.getText();
	}

	public void setText(String newText)
	{
		editor.setText(newText);
		editor.updateUI(); //Resets view position to top of scroll pane
	}

	public void disableEdits()
	{
		editor.setEditable(false);
	}

	JScrollPane widget;
	UiTextArea editor;
}

