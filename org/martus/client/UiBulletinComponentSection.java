/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

package org.martus.client;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import org.martus.common.AttachmentProxy;
import org.martus.common.FieldDataPacket;

abstract public class UiBulletinComponentSection extends JPanel
{
	UiBulletinComponentSection(MartusApp appToUse, boolean encrypted)
	{
		app = appToUse;
		
		ParagraphLayout layout = new ParagraphLayout();
		layout.outdentFirstField();
		setLayout(layout);

		setBorder(new EtchedBorder());

		encryptedIndicator = new JLabel("", null, JLabel.LEFT);
		encryptedIndicator.setVerticalTextPosition(JLabel.TOP);
		encryptedIndicator.setFont(encryptedIndicator.getFont().deriveFont(Font.BOLD));
		
		damagedIndicator = new JLabel("", null, JLabel.LEFT);
		damagedIndicator.setVerticalTextPosition(JLabel.TOP);
		damagedIndicator.setText(app.getFieldLabel("MayBeDamaged"));
		damagedIndicator.setFont(damagedIndicator.getFont().deriveFont(Font.BOLD));
		damagedIndicator.setBackground(Color.yellow);
		damagedIndicator.setForeground(Color.black);
		damagedIndicator.setOpaque(true);
		damagedIndicator.setBorder(new LineBorder(Color.black, 2));

		updateEncryptedIndicator(encrypted);
		updateDamagedIndicator(false);
		add(encryptedIndicator);
		add(damagedIndicator);
	}

	UiField[] createLabelsAndFields(JPanel target, String[] tags)
	{
		fieldTags = tags;

		fields = new UiField[tags.length];
		for(int fieldNum = 0; fieldNum < tags.length; ++fieldNum)
		{
			fields[fieldNum] = createField(tags[fieldNum]);

			target.add(createLabel(tags[fieldNum]), ParagraphLayout.NEW_PARAGRAPH);
			target.add(fields[fieldNum].getComponent());
		}
		JLabel attachments = new JLabel(app.getFieldLabel("attachments"));
		target.add(attachments, ParagraphLayout.NEW_PARAGRAPH);
		return fields;
	}
	
	public void copyDataFromPacket(FieldDataPacket fdp)
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			String text = "";
			if(fdp != null)
				text = fdp.get(fieldTags[fieldNum]);
			fields[fieldNum].setText(text);
		}

		if(fdp == null)
			return;

		AttachmentProxy[] attachments = fdp.getAttachments();
		for(int i = 0 ; i < attachments.length ; ++i)
			addAttachment(attachments[i]);	
	}
	
 	public JLabel createLabel(String fieldTag)
	{
		return new JLabel(app.getFieldLabel(fieldTag));
	}

	private UiField createField(String fieldName)
	{
		UiField field = null;

		switch(Bulletin.getFieldType(fieldName))
		{
			case Bulletin.MULTILINE:
				field = createMultilineField();
				break;
			case Bulletin.DATE:
				field = createDateField();
				break;
			case Bulletin.CHOICE: 
				ChoiceItem[] languages = 
					app.getLanguageNameChoices(MartusLocalization.ALL_LANGUAGE_CODES);
				field = createChoiceField(languages);
				break;
			case Bulletin.NORMAL:
			default:
				field = createNormalField();
				break;
		}
		field.getComponent().setBorder(new LineBorder(Color.black));
		return field;
	}
	

	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		String iconFileName = "unlocked.jpg";
		String title = app.getFieldLabel("publicsection");
		if(isEncrypted == ENCRYPTED)
		{
			iconFileName = "locked.jpg";
			title = app.getFieldLabel("privatesection");
		}
		
		Icon icon = new ImageIcon(UiBulletinComponentSection.class.getResource(iconFileName));
		encryptedIndicator.setIcon(icon);
		encryptedIndicator.setText(title);
	}

	public void updateSectionBorder(boolean isEncrypted) 
	{
		if(isEncrypted)
			setBorder(new LineBorder(Color.red, 5));
		else
			setBorder(new LineBorder(Color.lightGray, 5));
	}
	
	public void disableEdits()
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			fields[fieldNum].disableEdits();
		}
	}

	public void updateDamagedIndicator(boolean isDamaged)
	{
		damagedIndicator.setVisible(isDamaged);
	}
	
	ParagraphLayout getParagraphLayout()
	{
		return (ParagraphLayout)getLayout();
	}

	int getFirstColumnWidth()
	{
		return getParagraphLayout().getFirstColumnMaxWidth(this);
	}

	void matchFirstColumnWidth(UiBulletinComponentSection otherSection)
	{
		int thisWidth = getFirstColumnWidth();
		int otherWidth = otherSection.getFirstColumnWidth();
		if(otherWidth > thisWidth)
			getParagraphLayout().setFirstColumnWidth(otherWidth);
	}
	MartusApp app;
	JLabel encryptedIndicator;
	JLabel damagedIndicator;
	UiField[] fields;
	String[] fieldTags;
	
	public final static boolean ENCRYPTED = true;
	public final static boolean NOT_ENCRYPTED = false;

	abstract public UiField createNormalField();
	abstract public UiField createMultilineField();
	abstract public UiField createChoiceField(ChoiceItem[] choices);
	abstract public UiField createDateField();
	abstract public void createAttachmentTable();
	abstract public void addAttachment(AttachmentProxy a);
	abstract public void clearAttachments();
}
