package org.martus.common;

import java.rmi.server.UID;

public class UniversalId implements Comparable
{
	public static class NotUniversalIdException extends Exception {}
	
	public static UniversalId createFromAccountAndLocalId(String accountId, String localId)
	{
		return new UniversalId(accountId, localId);
	}
	
	public static UniversalId createFromAccountAndPrefix(String accountId, String prefix)
	{
		String localId = prefix + new UID().toString();
		return new UniversalId(accountId, localId);
	}
	
	public static UniversalId createDummyUniversalId()
	{
		return createFromAccountAndPrefix("DummyAccount", "Dummy");
	}

	static UniversalId createDummyFromString(String uidAsString)
	{
		String accountId = uidAsString.substring(0, 1);
		String localId = uidAsString.substring(2);
		return createFromAccountAndLocalId(accountId, localId);
	}
	
	public static UniversalId createFromString(String uidAsString) throws 
			NotUniversalIdException
	{
		int dashAt = uidAsString.indexOf("-");
		if(dashAt < 0)
			throw new NotUniversalIdException();
			
		String accountId = uidAsString.substring(0, dashAt);
		String localId = uidAsString.substring(dashAt + 1);
		return createFromAccountAndLocalId(accountId, localId);
	}

	private UniversalId(String accountIdToUse, String localIdToUse)
	{
		setAccountId(accountIdToUse);
		setLocalId(localIdToUse);
	}
	
	public String getAccountId()
	{
		return accountId;
	}
	
	public String getLocalId()
	{
		return localId;
	}
	
	public String toString()
	{
		return getAccountId() + "-" + getLocalId();
	}
	
	public boolean equals(Object otherObject)
	{
		if(otherObject == this)
			return true;
		if(otherObject == null)
			return false;
		if(otherObject.getClass() != getClass())
			return false;

		UniversalId otherId = (UniversalId)otherObject;
		if(!otherId.getAccountId().equals(getAccountId()))
			return false;
		if(!otherId.getLocalId().equals(getLocalId()))
			return false;
			
		return true;
	}
	
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	public int compareTo(Object other)
	{
		return toString().compareTo(((UniversalId)other).toString());
	}

	public void setAccountId(String newAccountId)
	{
		accountId = newAccountId;
	}
	
	public void setLocalId(String newLocalId)
	{
		localId = newLocalId.replace(':', '-');
	}
	
	private String accountId;
	private String localId;
}
