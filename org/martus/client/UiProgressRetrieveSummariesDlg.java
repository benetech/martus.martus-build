package org.martus.client;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class UiProgressRetrieveSummariesDlg extends JDialog 
{
	public UiProgressRetrieveSummariesDlg(UiMainWindow window, String tag)
	{
		super(window, window.getApp().getWindowTitle(tag), true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		mainWindow = window;
		bulletinCountMeter = new UiProgressMeter();
		statusMessage = window.getApp().getFieldLabel(tag);
		updateBulletinCountMeter(0, 1);	
		Box vBox = Box.createVerticalBox();
		vBox.add(new JLabel("    "));
		vBox.add(bulletinCountMeter);
		vBox.add(new JLabel("    "));
		getContentPane().add(vBox);
		getContentPane().add(new JLabel("    "), BorderLayout.EAST);
		getContentPane().add(new JLabel("    "), BorderLayout.WEST);
		window.centerDlg(this);
	}
	
	public void updateBulletinCountMeter(int currentValue, int maxValue)
	{
		bulletinCountMeter.updateProgressMeter(statusMessage, currentValue, maxValue);	
	}
	
	private UiProgressMeter bulletinCountMeter;
	private UiMainWindow mainWindow;
	private String statusMessage;
}
