package org.martus.client;

import java.util.Vector;
import javax.swing.tree.DefaultTreeModel;

public class FolderList extends DefaultTreeModel
{
	public FolderList(MartusApp appToUse)
	{
		super(new FolderTreeNode("?", appToUse));
		app = appToUse;
		root = (FolderTreeNode)getRoot();
	}

	public int getCount()
	{
		return root.getChildCount();
	}

	public void loadFolders(BulletinStore store)
	{
		while(getCount() > 0)
		{
			FolderTreeNode item = (FolderTreeNode)getChild(root, 0);
			removeNodeFromParent(item);
		}
		
		Vector visibleFolderNames = store.getVisibleFolderNames();

		for(int f = 0; f < visibleFolderNames.size(); ++f)
		{
			String folderName = (String)visibleFolderNames.get(f);
			FolderTreeNode item = new FolderTreeNode(folderName, app);
			insertNodeInto(item, root, getCount());
		}
	}

	public String getName(int index)
	{
		FolderTreeNode node = getNode(index);
		return node.toString();
	}

	public FolderTreeNode getNode(int index)
	{
		return (FolderTreeNode)getChild(root, index);
	}

	public FolderTreeNode findFolderByInternalName(String folderName)
	{
		for(int i = 0; i < getCount(); ++i)
		{
			FolderTreeNode node = getNode(i);
			if(folderName.equals(node.getInternalName()))
				return node;
		}
		return null;
	}

	FolderTreeNode root;
	MartusApp app;
}
