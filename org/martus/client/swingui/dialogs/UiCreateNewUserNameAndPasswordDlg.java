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

package org.martus.client.swingui.dialogs;

import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.core.Exceptions.BlankUserNameException;
import org.martus.client.core.Exceptions.PasswordMatchedUserNameException;
import org.martus.client.core.Exceptions.PasswordTooShortException;
import org.martus.client.swingui.UiMainWindow;

/**
 * UiCreateNewUserNameAndPasswordDlg
 *
 * Class encapusulates all aspects of creating a new username and password combo
 * - Displays the username and password entry dialog
 * - Checks the username and password to make sure they meet our requirements
 * - Confirms the username and password
 * - Reminds the user to remember his/her password
 *
 * @author dchu
 *
 */
public class UiCreateNewUserNameAndPasswordDlg
{
	public UiCreateNewUserNameAndPasswordDlg(
		UiMainWindow window,
		String originalUserName)
	{
		mainWindow = window;
		while (true)
		{
			UiSigninDlg signinDlg1 =
				new UiSigninDlg(window, window, UiSigninDlg.CREATE_NEW, originalUserName);
			if (!signinDlg1.getResult())
				return;
			userName1 = signinDlg1.getName();
			userPassword1 = signinDlg1.getPassword();
			String defaultUserName = userName1;
			if (originalUserName == null || originalUserName.length() == 0)
				defaultUserName = "";

			UiSigninDlg signinDlg2 =
				new UiSigninDlg(window, window, UiSigninDlg.RETYPE_USERNAME_PASSWORD, defaultUserName);
			if (!signinDlg2.getResult())
				return;
			String userName2 = signinDlg2.getName();
			String userPassword2 = signinDlg2.getPassword();

			// make sure the passwords and usernames match
			if (!userPassword1.equals(userPassword2))
			{
				window.notifyDlg(window, "passwordsdontmatch");
				continue;
			}
			if (!userName1.equals(userName2))
			{
				window.notifyDlg(window, "usernamessdontmatch");
				continue;
			}

			// next make sure the username and password is valid
			try
			{
				MartusUserNameAndPassword.validateUserNameAndPassword(userName1, userPassword1);
			}
			catch (BlankUserNameException bune)
			{
				window.notifyDlg(window, "UserNameBlank");
				continue;
			}
			catch (PasswordTooShortException ptse)
			{
				window.notifyDlg(window, "PasswordInvalid");
				continue;
			}
			catch (PasswordMatchedUserNameException pmune)
			{
				window.notifyDlg(window, "PasswordMatchesUserName");
				continue;
			}

			// finally warn them if its a weak password
			if(MartusUserNameAndPassword.isWeakPassword(userPassword1))
			{
				if(!window.confirmDlg(window, "RedoWeakPassword"))
					continue;
			}

			remindUsersToRememberPassword();
			result = true;
			break;
		}
	}

	private void remindUsersToRememberPassword()
	{
		mainWindow.notifyDlg(mainWindow, "RememberPassword");
	}

	public boolean isDataValid()
	{
		return result;
	}

	public String getUserName()
	{
		return userName1;
	}

	public String getPassword()
	{
		return userPassword1;
	}

	private String userName1;
	private String userPassword1;
	private UiMainWindow mainWindow;
	private boolean result;
}
