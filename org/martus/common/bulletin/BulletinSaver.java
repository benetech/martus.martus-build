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

package org.martus.common.bulletin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.Base64;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.Base64.InvalidBase64Exception;

public class BulletinSaver
{

	public static void saveToClientDatabase(Bulletin b, Database db, boolean mustEncryptPublicData, MartusCrypto signer) throws
			IOException,
			MartusCrypto.CryptoException
	{
		UniversalId uid = b.getUniversalId();
		BulletinHeaderPacket oldBhp = new BulletinHeaderPacket(uid);
		DatabaseKey key = new DatabaseKey(uid);
		boolean bulletinAlreadyExisted = false;
		try
		{
			if(db.doesRecordExist(key))
			{
				InputStreamWithSeek in = db.openInputStream(key, signer);
				oldBhp.loadFromXml(in, signer);
				bulletinAlreadyExisted = true;
			}
		}
		catch(Exception ignoreItBecauseWeCantDoAnythingAnyway)
		{
			//e.printStackTrace();
			//System.out.println("Bulletin.saveToDatabase: " + e);
		}

		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();

		FieldDataPacket publicDataPacket = b.getFieldDataPacket();
		boolean shouldEncryptPublicData = (b.isDraft() || b.isAllPrivate());
		publicDataPacket.setEncrypted(shouldEncryptPublicData);
		Packet packet1 = publicDataPacket;
		boolean encryptPublicData = mustEncryptPublicData;
		Database db1 = db;
		MartusCrypto signer1 = signer;

		byte[] dataSig = packet1.writeXmlToClientDatabase(db1, encryptPublicData, signer1);
		bhp.setFieldDataSignature(dataSig);
		Packet packet2 = b.getPrivateFieldDataPacket();
		boolean encryptPublicData1 = mustEncryptPublicData;
		Database db2 = db;
		MartusCrypto signer2 = signer;

		byte[] privateDataSig = packet2.writeXmlToClientDatabase(db2, encryptPublicData1, signer2);
		bhp.setPrivateFieldDataSignature(privateDataSig);

		for(int i = 0; i < b.getPendingPublicAttachments().size(); ++i)
		{
			// TODO: Should the bhp also remember attachment sigs?
			Packet packet = (Packet)b.getPendingPublicAttachments().get(i);
			boolean encryptPublicData2 = mustEncryptPublicData;
			Database db3 = db;
			MartusCrypto signer3 = signer;
			packet.writeXmlToClientDatabase(db3, encryptPublicData2, signer3);
		}

		for(int i = 0; i < b.getPendingPrivateAttachments().size(); ++i)
		{
			// TODO: Should the bhp also remember attachment sigs?
			Packet packet = (Packet)b.getPendingPrivateAttachments().get(i);
			Packet packet3 = packet;
			boolean encryptPublicData2 = mustEncryptPublicData;
			Database db3 = db;
			MartusCrypto signer3 = signer;
			packet3.writeXmlToClientDatabase(db3, encryptPublicData2, signer3);
		}

		bhp.updateLastSavedTime();
		Packet packet = bhp;
		boolean encryptPublicData2 = mustEncryptPublicData;
		Database db3 = db;
		MartusCrypto signer3 = signer;
		packet.writeXmlToClientDatabase(db3, encryptPublicData2, signer3);

		if(bulletinAlreadyExisted)
		{
			String accountId = b.getAccount();
			String[] oldPublicAttachmentIds = oldBhp.getPublicAttachmentIds();
			String[] newPublicAttachmentIds = bhp.getPublicAttachmentIds();
			BulletinSaver.deleteRemovedPackets(db, accountId, oldPublicAttachmentIds, newPublicAttachmentIds);

			String[] oldPrivateAttachmentIds = oldBhp.getPrivateAttachmentIds();
			String[] newPrivateAttachmentIds = bhp.getPrivateAttachmentIds();
			BulletinSaver.deleteRemovedPackets(db, accountId, oldPrivateAttachmentIds, newPrivateAttachmentIds);
		}
	}


	protected static void deleteRemovedPackets(Database db, String accountId, String[] oldIds, String[] newIds)
	{
		for(int oldIndex = 0; oldIndex < oldIds.length; ++oldIndex)
		{
			String oldLocalId = oldIds[oldIndex];
			if(!MartusUtilities.isStringInArray(newIds, oldLocalId))
			{
				UniversalId auid = UniversalId.createFromAccountAndLocalId(accountId, oldLocalId);
				db.discardRecord(new DatabaseKey(auid));
			}
		}
	}

	public static void extractAttachmentToFile(Database db, AttachmentProxy a, MartusCrypto verifier, File destFile) throws
		IOException,
		Base64.InvalidBase64Exception,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		Packet.WrongPacketTypeException,
		MartusCrypto.CryptoException
	{
		FileOutputStream out = new FileOutputStream(destFile);
		extractAttachmentToStream(db, a, verifier, out);
	}


	public static void extractAttachmentToStream(Database db, AttachmentProxy a, MartusCrypto verifier, OutputStream out)
		throws
			IOException,
			CryptoException,
			InvalidPacketException,
			SignatureVerificationException,
			WrongPacketTypeException,
			InvalidBase64Exception
	{
		UniversalId uid = a.getUniversalId();
		byte[] sessionKeyBytes = a.getSessionKeyBytes();
		DatabaseKey key = new DatabaseKey(uid);
		InputStreamWithSeek xmlIn = db.openInputStream(key, verifier);
		AttachmentPacket.exportRawFileFromXml(xmlIn, sessionKeyBytes, verifier, out);
	}
}
