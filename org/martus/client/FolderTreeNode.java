package org.martus.client;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class FolderTreeNode extends DefaultMutableTreeNode 
{

	public FolderTreeNode(Object obj, MartusApp appToUse)
	{
		super(obj);	
		app = appToUse;
	}

	public String getInternalName()
	{
		return super.toString();
	}

	public String toString() 
	{
		String internal = getInternalName();
		return app.getFolderLabel(internal);
	}
	
	public String getLocalizedName()
	{
		return toString();
	}

	private MartusApp app;
}
