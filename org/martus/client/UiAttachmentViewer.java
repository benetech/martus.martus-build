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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.martus.common.AttachmentProxy;



public class UiAttachmentViewer extends JPanel
{
	public UiAttachmentViewer(UiMainWindow mainWindowToUse, UiBulletinComponent bulletinComponentToUse)
	{
		mainWindow = mainWindowToUse;
		bulletinComponent = bulletinComponentToUse;
		app = mainWindow.getApp();
		model = new AttachmentTableModel();
		ParagraphLayout layout = new ParagraphLayout();
		setLayout(layout);

		attachmentTable = new JTable(model);
		attachmentTable.createDefaultColumnsFromModel();
		attachmentTable.setColumnSelectionAllowed(false);

		Box buttonBox = Box.createHorizontalBox();
		Box vbox = Box.createVerticalBox();
		attachmentPane = new JScrollPane(attachmentTable);
		attachmentPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		attachmentPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
	
		vbox.add(attachmentPane);

		saveButton = new JButton(app.getButtonLabel("saveattachment"));
		saveButton.addActionListener(new SaveHandler());
		saveButton.setEnabled(false);
		buttonBox.add(saveButton);

		buttonBox.add(Box.createHorizontalGlue());
		vbox.add(buttonBox);
		add(vbox);

		resizeTable();
		attachmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void resizeTable() 
	{
		Dimension d = attachmentTable.getPreferredScrollableViewportSize();
		int rowHeight = attachmentTable.getRowHeight() + attachmentTable.getRowMargin() ;
		d.height = model.getRowCount() * rowHeight;
		attachmentTable.setPreferredScrollableViewportSize(d);
		saveButton.setEnabled(model.getRowCount() > 0);
	}
	
	public void addAttachment(AttachmentProxy a)
	{
		model.add(a);
		resizeTable();
	}
	
	public void clearAttachments()
	{
		model.clear();
		resizeTable();
	}

	class AttachmentTableModel extends AbstractTableModel
	{
		public AttachmentTableModel()
		{
			attachmentList = new Vector();
		}
		
		void clear()
		{
			attachmentList.clear();
			fireTableDataChanged();
		}

		public void add(AttachmentProxy a)
		{
			attachmentList.add(a);
			fireTableDataChanged();
		}

		public int getRowCount()
		{
			return attachmentList.size();
		}

		public int getColumnCount()
		{
			return 1;
		}

		public String getColumnName(int column)
		{
			return app.getButtonLabel("attachmentlabel");
		}

		public AttachmentProxy getAttachmentProxyAt(int row, int column)
		{
			return (AttachmentProxy)attachmentList.get(row);
		}

		public Object getValueAt(int row, int column)
		{
			AttachmentProxy a = (AttachmentProxy)attachmentList.get(row);
			return a.getLabel();
		}

		public void setValueAt(Object value, int row, int column)
		{
		}

		public boolean isCellEditable(int row, int column)
		{
			return false;
		}

		private Vector attachmentList;
	}

	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			JFileChooser chooser = new JFileChooser();
			int selection = attachmentTable.getSelectedRow();
			int rowCount = attachmentTable.getRowCount();
			if(selection > rowCount || rowCount <= 0)
				return;

			if(selection == -1)
			{
				if(rowCount == 1)
					selection = 0;
				else
				{
					getToolkit().beep();
					return;
				}
			}
			String fileName = (String)model.getValueAt(selection,1);
				
			chooser.setSelectedFile(new File(fileName));
			File last = mainWindow.getLastAttachmentSaveDirectory();
			if(last != null)
				chooser.setCurrentDirectory(last);
			int returnVal = chooser.showSaveDialog(mainWindow);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				mainWindow.setLastAttachmentSaveDirectory(chooser.getCurrentDirectory());
				File outputFile = chooser.getSelectedFile();
				if(outputFile.exists())
				{
					if(!mainWindow.confirmDlg(mainWindow,"OverWriteExistingFile"))
						return;						
				}
				AttachmentProxy proxy = model.getAttachmentProxyAt(selection,1);
				try 
				{
					Bulletin b = bulletinComponent.getCurrentBulletin();
					b.extractAttachmentToFile(proxy, app.getSecurity(), outputFile);
				} 
				catch(Exception e) 
				{
					mainWindow.notifyDlg(mainWindow, "UnableToSaveAttachment");
					System.out.println("Unable to save file :" + e);
				} 
			}
		}
	}

	UiMainWindow mainWindow;
	UiBulletinComponent bulletinComponent;
	MartusApp app;
	AttachmentTableModel model;
	JTable attachmentTable;
	JButton saveButton;
	JScrollPane attachmentPane;
	static final int VISIBLE_ROW_COUNT = 4;
}
