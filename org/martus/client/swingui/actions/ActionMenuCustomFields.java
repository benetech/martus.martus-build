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

package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;

import org.martus.client.core.BulletinStore;
import org.martus.client.core.CustomFieldSpecValidator;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.FieldSpec;

public class ActionMenuCustomFields extends UiMenuAction
{
	public ActionMenuCustomFields(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "CustomFields");
	}
	
	public void actionPerformed(ActionEvent arg0)
	{
		if(!mainWindow.confirmDlg(mainWindow, "EnterCustomFields"))
			return;
		
		MartusApp app = mainWindow.getApp();
		BulletinStore store = app.getStore();
		FieldSpec[] existingSpecs = store.getPublicFieldTags();
		FieldSpec[] newSpecs = getCustomizedFieldsFromUser(existingSpecs);
		if(newSpecs == null)
			return;
			
		store.setPublicFieldTags(newSpecs);
		String fieldSpecString = FieldSpec.buildFieldListString(newSpecs);
		app.getConfigInfo().setCustomFieldSpecs(fieldSpecString);

		try
		{
			app.saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			mainWindow.notifyDlg(mainWindow, "ErrorSavingConfig");
		}
	}

	private FieldSpec[] getCustomizedFieldsFromUser(FieldSpec[] existingSpecs)
	{
		String existingTags = FieldSpec.buildFieldListString(existingSpecs);
		while(true)
		{
			String newTags = mainWindow.getStringInput("CustomFields", "", existingTags);
			if(newTags == null)
				return null;
				
			if(newTags.length() == 0)
			{
				if(mainWindow.confirmDlg(mainWindow, "UndoCustomFields"))
					return FieldSpec.getDefaultPublicFieldSpecs();
				continue;
			}
 
			FieldSpec[] newSpecs = FieldSpec.parseFieldSpecsFromString(newTags);
			CustomFieldSpecValidator checker = new CustomFieldSpecValidator(newSpecs);
			if(checker.isValid())
				return newSpecs;
				
			mainWindow.notifyDlg(mainWindow, "ErrorInCustomFields");
			existingTags = newTags;
		}
	}
	
}
