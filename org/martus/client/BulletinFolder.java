package org.martus.client;

import java.util.*;

import org.martus.common.*;

public class BulletinFolder
{
	final int ASCENDING = 1;
	final int DESCENDING = -ASCENDING;

	BulletinFolder(BulletinStore storeToUse, String nameToUse)
	{
		store = storeToUse;
		name = nameToUse;

		idList = new Vector();
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
		return idList.size();
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

	public boolean canAdd(String bulletinStatus)
	{
		if(getStatusAllowed() == null)
			return true;

		return (getStatusAllowed().indexOf(bulletinStatus) != -1);
	}

	public synchronized void add(Bulletin b)
	{
		UniversalId id = b.getUniversalId();
		if(idList.contains(id))
		{
			//System.out.println("already contains " + id);
			return;
		}

		if(store.findBulletinByUniversalId(id) == null)
		{
			//System.out.println("not in store: " + id);
			return;
		}

		String thisValue = b.get(sortTag);
		int index;
		for(index = 0; index < idList.size(); ++index)
		{
			Bulletin tryBulletin = getBulletin(index);
			if(tryBulletin.get(sortTag).compareTo(thisValue) * sortDir > 0)
				break;
		}
		idList.insertElementAt(id, index);
	}

	public synchronized void remove(UniversalId id)
	{
		if(!idList.contains(id))
			return;
		idList.remove(id);
	}

	public synchronized void removeAll()
	{
		idList.clear();
	}

	public Bulletin getBulletin(int index)
	{
		UniversalId uid = getBulletinUniversalId(index);
		if(uid == null)
			return null;
		return store.findBulletinByUniversalId(uid);
	}
	
	public UniversalId getBulletinUniversalId(int index)
	{
		if(index < 0 || index >= idList.size())
			return null;
		return  (UniversalId)idList.get(index);
	}

	public boolean contains(Bulletin b)
	{
		UniversalId id = b.getUniversalId();
		return idList.contains(id);
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
		return idList.indexOf(id);
	}
	
	public static boolean isNameVisible(String folderName)
	{
		return !folderName.startsWith("*");
	}

	private synchronized void sortExisting()
	{
		int size = idList.size();
		Vector oldIdList = idList;
		idList = new Vector();
		for(int i = 0; i < size; ++i)
		{
			UniversalId id = (UniversalId)oldIdList.get(i);
			add(store.findBulletinByUniversalId(id));
		}
	}

	private BulletinStore store;
	private String name;

	private Vector idList;
	private boolean canRename = true;
	private boolean canDelete = true;
	private String sortTag = "eventdate";
	private int sortDir = ASCENDING;
	private String statusAllowed;

}
