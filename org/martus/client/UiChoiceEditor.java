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

import javax.swing.JComboBox;
import javax.swing.JComponent;

public class UiChoiceEditor extends UiField
{
	public UiChoiceEditor(ChoiceItem[] choicesToUse)
	{
		choices = choicesToUse;
		widget = new JComboBox(choices);
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public String getText()
	{
		ChoiceItem item = (ChoiceItem)widget.getSelectedItem();
		return item.getCode();
	}

	public void setText(String newText)
	{
		ChoiceItem item = choices[0];
		for(int i = 0; i < choices.length; ++i)
		{
			if(newText.equals(choices[i].getCode()))
			{
				item = choices[i];
				break;
			}
		}
		widget.setSelectedItem(item);
	}

	public void disableEdits()
	{
		widget.setEnabled(false);
	}

	JComboBox widget;
	ChoiceItem[] choices;
}

