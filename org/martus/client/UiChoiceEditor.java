/* $Id: UiChoiceEditor.java,v 1.4 2002/09/25 22:07:14 kevins Exp $ */
package org.martus.client;

import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.event.*;

import org.martus.client.*;

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

