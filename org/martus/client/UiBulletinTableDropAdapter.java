/* $Id: UiBulletinTableDropAdapter.java,v 1.4 2002/09/25 22:35:42 kevins Exp $ */
package org.martus.client;

import java.awt.Point;

import org.martus.client.*;

class UiBulletinTableDropAdapter extends UiBulletinDropAdapter
{
	UiBulletinTableDropAdapter(UiBulletinTable tableToUse, UiMainWindow mainWindow)
	{
		super(mainWindow);
		table = tableToUse;
	}

	public BulletinFolder getFolder(Point at)
	{
		return table.getFolder();
	}

	UiBulletinTable table;
}
