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
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.martus.client.swingui.dialogs.UiSigninDlg;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiWrappedTextArea;

public class UiSigninPanel extends JPanel implements VirtualKeyboardHandler
{
	public UiSigninPanel(UiSigninDlg dialogToUse, int mode, String username)
	{
		owner = dialogToUse;
		mainWindow = owner.getMainWindow();
		UiLocalization localization = mainWindow.getLocalization();
		setLayout(new ParagraphLayout());
		
		if(mode == UiSigninDlg.TIMED_OUT)
		{
			add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel timedOutNote1 = new JLabel(localization.getFieldLabel("timedout1"));
			add(timedOutNote1);
			if(mainWindow.isModifyingBulletin())
			{
				add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
				JLabel timedOutNote2 = new JLabel(localization.getFieldLabel("timedout2"));
				add(timedOutNote2);
			}
		}
		else if(mode == UiSigninDlg.SECURITY_VALIDATE)
		{
			add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel securityServerConfigValidate = new JLabel(localization.getFieldLabel("securityServerConfigValidate"));
			add(securityServerConfigValidate);
		}
		else if(mode == UiSigninDlg.RETYPE_USERNAME_PASSWORD)
		{
			add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel retypeUserNamePassword = new JLabel(localization.getFieldLabel("RetypeUserNameAndPassword"));
			add(retypeUserNamePassword);
		}
		else if(mode == UiSigninDlg.CREATE_NEW)
		{
			add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel createNewUserNamePassword = new JLabel(localization.getFieldLabel("CreateNewUserNamePassword"));
			add(createNewUserNamePassword);
		
			add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			UiWrappedTextArea helpOnCreatingPassword = new UiWrappedTextArea(localization.getFieldLabel("HelpOnCreatingNewPassword"));
			add(helpOnCreatingPassword);
		
		}
		
		userNameDescription = new JLabel("");
		passwordDescription = new JLabel("");
		
		add(new JLabel(localization.getFieldLabel("username")), ParagraphLayout.NEW_PARAGRAPH);
		nameField = new JTextField(20);
		nameField.setText(username);
		add(userNameDescription);
		add(nameField);
		
		add(new JLabel(localization.getFieldLabel("password")), ParagraphLayout.NEW_PARAGRAPH);
		passwordField = new JPasswordField(20);
		
		switchToNormalKeyboard = new JButton(localization.getButtonLabel("VirtualKeyboardSwitchToNormal"));
		switchToNormalKeyboard.addActionListener(new SwitchKeyboardHandler());
		passwordArea = new JPanel();
		add(passwordArea);
		new UiVirtualKeyboard(localization, this);
		UpdatePasswordArea();
		
		if(username.length() > 0)
			passwordField.requestFocus();
	}
	
	public String getName()
	{
		return nameField.getText();
	}
	
	public String getPassword()
	{
		return new String(passwordField.getPassword());
	}
	
	public void refreshForNewVirtualMode()
	{
		passwordArea.updateUI();
		userNameDescription.updateUI();
		nameField.requestFocus();
		owner.virtualPasswordHasChanged();
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
		refreshForNewVirtualMode();
		owner.sizeHasChanged();
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
		refreshForNewVirtualMode();
		owner.sizeHasChanged();
	}

	public void setPassword(String virtualPassword)
	{
		passwordField.setText(virtualPassword);
		passwordField.updateUI();
		owner.virtualPasswordHasChanged();
	}

	public void switchKeyboards()
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

	public class SwitchKeyboardHandler extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			switchKeyboards();
		}
	}

	UiSigninDlg owner;
	UiMainWindow mainWindow;
	private JLabel userNameDescription;
	private JLabel passwordDescription;
	private JTextField nameField;
	private JPasswordField passwordField;
	private JPanel passwordArea;
	private JPanel virtualKeyboardPanel;
	private JButton switchToNormalKeyboard;
}
