package org.martus.client;

import org.martus.common.MartusUtilities.ServerErrorException;

public class DeleteMyServerDraftsTableModel extends RetrieveTableModelNonHQ
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

	public String getColumnName(int column)
	{
		if(column == 0)
			return app.getFieldLabel("DeleteFlag");
		if(column == 1)
			return app.getFieldLabel(Bulletin.TAGTITLE);
		return app.getFieldLabel("BulletinSize");
	}
}
