package org.martus.client;

import javax.swing.JTextArea;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyListener;

public class UiWrappedTextArea extends JTextArea
{
	public UiWrappedTextArea(UiMainWindow mainWindow, String message)
	{
		this(mainWindow, message, 80);	
	}


	public UiWrappedTextArea(UiMainWindow mainWindow, String message, int maxChars)
	{
		super(message);
		int cols = message.length();
		int rows = 1;
		if(cols > maxChars)
		{
			rows = (cols / maxChars) + 1;
			cols = maxChars;
		}

		int start = message.indexOf("\n\n");
		while(start >= 0 )
		{
			++rows;	
			start = message.indexOf("\n\n", start+2);
		}

		setRows(rows);
		setColumns(cols);
		setEditable(false);
		setFocusable(true);
		setWrapStyleWord(true);
		setLineWrap(true);
		setBackground(mainWindow.getBackground());
		setForeground(mainWindow.getForeground());
	}

}
