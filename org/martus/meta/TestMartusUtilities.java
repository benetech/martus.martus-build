package org.martus.meta;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.client.Bulletin;
import org.martus.client.BulletinStore;
import org.martus.common.AttachmentProxy;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MockClientDatabase;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;
import org.martus.common.Packet.WrongAccountException;

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
		Database db = new MockClientDatabase();
		BulletinStore store = new BulletinStore(db);
		store.setSignatureGenerator(security);

		File sampleAttachment = createTempFile("This is some data");
		AttachmentProxy ap = new AttachmentProxy(sampleAttachment);

		Bulletin b = store.createEmptyBulletin();
		b.addPublicAttachment(ap);
		b.save();
		String accountId = b.getAccount();
		DatabaseKey key = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());

		File originalZipFile = createTempFile();
		MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, key, originalZipFile, security);
		validateZipFile(accountId, originalZipFile);

		File copiedZipFile = createCopyOfZipFile(originalZipFile, null);
		validateZipFile(accountId, copiedZipFile);
		
		File zipWithoutHeaderPacket = createCopyOfZipFile(originalZipFile, "B-");
		try
		{
			validateZipFile(accountId, zipWithoutHeaderPacket);
			fail("Should have thrown for missing header");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithoutDataPackets = createCopyOfZipFile(originalZipFile, "F-");
		try
		{
			validateZipFile(accountId, zipWithoutDataPackets);
			fail("Should have thrown for missing data packets");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithoutAttachmentPackets = createCopyOfZipFile(originalZipFile, "A-");
		try
		{
			validateZipFile(accountId, zipWithoutAttachmentPackets);
			fail("Should have thrown for missing attachment");
		}
		catch (IOException ignoreExpectedException)
		{
		}
		
		// add an extra packet and make sure the validate fails
	}

	private void validateZipFile(String accountId, File copiedZipFile)
		throws
			ZipException,
			IOException,
			InvalidPacketException,
			SignatureVerificationException,
			WrongAccountException,
			DecryptionException
	{
		ZipFile copiedZip = new ZipFile(copiedZipFile);
		MartusUtilities.validateIntegrityOfZipFilePackets(accountId, copiedZip, security);
		copiedZip.close();
	}

	private File createCopyOfZipFile(File tempZipFile, String excludeStartsWith)
		throws IOException, FileNotFoundException, ZipException
	{
		File copiedZipFile = createTempFile();
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(copiedZipFile));
		
		ZipFile zip = new ZipFile(tempZipFile);
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			if(excludeStartsWith != null && entry.getName().startsWith(excludeStartsWith))
				continue;
			InputStream in = new BufferedInputStream(zip.getInputStream(entry));
			zipOut.putNextEntry(entry);
			int dataLength = (int)entry.getSize();
			byte[] data = new byte[dataLength];
			in.read(data);
			zipOut.write(data);
		}
		zip.close();
		zipOut.close();
		return copiedZipFile;
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

		MartusUtilities.verifyFileAndSignature(normalFile, normalFileSigBySecurity, security, security.getPublicKeyString());
		
		try
		{
			MartusUtilities.verifyFileAndSignature(normalFile, normalFileSigBySecurity, security, "this would be a different public key");
			fail("signature file's public key is not the verifiers public key should have thrown.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
		}

		try
		{
			MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security, security.getPublicKeyString());
			fail("testCreateSignatureFromFile 1: Should have thrown FileVerificationException.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
		}

		normalFileSigBySecurity.delete();
		normalFile.delete();
		anotherFile.delete();
		
		try
		{
			MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security, security.getPublicKeyString());
			fail("testCreateSignatureFromFile 2: Should have thrown FileVerificationException.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
		}
	}
	
	static MartusSecurity security;
}
