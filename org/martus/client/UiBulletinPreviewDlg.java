package org.martus.client;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.martus.common.FieldDataPacket;

public class UiBulletinPreviewDlg extends JDialog implements ActionListener
{
	
	public UiBulletinPreviewDlg(UiMainWindow owner, FieldDataPacket fdp)
	{
		super(owner, owner.getApp().getWindowTitle("BulletinPreview"), true);
		boolean isEncrypted = fdp.isEncrypted();
		getContentPane().setLayout(new ParagraphLayout());

		UiBulletinComponentViewSection view = new UiBulletinComponentViewSection(null, owner, owner.getApp(), isEncrypted);
		String[] standardFieldTags = Bulletin.getStandardFieldNames();
		view.createLabelsAndFields(view, standardFieldTags);
		view.createAttachmentTable();
		view.copyDataFromPacket(fdp);
		view.disableEdits();
		view.attachmentViewer.saveButton.setVisible(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.getViewport().add(view);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setMaximumSize(new Dimension(600, 400));
		scrollPane.setPreferredSize(new Dimension(600, 400));

		JButton ok = new JButton(owner.getApp().getButtonLabel("ok"));
		ok.addActionListener(this);

		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(scrollPane, ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok, ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		
		getRootPane().setDefaultButton(ok);
		owner.centerDlg(this);
		setResizable(true);
		show();
	}

	public void actionPerformed(ActionEvent ae) 
	{
		dispose();
	}

}
