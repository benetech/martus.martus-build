
package org.martus.client;
public class BulletinSummary
{
	public BulletinSummary(String accountIdToUse, String localIdToUse, String titleToUse, String authorToUse)
	{
		accountId = accountIdToUse;
		localId = localIdToUse;
		title = titleToUse;
		author = authorToUse;
		size = 0;
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
	

	private String accountId;
	String localId;
	String title;
	String author;
	int size;
	boolean checkedFlag;
	boolean downloadable;
}
