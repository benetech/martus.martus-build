package org.martus.client;

public class Exceptions
{
	public static class MartusClientApplicationException extends Exception
	{
	}
	
	public static class InvalidUserNameOrPassword extends MartusClientApplicationException
	{
	}
	
	public static class BlankUserNameException extends InvalidUserNameOrPassword
	{
	}

	public static class PasswordMatchedUserNameException extends InvalidUserNameOrPassword
	{
	}
	
	public static class PasswordTooShortException extends InvalidUserNameOrPassword
	{
	}
}
