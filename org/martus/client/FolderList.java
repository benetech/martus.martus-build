package org.martus.client;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class FolderList extends DefaultTreeModel
{
	public FolderList()
	{
		super(new DefaultMutableTreeNode("?"));
		root = (DefaultMutableTreeNode)getRoot();
	}

	public int getCount()
	{
		return root.getChildCount();
	}

	public void loadFolders(BulletinStore store)
	{
		while(getCount() > 0)
		{
			DefaultMutableTreeNode item = (DefaultMutableTreeNode)getChild(root, 0);
			removeNodeFromParent(item);
		}

		for(int f = 0; f < store.getFolderCount(); ++f)
		{
			BulletinFolder folder = store.getFolder(f);
			String folderName = folder.getName();
			if(BulletinFolder.isNameVisible(folderName))
			{
				DefaultMutableTreeNode item = new DefaultMutableTreeNode(folderName);
				insertNodeInto(item, root, getCount());
			}
		}
	}

	public String getName(int index)
	{
		DefaultMutableTreeNode node = getNode(index);
		return node.toString();
	}

	public DefaultMutableTreeNode getNode(int index)
	{
		return (DefaultMutableTreeNode)getChild(root, index);
	}

	public DefaultMutableTreeNode findFolder(String folderName)
	{
		for(int i = 0; i < getCount(); ++i)
		{
			DefaultMutableTreeNode node = getNode(i);
			if(folderName.equals(node.toString()))
				return node;
		}

		return null;
	}

	DefaultMutableTreeNode root;
}
