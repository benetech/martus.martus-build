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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class UiContactInfoDlg extends JDialog implements ActionListener
{
	public UiContactInfoDlg(UiMainWindow mainWindow, ConfigInfo infoToUse)
	{
		super(mainWindow, "", true);
		info = infoToUse;

		MartusApp app = mainWindow.getApp();
		setTitle(app.getWindowTitle("setupcontact"));
		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		source = new JTextField(50);
		organization = new JTextField(50);
		email = new JTextField(50);
		webpage = new JTextField(50);
		phone = new JTextField(50);
		address = new UiTextArea(5, 50);
		address.setLineWrap(true);
		address.setWrapStyleWord(true);
		JScrollPane addressScrollPane = new JScrollPane(address, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		source.setText(info.getAuthor());
		organization.setText(info.getOrganization());
		email.setText(info.getEmail());
		webpage.setText(info.getWebPage());
		phone.setText(info.getPhone());
		address.setText(info.getAddress());

		getContentPane().setLayout(new ParagraphLayout());
		JLabel space = new JLabel(" ");
		getContentPane().add(space, ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel());
		
		UiWrappedTextArea infoRequired = new UiWrappedTextArea(mainWindow, app.getFieldLabel("ContactInfoRequiredFields"), 60);
		infoRequired.setFont(space.getFont());
		infoRequired.setRows(2);
		getContentPane().add(infoRequired);
		
		String authorPrompt = app.getFieldLabel("AuthorRequired");
		getContentPane().add(new JLabel(authorPrompt), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(source);

		String organizationPrompt = app.getFieldLabel("OrganizationRequired");
		getContentPane().add(new JLabel(organizationPrompt), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(organization);
		getContentPane().add(new JLabel(app.getFieldLabel("email")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(email);
		getContentPane().add(new JLabel(app.getFieldLabel("webpage")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(webpage);
		getContentPane().add(new JLabel(app.getFieldLabel("phone")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(phone);
		getContentPane().add(new JLabel(app.getFieldLabel("address")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(addressScrollPane);
		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel(app.getFieldLabel("ContactInfoDescriptionOfFields")));

		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		UiWrappedTextArea infoFuture = new UiWrappedTextArea(mainWindow, app.getFieldLabel("ContactInfoFutureUseOfFields"), 60);
		infoFuture.setFont(space.getFont());
		infoFuture.setRows(3);
		getContentPane().add(infoFuture);

		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel(app.getFieldLabel("ContactInfoUpdateLater")));

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		mainWindow.centerDlg(this);
		setResizable(true);
		show();
	}

	public boolean getResult()
	{
		return result;
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
		{
			info.setAuthor(source.getText());
			info.setOrganization(organization.getText());
			info.setEmail(email.getText());
			info.setWebPage(webpage.getText());
			info.setPhone(phone.getText());
			info.setAddress(address.getText());
			result = true;
		}
		dispose();
	}

	ConfigInfo info;
	boolean result;

	JTextField source;
	JTextField organization;
	JTextField email;
	JTextField webpage;
	JTextField phone;
	UiTextArea address;

	JButton ok;
}
