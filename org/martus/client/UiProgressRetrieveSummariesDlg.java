package org.martus.client;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JLabel;

public class UiProgressRetrieveSummariesDlg extends UiProgressRetrieveDlg
{
	public UiProgressRetrieveSummariesDlg(UiMainWindow window, String tag)
	{
		super(window, tag);
		Box vBox = Box.createVerticalBox();
		vBox.add(new JLabel("    "));
		vBox.add(bulletinCountMeter);
		vBox.add(new JLabel("    "));
		vBox.add(cancel);
		vBox.add(new JLabel("    "));
		getContentPane().add(vBox);
		window.centerDlg(this);
	}
}
