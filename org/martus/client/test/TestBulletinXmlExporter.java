/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

package org.martus.client.test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

import org.martus.client.core.Bulletin;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.BulletinXmlExporter;
import org.martus.common.AttachmentProxy;
import org.martus.common.BulletinConstants;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.MartusCrypto.EncryptionException;

public class TestBulletinXmlExporter extends TestCaseEnhanced
{
	public TestBulletinXmlExporter(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		if(store==null)
		{
			store = new BulletinStore(new MockClientDatabase());
			store.setSignatureGenerator(new MockMartusSecurity());
		}
	}

	public void testExportOneBulletin() throws Exception
	{
		Bulletin b = new Bulletin(store);
		
		final String sampleAuthor = "someone special";
		final String samplePrivateInfo = "this should not appear in the xml";

		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);
		b.set(BulletinConstants.TAGPRIVATEINFO, samplePrivateInfo);
		
		Vector list = new Vector();
		list.add(b);
		String result = doExport(list);

		assertContains("<ExportedMartusBulletins>", result);
		assertContains("<MartusBulletin>", result);
		assertContains(b.getAccount(), result);
		assertContains(b.getLocalId(), result);
		assertContains(sampleAuthor, result);
		
		//System.out.println(result);
	}

	public void testExportWithPublicAttachments() throws Exception
	{
		Bulletin b = new Bulletin(store);
		final File sampleAttachmentFile1 = addNewSampleAttachment(b);
		final File sampleAttachmentFile2 = addNewSampleAttachment(b);

		Vector list = new Vector();
		list.add(b);
		String result = doExport(list);

		assertContains(sampleAttachmentFile1.getName(), result);
		assertContains(sampleAttachmentFile2.getName(), result);
	}

	public void testExportMultipleBulletins() throws Exception
	{
		Bulletin b1 = new Bulletin(store);
		Bulletin b2 = new Bulletin(store);
		
		final String sampleTitle1 = "a big event took place!";
		final String sampleTitle2 = "watch this space";
		b1.set(BulletinConstants.TAGTITLE, sampleTitle1);
		b2.set(BulletinConstants.TAGTITLE, sampleTitle2);

		StringWriter writer = new StringWriter();
		Vector list = new Vector();
		list.add(b1);
		list.add(b2);
		BulletinXmlExporter.exportBulletins(list, writer);
		String result = writer.toString();

		assertContains(sampleTitle1, result);
		assertContains(sampleTitle2, result);
	}

	String doExport(Vector list) throws IOException
	{
		StringWriter writer = new StringWriter();
		BulletinXmlExporter.exportBulletins(list, writer);
		String result = writer.toString();
		return result;
	}

	File addNewSampleAttachment(Bulletin b)
		throws IOException, EncryptionException
	{
		final File sampleAttachmentFile = createTempFile();
		AttachmentProxy ap = new AttachmentProxy(sampleAttachmentFile);
		b.addPublicAttachment(ap);
		return sampleAttachmentFile;
	}

	static BulletinStore store;
}
