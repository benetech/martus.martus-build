/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002,2003, Beneficent
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileDoesntExistsException;
import org.martus.util.Base64;
import org.martus.util.UnicodeWriter;


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
		File sigDir = MartusServerUtilities.getSignatureDirectoryForFile(fileToSign);

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

	public void testCreateBulletinUploadRecord() throws Exception
	{
		TRACE_BEGIN("testCreateBulletinUploadRecord");

		MockMartusSecurity clientSecurity = MockMartusSecurity.createClient();
		Bulletin b1 = new Bulletin(clientSecurity);
		b1.set(Bulletin.TAGTITLE, "Title1");

		String bur = MartusServerUtilities.createBulletinUploadRecord(b1.getLocalId(), serverSecurity);
		String bur2 = MartusServerUtilities.createBulletinUploadRecord(b1.getLocalId(), serverSecurity);
		assertEquals(bur, bur2);

		BufferedReader reader = new BufferedReader(new StringReader(bur));
		String gotFileTypeIdentifier = reader.readLine();
		assertEquals("Martus Bulletin Upload Record 1.0", gotFileTypeIdentifier);
		assertEquals(b1.getLocalId(), reader.readLine());
		String gotTimeStamp = reader.readLine();
		String now = MartusServerUtilities.createTimeStamp();
		assertStartsWith(now.substring(0, 13), gotTimeStamp);
		String gotDigest = reader.readLine();
		byte[] partOfPrivateKey = serverSecurity.getDigestOfPartOfPrivateKey();
		String stringToDigest = gotFileTypeIdentifier + "\n" + b1.getLocalId() + "\n" + gotTimeStamp + "\n" + Base64.encode(partOfPrivateKey) + "\n"; 
		assertEquals(gotDigest, MartusSecurity.createDigestString(stringToDigest));

		String bogusStringToDigest = gotFileTypeIdentifier + gotTimeStamp + b1.getLocalId() + Base64.encode(partOfPrivateKey); 
		assertNotEquals(MartusSecurity.createDigestString(bogusStringToDigest), gotDigest);
		TRACE_END();
	}
	
	public void testGetBurKey() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();

		DatabaseKey draftKey = DatabaseKey.createDraftKey(uid);
		assertTrue("not draft?", MartusServerUtilities.getBurKey(draftKey).isDraft());

		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		DatabaseKey sealedBurKey = MartusServerUtilities.getBurKey(sealedKey);
		assertTrue("not sealed?", sealedBurKey.isSealed());
		
		assertEquals(uid.getAccountId(), sealedBurKey.getAccountId());
		assertEquals("BUR-" + uid.getLocalId(), sealedBurKey.getLocalId());
	}
	
	public void testWasBurCreatedByThisCrypto() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherServer();
		String burRecord = MartusServerUtilities.createBulletinUploadRecord(uid.getLocalId(), otherSecurity);
		assertTrue("This burRecord was not created by this security?", MartusServerUtilities.wasBurCreatedByThisCrypto(burRecord, otherSecurity));
		assertFalse("This burRecord was created by this security?", MartusServerUtilities.wasBurCreatedByThisCrypto(burRecord, serverSecurity));
	}

	public void testWriteContactInfo() throws Exception
	{
		try
		{
			MartusServerUtilities.writeContatctInfo("bogusId", new Vector(), null);
			fail("Should have thrown invalid file");
		}
		catch (Exception expectedException)
		{
		}

		Vector contactInfo = new Vector();
		String clientId = "id";
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		String data1 = "Data";
		contactInfo.add(data1);
		String data2 = "Data2";
		contactInfo.add(data2);
		String signature = "Signature";
		contactInfo.add(signature);

		File contactInfoFile = createTempFile();
		MartusServerUtilities.writeContatctInfo(clientId, contactInfo, contactInfoFile);

		assertTrue("File Doesn't exist?", contactInfoFile.exists());

		FileInputStream contactFileInputStream = new FileInputStream(contactInfoFile);
		DataInputStream in = new DataInputStream(contactFileInputStream);

		String inputPublicKey = in.readUTF();
		int inputDataCount = in.readInt();
		String inputData =  in.readUTF();
		String inputData2 =  in.readUTF();
		String inputSig = in.readUTF();
		in.close();

		assertEquals("Public key doesn't match", clientId, inputPublicKey);
		assertEquals("data size not two?", 2, inputDataCount);
		assertEquals("data not correct?", data1, inputData);
		assertEquals("data2 not correct?", data2, inputData2);
		assertEquals("signature doesn't match?", signature, inputSig);		

		contactInfoFile.delete();
		contactInfoFile.getParentFile().delete();
	}

	public void testGetContactInfo() throws Exception
	{
		File invalidFile = createTempFile();
		try
		{
			MartusServerUtilities.getContactInfo(invalidFile);
			fail("Should have thrown invalid file");
		}
		catch (Exception expectedException)
		{
		}

		Vector contactInfo = new Vector();
		String clientId = "id";
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		String data1 = "Data";
		contactInfo.add(data1);
		String data2 = "Data2";
		contactInfo.add(data2);
		String signature = "Signature";
		contactInfo.add(signature);

		File contactInfoFile = createTempFile();
		MartusServerUtilities.writeContatctInfo(clientId, contactInfo, contactInfoFile);
		Vector retrievedInfo = MartusServerUtilities.getContactInfo(contactInfoFile);
		assertEquals("Vector wrong size", contactInfo.size(), retrievedInfo.size());
		
		String inputPublicKey = (String)retrievedInfo.get(0);
		int inputDataCount = ((Integer)retrievedInfo.get(1)).intValue();
		String inputData = (String)retrievedInfo.get(2);
		String inputData2 = (String)retrievedInfo.get(3);
		String inputSig = (String)retrievedInfo.get(4);

		assertEquals("Public key doesn't match", clientId, inputPublicKey);
		assertEquals("data size not two?", 2, inputDataCount);
		assertEquals("data not correct?", data1, inputData);
		assertEquals("data2 not correct?", data2, inputData2);
		assertEquals("signature doesn't match?", signature, inputSig);		

		contactInfoFile.delete();
		contactInfoFile.getParentFile().delete();
	}

	
	public File createTempFileWithContents(String content)
		throws IOException
	{
		File file = createTempFileFromName("$$$MartusTestMartusServerUtilities");
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(content);
		writer.flush();
		writer.close();
		
		return file;
	}

	static MartusSecurity serverSecurity;
}
