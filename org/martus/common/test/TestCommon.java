/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.common.test;

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
		suite.addTest(new TestSuite(TestAttachmentPacket.class));
		suite.addTest(new TestSuite(TestAttachmentProxy.class));
		suite.addTest(new TestSuite(TestBase64XmlOutputStream.class));
		suite.addTest(new TestSuite(TestBulletin.class));
		suite.addTest(new TestSuite(TestBulletinHeaderPacket.class));
		suite.addTest(new TestSuite(TestBulletinLoader.class));
		suite.addTest(new TestSuite(TestBulletinSaver.class));
		suite.addTest(new TestSuite(TestBulletinSearcher.class));
		suite.addTest(new TestSuite(TestBulletinZipImporter.class));
		suite.addTest(new TestSuite(TestDatabaseKey.class));
		suite.addTest(new TestSuite(TestFieldDataPacket.class));
		suite.addTest(new TestSuite(TestFieldSpec.class));
		suite.addTest(new TestSuite(TestFileDatabase.class));
		suite.addTest(new TestSuite(TestFileInputStreamWithSeek.class));
		suite.addTest(new TestSuite(TestFileOutputStreamViaTemp.class));
//logi put back in after 30 day wait		suite.addTest(new TestSuite(TestKeyShareSaveRestore.class));
		suite.addTest(new TestSuite(TestMartusSecurity.class));
		suite.addTest(new TestSuite(TestMartusUtilities.class));
		suite.addTest(new TestSuite(TestMartusXml.class));
		suite.addTest(new TestSuite(TestPacket.class));
		suite.addTest(new TestSuite(TestSearchParser.class));
		suite.addTest(new TestSuite(TestSearchTreeNode.class));
		suite.addTest(new TestSuite(TestUnicodeFileReader.class));
		suite.addTest(new TestSuite(TestUnicodeFileWriter.class));
		suite.addTest(new TestSuite(TestUniversalId.class));
		suite.addTest(new TestSuite(TestXmlWriterFilter.class));
		suite.addTest(new TestSuite(TestZipEntryInputStream.class));

		return suite;
	}
}
