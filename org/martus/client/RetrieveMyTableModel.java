package org.martus.client;

import java.util.Vector;

import org.martus.client.MartusApp.ServerErrorException;



public class RetrieveMyTableModel extends RetrieveTableModel 
{
	public RetrieveMyTableModel(MartusApp appToUse, UiProgressRetrieveSummariesDlg retriever)
	{
		super(appToUse, retriever);
	}

	public void Initalize() throws ServerErrorException
	{
		getMySummaries();
		summaries = getSummariesForBulletinsNotInStore(allSummaries);
	}
	
	public String getColumnName(int column)
	{
		if(column == 0)
			return app.getFieldLabel("retrieveflag");

		return app.getFieldLabel(Bulletin.TAGTITLE);
	}

	public int getColumnCount()
	{
		return 2;
	}

	public Object getValueAt(int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)summaries.get(row);
		if(column == 0)
		{
			return new Boolean(summary.isChecked());
		}
		return summary.getTitle();
	}

	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)summaries.get(row);
		if(column == 0)
		{
			summary.setChecked(((Boolean)value).booleanValue());
		}
	}

	public Class getColumnClass(int column)
	{
		if(column == 0)
			return Boolean.class;

		return String.class;
	}
}
