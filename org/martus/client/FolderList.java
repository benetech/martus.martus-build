package org.martus.client;

import javax.swing.tree.DefaultMutableTreeNode;
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

		for(int f = 0; f < store.getFolderCount(); ++f)
		{
			BulletinFolder folder = store.getFolder(f);
			String folderName = folder.getName();
			if(BulletinFolder.isNameVisible(folderName))
			{
				FolderTreeNode item = new FolderTreeNode(folderName, app);
				insertNodeInto(item, root, getCount());
			}
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

	public FolderTreeNode findFolder(String folderName)
	{
		for(int i = 0; i < getCount(); ++i)
		{
			FolderTreeNode node = getNode(i);
			if(folderName.equals(node.toString()))
				return node;
		}

		return null;
	}

	FolderTreeNode root;
	MartusApp app;
}
