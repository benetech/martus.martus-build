package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public class UiModelessBusyDlg extends JDialog
{

	public UiModelessBusyDlg(String message)
	{
		super();
		getContentPane().add(new JLabel(" "), BorderLayout.NORTH);
		getContentPane().add(new JLabel(" "), BorderLayout.SOUTH);
		getContentPane().add(new JLabel("     "), BorderLayout.EAST);
		getContentPane().add(new JLabel("     "), BorderLayout.WEST);
		getContentPane().add(new JLabel(message), BorderLayout.CENTER);
		getRootPane().setBorder(new LineBorder(Color.black, 5));
		setUndecorated(true);
		pack();
		Dimension size = getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(MartusApp.center(size, screen));
		setResizable(false);
		origCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		show();
	}
	
	public void endDialog()
	{
		setCursor(origCursor);
		dispose();
	}
	
	Cursor origCursor;
}
