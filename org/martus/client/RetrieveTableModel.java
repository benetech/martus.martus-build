package org.martus.client;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.martus.client.MartusApp.ServerErrorException;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.MartusSignatureException;

abstract public class RetrieveTableModel extends AbstractTableModel
{
	public RetrieveTableModel(MartusApp appToUse)
	{
		app = appToUse;
		summaries = new Vector();
		store = app.getStore();
	}

	abstract public void Initalize() throws ServerErrorException;

	public Vector getSummariesForBulletinsNotInStore(Vector allSummaries) 
	{
		Vector result = new Vector();
		Iterator iterator = allSummaries.iterator();
		while(iterator.hasNext())
		{
			BulletinSummary currentSummary = (BulletinSummary)iterator.next();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(currentSummary.getAccountId(), currentSummary.getLocalId());
			if(store.findBulletinByUniversalId(uid) != null)
				continue;
			result.add(currentSummary);
		}
		return result;
	}

	public void setAllFlags(boolean flagState)
	{
		for(int i = 0; i < summaries.size(); ++i)
			((BulletinSummary)summaries.get(i)).setFlag(flagState);
		fireTableDataChanged();
	}

	public Vector getUniversalIdList()
	{
		Vector uidList = new Vector();

		for(int i = 0; i < summaries.size(); ++i)
		{
			BulletinSummary summary = (BulletinSummary)summaries.get(i);
			if(summary.getFlag())
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(summary.getAccountId(), summary.getLocalId());
				uidList.add(uid);
			}
		}
		return uidList;

	}

	public int getRowCount()
	{
		return summaries.size();
	}

	public boolean isCellEditable(int row, int column)
	{
		if(column == 0)
			return true;

		return false;
	}

	public Vector getMySummaries() throws ServerErrorException
	{
		Vector summaryStrings = app.getMyServerBulletinSummaries();
		return createSummariesFromStrings(app.getAccountId(), summaryStrings);
	}

	public Vector getMyDraftSummaries() throws ServerErrorException
	{
		Vector summaryStrings = app.getMyDraftServerBulletinSummaries();
		return createSummariesFromStrings(app.getAccountId(), summaryStrings);
	}

	public Vector getFieldOfficeSealedSummaries(String fieldOfficeAccountId) throws ServerErrorException
	{
		try 
		{
			NetworkResponse response = app.getCurrentSSLServerProxy().getSealedBulletinIds(app.security, fieldOfficeAccountId);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return createSummariesFromStrings(fieldOfficeAccountId, response.getResultVector());
		} 
		catch (MartusSignatureException e)
		{
			System.out.println("RetrieveTableModle.getFieldOfficeSealedSummaries: " + e);
		}
		throw new ServerErrorException();
	}

	public Vector getFieldOfficeDraftSummaries(String fieldOfficeAccountId) throws ServerErrorException
	{
		try 
		{
			NetworkResponse response = app.getCurrentSSLServerProxy().getDraftBulletinIds(app.security, fieldOfficeAccountId);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return createSummariesFromStrings(fieldOfficeAccountId, response.getResultVector());
		} 
		catch (MartusSignatureException e)
		{
			System.out.println("MartusApp.getFieldOfficeDraftSummaries: " + e);
		}
		throw new ServerErrorException();
	}

	public Vector createSummariesFromStrings(String accountId, Vector summaryStrings)
		throws ServerErrorException 
	{
		Vector allSummaries = new Vector();
		Iterator iterator = summaryStrings.iterator();
		while(iterator.hasNext())
		{
			String pair = (String)iterator.next();
			BulletinSummary bulletinSummary = app.createSummaryFromString(accountId, pair);
			allSummaries.add(bulletinSummary);
		}
		return allSummaries;
	}



	MartusApp app;
	Vector summaries;
	BulletinStore store;
}
