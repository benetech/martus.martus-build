
package org.martus.client;

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
