/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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
		b.setAllPrivate(false);

		final String sampleAuthor = "someone special";

		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);

		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false);

		assertContains("<ExportedMartusBulletins>", result);
		assertContains("<MartusBulletin>", result);
		assertContains(b.getAccount(), result);
		assertContains(b.getLocalId(), result);
		assertContains(sampleAuthor, result);
		assertNotContains("<PrivateData>", result);
		assertNotContains("<AttachmentList>", result);

		//System.out.println(result);
	}

	public void testExportWithPublicAttachments() throws Exception
	{
		Bulletin b = new Bulletin(store);
		b.setAllPrivate(false);
		final File sampleAttachmentFile1 = addNewPublicSampleAttachment(b);
		final File sampleAttachmentFile2 = addNewPublicSampleAttachment(b);

		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false);

		assertContains(sampleAttachmentFile1.getName(), result);
		assertContains(sampleAttachmentFile2.getName(), result);
	}

	public void testExportMultipleBulletins() throws Exception
	{
		Bulletin b1 = new Bulletin(store);
		b1.setAllPrivate(false);
		Bulletin b2 = new Bulletin(store);
		b2.setAllPrivate(false);

		final String sampleTitle1 = "a big event took place!";
		final String sampleTitle2 = "watch this space";
		b1.set(BulletinConstants.TAGTITLE, sampleTitle1);
		b2.set(BulletinConstants.TAGTITLE, sampleTitle2);

		StringWriter writer = new StringWriter();
		Vector list = new Vector();
		list.add(b1);
		list.add(b2);
		BulletinXmlExporter.exportBulletins(writer, list, false);
		String result = writer.toString();

		assertContains(sampleTitle1, result);
		assertContains(sampleTitle2, result);
	}

	public void testExportPrivateData() throws Exception
	{
		Bulletin b = new Bulletin(store);
		b.setAllPrivate(false);

		final String samplePublic = "someone special";
		final String samplePrivate = "shhhhh! it's private!";

		b.set(BulletinConstants.TAGPUBLICINFO, samplePublic);
		b.set(BulletinConstants.TAGPRIVATEINFO, samplePrivate);

		Vector list = new Vector();
		list.add(b);
		String publicOnly = doExport(list, false);
		assertContains("<PublicDataOnly></PublicDataOnly>", publicOnly);
		assertContains(samplePublic, publicOnly);
		assertNotContains(samplePrivate, publicOnly);

		String publicAndPrivate = doExport(list, true);
		assertContains("<PublicAndPrivateData></PublicAndPrivateData>", publicAndPrivate);
		assertContains(samplePublic, publicAndPrivate);
		assertContains(samplePrivate, publicAndPrivate);
	}

	public void testExportWithPrivateAttachment() throws Exception
	{
		Bulletin b = new Bulletin(store);
		b.setAllPrivate(false);
		final File sampleAttachmentFile1 = addNewPrivateSampleAttachment(b);

		Vector list = new Vector();
		list.add(b);

		String publicOnly = doExport(list, false);
		assertNotContains(sampleAttachmentFile1.getName(), publicOnly);

		String publicAndPrivate = doExport(list, true);
		assertContains(sampleAttachmentFile1.getName(), publicAndPrivate);
	}

	public void testExportAnAllPrivateBulletin() throws Exception
	{
		Bulletin b = new Bulletin(store);
		b.setAllPrivate(true);
		final String sampleAuthor = "someone special";
		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);

		Vector list = new Vector();
		list.add(b);
		String publicOnly = doExport(list, false);
		String publicAndPrivate = doExport(list, true);

		assertContains(b.getAccount(), publicOnly);
		assertContains(b.getLocalId(), publicOnly);
		assertContains("<AllPrivate></AllPrivate>", publicOnly);
		assertNotContains(sampleAuthor, publicOnly);
		assertNotContains("<PublicData>", publicOnly);
		assertNotContains("<PrivateData>", publicOnly);

		assertContains(b.getAccount(), publicAndPrivate);
		assertContains(b.getLocalId(), publicAndPrivate);
		assertContains("<AllPrivate></AllPrivate>", publicAndPrivate);
		assertContains(sampleAuthor, publicAndPrivate);
		assertContains("<PublicData>", publicAndPrivate);
		assertContains("<PrivateData>", publicAndPrivate);
	}

	String doExport(Vector list, boolean includePrivateData) throws IOException
	{
		StringWriter writer = new StringWriter();
		BulletinXmlExporter.exportBulletins(writer, list, includePrivateData);
		String result = writer.toString();
		return result;
	}

	File addNewPublicSampleAttachment(Bulletin b)
		throws IOException, EncryptionException
	{
		final File sampleAttachmentFile = createTempFile();
		AttachmentProxy ap = new AttachmentProxy(sampleAttachmentFile);
		b.addPublicAttachment(ap);
		return sampleAttachmentFile;
	}

	File addNewPrivateSampleAttachment(Bulletin b)
		throws IOException, EncryptionException
	{
		final File sampleAttachmentFile = createTempFile();
		AttachmentProxy ap = new AttachmentProxy(sampleAttachmentFile);
		b.addPrivateAttachment(ap);
		return sampleAttachmentFile;
	}

	static BulletinStore store;
}
