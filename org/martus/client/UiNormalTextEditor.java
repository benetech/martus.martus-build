/* $Id: UiNormalTextEditor.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;

import javax.swing.event.*;

import org.martus.client.*;

public class UiNormalTextEditor extends UiNormalTextField
{
	public UiNormalTextEditor(MartusApp appToUse)
	{
		super(appToUse);
		widget = new UiTextArea(1, 50);
		supportContextMenu();
	}
}

