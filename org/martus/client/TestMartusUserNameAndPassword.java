package org.martus.client;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.martus.client.exception.BlankUserNameException;
import org.martus.client.exception.InvalidUserNameOrPassword;
import org.martus.client.exception.MartusClientApplicationException;
import org.martus.client.exception.PasswordMatchedUserNameException;
import org.martus.client.exception.PasswordTooShortException;
import org.martus.common.TestCaseEnhanced;

public class TestMartusUserNameAndPassword extends TestCaseEnhanced
{
	public TestMartusUserNameAndPassword(String name)
	{
		super(name);
	}

	public void testValidateUserNameAndPassword() throws MartusClientApplicationException
	{
		boolean threwRightException = false;
		
		try
		{
			MartusUserNameAndPassword.validateUserNameAndPassword("", "validPassword");
			fail("Why wasn't a BlankUserNameException thrown?");
		}
		catch(BlankUserNameException ignoreExpectedException)
		{}

		try
		{
			MartusUserNameAndPassword.validateUserNameAndPassword("validUserName","validUserName");
			fail("Why was another exception (not PasswordMatchedUserNameException) thrown?");
		}
		catch(PasswordMatchedUserNameException ignoreExpectedException)
		{}

		try
		{
			MartusUserNameAndPassword.validateUserNameAndPassword("validUserName","short");
			fail("Why was another exception (not PasswordTooShortException) thrown?");
		}
		catch(PasswordTooShortException ignoreExpectedException)
		{}
	}
	
	public void testIsWeakPassword()
	{
		assertTrue("Why was 'test' not a weak password?", MartusUserNameAndPassword.isWeakPassword("test"));	
		assertFalse("Why was '123456789012345%$' not a strong password?", MartusUserNameAndPassword.isWeakPassword("123456789012345%$"));
	}
	


}
