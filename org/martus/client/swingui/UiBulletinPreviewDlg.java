/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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

package org.martus.client.swingui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.martus.client.core.Bulletin;
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
		view.updateSectionBorder(isEncrypted);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.getViewport().add(view);
		scrollPane.setPreferredSize(new Dimension(720, 500));

		JButton ok = new JButton(owner.getApp().getButtonLabel("ok"));
		ok.addActionListener(this);
		Dimension okSize = ok.getPreferredSize();
		okSize.width += 40;
		ok.setPreferredSize(okSize);

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
