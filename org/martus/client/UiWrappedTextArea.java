package org.martus.client;

import javax.swing.JTextArea;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyListener;

public class UiWrappedTextArea extends JTextArea
{
	public UiWrappedTextArea(UiMainWindow mainWindow, String message)
	{
		super(message);
		final int MAXCHARS = 80;
		int cols = message.length();
		int rows = 1;
		if(cols > MAXCHARS)
		{
			rows = (cols / MAXCHARS) + 1;
			cols = MAXCHARS;
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
	
	public void setFont(Font font, int columns)
	{
		setFont(font);
		setColumns(columns);
		setRows(0);
	}
}
