package org.martus.client;

import java.io.IOException;

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
		if(allPrivateField.getText().equals(allPrivateField.TRUESTRING))
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
