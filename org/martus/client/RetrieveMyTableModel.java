package org.martus.client;

import org.martus.common.MartusUtilities.ServerErrorException;



public class RetrieveMyTableModel extends RetrieveTableModelNonHQ 
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
}
