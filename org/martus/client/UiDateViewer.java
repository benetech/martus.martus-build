package org.martus.client;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class UiDateViewer extends UiField
{
	public UiDateViewer(MartusApp appToUse)
	{
		app = appToUse;
		label = new JLabel();
		label.setPreferredSize(new Dimension(80,20)); //Set for printing correctly
	}

	public JComponent getComponent()
	{
		return label;
	}

	public String getText()
	{
		return "";
	}

	public void setText(String newText)
	{
		value = app.convertStoredToDisplay(newText);
		label.setText(" "+value);
	}

	public void disableEdits()
	{
	}

	MartusApp app;
	JLabel label;
	String value;
}

