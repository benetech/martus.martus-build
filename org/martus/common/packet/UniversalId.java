/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

package org.martus.common.packet;

import java.io.Serializable;
import java.rmi.server.UID;

public class UniversalId implements Comparable, Serializable
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
