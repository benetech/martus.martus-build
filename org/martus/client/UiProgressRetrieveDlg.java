package org.martus.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;

public class UiProgressRetrieveDlg extends JDialog 
{
	public UiProgressRetrieveDlg(UiMainWindow window, String tag)
	{
		super(window, window.getApp().getWindowTitle(tag), true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowEventHandler());
		cancel = new JButton(window.getApp().getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		cancel.setAlignmentX(JButton.CENTER_ALIGNMENT);
		bulletinCountMeter = new UiProgressMeter(this);
		statusMessage = window.getApp().getFieldLabel(tag);
		updateBulletinCountMeter(0, 1);	
	}

	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			requestExit();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
				requestExit();
		}
	}
	
	private void requestExit()
	{
		isExitRequested = true;
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
		return isExitRequested;	
	}

	public void updateBulletinCountMeter(int currentValue, int maxValue)
	{
		bulletinCountMeter.updateProgressMeter(statusMessage, currentValue, maxValue);	
	}
	
	public UiProgressMeter bulletinCountMeter;
	public JButton cancel;

	private String statusMessage;
	private boolean isExitRequested;
}
