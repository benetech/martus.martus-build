package org.martus.client;

import javax.swing.table.AbstractTableModel;

import org.martus.common.UniversalId;

public class BulletinTableModel extends AbstractTableModel
{
    public BulletinTableModel(MartusApp appToUse)
    {
		app = appToUse;
    }

	public void setFolder(BulletinFolder folderToUse)
	{
		if(folder != null)
		{
			fireTableRowsDeleted(0, folder.getBulletinCount());
		}

		folder = folderToUse;
		fireTableRowsInserted(0, folder.getBulletinCount());
	}

	public BulletinFolder getFolder()
	{
		return folder;
	}

	public int getRowCount()
	{
		if(folder == null)
			return 0;

		return folder.getBulletinCount();
	}

	public int getColumnCount()
	{
		return fieldTags.length;
	}

	public Bulletin getBulletin(int rowIndex)
	{
		return folder.getBulletinSorted(rowIndex);
	}

	public int findBulletin(UniversalId uid)
	{
		if(uid == null)
			return -1;

		return folder.find(uid);
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		UniversalId uid = folder.getBulletinUniversalIdSorted(rowIndex);
		if(uid == null)
			return "";

		String fieldTag = fieldTags[columnIndex];
		String value = getFolder().getStore().getFieldData(uid, fieldTag);
		if(fieldTag.equals(Bulletin.TAGSTATUS))
		{
			value = app.getStatusLabel(value);
		}
	 	if(Bulletin.getFieldType(fieldTag) == Bulletin.DATE)
		{
			value = app.convertStoredToDisplay(value);
		}
		return value;
	}

	public String getColumnName(int columnIndex)
	{
		return app.getFieldLabel(getFieldName(columnIndex));
	}

	public String getFieldName(int columnIndex)
	{
		return fieldTags[columnIndex];
	}

	public void sortByColumn(int columnIndex)
	{
		folder.sortBy(getFieldName(columnIndex));
	}

	private final String[] fieldTags =
		{Bulletin.TAGSTATUS, "eventdate", "title", "author"};

	MartusApp app;
	BulletinFolder folder;
}
