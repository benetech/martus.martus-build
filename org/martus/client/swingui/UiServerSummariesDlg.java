/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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

package org.martus.client.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.martus.client.core.MartusApp;
import org.martus.common.FieldDataPacket;

public class UiServerSummariesDlg extends JDialog
{
	public UiServerSummariesDlg(UiMainWindow owner, RetrieveTableModel tableModel,
			String windowTitleTag, String topMessageTag, String okButtonTag, String noneSelectedTag)
	{
		super(owner, owner.getApp().getWindowTitle(windowTitleTag), true);
		mainWindow = owner;
		model = tableModel;
		this.noneSelectedTag = noneSelectedTag;
		initialize(topMessageTag, okButtonTag);
	}

	void initialize(String topMessageTag, String okButtonTag)
	{
		disabledBackgroundColor = getBackground();
		JLabel label = new JLabel("");
		String topMessageText = getApp().getFieldLabel(topMessageTag);
		UiWrappedTextArea retrieveMessage = new UiWrappedTextArea(mainWindow, topMessageText);
		tableBox = Box.createVerticalBox();
		table = new RetrieveJTable(model);
		oldBooleanRenderer = table.getDefaultRenderer(Boolean.class);
		oldIntegerRenderer = table.getDefaultRenderer(Integer.class);
		table.setDefaultRenderer(Boolean.class, new BooleanRenderer());
		table.setDefaultRenderer(Integer.class, new IntegerRenderer());
		table.setDefaultRenderer(String.class, new StringRenderer());

		table.createDefaultColumnsFromModel();
		tableBox.add(table.getTableHeader());
		tableBox.add(new JScrollPane(table));
		Dimension tableBoxSize = tableBox.getPreferredSize();
		tableBoxSize.height = 350; //To fit in 800x600
		tableBox.setPreferredSize(tableBoxSize);

		JRadioButton downloadableSummaries = new JRadioButton(getApp().getButtonLabel("DownloadableSummaries"), true);
		downloadableSummaries.addActionListener(new ChangeDownloadableSummariesHandler());
		JRadioButton allSummaries = new JRadioButton(getApp().getButtonLabel("AllSummaries"), false);
		allSummaries.addActionListener(new ChangeAllSummariesHandler());
		ButtonGroup summariesGroup = new ButtonGroup();
		summariesGroup.add(downloadableSummaries);
		summariesGroup.add(allSummaries);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(0, 1));
		radioPanel.add(downloadableSummaries);
		radioPanel.add(allSummaries);

		JButton ok = new JButton(getApp().getButtonLabel(okButtonTag));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(getApp().getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		JButton preview = new JButton(getApp().getButtonLabel("Preview"));
		preview.addActionListener(new PreviewHandler());

		JButton checkAll = new JButton(getApp().getButtonLabel("checkall"));
		checkAll.addActionListener(new CheckAllHandler());
		JButton unCheckAll = new JButton(getApp().getButtonLabel("uncheckall"));
		unCheckAll.addActionListener(new UnCheckAllHandler());

		getContentPane().setLayout(new ParagraphLayout());
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(retrieveMessage);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(label);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(tableBox);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(radioPanel);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(checkAll);
		getContentPane().add(unCheckAll);
		getContentPane().add(preview);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);


		getRootPane().setDefaultButton(ok);
		mainWindow.centerDlg(this);
		setResizable(true);
		show();
	}

	public boolean getResult()
	{
		return result;
	}

	public Vector getUniversalIdList()
	{
		return model.getUniversalIdList();
	}

	MartusApp getApp()
	{
		return mainWindow.getApp();
	}

	class RetrieveJTable extends JTable
	{
		public RetrieveJTable(TableModel model)
		{
			super(model);
		}

		public void doLayout()
		{
			Dimension tableBoxSize = tableBox.getPreferredSize();
			TableColumn firstColumn = getColumnModel().getColumn(0);
			int numberOfColumns = getColumnModel().getColumnCount();
			firstColumn.setMaxWidth(tableBoxSize.width/2);
			firstColumn.setPreferredWidth(tableBoxSize.width/(numberOfColumns+1));

			TableColumn lastColumn = getColumnModel().getColumn(numberOfColumns-1);
			lastColumn.setMaxWidth(tableBoxSize.width/2);
			lastColumn.setPreferredWidth(tableBoxSize.width/(numberOfColumns+1));

			super.doLayout();
		}
	}

	class BooleanRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			Component cell = oldBooleanRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(enabledBackgroundColor == null)
				enabledBackgroundColor = cell.getBackground();
			if(model.isDownloadable(row))
			{
				cell.setEnabled(true);
				if(!isSelected)
					cell.setBackground(enabledBackgroundColor);
			}
			else
			{
				cell.setEnabled(false);
				if(!isSelected)
					cell.setBackground(disabledBackgroundColor);
			}
			return cell;
		}
		Color enabledBackgroundColor;
	}

	class IntegerRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			Component cell = oldIntegerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(enabledBackgroundColor == null)
				enabledBackgroundColor = cell.getBackground();
			if(model.isDownloadable(row))
			{
				cell.setEnabled(true);
				if(!isSelected)
					cell.setBackground(enabledBackgroundColor);
			}
			else
			{
				cell.setEnabled(false);
				if(!isSelected)
					cell.setBackground(disabledBackgroundColor);
			}
			return cell;
		}
		Color enabledBackgroundColor;
	}

	class StringRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			if(normalBackgroundColor == null)
			{
				Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				normalBackgroundColor = cell.getBackground();
			}

			if(!model.isDownloadable(row))
				setBackground(disabledBackgroundColor);
			else
				setBackground(normalBackgroundColor);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		Color normalBackgroundColor;
	}


	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			Vector uidList = getUniversalIdList();
			if( uidList.size() == 0)
			{
				mainWindow.notifyDlg(mainWindow, noneSelectedTag);
				return;
			}

			result = true;
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}

	class PreviewHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{

			int[] row = table.getSelectedRows();
			if(row.length <= 0)
			{
				mainWindow.notifyDlg(mainWindow, "PreviewNoBulletinsSelected");
			}
			else if(row.length==1)
			{
				FieldDataPacket fdp = model.getBulletinSummary(row[0]).getFieldDataPacket();
				new UiBulletinPreviewDlg(mainWindow, fdp);
			}
			else
			{
				mainWindow.notifyDlg(mainWindow, "PreviewOneBulletinOnly");
			}
		}
	}

	class CheckAllHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.setAllFlags(true);
		}
	}

	class UnCheckAllHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.setAllFlags(false);
		}
	}

	class ChangeDownloadableSummariesHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.changeToDownloadableSummaries();
			model.fireTableStructureChanged();
		}
	}

	class ChangeAllSummariesHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.changeToAllSummaries();
			model.fireTableStructureChanged();
		}
	}

	UiMainWindow mainWindow;
	JTextField text;
	boolean result;
	RetrieveJTable table;
	RetrieveTableModel model;
	TableCellRenderer oldBooleanRenderer;
	TableCellRenderer oldIntegerRenderer;
	Color disabledBackgroundColor;
	Box tableBox;
	String noneSelectedTag;
}
