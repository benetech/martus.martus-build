package org.martus.client;

import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.*;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.martus.client.*;

public class UiMultilineViewer extends UiTextField
{
	public UiMultilineViewer(MartusApp appToUse)
	{
		super(appToUse);
		text = new PreviewTextArea(1, 40);
		supportContextMenu();
	}

	public JComponent getComponent()
	{
		return text;
	}

	public JTextComponent getEditor()
	{
		return text;
	}

	public String getText()
	{
		return "";
	}

	public void setText(String newText)
	{
		text.setText(newText);
	}

	public void disableEdits()
	{
	}

	class PreviewTextArea extends UiTextArea
	{
		PreviewTextArea(int rows, int cols)
		{
			super(rows, cols);
			setLineWrap(true);
			setWrapStyleWord(true);
			setAutoscrolls(false);
			setEditable(false);
			setFont(new Font("SansSerif", Font.PLAIN, 12));
		}

		// overridden ONLY because setting the text to a new
		// value was causing the nearest enclosing scroll pane
		// to jump to this field
		public void scrollRectToVisible(Rectangle rect)
		{
			// do nothing!
		}
	}

	UiTextArea text;
}

