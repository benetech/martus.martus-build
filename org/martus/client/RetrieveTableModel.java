package org.martus.client;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.martus.client.MartusApp.ServerErrorException;
import org.martus.common.UniversalId;

abstract public class RetrieveTableModel extends AbstractTableModel
{
	public RetrieveTableModel(MartusApp appToUse) throws ServerErrorException
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

	MartusApp app;
	Vector summaries;
	BulletinStore store;
}
