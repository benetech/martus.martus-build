package org.martus.client;

import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

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

	public void setFolder(BulletinFolder folder)
	{
		table.setFolder(folder);
		selectFirstBulletin();
	}
	
	public Bulletin[] getSelectedBulletins()
	{
		return table.getSelectedBulletins();
	}

	public Bulletin getSingleSelectedBulletin()
	{
		return table.getSingleSelectedBulletin();
	}

	public void selectFirstBulletin()
	{
		setCurrentBulletinIndex(0);
	}

	public void selectLastBulletin()
	{
		setCurrentBulletinIndex(table.getRowCount()-1);
	}
	
	public int getCurrentBulletinIndex()
	{
		return(table.getSelectedRow());	
	}
	
	public void setCurrentBulletinIndex(int index)
	{
		table.selectRow(index);	
		parent.bulletinSelectionHasChanged();
	}
	
	public void folderContentsHaveChanged(BulletinFolder folder)
	{
System.out.println("UiBulletinTablePane.folderContentsHaveChanged");
		if(folder.equals(table.getFolder()))
		{
			Bulletin[] selected = table.getSelectedBulletins();
			table.setFolder(folder);
			table.selectBulletins(selected);
			parent.bulletinSelectionHasChanged();
			// the following is required (for unknown reasons)
			// to get the table to redraw (later) if it is currently
			// under a window. Yuck! kbs.
			repaint();
		}
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		table.bulletinContentsHaveChanged(b);
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

	public void doDiscardBulletin()
	{
		table.doDiscardBulletin();
	}

	class TablePaneMouseAdapter extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			if(e.isPopupTrigger())
				handleRightClick(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			if(e.isPopupTrigger())
				handleRightClick(e);
		}


		public void mouseClicked(MouseEvent e)
		{
			if(e.isPopupTrigger())
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
