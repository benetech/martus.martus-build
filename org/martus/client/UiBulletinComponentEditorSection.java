package org.martus.client;

import javax.swing.JComponent;

import org.martus.common.AttachmentProxy;

public class UiBulletinComponentEditorSection extends UiBulletinComponentSection 
{

	public UiBulletinComponentEditorSection(UiBulletinComponent bulletinComponentToUse, UiMainWindow ownerToUse, MartusApp appToUse, boolean encrypted) 
	{
		super(appToUse, encrypted);
		app = appToUse;
		owner = ownerToUse;
		bulletinComponent = bulletinComponentToUse;
	}

	public UiField createNormalField()
	{
		return new UiNormalTextEditor(app);
	}

	public UiField createMultilineField()
	{
		return new UiMultilineTextEditor(app);
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceEditor(choices);
	}

	public UiField createDateField()
	{
		return new UiDateEditor(app);
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
	MartusApp app;
	UiMainWindow owner;
	UiBulletinComponent bulletinComponent;
}
