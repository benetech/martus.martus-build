package org.martus.client;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
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
		if(fdp == null)
			return;
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			String text = "";
			text = fdp.get(fieldTags[fieldNum]);
			fields[fieldNum].setText(text);
		}

		AttachmentProxy[] attachments = fdp.getAttachments();
		for(int i = 0 ; i < attachments.length ; ++i)
			addAttachment(attachments[i]);	
	}
	
 	public JLabel createLabel(String fieldTag)
	{
		//Extra spaces added for correct printing.
		return new JLabel("      " + app.getFieldLabel(fieldTag)+ " ");
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
