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
		suite.addTest(new TestSuite(TestMartusUtilities.class));
		suite.addTest(new TestSuite(TestThreads.class));

		suite.addTest(new TestSuite(TestMartusServer.class));

		suite.addTest(new TestSuite(TestMartusApp.class));
		suite.addTest(new TestSuite(TestRetrieveTableModel.class));
		suite.addTest(new TestSuite(TestRetrieveMyTableModel.class));
		suite.addTest(new TestSuite(TestRetrieveMyDraftsTableModel.class));
		suite.addTest(new TestSuite(TestRetrieveHQTableModel.class));
		suite.addTest(new TestSuite(TestRetrieveHQDraftsTableModel.class));
		suite.addTest(new TestSuite(TestDeleteDraftsTableModel.class));
		suite.addTest(new TestSuite(TestSimpleX509TrustManager.class));
		
		// shared stuff
		suite.addTest(org.martus.common.TestAll.suite());
		suite.addTest(org.martus.server.TestAll.suite());
		suite.addTest(org.martus.client.TestAll.suite());

	    return suite;
	}
}
