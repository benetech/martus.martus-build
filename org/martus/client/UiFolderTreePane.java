package org.martus.client;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.martus.client.*;

class UiFolderTreePane extends JScrollPane
{
	UiFolderTreePane(UiMainWindow mainWindow)
	{
		parent = mainWindow;
		store = parent.getStore();
		
		model = new FolderList(parent.getApp());
		model.loadFolders(store);

		tree = new UiFolderTree(model, store, parent);
		tree.addMouseListener(new FolderTreeMouseAdapter());

		getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		getViewport().add(tree);
	}

	public boolean selectFolder(String folderName)
	{
		TreePath path = getPathOfFolder(folderName);
		if(path == null)
			return false;

		tree.clearSelection();
		tree.addSelectionPath(path);
		return true;
	}
	
	public String getSelectedFolderName()
	{
		return tree.getSelectedFolderName();	
	}
	
	public void createNewFolder() 
	{
		tree.stopEditing();

		BulletinFolder newFolder = parent.getApp().createUniqueFolder();
		if(newFolder == null)
			return;
		parent.folderHasChanged(newFolder);
		FolderTreeNode node = model.findFolderByLocalizedName(newFolder.getName());
		if(node == null)
			return;
		tree.stopEditing();
		tree.startEditingAtPath(getPathOfNode(node));
		return;
	}

	public void folderHasChanged(BulletinFolder f)
	{
		String selectedName = tree.getSelectedFolderName();
		model.loadFolders(store);
		selectFolder(selectedName);
	}
	
	public void folderContentsHaveChanged(BulletinFolder f)
	{
		FolderTreeNode node = model.findFolderByLocalizedName(f.getName());
		if(node != null)
			model.nodeChanged(node);
	}

	class FolderTreeMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if(!e.isMetaDown())
				return;

			FolderTreeNode node = null;
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if(path != null)
			{
				tree.setSelectionPath(path);
				node = (FolderTreeNode)path.getLastPathComponent();
			}

			JPopupMenu menu = new JPopupMenu();
			menu.add(new JMenuItem(new ActionNewFolder()));
			menu.add(new JMenuItem(new ActionRename(node)));
			menu.add(new JMenuItem(new ActionDelete(node)));
			menu.addSeparator();
			menu.add(parent.getActionMenuPaste());
			menu.show(UiFolderTreePane.this, e.getX(), e.getY());
		}
	}

	class ActionNewFolder extends AbstractAction
	{
		public ActionNewFolder()
		{
			super(parent.getApp().getMenuLabel("newFolder"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			createNewFolder();
			return;
		}

		public boolean isEnabled()
		{
			return true;
		}
	}

	class ActionDelete extends AbstractAction
	{
		public ActionDelete(FolderTreeNode node)
		{
			String text = parent.getApp().getMenuLabel("DeleteFolder") + " ";
			if(node != null)
				text += node.getLocalizedName();

			putValue(NAME, text);
			nodeToDelete = node;
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(store.findFolder(nodeToDelete.getInternalName()).getBulletinCount() == 0 
				|| parent.confirmDlg(parent, "deletefolder"))
			{
				store.deleteFolder(nodeToDelete.getInternalName());
				parent.folderHasChanged(null);
			}
		}

		public boolean isEnabled()
		{
			if(nodeToDelete == null)
				return false;

			BulletinFolder folder = store.findFolder(nodeToDelete.getInternalName());
			if(folder == null || !folder.canDelete())
				return false;

			return true;
		}

		FolderTreeNode nodeToDelete;
	}

	class ActionRename extends AbstractAction
	{
		public ActionRename(FolderTreeNode node)
		{
			String text = parent.getApp().getMenuLabel("RenameFolder") + " ";
			if(node != null)
				text += node.getLocalizedName();

			putValue(NAME, text);
			nodeToRename = node;
		}

		public void actionPerformed(ActionEvent ae)
		{
			System.out.println("Rename " + nodeToRename.getLocalizedName());
			TreePath path = getPathOfNode(nodeToRename);
			if(!tree.isPathEditable(path))
				return;

			BulletinFolder folder = getFolderAt(path);
			tree.startEditingAtPath(path);
		}

		public boolean isEnabled()
		{
			if(nodeToRename == null)
				return false;

			BulletinFolder folder = store.findFolder(nodeToRename.getInternalName());
			if(folder != null && folder.canRename())
				return true;

			return false;
		}

		FolderTreeNode nodeToRename;
	}


	private BulletinFolder getFolderAt(TreePath path)
	{
		FolderTreeNode node = (FolderTreeNode)path.getLastPathComponent();
		if (node == null)
		{
			return null;
		}
		if (!node.isLeaf())
		{
			return null;
		}

		String name = node.getInternalName();
		return store.findFolder(name);
	}

	private TreePath getPathOfNode(FolderTreeNode node)
	{
		TreePath rootPath = new TreePath(model.getRoot());
		return rootPath.pathByAddingChild(node);
	}

	private TreePath getPathOfFolder(String folderName)
	{
		FolderTreeNode node = model.findFolderByLocalizedName(folderName);
		if(node == null)
			return null;
		return getPathOfNode(node);
	}

	UiMainWindow parent;
	BulletinStore store;
	FolderList model;
	UiFolderTree tree;
}
