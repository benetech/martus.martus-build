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

import org.martus.common.Bulletin;
import org.martus.common.FieldDataPacket;

public class BulletinSummary
{
	public BulletinSummary(String accountIdToUse, String localIdToUse, FieldDataPacket fdpToUse, int sizeToUse)
	{
		accountId = accountIdToUse;
		localId = localIdToUse;
		size = sizeToUse;
		title = fdpToUse.get(Bulletin.TAGTITLE);
		author = fdpToUse.get(Bulletin.TAGAUTHOR);
		fdp = fdpToUse;
	}

	public void setChecked(boolean newValue)
	{
		if(downloadable)
			checkedFlag = newValue;
	}

	public boolean isChecked()
	{
		return checkedFlag;
	}

	public String getAccountId()
	{
		return accountId;
	}

	public String getLocalId()
	{
		return localId;
	}

	public String getTitle()
	{
		return title;
	}

	public String getAuthor()
	{
		return author;
	}

	public boolean isDownloadable()
	{
		return downloadable;
	}

	public void setDownloadable(boolean downloadable)
	{
		this.downloadable = downloadable;
	}

	public int getSize()
	{
		return size;
	}

	public FieldDataPacket getFieldDataPacket()
	{
		return fdp;
	}

	private FieldDataPacket fdp;
	private String accountId;
	String localId;
	String title;
	String author;
	int size;
	boolean checkedFlag;
	boolean downloadable;
}
