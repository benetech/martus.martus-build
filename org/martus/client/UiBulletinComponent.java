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
		setLayout(new BorderLayout());

		publicStuff = new Section(NOT_ENCRYPTED);
		privateStuff = new Section(ENCRYPTED);
		privateStuff.setBorder(new LineBorder(Color.red, 5));

		allPrivateField = createBoolField();
		publicStuff.add(createLabel("allprivate"), ParagraphLayout.NEW_PARAGRAPH);
		publicStuff.add(allPrivateField.getComponent());

		String[] standardFieldTags = Bulletin.getStandardFieldNames();
		String[] privateFieldTags = Bulletin.getPrivateFieldNames();

		int numFields = standardFieldTags.length + privateFieldTags.length;
		fields = new UiField[numFields];
		fieldTags = new String[numFields];
		
		int thisField = 0;
		createLabelsAndFields(publicStuff, standardFieldTags, 0);
		createLabelsAndFields(privateStuff, privateFieldTags, standardFieldTags.length);
		
		JLabel publicAttachments = new JLabel(getApp().getFieldLabel("attachments"));
		publicStuff.add(publicAttachments, ParagraphLayout.NEW_PARAGRAPH);
		publicStuff.add(createPublicAttachmentTable());

		JLabel privateAttachments = new JLabel(getApp().getFieldLabel("attachments"));
		privateStuff.add(privateAttachments, ParagraphLayout.NEW_PARAGRAPH);
		privateStuff.add(createPrivateAttachmentTable());

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

	abstract public UiField createNormalField();
	abstract public UiField createMultilineField();
	abstract public UiField createBoolField();
	abstract public UiField createChoiceField(ChoiceItem[] choices);
	abstract public UiField createDateField();
	abstract public JComponent createPublicAttachmentTable();
	abstract public JComponent createPrivateAttachmentTable();

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
		
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			String text = "";
			if(bulletin != null)
				text = bulletin.get(fieldTags[fieldNum]);
			fields[fieldNum].setText(text);
		}
		
		clearPublicAttachments();
		clearPrivateAttachments();
		if(bulletin != null)
		{
			AttachmentProxy[] publicAttachments = bulletin.getPublicAttachments();
			for(int i = 0 ; i < publicAttachments.length ; ++i)
				addPublicAttachment(publicAttachments[i]);	

			AttachmentProxy[] privateAttachments = bulletin.getPrivateAttachments();
			for(int i = 0 ; i < privateAttachments.length ; ++i)
				addPrivateAttachment(privateAttachments[i]);	
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

	abstract public void addPublicAttachment(AttachmentProxy a);
	abstract public void addPrivateAttachment(AttachmentProxy a);
	abstract public void clearPublicAttachments();
	abstract public void clearPrivateAttachments();

	public void setEncryptionChangeListener(EncryptionChangeListener listener)
	{
		encryptionListener = listener;
	}

	protected void fireEncryptionChange(boolean newState)
	{
		if(encryptionListener != null)
			encryptionListener.encryptionChanged(newState);
	}

 	private JLabel createLabel(String fieldTag)
	{
		//Extra spaces added for correct printing.
		return new JLabel("      " + getApp().getFieldLabel(fieldTag)+ " ");
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
				{
					new ChoiceItem("en", getApp().getLanguageName("en")),
					new ChoiceItem("es", getApp().getLanguageName("es")),
					new ChoiceItem("ru", getApp().getLanguageName("ru")),
					new ChoiceItem("fr", getApp().getLanguageName("fr")),
					new ChoiceItem("de", getApp().getLanguageName("de")),
					new ChoiceItem("ta", getApp().getLanguageName("ta")),
					new ChoiceItem("si", getApp().getLanguageName("si"))
				};
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

	void createLabelsAndFields(JPanel target, String[] tags, int startIndex)
	{
		for(int fieldNum = 0; fieldNum < tags.length; ++fieldNum)
		{
			int thisField = startIndex + fieldNum;
			String fieldTag = tags[fieldNum];

			fieldTags[thisField] = fieldTag;
			fields[thisField] = createField(fieldTag);

			target.add(createLabel(fieldTag), ParagraphLayout.NEW_PARAGRAPH);
			target.add(fields[thisField].getComponent());
		}
		
	}
	
	public class Section extends JPanel
	{
		Section(boolean encrypted)
		{
			ParagraphLayout layout = new ParagraphLayout();
			layout.outdentFirstField();
			setLayout(layout);

			setBorder(new EtchedBorder());

			encryptedIndicator = new JLabel("", null, JLabel.LEFT);
			encryptedIndicator.setVerticalTextPosition(JLabel.TOP);
			encryptedIndicator.setFont(encryptedIndicator.getFont().deriveFont(Font.BOLD));
			
			damagedIndicator = new JLabel("", null, JLabel.LEFT);
			damagedIndicator.setVerticalTextPosition(JLabel.TOP);
			damagedIndicator.setText(getApp().getFieldLabel("MayBeDamaged"));
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
		
		public void updateEncryptedIndicator(boolean isEncrypted)
		{
			String iconFileName = "unlocked.jpg";
			String title = getApp().getFieldLabel("publicsection");
			if(isEncrypted == ENCRYPTED)
			{
				iconFileName = "locked.jpg";
				title = getApp().getFieldLabel("privatesection");
			}
			
			Icon icon = new ImageIcon(Section.class.getResource(iconFileName));
			encryptedIndicator.setIcon(icon);
			encryptedIndicator.setText(title);
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

		void matchFirstColumnWidth(Section otherSection)
		{
			int thisWidth = getFirstColumnWidth();
			int otherWidth = otherSection.getFirstColumnWidth();
			if(otherWidth > thisWidth)
				getParagraphLayout().setFirstColumnWidth(otherWidth);
		}
		
		JLabel encryptedIndicator;
		JLabel damagedIndicator;
	}

	UiMainWindow mainWindow;

	String[] fieldTags;
	UiField[] fields;
	UiField allPrivateField;
	Bulletin currentBulletin;
	EncryptionChangeListener encryptionListener;
	boolean wasEncrypted;
	Section publicStuff;
	Section privateStuff;
	final boolean ENCRYPTED = true;
	final boolean NOT_ENCRYPTED = false;
}
