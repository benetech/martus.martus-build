package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;



public class UiVirtualKeyboard
{

	public UiVirtualKeyboard(UiMainWindow mainWindow, VirtualKeyboardHandler uiHandler)
	{
		MartusApp app = mainWindow.getApp();
		handler = uiHandler;
		password = "";
		String keys = app.getFieldLabel("VirtualKeyboardKeys");
		space = app.getFieldLabel("VirtualKeyboardSpace");
		delete = app.getFieldLabel("VirtualKeyboardBackSpace");

		UpdateHandler updateHandler = new UpdateHandler();

		Container vKeyboard = new Container();
		int columns = 13;
		if(mainWindow.isMacintosh())
			columns = 10;
		int rows = keys.length() / columns;
		vKeyboard.setLayout(new GridLayout(rows, columns));		
		for(int i = 0; i < keys.length(); ++i)
		{
			JButton key = new JButton(keys.substring(i,i+1));
			key.setFocusPainted(false);
			key.addActionListener(updateHandler);
			vKeyboard.add(key);
		}

		Container bottomRow = new Container();
		bottomRow.setLayout(new GridLayout(1,3));
		JButton spaceButton = new JButton(space);
		spaceButton.addActionListener(updateHandler);
		JButton deleteButton = new JButton(delete);
		deleteButton.addActionListener(updateHandler);
		bottomRow.add(spaceButton);
		bottomRow.add(new JLabel(""));
		bottomRow.add(deleteButton);

		Container entireKeyboard = new Container();
		entireKeyboard.setLayout(new BorderLayout());
		entireKeyboard.add(vKeyboard, BorderLayout.NORTH);
		entireKeyboard.add(bottomRow, BorderLayout.SOUTH);

		JPanel virtualKeyboard = new JPanel();
		virtualKeyboard.add(entireKeyboard);
		virtualKeyboard.setBorder(new LineBorder(Color.black, 1));

		handler.addKeyboard(virtualKeyboard);
	}

	public class UpdateHandler extends AbstractAction 
	{
		public void actionPerformed(ActionEvent e)
		{
			JButton buttonPressed = (JButton)(e.getSource());
			String passChar = buttonPressed.getText();
			if(passChar.equals(space))
				password += " ";
			else if(passChar.equals(delete))
			{
				if(password.length() > 0)
					password = password.substring(0,password.length()-1);
			}
			else
				password += passChar;
			handler.setPassword(password);
		} 
	}
	private VirtualKeyboardHandler handler;
	private String password;	
	private String space;
	private String delete;
}
