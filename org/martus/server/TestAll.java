package org.martus.server;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll 
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

	    return suite;
	}
}
