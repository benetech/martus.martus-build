package org.martus.client;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.martus.client.*;
import org.martus.common.*;

public class UiBulletinView extends UiBulletinComponent
{
	UiBulletinView(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		viewColors = new UiColors();
		disableEdits();
		// ensure that attachmentViewer gets initialized
		createPublicAttachmentTable();
		createPrivateAttachmentTable();
	}
	
	public void startPrintMode()
	{
		viewColors.publicBackground = publicStuff.getBackground();
		viewColors.publicForeground = publicStuff.getForeground();
		viewColors.privateBackground = privateStuff.getBackground();
		viewColors.privateForeground = privateStuff.getForeground();

		publicStuff.setBackground(Color.white);
		publicStuff.setForeground(Color.black);
		privateStuff.setBackground(Color.white);
		privateStuff.setForeground(Color.black);
		publicAttachmentViewer.startPrintMode();
		privateAttachmentViewer.startPrintMode();
	}
	
	public void endPrintMode()
	{
		publicStuff.setBackground(viewColors.publicBackground);
		publicStuff.setForeground(viewColors.publicForeground);
		privateStuff.setBackground(viewColors.privateBackground);
		privateStuff.setForeground(viewColors.privateForeground);
		publicAttachmentViewer.endPrintMode();
		privateAttachmentViewer.endPrintMode();
	}
	

	public UiField createDateField()
	{
		return new UiDateViewer(getApp());
	}

	public UiField createNormalField()
	{
		return new UiNormalTextViewer(getApp());
	}

	public UiField createMultilineField()
	{
		return new UiMultilineViewer(getApp());
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceViewer(choices);
	}

	public UiField createBoolField()
	{
		return new UiBoolViewer(getApp());
	}

	public JComponent createPublicAttachmentTable()
	{
		if(publicAttachmentViewer == null)
			publicAttachmentViewer = new UiAttachmentViewer(getMainWindow(), this);

		return publicAttachmentViewer;
	}

	public JComponent createPrivateAttachmentTable()
	{
		if(privateAttachmentViewer == null)
			privateAttachmentViewer = new UiAttachmentViewer(getMainWindow(), this);

		return privateAttachmentViewer;
	}
	
	public void addPublicAttachment(AttachmentProxy a)
	{
		publicAttachmentViewer.addAttachment(a);
	}
	
	public void clearPublicAttachments()
	{
		publicAttachmentViewer.clearAttachments();
	}

	public void addPrivateAttachment(AttachmentProxy a)
	{
		privateAttachmentViewer.addAttachment(a);
	}
	
	public void clearPrivateAttachments()
	{
		privateAttachmentViewer.clearAttachments();
	}

	class UiColors
	{
		public Color publicForeground;
		public Color publicBackground;
		public Color privateForeground;
		public Color privateBackground;
	}
	
	private UiAttachmentViewer publicAttachmentViewer;
	private UiAttachmentViewer privateAttachmentViewer;
	private UiColors viewColors;
}
