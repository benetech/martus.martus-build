/* $Id: UiNormalTextViewer.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;

import java.awt.Font;
import javax.swing.event.*;

import org.martus.client.*;

public class UiNormalTextViewer extends UiNormalTextField
{
	public UiNormalTextViewer(MartusApp appToUse)
	{
		super(appToUse);
		widget = new UiTextArea(1, 40);
		widget.setFont(new Font("SansSerif", Font.PLAIN, 12));
		supportContextMenu();
	}

}

