package org.martus.client;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class UiChoiceViewer extends UiField
{
	public UiChoiceViewer(ChoiceItem[] choicesToUse)
	{
		choices = choicesToUse;
		widget = new JLabel();
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public String getText()
	{
		return "";
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
		widget.setText(" " + item.toString() + " ");
	}

	public void disableEdits()
	{
	}

	JLabel widget;
	ChoiceItem[] choices;
}

