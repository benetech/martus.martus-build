package org.martus.server.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.common.TestCommon;

public class TestAll
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
		TestSuite suite = new TestSuite("All Server and Common Martus Tests");

		suite.addTest(TestCommon.suite());
		suite.addTest(TestServer.suite());

		return suite;
	}
}
