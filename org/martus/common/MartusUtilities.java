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

package org.martus.common;

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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.Base64.InvalidBase64Exception;
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

	public static class ServerErrorException extends Exception 
	{
		public ServerErrorException(String message)
		{
			super(message);
		}
		
		public ServerErrorException()
		{
			this("");
		}
	}

	
	public static int getCappedFileLength(File file) throws FileTooLargeException
	{
		long rawLength = file.length();
		if(rawLength >= Integer.MAX_VALUE || rawLength < 0)
			throw new FileTooLargeException();
			
		return (int)rawLength;
	}

	public static String getVersionDate()
	{
		java.lang.Class classToUse = MartusUtilities.class;
		String versionDate = "[?]";
		InputStream versionStream = null;
		String fileVersionInfo = "BuildDate.txt";
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
	
	public static File getSignatureFileFromFile(File originalFile)
	{
		return new File(originalFile.getAbsolutePath() + ".sig");	
	}
	
	public static void deleteInterimFileAndSignature(File tempFile) 
	{
		File tempFileSignature = MartusUtilities.getSignatureFileFromFile(tempFile);
		tempFile.delete();
		tempFileSignature.delete();
	}
	
	public static File createSignatureFileFromFile(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException
	{		
		File newSigFile = new File(fileToSign.getAbsolutePath() + ".sig.new");
		File existingSig = getSignatureFileFromFile(fileToSign);
		
		if( newSigFile.exists() )
			newSigFile.delete();

		byte[] signature = createSignatureFromFile(fileToSign, signer);
		String sigString = Base64.encode(signature);
		
		UnicodeWriter writer = new UnicodeWriter(newSigFile);
		writer.writeln(signer.getPublicKeyString());
		writer.writeln(sigString);
		writer.flush();
		writer.close();

		if(existingSig.exists() )
		{
			existingSig.delete();
		}
		
		newSigFile.renameTo(existingSig);
		
		return existingSig;
	}
	
	public static void verifyFileAndSignature(File fileToVerify, File signatureFile, MartusCrypto verifier, String accountId)
		throws FileVerificationException
	{
		FileInputStream inData = null;
		try
		{
			UnicodeReader reader = new UnicodeReader(signatureFile);
			String key = reader.readLine();
			String signature = reader.readLine();
			reader.close();
			
			if(!key.equals(accountId))
				throw new FileVerificationException();				
			
			inData = new FileInputStream(fileToVerify);
			if( !verifier.isSignatureValid(key, inData, Base64.decode(signature)) )
				throw new FileVerificationException();
		}
		catch(Exception e)
		{
			throw new FileVerificationException();
		}
		finally
		{
			try
			{
				if(inData != null)
					inData.close();
			}
			catch (IOException ignoredException)
			{
			}
		}
	}
	
	public static void exportPublicKey(MartusCrypto security, File outputfile)
		throws MartusSignatureException, InvalidBase64Exception, IOException
	{
		ByteArrayInputStream in = null;
		UnicodeWriter writer = null;		
		try
		{
			String publicKeyString = security.getPublicKeyString();
			byte[] publicKeyBytes = Base64.decode(publicKeyString);
			in = new ByteArrayInputStream(publicKeyBytes);
			byte[] sigBytes = security.createSignature(in);
			
			writer = new UnicodeWriter(outputfile);
			writer.writeln(publicKeyString);
			writer.writeln(Base64.encode(sigBytes));
		}
		finally
		{
			if( in != null )
			{
				try
				{
					in.close();
				}
				catch (IOException ignored)
				{
					;
				}
			}
			
			if(writer != null)
			{
				try
				{
					writer.close();
				}
				catch(IOException ignored)
				{
					;
				}
			}
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
			validateIntegrityOfZipFilePackets(headerKey.getAccountId(), zip, security);
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
	
	public static Vector getRetrieveBulletinSummaryTags()
	{
		Vector tags = new Vector();
		tags.add(NetworkInterfaceConstants.TAG_BULLETIN_SIZE);
		return tags;	
	}
	
	public static DatabaseKey[] getPublicPacketKeys(BulletinHeaderPacket bhp)
	{
		String accountId = bhp.getAccountId();
		String[] publicAttachmentIds = bhp.getPublicAttachmentIds();
		
		int corePacketCount = 3;
		int publicAttachmentCount = publicAttachmentIds.length;
		int totalPacketCount = corePacketCount + publicAttachmentCount;
		DatabaseKey[] keys = new DatabaseKey[totalPacketCount];
		
		int next = 0;
		UniversalId dataUid = UniversalId.createFromAccountAndLocalId(accountId, bhp.getFieldDataPacketId());
		keys[next++] = createKeyWithHeaderStatus(bhp, dataUid);
		
		keys[next++] = createKeyWithHeaderStatus(bhp, dataUid);
		for(int i=0; i < publicAttachmentIds.length; ++i)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, publicAttachmentIds[i]);
			keys[next++] = createKeyWithHeaderStatus(bhp, uid);
		}
		keys[next++] = createKeyWithHeaderStatus(bhp, bhp.getUniversalId());
		
		return keys;
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
	
	public static int getBulletinSize(Database db, BulletinHeaderPacket bhp)
	{
		int size = 0;
		DatabaseKey[] bulletinPacketKeys  = getAllPacketKeys(bhp);
		for(int i = 0 ; i < bulletinPacketKeys.length ; ++i)
		{
			try 
			{
				size += db.getRecordSize(bulletinPacketKeys[i]);
			} 
			catch (IOException e) 
			{
				System.out.println("MartusUtilities:bulletinPacketKeys error= " + e);
				return 0;
			}
		}
		return size;
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
			
		validateIntegrityOfZipFilePackets(authorAccountId, zip, security);
		deleteDraftBulletinPackets(db, header.getUniversalId(), security);
		
		HashMap zipEntries = new HashMap();
		StreamCopier copier = new StreamCopier();
		StreamEncryptor encryptor = new StreamEncryptor(security);

		DatabaseKey[] keys = getAllPacketKeys(header);
		for (int i = 0; i < keys.length; i++)
		{
			String localId = keys[i].getLocalId();
			ZipEntry entry = zip.getEntry(localId);
			
			InputStreamWithSeek in = new ZipEntryInputStream(zip, entry);

			final String tempFileName = "$$$importZip";
			File file = File.createTempFile(tempFileName, null);
			FileOutputStream rawOut = new FileOutputStream(file);

			StreamFilter filter = copier;
			if(db.mustEncryptLocalData() && doesPacketNeedLocalEncryption(header, in))
				filter = encryptor;
				
			copyStreamWithFilter(in, rawOut, filter);

			rawOut.close();
			in.close();
		
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, keys[i].getLocalId());
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

	public static void validateIntegrityOfZipFilePackets(String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			InvalidPacketException,
			IOException,
			SignatureVerificationException,
			WrongAccountException,
			DecryptionException 
	{
		BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, security);
		DatabaseKey[] keys = getAllPacketKeys(bhp);
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

	public static int retrieveBulletinZipToStream(UniversalId uid, OutputStream outputStream, 
			int chunkSize, BulletinRetrieverGatewayInterface gateway, MartusCrypto security, 
			ProgressMeterInterface progressMeter, String progressTag)
		throws
			MartusCrypto.MartusSignatureException,
			ServerErrorException,
			IOException,
			Base64.InvalidBase64Exception
	{
		int masterTotalSize = 0;
		int totalSize = 0;
		int chunkOffset = 0;
		String lastResponse = "";
		if(progressMeter != null)
			progressMeter.updateProgressMeter(progressTag, 0, 1);	
		while(!lastResponse.equals(NetworkInterfaceConstants.OK))
		{
			NetworkResponse response = gateway.getBulletinChunk(security, 
								uid.getAccountId(), uid.getLocalId(), chunkOffset, chunkSize);
								
			lastResponse = response.getResultCode();
			if(!lastResponse.equals(NetworkInterfaceConstants.OK) &&
				!lastResponse.equals(NetworkInterfaceConstants.CHUNK_OK))
			{
				//System.out.println((String)result.get(0));
				throw new ServerErrorException("result=" + lastResponse);
			}
			
			Vector result = response.getResultVector();
			totalSize = ((Integer)result.get(0)).intValue();
			if(masterTotalSize == 0)
				masterTotalSize = totalSize;
				
			if(totalSize != masterTotalSize)
				throw new ServerErrorException("totalSize not consistent");
			if(totalSize < 0)
				throw new ServerErrorException("totalSize negative");
				
			int thisChunkSize = ((Integer)result.get(1)).intValue();
			if(thisChunkSize < 0 || thisChunkSize > totalSize - chunkOffset)
				throw new ServerErrorException("chunkSize out of range");
			
			// TODO: validate that length of data == chunkSize that was returned
			String data = (String)result.get(2);
			StringReader reader = new StringReader(data);
		
			Base64.decode(reader, outputStream);
			chunkOffset += thisChunkSize;
			if(progressMeter != null)
			{
				if(progressMeter.shouldExit())
					break;					
				progressMeter.updateProgressMeter(progressTag, chunkOffset, masterTotalSize);	
			}
		}
		if(progressMeter != null)
			progressMeter.updateProgressMeter(progressTag, chunkOffset, masterTotalSize);	
		return masterTotalSize;
	}

	public static String getXmlEncoded(String text)
	{
		StringBuffer buf = new StringBuffer(text);
		for(int i = 0; i < buf.length(); ++i)
		{
			char c = buf.charAt(i);
			if(c == '&')
			{
				buf.replace(i, i+1, "&amp;");
			}
			else if(c == '<')
			{
				buf.replace(i, i+1, "&lt;");
			}
			else if(c == '>')
			{
				buf.replace(i, i+1, "&gt;");
			}
		}
		return new String(buf);
	}

	public static void copyStreamWithFilter(InputStream in, OutputStream rawOut, 
									StreamFilter filter) throws IOException
	{
		BufferedOutputStream out = (new BufferedOutputStream(rawOut));
		try
		{
			filter.copyStream(in, out);
		}
		finally
		{
			out.flush();
			rawOut.flush();
			
			// TODO: We really want to do a sync here, so the server does not 
			// have to journal all written data. But under Windows, the unit 
			// tests pass, but the actual app throws an exception here. We 
			// can't figure out why.
			//rawOut.getFD().sync();
			out.close();
		}
	}

	public static boolean doesPacketNeedLocalEncryption(BulletinHeaderPacket bhp, InputStreamWithSeek fdpInputStream) throws IOException
	{
		if(bhp.hasAllPrivateFlag() && bhp.isAllPrivate())
			return false;

		int firstByteIsZeroIfEncrypted = fdpInputStream.read();
		fdpInputStream.seek(0);
		if(firstByteIsZeroIfEncrypted == 0)
			return false;

		final String encryptedTag = MartusXml.getTagStart(MartusXml.EncryptedFlagElementName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fdpInputStream));
		String thisLine = null;
		while( (thisLine = reader.readLine()) != null)
		{
			if(thisLine.indexOf(encryptedTag) >= 0)
			{
				fdpInputStream.seek(0);
				return false;
			}
		}
		
		fdpInputStream.seek(0);
		return true;
	}

	public static boolean isStringInArray(String[] array, String lookFor) 
	{
		for(int newIndex = 0; newIndex < array.length; ++newIndex)
		{
			if(lookFor.equals(array[newIndex]))
				return true;
		}
		
		return false;
	}
}
