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
import org.martus.client.swingui.fields.UiAttachmentEditor;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiMultilineTextEditor;
import org.martus.client.swingui.fields.UiNormalTextEditor;
import org.martus.common.AttachmentProxy;

public class UiBulletinComponentEditorSection extends UiBulletinComponentSection
{

	public UiBulletinComponentEditorSection(UiBulletinComponent bulletinComponentToUse, UiMainWindow ownerToUse, boolean encrypted)
	{
		super(ownerToUse.getLocalization(), encrypted);
		owner = ownerToUse;
		bulletinComponent = bulletinComponentToUse;
	}

	public UiField createNormalField()
	{
		return new UiNormalTextEditor(localization);
	}

	public UiField createMultilineField()
	{
		return new UiMultilineTextEditor(localization);
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceEditor(choices);
	}

	public UiField createDateField()
	{
		return new UiDateEditor(localization);
	}

	public void addAttachment(AttachmentProxy a)
	{
		attachmentEditor.addAttachment(a);
	}

	public void clearAttachments()
	{
		attachmentEditor.clearAttachments();
	}

	public void createAttachmentTable()
	{
		attachmentEditor = new UiAttachmentEditor(owner);
		add(attachmentEditor);
	}

	UiAttachmentEditor attachmentEditor;
	UiMainWindow owner;
	UiBulletinComponent bulletinComponent;
}
