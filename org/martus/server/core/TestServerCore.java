package org.martus.server.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.server.formirroring.TestMirroringRetriever;
import org.martus.server.formirroring.TestServerForMirroring;
import org.martus.server.formirroring.TestSupplierSideMirroringHandler;

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
		suite.addTest(new TestSuite(TestSupplierSideMirroringHandler.class));
		suite.addTest(new TestSuite(TestMirroringRetriever.class));
		suite.addTest(new TestSuite(TestServerForMirroring.class));

	    return suite;
	}
}
