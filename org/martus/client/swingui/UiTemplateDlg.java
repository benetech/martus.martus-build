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

package org.martus.client.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;



public class UiTemplateDlg extends JDialog implements ActionListener
{
	public UiTemplateDlg(UiMainWindow owner, ConfigInfo infoToUse)
	{
		super(owner, "", true);
		info = infoToUse;
		mainWindow = owner;
		app = owner.getApp();
		setTitle(app.getWindowTitle("BulletinDetails"));
		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		JButton help = new JButton(app.getButtonLabel("help"));
		help.addActionListener(new helpHandler());

		details = new UiTextArea(5, 50);
		details.setLineWrap(true);
		details.setWrapStyleWord(true);
		JScrollPane detailScrollPane = new JScrollPane(details, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		details.setText(info.getTemplateDetails());

		getContentPane().setLayout(new ParagraphLayout());
		getContentPane().add(new JLabel(app.getFieldLabel("TemplateDetails")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(detailScrollPane);

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);
		getContentPane().add(help);

		getRootPane().setDefaultButton(ok);
		owner.centerDlg(this);
		setResizable(false);
		show();
	}


	class helpHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			String title = app.getWindowTitle("HelpDefaultDetails");
			String helpMsg = app.getFieldLabel("HelpDefaultDetails");
			String helpMsgExample = app.getFieldLabel("HelpExampleDefaultDetails");
			String helpMsgExample1 = app.getFieldLabel("HelpExample1DefaultDetails");
			String helpMsgExample2 = app.getFieldLabel("HelpExample2DefaultDetails");
			String helpMsgExampleEtc = app.getFieldLabel("HelpExampleEtcDefaultDetails");
			String ok = app.getButtonLabel("ok");
			String[] contents = {helpMsg, "", "",helpMsgExample, helpMsgExample1, "", helpMsgExample2, "", helpMsgExampleEtc};
			String[] buttons = {ok};
			
			new UiNotifyDlg(mainWindow, mainWindow, title, contents, buttons);
		}
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
			info.setTemplateDetails(details.getText());
			result = true;
		}
		dispose();
	}

	ConfigInfo info;
	JButton ok;
	UiTextArea details;
	boolean result;
	UiMainWindow mainWindow;
	MartusApp app;
}
