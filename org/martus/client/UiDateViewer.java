/* $Id: UiDateViewer.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.*;

import org.martus.client.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class UiDateViewer extends UiField
{
	public UiDateViewer(MartusApp appToUse)
	{
		app = appToUse;
		label = new JLabel();
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
		label.setText(value);
	}

	public void disableEdits()
	{
	}

	MartusApp app;
	JLabel label;
	String value;
}

