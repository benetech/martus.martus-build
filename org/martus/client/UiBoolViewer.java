package org.martus.client;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class UiBoolViewer extends UiField
{
	public UiBoolViewer(MartusApp appToUse)
	{
		app = appToUse;
		widget = new JLabel();
		widget.setBorder(new LineBorder(Color.black));
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

		widget.setText(" " + text + " ");
	}

	public void disableEdits()
	{
	}

	MartusApp app;
	JLabel widget;
}

