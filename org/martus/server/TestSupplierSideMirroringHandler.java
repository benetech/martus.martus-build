package org.martus.server;

import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.TestCaseEnhanced;

public class TestSupplierSideMirroringHandler extends TestCaseEnhanced
{
	public TestSupplierSideMirroringHandler(String name) 
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		supplierSecurity = new MockMartusSecurity();
		handler = new SupplierSideMirroringHandler(supplierSecurity);
		
		callerSecurity = new MockMartusSecurity();
		callerSecurity.createKeyPair();
	}

	public void testBadSignature() throws Exception
	{
		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		parameters.add("Hello");
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.SIG_ERROR, result.get(0));
	}

	public void testNonStringCommand() throws Exception
	{
		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add(new Integer(3));
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.UNKNOWN_COMMAND, result.get(0));
	}
	
	public void testUnknownCommand() throws Exception
	{
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
		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_PING);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(1, result.size());
		assertEquals(MirroringInterface.OK, result.get(0));
	}
	
	public void testGetAllAccounts() throws Exception
	{
		String accountId = callerSecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_GET_ACCOUNTS);
		String sig = MartusUtilities.sign(parameters, callerSecurity);
		Vector result = handler.request(accountId, parameters, sig);
		assertEquals(2, result.size());
		assertEquals(MirroringInterface.OK, result.get(0));
		Vector accounts = (Vector)result.get(1);
		assertEquals(0, accounts.size());
	}

	MartusCrypto supplierSecurity;
	MirroringInterface handler;
	MartusCrypto callerSecurity;
}
