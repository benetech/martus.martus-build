package org.martus.client;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;



import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class UiNotifyDlg extends JDialog implements ActionListener
{

	public UiNotifyDlg(UiMainWindow main, JFrame owner, String title, String[] contents, String[] buttons)
	{
		super(owner, title , true);
		mainWindow = main;
		getContentPane().add(new JLabel("      "), BorderLayout.WEST);
		getContentPane().add(new JLabel("      "), BorderLayout.EAST);

		Box vbox = Box.createVerticalBox();
		vbox.add(new JLabel(" "));
		for(int i = 0 ; i < contents.length ; ++i)
			vbox.add(createWrappedTextArea(contents[i]));
		vbox.add(new JLabel(" "));

		ok = new JButton(buttons[0]);
		ok.addActionListener(this);
		Box hbox = Box.createHorizontalBox();
		hbox.add(ok);
		JButton button = null;
		for(int j = 1 ; j < buttons.length; ++j)
		{
			button = new JButton(buttons[j]);
			button.addActionListener(this);
			hbox.add(button);
		}
		vbox.add(hbox);
		vbox.add(new JLabel(" "));
		
		getContentPane().add(vbox, BorderLayout.CENTER);
		pack();
		Dimension size = getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(MartusApp.center(size, screen));
		setResizable(true);
		getRootPane().setDefaultButton(ok);
		ok.requestFocus(true);
		show();
	}
	
	private JTextArea createWrappedTextArea(String message) 
	{
		UiWrappedTextArea msgArea = new UiWrappedTextArea(mainWindow, message);
		msgArea.addKeyListener(new TabToOkButton());
		return msgArea;		
	}

	public class TabToOkButton extends KeyAdapter 
	{
		public void keyPressed(KeyEvent ke) 
		{
			if (ke.getKeyCode() == KeyEvent.VK_TAB)
			{
				ok.requestFocus();	
			}
		} 
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		exit();
	}
	
	public void exit()
	{
		if(ok.hasFocus())
			result = ok.getText();
		else
			result = "";
		dispose();
	}

	public String getResult()
	{
		return result;	
	}

	String result;
	JButton ok;
	UiMainWindow mainWindow;
}
