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

package org.martus.client.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.martus.client.core.ConfigInfo;
import org.martus.swing.*;

class UiSetupDlg extends JDialog implements ActionListener
{
	UiSetupDlg(UiMainWindow owner, ConfigInfo infoToUse)
	{
		super(owner, "", true);
		mainWindow = owner;
		info = infoToUse;

		UiLocalization localization = owner.getLocalization();

		setTitle(localization.getWindowTitle("setup"));
		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		UiTextArea description = new UiTextArea(localization.getFieldLabel("setupdescription"));
		description.setEditable(false);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		source = new JTextField(50);
		organization = new JTextField(50);
		email = new JTextField(50);
		webpage = new JTextField(50);
		phone = new JTextField(50);
		address = new UiTextArea(4, 50);
		address.setLineWrap(true);
		address.setWrapStyleWord(true);

		getContentPane().setLayout(new ParagraphLayout());
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(description);

		getContentPane().add(new JLabel(localization.getFieldLabel("author")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(source);

		getContentPane().add(new JLabel(localization.getFieldLabel("organization")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(organization);
		getContentPane().add(new JLabel(localization.getFieldLabel("email")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(email);
		getContentPane().add(new JLabel(localization.getFieldLabel("webpage")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(webpage);
		getContentPane().add(new JLabel(localization.getFieldLabel("phone")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(phone);
		getContentPane().add(new JLabel(localization.getFieldLabel("address")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(address);

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		Utilities.centerDlg(this);
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
			result = true;
		}
		dispose();
	}

	UiMainWindow mainWindow;
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
