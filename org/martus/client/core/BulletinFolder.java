/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.core;

import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.UniversalId;

public class BulletinFolder
{
	public final int ASCENDING = 1;
	public final int DESCENDING = -ASCENDING;

	public BulletinFolder(BulletinStore storeToUse, String nameToUse)
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

		String thisValue = store.getFieldData(uid, sortTag);
		int index;
		for(index = 0; index < sortedIdList.size(); ++index)
		{
			UniversalId tryUid = getBulletinUniversalIdSorted(index);
			String tryValue = getStore().getFieldData(tryUid, sortTag);
			if(tryValue.compareTo(thisValue) * sortDir > 0)
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
