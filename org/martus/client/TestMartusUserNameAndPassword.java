package org.martus.client;

import org.martus.client.Exceptions.BlankUserNameException;
import org.martus.client.Exceptions.MartusClientApplicationException;
import org.martus.client.Exceptions.PasswordMatchedUserNameException;
import org.martus.client.Exceptions.PasswordTooShortException;
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
