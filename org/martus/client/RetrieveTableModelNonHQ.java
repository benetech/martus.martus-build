package org.martus.client;



abstract public class RetrieveTableModelNonHQ extends RetrieveTableModel {

	public RetrieveTableModelNonHQ(MartusApp appToUse) 
	{
		super(appToUse);
	}

	public String getColumnName(int column)
	{
		switch(column)
		{
		case 0:
			return app.getFieldLabel("retrieveflag");
		case 1:
			return app.getFieldLabel(Bulletin.TAGTITLE);
		case 2:
		default:
			return app.getFieldLabel("BulletinSize");
		}
	}

	public int getColumnCount()
	{
		return 3;
	}

	public Object getValueAt(int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		switch(column)
		{
		case 0:
			return new Boolean(summary.isChecked());
		case 1:
			return summary.getTitle();
		case 2:
				return getSizeInKbytes(summary.getSize());
		default:
			return "";
		}
	}

	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == 0)
			summary.setChecked(((Boolean)value).booleanValue());
	}

	public Class getColumnClass(int column)
	{
		switch(column)
		{
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		default:
			return null;
		}
	}
}
