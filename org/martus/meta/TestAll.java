package org.martus.meta;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.client.test.TestClient;
import org.martus.common.TestCommon;
import org.martus.server.core.TestServerCore;
import org.martus.server.forclients.TestServerForClientsPackage;

public class TestAll extends java.lang.Object 
{
    public TestAll() 
    {
    }

	public static void main (String[] args) 
	{
		runTests();
	}

	public static void runTests () 
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite ( ) 
	{
		TestSuite suite= new TestSuite("All Martus Tests");

		suite.addTest(TestMeta.suite());
		
		// shared stuff
		suite.addTest(TestCommon.suite());
		suite.addTest(TestServerCore.suite());
		suite.addTest(TestServerForClientsPackage.suite());
		suite.addTest(TestClient.suite());

	    return suite;
	}
}
