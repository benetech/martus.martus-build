package org.martus.client;

import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.UniversalId;

public class BulletinFolder
{
	final int ASCENDING = 1;
	final int DESCENDING = -ASCENDING;

	BulletinFolder(BulletinStore storeToUse, String nameToUse)
	{
		store = storeToUse;
		name = nameToUse;

		rawIdList = new Vector();
		sortedIdList = null;
	}

	public BulletinStore getStore()
	{
		return store;
	}

	public synchronized void setName(String newName)
	{
		if(canRename)
		{
			name = newName;
		}
	}

	public String getName()
	{
		return name;
	}

	public void preventRename()
	{
		canRename = false;
	}

	public boolean canRename()
	{
		return canRename;
	}

	public void preventDelete()
	{
		canDelete = false;
	}

	public boolean canDelete()
	{
		return canDelete;
	}

	public String getStatusAllowed()
	{
		return statusAllowed;
	}

	public void setStatusAllowed(String status)
	{
		statusAllowed = status;
	}

	public int getBulletinCount()
	{
		return rawIdList.size();
	}

	public String sortedBy()
	{
		return sortTag;
	}
	
	public int getSortDirection()
	{
		return sortDir;	
	}
	
	public boolean isVisible()
	{
		return isNameVisible(getName());
	}

	public boolean isLocalized()
	{
		return isNameLocalized(getName());
	}

	public boolean canAdd(String bulletinStatus)
	{
		if(getStatusAllowed() == null)
			return true;

		return (getStatusAllowed().indexOf(bulletinStatus) != -1);
	}

	public synchronized void add(Bulletin b) 
	{
		add(b.getUniversalId());		
	}
	
	synchronized void add(UniversalId id) 
	{
		if(rawIdList.contains(id))
		{
			//System.out.println("already contains " + id);
			sortExisting();
			return;
		}
		
		DatabaseKey key = new DatabaseKey(id);
		Database db = store.getDatabase();
		if(!db.doesRecordExist(key))
		{
			//System.out.println("not in store: " + id);
			return;
		}
		
		rawIdList.add(id);
		insertIntoSortedList(id);
	}

	public synchronized void remove(UniversalId id)
	{
		if(!rawIdList.contains(id))
			return;
		rawIdList.remove(id);
		if(sortedIdList != null)
			sortedIdList.remove(id);
	}

	public synchronized void removeAll()
	{
		rawIdList.clear();
		sortedIdList = null;
	}

	public Bulletin getBulletinSorted(int index)
	{
		UniversalId uid = getBulletinUniversalIdSorted(index);
		if(uid == null)
			return null;
		return store.findBulletinByUniversalId(uid);
	}
	
	public UniversalId getBulletinUniversalIdSorted(int index)
	{
		needSortedIdList();
		if(index < 0 || index >= sortedIdList.size())
			return null;
		return  (UniversalId)sortedIdList.get(index);
	}

	public UniversalId getBulletinUniversalIdUnsorted(int index)
	{
		if(index < 0 || index >= rawIdList.size())
			return null;
		return  (UniversalId)rawIdList.get(index);
	}

	public synchronized boolean contains(Bulletin b)
	{
		UniversalId id = b.getUniversalId();
		return rawIdList.contains(id);
	}

	public void sortBy(String tag)
	{
		if(tag.equals(sortedBy()))
		{
			sortDir = -sortDir;
		}
		else
		{
			sortTag = tag;
			sortDir = ASCENDING;
		}
		sortExisting();
	}

	public int find(UniversalId id)
	{
		needSortedIdList();
		return sortedIdList.indexOf(id);
	}
	
	public static boolean isNameVisible(String folderName)
	{
		return !folderName.startsWith("*");
	}

	public static boolean isNameLocalized(String folderName)
	{
		return folderName.startsWith("%");
	}

	private void insertIntoSortedList(UniversalId uid) 
	{
		if(sortedIdList == null)
			return;
			
		Bulletin b = store.findBulletinByUniversalId(uid);
		String thisValue = b.get(sortTag);
		int index;
		for(index = 0; index < sortedIdList.size(); ++index)
		{
			Bulletin tryBulletin = getBulletinSorted(index);
			if(tryBulletin.get(sortTag).compareTo(thisValue) * sortDir > 0)
				break;
		}
		sortedIdList.insertElementAt(uid, index);
	}

	private synchronized void sortExisting()
	{
		sortedIdList = new Vector();
		for(int i = 0; i < rawIdList.size(); ++i)
		{
			UniversalId id = (UniversalId)rawIdList.get(i);
			insertIntoSortedList(id);
		}
	}
	
	private void needSortedIdList()
	{
		if(sortedIdList == null)
			sortExisting();
	}

	private BulletinStore store;
	private String name;

	private Vector rawIdList;
	private Vector sortedIdList;
	private boolean canRename = true;
	private boolean canDelete = true;
	private String sortTag = "eventdate";
	private int sortDir = ASCENDING;
	private String statusAllowed;

}
