package org.martus.client;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JLabel;

public class UiProgressRetrieveBulletinsDlg extends UiProgressRetrieveDlg
{
	public UiProgressRetrieveBulletinsDlg(UiMainWindow window, String tag)
	{
		super(window, tag);
		chunkCountMeter = new UiProgressMeter(this);
		chunkCountMeter.updateProgressMeter(window.getApp().getFieldLabel("ChunkProgressStatusMessage"), 0, 1);			
		Box vBox = Box.createVerticalBox();
		vBox.add(new JLabel("    "));
		vBox.add(bulletinCountMeter);
		vBox.add(chunkCountMeter);
		vBox.add(new JLabel("    "));
		vBox.add(cancel);
		vBox.add(new JLabel("    "));
		getContentPane().add(vBox);
		window.centerDlg(this);
	}
	
	public UiProgressMeter getChunkCountMeter()
	{
		return chunkCountMeter;	
	}

	private UiProgressMeter chunkCountMeter;
}
