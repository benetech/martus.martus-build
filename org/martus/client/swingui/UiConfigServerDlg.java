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
import org.martus.client.core.MartusApp;
import org.martus.common.MartusCrypto;
import org.martus.common.Base64.InvalidBase64Exception;

public class UiConfigServerDlg extends JDialog implements ActionListener
{
	public UiConfigServerDlg(UiMainWindow owner, ConfigInfo infoToUse)
	{
		super(owner, "", true);
		serverPublicKey = "";

		info = infoToUse;
		mainWindow = owner;
		app = owner.getApp();
		UiLocalization localization = mainWindow.getLocalization();
		
		setTitle(localization.getWindowTitle("ConfigServer"));
		getContentPane().setLayout(new ParagraphLayout());

		serverIP = new JTextField(25);
		serverPublicCode = new JTextField(25);

		getContentPane().add(new JLabel(localization.getFieldLabel("ServerNameEntry")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(serverIP);
		serverIPAddress = info.getServerName();
		serverIP.setText(serverIPAddress);
		serverIP.requestFocus();

		getContentPane().add(new JLabel(localization.getFieldLabel("ServerPublicCodeEntry")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(serverPublicCode);
		String serverPublicKey = info.getServerPublicKey();
		String serverCode = "";
		try
		{
			if(serverPublicKey.length() > 0)
			{
				serverCode = MartusCrypto.computePublicCode(serverPublicKey);
				serverCode = MartusCrypto.formatPublicCode(serverCode);
			}
		}
		catch (InvalidBase64Exception e)
		{
		}
		serverPublicCode.setText(serverCode);

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);

		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		getContentPane().add(ok);
		getContentPane().add(cancel);
		getRootPane().setDefaultButton(ok);

		UiUtilities.centerDlg(this);
		setResizable(true);
		show();
	}

	public boolean getResult()
	{
		return result;
	}

	public String getServerIPAddress()
	{
		return serverIPAddress;
	}

	public String getServerPublicKey()
	{
		return serverPublicKey;
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
		{
			String name = serverIP.getText();
			String publicCode = serverPublicCode.getText();
			if(!ValidateInformation(name, publicCode))
				return;
			result = true;
		}
		dispose();
	}

	private boolean ValidateInformation(String serverName, String userEnteredPublicCode)
	{
		if(serverName.length() == 0)
			return errorMessage("InvalidServerName");

		String normalizedPublicCode = MartusCrypto.removeNonDigits(userEnteredPublicCode);
		if(normalizedPublicCode.length() == 0)
			return errorMessage("InvalidServerCode");

		if(!app.isNonSSLServerAvailable(serverName))
			return errorMessage("ConfigNoServer");

		String serverKey = null;
		String serverPublicCode = null;
		try
		{
			serverKey = app.getServerPublicKey(serverName);
			serverPublicCode = MartusCrypto.computePublicCode(serverKey);
		}
		catch(Exception e)
		{
			System.out.println(e);
			return errorMessage("ServerInfoInvalid");
		}
		if(!serverPublicCode.equals(normalizedPublicCode))
			return errorMessage("ServerCodeWrong");

		serverIPAddress = serverName;
		serverPublicKey = serverKey;
		return true;
	}

	private boolean errorMessage(String messageTag)
	{
		mainWindow.notifyDlg(mainWindow, messageTag);
		return false;
	}

	MartusApp app;
	UiMainWindow mainWindow;
	ConfigInfo info;

	JButton ok;
	JTextField serverIP;
	JTextField serverPublicCode;

	String serverIPAddress;
	String serverPublicKey;

	boolean result;
}
