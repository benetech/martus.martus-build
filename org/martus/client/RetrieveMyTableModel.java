package org.martus.client;

import java.util.Vector;

import org.martus.client.MartusApp.ServerErrorException;



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
