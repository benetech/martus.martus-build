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

package org.martus.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.SimpleX509TrustManager;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.Base64;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.StreamFilter;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.Base64.InvalidBase64Exception;

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
		return "#{BUILDDATE}";
	}

	public static byte[] createSignatureFromFile(File fileToSign, MartusCrypto signer)
		throws IOException, MartusSignatureException
	{
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(fileToSign);
			byte[] signature = signer.createSignatureOfStream(in);
			return signature;
		}
		finally
		{
			if(in != null)
				in.close();
		}
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

	public static class InvalidPublicKeyFileException extends Exception {}
	
	public static Vector importServerPublicKeyFromFile(File file, MartusCrypto verifier) throws 
		IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		Vector result = new Vector();

		UnicodeReader reader = new UnicodeReader(file);
		try
		{
			String fileType = reader.readLine();
			String keyType = reader.readLine();
			String publicKey = reader.readLine();
			String signature = reader.readLine();

			if(!fileType.startsWith(PUBLIC_KEY_FILE_IDENTIFIER))
				throw new InvalidPublicKeyFileException();
			if(!keyType.equals(PUBLIC_KEY_TYPE_SERVER))
				throw new InvalidPublicKeyFileException();

			validatePublicInfo(publicKey, signature, verifier);

			result.add(publicKey);
			result.add(signature);
		}
		finally
		{
			reader.close();
		}

		return result;
	}
	
	public static void exportServerPublicKey(MartusCrypto security, File outputfile)
		throws MartusSignatureException, InvalidBase64Exception, IOException
	{
		String publicKeyString = security.getPublicKeyString();
		String sigString = security.getSignatureOfPublicKey();

		UnicodeWriter writer = new UnicodeWriter(outputfile);
		try
		{
			writeServerPublicKey(writer, publicKeyString, sigString);
		}
		finally
		{
			writer.close();
		}
	}

	public static void writeServerPublicKey(UnicodeWriter writer, String publicKeyString, String sigString)
		throws IOException
	{
		writer.writeln(PUBLIC_KEY_FILE_IDENTIFIER + "1.0");
		writer.writeln(PUBLIC_KEY_TYPE_SERVER);
		writer.writeln(publicKeyString);
		writer.writeln(sigString);
	}
	
	public static Vector importClientPublicKeyFromFile(File file) throws IOException
	{
		Vector result = new Vector();

		UnicodeReader reader = new UnicodeReader(file);
		String publicKey = reader.readLine();
		String signature = reader.readLine();
		reader.close();

		result.add(publicKey);
		result.add(signature);

		return result;
	}

	public static void exportClientPublicKey(MartusCrypto security, File outputfile)
		throws MartusSignatureException, InvalidBase64Exception, IOException
	{
		String publicKeyString = security.getPublicKeyString();
		String sigString = security.getSignatureOfPublicKey();

		UnicodeWriter writer = new UnicodeWriter(outputfile);
		try
		{
			writer.writeln(publicKeyString);
			writer.writeln(sigString);
		}
		finally
		{
			writer.close();
		}
	}

	public static Vector getRetrieveBulletinSummaryTags()
	{
		Vector tags = new Vector();
		tags.add(NetworkInterfaceConstants.TAG_BULLETIN_SIZE);
		return tags;
	}

	public static int getBulletinSize(Database db, BulletinHeaderPacket bhp)
	{
		int size = 0;
		DatabaseKey[] bulletinPacketKeys  = BulletinZipUtilities.getAllPacketKeys(bhp);
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
			catch (RecordHiddenException e)
			{
				e.printStackTrace();
				return 0;
			}
		}
		return size;
	}

	public static void deleteDraftBulletinPackets(Database db, UniversalId bulletinUid, MartusCrypto security) throws
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
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
		for (int i = 0; i < keys.length; i++)
		{
			db.discardRecord(keys[i]);
		}
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

	public static class PublicInformationInvalidException extends Exception {}


	public static void validatePublicInfo(String accountId, String sig, MartusCrypto verifier) throws
		PublicInformationInvalidException
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(accountId));
			if(!verifier.isValidSignatureOfStream(accountId, in, Base64.decode(sig)))
				throw new PublicInformationInvalidException();
	
		}
		catch(Exception e)
		{
			//System.out.println("MartusApp.getServerPublicCode: " + e);
			throw new PublicInformationInvalidException();
		}
	}

	public static SSLSocketFactory createSocketFactory(SimpleX509TrustManager tm) throws Exception
	{
		TrustManager []tma = {tm};
		SSLContext sslContext = SSLContext.getInstance( "TLS" );
		SecureRandom secureRandom = new SecureRandom();
		sslContext.init( null, tma, secureRandom);
	
		return sslContext.getSocketFactory();
	
	}

	public static void startTimer(TimerTask task, long interval)
	{
		final long IMMEDIATELY = 0;
	
		Timer timer = new Timer(true);
		timer.schedule(task, IMMEDIATELY, interval);
	}

	static private boolean isCharOkInFileName(char c)
	{
		if(Character.isLetterOrDigit(c))
			return true;
		return false;
	}

	static public String toFileName(String text)
	{
		final int maxLength = 20;
		final int minLength = 3;
	
		if(text.length() > maxLength)
			text = text.substring(0, maxLength);
	
		text = createValidFileName(text);
		if(text.length() < minLength)
			text = "Martus-" + text;
	
		return text;
	}

	public static String createValidFileName(String text) 
	{
		char[] chars = text.toCharArray();
		for(int i = 0; i < chars.length; ++i)
		{
			if(!MartusUtilities.isCharOkInFileName(chars[i]))
				chars[i] = ' ';
		}
		
		text = new String(chars).trim();
		return text;
	}

	public static boolean isFileNameValid(String originalFileName)
	{
		String newFileName = createValidFileName(originalFileName);
		return newFileName.equals(originalFileName);
	}

	public static String extractIpFromFileName(String fileName) throws 
		MartusUtilities.InvalidPublicKeyFileException 
	{
		final String ipStartString = "ip=";
		int ipStart = fileName.indexOf(ipStartString);
		if(ipStart < 0)
			throw new MartusUtilities.InvalidPublicKeyFileException();
		ipStart += ipStartString.length();
		int ipEnd = ipStart;
		for(int i=0; i < 3; ++i)
		{
			ipEnd = fileName.indexOf(".", ipEnd+1);
			if(ipEnd < 0)
				throw new MartusUtilities.InvalidPublicKeyFileException();
		}
		++ipEnd;
		while(ipEnd < fileName.length() && Character.isDigit(fileName.charAt(ipEnd)))
			++ipEnd;
		String ip = fileName.substring(ipStart, ipEnd);
		return ip;
	}

	public static synchronized Vector loadListFromFile(BufferedReader readerInput)
		throws IOException
	{
		Vector result = new Vector();
		try
		{
			while(true)
			{
				String currentLine = readerInput.readLine();
				if(currentLine == null)
					break;
				if(currentLine.length() == 0)
					continue;
					
				if( result.contains(currentLine) )
					continue;
	
				result.add(currentLine);
				//System.out.println("loadListFromFile: " + currentLine);
			}
			
			return result;
		}
		catch(IOException e)
		{
			throw new IOException(e.getMessage());
		}
	}

	static final String PUBLIC_KEY_FILE_IDENTIFIER = "Martus Public Key:";
	static final String PUBLIC_KEY_TYPE_SERVER = "Server";
}
