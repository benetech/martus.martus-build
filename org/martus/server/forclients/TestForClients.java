package org.martus.server.forclients;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestForClients
{
	public static void main(String[] args) 
	{
		runTests();
	}

	public static void runTests() 
	{
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() 
	{
		TestSuite suite = new TestSuite("All Server ForClient Tests");

		suite.addTest(new TestSuite(TestMartusServer.class));
		suite.addTest(new TestSuite(TestServerForClients.class));
		suite.addTest(new TestSuite(TestServerSideNetworkHandler.class));

		return suite;
	}
}
