package org.martus.client;

import java.awt.Font;

public class UiNormalTextEditor extends UiNormalTextField
{
	public UiNormalTextEditor(MartusApp appToUse)
	{
		super(appToUse);
		widget = new UiTextArea(1, UiConstants.textFieldColumns);
		widget.setLineWrap(true);
		widget.setWrapStyleWord(true);
		widget.setFont(new Font("SansSerif", Font.PLAIN, UiConstants.defaultFontSize));
		supportContextMenu();
	}
}

