package org.martus.client;

import java.awt.Font;

public class UiNormalTextViewer extends UiNormalTextField
{
	public UiNormalTextViewer(MartusApp appToUse)
	{
		super(appToUse);
		widget = new UiTextArea(1, 40);
		widget.setLineWrap(true);
		widget.setFont(new Font("SansSerif", Font.PLAIN, 13));
		supportContextMenu();
	}

}

