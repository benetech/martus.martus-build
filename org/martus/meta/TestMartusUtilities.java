package org.martus.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.MartusUtilities.FileVerificationException;

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
	
	public void testValidateIntegrityOfZipFilePackets() throws Exception
	{
		File tempZipFile = createTempFile();
		OutputStream rawOut = new FileOutputStream(tempZipFile);
		ZipOutputStream zipOut = new ZipOutputStream(rawOut);
		
		ZipEntry entry = new ZipEntry("test");
		zipOut.putNextEntry(entry);

		zipOut.close();
	}
	
	public void testCreateSignatureFromFile()
		throws Exception
	{
		MartusSecurity otherSecurity = new MartusSecurity();
		otherSecurity.createKeyPair(512);
		
		String string1 = "The string to write into the file to sign.";
		String string2 = "The other string to write to another file to sign.";
		
		File normalFile = createTempFile(string1);
		File anotherFile = createTempFile(string2);

		File normalFileSigBySecurity = MartusUtilities.createSignatureFileFromFile(normalFile, security);

		MartusUtilities.verifyFileAndSignature(normalFile, normalFileSigBySecurity, security);
		
		try
		{
			MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security);
			fail("testCreateSignatureFromFile 1: Should have thrown FileVerificationException.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
			;
		}
		
		anotherFile = createTempFile(string1);
		MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security);
		File normalFileSigByOtherSecurity = MartusUtilities.createSignatureFileFromFile(normalFile, otherSecurity);
		MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigByOtherSecurity, security);
		
		normalFileSigBySecurity.delete();
		normalFileSigByOtherSecurity.delete();
		normalFile.delete();
		anotherFile.delete();
	}
	
	static MartusSecurity security;
}
