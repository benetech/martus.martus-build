package org.martus.client;

import org.martus.common.TestCommon;

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
		TestSuite suite= new TestSuite("All Client Martus Tests");

		suite.addTest(TestCommon.suite());
		suite.addTest(TestClient.suite());

	    return suite;
	}
}
