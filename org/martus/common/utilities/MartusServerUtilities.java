/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002,2003, Beneficent
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

package org.martus.common.utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.crypto.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.Base64;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class MartusServerUtilities
{
	public static BulletinHeaderPacket saveZipFileToDatabase(Database db, String authorAccountId, File zipFile, MartusCrypto verifier)
		throws
			ZipException,
			IOException,
			Database.RecordHiddenException,
			Packet.InvalidPacketException,
			Packet.SignatureVerificationException,
			SealedPacketExistsException,
			DuplicatePacketException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException
	{
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(zipFile);
			BulletinHeaderPacket header = MartusServerUtilities.validateZipFilePacketsForServerImport(db, authorAccountId, zip, verifier);
			BulletinZipUtilities.importBulletinPacketsFromZipFileToDatabase(db, authorAccountId, zip, verifier);
			return header;
		}
		finally
		{
			if(zip != null)
				zip.close();
		}
	}

	public static BulletinHeaderPacket validateZipFilePacketsForServerImport(Database db, String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			Packet.InvalidPacketException,
			IOException,
			Packet.SignatureVerificationException,
			SealedPacketExistsException,
			DuplicatePacketException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException 
	{
		BulletinZipUtilities.validateIntegrityOfZipFilePackets(authorAccountId, zip, security);

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
				DatabaseKey newKey = header.createKeyWithHeaderStatus(uid);
				if(newKey.isDraft())
					throw new SealedPacketExistsException(entry.getName());
				else
					throw new DuplicatePacketException(entry.getName());
			}
		}
		
		return header;
	}

	public static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, String passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}

	public static File getSignatureDirectoryForFile(File originalFile)
	{
		return new File(originalFile.getParent() + File.separatorChar + MARTUS_SIGNATURE_FILE_DIRECTORY_NAME);
	}
	
	public static Date getDateOfSignatureFile(File signatureFile)
		throws IOException
	{
		UnicodeReader reader = new UnicodeReader(signatureFile);
		String identifier = reader.readLine();
		String timestampDate = reader.readLine();
		reader.close();
		
		if(identifier.compareTo(MARTUS_SIGNATURE_FILE_IDENTIFIER) != 0)
		{
			return null;
		}
		
		SimpleDateFormat formatDate = new SimpleDateFormat(MARTUS_SIGNATURE_FILE_DATE_FORMAT);
		formatDate.setCalendar(Calendar.getInstance());
		
		Date date;
		try
		{
			date = formatDate.parse(timestampDate);
		}
		catch (ParseException e)
		{
			return null;
		}
		
		return date;
	}
	
	public static File getLatestSignatureFileFromFile(File originalFile)
		throws IOException, ParseException, MartusSignatureFileDoesntExistsException
	{
		Vector signatureFiles =  getSignaturesForFile(originalFile);
		
		if(signatureFiles.size() == 0)
		{
			throw new MartusSignatureFileDoesntExistsException();
		}
		
		Date latestSigDate = null;
		File latestSignatureFile = null;
		
		for(int x = 0; x < signatureFiles.size(); x++)
		{
			File nextSignatureFile = (File) signatureFiles.get(x);

			Date nextSigDate = getDateOfSignatureFile(nextSignatureFile);
			if(isDateLatest(nextSigDate, latestSigDate))
			{
				latestSigDate = nextSigDate;
				latestSignatureFile = nextSignatureFile;
			}
		}
		
		if(latestSignatureFile == null)
		{
			throw new MartusSignatureFileDoesntExistsException();
		}
		 
		return latestSignatureFile;
	}

	public static boolean isMatchingSigFile(File originalFile, File nextSignatureFile)
	{
		String orginalFilename = originalFile.getName();
		String nextFilename = nextSignatureFile.getName();
		
		boolean isMatchingSigFile = nextFilename.endsWith(".sig") && nextFilename.startsWith(orginalFilename);
		
		return isMatchingSigFile;
	}
	
	public static boolean isDateLatest(Date nextSigDate, Date latestSigDate)
	{
		if(nextSigDate == null)
		{
			return false;
		}
		
		if(latestSigDate == null)
		{
			return true;
		}

		return nextSigDate.after(latestSigDate);
	}

	public synchronized static File createSignatureFileFromFileOnServer(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException, InterruptedException, MartusSignatureFileAlreadyExistsException
	{
		Thread.sleep(1000);
		String dateStamp = createTimeStamp();
		
		File sigDir = getSignatureDirectoryForFile(fileToSign);
		File signatureFile = new File(sigDir.getPath() + File.separatorChar + fileToSign.getName() + "." + dateStamp + ".sig");
		
		if(signatureFile.exists() )
		{
			throw new MartusSignatureFileAlreadyExistsException();
		}

		if(! sigDir.exists())
		{
			sigDir.mkdir();
		}
		
		writeSignatureFileWithDatestamp(signatureFile, dateStamp, fileToSign, signer);


		return signatureFile;
	}

	public static String createTimeStamp()
	{
		long millisSince1970 = System.currentTimeMillis();
		return getFormattedTimeStamp(millisSince1970);
	}

	public static String getFormattedTimeStamp(long millisSince1970)
	{
		Timestamp stamp = new Timestamp(millisSince1970);
		SimpleDateFormat formatDate = new SimpleDateFormat(MARTUS_SIGNATURE_FILE_DATE_FORMAT);
		String dateStamp = formatDate.format(stamp);
		return dateStamp;
	}
	
	public synchronized static void writeSignatureFileWithDatestamp(File signatureFile, String date, File fileToSign, MartusCrypto signer)
	throws IOException, MartusSignatureException
	{	
		long filesize = fileToSign.length();
		long lineCount = getLineCountForFile(fileToSign);
		byte[] signature = MartusUtilities.createSignatureFromFile(fileToSign, signer);
		String sigString = Base64.encode(signature);

		UnicodeWriter writer = new UnicodeWriter(signatureFile);
		try
		{
			writer.writeln(MARTUS_SIGNATURE_FILE_IDENTIFIER);
			writer.writeln(date);
			writer.writeln(Long.toString(filesize));
			writer.writeln(Long.toString(lineCount));
			writer.writeln(signer.getPublicKeyString());
			writer.writeln(sigString);
			writer.flush();
		}
		finally
		{
			writer.close();
		}
	}
	
	static long getLineCountForFile(File file)
		throws IOException
	{
		LineNumberReader in = new LineNumberReader(new FileReader(file));
		long numLines = 0;
		while(in.readLine() != null)
			 ;
		 numLines = in.getLineNumber();
		 in.close();
		 
		return numLines;
		
	}	
	
	public static void verifyFileAndSignatureOnServer(File fileToVerify, File signatureFile, MartusCrypto verifier, String accountId)
		throws FileVerificationException
	{
		FileInputStream inData = null;
		try
		{
			UnicodeReader reader = new UnicodeReader(signatureFile);
			// get Identifier
			reader.readLine();
			// get signature date
			reader.readLine();
			long filesize = Long.parseLong(reader.readLine());
			// get lineCount
			reader.readLine();
			String key = reader.readLine();
			String signature = reader.readLine();
			reader.close();
			
			long verifyFileSize = fileToVerify.length();
			if(filesize != verifyFileSize)
				throw new FileVerificationException();

			if(!key.equals(accountId))
				throw new FileVerificationException();

			inData = new FileInputStream(fileToVerify);
			if( !verifier.isValidSignatureOfStream(key, inData, Base64.decode(signature)) )
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
	
	public static Vector getSignaturesForFile(File originalFile)
	{
		File sigDir = getSignatureDirectoryForFile(originalFile);
		
		File[] filesAvailable = sigDir.listFiles();
		Vector signatureFiles = new Vector();
		
		if(filesAvailable != null)
		{
			for (int i = 0; i < filesAvailable.length; i++)
			{
				File file = filesAvailable[i];
				if(isMatchingSigFile(originalFile, file))
				{
					signatureFiles.add(file);
				}
			}
		}
		return signatureFiles;
	}
	
	public static void deleteSignaturesForFile(File originalFile)
	{
		Vector sigFiles = MartusServerUtilities.getSignaturesForFile(originalFile);

		for (Iterator iter = sigFiles.iterator(); iter.hasNext();)
		{
			File signature = (File) iter.next();
			signature.delete();
		}
		
		File sigDir = MartusServerUtilities.getSignatureDirectoryForFile(originalFile);
		sigDir.delete();
	}
	
	public static byte [] getFileContents(File plainTextFile) throws IOException
	{
		long size = plainTextFile.length();
		
		if(size > MAX_ALLOWED_ENCRYPTED_FILESIZE)
		{
			throw new FileTooLargeException();
		}
		
		byte [] result = new byte[(int) size];
		
		FileInputStream inputStream = new FileInputStream(plainTextFile);
		inputStream.read(result);
		inputStream.close();
		
		return result;
	}

	public static MartusCrypto loadKeyPair(String keyPairFileName, boolean showPrompt)
	{
		File keyPairFile = new File(keyPairFileName);
		if(!keyPairFile.exists())
		{
			System.out.println("Error missing keypair");
			System.exit(3);
		}
		
		if(showPrompt)
		{
			System.out.print("Enter server passphrase:");
			System.out.flush();
		}
	
		try
		{
			UnicodeReader reader = new UnicodeReader(System.in);
			String passphrase = reader.readLine();
			return MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase);
		}
		catch (MartusCrypto.AuthorizationFailedException e)
		{
			System.err.println("Error probably bad passphrase: " + e + "\n");
			System.exit(1);
		}
		catch(Exception e)
		{
			System.err.println("Error loading keypair: " + e + "\n");
			System.exit(3);
		}
		return null;
	}

	public static String createBulletinUploadRecord(String bulletinLocalId, MartusCrypto security) throws MartusCrypto.CreateDigestException
	{
		String timeStamp = MartusServerUtilities.createTimeStamp();
		return createBulletinUploadRecordWithSpecificTimeStamp(
			bulletinLocalId, timeStamp, security);
	}

	public static String createBulletinUploadRecordWithSpecificTimeStamp(
		String bulletinLocalId,
		String timeStamp,
		MartusCrypto security)
		throws CreateDigestException
	{
		byte[] partOfPrivateKey = security.getDigestOfPartOfPrivateKey();
		String stringToDigest = 
				BULLETIN_UPLOAD_RECORD_IDENTIFIER + newline +
				bulletinLocalId + newline +
				timeStamp + newline +
				Base64.encode(partOfPrivateKey) + newline;
		String digest = MartusSecurity.createDigestString(stringToDigest);
		return 
			BULLETIN_UPLOAD_RECORD_IDENTIFIER + newline + 
			bulletinLocalId + newline +
			timeStamp + newline +
			digest + newline;
	}
	
	public static boolean wasBurCreatedByThisCrypto(String burToTest, MartusCrypto security)
	{
		BufferedReader reader = new BufferedReader(new StringReader(burToTest));
		String digestFromTestBur;
		String digestCreatedFromThisCrypto;
		try
		{
			String fileTypeIdentifier = reader.readLine();
			String localId = reader.readLine();
			String timeStamp = reader.readLine(); 
			digestFromTestBur = reader.readLine();

			String stringToDigest = 
					fileTypeIdentifier + newline +
					localId  + newline +
					timeStamp + newline +
					Base64.encode(security.getDigestOfPartOfPrivateKey()) + newline;

			digestCreatedFromThisCrypto = MartusSecurity.createDigestString(stringToDigest);

		}
		catch (Exception e)
		{
			return false;
		}

		return (digestCreatedFromThisCrypto.equals(digestFromTestBur));		
	}


	public static DatabaseKey getBurKey(DatabaseKey key)
	{
		UniversalId burUid = UniversalId.createFromAccountAndLocalId(key.getAccountId(), "BUR-" + key.getLocalId());
		DatabaseKey burKey = new DatabaseKey(burUid);
		
		if(key.isDraft())
			burKey.setDraft();
		return burKey;
	}

	public static void writeSpecificBurToDatabase(Database db, BulletinHeaderPacket bhp, String bur)
		throws IOException, Database.RecordHiddenException
	{
		DatabaseKey headerKey = bhp.createKeyWithHeaderStatus(bhp.getUniversalId());
		DatabaseKey burKey = MartusServerUtilities.getBurKey(headerKey);
		db.writeRecord(burKey, bur);
	}
	
	static public void writeContatctInfo(String accountId, Vector contactInfo, File contactInfoFile) throws IOException
	{
		contactInfoFile.getParentFile().mkdirs();
		FileOutputStream contactFileOutputStream = new FileOutputStream(contactInfoFile);
		DataOutputStream out = new DataOutputStream(contactFileOutputStream);
		out.writeUTF((String)contactInfo.get(0));
		out.writeInt(((Integer)(contactInfo.get(1))).intValue());
		for(int i = 2; i<contactInfo.size(); ++i)
		{
			out.writeUTF((String)contactInfo.get(i));
		}
		out.close();
	}

	static public Vector getContactInfo(File contactFile) throws FileNotFoundException, IOException
	{
		Vector contactInfo = new Vector();
		FileInputStream contactFileInputStream = new FileInputStream(contactFile);
		DataInputStream in = new DataInputStream(contactFileInputStream);
	
		contactInfo.add(in.readUTF());
		int inputDataCount = in.readInt();
		contactInfo.add(new Integer(inputDataCount));
		for(int i = 0; i < inputDataCount + 1; ++i)
		{
			contactInfo.add(in.readUTF());
		}			
		in.close();
		return contactInfo;
	}

	public static class MartusSignatureFileAlreadyExistsException extends Exception {}
	public static class MartusSignatureFileDoesntExistsException extends Exception {}
	public static class FileTooLargeException extends IOException {}

	public static class DuplicatePacketException extends Exception
	{
		public DuplicatePacketException(String message)
		{
			super(message);
		}
	}
	
	public static class SealedPacketExistsException extends Exception
	{
		public SealedPacketExistsException(String message)
		{
			super(message);
		}
	}
	
	private static final String MARTUS_SIGNATURE_FILE_DATE_FORMAT = "yyyyMMdd-HHmmss";
	private static final String MARTUS_SIGNATURE_FILE_IDENTIFIER = "Martus Signature File";
	private static final String MARTUS_SIGNATURE_FILE_DIRECTORY_NAME = "signatures";
	private static final int MAX_ALLOWED_ENCRYPTED_FILESIZE = 1000*1000;

	private static final String BULLETIN_UPLOAD_RECORD_IDENTIFIER = "Martus Bulletin Upload Record 1.0";
	final static String newline = "\n";
	
}
