package org.martus.client;

import javax.swing.JFrame;

import org.martus.client.exception.*;

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
