/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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

package org.martus.client.swingui;

import java.io.IOException;

import org.martus.client.core.Bulletin;
import org.martus.client.core.MartusApp;
import org.martus.common.AttachmentProxy;
import org.martus.common.MartusCrypto;

public class UiBulletinEditor extends UiBulletinComponent
{
	UiBulletinEditor(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		owner = mainWindowToUse;
		Initalize();
		// ensure that attachmentEditor gets initialized
	}

	public UiBulletinComponentSection createBulletinComponentSection(MartusApp app, boolean encrypted)
	{
		return new UiBulletinComponentEditorSection(this, owner, app, encrypted);
	}

	public void copyDataToBulletin(Bulletin bulletin) throws
		IOException,
		MartusCrypto.EncryptionException
	{
		bulletin.clear();

		boolean isAllPrivate = false;
		if(allPrivateField.getText().equals(UiField.TRUESTRING))
			isAllPrivate = true;

		bulletin.setAllPrivate(isAllPrivate);
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			bulletin.set(fieldTags[fieldNum], fields[fieldNum].getText());
		}

		UiBulletinComponentEditorSection publicSection = (UiBulletinComponentEditorSection)publicStuff;
		AttachmentProxy[] publicAttachments = publicSection.attachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < publicAttachments.length; ++aIndex)
		{
			AttachmentProxy a = publicAttachments[aIndex];
			bulletin.addPublicAttachment(a);
		}

		UiBulletinComponentEditorSection privateSection = (UiBulletinComponentEditorSection)privateStuff;
		AttachmentProxy[] privateAttachments = privateSection.attachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < privateAttachments.length; ++aIndex)
		{
			AttachmentProxy a = privateAttachments[aIndex];
			bulletin.addPrivateAttachment(a);
		}

	}

	public UiField createBoolField()
	{
		return new UiBoolEditor(this);
	}

	UiMainWindow owner;
}
