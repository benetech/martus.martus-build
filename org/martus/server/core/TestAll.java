package org.martus.server.core;

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

		suite.addTest(TestServerCore.suite());
		suite.addTest(org.martus.common.TestCommon.suite());

	    return suite;
	}
}
