package org.martus.server.forclients;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestServerForClientsPackage extends TestCase
{

	public TestServerForClientsPackage(String name)
	{
		super(name);
	}

	public static void main(String[] args)
	{
	}

	public static Test suite() 
	{
		TestSuite suite= new TestSuite("All ForClient Tests");

		suite.addTest(new TestSuite(TestServerForClients.class));
		suite.addTest(new TestSuite(TestMartusServer.class));
		suite.addTest(new TestSuite(TestServerSideNetworkHandler.class));
		
		return suite;
	}
}
