package org.martus.server;

import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;

public class TestSupplierSideMirroringHandler extends TestCaseEnhanced
{
	public TestSupplierSideMirroringHandler(String name) 
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		db = new MockServerDatabase();
		supplierSecurity = new MockMartusSecurity();
		handler = new SupplierSideMirroringHandler(db, supplierSecurity);
		
		callerSecurity = new MockMartusSecurity();
		callerSecurity.createKeyPair();
		callerAccountId = callerSecurity.getPublicKeyString();
	}

	public void testBadSignature() throws Exception
	{
		Vector parameters = new Vector();
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		parameters.add("Hello");
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.SIG_ERROR, result.get(0));
	}

	public void testNonStringCommand() throws Exception
	{
		handler.addAuthorizedCaller(callerAccountId);

		Vector parameters = new Vector();
		parameters.add(new Integer(3));
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.BAD_PARAMETER, result.get(0));
	}
	
	public void testUnknownCommand() throws Exception
	{
		handler.addAuthorizedCaller(callerAccountId);

		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add("This will never be a valid command!");
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.UNKNOWN_COMMAND, result.get(0));
	}
	
	
	public void testPing() throws Exception
	{
		handler.addAuthorizedCaller(callerAccountId);

		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_PING);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.OK, result.get(0));
	}
	
	public void testGetAllAccountsNotAuthorized() throws Exception
	{
		handler.clearAllAuthorizedCallers();
		
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_LIST_ACCOUNTS_FOR_BACKUP);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.NOT_AUTHORIZED, result.get(0));
	}

	public void testGetAllAccountsNoneAvailable() throws Exception
	{
		handler.clearAllAuthorizedCallers();
		handler.addAuthorizedCaller(callerAccountId);
		
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_LIST_ACCOUNTS_FOR_BACKUP);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(2, result.size());
		assertEquals(MirroringInterface.OK, result.get(0));
		Vector accounts = (Vector)result.get(1);
		assertEquals(0, accounts.size());
	}

	public void testGetAllAccounts() throws Exception
	{
		handler.addAuthorizedCaller(callerAccountId);

		String accountId1 = "first sample account";
		writeSealedRecord(db, accountId1);
		String accountId2 = "second sample account";
		writeSealedRecord(db, accountId2);

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_LIST_ACCOUNTS_FOR_BACKUP);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(MirroringInterface.OK, result.get(0));
		Vector accounts = (Vector)result.get(1);
		assertEquals(2, accounts.size());
		assertContains(accountId1, accounts);
		assertContains(accountId2, accounts);
	}
	
	public void testListSealedBulletinsNotAuthorized() throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_LIST_SEALED_BULLETINS_FOR_BACKUP);
		parameters.add("account id to ignore");
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.NOT_AUTHORIZED, result.get(0));
	}
	
//	public void testListSealedBulletins() throws Exception
//	{
//		String accountId1 = "first sample account";
//		String localId1 = writeSealedRecord(db, accountId1);
//		String accountId2 = "second sample account";
//		String localId2 = writeSealedRecord(db, accountId2);
//
//		Vector parameters = new Vector();
//		parameters.add(MirroringInterface.CMD_LIST_SEALED_BULLETINS_FOR_BACKUP);
//		String sig = MartusUtilities.sign(parameters, callerSecurity);
//		Vector result = handler.request(callerAccountId, parameters, sig);
//		assertEquals(MirroringInterface.OK, result.get(0));
//		Vector localIds = (Vector)result.get(1);
//		assertEquals(2, localIds.size());
//		assertContains(localId1, localIds);
//		assertContains(localId2, localIds);
//	}
	
	String writeSealedRecord(Database db, String accountId) throws Exception
	{
		UniversalId uid = UniversalId.createFromAccountAndPrefix(accountId, "x");
		DatabaseKey key = DatabaseKey.createSealedKey(uid);
		db.writeRecord(key, "just some sample data");
		return key.getLocalId();
	}

	MockServerDatabase db;
	MartusCrypto supplierSecurity;
	SupplierSideMirroringHandler handler;
	MartusCrypto callerSecurity;
	String callerAccountId;
}
