package org.martus.server.core;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestServerCore
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
		TestSuite suite= new TestSuite("All Server Core Martus Tests");

		suite.addTest(new TestSuite(TestServerFileDatabase.class));

	    return suite;
	}
}
