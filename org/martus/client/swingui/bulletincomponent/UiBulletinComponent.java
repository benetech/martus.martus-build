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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.martus.client.core.EncryptionChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiField;
import org.martus.common.FieldSpec;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.ParagraphLayout;

abstract public class UiBulletinComponent extends JPanel implements Scrollable, ChangeListener
{
	public UiBulletinComponent(UiMainWindow mainWindowToUse)
	{
		super();
		mainWindow = mainWindowToUse;
	}

	public void createSections()
	{
		FieldSpec[] publicFieldSpecs = currentBulletin.getPublicFieldTags();
		FieldSpec[] privateFieldSpecs = currentBulletin.getPrivateFieldTags();

		int numPublicFields = publicFieldSpecs.length;
		int indexOfFirstPublicField = 0;
		int indexOfFirstPrivateField = numPublicFields;
		int numFields = numPublicFields + privateFieldSpecs.length;
		fields = new UiField[numFields];
		fieldTags = new String[numFields];

		publicStuff = createBulletinComponentSection(UiBulletinComponentSection.NOT_ENCRYPTED);
		allPrivateField = createBoolField();
		allPrivateField.initalize();
		publicStuff.add(publicStuff.createLabel(new FieldSpec("allprivate")), ParagraphLayout.NEW_PARAGRAPH);
		publicStuff.add(allPrivateField.getComponent());
		createLabelsAndFields(publicStuff, publicFieldSpecs, indexOfFirstPublicField);
		if(!isEditable)
			publicStuff.disableEdits();

		privateStuff = createBulletinComponentSection(UiBulletinComponentSection.ENCRYPTED);
		privateStuff.updateSectionBorder(true);
		createLabelsAndFields(privateStuff, privateFieldSpecs, indexOfFirstPrivateField);
		if(!isEditable)
			privateStuff.disableEdits();

		publicStuff.matchFirstColumnWidth(privateStuff);
		privateStuff.matchFirstColumnWidth(publicStuff);
		setLayout(new BorderLayout());
		add(publicStuff, BorderLayout.NORTH);
		add(privateStuff, BorderLayout.SOUTH);
	}

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	public Bulletin getCurrentBulletin()
	{
		return currentBulletin;
	}

	public void copyDataToBulletin(Bulletin bulletin) throws
			IOException,
			MartusCrypto.EncryptionException
	{
	}
	

	public void validateData() throws UiField.DataInvalidException 
	{
	}

	public void copyDataFromBulletin(Bulletin bulletinToShow) throws IOException
	{
		removeAll();
		currentBulletin = bulletinToShow;
		if(currentBulletin == null)
		{
			publicStuff = null;
			privateStuff = null;
			repaint();
			return;
		}
		
		createSections();

		String isAllPrivate = UiField.FALSESTRING;
		if(currentBulletin.isAllPrivate())
			isAllPrivate = UiField.TRUESTRING;
		allPrivateField.setText(isAllPrivate);

		publicStuff.clearAttachments();
		privateStuff.clearAttachments();

		FieldDataPacket publicData = null;
		FieldDataPacket privateData = null;
		publicData = currentBulletin.getFieldDataPacket();
		privateData = currentBulletin.getPrivateFieldDataPacket();
		publicStuff.copyDataFromPacket(publicData);
		privateStuff.copyDataFromPacket(privateData);

		boolean isDamaged = false;
		if(!currentBulletin.isValid())
		{
			System.out.println("Damaged: " + currentBulletin.getLocalId());
			isDamaged = true;
		}

		publicStuff.updateDamagedIndicator(isDamaged);
		privateStuff.updateDamagedIndicator(isDamaged);
		repaint();
	}

	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		if(publicStuff == null)
			return;
			
		publicStuff.updateEncryptedIndicator(isEncrypted);
		publicStuff.updateSectionBorder(isEncrypted);
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

	void createLabelsAndFields(UiBulletinComponentSection target, FieldSpec[] specs, int startIndex)
	{
		UiField[] fieldsInThisSection = target.createLabelsAndFields(target, specs);
		for(int fieldNum = 0; fieldNum < specs.length; ++fieldNum)
		{
			int thisField = startIndex + fieldNum;
			fieldTags[thisField] = specs[fieldNum].getTag();
			fields[thisField] = fieldsInThisSection[fieldNum];
		}
		target.createAttachmentTable();
	}

	// ChangeListener interface
	public void stateChanged(ChangeEvent event)
	{
		String flagString = allPrivateField.getText();
		boolean nowEncrypted = (flagString.equals(UiField.TRUESTRING));
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
	boolean isEditable;
	UiBulletinComponentSection publicStuff;
	UiBulletinComponentSection privateStuff;

	abstract public UiField createBoolField();
	abstract public UiBulletinComponentSection createBulletinComponentSection(boolean encrypted);
}
