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

package org.martus.client.swingui.foldertree;

import org.martus.client.core.BulletinFolder;
import org.martus.client.swingui.UiLocalization;

public class FolderNode
{
	FolderNode(UiLocalization localizationToUse)
	{
		this("??", localizationToUse);
	}

	public FolderNode(String internalNameToUse, UiLocalization localizationToUse)
	{
		setInternalName(internalNameToUse);
		localization = localizationToUse;
	}
	
	String getInternalName()
	{
		return internalName;
	}
	
	public String getLocalizedName()
	{
		String internalName = getInternalName();
		if(BulletinFolder.isNameLocalized(internalName))
			return localization.getLocalizedFolderName(internalName);
		return internalName;
	}
	
	public String toString()
	{
		return getLocalizedName();
	}
	
	void setInternalName(String newInternalName)
	{
		internalName = newInternalName;
	}
	
	String internalName;
	UiLocalization localization;
}
