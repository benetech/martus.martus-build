package org.martus.server;

import java.io.File;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.MartusSignatureException;

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
		realGateway = new CallerSideMirroringGateway(handler);
		realRetriever = new MirroringRetriever(db, realGateway, security);
		
		fakeGateway = new FakeCallerSideMirroringGateway();
		fakeRetriever = new MirroringRetriever(db, fakeGateway, security);
	}
	
	public void testGetNextUidToRetrieve() throws Exception
	{
		assertNull("uid right after constructor?", fakeRetriever.getNextUidToRetrieve());
		Vector uids = new Vector();
		for(int i=0; i < 3; ++i)
			uids.add(UniversalId.createDummyUniversalId());

		fakeRetriever.uidsToRetrieve.addAll(uids);
		for(int i=0; i < uids.size(); ++i)
			assertEquals("wrong " + i + "?", uids.get(i), fakeRetriever.getNextUidToRetrieve());

		assertNull("uid right after emptied?", fakeRetriever.getNextUidToRetrieve());
		UniversalId uid = fakeRetriever.getNextUidToRetrieve();
		assertEquals("wrong fake uid?", fakeGateway.fakeUid, uid);
	}
	
	public void testGetNextAccountToRetrieve() throws Exception
	{
		assertNull("account right after constructor?", fakeRetriever.getNextAccountToRetrieve());
		Vector accounts = new Vector();
		for(int i=0; i < 3; ++i)
			accounts.add(Integer.toString(i));
			
		fakeRetriever.accountsToRetrieve.addAll(accounts);
		assertEquals("wrong fake account1?", "a", fakeRetriever.getNextAccountToRetrieve());
		for (int i = 0; i < accounts.size(); i++)
			assertEquals("wrong " + i + "?", accounts.get(i), fakeRetriever.getNextAccountToRetrieve());

		assertNull("account right after emptied?", fakeRetriever.getNextAccountToRetrieve());
		assertEquals("wrong fake account2?", "a", fakeRetriever.getNextAccountToRetrieve());
	}
	
	public void testRetrieveOneBulletin() throws Exception
	{
		supplier.returnResultTag = NetworkInterfaceConstants.OK;
		
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

	FakeCallerSideMirroringGateway fakeGateway;
	MirroringRetriever fakeRetriever;
}

class FakeCallerSideMirroringGateway implements CallerSideMirroringGatewayInterface
{
	public NetworkResponse listAccountsForMirroring(MartusCrypto signer) throws MartusSignatureException
	{
		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.OK);
		Vector accounts = new Vector();
		accounts.add("a");
		result.add(accounts);
		return new NetworkResponse(result);
	}
	
	public NetworkResponse listBulletinsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException
	{
		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.OK);
		Vector uids = new Vector();
		uids.add(fakeUid);
		result.add(uids);
		return new NetworkResponse(result);
	}

	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int maxChunkSize) throws 
			MartusCrypto.MartusSignatureException
	{
		return null;
	}
	
	UniversalId fakeUid = UniversalId.createDummyUniversalId();
}
