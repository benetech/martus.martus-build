package org.martus.server.formirroring;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestForMirroring
{
	public static void main(String[] args)
	{
		runTests();
	}

	public static void runTests() 
	{
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite ( ) 
	{
		TestSuite suite= new TestSuite("All Server Mirroring Tests");

		suite.addTest(new TestSuite(TestMirroringRetriever.class));
		suite.addTest(new TestSuite(TestServerForMirroring.class));
		suite.addTest(new TestSuite(TestSupplierSideMirroringHandler.class));

		return suite;
	}
}
