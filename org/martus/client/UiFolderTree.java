package org.martus.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DropTarget;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


class UiFolderTree extends JTree implements TreeSelectionListener
{
	public UiFolderTree(TreeModel model, BulletinStore storeToUse, UiMainWindow mainWindow)
	{
		super(model);
		store = storeToUse;
		observer = mainWindow;

		setShowsRootHandles(false);
		setEditable(true);
		setInvokesStopCellEditing(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		dropTarget = new DropTarget(this, new UiFolderTreeDropAdapter(this, mainWindow));
		DefaultTreeCellRenderer renderer = new FolderTreeNodeRenderer();
		setCellRenderer(renderer);
		setCellEditor(new FolderTreeCellEditor(this, renderer));
	}

	public String getSelectedFolderName()
	{
		TreePath path = getSelectionPath();
		if(path == null)
			return "";
			
		BulletinFolder f = getFolderAt(path);
		if(f == null)
			return "";

		return f.getName();
	}

	public BulletinFolder getFolder(Point at)
	{
		TreePath path = getPathForLocation(at.x, at.y);
		if(path == null)
			return null;
		return getFolderAt(path);
	}

	public BulletinFolder getFolderAt(TreePath path)
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

	// override superclass method
	public boolean isPathEditable(TreePath path)
	{
		BulletinFolder folder = getFolderAt(path);
		if(folder == null)
			return false;

		return folder.canRename();
	}

	// TreeSelectionListener interface
	public void valueChanged(TreeSelectionEvent e)
 	{
		BulletinFolder folder = getFolderAt(e.getPath());
		if(folder != null)
			observer.folderSelectionHasChanged(folder);
    }

	private FolderTreeNode getActualNode(TreeModelEvent event)
	{
		FolderTreeNode node = getChildNodeIfAny(event);

		if(node == null)
		{
			TreePath path = event.getTreePath();
			node = (FolderTreeNode)path.getLastPathComponent();
		}

		return node;
	}

	private TreePath getActualPath(TreeModelEvent event)
	{
		TreePath path = event.getTreePath();
		FolderTreeNode node = getChildNodeIfAny(event);
		if(node != null)
			path = path.pathByAddingChild(node);

		return path;
	}

	private FolderTreeNode getChildNodeIfAny(TreeModelEvent event)
	{
		FolderTreeNode child = null;
		TreePath path = event.getTreePath();
		FolderTreeNode parent = (FolderTreeNode)path.getLastPathComponent();

		// we may get passed the parent node, with a child index
		int[] children = event.getChildIndices();
		if(children != null && children.length > 0)
			child = (FolderTreeNode)parent.getChildAt(children[0]);

		return child;
	}

	private TreePath getPathOfNode(FolderTreeNode node)
	{
		TreePath rootPath = new TreePath(getModel().getRoot());
		return rootPath.pathByAddingChild(node);
	}

	class FolderTreeNodeRenderer extends DefaultTreeCellRenderer 
	{
		FolderTreeNodeRenderer()
		{
			label = new JLabel();
			label.setOpaque(true);
			
			rootIcon = getLeafIcon();
			closedIcon = getClosedIcon();
			openIcon = getOpenIcon();
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, 
					boolean isSelected, boolean isExpanded, boolean isLeaf,
					int row, boolean hasFocus) 
		{
			if(row == 0)
			{
				label.setIcon(rootIcon);
				label.setText(observer.getApp().getButtonLabel("FolderTreeRoot"));
			}
			else
			{
				if(isSelected)
					label.setIcon(openIcon);
				else
					label.setIcon(closedIcon);
				FolderTreeNode folderNode = (FolderTreeNode)value;
				BulletinFolder folder = store.findFolder(folderNode.getInternalName());
				String show = "?";
				if(folder != null)
					show = folderNode.getLocalizedName()+ " (" + folder.getBulletinCount() + ")";
				label.setText(show);
			}

			Color foreground = getTextNonSelectionColor();
			Color background = getBackgroundNonSelectionColor();
			if(isSelected)
			{
				foreground = getTextSelectionColor();
				background = getBackgroundSelectionColor();
			}
			label.setForeground(foreground);
			label.setBackground(background);

			return label;
		}
		
		JLabel label;
		Icon rootIcon;
		Icon closedIcon;
		Icon openIcon;
	}

	class FolderTreeCellEditor extends DefaultTreeCellEditor implements CellEditorListener
	{
		FolderTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
		{
			super(tree, renderer);
			addCellEditorListener(this);
		}
		
		// begin CellEditorListener interface
		public void editingStopped(ChangeEvent e)
		{
			String newFolderName = getCellEditorValue().toString();
			System.out.println("editingStopped: " + newFolderName);

			if(newFolderName.equals(oldLocalizedFolderName))
			{
				System.out.println("Same name");
				newFolderName = oldInternalFolderName;
			}
			else if(!store.renameFolder(oldInternalFolderName, newFolderName))
			{
				System.out.println("rename failed");
				newFolderName = oldInternalFolderName;
			}

			TreePath path = getPathOfNode(node);
			getModel().valueForPathChanged(path, newFolderName);
		}
		
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) 
		{
			node = (FolderTreeNode)value;
			oldLocalizedFolderName = node.getLocalizedName();
			oldInternalFolderName = node.getInternalName();
			Component textField = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
			((JTextComponent)editingComponent).selectAll();
			return textField;
		}

		public void editingCanceled(ChangeEvent arg0) 
		{
		}
		// end CellEditorListener interface

		String oldLocalizedFolderName;
		String oldInternalFolderName;
		FolderTreeNode node;
	}


	BulletinStore store;
	UiMainWindow observer;
	DropTarget dropTarget;
}
