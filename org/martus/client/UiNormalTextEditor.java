package org.martus.client;

public class UiNormalTextEditor extends UiNormalTextField
{
	public UiNormalTextEditor(MartusApp appToUse)
	{
		super(appToUse);
		widget = new UiTextArea(1, 50);
		widget.setLineWrap(true);
		supportContextMenu();
	}
}

