package org.martus.client.core;

import java.io.IOException;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.Packet;
import org.martus.common.UniversalId;

public class BulletinSaver
{

	public static void saveToDatabase(Bulletin b, Database db) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		MartusCrypto signer = b.getStore().getSignatureGenerator();
		
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
		boolean mustEncryptPublicData = b.mustEncryptPublicData();
		Packet packet1 = publicDataPacket;
		boolean encryptPublicData = mustEncryptPublicData;
		Database db1 = db;
		MartusCrypto signer1 = signer;
			
		byte[] dataSig = packet1.writeXmlToDatabase(db1, encryptPublicData, signer1);
		bhp.setFieldDataSignature(dataSig);
		Packet packet2 = b.getPrivateFieldDataPacket();
		boolean encryptPublicData1 = mustEncryptPublicData;
		Database db2 = db;
		MartusCrypto signer2 = signer;
	
		byte[] privateDataSig = packet2.writeXmlToDatabase(db2, encryptPublicData1, signer2);
		bhp.setPrivateFieldDataSignature(privateDataSig);
	
		for(int i = 0; i < b.getPendingPublicAttachments().size(); ++i)
		{
			// TODO: Should the bhp also remember attachment sigs?
			Packet packet = (Packet)b.getPendingPublicAttachments().get(i);
			boolean encryptPublicData2 = mustEncryptPublicData;
			Database db3 = db;
			MartusCrypto signer3 = signer;
			byte[] writePacketToDatabase = packet.writeXmlToDatabase(db3, encryptPublicData2, signer3);
		}
	
		for(int i = 0; i < b.getPendingPrivateAttachments().size(); ++i)
		{
			// TODO: Should the bhp also remember attachment sigs?
			Packet packet = (Packet)b.getPendingPrivateAttachments().get(i);
			Packet packet3 = packet;
			boolean encryptPublicData2 = mustEncryptPublicData;
			Database db3 = db;
			MartusCrypto signer3 = signer;
			byte[] writePacketToDatabase = packet3.writeXmlToDatabase(db3, encryptPublicData2, signer3);
		}
	
		bhp.updateLastSavedTime();
		Packet packet = bhp;
		boolean encryptPublicData2 = mustEncryptPublicData;
		Database db3 = db;
		MartusCrypto signer3 = signer;
		byte[] writePacketToDatabase = packet.writeXmlToDatabase(db3, encryptPublicData2, signer3);
	
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
}
