package org.martus.server.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.server.forclients.TestForClients;
import org.martus.server.formirroring.TestForMirroring;

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

		suite.addTest(TestServerCore.suite());
		suite.addTest(TestForClients.suite());
		suite.addTest(TestForMirroring.suite());

	    return suite;
	}
}
