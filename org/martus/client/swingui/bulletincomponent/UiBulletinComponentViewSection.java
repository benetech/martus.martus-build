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

package org.martus.client.swingui.bulletincomponent;

import org.martus.client.core.ChoiceItem;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiAttachmentViewer;
import org.martus.client.swingui.fields.UiChoiceViewer;
import org.martus.client.swingui.fields.UiDateViewer;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFlexiDateViewer;
import org.martus.client.swingui.fields.UiMultilineViewer;
import org.martus.client.swingui.fields.UiNormalTextViewer;
import org.martus.common.bulletin.AttachmentProxy;

public class UiBulletinComponentViewSection extends UiBulletinComponentSection
{

	public UiBulletinComponentViewSection(UiBulletinComponent bulletinComponentToUse, UiMainWindow ownerToUse, boolean encrypted)
	{
		super(ownerToUse.getLocalization(), encrypted);
		owner = ownerToUse;
		bulletinComponent = bulletinComponentToUse;
	}

	public UiField createFlexiDateField()
	{		
		return new UiFlexiDateViewer(localization); 
	}
	
	public UiField createDateField()
	{
		return new UiDateViewer(localization);
	}
	
	public UiField createNormalField()
	{
		return new UiNormalTextViewer(localization);
	}

	public UiField createMultilineField()
	{
		return new UiMultilineViewer(localization);
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceViewer(choices);
	}

	public void createAttachmentTable()
	{
		attachmentViewer = new UiAttachmentViewer(owner, bulletinComponent);
		add(attachmentViewer);
	}

	public void addAttachment(AttachmentProxy a)
	{
		attachmentViewer.addAttachment(a);
	}

	public void clearAttachments()
	{
		attachmentViewer.clearAttachments();
	}

	public UiAttachmentViewer attachmentViewer;
	UiMainWindow owner;
	UiBulletinComponent bulletinComponent;
}
