package org.martus.client;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

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

