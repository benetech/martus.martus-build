package org.martus.client;

import java.util.Vector;

import org.martus.common.MartusUtilities.ServerErrorException;



public class RetrieveHQTableModel extends RetrieveTableModelHQ 
{
	public RetrieveHQTableModel(MartusApp appToUse)
	{
		super(appToUse);
	}

	public void initialize(UiProgressRetrieveSummariesDlg progressDlg) throws ServerErrorException
	{
		setProgressDialog(progressDlg);
		Vector accounts = app.getFieldOfficeAccounts();
		for(int a = 0; a < accounts.size(); ++a)
		{
			String accountId = (String)accounts.get(a);
			getFieldOfficeSealedSummaries(accountId);
		}
		setCurrentSummaries();
	}
}
