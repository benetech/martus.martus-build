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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.martus.client.core.Bulletin;
import org.martus.client.core.BulletinZipImporter;
import org.martus.client.core.BulletinStore;
import org.martus.common.AttachmentProxy;
import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.MartusConstants;
import org.martus.common.MartusCrypto;
import org.martus.common.Packet;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.CryptoException;

public class MockBulletin extends Bulletin
{
	public MockBulletin()
	{
		super((BulletinStore) null);
	}

	public static void loadFromZipString(Bulletin b, String zipString, MartusCrypto sigVerifier) throws IOException, Base64.InvalidBase64Exception
	{
		File tempFile = null;
		try
		{
			tempFile = Base64.decodeToTempFile(zipString);
			BulletinZipImporter.loadFromFile(b, tempFile, sigVerifier);
		}
		finally
		{
			if(tempFile != null)
				tempFile.delete();
		}
	}

	public static String saveToZipString(Database db, Bulletin b, MartusCrypto security) throws
		IOException,
		MartusCrypto.CryptoException
	{
		File tempFile = File.createTempFile("$$$Martus-saveToZipString", null);
		try
		{
			tempFile.deleteOnExit();
			saveToFile(db, b, tempFile, security);
			FileInputStream inputStream = new FileInputStream(tempFile);
			int len = inputStream.available();
			byte[] rawBytes = new byte[len];
			inputStream.read(rawBytes);
			inputStream.close();
			return Base64.encode(rawBytes);
		}
		finally
		{
			tempFile.delete();
		}

	}

	public static void saveToFile(Database db, Bulletin b, File destFile, MartusCrypto security) throws
		IOException,
		MartusCrypto.CryptoException
	{
		BulletinHeaderPacket header = b.getBulletinHeaderPacket();

		FieldDataPacket publicDataPacket = b.getFieldDataPacket();
		boolean shouldEncryptPublicData = (b.isDraft() || b.isAllPrivate());
		publicDataPacket.setEncrypted(shouldEncryptPublicData);

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
		ZipOutputStream zipOut = new ZipOutputStream(outputStream);
		try
		{
			byte[] dataSig = writePacketToZip(b, zipOut, b.getFieldDataPacket(), security);
			header.setFieldDataSignature(dataSig);

			byte[] privateDataSig = writePacketToZip(b, zipOut, b.getPrivateFieldDataPacket(), security);
			header.setPrivateFieldDataSignature(privateDataSig);

			writeAttachmentsToZip(db, b, zipOut, b.getPublicAttachments(), security);
			writeAttachmentsToZip(db, b, zipOut, b.getPrivateAttachments(), security);

			writePacketToZip(b, zipOut, header, security);
		}
		finally
		{
			zipOut.close();
		}
	}

	public static void writeAttachmentsToZip(Database db, Bulletin b, ZipOutputStream zipOut, AttachmentProxy[] attachments, MartusCrypto sigVerifier) throws
		IOException,
		CryptoException
	{
		for(int i = 0 ; i < attachments.length ; ++i)
		{
			UniversalId uid = attachments[i].getUniversalId();
			ZipEntry attachmentEntry = new ZipEntry(uid.getLocalId());
			zipOut.putNextEntry(attachmentEntry);
			InputStream in = new BufferedInputStream(db.openInputStream(new DatabaseKey(uid), sigVerifier));

			byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
			int got;
			while((got = in.read(bytes)) != -1)
			{
				zipOut.write(bytes, 0, got);
			}
			in.close();
			zipOut.flush();
		}
	}

	static byte[] writePacketToZip(Bulletin b, ZipOutputStream zipOut, Packet packet, MartusCrypto security) throws
		IOException
	{
		ZipEntry entry = new ZipEntry(packet.getLocalId());
		zipOut.putNextEntry(entry);

		UnicodeWriter writer = new UnicodeWriter(zipOut);
		byte[] sig = packet.writeXml(writer, security);
		writer.flush();
		return sig;
	}

}
