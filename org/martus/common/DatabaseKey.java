package org.martus.common;



public class DatabaseKey implements Comparable
{
	public DatabaseKey(UniversalId uidToUse)
	{
		uid = uidToUse;
		status = statusSealed;
	}

	public UniversalId getUniversalId()
	{
		return uid;
	}
	
	public String getAccountId()
	{
		return getUniversalId().getAccountId();
	}

	public String getLocalId()
	{
		return getUniversalId().getLocalId();
	}
	
	public boolean isSealed()
	{
		return (status == statusSealed);
	}
	
	public boolean isDraft()
	{
		return (status == statusDraft);
	}

	public void setDraft()
	{
		status = statusDraft;
	}
	
	public void setSealed()
	{
		status = statusSealed;
	}

	public boolean equals(Object otherObject)
	{
		if(this == otherObject)
			return true;

		if(otherObject instanceof DatabaseKey)
		{
			DatabaseKey otherKey = (DatabaseKey)otherObject;
			return getString().equals(otherKey.getString());
		}

		return false;
	}

	public int hashCode()
	{
		return getString().hashCode();
	}
	
	public int compareTo(Object other)
	{
		return getString().compareTo(((DatabaseKey)other).getString());
	}

	private String getString()
	{
		String statusCode = "?";
		if(isDraft())
			statusCode = "D";
		else if(isSealed())
			statusCode = "S";
		return statusCode + "-" + uid.toString();
	}
	
	private static final int statusUnknown = 0;
	private static final int statusSealed = 1;
	private static final int statusDraft = 2;
	
	UniversalId uid;
	int status;
}
