package org.martus.common;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestCommon
{
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
		TestSuite suite= new TestSuite("All Common Martus Tests");

		// common stuff
		suite.addTest(new TestSuite(TestUniversalId.class));
		suite.addTest(new TestSuite(TestDatabaseKey.class));
		suite.addTest(new TestSuite(TestFileDatabase.class));
		suite.addTest(new TestSuite(TestMartusSecurity.class));
		suite.addTest(new TestSuite(TestUnicodeFileWriter.class));
		suite.addTest(new TestSuite(TestUnicodeFileReader.class));
		suite.addTest(new TestSuite(TestZipEntryInputStream.class));
		suite.addTest(new TestSuite(TestFileInputStreamWithSeek.class));
		suite.addTest(new TestSuite(TestMartusXml.class));
		suite.addTest(new TestSuite(TestXmlWriterFilter.class));
		suite.addTest(new TestSuite(TestPacket.class));
		suite.addTest(new TestSuite(TestBulletinHeaderPacket.class));
		suite.addTest(new TestSuite(TestFieldDataPacket.class));
		suite.addTest(new TestSuite(TestAttachmentPacket.class));
		suite.addTest(new TestSuite(TestAttachmentProxy.class));
		suite.addTest(new TestSuite(TestBase64XmlOutputStream.class));
		
		return suite;
	}
}
