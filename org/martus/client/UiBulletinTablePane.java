/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client;

import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.martus.common.UniversalId;

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
		if(!parent.isMainWindowInitalizing())
			selectFirstBulletin();
	}
	
	public UniversalId[] getSelectedBulletinUids()
	{
		return table.getSelectedBulletinUids();
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
		if(folder.equals(table.getFolder()))
		{
			UniversalId[] selected = table.getSelectedBulletinUids();
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

	public void doModifyBulletin()
	{
		table.doModifyBulletin();
	}

	public void doCutBulletins()
	{
		table.doCutBulletins();
	}

	public void doCopyBulletins()
	{
		table.doCopyBulletins();
	}

	public void doPasteBulletin()
	{
		table.doPasteBulletin();
	}

	public void doDiscardBulletins()
	{
		table.doDiscardBulletins();
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
