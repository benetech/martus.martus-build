package org.martus.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import sun.awt.WindowClosingListener;

public class UiProgressRetrieveSummariesDlg extends JDialog
{
	public UiProgressRetrieveSummariesDlg(UiMainWindow window, String tag)
	{
		super(window, window.getApp().getWindowTitle(tag), true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowEventHandler());
		mainWindow = window;
		bulletinCountMeter = new UiProgressMeter();
		statusMessage = window.getApp().getFieldLabel(tag);
		updateBulletinCountMeter(0, 1);	
		Box vBox = Box.createVerticalBox();
		vBox.add(new JLabel("    "));
		vBox.add(bulletinCountMeter);
		vBox.add(new JLabel("    "));
		cancel = new JButton(window.getApp().getButtonLabel("Cancel"));
		cancel.addActionListener(new CancelHandler());
		vBox.add(cancel);
		getContentPane().add(vBox);
		getContentPane().add(new JLabel("    "), BorderLayout.EAST);
		getContentPane().add(new JLabel("    "), BorderLayout.WEST);
		window.centerDlg(this);
		
	}

	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			System.out.println("out");
			RequestExit();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			System.out.println(ae.getSource());
			if(ae.getSource() == cancel)
				RequestExit();
		}
	}
	
	private void RequestExit()
	{
		requestExit = true;
		cancel.setEnabled(false);
	}

	public void beginRetrieve()
	{
		show();
	}
	
	public void finishedRetrieve()
	{
		dispose();	
	}
	
	public boolean shouldExit()
	{
		return requestExit;	
	}
	
	public void updateBulletinCountMeter(int currentValue, int maxValue)
	{
		bulletinCountMeter.updateProgressMeter(statusMessage, currentValue, maxValue);	
	}
	
	private UiProgressMeter bulletinCountMeter;
	private UiMainWindow mainWindow;
	private String statusMessage;
	private JButton cancel;
	private boolean requestExit;

}
