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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.common.UnicodeReader;
import org.martus.swing.*;



public class UiTemplateDlg extends JDialog implements ActionListener
{
	public UiTemplateDlg(UiMainWindow owner, ConfigInfo infoToUse, File defaultDetailsFileToUse)
	{
		super(owner, "", true);
		info = infoToUse;
		mainWindow = owner;
		defaultDetailsFile = defaultDetailsFileToUse;

		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("BulletinDetails"));
		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		JButton help = new JButton(localization.getButtonLabel("help"));
		help.addActionListener(new helpHandler());
		JButton loadFromFile = new JButton(localization.getButtonLabel("ResetContents"));
		loadFromFile.addActionListener(new loadFileHandler());

		details = new UiTextArea(15, 65);
		details.setLineWrap(true);
		details.setWrapStyleWord(true);
		JScrollPane detailScrollPane = new JScrollPane(details, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		details.setText(info.getTemplateDetails());

		getContentPane().setLayout(new ParagraphLayout());
		getContentPane().add(new JLabel(localization.getFieldLabel("TemplateDetails")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(detailScrollPane);

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(help);
		getContentPane().add(loadFromFile);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);
		Utilities.centerDlg(this);
		setResizable(false);
	}


	class helpHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			UiLocalization localization = mainWindow.getLocalization();
			String title = localization.getWindowTitle("HelpDefaultDetails");
			String helpMsg = localization.getFieldLabel("HelpDefaultDetails");
			String helpMsgExample = localization.getFieldLabel("HelpExampleDefaultDetails");
			String helpMsgExample1 = localization.getFieldLabel("HelpExample1DefaultDetails");
			String helpMsgExample2 = localization.getFieldLabel("HelpExample2DefaultDetails");
			String helpMsgExampleEtc = localization.getFieldLabel("HelpExampleEtcDefaultDetails");
			String ok = localization.getButtonLabel("ok");
			String[] contents = {helpMsg, "", "",helpMsgExample, helpMsgExample1, "", helpMsgExample2, "", helpMsgExampleEtc};
			String[] buttons = {ok};

			new UiNotifyDlg(mainWindow, title, contents, buttons);
		}
	}

	class loadFileHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(mainWindow.confirmDlg(mainWindow, "ResetDefaultDetails"))
			{
				details.setText("");
				try
				{
					if(defaultDetailsFile.exists())
						loadFile(defaultDetailsFile);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					mainWindow.notifyDlg(mainWindow, "ErrorReadingFile");
				}
			}
		}

	}

	public void loadFile(File fileToLoad) throws IOException
	{
		String data = "";
		BufferedReader reader = new BufferedReader(new UnicodeReader(fileToLoad));
		while(true)
		{
			String line = reader.readLine();
			if(line == null)
				break;
			data += line;
			data += "\n";
		}
		reader.close();
		details.setText(data);
	}

	class DefaultDetailsFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			if(pathname.isDirectory())
				return true;
			return(pathname.getName().endsWith(MartusApp.DEFAULT_DETAILS_EXTENSION));
		}

		public String getDescription()
		{
			return mainWindow.getLocalization().getFieldLabel("DefaultDetailFiles");
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
	File defaultDetailsFile;
}
