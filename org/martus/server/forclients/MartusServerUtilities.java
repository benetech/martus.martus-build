package org.martus.server.forclients;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.Packet;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.FileVerificationException;

public class MartusServerUtilities
{
	public static void saveZipFileToDatabase(Database db, String authorAccountId, File zipFile, MartusCrypto verifier)
		throws
			ZipException,
			IOException,
			Packet.InvalidPacketException,
			Packet.SignatureVerificationException,
			MartusServer.SealedPacketExistsException,
			MartusServer.DuplicatePacketException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException
	{
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(zipFile);
			MartusServerUtilities.validateZipFilePacketsForServerImport(db, authorAccountId, zip, verifier);
			MartusUtilities.importBulletinPacketsFromZipFileToDatabase(db, authorAccountId, zip, verifier);
		}
		finally
		{
			if(zip != null)
				zip.close();
		}
	}

	public static void validateZipFilePacketsForServerImport(Database db, String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			Packet.InvalidPacketException,
			IOException,
			Packet.SignatureVerificationException,
			MartusServer.SealedPacketExistsException,
			MartusServer.DuplicatePacketException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException 
	{
		MartusUtilities.validateIntegrityOfZipFilePackets(authorAccountId, zip, security);

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
					throw new MartusServer.SealedPacketExistsException(entry.getName());
				else
					throw new MartusServer.DuplicatePacketException(entry.getName());
			}
		}
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

	public static File getPathToSignatureDirForFile(File originalFile)
	{
		return new File(originalFile.getParentFile().getPath() + File.separatorChar + "signature");
	}
	
	public static Date getDateForSignatureFile(File signatureFile)
		throws IOException
	{
		UnicodeReader reader = new UnicodeReader(signatureFile);
		String timestampDate = reader.readLine();
		reader.close();
		
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd-HHmmss");
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
		throws IOException, ParseException
	{
		File sigDir = getPathToSignatureDirForFile(originalFile);
		String [] signatureFilenames =  sigDir.list();
		
		String latestSignatureFilename = null;
		Date latestSigDate = null;
		
		String nextSignatureFilename = null;
		Date nextSigDate = null;
		
		for(int x = 0; x < signatureFilenames.length; x++)
		{
			if(signatureFilenames[x].endsWith(".sig") && signatureFilenames[x].startsWith(originalFile.getName()))
			{
				if(latestSignatureFilename == null)
				{
					latestSignatureFilename = signatureFilenames[x];
					latestSigDate = getDateForSignatureFile(new File(sigDir, latestSignatureFilename));
					if(latestSigDate == null)
					{
						latestSignatureFilename = null;
					}
				}
				else
				{
					nextSignatureFilename = signatureFilenames[x];
					nextSigDate = getDateForSignatureFile(new File(sigDir, nextSignatureFilename));
			
					if(nextSigDate != null && nextSigDate.after(latestSigDate))
					{
						latestSigDate = nextSigDate;
						latestSignatureFilename = nextSignatureFilename;
					}
				}
			}
		}
		 
		return new File(sigDir, latestSignatureFilename);
	}
	
	public static void deleteSignatureFileOnServer(File sigFile)
	{
		if(sigFile.exists() )
		{
			sigFile.delete();
		}
		
		File sigDir = sigFile.getParentFile();
		String [] files = sigDir.list();
		if(files.length == 0)
		{
			sigDir.delete();
		}
	}

	public static File createSignatureFileFromFileOnServer(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException, InterruptedException
	{
		Thread.sleep(1000);
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String tstamp = formatDate.format(stamp);
		
		File sigDir = getPathToSignatureDirForFile(fileToSign);
		File signatureFile = new File(sigDir.getPath() + File.separatorChar + fileToSign.getName() + "." + tstamp + ".sig");
		
		
		File newSigFile = new File(signatureFile.getAbsolutePath() + ".new");

		if( newSigFile.exists() )
			newSigFile.delete();

		long filesize = fileToSign.length();
		long lineCount = getLineCountForFile(fileToSign);
		byte[] signature = MartusUtilities.createSignatureFromFile(fileToSign, signer);
		String sigString = Base64.encode(signature);

		UnicodeWriter writer = new UnicodeWriter(newSigFile);
		writer.writeln(tstamp);
		writer.writeln(Long.toString(filesize));
		writer.writeln(Long.toString(lineCount));
		writer.writeln(signer.getPublicKeyString());
		writer.writeln(sigString);
		writer.flush();
		writer.close();

		if(signatureFile.exists() )
		{
			signatureFile.delete();
		}
		else
		{
			if(! sigDir.exists())
			{
				sigDir.mkdir();
			}
		}

		newSigFile.renameTo(signatureFile);

		return signatureFile;
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
}