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

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import org.martus.client.core.ChoiceItem;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiWarningLabel;
import org.martus.client.swingui.fields.UiField;
import org.martus.common.FieldSpec;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.ParagraphLayout;

abstract public class UiBulletinComponentSection extends JPanel
{
	UiBulletinComponentSection(UiLocalization localizationToUse, boolean encrypted)
	{
		localization = localizationToUse;

		ParagraphLayout layout = new ParagraphLayout();
		layout.outdentFirstField();
		setLayout(layout);

		setBorder(new EtchedBorder());

		encryptedIndicator = new JLabel("", null, JLabel.LEFT);
		encryptedIndicator.setVerticalTextPosition(JLabel.TOP);
		encryptedIndicator.setFont(encryptedIndicator.getFont().deriveFont(Font.BOLD));

		damagedIndicator = new UiWarningLabel();

		updateEncryptedIndicator(encrypted);
		clearDamagedIndicator();
		add(encryptedIndicator);
		add(damagedIndicator);
	}

	public UiField[] createLabelsAndFields(JPanel target, FieldSpec[] specs)
	{
		fieldTags = specs;

		fields = new UiField[specs.length];
		for(int fieldNum = 0; fieldNum < specs.length; ++fieldNum)
		{
			fields[fieldNum] = createField(specs[fieldNum]);
			fields[fieldNum].initalize();
			target.add(createLabel(specs[fieldNum]), ParagraphLayout.NEW_PARAGRAPH);
			target.add(fields[fieldNum].getComponent());
		}
		JLabel attachments = new JLabel(localization.getFieldLabel("attachments"));
		target.add(attachments, ParagraphLayout.NEW_PARAGRAPH);
		return fields;
	}

	public void copyDataFromPacket(FieldDataPacket fdp)
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			String text = "";
			if(fdp != null)
				text = fdp.get(fieldTags[fieldNum].getTag());
			fields[fieldNum].setText(text);
		}

		if(fdp == null)
			return;

		AttachmentProxy[] attachments = fdp.getAttachments();
		for(int i = 0 ; i < attachments.length ; ++i)
			addAttachment(attachments[i]);
	}

 	public JLabel createLabel(FieldSpec spec)
	{
		String labelText = spec.getLabel();
		if(labelText.equals(""))
			labelText = localization.getFieldLabel(spec.getTag());
		return new JLabel(labelText);
	}

	private UiField createField(FieldSpec fieldSpec)
	{
		String fieldName = fieldSpec.getTag();
		UiField field = null;

		switch(fieldSpec.getType())
		{
			case FieldSpec.TYPE_MULTILINE:
				field = createMultilineField();
				break;
			case FieldSpec.TYPE_DATE:
				field = createDateField();
				break;
			case FieldSpec.TYPE_DATERANGE:
				field = createFlexiDateField();
				break;
			case FieldSpec.TYPE_CHOICE:
				ChoiceItem[] languages =
					localization.getLanguageNameChoices();
				field = createChoiceField(languages);
				break;					
			case FieldSpec.TYPE_NORMAL:
				field = createNormalField();
				break;
			case FieldSpec.TYPE_UNKNOWN:
			default:
				field = createUnknownField();
				break;
		}
		field.getComponent().setBorder(new LineBorder(Color.black));
		return field;
	}


	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		String iconFileName = "unlocked.jpg";
		String title = localization.getFieldLabel("publicsection");
		if(isEncrypted == ENCRYPTED)
		{
			iconFileName = "locked.jpg";
			title = localization.getFieldLabel("privatesection");
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
	
	public void clearDamagedIndicator()
	{
		damagedIndicator.setVisible(false);
	}

	public void updateDamagedIndicator(String text)
	{
		damagedIndicator.setText(text);
		damagedIndicator.setVisible(true);
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

	UiLocalization localization;
	JLabel encryptedIndicator;
	JLabel damagedIndicator;
	UiField[] fields;
	FieldSpec[] fieldTags;

	public final static boolean ENCRYPTED = true;
	public final static boolean NOT_ENCRYPTED = false;

	abstract public UiField createNormalField();
	abstract public UiField createMultilineField();
	abstract public UiField createChoiceField(ChoiceItem[] choices);
	abstract public UiField createDateField();
	abstract public UiField createFlexiDateField();
	abstract public UiField createUnknownField();
	abstract public void createAttachmentTable();
	abstract public void addAttachment(AttachmentProxy a);
	abstract public void clearAttachments();
}
