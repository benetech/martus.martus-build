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
		int messageLength = message.length();
		int cols = messageLength;
		int rows = 1;
		if(cols > maxChars)
			cols = maxChars;

		int startOfParagraph = 0;
		int endOfParagraph = message.indexOf("\n");
		
		if(endOfParagraph == -1 && messageLength > maxChars)
			rows = messageLength / maxChars;
		
		while(endOfParagraph >= 0 )
		{
			int paragraphLength = endOfParagraph - startOfParagraph;
			rows += (paragraphLength / cols) + 1;

			startOfParagraph = endOfParagraph + 1;
			endOfParagraph = message.indexOf("\n", startOfParagraph);
		}
		
		// cushion for safety
		if(messageLength > maxChars)
			++rows;

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
