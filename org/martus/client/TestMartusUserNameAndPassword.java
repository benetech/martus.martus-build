package org.martus.client;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.martus.client.exception.BlankUserNameException;
import org.martus.client.exception.InvalidUserNameOrPassword;
import org.martus.client.exception.PasswordMatchedUserNameException;
import org.martus.client.exception.PasswordTooShortException;
import org.martus.common.TestCaseEnhanced;

public class TestMartusUserNameAndPassword extends TestCaseEnhanced
{
	public TestMartusUserNameAndPassword(String name)
	{
		super(name);
	}

	public void testValidateUserNameAndPassword()
	{
		boolean threwRightException = false;
		
		try
		{
			MartusUserNameAndPassword.validateUserNameAndPassword("", "validPassword");
		}
		catch(BlankUserNameException bune)
		{
			threwRightException = true;
		}
		catch(Exception e)
		{
			Assert.fail("Why was a different exception (not BlankUserNameException) thrown?");
		}

		Assert.assertTrue("Why wasn't BlankUserNameException thrown?", threwRightException);
		threwRightException = false;

		try
		{
			MartusUserNameAndPassword.validateUserNameAndPassword("validUserName","validUserName");
		}
		catch(PasswordMatchedUserNameException pmune)
		{
			threwRightException = true;
		}
		catch(Exception e)
		{
			Assert.fail("Why was another exception (not PasswordMatchedUserNameException) thrown?");
		}
		Assert.assertTrue("Why wasn't PasswordMatchedUserNameException thrown?", threwRightException);
		threwRightException = false;


		try
		{
			MartusUserNameAndPassword.validateUserNameAndPassword("validUserName","short");
		}
		catch(PasswordTooShortException ptse)
		{
			threwRightException = true;
		}
		catch(Exception e)
		{
			Assert.fail("Why was another exception (not PasswordTooShortException) thrown?");
		}			
		Assert.assertTrue("Why wasn't PasswordTooShortException thrown?", threwRightException);
		threwRightException = false;				
	}
	
	public void testIsWeakPassword()
	{
		Assert.assertTrue("Why was 'test' not a weak password?", MartusUserNameAndPassword.isWeakPassword("test"));	
		Assert.assertFalse("Why was '123456789012345%$' not a strong password?", MartusUserNameAndPassword.isWeakPassword("123456789012345%$"));
	}
	


}
