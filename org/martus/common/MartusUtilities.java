package org.martus.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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
	public static class FileVerificationException extends Exception {}
	public static class FileSigningException extends Exception {}
	
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
					signer.signatureDigestBytes(bytesToSign);
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
	
	public static byte[] createSignatureFromFile(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException
	{
			FileInputStream in = new FileInputStream(fileToSign);
			byte[] signature = signer.createSignature(in);
			in.close();
			return signature;
	}
	
	public static File createSignatureFileFromFile(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException
	{		
		String sigPath = fileToSign.getAbsolutePath();
		File newSigFile = new File(sigPath + ".sig.new");
		File existingSig = new File(sigPath + ".sig");
		
		if( newSigFile.exists() )
			newSigFile.delete();

		byte[] signature = createSignatureFromFile(fileToSign, signer);

		FileOutputStream out = new FileOutputStream(newSigFile);
		out.write(signature);
		out.close();

		if(existingSig.exists() )
		{
			existingSig.delete();
		}
		
		newSigFile.renameTo(existingSig);
		
		return existingSig;
	}
	
	public static void verifyFileAndSignature(File fileToVerify, File signatureFile, MartusSecurity verifier)
		throws FileVerificationException
	{
			try
			{
				byte[] signature = new byte[(int) signatureFile.length()];
				FileInputStream inSignature = new FileInputStream(signatureFile);
				inSignature.read(signature);
				inSignature.close();
				
				FileInputStream inData = new FileInputStream(fileToVerify);
				if( !verifier.isSignatureValid( verifier.getPublicKeyString(), inData, signature) )
					throw new FileVerificationException();

				inData.close();
			}
			catch(Exception e)
			{
				throw new FileVerificationException();
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
			validateIntegrityOfZipFilePackets(db, headerKey.getAccountId(), zip, security);
			zip.close();
		}
		catch (InvalidPacketException e)
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

		keys[next++] = createKeyWithHeaderStatus(bhp, dataUid);
		keys[next++] = createKeyWithHeaderStatus(bhp, privateDataUid);
		for(int i=0; i < publicAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, publicAttachmentIds[i]);
			keys[next++] = createKeyWithHeaderStatus(bhp, uid);
		}
		for(int i=0; i < privateAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, privateAttachmentIds[i]);
			keys[next++] = createKeyWithHeaderStatus(bhp, uid);
		}
		keys[next++] = createKeyWithHeaderStatus(bhp, bhp.getUniversalId());
		
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
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		Packet.WrongAccountException,
		MartusCrypto.DecryptionException
	{
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		if(authorAccountId == null)
			authorAccountId = header.getAccountId();
			
		validateIntegrityOfZipFilePackets(db, authorAccountId, zip, security);
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

	public static void validateIntegrityOfZipFilePackets(Database db, String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			InvalidPacketException,
			IOException,
			SignatureVerificationException,
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

	public static DatabaseKey createKeyWithHeaderStatus(BulletinHeaderPacket header, UniversalId uid) 
	{
		if(header.getStatus().equals(BulletinConstants.STATUSDRAFT))
			return DatabaseKey.createDraftKey(uid);
		else
			return DatabaseKey.createSealedKey(uid);
	}

	public static void deleteBulletinFromDatabase(BulletinHeaderPacket bhp, Database db, MartusCrypto crypto)
		throws
			IOException,
			MartusCrypto.CryptoException,
			UnsupportedEncodingException,
			Packet.InvalidPacketException,
			Packet.WrongPacketTypeException,
			Packet.SignatureVerificationException,
			MartusCrypto.DecryptionException,
			MartusCrypto.NoKeyPairException
	{
		DatabaseKey[] keys = MartusUtilities.getAllPacketKeys(bhp);
		for (int i = 0; i < keys.length; i++)
		{
			db.discardRecord(keys[i]);
		}
	}
}
