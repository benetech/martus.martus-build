/* $Id: UiTextField.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;

import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import org.martus.client.*;

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

