package org.martus.meta;

import junit.framework.Test;
import junit.framework.TestSuite;

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

		// meta stuff
		suite.addTest(new TestSuite(TestSSL.class));
		suite.addTest(new TestSuite(TestDatabase.class));
		
		// shared stuff
		suite.addTest(org.martus.common.TestAll.suite());
		suite.addTest(org.martus.server.TestAll.suite());
		suite.addTest(org.martus.client.TestAll.suite());

	    return suite;
	}
}
