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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.MartusConstants;
import org.martus.common.MartusUtilities;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.StreamEncryptor;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.BulletinRetrieverGatewayInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.Base64;
import org.martus.util.ByteArrayInputStreamWithSeek;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.StreamCopier;
import org.martus.util.StreamFilter;
import org.martus.util.ZipEntryInputStream;


public class BulletinZipUtilities
{

	public static void exportPublicBulletinPacketsFromDatabaseToZipFile(Database db, DatabaseKey headerKey, File destZipFile, MartusCrypto security) throws
			IOException,
			MartusCrypto.CryptoException,
			UnsupportedEncodingException,
			Packet.InvalidPacketException,
			Packet.WrongPacketTypeException,
			Packet.SignatureVerificationException,
			MartusCrypto.DecryptionException,
			MartusCrypto.NoKeyPairException,
			FileNotFoundException
	{
		BulletinHeaderPacket bhp;
		String headerXml = "";
	
		bhp = new BulletinHeaderPacket("");
		try
		{
			headerXml = db.readRecord(headerKey, security);
	
			byte[] headerBytes = headerXml.getBytes("UTF-8");
	
			ByteArrayInputStreamWithSeek headerIn = new ByteArrayInputStreamWithSeek(headerBytes);
	
			MartusCrypto doNotCheckSigDuringDownload = null;
			bhp.loadFromXml(headerIn, doNotCheckSigDuringDownload);
			DatabaseKey[] packetKeys = bhp.getPublicPacketKeys();
	
			FileOutputStream outputStream = new FileOutputStream(destZipFile);
			BulletinZipUtilities.extractPacketsToZipStream(headerKey.getAccountId(), db, packetKeys, outputStream, security);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
		// TODO: REMOVE THIS! IT IS ONLY FOR DEBUGGING! SLOW SLOW SLOW!
		try
		{
			ZipFile zip = new ZipFile(destZipFile);
			BulletinZipUtilities.validateIntegrityOfZipFilePublicPackets(headerKey.getAccountId(), zip, security);
			zip.close();
		}
		catch (Packet.InvalidPacketException e)
		{
			System.out.println("MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile:");
			System.out.println("  InvalidPacket in bulletin: " + bhp.getLocalId());
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile: validation failed!");
			throw new IOException("Zip validation exception: " + e.getMessage());
		}
	}

	public static void exportBulletinPacketsFromDatabaseToZipFile(Database db, DatabaseKey headerKey, File destZipFile, MartusCrypto security) throws
			IOException,
			MartusCrypto.CryptoException,
			UnsupportedEncodingException,
			Packet.InvalidPacketException,
			Packet.WrongPacketTypeException,
			Packet.SignatureVerificationException,
			MartusCrypto.DecryptionException,
			MartusCrypto.NoKeyPairException,
			FileNotFoundException
	{
		String headerXml = db.readRecord(headerKey, security);
		byte[] headerBytes = headerXml.getBytes("UTF-8");
	
		ByteArrayInputStreamWithSeek headerIn = new ByteArrayInputStreamWithSeek(headerBytes);
		BulletinHeaderPacket bhp = new BulletinHeaderPacket("");
	
		MartusCrypto doNotCheckSigDuringDownload = null;
		bhp.loadFromXml(headerIn, doNotCheckSigDuringDownload);
		UniversalId loadedUid = bhp.getUniversalId();
		if(!loadedUid.equals(headerKey.getUniversalId()))
			throw new Packet.InvalidPacketException("Loaded uid doesn't match key uid");
	
		DatabaseKey[] packetKeys = BulletinZipUtilities.getAllPacketKeys(bhp);
	
		FileOutputStream outputStream = new FileOutputStream(destZipFile);
		BulletinZipUtilities.extractPacketsToZipStream(headerKey.getAccountId(), db, packetKeys, outputStream, security);
	
		// TODO: REMOVE THIS! IT IS ONLY FOR DEBUGGING! SLOW SLOW SLOW!
		try
		{
			ZipFile zip = new ZipFile(destZipFile);
			BulletinZipUtilities.validateIntegrityOfZipFilePackets(headerKey.getAccountId(), zip, security);
			zip.close();
		}
		catch (Packet.InvalidPacketException e)
		{
			System.out.println("MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile:");
			System.out.println("  InvalidPacket in bulletin: " + bhp.getLocalId());
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile: validation failed!");
			throw new IOException("Zip validation exception: " + e.getMessage());
		}
	}

	public static DatabaseKey[] getAllPacketKeys(BulletinHeaderPacket bhp)
	{
		String accountId = bhp.getAccountId();
		String[] publicAttachmentIds = bhp.getPublicAttachmentIds();
		String[] privateAttachmentIds = bhp.getPrivateAttachmentIds();
	
		int corePacketCount = 3;
		int publicAttachmentCount = publicAttachmentIds.length;
		int privateAttachmentCount = privateAttachmentIds.length;
		int totalPacketCount = corePacketCount + publicAttachmentCount + privateAttachmentCount;
		DatabaseKey[] keys = new DatabaseKey[totalPacketCount];
		int next = 0;
	
		UniversalId dataUid = UniversalId.createFromAccountAndLocalId(accountId, bhp.getFieldDataPacketId());
		UniversalId privateDataUid = UniversalId.createFromAccountAndLocalId(accountId, bhp.getPrivateFieldDataPacketId());
	
		keys[next++] = bhp.createKeyWithHeaderStatus(dataUid);
		keys[next++] = bhp.createKeyWithHeaderStatus(privateDataUid);
		for(int i=0; i < publicAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, publicAttachmentIds[i]);
			keys[next++] = bhp.createKeyWithHeaderStatus(uid);
		}
		for(int i=0; i < privateAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, privateAttachmentIds[i]);
			keys[next++] = bhp.createKeyWithHeaderStatus(uid);
		}
		keys[next++] = bhp.createKeyWithHeaderStatus(bhp.getUniversalId());
	
		return keys;
	}

	public static void extractPacketsToZipStream(String clientId, Database db, DatabaseKey[] packetKeys, OutputStream outputStream, MartusCrypto security) throws
		IOException,
		UnsupportedEncodingException
	{
		ZipOutputStream zipOut = new ZipOutputStream(outputStream);
// TODO: Setting the method to STORED seems like it should dramatically
// speed up writing and reading zip files. The javadocs say it is supported.
// But every time I try it, the zip file ends up empty. kbs.
//		zipOut.setMethod(zipOut.STORED);

		try
		{
			for(int i = 0; i < packetKeys.length; ++i)
			{
				DatabaseKey key = packetKeys[i];
				ZipEntry entry = new ZipEntry(key.getLocalId());
				zipOut.putNextEntry(entry);

				InputStream in = db.openInputStream(key, security);

				int got;
				byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
				while( (got=in.read(bytes)) >= 0)
					zipOut.write(bytes, 0, got);

				in.close();
				zipOut.flush();
			}
		}
		catch(MartusCrypto.CryptoException e)
		{
			throw new IOException("CryptoException " + e);
		}
		finally
		{
			zipOut.close();
		}
	}

	public static void importBulletinPacketsFromZipFileToDatabase(Database db, String authorAccountId, ZipFile zip, MartusCrypto security)
		throws IOException,
		Database.RecordHiddenException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		Packet.WrongAccountException,
		MartusCrypto.DecryptionException
	{
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		if(authorAccountId == null)
			authorAccountId = header.getAccountId();
	
		BulletinZipUtilities.validateIntegrityOfZipFilePackets(authorAccountId, zip, security);
		MartusUtilities.deleteDraftBulletinPackets(db, header.getUniversalId(), security);
	
		HashMap zipEntries = new HashMap();
		StreamCopier copier = new StreamCopier();
		StreamEncryptor encryptor = new StreamEncryptor(security);
	
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(header);
		for (int i = 0; i < keys.length; i++)
		{
			String localId = keys[i].getLocalId();
			ZipEntry entry = zip.getEntry(localId);
	
			InputStreamWithSeek in = new ZipEntryInputStream(zip, entry);
	
			final String tempFileName = "$$$importZip";
			File file = File.createTempFile(tempFileName, null);
			file.deleteOnExit();
			FileOutputStream rawOut = new FileOutputStream(file);
	
			StreamFilter filter = copier;
			if(db.mustEncryptLocalData() && MartusUtilities.doesPacketNeedLocalEncryption(header, in))
				filter = encryptor;
	
			MartusUtilities.copyStreamWithFilter(in, rawOut, filter);
	
			rawOut.close();
			in.close();
	
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, keys[i].getLocalId());
			DatabaseKey key = header.createKeyWithHeaderStatus(uid);
	
			zipEntries.put(key,file);
		}
		db.importFiles(zipEntries);
	}

	public static void validateIntegrityOfZipFilePublicPackets(String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			Packet.InvalidPacketException,
			IOException,
			Packet.SignatureVerificationException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException
	{
		BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, security);
		DatabaseKey[] keys = bhp.getPublicPacketKeys();
		Vector localIds = new Vector();
		for (int i = 0; i < keys.length; i++)
			localIds.add(keys[i].getLocalId());
	
		//TODO validate Header Packet matches other packets
		Enumeration entries = zip.entries();
		if(!entries.hasMoreElements())
		{
			throw new Packet.InvalidPacketException("Empty zip file");
		}
	
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
	
			if(entry.isDirectory())
			{
				throw new Packet.InvalidPacketException("Directory entry");
			}
	
			if(entry.getName().startsWith(".."))
			{
				throw new Packet.InvalidPacketException("Relative path in name");
			}
	
			if(entry.getName().indexOf("\\") >= 0 ||
				entry.getName().indexOf("/") >= 0 )
			{
				throw new Packet.InvalidPacketException("Path in name");
			}
	
			String thisLocalId = entry.getName();
			if(!localIds.contains(thisLocalId))
				throw new IOException("Extra packet");
			localIds.remove(thisLocalId);
			InputStreamWithSeek in = new ZipEntryInputStream(zip, entry);
			Packet.validateXml(in, authorAccountId, entry.getName(), null, security);
		}
	
		if(localIds.size() > 0)
			throw new IOException("Missing packets");
	}

	public static void validateIntegrityOfZipFilePackets(String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			Packet.InvalidPacketException,
			IOException,
			Packet.SignatureVerificationException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException
	{
		BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, security);
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
		Vector localIds = new Vector();
		for (int i = 0; i < keys.length; i++)
			localIds.add(keys[i].getLocalId());
	
		//TODO validate Header Packet matches other packets
		Enumeration entries = zip.entries();
		if(!entries.hasMoreElements())
		{
			throw new Packet.InvalidPacketException("Empty zip file");
		}
	
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
	
			if(entry.isDirectory())
			{
				throw new Packet.InvalidPacketException("Directory entry");
			}
	
			if(entry.getName().startsWith(".."))
			{
				throw new Packet.InvalidPacketException("Relative path in name");
			}
	
			if(entry.getName().indexOf("\\") >= 0 ||
				entry.getName().indexOf("/") >= 0 )
			{
				throw new Packet.InvalidPacketException("Path in name");
			}
	
			String thisLocalId = entry.getName();
			if(!localIds.contains(thisLocalId))
				throw new IOException("Extra packet");
			localIds.remove(thisLocalId);
			InputStreamWithSeek in = new ZipEntryInputStream(zip, entry);
			Packet.validateXml(in, authorAccountId, entry.getName(), null, security);
		}
	
		if(localIds.size() > 0)
			throw new IOException("Missing packets");
	}

	public static int retrieveBulletinZipToStream(UniversalId uid, OutputStream outputStream,
			int chunkSize, BulletinRetrieverGatewayInterface gateway, MartusCrypto security,
			ProgressMeterInterface progressMeter)
		throws
			MartusCrypto.MartusSignatureException,
			MartusUtilities.ServerErrorException,
			IOException,
			Base64.InvalidBase64Exception
	{
		int masterTotalSize = 0;
		int totalSize = 0;
		int chunkOffset = 0;
		String lastResponse = "";
		if(progressMeter != null)
			progressMeter.updateProgressMeter(0, 1);
		while(!lastResponse.equals(NetworkInterfaceConstants.OK))
		{
			NetworkResponse response = gateway.getBulletinChunk(security,
								uid.getAccountId(), uid.getLocalId(), chunkOffset, chunkSize);
	
			lastResponse = response.getResultCode();
			if(!lastResponse.equals(NetworkInterfaceConstants.OK) &&
				!lastResponse.equals(NetworkInterfaceConstants.CHUNK_OK))
			{
				//System.out.println((String)result.get(0));
				throw new MartusUtilities.ServerErrorException("result=" + lastResponse);
			}
	
			Vector result = response.getResultVector();
			totalSize = ((Integer)result.get(0)).intValue();
			if(masterTotalSize == 0)
				masterTotalSize = totalSize;
	
			if(totalSize != masterTotalSize)
				throw new MartusUtilities.ServerErrorException("totalSize not consistent");
			if(totalSize < 0)
				throw new MartusUtilities.ServerErrorException("totalSize negative");
	
			int thisChunkSize = ((Integer)result.get(1)).intValue();
			if(thisChunkSize < 0 || thisChunkSize > totalSize - chunkOffset)
				throw new MartusUtilities.ServerErrorException("chunkSize out of range: " + thisChunkSize);
	
			// TODO: validate that length of data == chunkSize that was returned
			String data = (String)result.get(2);
			StringReader reader = new StringReader(data);
	
			Base64.decode(reader, outputStream);
			chunkOffset += thisChunkSize;
			if(progressMeter != null)
			{
				if(progressMeter.shouldExit())
					break;
				progressMeter.updateProgressMeter(chunkOffset, masterTotalSize);
			}
		}
		if(progressMeter != null)
			progressMeter.updateProgressMeter(chunkOffset, masterTotalSize);
		return masterTotalSize;
	}
}
