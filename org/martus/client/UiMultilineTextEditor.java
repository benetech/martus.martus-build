package org.martus.client;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

import org.martus.client.*;

public class UiMultilineTextEditor extends UiTextField
{
	public UiMultilineTextEditor(MartusApp appToUse)
	{
		super(appToUse);
		editor = new UiTextArea(5, 50);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);

		widget = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		supportContextMenu();
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public JTextComponent getEditor()
	{
		return editor;
	}

	public String getText()
	{
		return editor.getText();
	}

	public void setText(String newText)
	{
		editor.setText(newText);
	}

	public void disableEdits()
	{
		editor.setEditable(false);
	}

	JScrollPane widget;
	UiTextArea editor;
}

