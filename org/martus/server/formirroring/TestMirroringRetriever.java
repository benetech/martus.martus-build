package org.martus.server.formirroring;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.Bulletin;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.BulletinSaver;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;
import org.martus.server.core.LoggerForTesting;
import org.martus.server.forclients.MartusServerUtilities;

public class TestMirroringRetriever extends TestCaseEnhanced
{
	public TestMirroringRetriever(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		db = new MockServerDatabase();
		security = MockMartusSecurity.createServer();
		
		supplier = new FakeServerSupplier();
		supplier.authorizedCaller = security.getPublicKeyString();

		handler = new SupplierSideMirroringHandler(supplier, security);
		realGateway = new CallerSideMirroringGateway(handler);
		LoggerForTesting logger = new LoggerForTesting();
		realRetriever = new MirroringRetriever(db, realGateway, logger, security);
		
	}
	
	public void testGetNextUidToRetrieve() throws Exception
	{
		assertNull("uid right after constructor?", realRetriever.getNextUidToRetrieve());
		Vector uids = new Vector();
		for(int i=0; i < 3; ++i)
		{
			UniversalId uid = UniversalId.createDummyUniversalId(); 
			uids.add(uid);
			realRetriever.uidsToRetrieve.add(uid);
		}

		for(int i=0; i < uids.size(); ++i)
			assertEquals("wrong " + i + "?", uids.get(i), realRetriever.getNextUidToRetrieve());

		assertNull("uid right after emptied?", realRetriever.getNextUidToRetrieve());
		assertNull("uid again after emptied?", realRetriever.getNextUidToRetrieve());
	}
	
	public void testGetNextAccountToRetrieve() throws Exception
	{
		assertNull("account right after constructor?", realRetriever.getNextAccountToRetrieve());
		Vector accounts = new Vector();
		for(int i=0; i < 3; ++i)
			accounts.add(Integer.toString(i));
			
		realRetriever.accountsToRetrieve.addAll(accounts);
		for (int i = 0; i < accounts.size(); i++)
			assertEquals("wrong " + i + "?", accounts.get(i), realRetriever.getNextAccountToRetrieve());

		assertNull("account right after emptied?", realRetriever.getNextAccountToRetrieve());
		assertNull("account again after emptied?", realRetriever.getNextAccountToRetrieve());
	}
	
	public void testRetrieveOneBulletin() throws Exception
	{
		supplier.returnResultTag = MirroringInterface.RESULT_OK;
		
		UniversalId uid = UniversalId.createDummyUniversalId();
		File gotFile = realRetriever.retrieveOneBulletin(uid);
		assertEquals(uid.getAccountId(), supplier.gotAccount);
		assertEquals(uid.getLocalId(), supplier.gotLocalId);

		int expectedLength = Base64.decode(supplier.returnZipData).length;
		assertEquals("file wrong length?", expectedLength, gotFile.length());
	}
	
	public void testTick() throws Exception
	{
		realRetriever.tick();
		assertNull("tick asked for account?", supplier.gotAccount);
		assertNull("tick asked for id?", supplier.gotLocalId);

		MockServerDatabase fakeDatabase = new MockServerDatabase();
		MartusCrypto otherServerSecurity = MockMartusSecurity.createOtherServer();

		MartusCrypto clientSecurity = MockMartusSecurity.createClient();
		supplier.addAccountToMirror(clientSecurity.getPublicKeyString());
		Vector bulletins = new Vector();
		String[] burs = new String[3];
		for(int i=0; i < 3; ++i)
		{
			Bulletin b = new Bulletin(clientSecurity);
			b.setSealed();
			bulletins.add(b);
			DatabaseKey key = new DatabaseKey(b.getUniversalId());
			key.setSealed();
			BulletinSaver.saveToDatabase(b, fakeDatabase, false, clientSecurity);

			String bur = MartusServerUtilities.createBulletinUploadRecord(b.getLocalId(), otherServerSecurity);
			burs[i] = bur;
			MartusServerUtilities.writeSpecificBurToDatabase(fakeDatabase, b.getBulletinHeaderPacket(), bur);
			assertEquals("after write bur" + i, (i+1)*databaseRecordsPerBulletin, fakeDatabase.getRecordCount());

			InputStreamWithSeek in = fakeDatabase.openInputStream(key, otherServerSecurity);
			byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, otherServerSecurity);
			in.close();
			String sigString = Base64.encode(sigBytes);
			supplier.addBulletinToMirror(key, sigString);
		}

		assertEquals("before tick a", 0, db.getRecordCount());
		realRetriever.tick();
		assertNull("tick a asked for account?", supplier.gotAccount);
		assertNull("tick a asked for id?", supplier.gotLocalId);
		assertEquals("after tick a", 0, db.getRecordCount());
		realRetriever.tick();
		assertNull("tick b asked for account?", supplier.gotAccount);
		assertNull("tick b asked for id?", supplier.gotLocalId);
		assertEquals("after tick b", 0, db.getRecordCount());

		supplier.returnResultTag = MirroringInterface.RESULT_OK;
		for(int goodTick = 0; goodTick < 3; ++goodTick)
		{
			Bulletin expectedBulletin = (Bulletin)bulletins.get(goodTick);
			supplier.returnZipData = getZipString(fakeDatabase, expectedBulletin, clientSecurity);
			supplier.addBur(expectedBulletin.getAccount(), expectedBulletin.getLocalId(), burs[goodTick]);
			realRetriever.tick();
			assertEquals("tick " + goodTick + " wrong account?", clientSecurity.getPublicKeyString(), supplier.gotAccount);
			assertEquals("tick " + goodTick + " wrong id?", ((Bulletin)bulletins.get(goodTick)).getLocalId(), supplier.gotLocalId);
			assertEquals("after tick " + goodTick, (goodTick+1)*databaseRecordsPerBulletin, db.getRecordCount());
		}
		realRetriever.tick();
		assertEquals("after extra tick", 3*databaseRecordsPerBulletin, db.getRecordCount());
	}
	
	private String getZipString(Database dbToExportFrom, Bulletin b, MartusCrypto signer) throws Exception
	{
		String accountId = b.getAccount();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DatabaseKey[] packetKeys = MartusUtilities.getAllPacketKeys(b.getBulletinHeaderPacket());
		MartusUtilities.extractPacketsToZipStream(accountId, dbToExportFrom, packetKeys, out, signer);
		String zipString = Base64.encode(out.toByteArray());
		return zipString;
	}

	final static int databaseRecordsPerBulletin = 4;

	MockServerDatabase db;
	MartusSecurity security;
	FakeServerSupplier supplier;
	SupplierSideMirroringHandler handler;
	CallerSideMirroringGateway realGateway;
	MirroringRetriever realRetriever;
}
