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
		
		model = new FolderList();
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
		DefaultMutableTreeNode node = model.findFolder(newFolder.getName());
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
		DefaultMutableTreeNode node = model.findFolder(f.getName());
		if(node != null)
			model.nodeChanged(node);
	}

	class FolderTreeMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if(!e.isMetaDown())
				return;

			DefaultMutableTreeNode node = null;
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if(path != null)
			{
				tree.setSelectionPath(path);
				node = (DefaultMutableTreeNode)path.getLastPathComponent();
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
		public ActionDelete(DefaultMutableTreeNode node)
		{
			String text = parent.getApp().getMenuLabel("DeleteFolder") + " ";
			if(node != null)
				text += node.toString();

			putValue(NAME, text);
			nodeToDelete = node;
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(store.findFolder(nodeToDelete.toString()).getBulletinCount() == 0 
				|| parent.confirmDlg(parent, "deletefolder"))
			{
				store.deleteFolder(nodeToDelete.toString());
				parent.folderHasChanged(null);
			}
		}

		public boolean isEnabled()
		{
			if(nodeToDelete == null)
				return false;

			BulletinFolder folder = store.findFolder(nodeToDelete.toString());
			if(folder == null || !folder.canDelete())
				return false;

			return true;
		}

		DefaultMutableTreeNode nodeToDelete;
	}

	class ActionRename extends AbstractAction
	{
		public ActionRename(DefaultMutableTreeNode node)
		{
			String text = parent.getApp().getMenuLabel("RenameFolder") + " ";
			if(node != null)
				text += node.toString();

			putValue(NAME, text);
			nodeToRename = node;
		}

		public void actionPerformed(ActionEvent ae)
		{
			System.out.println("Rename " + nodeToRename.toString());
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

			BulletinFolder folder = store.findFolder(nodeToRename.toString());
			if(folder != null && folder.canRename())
				return true;

			return false;
		}

		DefaultMutableTreeNode nodeToRename;
	}


	private BulletinFolder getFolderAt(TreePath path)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		if (node == null)
		{
			return null;
		}
		if (!node.isLeaf())
		{
			return null;
		}

		String name = node.toString();
		return store.findFolder(name);
	}

	private TreePath getPathOfNode(DefaultMutableTreeNode node)
	{
		TreePath rootPath = new TreePath(model.getRoot());
		return rootPath.pathByAddingChild(node);
	}

	private TreePath getPathOfFolder(String folderName)
	{
		DefaultMutableTreeNode node = model.findFolder(folderName);
		if(node == null)
			return null;
		return getPathOfNode(node);
	}

	UiMainWindow parent;
	BulletinStore store;
	FolderList model;
	UiFolderTree tree;
}
