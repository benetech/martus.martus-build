package org.martus.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.Database.AccountVisitor;
import org.martus.common.Database.PacketVisitor;
import org.martus.common.MartusCrypto.CryptoException;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusCrypto.NoKeyPairException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;
import org.martus.common.Packet.WrongAccountException;
import org.martus.common.Packet.WrongPacketTypeException;

public class MartusUtilities 
{
	public static class FileTooLargeException extends Exception {}
	public static class DuplicatePacketException extends Exception
	{
		DuplicatePacketException(String message)
		{
			super(message);
		}
	}
	
	public static class SealedPacketExistsException extends Exception
	{
		SealedPacketExistsException(String message)
		{
			super(message);
		}
	}

	
	public static int getCappedFileLength(File file) throws FileTooLargeException
	{
		long rawLength = file.length();
		if(rawLength >= Integer.MAX_VALUE || rawLength < 0)
			throw new FileTooLargeException();
			
		return (int)rawLength;
	}

	public static String getVersionDate(java.lang.Class classToUse)
	{
		String versionDate = "";
		InputStream versionStream = null;
		String fileVersionInfo = "version.txt";
		versionStream = classToUse.getResourceAsStream(fileVersionInfo);
		if(versionStream != null)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(versionStream));
			try 
			{
				versionDate = reader.readLine();
				reader.close();
			} 
			catch(IOException ifNoDateAvailableLeaveItBlank) 
			{
			}
		}
		return versionDate;
	}

	public static synchronized String createSignature(String stringToSign, MartusCrypto security)
		throws UnsupportedEncodingException, MartusSignatureException 
	{
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = security.createSignature(new ByteArrayInputStream(bytesToSign));
		String signature = Base64.encode(sigBytes);
		return signature;
	}

	public static boolean verifySignature(Vector dataToSign, MartusCrypto verifier, String signedBy, String sig)
	{
		try
		{
			synchronized(verifier)
			{
				verifier.signatureInitializeVerify(signedBy);
				for(int element = 0; element < dataToSign.size(); ++element)
				{
					String thisElement = dataToSign.get(element).toString();
					byte[] bytesToSign = thisElement.getBytes("UTF-8");
					//TODO: might want to optimize this for speed
					for(int b = 0; b < bytesToSign.length; ++b)
						verifier.signatureDigestByte(bytesToSign[b]);
					verifier.signatureDigestByte((byte)0);
				}
				byte[] sigBytes = Base64.decode(sig);
				return verifier.signatureIsValid(sigBytes);
			}
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static String sign(Vector dataToSign, MartusCrypto signer) throws 
			MartusCrypto.MartusSignatureException
	{
		try
		{
			synchronized(signer)
			{
				signer.signatureInitializeSign();
				for(int element = 0; element < dataToSign.size(); ++element)
				{
					String thisElement = dataToSign.get(element).toString();
					byte[] bytesToSign = thisElement.getBytes("UTF-8");
					//TODO: might want to optimize this for speed
					for(int b = 0; b < bytesToSign.length; ++b)
						signer.signatureDigestByte(bytesToSign[b]);
					signer.signatureDigestByte((byte)0);
				}
				return Base64.encode(signer.signatureGet());
			}
		}
		catch(Exception e)
		{
			// TODO: Needs tests!
			e.printStackTrace();
			System.out.println("ServerProxy.sign: " + e);
			throw new MartusCrypto.MartusSignatureException();
		}
	}

	public static String formatPublicCode(String publicCode) 
	{
		String formatted = "";
		while(publicCode.length() > 0)
		{
			String portion = publicCode.substring(0, 4);
			formatted += portion + "." ;
			publicCode = publicCode.substring(4);
		}
		if(formatted.endsWith("."))
			formatted = formatted.substring(0,formatted.length()-1);
		return formatted;
	}
	
	public static String computePublicCode(String publicKeyString) throws 
		Base64.InvalidBase64Exception
	{
		String digest = null;
		try
		{
			digest = MartusSecurity.createDigestString(publicKeyString);
		}
		catch(Exception e)
		{
			System.out.println("MartusApp.computePublicCode: " + e);
			return "";
		}
		
		final int codeSizeChars = 20;
		char[] buf = new char[codeSizeChars];
		int dest = 0;
		for(int i = 0; i < codeSizeChars/2; ++i)
		{
			int value = Base64.getValue(digest.charAt(i));
			int high = value >> 3;
			int low = value & 0x07;
			
			buf[dest++] = (char)('1' + high);
			buf[dest++] = (char)('1' + low);
		}
		return new String(buf);
	}

	public static void exportBulletinPacketsFromDatabaseToZipFile(Database db, DatabaseKey headerKey, File destZipFile, MartusCrypto security) throws
			IOException,
			CryptoException,
			UnsupportedEncodingException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException,
			NoKeyPairException,
			FileNotFoundException 
	{
		String headerXml = db.readRecord(headerKey, security);
		byte[] headerBytes = headerXml.getBytes("UTF-8");
		
		ByteArrayInputStreamWithSeek headerIn = new ByteArrayInputStreamWithSeek(headerBytes);
		BulletinHeaderPacket bhp = new BulletinHeaderPacket("");
		
		MartusCrypto doNotCheckSigDuringDownload = null;
		bhp.loadFromXml(headerIn, doNotCheckSigDuringDownload);
		DatabaseKey[] packetKeys = getAllPacketKeys(bhp);

		FileOutputStream outputStream = new FileOutputStream(destZipFile);
		extractPacketsToZipStream(headerKey.getAccountId(), db, packetKeys, outputStream, security);

		// TODO: REMOVE THIS! IT IS ONLY FOR DEBUGGING! SLOW SLOW SLOW!		
		try 
		{
			ZipFile zip = new ZipFile(destZipFile);
			validateZipFilePackets(db, headerKey.getAccountId(), zip, security);
			zip.close();
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

		keys[next++] = new DatabaseKey(dataUid);
		keys[next++] = new DatabaseKey(privateDataUid);
		for(int i=0; i < publicAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, publicAttachmentIds[i]);
			keys[next++] = new DatabaseKey(uid);
		}
		for(int i=0; i < privateAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, privateAttachmentIds[i]);
			keys[next++] = new DatabaseKey(uid);
		}
		keys[next++] = new DatabaseKey(bhp.getUniversalId());
		
		boolean isDraft = (bhp.getStatus().equals(BulletinConstants.STATUSDRAFT));
		for(int i=0; i < keys.length; ++i)
		{
			if(isDraft)
				keys[i].setDraft();
			else
				keys[i].setSealed();
		}
		return keys;
	}
	
	public static void extractPacketsToZipStream(String clientId, Database db, DatabaseKey[] packetKeys, OutputStream outputStream, MartusCrypto security) throws 
		IOException, 
		UnsupportedEncodingException 
	{
		ZipOutputStream zipOut = new ZipOutputStream(outputStream);
		
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
			}
		} 
		catch(CryptoException e) 
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
		DuplicatePacketException,
		SealedPacketExistsException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		Packet.WrongAccountException,
		MartusCrypto.DecryptionException
	{
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		if(authorAccountId == null)
			authorAccountId = header.getAccountId();
			
		validateZipFilePacketsForImport(db, authorAccountId, zip, security);
		deleteDraftBulletinPackets(db, header.getUniversalId(), security);
		
		HashMap zipEntries = new HashMap();
		
		final String tempFileName = "$$$importZip";
		
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			InputStream in = new BufferedInputStream(zip.getInputStream(entry));

			File file = File.createTempFile(tempFileName, null);

			FileOutputStream rawOut = new FileOutputStream(file);
			BufferedOutputStream out = new BufferedOutputStream(rawOut);
			
			byte bytes[] = new byte[MartusConstants.streamBufferCopySize];
			int nBytesRead;
			while(in.available() > 0)
			{
				nBytesRead = in.read(bytes);
				out.write(bytes, 0, nBytesRead);
			}
			out.flush();
			out.close();
		
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, entry.getName());
			DatabaseKey key = MartusUtilities.createKeyWithHeaderStatus(header, uid);

			zipEntries.put(key,file);
		}
		db.importFiles(zipEntries);
	}

	private static void deleteDraftBulletinPackets(Database db, UniversalId bulletinUid, MartusCrypto security) throws
		IOException
	{
		DatabaseKey headerKey = DatabaseKey.createDraftKey(bulletinUid);
		if(!db.doesRecordExist(headerKey))
			return;
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(bulletinUid);
		try 
		{
			InputStreamWithSeek in = db.openInputStream(headerKey, security);
			bhp.loadFromXml(in, security);
		} 
		catch (Exception e) 
		{
			throw new IOException(e.toString());
		}
		
		String accountId = bhp.getAccountId();
		deleteDraftPacket(db, accountId, bhp.getLocalId());
		deleteDraftPacket(db, accountId, bhp.getFieldDataPacketId());
		deleteDraftPacket(db, accountId, bhp.getPrivateFieldDataPacketId());
		
		String[] publicAttachmentIds = bhp.getPublicAttachmentIds();
		for(int i = 0; i < publicAttachmentIds.length; ++i)
		{
			deleteDraftPacket(db, accountId, publicAttachmentIds[i]);
		}

		String[] privateAttachmentIds = bhp.getPrivateAttachmentIds();
		for(int i = 0; i < privateAttachmentIds.length; ++i)
		{
			deleteDraftPacket(db, accountId, privateAttachmentIds[i]);
		}
	}

	private static void deleteDraftPacket(Database db, String accountId, String localId)
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
		DatabaseKey key = DatabaseKey.createDraftKey(uid);
		db.discardRecord(key);
	}

	private static void validateZipFilePacketsForImport(Database db, String authorAccountId, ZipFile zip, MartusCrypto security) throws 
		IOException, 
		DuplicatePacketException,
		SealedPacketExistsException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		Packet.WrongAccountException,
		MartusCrypto.DecryptionException
	{
		validateZipFilePackets(db, authorAccountId, zip, security);
		ensureZipFilePacketsOkToImport(db, authorAccountId, zip, security);
	}

	public static void validateZipFilePackets(Database db, String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			InvalidPacketException,
			IOException,
			SignatureVerificationException,
			SealedPacketExistsException,
			DuplicatePacketException,
			WrongAccountException,
			DecryptionException 
	{
		//TODO validate Header Packet matches other packets
		Enumeration entries = zip.entries();
		if(!entries.hasMoreElements())
		{
			throw new Packet.InvalidPacketException("Empty zip file");
		}

		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			InputStreamWithSeek in = new ZipEntryInputStream(zip, entry);
			Packet.validateXml(in, authorAccountId, entry.getName(), null, security);
		}
	}

	private static void ensureZipFilePacketsOkToImport(Database db, String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			InvalidPacketException,
			IOException,
			SignatureVerificationException,
			SealedPacketExistsException,
			DuplicatePacketException,
			WrongAccountException,
			DecryptionException 
	{
		//TODO validate Header Packet matches other packets
		Enumeration entries = zip.entries();
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, entry.getName());
			DatabaseKey trySealedKey = new DatabaseKey(uid);
			trySealedKey.setSealed();
			if(db.doesRecordExist(trySealedKey))
			{
				DatabaseKey newKey = MartusUtilities.createKeyWithHeaderStatus(header, uid);
				if(newKey.isDraft())
					throw new SealedPacketExistsException(entry.getName());
				else
					throw new DuplicatePacketException(entry.getName());
			}
		}
	}

	public static DatabaseKey createKeyWithHeaderStatus(BulletinHeaderPacket header, UniversalId uid) 
	{
		if(header.getStatus().equals(BulletinConstants.STATUSDRAFT))
			return DatabaseKey.createDraftKey(uid);
		else
			return DatabaseKey.createSealedKey(uid);
	}

}
