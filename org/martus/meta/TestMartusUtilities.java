package org.martus.meta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SyncFailedException;

import org.martus.common.MartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UnicodeWriter;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusUtilities.FileSigningException;
import org.martus.common.MartusUtilities.FileVerificationException;

import org.martus.common.MartusUtilities;

public class TestMartusUtilities extends TestCaseEnhanced 
{
	public TestMartusUtilities(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
    {
    	if(security == null)
		{
			security = new MartusSecurity();
			security.createKeyPair(512);
		}
    }

	// TODO: create tests for all the MartusUtilities methods
	public void testBasics()
	{
	}
	
	public void testCreateSignatureFromFile()
		throws IOException, CryptoInitializationException, MartusSignatureException, 
				FileSigningException, FileVerificationException
	{
		MartusSecurity otherSecurity = new MartusSecurity();
		otherSecurity.createKeyPair(512);
		
		String string1 = "The string to write into the file to sign.";
		String string2 = "The other string to write to another file to sign.";
		
		File normalFile = createTempFile(string1);
		File anotherFile = createTempFile(string2);

		File normalFileSigBySecurity = MartusUtilities.createSignatureFromFile(normalFile, security);

		MartusUtilities.verifyFileAndSignature(normalFile, normalFileSigBySecurity, security );
		
		try
		{
			MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security );
			fail("testCreateSignatureFromFile 2: Should have thrown FileVerificationException.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
			;
		}
		
		anotherFile = createTempFile(string1);

		MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security );
		
		File normalFileSigByOtherSecurity = MartusUtilities.createSignatureFromFile(normalFile, otherSecurity);
		try
		{
			MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigByOtherSecurity, security );
			fail("testCreateSignatureFromFile 4: Should have thrown FileVerificationException.");
		}
		catch(FileVerificationException e)
		{
			;
		}
	}
	
	static MartusSecurity security;
}
