package org.martus.client;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class UiProgressRetrieveDlg extends JDialog
{

	public UiProgressRetrieveDlg(UiMainWindow window, String tag)
	{
		super(window, window.getApp().getWindowTitle(tag), true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		mainWindow = window;
		bulletinCountMeter = new UiProgressMeter();
		//RIGHT_ALIGNMENT causes ... in text presentation for some reason
		//bulletinCountMeter.setAlignmentX(bulletinCountMeter.RIGHT_ALIGNMENT);
		chunkCountMeter = new UiProgressMeter();
		//chunkCountMeter.setAlignmentX(chunkCountMeter.RIGHT_ALIGNMENT);
		statusMessage = window.getApp().getFieldLabel(tag);
		updateBulletinCountMeter(0, 1);	
		chunkCountMeter.updateProgressMeter(mainWindow.getApp().getFieldLabel("ChunkProgressStatusMessage"), 0, 1);			
		Box vBox = Box.createVerticalBox();
		vBox.add(new JLabel("    "));
		vBox.add(bulletinCountMeter);
		vBox.add(chunkCountMeter);
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
	
	public UiProgressMeter getChunkCountMeter()
	{
		return chunkCountMeter;	
	}

	private UiProgressMeter bulletinCountMeter;
	private UiProgressMeter chunkCountMeter;
	private UiMainWindow mainWindow;
	private String statusMessage;

}
