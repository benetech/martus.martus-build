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

package org.martus.server.foramplifiers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import org.martus.common.LoggerForTesting;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinSaver;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.test.TestCaseEnhanced;
import org.martus.server.forclients.MartusServerUtilities;
import org.martus.server.forclients.MockMartusServer;

public class TestServerForAmplifiers extends TestCaseEnhanced
{
	public TestServerForAmplifiers(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		if(logger == null)
			logger = new LoggerForTesting();
		if(clientSecurity == null)
		{
			clientSecurity = new MockMartusSecurity();
			clientSecurity.createKeyPair();
		}
		
		if(coreServer == null)
		{
			MockMartusSecurity mockServer = MockMartusSecurity.createServer();
			coreServer = new MockMartusServer();
			coreServer.setSecurity(mockServer);
			coreServer.serverForClients.clearCanUploadList();
			coreServer.allowUploads(clientSecurity.getPublicKeyString());
		}
		
		if(otherServer == null)
		{
			MockMartusSecurity mockOtherServer = MockMartusSecurity.createOtherServer();
			otherServer = new MockMartusServer();
			otherServer.setSecurity(mockOtherServer);
			otherServer.serverForClients.clearCanUploadList();
			otherServer.allowUploads(clientSecurity.getPublicKeyString());
		}

		if(clientDatabase == null)
		{
			clientDatabase = new MockClientDatabase();
			b1 = new Bulletin(clientSecurity);
			b1.setAllPrivate(false);
			b1.set(Bulletin.TAGTITLE, "Title1");
			b1.set(Bulletin.TAGPUBLICINFO, "Details1");
			b1.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
			File attachment = createTempFile();
			FileOutputStream out = new FileOutputStream(attachment);
			out.write(b1AttachmentBytes);
			out.close();
			b1.addPublicAttachment(new AttachmentProxy(attachment));
			b1.addPrivateAttachment(new AttachmentProxy(attachment));
			b1.setSealed();
			BulletinSaver.saveToClientDatabase(b1, clientDatabase, true, clientSecurity);
			b1 = BulletinLoader.loadFromDatabase(clientDatabase, DatabaseKey.createSealedKey(b1.getUniversalId()), clientSecurity);
			b1ZipString = BulletinForTesting.saveToZipString(clientDatabase, b1, clientSecurity);
	
			b2 = new Bulletin(clientSecurity);
			b2.setAllPrivate(true);
			b2.set(Bulletin.TAGTITLE, "Title2");
			b2.set(Bulletin.TAGPUBLICINFO, "Details2");
			b2.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails2");
			b2.setSealed();
			BulletinSaver.saveToClientDatabase(b2, clientDatabase, true, clientSecurity);
			b2ZipString = BulletinForTesting.saveToZipString(clientDatabase, b2, clientSecurity);

			b3 = new Bulletin(clientSecurity);
			b3.setAllPrivate(false);
			b3.set(Bulletin.TAGTITLE, "Title1");
			b3.set(Bulletin.TAGPUBLICINFO, "Details1");
			b3.setSealed();
			BulletinSaver.saveToClientDatabase(b3, clientDatabase, true, clientSecurity);
			b3 = BulletinLoader.loadFromDatabase(clientDatabase, DatabaseKey.createSealedKey(b3.getUniversalId()), clientSecurity);
			b3ZipString = BulletinForTesting.saveToZipString(clientDatabase, b3, clientSecurity);

			b4 = new Bulletin(clientSecurity);
			b4.setAllPrivate(false);
			b4.set(Bulletin.TAGTITLE, "Title4");
			b4.set(Bulletin.TAGPUBLICINFO, "Details4");
			b4.setDraft();
			BulletinSaver.saveToClientDatabase(b4, clientDatabase, true, clientSecurity);
			b4 = BulletinLoader.loadFromDatabase(clientDatabase, DatabaseKey.createDraftKey(b4.getUniversalId()), clientSecurity);
			b4ZipString = BulletinForTesting.saveToZipString(clientDatabase, b4, clientSecurity);
		}
	}

	protected void tearDown() throws Exception
	{
		coreServer.deleteAllFiles();
		otherServer.deleteAllFiles();
	}
	
	public void testAmplifierGetContactInfo() throws Exception
	{
		MockMartusSecurity amplifier = MockMartusSecurity.createAmplifier();

		Vector parameters = new Vector();
		parameters.add(clientSecurity.getPublicKeyString());
		String signature = amplifier.createSignatureOfVectorOfStrings(parameters);

		File compliance = new File(coreServer.getStartupConfigDirectory(), "compliance.txt");
		compliance.deleteOnExit();
		compliance.createNewFile();
		File ampsWhoCallUs = new File(coreServer.getStartupConfigDirectory(), "AmpsWhoCallUs");
		ampsWhoCallUs.deleteOnExit();
		ampsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(ampsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		pubKeyFile1.deleteOnExit();
		MartusUtilities.exportServerPublicKey(amplifier, pubKeyFile1);
		
		coreServer.loadConfigurationFiles();
		compliance.delete();
		pubKeyFile1.delete();
		ampsWhoCallUs.delete();
		
		Vector invalidNumberOfParameters = new Vector();
		String invalidNumberOfParamsSig = amplifier.createSignatureOfVectorOfStrings(invalidNumberOfParameters);

		Vector response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), invalidNumberOfParameters, invalidNumberOfParamsSig);
		assertEquals("Incomplete request should have been retuned", ServerForAmplifiers.INCOMPLETE, response.get(0));

		response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), parameters, "bad sig");
		assertEquals("Bad Signature should have been returned", ServerForAmplifiers.SIG_ERROR, response.get(0));

		response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), parameters, signature);
		assertEquals("Should not have found contact info since it hasn't been uploaded yet", ServerForAmplifiers.NOT_FOUND, response.get(0));

		String clientId = clientSecurity.getPublicKeyString();
		String data1 = "data1";
		String data2 = "data2";
		Vector contactInfo = new Vector();
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		contactInfo.add(data1);
		contactInfo.add(data2);
		String infoSignature = clientSecurity.createSignatureOfVectorOfStrings(contactInfo);
		contactInfo.add(infoSignature);
		String result = coreServer.putContactInfo(clientId, contactInfo);
		assertEquals("Not ok?", NetworkInterfaceConstants.OK, result);
		
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), parameters, signature);
		assertEquals("Should have found contact info since it has been uploaded", ServerForAmplifiers.OK, response.get(0));
		Vector infoReturned = (Vector)response.get(1);
		assertEquals("Should be same size as was put in", contactInfo.size(), infoReturned.size());
		assertEquals("Public key doesn't match", clientId, infoReturned.get(0));
		assertEquals("data size not two?", 2, ((Integer)infoReturned.get(1)).intValue());
		assertEquals("data not correct?", data1, infoReturned.get(2));
		assertEquals("data2 not correct?", data2, infoReturned.get(3));
		assertEquals("signature doesn't match?", infoSignature, infoReturned.get(4));		
	}


	public void testIsAuthorizedForAmplifying() throws Exception
	{
		MockMartusServer nobodyAuthorizedCore = new MockMartusServer();
		ServerForAmplifiers nobodyAuthorized = new ServerForAmplifiers(nobodyAuthorizedCore, logger);
		nobodyAuthorized.loadConfigurationFiles();
		assertFalse("client already authorized?", nobodyAuthorized.isAuthorizedAmp(clientSecurity.getPublicKeyString()));
		nobodyAuthorizedCore.deleteAllFiles();
		
		MockMartusServer oneAuthorizedCore = new MockMartusServer();
		oneAuthorizedCore.enterSecureMode();
		File ampsWhoCallUs = new File(oneAuthorizedCore.getStartupConfigDirectory(), "AmpsWhoCallUs");
		ampsWhoCallUs.deleteOnExit();
		ampsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(ampsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		pubKeyFile1.deleteOnExit();
		MartusUtilities.exportServerPublicKey(clientSecurity, pubKeyFile1);
		ServerForAmplifiers oneAuthorized = new ServerForAmplifiers(oneAuthorizedCore, logger);
		oneAuthorized.loadConfigurationFiles();
		assertTrue("client1 not authorized?", oneAuthorized.isAuthorizedAmp(clientSecurity.getPublicKeyString()));
		assertFalse("ourselves authorized?", oneAuthorized.isAuthorizedForMirroring(coreServer.getAccountId()));
		ampsWhoCallUs.delete();
		oneAuthorizedCore.deleteAllFiles();
		
	}

	public void testAmplifierServer() throws Exception
	{
		MockMartusSecurity amplifier = MockMartusSecurity.createAmplifier();


		Vector parameters = new Vector();
		parameters.add(clientSecurity.getPublicKeyString());
		String signature = amplifier.createSignatureOfVectorOfStrings(parameters);

		File compliance = new File(coreServer.getStartupConfigDirectory(), "compliance.txt");
		compliance.deleteOnExit();
		compliance.createNewFile();
		File ampsWhoCallUs = new File(coreServer.getStartupConfigDirectory(), "AmpsWhoCallUs");
		ampsWhoCallUs.deleteOnExit();
		ampsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(ampsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		pubKeyFile1.deleteOnExit();
		MartusUtilities.exportServerPublicKey(amplifier, pubKeyFile1);
		
		coreServer.loadConfigurationFiles();
		compliance.delete();
		pubKeyFile1.delete();
		ampsWhoCallUs.delete();
		
		// a draft should be ignored by the rest of this test
		uploadSampleBulletin(coreServer, b4.getLocalId(), b4ZipString);
		
		String ampAccountId = amplifier.getPublicKeyString();
		Vector response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		assertEquals("Authorized amp requested data failed?", ServerForAmplifiers.OK, response.get(0));

		assertEquals("Failed to get list of public bulletin ids?", ServerForAmplifiers.OK, response.get(0));
		Vector uIds = (Vector)response.get(1);
		assertEquals("bulletins found?", 0, uIds.size());

		uploadSampleBulletin(coreServer, b1.getLocalId(), b1ZipString);
		uploadSampleBulletin(coreServer, b2.getLocalId(), b2ZipString);
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		uIds = (Vector)response.get(1);
		assertEquals("incorect # of bulletins found after uploading?", 1, uIds.size());
		assertEquals("B1 should had been returned", b1.getLocalId(), uIds.get(0));

		uploadSampleBulletin(otherServer, b3.getLocalId(), b3ZipString);
		uploadSampleBulletin(coreServer, b3.getLocalId(), b3ZipString);
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		uIds = (Vector)response.get(1);
		assertEquals("Currently B3 is a bulletin not mirrored?", 2, uIds.size());
		
		String bulletin3LocalId = b3.getLocalId();
		String burFromOtherDatabase = MartusServerUtilities.createBulletinUploadRecord(bulletin3LocalId, otherServer.getSecurity());
		MartusServerUtilities.writeSpecificBurToDatabase(coreServer.getDatabase(), b3.getBulletinHeaderPacket(), burFromOtherDatabase);
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		uIds = (Vector)response.get(1);
		assertEquals("incorect # of bulletins found after mirroring, should only amplify own bulletins?", 1, uIds.size());
	}

	void uploadSampleBulletin(MockMartusServer serverToUse, String bulletinLocalId, String bulletinZip ) 
	{
		serverToUse.serverForClients.clearCanUploadList();
		serverToUse.allowUploads(clientSecurity.getPublicKeyString());
		serverToUse.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinLocalId, bulletinZip);
	}
	
	MockMartusServer coreServer;
	MockMartusServer otherServer;
	LoggerForTesting logger;
	private static Bulletin b1;
	private static String b1ZipString;

	private static Bulletin b2;
	private static String b2ZipString;

	private static Bulletin b3;
	private static String b3ZipString;

	private static Bulletin b4;
	private static String b4ZipString;

	private static MartusSecurity clientSecurity;
	private static MockClientDatabase clientDatabase;

	final static byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
	final static byte[] file1Bytes = {1,2,3,4,4,3,2,1};
	final static byte[] file2Bytes = {1,2,3,4,4,3,2,1,0};

}

