/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.common.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinSaver;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.util.Base64;
import org.martus.util.ByteArrayInputStreamWithSeek;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


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
	public void testExportServerPublicKey() throws Exception
	{
		File keyFile = createTempFile();
		MartusUtilities.exportServerPublicKey(security, keyFile);
		
		UnicodeReader reader = new UnicodeReader(keyFile);
		String sigFileIdentifier = reader.readLine();
		String sigFileType = reader.readLine();
		String key = reader.readLine();
		String sig = reader.readLine();
		reader.close();
		assertStartsWith("Martus Public Key", sigFileIdentifier);
		assertEquals("Server", sigFileType);
		assertEquals("wrong public key?", security.getPublicKeyString(), key);
		MartusUtilities.validatePublicInfo(key, sig, security);
		
		File badFile = new File(BAD_FILENAME);
		try
		{
			MartusUtilities.exportServerPublicKey(security, badFile);
			fail("Should have thrown");
		}
		catch (IOException ignoreExpectedException)
		{
		}
	}
	
	public void testImportServerPublicKeyFromFile() throws Exception
	{
		String key = security.getPublicKeyString();
		byte[] publicKeyBytes = Base64.decode(key);
		InputStream in = new ByteArrayInputStream(publicKeyBytes);
		byte[] sigBytes = security.createSignatureOfStream(in);
		String sig = Base64.encode(sigBytes);

		File keyFile = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(keyFile);
		writer.writeln("Martus Public Key:1.0");
		writer.writeln("Server");
		writer.writeln(key);
		writer.writeln(sig);
		writer.close();

		Vector result = MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
		assertEquals(2, result.size());
		String gotKey = (String)result.get(0);
		String gotSig = (String)result.get(1);
		assertEquals("wrong public key?", key, gotKey);
		assertEquals("wrong sig?", sig, gotSig);
	}

	public void testImportServerPublicKeyFromFileThatIsClient() throws Exception
	{
		File keyFile = createTempFile();
		MartusUtilities.exportClientPublicKey(security, keyFile);

		try
		{
			MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
		}
		catch (InvalidPublicKeyFileException ignoreExpectedException)
		{
		}
	}

	public void testImportServerPublicKeyFromFileBad() throws Exception
	{
		File keyFile = new File(BAD_FILENAME);
		try
		{
			MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
			fail("should have thrown");
		}
		catch (IOException ignoreExpectedException)
		{
		}
	}

	public void testImportServerPublicKeyFromFileBadSig() throws Exception
	{
		MockMartusSecurity other = MockMartusSecurity.createOtherServer();
		String key = other.getPublicKeyString();
		byte[] publicKeyBytes = Base64.decode(key);
		InputStream in = new ByteArrayInputStream(publicKeyBytes);
		byte[] sigBytes = security.createSignatureOfStream(in);
		String sig = Base64.encode(sigBytes);

		File keyFile = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(keyFile);
		writer.writeln("Martus Public Key:1.0");
		writer.writeln("Server");
		writer.writeln(key);
		writer.writeln(sig);
		writer.close();

		try
		{
			MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
			fail("should have thrown");
		}
		catch (PublicInformationInvalidException ignoreExpectedException)
		{
		}
	}

	public void testExportClientPublicKey() throws Exception
	{
		File keyFile = createTempFile();
		MartusUtilities.exportClientPublicKey(security, keyFile);
		
		UnicodeReader reader = new UnicodeReader(keyFile);
		String key = reader.readLine();
		String sig = reader.readLine();
		reader.close();
		assertEquals("wrong public key?", security.getPublicKeyString(), key);
		MartusUtilities.validatePublicInfo(key, sig, security);
		
		File badFile = new File(BAD_FILENAME);
		try
		{
			MartusUtilities.exportClientPublicKey(security, badFile);
			fail("Should have thrown");
		}
		catch (IOException ignoreExpectedException)
		{
		}
	}
	
	public void testImportClientPublicKeyFromFile() throws Exception
	{
		String key = security.getPublicKeyString();
		byte[] publicKeyBytes = Base64.decode(key);
		InputStream in = new ByteArrayInputStream(publicKeyBytes);
		byte[] sigBytes = security.createSignatureOfStream(in);
		String sig = Base64.encode(sigBytes);

		File keyFile = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(keyFile);
		writer.writeln(key);
		writer.writeln(sig);
		writer.close();

		Vector result = MartusUtilities.importClientPublicKeyFromFile(keyFile);
		assertEquals(2, result.size());
		String gotKey = (String)result.get(0);
		String gotSig = (String)result.get(1);
		assertEquals("wrong public key?", key, gotKey);
		assertEquals("wrong sig?", sig, gotSig);
	}

	public void testValidateIntegrityOfZipFilePackets() throws Exception
	{
		Database db = new MockClientDatabase();

		File sampleAttachment = createTempFileFromName("This is some data");
		AttachmentProxy ap = new AttachmentProxy(sampleAttachment);

		Bulletin b = new Bulletin(security);
		b.addPublicAttachment(ap);
		BulletinSaver.saveToClientDatabase(b, db, true, security);
		String accountId = b.getAccount();
		DatabaseKey key = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());

		File originalZipFile = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, key, originalZipFile, security);
		validateZipFile(accountId, originalZipFile);

		File copiedZipFile = createCopyOfZipFile(originalZipFile, null, null);
		validateZipFile(accountId, copiedZipFile);

		File zipWithoutHeaderPacket = createCopyOfZipFile(originalZipFile, "B-", null);
		try
		{
			validateZipFile(accountId, zipWithoutHeaderPacket);
			fail("Should have thrown for missing header");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithoutDataPackets = createCopyOfZipFile(originalZipFile, "F-", null);
		try
		{
			validateZipFile(accountId, zipWithoutDataPackets);
			fail("Should have thrown for missing data packets");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithoutAttachmentPackets = createCopyOfZipFile(originalZipFile, "A-", null);
		try
		{
			validateZipFile(accountId, zipWithoutAttachmentPackets);
			fail("Should have thrown for missing attachment");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithExtraEntry = createCopyOfZipFile(originalZipFile, null, "unexpected");
		try
		{
			validateZipFile(accountId, zipWithExtraEntry);
			fail("Should have thrown for extra entry");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithRelativePathInformation = createCopyOfZipFile(originalZipFile, null, "../../../acctmap.txt");
		try
		{
			validateZipFile(accountId, zipWithRelativePathInformation);
			fail("Should have thrown for relative path in name");
		}
		catch(InvalidPacketException ignoreExpectedException)
		{
		}

		File zipWithAbsolutePathInformation = createCopyOfZipFile(originalZipFile, null, "c:/MartusServer/packets/acctmap.txt");
		try
		{
			validateZipFile(accountId, zipWithAbsolutePathInformation);
			fail("Should have thrown for absolute path in name");
		}
		catch(InvalidPacketException ignoreExpectedException)
		{
		}
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
		try
		{
			BulletinZipUtilities.validateIntegrityOfZipFilePackets(accountId, copiedZip, security);
		}
		finally
		{
			copiedZip.close();
		}
	}

	private File createCopyOfZipFile(File tempZipFile, String excludeStartsWith, String entryToAdd)
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
		if(entryToAdd != null)
		{
			ZipEntry newEntry = new ZipEntry(entryToAdd);
			zipOut.putNextEntry(newEntry);
		}
		zip.close();
		zipOut.close();
		return copiedZipFile;
	}

	public void testGetBulletinSize() throws Exception
	{
		byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
		Database db = new MockClientDatabase();

		Bulletin b1 = new Bulletin(security);
		BulletinSaver.saveToClientDatabase(b1, db, true, security);
		BulletinHeaderPacket bhp = b1.getBulletinHeaderPacket();
		int emptySize = MartusUtilities.getBulletinSize(db, bhp);
		assertTrue("empty size not correct?", emptySize > 1000 && emptySize < 3000);
		b1.set(Bulletin.TAGTITLE, "Title");
		b1.set(Bulletin.TAGPUBLICINFO, "Details1");
		b1.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
		File attachment = createTempFile();
		FileOutputStream out = new FileOutputStream(attachment);
		out.write(b1AttachmentBytes);
		out.close();
		b1.addPublicAttachment(new AttachmentProxy(attachment));
		b1.addPrivateAttachment(new AttachmentProxy(attachment));
		BulletinSaver.saveToClientDatabase(b1, db, true, security);
		b1 = BulletinLoader.loadFromDatabase(db, DatabaseKey.createSealedKey(b1.getUniversalId()), security);

		int size = MartusUtilities.getBulletinSize(db, bhp);
		b1.set(Bulletin.TAGTITLE, "This is an very long title and should change the size of the result if things are working correctly");
		BulletinSaver.saveToClientDatabase(b1, db, true, security);
		int size2 = MartusUtilities.getBulletinSize(db, bhp);
		assertTrue("Size too small?", size > 4000);
		assertNotEquals("Sizes match?", size, size2);
	}

	public void testCreateSignatureFromFile()
		throws Exception
	{
		MartusSecurity otherSecurity = new MartusSecurity();
		otherSecurity.createKeyPair(512);

		String string1 = "The string to write into the file to sign.";
		String string2 = "The other string to write to another file to sign.";

		File normalFile = createTempFileWithData(string1);
		File anotherFile = createTempFileWithData(string2);

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

	public void testDoesPacketNeedLocalEncryption() throws Exception
	{
		final String accountId = "dummy";
		BulletinHeaderPacket bhpWithoutFlag = new BulletinHeaderPacket(accountId);
		BulletinHeaderPacket bhpWithFlagPrivate = new BulletinHeaderPacket(accountId);
		bhpWithFlagPrivate.setAllPrivate(true);
		BulletinHeaderPacket bhpWithFlagPublic = new BulletinHeaderPacket(accountId);
		bhpWithFlagPublic.setAllPrivate(false);
		byte[] binaryEncryptedData = {0, 1, 2, 3};
		byte[] tagEncryptedData = new String("blah blah blah\n<Encrypted> blah blah").getBytes();
		byte[] plainTextData = new String("There is nothing here\nto indicate that it is Encrypted!").getBytes();

		verifyDoesPacketNeedLocalEncryption("headerSaysPublic, binary encrypted",
						false, bhpWithFlagPublic, binaryEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPublic, tag encrypted",
						false, bhpWithFlagPublic, tagEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPublic, plain text",
						true, bhpWithFlagPublic, plainTextData);

		verifyDoesPacketNeedLocalEncryption("headerDoesntKnow, binary encrypted",
						false, bhpWithoutFlag, binaryEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerDoesntKnow, tag encrypted",
						false, bhpWithoutFlag, tagEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerDoesntKnow, plain text",
						true, bhpWithoutFlag, plainTextData);

		verifyDoesPacketNeedLocalEncryption("headerSaysPrivate, binary encrypted",
						false, bhpWithFlagPrivate, binaryEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPrivate, tag encrypted",
						false, bhpWithFlagPrivate, tagEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPrivate, plain text",
						false, bhpWithFlagPrivate, plainTextData);
	}
	
	public void testHeaderInWrongPlace() throws Exception
	{
		MockMartusSecurity client1 = MockMartusSecurity.createClient();
		MockMartusSecurity client2 = MockMartusSecurity.createOtherClient();
		UniversalId realUid = BulletinHeaderPacket.createUniversalId(client1.getPublicKeyString());
		UniversalId wrongUid = BulletinHeaderPacket.createUniversalId(client2.getPublicKeyString());
		BulletinHeaderPacket bhp1 = new BulletinHeaderPacket(realUid);

		Database db = new MockServerDatabase();
		DatabaseKey wrongKey = new DatabaseKey(wrongUid);
		bhp1.writeXmlToDatabase(db, wrongKey, false, client1);

		File tempFile = createTempFile();
		try
		{
			BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, wrongKey, tempFile, client1);
			fail("should have thrown");
		}
		catch(InvalidPacketException ignoreExpectedException)
		{
		}
	}
	
	public void verifyDoesPacketNeedLocalEncryption(String label, boolean expected,
							BulletinHeaderPacket bhp, byte[] bytes1)
		throws IOException
	{
		InputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes1);
		int firstByte = in.read();
		in.seek(0);
		assertEquals(label, expected,
				MartusUtilities.doesPacketNeedLocalEncryption(bhp, in));
		assertEquals(label + " didn't reset?", firstByte, in.read());
	}

	static MartusSecurity security;
}
