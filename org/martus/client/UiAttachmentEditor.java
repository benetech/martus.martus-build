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
import javax.swing.table.AbstractTableModel;

import org.martus.common.AttachmentProxy;

import MartusJava.UiMainWindow;


public class UiAttachmentEditor extends JPanel
{
	public UiAttachmentEditor(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		ParagraphLayout layout = new ParagraphLayout();
		setLayout(layout);

		model = new AttachmentTableModel();

		table = new JTable(model);
		table.createDefaultColumnsFromModel();
		table.setColumnSelectionAllowed(false);
		Box hbox = Box.createHorizontalBox();
		Box vbox = Box.createVerticalBox();

		vbox.add(new JScrollPane(table));
		
		MartusApp app = mainWindowToUse.getApp();
		JButton add = new JButton(app.getButtonLabel("addattachment"));
		add.addActionListener(new AddHandler());
		hbox.add(add);
		remove = new JButton(app.getButtonLabel("removeattachment"));
		remove.addActionListener(new RemoveHandler());
		remove.setEnabled(false);
		hbox.add(remove);
		hbox.add(Box.createHorizontalGlue());
		vbox.add(hbox);
		add(vbox);

		Dimension d = table.getPreferredScrollableViewportSize();
		int rowHeight = table.getRowHeight() + table.getRowMargin() ;
		d.height = VISIBLE_ROW_COUNT * rowHeight;
		table.setPreferredScrollableViewportSize(d);
	}

	public AttachmentProxy[] getAttachments()
	{
		return model.getAttachments();
	}
	
	public void addAttachment(AttachmentProxy a)
	{
		model.add(a);
	}
	
	public void clearAttachments()
	{
		model.clear();
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
			remove.setEnabled(false);
			fireTableDataChanged();
		}
		
		public void add(AttachmentProxy a)
		{
			attachmentList.add(a);
			remove.setEnabled(true);
			fireTableDataChanged();
		}

		public void remove(int row)
		{
			attachmentList.remove(row);
			if(getRowCount() == 0)
				remove.setEnabled(false);
			fireTableDataChanged();
		}

		public AttachmentProxy[] getAttachments()
		{
			AttachmentProxy[] list = new AttachmentProxy[attachmentList.size()];
			for(int i = 0; i < list.length; ++i)
				list[i] = (AttachmentProxy)attachmentList.get(i);
				
			return list;
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
			return mainWindow.getApp().getButtonLabel("attachmentlabel");
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
	
	class AddHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			JFileChooser chooser = new JFileChooser();
			File last = mainWindow.getLastAttachmentLoadDirectory();
			if(last != null)
				chooser.setCurrentDirectory(last);
			int returnVal = chooser.showOpenDialog(UiAttachmentEditor.this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				File newAttachmentFile = chooser.getSelectedFile();
				mainWindow.setLastAttachmentLoadDirectory(chooser.getCurrentDirectory());
				AttachmentProxy a = new AttachmentProxy(newAttachmentFile);
				model.add(a);
			}
		}
	}

	class RemoveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			int[] rows = table.getSelectedRows();
			if(rows.length == 0)
				return;
			if(!mainWindow.confirmDlg(null,"RemoveAttachment"))
				return;
			for(int i = rows.length-1; i >= 0; --i)
			{
				model.remove(rows[i]);
			}
		}
	}

	JTable table;
	JButton remove;
	AttachmentTableModel model;
	UiMainWindow mainWindow;
	static final int VISIBLE_ROW_COUNT = 4;
}
