/* $Id: UiNormalTextField.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;

import javax.swing.JComponent;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import org.martus.client.*;

public abstract class UiNormalTextField extends UiTextField
{
	public UiNormalTextField(MartusApp appToUse)
	{
		super(appToUse);
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public JTextComponent getEditor()
	{
		return widget;
	}

	public String getText()
	{
		return widget.getText();
	}

	public void setText(String newText)
	{
		widget.setText(newText);
	}

	public void disableEdits()
	{
		widget.setEditable(false);
	}

	UiTextArea widget;
}

