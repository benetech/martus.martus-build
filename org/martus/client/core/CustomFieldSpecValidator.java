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

package org.martus.client.core;

import java.util.Vector;

import org.martus.common.FieldSpec;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;

public class CustomFieldSpecValidator
{
	public CustomFieldSpecValidator(FieldSpec[] specsToCheck)
	{
		if(specsToCheck == null)
		{
			isNull = true;
			return;
		}
		
		checkForRequiredFields(specsToCheck);
		checkForBlankTags(specsToCheck);
		checkForDuplicateFields(specsToCheck);
		checkForMissingCustomLabels(specsToCheck);
		checkForLabelsOnStandardFields(specsToCheck);
	}
		
	public boolean isValid()
	{
		if(isNull)
			return false;
		
		if(blankTagCount != 0)
			return false;
			
		if(missingTags.size() != 0)
			return false;
			
		if(duplicateTags.size() != 0)
			return false;
			
		if(customTagsWithoutLabels.size() != 0)
			return false;
			
		if(standardTagsWithLabels.size() != 0)
			return false;
			
		return true;
	}
	
	private void checkForRequiredFields(FieldSpec[] specsToCheck)
	{
		missingTags = new Vector();
		missingTags.add(BulletinConstants.TAGAUTHOR);
		missingTags.add(BulletinConstants.TAGLANGUAGE);
		missingTags.add(BulletinConstants.TAGENTRYDATE);
		missingTags.add(BulletinConstants.TAGTITLE);
		for (int i = 0; i < specsToCheck.length; i++)
		{
			String tag = specsToCheck[i].getTag();
			if(missingTags.contains(tag))
			missingTags.remove(tag);
		}
	}
	
	private void checkForBlankTags(FieldSpec[] specsToCheck)
	{
		blankTagCount = 0;
		for (int i = 0; i < specsToCheck.length; i++)
		{
			String tag = specsToCheck[i].getTag();
			if(tag.length() == 0)
				++blankTagCount;
		}
	}
	
	private void checkForDuplicateFields(FieldSpec[] specsToCheck)
	{
		Vector foundTags = new Vector();
		duplicateTags = new Vector();
		for (int i = 0; i < specsToCheck.length; i++)
		{
			String tag = specsToCheck[i].getTag();
			if(foundTags.contains(tag))
				duplicateTags.add(tag);
			foundTags.add(tag);
		}
	}
	
	private void checkForMissingCustomLabels(FieldSpec[] specsToCheck)
	{
		customTagsWithoutLabels = new Vector();
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec spec = specsToCheck[i]; 
			String tag = spec.getTag();
			if(isCustomFieldTag(tag) && spec.getLabel().equals(""))
				customTagsWithoutLabels.add(tag);
		}
	}

	private void checkForLabelsOnStandardFields(FieldSpec[] specsToCheck)
	{
		standardTagsWithLabels = new Vector();
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec spec = specsToCheck[i]; 
			String tag = spec.getTag();
			if(!isCustomFieldTag(tag) && !spec.getLabel().equals(""))
				standardTagsWithLabels.add(tag);
		}
	}
	
	private boolean isCustomFieldTag(String tag)
	{
		Vector standardTags = Bulletin.getDefaultPublicFieldTags();
		if(standardTags.contains(tag))
			return false;
		return true;
	}
	
	boolean isNull;
	int blankTagCount;
	Vector missingTags;
	Vector duplicateTags;
	Vector customTagsWithoutLabels;
	Vector standardTagsWithLabels;
}
