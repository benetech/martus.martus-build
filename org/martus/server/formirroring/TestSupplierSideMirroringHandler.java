package org.martus.server.formirroring;

import java.io.StringWriter;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.BulletinConstants;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterfaceConstants;
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
		supplier = new FakeServerSupplier();
		supplierSecurity = supplier.getSecurity();
		handler = new SupplierSideMirroringHandler(supplier, supplierSecurity);
		
		callerSecurity = MockMartusSecurity.createClient();
		callerAccountId = callerSecurity.getPublicKeyString();
		
		authorSecurity = MockMartusSecurity.createOtherClient();
	}

	public void testBadSignature() throws Exception
	{
		Vector parameters = new Vector();
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		parameters.add("Hello");
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.SIG_ERROR, result.get(0));
	}

	public void testNonStringCommand() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		Vector parameters = new Vector();
		parameters.add(new Integer(3));
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, result.get(0));
	}
	
	public void testUnknownCommand() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add("This will never be a valid command!");
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.UNKNOWN_COMMAND, result.get(0));
	}
	
	
	public void testPing() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_PING);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(2, result.size());
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		Vector publicInfo = (Vector)result.get(1);
		String publicKey = (String)publicInfo.get(0);
		String gotSig = (String)publicInfo.get(1);
		MartusUtilities.validatePublicInfo(publicKey, gotSig, callerSecurity);
		assertEquals(supplierSecurity.getPublicKeyString(), publicInfo.get(0));
	}
	
	public void testUnsignedPing() throws Exception
	{
		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_PING);
		String sig = "";
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(2, result.size());
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		Vector publicInfo = (Vector)result.get(1);
		String publicKey = (String)publicInfo.get(0);
		String gotSig = (String)publicInfo.get(1);
		MartusUtilities.validatePublicInfo(publicKey, gotSig, callerSecurity);
		assertEquals(supplierSecurity.getPublicKeyString(), publicInfo.get(0));
	}

	public void testAnonymousPing() throws Exception
	{
		String accountId = "";
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_PING);
		String sig = "";
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(2, result.size());
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		Vector publicInfo = (Vector)result.get(1);
		String publicKey = (String)publicInfo.get(0);
		String gotSig = (String)publicInfo.get(1);
		MartusUtilities.validatePublicInfo(publicKey, gotSig, callerSecurity);
		assertEquals(supplierSecurity.getPublicKeyString(), publicInfo.get(0));
	}

	public void testGetAllAccountsNotAuthorized() throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_ACCOUNTS);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.NOT_AUTHORIZED, result.get(0));
	}

	public void testGetAllAccountsNoneAvailable() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;
		
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_ACCOUNTS);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(2, result.size());
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		Vector accounts = (Vector)result.get(1);
		assertEquals(0, accounts.size());
	}

	public void testGetAllAccounts() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		String accountId1 = "first sample account";
		supplier.addAccountToMirror(accountId1);
		String accountId2 = "second sample account";
		supplier.addAccountToMirror(accountId2);

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_ACCOUNTS);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		Vector accounts = (Vector)result.get(1);
		assertEquals(2, accounts.size());
		assertContains(accountId1, accounts);
		assertContains(accountId2, accounts);
	}
	
	public void testListBulletinsNotAuthorized() throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_SEALED_BULLETINS);
		parameters.add("account id to ignore");
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.NOT_AUTHORIZED, result.get(0));
	}
	
	public void testListBulletinsBadAuthorAccountId() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_SEALED_BULLETINS);
		parameters.add(new Integer(3));
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, result.get(0));
	}
	
	public void testListBulletins() throws Exception
	{
		String authorAccountId = authorSecurity.getPublicKeyString();
		
		BulletinHeaderPacket bhp1 = new BulletinHeaderPacket(authorAccountId);
		bhp1.setStatus(BulletinConstants.STATUSSEALED);
		Vector result1 = writeSampleHeaderPacket(bhp1);
		
		BulletinHeaderPacket bhp2 = new BulletinHeaderPacket(authorAccountId);
		bhp2.setStatus(BulletinConstants.STATUSSEALED);
		Vector result2 = writeSampleHeaderPacket(bhp2);

		BulletinHeaderPacket bhpDraft = new BulletinHeaderPacket(authorAccountId);
		bhp2.setStatus(BulletinConstants.STATUSDRAFT);
		Vector result3 = writeSampleHeaderPacket(bhpDraft);
		
		supplier.authorizedCaller = callerAccountId;

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_LIST_SEALED_BULLETINS);
		parameters.add(authorAccountId);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		Vector infos = (Vector)result.get(1);
		assertEquals(3, infos.size());
		assertContains(result1, infos);
		assertContains(result2, infos);
		assertContains(result3, infos);
	}
	
	public void testGetBulletinUploadRecordNotFound() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_GET_BULLETIN_UPLOAD_RECORD);
		parameters.add("No such account");
		parameters.add("No such bulletin");
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(NetworkInterfaceConstants.NOT_FOUND, result.get(0));
		assertEquals(1, result.size());
	}

	public void testGetBulletinUploadRecord() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		String accountId = "account";
		String localId = "local";
		String bur = "This pretends to be a BUR";
		supplier.addBur(accountId, localId, bur);
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_GET_BULLETIN_UPLOAD_RECORD);
		parameters.add(accountId);
		parameters.add(localId);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		assertEquals(2, result.size());
	}

	public void testGetBulletinChunkNotAuthorized() throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_GET_BULLETIN_CHUNK);
		parameters.add("account id to ignore");
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.NOT_AUTHORIZED, result.get(0));
	}
	
	public void testGetBulletinChunkBadAuthorAccountId() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_GET_BULLETIN_CHUNK);
		parameters.add(new Integer(3));
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, result.get(0));
	}
	
	public void testGetBulletinChunkBadParameter() throws Exception
	{
		supplier.authorizedCaller = callerAccountId;

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_GET_BULLETIN_CHUNK);
		parameters.add("pretend account");
		parameters.add("pretend localid");
		parameters.add(new Integer(3));
		parameters.add("bad maxChunkSize");
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, result.get(0));
	}
	
	public void testGetBulletinChunk() throws Exception
	{
		final String authorAccountId = "a";
		final String bulletinLocalId = "b";
		final int offset = 123;
		final int maxChunkSize = 456;
		
		supplier.returnResultTag = NetworkInterfaceConstants.CHUNK_OK;
		supplier.authorizedCaller = callerAccountId;

		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_GET_BULLETIN_CHUNK);
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(offset));
		parameters.add(new Integer(maxChunkSize));
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(callerAccountId, parameters, sig);

		assertEquals(authorAccountId, supplier.gotAccount);
		assertEquals(bulletinLocalId, supplier.gotLocalId);
		assertEquals(offset, supplier.gotChunkOffset);
		assertEquals(maxChunkSize, supplier.gotMaxChunkSize);
	
		assertEquals(2, result.size());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, result.get(0));
		Vector details = (Vector)result.get(1);
		assertEquals(new Integer(supplier.getChunkSize() * 3), details.get(0));
		assertEquals(new Integer(supplier.getChunkSize()), details.get(1));
		assertEquals(supplier.returnZipData, details.get(2));
	}
	
	Vector writeSampleHeaderPacket(BulletinHeaderPacket bhp) throws Exception
	{
		StringWriter writer = new StringWriter();
		byte[] sigBytes = bhp.writeXml(writer, authorSecurity);
		DatabaseKey key = DatabaseKey.createDraftKey(bhp.getUniversalId());
		String sigString = Base64.encode(sigBytes);
		supplier.addBulletinToMirror(key, sigString);
		
		Vector info = new Vector();
		info.add(bhp.getLocalId());
		info.add(sigString);
		return info;
	}
	
	String writeSealedRecord(Database db, String accountId) throws Exception
	{
		UniversalId uid = UniversalId.createFromAccountAndPrefix(accountId, "x");
		DatabaseKey key = DatabaseKey.createSealedKey(uid);
		db.writeRecord(key, "just some sample data");
		return key.getLocalId();
	}

	FakeServerSupplier supplier;
	MartusCrypto supplierSecurity;
	SupplierSideMirroringHandler handler;
	MartusCrypto callerSecurity;
	String callerAccountId;
	
	MartusCrypto authorSecurity;
}

