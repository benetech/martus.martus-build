/* $Id: UiBoolViewer.java,v 1.3 2002/04/05 21:34:34 kevins Exp $ */
package org.martus.client;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.*;

import org.martus.client.*;

public class UiBoolViewer extends UiField
{
	public UiBoolViewer(MartusApp appToUse)
	{
		app = appToUse;
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
		String text = "";
		if(newText.equals(TRUESTRING))
			text = app.getButtonLabel("yes");
		else
			text = app.getButtonLabel("no");

		widget.setText(text);
	}

	public void disableEdits()
	{
	}

	MartusApp app;
	JLabel widget;
}

