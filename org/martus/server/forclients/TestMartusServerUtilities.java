package org.martus.server.forclients;

import java.io.File;
import java.io.IOException;

import org.martus.common.MartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UnicodeWriter;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.server.forclients.MartusServerUtilities.MartusSignatureFileDoesntExistsException;


public class TestMartusServerUtilities extends TestCaseEnhanced
{
	public TestMartusServerUtilities(String name) throws Exception
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		TRACE_BEGIN("setUp");
		
		if(serverSecurity == null)
		{
			serverSecurity = new MartusSecurity();
			serverSecurity.createKeyPair(512);
		}

		TRACE_END();
	}
	
	public void tearDown() throws Exception
	{
		TRACE_BEGIN("tearDown");
		TRACE_END();
	}

	public void testServerFileSigning() throws Exception
	{
		TRACE_BEGIN("testServerFileSigning");

		File fileToSign = createTempFileWithContents("Line 1 of test text\n");
		File fileValidSignature;
		File fileInvalidSignature;

		try
		{
			MartusServerUtilities.getLatestSignatureFileFromFile(fileToSign);
			fail("Signature file should not exist.");
		}
		catch (MartusSignatureFileDoesntExistsException ignoredException)
		{}
		
		fileValidSignature = MartusServerUtilities.createSignatureFileFromFileOnServer(fileToSign, serverSecurity);
		assertTrue("createSignatureFileFromFileOnServer", fileValidSignature.exists() );
		
		try
		{
			MartusServerUtilities.verifyFileAndSignatureOnServer(fileToSign, fileValidSignature, serverSecurity, serverSecurity.getPublicKeyString());
		}
		catch (FileVerificationException e)
		{
			fail("Signature did not verify against file.");
		}
				
		File tempFile = createTempFileWithContents("Line 1 of test text\nLine 2 of test text\n");
		fileInvalidSignature = MartusServerUtilities.createSignatureFileFromFileOnServer(tempFile, serverSecurity);
		try
		{
			MartusServerUtilities.verifyFileAndSignatureOnServer(fileToSign, fileInvalidSignature, serverSecurity, serverSecurity.getPublicKeyString());
			fail("Should not verify against incorrect signature.");
		}
		catch (FileVerificationException e)
		{}
		
		fileToSign.delete();
		fileValidSignature.delete();
		tempFile.delete();
		fileInvalidSignature.delete();

		TRACE_END();
	}
	
	public void testGetLatestSignatureFile() throws Exception
	{
		File fileToSign = createTempFileWithContents("Line 1 of test text\n");
		File sigDir = MartusServerUtilities.getPathToSignatureDirForFile(fileToSign);

		File earliestFile = new File(sigDir, fileToSign.getName() + "1.sig");
		MartusServerUtilities.writeSignatureFileWithDatestamp(earliestFile, "20010109-120001", fileToSign, serverSecurity);
		
		File newestFile = new File(sigDir, fileToSign.getName() + "2.sig");
		MartusServerUtilities.writeSignatureFileWithDatestamp(newestFile, "20040109-120001", fileToSign, serverSecurity);
		
		File validSignatureFile = MartusServerUtilities.getLatestSignatureFileFromFile(fileToSign);
		assertEquals("Incorrect signature file retrieved", validSignatureFile.getAbsolutePath(), newestFile.getAbsolutePath());
		
		fileToSign.delete(); 
		earliestFile.delete();
		newestFile.delete();
		sigDir.delete();
	}
	
	public File createTempFileWithContents(String content)
		throws IOException
	{
		File file = File.createTempFile("$$$MartusTestMartusServerUtilities", null);
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(content);
		writer.flush();
		writer.close();
		
		return file;
	}
	
	static MartusSecurity serverSecurity;
}
