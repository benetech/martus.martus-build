package org.martus.client;

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

		suite.addTest(new TestSuite(TestMartusApp.class));
		suite.addTest(new TestSuite(TestBulletinFolder.class));
		suite.addTest(new TestSuite(TestBulletinStore.class));
		suite.addTest(new TestSuite(TestBulletin.class));
		suite.addTest(new TestSuite(TestSearchTreeNode.class));
		suite.addTest(new TestSuite(TestSearchParser.class));
		suite.addTest(new TestSuite(TestConfigInfo.class));
		suite.addTest(new TestSuite(TestMartusLocalization.class));
		suite.addTest(new TestSuite(TestBulletinsList.class));
		suite.addTest(new TestSuite(TestFolderList.class));
		suite.addTest(new TestSuite(TestTransferableBulletin.class));
		suite.addTest(new TestSuite(TestChoiceItem.class));
		suite.addTest(new TestSuite(TestRetrieveMyTableModel.class));
		suite.addTest(new TestSuite(TestRetrieveMyDraftsTableModel.class));
		suite.addTest(new TestSuite(TestRetrieveHQTableModel.class));
		suite.addTest(new TestSuite(TestRetrieveHQDraftsTableModel.class));
		suite.addTest(new TestSuite(TestCurrentUiState.class));
		suite.addTest(new TestSuite(TestSimpleX509TrustManager.class));
		suite.addTest(new TestSuite(TestClientFileDatabase.class));

		suite.addTest(org.martus.common.TestAll.suite());

	    return suite;
	}
}
