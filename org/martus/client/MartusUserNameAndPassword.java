package org.martus.client;

import org.martus.client.exception.BlankUserNameException;
import org.martus.client.exception.InvalidUserNameOrPassword;
import org.martus.client.exception.PasswordMatchedUserNameException;
import org.martus.client.exception.PasswordTooShortException;

/**
 * 
 * MartusUserNameAndPassword
 * 
 * @author dchu
 *
 * Encapsulates the business logic behind validating usernames and passwords
 * Supports the UiCreateNewUserNameAndPassword UI dialog
 * 
 */
public class MartusUserNameAndPassword
{
	public static final void validateUserNameAndPassword(String username, String password)
		throws
			BlankUserNameException,
			PasswordMatchedUserNameException,
			PasswordTooShortException
	{
		if (username.length() == 0)
			throw new BlankUserNameException();
		if (password.length() < BASIC_PASSWORD_LENGTH)
			throw new PasswordTooShortException();
		if (password.equals(username))
			throw new PasswordMatchedUserNameException();
	}
		
	public static final boolean isWeakPassword(String password)
	{
		if ((password.length() >= STRONG_PASSWORD_LENGTH)
			&& (containsEnoughNonAlphanumbericCharacters(password)))
			return false;
		return true;
	}

	private static final boolean containsEnoughNonAlphanumbericCharacters(String password)
	{
		int nonAlphanumericCounter = 0;
		int placeholder = 0;
		int passwordLength = password.length();
		for (int i = 0; i < passwordLength; i++)
		{
			if (!(Character.isLetterOrDigit(password.charAt(i))))
			{
				nonAlphanumericCounter++;
				if (nonAlphanumericCounter
					>= STRONG_PASSWORD_NUMBER_OF_NONALPHANUMERIC)
					return true;
			}
		}
		return false;
	}

	private static final int STRONG_PASSWORD_NUMBER_OF_NONALPHANUMERIC = 2;
	private static final int STRONG_PASSWORD_LENGTH = 15;
	private static final int BASIC_PASSWORD_LENGTH = 8;
}
