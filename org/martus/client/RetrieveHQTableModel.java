package org.martus.client;

import java.util.Vector;

import org.martus.client.MartusApp.ServerErrorException;



public class RetrieveHQTableModel extends RetrieveTableModel 
{
	public RetrieveHQTableModel(MartusApp appToUse) throws 
		ServerErrorException 
	{
		super(appToUse);
		Initalize();
	}

	public void Initalize() throws ServerErrorException
	{
		Vector accounts = app.getFieldOfficeAccounts();
		Vector allSummaries = new Vector();
		for(int a = 0; a < accounts.size(); ++a)
		{
			String accountId = (String)accounts.get(a);
			allSummaries.addAll(getFieldOfficeSealedSummaries(accountId));
			
		}
		summaries = getSummariesForBulletinsNotInStore(allSummaries);
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
				return app.getFieldLabel(Bulletin.TAGAUTHOR);
		}
		return "";		
	}

	public int getColumnCount()
	{
		return 3;
	}

	public Object getValueAt(int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)summaries.get(row);
		switch(column)
		{
			case 0:
				return new Boolean(summary.getFlag());
			case 1:
				return summary.getTitle();
			case 2:
				return summary.getAuthor();
		}
		return "";		
	}

	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)summaries.get(row);
		if(column == 0)
		{
			summary.setFlag(((Boolean)value).booleanValue());
		}
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
				return String.class;
		}
		return null;		
	}
}
