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

package org.martus.client.core;

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.common.MartusXml;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;

public class BulletinXmlExporter
{
	public static void exportBulletins(Writer dest, Vector bulletins, boolean includePrivateData)
		throws IOException
	{
		dest.write(MartusXml.getTagStart(ExportedBulletinsElementName));
		if(includePrivateData)
			writeElement(dest, PublicAndPrivateElementName, "");
		else
			writeElement(dest, PublicOnlyElementName, "");
		dest.write("\n");

		for (int i = 0; i < bulletins.size(); i++)
		{
			Bulletin b = (Bulletin)bulletins.get(i);
			exportOneBulletin(b, dest, includePrivateData);
		}
		dest.write(MartusXml.getTagEnd(ExportedBulletinsElementName));
	}

	static void exportOneBulletin(Bulletin b, Writer dest, boolean includePrivateData) throws IOException
	{
		dest.write(MartusXml.getTagStart(BulletinElementName));
		dest.write("\n");

		writeElement(dest, LocalIdElementName, b.getLocalId());
		writeElement(dest, AccountIdElementName, b.getAccount());
		if(b.isAllPrivate())
			writeElement(dest, AllPrivateElementName, "");

		if(includePrivateData || !b.isAllPrivate())
		{
			dest.write(MartusXml.getTagStart(PublicDataElementName));
			writeFields(dest, b, BulletinStore.getDefaultPublicFieldTags());
			writeAttachments(dest, b.getPublicAttachments());
			dest.write(MartusXml.getTagEnd(PublicDataElementName));
		}

		if(includePrivateData)
		{
			dest.write(MartusXml.getTagStart(PrivateDataElementName));
			writeFields(dest, b, BulletinStore.getDefaultPrivateFieldTags());
			writeAttachments(dest, b.getPrivateAttachments());
			dest.write(MartusXml.getTagEnd(PrivateDataElementName));
		}

		dest.write(MartusXml.getTagEnd(BulletinElementName));
		dest.write("\n");
	}

	static void writeAttachments(Writer dest, AttachmentProxy[] publicAttachments)
		throws IOException
	{
		if(publicAttachments.length == 0)
			return;

		dest.write(MartusXml.getTagStart(AttachmentsListElementName));
		for (int i = 0; i < publicAttachments.length; i++)
		{
			AttachmentProxy proxy = publicAttachments[i];
			writeElement(dest, AttachmentElementName, proxy.getLabel());
		}
		dest.write(MartusXml.getTagEnd(AttachmentsListElementName));
	}

	static void writeFields(Writer dest, Bulletin b, String[] tags)
		throws IOException
	{
		for (int i = 0; i < tags.length; i++)
		{
			String tag = tags[i];
			String rawFieldData = b.get(tag);
			writeElement(dest, tag, rawFieldData);
		}
	}

	static void writeElement(Writer dest, String tag, String rawFieldData) throws IOException
	{
		dest.write(MartusXml.getTagStart(tag));
		dest.write(MartusUtilities.getXmlEncoded(rawFieldData));
		dest.write(MartusXml.getTagEnd(tag));
	}

	public final static String ExportedBulletinsElementName = "ExportedMartusBulletins";
	public final static String PublicOnlyElementName = "PublicDataOnly";
	public final static String PublicAndPrivateElementName = "PublicAndPrivateData";
	public final static String BulletinElementName = "MartusBulletin";
	public final static String PublicDataElementName = "PublicData";
	public final static String PrivateDataElementName = "PrivateData";
	public final static String LocalIdElementName = "LocalId";
	public final static String AllPrivateElementName = "AllPrivate";
	public final static String AccountIdElementName = "AuthorAccountId";
	public final static String AttachmentsListElementName = "AttachmentList";
	public final static String AttachmentElementName = "Attachment";
}
