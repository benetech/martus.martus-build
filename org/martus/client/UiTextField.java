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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public abstract class UiTextField extends UiField
{
	public UiTextField(MartusApp appToUse)
	{
		app = appToUse;
		mouseAdapter = new TextFieldMouseAdapter();
	}

	public void supportContextMenu()
	{
		actionCut = new ActionCut();
		actionCopy = new ActionCopy();
		actionPaste = new ActionPaste();
		actionDelete = new ActionDelete();
		actionSelectAll = new ActionSelectAll();

		menu = new JPopupMenu();
		menu.add(actionCut);
		menu.add(actionCopy);
		menu.add(actionPaste);
		menu.add(actionDelete);
		menu.add(actionSelectAll);

		getEditor().addMouseListener(mouseAdapter);
	}

	public void contextMenu(MouseEvent e)
	{
		JTextComponent editor = getEditor();
		boolean editable = editor.isEditable();
		boolean selected = (editor.getSelectionStart() != editor.getSelectionEnd());

		actionCut.setEnabled(editable && selected);
		actionCopy.setEnabled(selected);
		actionPaste.setEnabled(editable);
		actionDelete.setEnabled(editable && selected);
		actionSelectAll.setEnabled(true);

		menu.show(getEditor(), e.getX(), e.getY());
	}

	public void cut()
	{
		getEditor().cut();
	}

	public void copy()
	{
		getEditor().copy();
	}

	public void paste()
	{
		getEditor().paste();
	}

	public void delete()
	{
		getEditor().replaceSelection("");
	}

	public void selectAll()
	{
		getEditor().selectAll();
	}

	abstract public JTextComponent getEditor();

	class TextFieldMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			super.mouseClicked(e);
			if(e.isMetaDown())
				contextMenu(e);
		}
	}

	class ActionCut extends AbstractAction
	{
		public ActionCut()
		{
			super(app.getMenuLabel("cut"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			cut();
		}
	}

	class ActionCopy extends AbstractAction
	{
		public ActionCopy()
		{
			super(app.getMenuLabel("copy"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			copy();
		}
	}

	class ActionPaste extends AbstractAction
	{
		public ActionPaste()
		{
			super(app.getMenuLabel("paste"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			paste();
		}
	}

	class ActionDelete extends AbstractAction
	{
		public ActionDelete()
		{
			super(app.getMenuLabel("delete"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			delete();
		}
	}

	class ActionSelectAll extends AbstractAction
	{
		public ActionSelectAll()
		{
			super(app.getMenuLabel("selectall"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			selectAll();
		}
	}

	MartusApp app;
	Action actionCut;
	Action actionCopy;
	Action actionPaste;
	Action actionDelete;
	Action actionSelectAll;
	JPopupMenu menu;
	MouseAdapter mouseAdapter;
}

