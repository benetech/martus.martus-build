package org.martus.client;

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

import org.martus.common.AttachmentProxy;
import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
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

	public static void loadFromZipString(Bulletin b, String zipString) throws IOException, Base64.InvalidBase64Exception
	{
		File tempFile = null;
		try
		{
			tempFile = Base64.decodeToTempFile(zipString);
			b.loadFromFile(tempFile, b.getStore().getSignatureVerifier());
		}
		finally
		{
			if(tempFile != null)
				tempFile.delete();
		}
	}
	
	public static String saveToZipString(Bulletin b) throws 
		IOException,
		MartusCrypto.CryptoException
	{
		File tempFile = File.createTempFile("$$$Martus-saveToZipString", null);
		try
		{
			tempFile.deleteOnExit();
			saveToFile(b, tempFile);
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

	public static void saveToFile(Bulletin b, File destFile) throws 
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
			byte[] dataSig = writePacketToZip(b, zipOut, b.getFieldDataPacket());
			header.setFieldDataSignature(dataSig);
			
			byte[] privateDataSig = writePacketToZip(b, zipOut, b.getPrivateFieldDataPacket());
			header.setPrivateFieldDataSignature(privateDataSig);
			
			writeAttachmentsToZip(b, zipOut, b.getPublicAttachments());
			writeAttachmentsToZip(b, zipOut, b.getPrivateAttachments());

			writePacketToZip(b, zipOut, header);
		}
		finally
		{
			zipOut.close();
		}
	}

	public static void writeAttachmentsToZip(Bulletin b, ZipOutputStream zipOut, AttachmentProxy[] attachments) throws 
		IOException, 
		CryptoException 
	{
		for(int i = 0 ; i < attachments.length ; ++i)
		{
			UniversalId uid = attachments[i].getUniversalId();
			ZipEntry attachmentEntry = new ZipEntry(uid.getLocalId());
			zipOut.putNextEntry(attachmentEntry);
			InputStream in = new BufferedInputStream(b.getDatabase().openInputStream(new DatabaseKey(uid), b.getStore().getSignatureVerifier()));

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
	
	static byte[] writePacketToZip(Bulletin b, ZipOutputStream zipOut, Packet packet) throws 
		IOException
	{
		ZipEntry entry = new ZipEntry(packet.getLocalId());
		zipOut.putNextEntry(entry);
		
		MartusCrypto signer = b.getStore().getSignatureGenerator();
		UnicodeWriter writer = new UnicodeWriter(zipOut);
		byte[] sig = packet.writeXml(writer, signer);
		writer.flush();
		return sig;
	}
	
}
