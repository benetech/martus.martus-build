package org.martus.client;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.event.*;

import org.martus.client.*;

public class UiBulletinTablePane extends JScrollPane
{
    public UiBulletinTablePane(UiMainWindow mainWindow)
	{
		parent = mainWindow;

		table = new UiBulletinTable(parent);

		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		getViewport().add(table);

		dropTarget = new DropTarget(getViewport(), table.getDropAdapter());
		addMouseListener(new TablePaneMouseAdapter());

	}

	public void forceRefresh()
	{
		folderContentsHaveChanged(table.getFolder());
	}

	public void setFolder(BulletinFolder folder)
	{
		table.setFolder(folder);
		table.selectFirstBulletin();
		refreshPreview();
	}

	public void selectFirstBulletin()
	{
		table.selectFirstBulletin();
	}
	
	public int getCurrentBulletinIndex()
	{
		return(table.getSelectedRow());	
	}
	
	public void setCurrentBulletinIndex(int index)
	{
		table.selectRow(index);	
	}
	
	public void folderContentsHaveChanged(BulletinFolder folder)
	{
		if(folder.equals(table.getFolder()))
		{
			Bulletin selected = table.getSelectedBulletin();
			table.setFolder(folder);
			table.selectBulletin(selected);
			refreshPreview();
			// the following is required (for unknown reasons)
			// to get the table to redraw (later) if it is currently
			// under a window. Yuck! kbs.
			repaint();
		}
	}

	public void bulletinHasChanged(Bulletin b)
	{
		table.bulletinHasChanged(b);
	}

	public void doEditBulletin()
	{
		table.doEditBulletin();
	}

	public void doCutBulletin()
	{
		table.doCutBulletin();
	}

	public void doCopyBulletin()
	{
		table.doCopyBulletin();
	}

	public void doPasteBulletin()
	{
		table.doPasteBulletin();
	}

	public void refreshPreview()
	{
		Bulletin b = table.getSelectedBulletin();
		parent.bulletinSelectionHasChanged(b);
	}

	class TablePaneMouseAdapter extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			super.mousePressed(e);
			handleRightClick(e);
		}
		public void mouseReleased(MouseEvent e)
		{
			super.mouseReleased(e);
			handleRightClick(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			super.mouseClicked(e);
			handleRightClick(e);
		}
		private void handleRightClick(MouseEvent e)
		{
			if(!e.isPopupTrigger())
				return;
			table.doPopupMenu(UiBulletinTablePane.this, e.getX(), e.getY());
		}
	}

	private UiMainWindow parent;

	private UiBulletinTable table;
	private DropTarget dropTarget;
}
