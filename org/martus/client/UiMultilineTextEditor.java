package org.martus.client;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

public class UiMultilineTextEditor extends UiTextField
{
	public UiMultilineTextEditor(MartusApp appToUse)
	{
		super(appToUse);
		editor = new UiTextArea(5, UiConstants.textFieldColumns);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);
		editor.setFont(new Font("SansSerif", Font.PLAIN, UiConstants.defaultFontSize));

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
		editor.updateUI(); //Resets view position to top of scroll pane
	}

	public void disableEdits()
	{
		editor.setEditable(false);
	}

	JScrollPane widget;
	UiTextArea editor;
}

