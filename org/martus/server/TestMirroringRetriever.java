package org.martus.server;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.BulletinRetrieverGatewayInterface;
import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;

public class TestMirroringRetriever extends TestCaseEnhanced
{
	public TestMirroringRetriever(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		db = new MockServerDatabase();
		security = new MockMartusSecurity();
		security.createKeyPair();
		
		supplier = new FakeServerSupplier();
		supplier.authorizedCaller = security.getPublicKeyString();

		handler = new SupplierSideMirroringHandler(supplier);
		gateway = new CallerSideMirroringGateway(handler);
	}
	
	public void testGetNextUidToRetrieve() throws Exception
	{
		MirroringRetriever retriever = new MirroringRetriever(db, gateway, security);
		assertNull("uid right after constructor?", retriever.getNextUidToRetrieve());
		Vector uids = new Vector();
		for(int i=0; i < 3; ++i)
			uids.add(UniversalId.createDummyUniversalId());

		retriever.uidsToRetrieve.addAll(uids);
		for(int i=0; i < uids.size(); ++i)
			assertEquals("wrong " + i + "?", uids.get(i), retriever.getNextUidToRetrieve());

		assertNull("uid after emptied?", retriever.getNextUidToRetrieve());
	}
	
	public void testRetrieveOneBulletin() throws Exception
	{
		supplier.returnResultTag = NetworkInterfaceConstants.OK;
		
		MirroringRetriever retriever = new MirroringRetriever(db, gateway, security);
		UniversalId uid = UniversalId.createDummyUniversalId();
		File gotFile = retriever.retrieveOneBulletin(uid);
		assertEquals(uid.getAccountId(), supplier.gotAccount);
		assertEquals(uid.getLocalId(), supplier.gotLocalId);

		int expectedLength = Base64.decode(supplier.returnZipData).length;
		assertEquals("file wrong length?", expectedLength, gotFile.length());
	}

	MockServerDatabase db;
	MartusSecurity security;
	FakeServerSupplier supplier;
	SupplierSideMirroringHandler handler;
	CallerSideMirroringGateway gateway;
}
