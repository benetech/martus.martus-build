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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.martus.swing.*;



public class UiSigninDlg extends JDialog implements VirtualKeyboardHandler
{
	public UiSigninDlg(UiMainWindow window, JFrame owner, int mode)
	{
		this(window, owner, mode, "");
	}

	public UiSigninDlg(UiMainWindow window, JFrame owner, int mode, String username)
	{
		super(owner, true);
		Initalize(window, owner, mode, username);
	}


	public void Initalize(UiMainWindow window, JFrame owner, int mode, String username)
	{
		mainWindow = window;
		
		UiLocalization localization = mainWindow.getLocalization();

		String title = getTextForTitle(localization, mode);
		setTitle(title);

		getContentPane().setLayout(new ParagraphLayout());
		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		if(mode == TIMED_OUT)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel timedOutNote1 = new JLabel(localization.getFieldLabel("timedout1"));
			getContentPane().add(timedOutNote1);
			if(window.isModifyingBulletin())
			{
				getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
				JLabel timedOutNote2 = new JLabel(localization.getFieldLabel("timedout2"));
				getContentPane().add(timedOutNote2);
			}
		}
		else if(mode == SECURITY_VALIDATE)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel securityServerConfigValidate = new JLabel(localization.getFieldLabel("securityServerConfigValidate"));
			getContentPane().add(securityServerConfigValidate);
		}
		else if(mode == RETYPE_USERNAME_PASSWORD)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel retypeUserNamePassword = new JLabel(localization.getFieldLabel("RetypeUserNameAndPassword"));
			getContentPane().add(retypeUserNamePassword);
		}
		else if(mode == CREATE_NEW)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel createNewUserNamePassword = new JLabel(localization.getFieldLabel("CreateNewUserNamePassword"));
			getContentPane().add(createNewUserNamePassword);

			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			UiWrappedTextArea helpOnCreatingPassword = new UiWrappedTextArea(localization.getFieldLabel("HelpOnCreatingNewPassword"));
			getContentPane().add(helpOnCreatingPassword);

		}

		userNameDescription = new JLabel("");
		passwordDescription = new JLabel("");

		getContentPane().add(new JLabel(localization.getFieldLabel("username")), ParagraphLayout.NEW_PARAGRAPH);
		nameField = new JTextField(20);
		nameField.setText(username);
		getContentPane().add(userNameDescription);
		getContentPane().add(nameField);

		getContentPane().add(new JLabel(localization.getFieldLabel("password")), ParagraphLayout.NEW_PARAGRAPH);
		passwordField = new JPasswordField(20);

		switchToNormalKeyboard = new JButton(localization.getButtonLabel("VirtualKeyboardSwitchToNormal"));
		switchToNormalKeyboard.addActionListener(new switchKeyboardHandler());
		passwordArea = new JPanel();
		getContentPane().add(passwordArea);
		keyboard = new UiVirtualKeyboard(localization, this);
		UpdatePasswordArea();

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);
		if(username.length() > 0)
			passwordField.requestFocus();
		Utilities.centerDlg(this);
		setResizable(true);
		show();
	}

	public static String getTextForTitle(UiLocalization localization, int mode)
	{
		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;
		String title = ""; 
		switch (mode)
		{
			case SECURITY_VALIDATE:
				title = localization.getWindowTitle("MartusSignInValidate"); 
				break;
			case RETYPE_USERNAME_PASSWORD:
				title = localization.getWindowTitle("MartusSignInRetypePassword"); 
				break;
			default:
				title = localization.getWindowTitle("MartusSignIn"); 
				break;
		}
		
		String completeTitle = title +" (" + versionInfo + ")";
		return completeTitle;
	}

	public boolean getResult()
	{
		return result;
	}

	public String getName()
	{
		return name;
	}

	public String getPassword()
	{
		return password;
	}

	public class switchKeyboardHandler extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			SwitchKeyboards();
		}
	}
	public void setPassword(String virtualPassword)
	{
		passwordField.setText(virtualPassword);
		passwordField.updateUI();
		ok.requestFocus();
	}

	public void SwitchKeyboards()
	{
		boolean viewingVirtualKeyboard = mainWindow.isCurrentDefaultKeyboardVirtual();
		if(viewingVirtualKeyboard)
		{
			if(!mainWindow.confirmDlg(null, "WarningSwitchToNormalKeyboard"))
				return;
		}
		mainWindow.setCurrentDefaultKeyboardVirtual(!viewingVirtualKeyboard);
		try
		{
			mainWindow.saveCurrentUiState();
		}
		catch(IOException e)
		{
			System.out.println("UiSigninDialog SwitchKeyboards :" + e);
		}

		UpdatePasswordArea();
	}

	public void UpdatePasswordArea()
	{
		boolean viewingVirtualKeyboard = mainWindow.isCurrentDefaultKeyboardVirtual();
		if(viewingVirtualKeyboard)
			displayPasswordAreaUsingVirtualKeyboard();
		else
			displayPasswordAreaUsingNormalKeyboard();
	}

	public void addKeyboard(JPanel keyboard)
	{
		virtualKeyboardPanel = keyboard;
	}

	public void displayPasswordAreaUsingVirtualKeyboard()
	{
		UiLocalization localization = mainWindow.getLocalization();

		passwordArea.removeAll();
		userNameDescription.setText(localization.getFieldLabel("VirtualUserNameDescription"));
		passwordDescription.setText(localization.getFieldLabel("VirtualPasswordDescription"));

		passwordArea.setLayout(new ParagraphLayout());
		passwordArea.setBorder(new LineBorder(Color.black, 2));
		passwordArea.add(new JLabel(""));
		passwordArea.add(passwordDescription);
		passwordField.setEditable(false);
		passwordArea.add(passwordField);

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		passwordArea.add(virtualKeyboardPanel);

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		switchToNormalKeyboard.setText(localization.getButtonLabel("VirtualKeyboardSwitchToNormal"));
		passwordArea.add(switchToNormalKeyboard);
		updateUI();
		Utilities.centerDlg(this);
	}

	public void displayPasswordAreaUsingNormalKeyboard()
	{
		UiLocalization localization = mainWindow.getLocalization();

		passwordArea.removeAll();
		passwordArea.updateUI();
		userNameDescription.setText("");
		passwordDescription.setText("");
		passwordArea.setLayout(new ParagraphLayout());
		passwordArea.setBorder(new LineBorder(Color.black, 2));

		passwordField.setEditable(true);
		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		passwordArea.add(passwordField);

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		JLabel warningNormalKeyboard = new JLabel(localization.getFieldLabel("NormalKeyboardMsg1"));
		warningNormalKeyboard.setFont(warningNormalKeyboard.getFont().deriveFont(Font.BOLD));
		passwordArea.add(warningNormalKeyboard);
		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		passwordArea.add(new JLabel(localization.getFieldLabel("NormalKeyboardMsg2")));

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		switchToNormalKeyboard.setText(localization.getButtonLabel("VirtualKeyboardSwitchToVirtual"));
		passwordArea.add(switchToNormalKeyboard);
		updateUI();
		Utilities.centerDlg(this);
	}
	public void updateUI()
	{
		passwordArea.updateUI();
		userNameDescription.updateUI();
		getRootPane().setDefaultButton(ok);
		nameField.requestFocus();
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = true;
			name = nameField.getText();
			password = new String(passwordField.getPassword());
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}

	private UiMainWindow mainWindow;
	private boolean result;
	private String name;
	private String password;
	private UiVirtualKeyboard keyboard;
	private JPanel passwordArea;
	private JTextField nameField;
	private JPasswordField passwordField;
	private JPanel virtualKeyboardPanel;
	private JLabel userNameDescription;
	private JLabel passwordDescription;
	private JButton switchToNormalKeyboard;
	private JButton ok;

	public final static int INITIAL = 1;
	public final static int TIMED_OUT = 2;
	public final static int SECURITY_VALIDATE = 3;
	public final static int RETYPE_USERNAME_PASSWORD = 4;
	public final static int CREATE_NEW = 5;
}

