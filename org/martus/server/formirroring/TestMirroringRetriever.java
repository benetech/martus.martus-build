package org.martus.server.formirroring;

import java.io.File;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.MartusSecurity;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
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
		security = MockMartusSecurity.createServer();
		
		supplier = new FakeServerSupplier();
		supplier.authorizedCaller = security.getPublicKeyString();

		handler = new SupplierSideMirroringHandler(supplier, security);
		realGateway = new CallerSideMirroringGateway(handler);
		realRetriever = new MirroringRetriever(db, realGateway, security);
		
	}
	
	public void testGetNextUidToRetrieve() throws Exception
	{
		assertNull("uid right after constructor?", realRetriever.getNextUidToRetrieve());
		Vector uids = new Vector();
		for(int i=0; i < 3; ++i)
			uids.add(UniversalId.createDummyUniversalId());

		realRetriever.uidsToRetrieve.addAll(uids);
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

	MockServerDatabase db;
	MartusSecurity security;
	FakeServerSupplier supplier;
	SupplierSideMirroringHandler handler;
	CallerSideMirroringGateway realGateway;
	MirroringRetriever realRetriever;
}
