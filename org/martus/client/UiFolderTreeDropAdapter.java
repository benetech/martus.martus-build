package org.martus.client;

import java.awt.Point;

import org.martus.client.*;

class UiFolderTreeDropAdapter extends UiBulletinDropAdapter
{
	UiFolderTreeDropAdapter(UiFolderTree treeToUse, UiMainWindow mainWindow)
	{
		super(mainWindow);
		tree = treeToUse;
	}

	public BulletinFolder getFolder(Point at)
	{
		BulletinFolder toFolder = tree.getFolder(at);
		return toFolder;
	}

	UiFolderTree tree;
}
