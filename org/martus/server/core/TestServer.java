package org.martus.server.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.server.forclients.TestMartusServer;
import org.martus.server.forclients.TestServerSideNetworkHandler;
import org.martus.server.formirroring.TestSupplierSideMirroringHandler;

public class TestServer
{
	public static void main(String[] args)
	{
		runTests();
	}

	public static void runTests () 
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite ( ) 
	{
		TestSuite suite= new TestSuite("All Server Martus Tests");

		suite.addTest(new TestSuite(TestServerFileDatabase.class));
		suite.addTest(new TestSuite(TestServerSideNetworkHandler.class));
		suite.addTest(new TestSuite(TestSupplierSideMirroringHandler.class));
		suite.addTest(new TestSuite(TestMartusServer.class));

	    return suite;
	}
}
