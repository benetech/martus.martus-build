package org.martus.client;

import java.util.Vector;

import org.martus.client.MartusApp.ServerErrorException;



public class RetrieveMyDraftsTableModel extends RetrieveTableModelNonHQ 
{
	public RetrieveMyDraftsTableModel(MartusApp appToUse)
	{
		super(appToUse);
	}

	public void initialize(UiProgressRetrieveSummariesDlg progressDlg) throws ServerErrorException
	{
		setProgressDialog(progressDlg);
		getMyDraftSummaries();
		setCurrentSummaries();
	}

}
