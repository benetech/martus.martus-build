package org.martus.meta;

import org.martus.client.*;
import org.martus.common.*;
import org.martus.server.*;

import junit.framework.*;

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
		suite.addTest(new TestSuite(TestUniversalId.class));
		suite.addTest(new TestSuite(TestDatabaseKey.class));
		suite.addTest(new TestSuite(TestFileDatabase.class));
		suite.addTest(new TestSuite(TestMartusSecurity.class));
		suite.addTest(new TestSuite(TestUnicodeFileWriter.class));
		suite.addTest(new TestSuite(TestUnicodeFileReader.class));
		suite.addTest(new TestSuite(TestZipEntryInputStream.class));
		suite.addTest(new TestSuite(TestFileInputStreamWithReset.class));
		suite.addTest(new TestSuite(TestXmlWriterFilter.class));
		suite.addTest(new TestSuite(TestPacket.class));
		suite.addTest(new TestSuite(TestBulletinHeaderPacket.class));
		suite.addTest(new TestSuite(TestFieldDataPacket.class));
		suite.addTest(new TestSuite(TestAttachmentPacket.class));
		suite.addTest(new TestSuite(TestAttachmentProxy.class));
		
		// client
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

		// server
		suite.addTest(new TestSuite(TestMartusServer.class));
		suite.addTest(new TestSuite(TestMartusXml.class));
		suite.addTest(new TestSuite(TestServerFileDatabase.class));
		suite.addTest(new TestSuite(TestServerSideNetworkHandler.class));

	    return suite;
	}
}
