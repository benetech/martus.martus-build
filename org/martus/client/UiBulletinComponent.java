package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.martus.common.AttachmentProxy;
import org.martus.common.MartusCrypto;

abstract public class UiBulletinComponent extends JPanel implements Scrollable, ChangeListener
{
	public UiBulletinComponent(UiMainWindow mainWindowToUse)
	{
		super();
		mainWindow = mainWindowToUse;
	}

	public void Initalize()
	{
		setLayout(new BorderLayout());

		publicStuff = createBulletinComponentSection(getApp(), UiBulletinComponentSection.NOT_ENCRYPTED);
		privateStuff = createBulletinComponentSection(getApp(), UiBulletinComponentSection.ENCRYPTED);
		privateStuff.setBorder(new LineBorder(Color.red, 5));

		allPrivateField = createBoolField();
		publicStuff.add(privateStuff.createLabel("allprivate"), ParagraphLayout.NEW_PARAGRAPH);
		publicStuff.add(allPrivateField.getComponent());

		String[] standardFieldTags = Bulletin.getStandardFieldNames();
		String[] privateFieldTags = Bulletin.getPrivateFieldNames();

		int numFields = standardFieldTags.length + privateFieldTags.length;
		fields = new UiField[numFields];
		fieldTags = new String[numFields];
		
		int thisField = 0;
		createLabelsAndFields(publicStuff, standardFieldTags, 0);
		createLabelsAndFields(privateStuff, privateFieldTags, standardFieldTags.length);
		
		publicStuff.matchFirstColumnWidth(privateStuff);
		privateStuff.matchFirstColumnWidth(publicStuff);

		add(publicStuff, BorderLayout.NORTH);
		add(privateStuff, BorderLayout.SOUTH);
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	public MartusApp getApp()
	{
		return getMainWindow().getApp();
	}

	public Bulletin getCurrentBulletin()
	{
		return currentBulletin;
	}

	public void disableEdits()
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			fields[fieldNum].disableEdits();
		}
	}

	public void copyDataToBulletin(Bulletin bulletin) throws 
			IOException,
			MartusCrypto.EncryptionException
	{
	}

	public void copyDataFromBulletin(Bulletin bulletin) throws IOException
	{
		currentBulletin = bulletin;
		
		String isAllPrivate = allPrivateField.FALSESTRING;
		if(bulletin != null && bulletin.isAllPrivate())
			isAllPrivate = allPrivateField.TRUESTRING;
		allPrivateField.setText(isAllPrivate);

		publicStuff.clearAttachments();
		privateStuff.clearAttachments();
		if(bulletin != null)
		{
			publicStuff.copyDataFromPacket(bulletin.getFieldDataPacket());
			privateStuff.copyDataFromPacket(bulletin.getPrivateFieldDataPacket());
		}

		boolean isDamaged = false;
		if(currentBulletin != null && !currentBulletin.isValid())
		{
			System.out.println("Damaged: " + currentBulletin.getLocalId());
			isDamaged = true;
		}

		publicStuff.updateDamagedIndicator(isDamaged);
		privateStuff.updateDamagedIndicator(isDamaged);
	}

	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		publicStuff.updateEncryptedIndicator(isEncrypted);
		if(isEncrypted)
			publicStuff.setBorder(new LineBorder(Color.red, 5));
		else
			publicStuff.setBorder(new LineBorder(Color.lightGray, 5));
	}

	public void setEncryptionChangeListener(EncryptionChangeListener listener)
	{
		encryptionListener = listener;
	}

	protected void fireEncryptionChange(boolean newState)
	{
		if(encryptionListener != null)
			encryptionListener.encryptionChanged(newState);
	}

	void createLabelsAndFields(UiBulletinComponentSection target, String[] tags, int startIndex)
	{
		UiField[] fieldsInThisSection = target.createLabelsAndFields(target, tags);
		for(int fieldNum = 0; fieldNum < tags.length; ++fieldNum)
		{
			int thisField = startIndex + fieldNum;
			fieldTags[thisField] = tags[fieldNum];
			fields[thisField] = fieldsInThisSection[fieldNum];
		}
		target.add(target.createAttachmentTable());
	}

	// ChangeListener interface
	public void stateChanged(ChangeEvent event)
	{
		String flagString = allPrivateField.getText();
		boolean nowEncrypted = (flagString.equals(allPrivateField.TRUESTRING));
		if(wasEncrypted != nowEncrypted)
		{
			wasEncrypted = nowEncrypted;
			fireEncryptionChange(nowEncrypted);
		}
	}


	// Scrollable interface
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 20;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 100;
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
	// End scrollable interface

	UiMainWindow mainWindow;

	String[] fieldTags;
	UiField[] fields;
	UiField allPrivateField;
	Bulletin currentBulletin;
	EncryptionChangeListener encryptionListener;
	boolean wasEncrypted;
	UiBulletinComponentSection publicStuff;
	UiBulletinComponentSection privateStuff;

	abstract public UiField createBoolField();
	abstract public UiBulletinComponentSection createBulletinComponentSection(MartusApp app, boolean encrypted);
}
