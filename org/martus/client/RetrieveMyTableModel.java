package org.martus.client;

import java.util.Vector;

import org.martus.client.MartusApp.ServerErrorException;



public class RetrieveMyTableModel extends RetrieveTableModel 
{
	public RetrieveMyTableModel(MartusApp appToUse)
	{
		super(appToUse);
	}

	public void initialize(UiProgressRetrieveSummariesDlg progressDlg) throws ServerErrorException
	{
		setProgressDialog(progressDlg);

		getMySummaries();
		setCurrentSummaries();
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
		default:
			return new Integer(summary.getSize()).toString();
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
		default:
			return String.class;
		}
	}
}
