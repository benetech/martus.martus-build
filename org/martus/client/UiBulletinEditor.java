package org.martus.client;

import java.io.IOException;

import javax.swing.JComponent;

import org.martus.client.*;
import org.martus.common.*;

public class UiBulletinEditor extends UiBulletinComponent
{
	UiBulletinEditor(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);

		// ensure that attachmentEditor gets initialized
		createPublicAttachmentTable();
		createPrivateAttachmentTable();
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

		AttachmentProxy[] publicAttachments = publicAttachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < publicAttachments.length; ++aIndex)
		{
			AttachmentProxy a = publicAttachments[aIndex];
			bulletin.addPublicAttachment(a);
		}

		AttachmentProxy[] privateAttachments = privateAttachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < privateAttachments.length; ++aIndex)
		{
			AttachmentProxy a = privateAttachments[aIndex];
			bulletin.addPrivateAttachment(a);
		}
	}

	public void addPublicAttachment(AttachmentProxy a)
	{
		publicAttachmentEditor.addAttachment(a);
	}

	public void clearPublicAttachments()
	{
		publicAttachmentEditor.clearAttachments();
	}
	
	public void addPrivateAttachment(AttachmentProxy a)
	{
		privateAttachmentEditor.addAttachment(a);
	}

	public void clearPrivateAttachments()
	{
		privateAttachmentEditor.clearAttachments();
	}
	
	public UiField createNormalField()
	{
		return new UiNormalTextEditor(getApp());
	}

	public UiField createMultilineField()
	{
		return new UiMultilineTextEditor(getApp());
	}

	public UiField createBoolField()
	{
		return new UiBoolEditor(this);
	}

	public UiField createDateField()
	{
		return new UiDateEditor(getApp());
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceEditor(choices);
	}

	public JComponent createPublicAttachmentTable()
	{
		if(publicAttachmentEditor == null)
			publicAttachmentEditor = new UiAttachmentEditor(mainWindow);

		return publicAttachmentEditor;
	}

	public JComponent createPrivateAttachmentTable()
	{
		if(privateAttachmentEditor == null)
			privateAttachmentEditor = new UiAttachmentEditor(mainWindow);

		return privateAttachmentEditor;
	}

	private UiAttachmentEditor publicAttachmentEditor;
	private UiAttachmentEditor privateAttachmentEditor;
}
