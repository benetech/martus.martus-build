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

package org.martus.server.formirroring;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.common.FieldSpec;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.TestCaseEnhanced;
import org.martus.server.core.LoggerForTesting;
import org.martus.server.forclients.MartusServerUtilities;
import org.martus.server.forclients.MockMartusServer;

public class TestServerForMirroring extends TestCaseEnhanced
{
	public TestServerForMirroring(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		logger = new LoggerForTesting();
		MockMartusSecurity serverSecurity = MockMartusSecurity.createServer();
		coreServer = new MockMartusServer();
		coreServer.setSecurity(serverSecurity);
		server = new ServerForMirroring(coreServer, logger);
		
		clientSecurity1 = MockMartusSecurity.createClient();
		clientSecurity2 = MockMartusSecurity.createOtherClient();

		Database db = server.getDatabase();

		bhp1 = new BulletinHeaderPacket(clientSecurity1.getPublicKeyString());
		bhp1.setStatus(BulletinConstants.STATUSSEALED);
		DatabaseKey key1 = bhp1.createKeyWithHeaderStatus(bhp1.getUniversalId());
		bhp1.writeXmlToDatabase(db, key1, false, clientSecurity1);

		bhp2 = new BulletinHeaderPacket(clientSecurity1.getPublicKeyString());
		bhp2.setStatus(BulletinConstants.STATUSSEALED);
		DatabaseKey key2 = bhp2.createKeyWithHeaderStatus(bhp2.getUniversalId());
		bhp2.writeXmlToDatabase(db, key2, false, clientSecurity1);

		bhp3 = new BulletinHeaderPacket(clientSecurity2.getPublicKeyString());
		bhp3.setStatus(BulletinConstants.STATUSSEALED);
		DatabaseKey key3 = bhp3.createKeyWithHeaderStatus(bhp3.getUniversalId());
		bhp3.writeXmlToDatabase(db, key3, false, clientSecurity2);

		bhp4 = new BulletinHeaderPacket(clientSecurity2.getPublicKeyString());
		bhp4.setStatus(BulletinConstants.STATUSDRAFT);
		DatabaseKey key4 = bhp4.createKeyWithHeaderStatus(bhp4.getUniversalId());
		bhp4.writeXmlToDatabase(db, key4, false, clientSecurity2);
		
		UniversalId fdpUid = FieldDataPacket.createUniversalId(clientSecurity1.getPublicKeyString());
		FieldSpec[] tags = {new FieldSpec("whatever")};
		FieldDataPacket fdp1 = new FieldDataPacket(fdpUid, tags);
		fdp1.writeXmlToClientDatabase(db, false, clientSecurity1);
		
		UniversalId otherPacketId = UniversalId.createFromAccountAndPrefix(clientSecurity2.getPublicKeyString(), "X");
		DatabaseKey key = new DatabaseKey(otherPacketId);
		db.writeRecord(key, "Not a valid packet");
	}

	protected void tearDown() throws Exception
	{
		coreServer.deleteAllFiles();
	}
	
	public void testGetPublicInfo() throws Exception
	{
		Vector publicInfo = server.getPublicInfo();
		assertEquals(2, publicInfo.size());
		String publicKey = (String)publicInfo.get(0);
		String gotSig = (String)publicInfo.get(1);
		String serverPublicKeyString = server.getSecurity().getPublicKeyString();
		MartusUtilities.validatePublicInfo(publicKey, gotSig, clientSecurity1);
		assertEquals(serverPublicKeyString, publicInfo.get(0));
		
	}
	
	public void testLoadMirrorsToCall() throws Exception
	{
		MockMartusServer noCallsToMakeCore = new MockMartusServer();
		ServerForMirroring noCallsToMake = new ServerForMirroring(noCallsToMakeCore, logger);
		noCallsToMake.createGatewaysWeWillCall();
		assertEquals(0, noCallsToMake.retrieversWeWillCall.size());
		noCallsToMakeCore.deleteAllFiles();
		
		MockMartusServer twoCallsToMakeCore = new MockMartusServer();
		twoCallsToMakeCore.enterSecureMode();
		File mirrorsWhoWeCall = new File(twoCallsToMakeCore.getStartupConfigDirectory(), "mirrorsWhoWeCall");
		mirrorsWhoWeCall.mkdirs();
		File pubKeyFile1 = new File(mirrorsWhoWeCall, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		MartusUtilities.exportServerPublicKey(clientSecurity1, pubKeyFile1);
		File pubKeyFile2 = new File(mirrorsWhoWeCall, "code=2.3.4.5.6-ip=2.3.4.5.txt");
		MartusUtilities.exportServerPublicKey(clientSecurity2, pubKeyFile2);
		ServerForMirroring twoCallsToMake = new ServerForMirroring(twoCallsToMakeCore, logger);
		twoCallsToMake.createGatewaysWeWillCall();
		assertEquals(2, twoCallsToMake.retrieversWeWillCall.size());
		mirrorsWhoWeCall.delete();
		twoCallsToMakeCore.deleteAllFiles();
	}
	
	public void testIsAuthorizedForMirroring() throws Exception
	{
		MockMartusServer nobodyAuthorizedCore = new MockMartusServer();
		ServerForMirroring nobodyAuthorized = new ServerForMirroring(nobodyAuthorizedCore, logger);
		nobodyAuthorized.loadConfigurationFiles();
		assertFalse("client already authorized?", nobodyAuthorized.isAuthorizedForMirroring(clientSecurity1.getPublicKeyString()));
		nobodyAuthorizedCore.deleteAllFiles();
		
		MockMartusServer twoAuthorizedCore = new MockMartusServer();
		twoAuthorizedCore.enterSecureMode();
		File mirrorsWhoCallUs = new File(twoAuthorizedCore.getStartupConfigDirectory(), "mirrorsWhoCallUs");
		mirrorsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(mirrorsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		MartusUtilities.exportServerPublicKey(clientSecurity1, pubKeyFile1);
		File pubKeyFile2 = new File(mirrorsWhoCallUs, "code=2.3.4.5.6-ip=2.3.4.5.txt");
		MartusUtilities.exportServerPublicKey(clientSecurity2, pubKeyFile2);
		ServerForMirroring twoAuthorized = new ServerForMirroring(twoAuthorizedCore, logger);
		twoAuthorized.loadConfigurationFiles();
		assertTrue("client1 not authorized?", twoAuthorized.isAuthorizedForMirroring(clientSecurity1.getPublicKeyString()));
		assertTrue("client2 not authorized?", twoAuthorized.isAuthorizedForMirroring(clientSecurity2.getPublicKeyString()));
		assertFalse("ourselves authorized?", twoAuthorized.isAuthorizedForMirroring(coreServer.getAccountId()));
		mirrorsWhoCallUs.delete();
		twoAuthorizedCore.deleteAllFiles();
		
	}

	public void testListAccounts() throws Exception
	{
		Vector result = server.listAccountsForMirroring();
		assertEquals(2, result.size());
		assertContains(clientSecurity1.getPublicKeyString(), result);
		assertContains(clientSecurity2.getPublicKeyString(), result);
	}
	
	public void testListBulletins() throws Exception
	{
		Database db = coreServer.getDatabase();
		MockServerDatabase mdb = (MockServerDatabase)db;
		assertEquals(6, mdb.getRecordCount());
		Set allKeys = mdb.getAllKeys();
		int drafts = 0;
		int sealeds = 0;
		for (Iterator iter = allKeys.iterator(); iter.hasNext();)
		{
			DatabaseKey key = (DatabaseKey)iter.next();
			if(key.isDraft())
				++drafts;
			else
				++sealeds;
		}
		assertEquals(5, sealeds);
		assertEquals(1, drafts);
		String publicKeyString1 = clientSecurity1.getPublicKeyString();
		Vector result1 = server.listBulletinsForMirroring(publicKeyString1);
		assertEquals(2, result1.size());
		Vector ids1 = new Vector();
		ids1.add(((Vector)result1.get(0)).get(0));
		ids1.add(((Vector)result1.get(1)).get(0));
		assertContains(bhp1.getLocalId(), ids1);
		assertContains(bhp2.getLocalId(), ids1);
		
		String publicKeyString2 = clientSecurity2.getPublicKeyString();
		Vector result2 = server.listBulletinsForMirroring(publicKeyString2);
		assertEquals(1, result2.size());
		Vector ids2 = new Vector();
		ids2.add(((Vector)result2.get(0)).get(0));
		assertContains(bhp3.getLocalId(), ids2);
	}
	
	public void testGetBulletinUploadRecord() throws Exception
	{
		String burNotFound = server.getBulletinUploadRecord(bhp1.getAccountId(), bhp1.getLocalId());
		assertNull("found bur?", burNotFound);

		String expectedBur = MartusServerUtilities.createBulletinUploadRecord(bhp1.getLocalId(), server.getSecurity());
		DatabaseKey headerKey = bhp1.createKeyWithHeaderStatus(bhp1.getUniversalId());
		String bulletinLocalId = headerKey.getLocalId();
		MartusServerUtilities.writeSpecificBurToDatabase(coreServer.getDatabase(), bhp1, expectedBur);
		String bur1 = server.getBulletinUploadRecord(bhp1.getAccountId(), bulletinLocalId);
		assertNotNull("didn't find bur1?", bur1);
		assertEquals("wrong bur?", expectedBur, bur1);
	}
	
	public void testExtractIpFromFileName() throws Exception
	{
		try
		{
			MartusUtilities.extractIpFromFileName("code=x.y.z");
			fail("Should have thrown missing ip=");
		}
		catch(InvalidPublicKeyFileException ignoreExpectedException)
		{
		}

		try
		{
			MartusUtilities.extractIpFromFileName("ip=1.2.3");
			fail("Should have thrown not enough dots");
		}
		catch(InvalidPublicKeyFileException ignoreExpectedException)
		{
		}

		assertEquals("1.2.3.4", MartusUtilities.extractIpFromFileName("ip=1.2.3.4"));
		assertEquals("2.3.4.5", MartusUtilities.extractIpFromFileName("ip=2.3.4.5.txt"));
		assertEquals("3.4.5.6", MartusUtilities.extractIpFromFileName("code=x.y.z-ip=3.4.5.6.txt"));
	}

	ServerForMirroring server;
	MockMartusServer coreServer;
	LoggerForTesting logger;

	MockMartusSecurity clientSecurity1;
	MockMartusSecurity clientSecurity2;

	BulletinHeaderPacket bhp1;
	BulletinHeaderPacket bhp2;
	BulletinHeaderPacket bhp3;
	BulletinHeaderPacket bhp4;
}
