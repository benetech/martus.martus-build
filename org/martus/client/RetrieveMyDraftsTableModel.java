package org.martus.client;

import org.martus.common.MartusUtilities.ServerErrorException;



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
