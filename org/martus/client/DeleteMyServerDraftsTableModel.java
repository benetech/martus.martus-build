package org.martus.client;

import org.martus.client.MartusApp.ServerErrorException;

public class DeleteMyServerDraftsTableModel extends RetrieveTableModel
{

	public DeleteMyServerDraftsTableModel(MartusApp appToUse)
	{
		super(appToUse);
	}

	public void initialize(UiProgressRetrieveSummariesDlg progressDlg) throws ServerErrorException
	{
		setProgressDialog(progressDlg);
		getMyDraftSummaries();
		setCurrentSummaries();
	}

	public int getColumnCount()
	{
		return 2;
	}

	public String getColumnName(int column)
	{
		if(column == 0)
			return app.getFieldLabel("DeleteFlag");

		return app.getFieldLabel(Bulletin.TAGTITLE);
	}

	public Class getColumnClass(int column)
	{
		if(column == 0)
			return Boolean.class;

		return String.class;
	}

	public Object getValueAt(int row, int column) 
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == 0)
		{
			return new Boolean(summary.isChecked());
		}
		return summary.getTitle();
	}

	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == 0)
		{
			summary.setChecked(((Boolean)value).booleanValue());
		}
	}


}
