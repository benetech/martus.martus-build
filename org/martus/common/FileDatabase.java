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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.FileVerificationException;


public class FileDatabase extends Database
{
	public FileDatabase(File directory, MartusCrypto securityToUse)
	{
		security = securityToUse;
		absoluteBaseDir = directory;
	}

	public static class MissingAccountMapException extends Exception {}
	public static class MissingAccountMapSignatureException extends Exception {}

	// Database interface
	public void deleteAllData() throws Exception
	{
		deleteAllPackets();

		accountMapFile.delete();
		deleteSignaturesForFile(accountMapFile);

		loadAccountMap();
	}
	
	public void deleteSignaturesForFile(File origFile)
	{
		File signature = MartusUtilities.getSignatureFileFromFile(origFile);
		if(signature.exists())
		{
			signature.delete();
		}
	}

	public void initialize() throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		accountMap = new TreeMap();
		accountMapFile = new File(absoluteBaseDir, ACCOUNTMAP_FILENAME);
		accountMapSignatureFile = MartusUtilities.getSignatureFileFromFile(accountMapFile);
		loadAccountMap();
		if(isAccountMapExpected() && !accountMapFile.exists())
		{
			throw new MissingAccountMapException();
		}
	}

	public boolean isAccountMapExpected()
	{
		if(absoluteBaseDir.exists())
		{
			int fileCount = absoluteBaseDir.list().length;
			if(fileCount > 0)
				return true;
			else
				return false;
		}
		return false;
	}

	public void writeRecord(DatabaseKey key, String record) throws IOException
	{
		writeRecord(key, new StringInputStream(record));
	}

	public int getRecordSize(DatabaseKey key) throws IOException
	{
		try
		{
			return (int)getFileForRecord(key).length();
		}
		catch (TooManyAccountsException e)
		{
			System.out.println("FileDatabase:getRecordSize" + e);
		}
		return 0;
	}

	public void importFiles(HashMap fileMapping)  throws IOException
	{
		Iterator keys = fileMapping.keySet().iterator();
		while(keys.hasNext())
		{
			DatabaseKey key = (DatabaseKey) keys.next();
			File file = (File) fileMapping.get(key);

			InputStream in = new FileInputStream(file.getAbsolutePath());
			writeRecord(key,in);
			in.close();
			file.delete();
		}
	}

	public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws
			IOException,
			MartusCrypto.CryptoException
	{
		if(encrypter == null)
			throw new IOException("Null encrypter");

		InputStream in = new StringInputStream(record);
		writeRecordUsingCopier(key, in, new StreamEncryptor(encrypter));
	}

	public void writeRecord(DatabaseKey key, InputStream in) throws IOException
	{
		writeRecordUsingCopier(key, in, new StreamCopier());
	}

	public String readRecord(DatabaseKey key, MartusCrypto decrypter) throws
			IOException,
			MartusCrypto.CryptoException
	{
		InputStreamWithSeek in = openInputStream(key, decrypter);
		if(in == null)
			return null;

		try
		{
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
			return new String(bytes, "UTF-8");
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private String decryptRecord(InputStreamWithSeek in, MartusCrypto decrypter) throws
			IOException,
			MartusCrypto.CryptoException
	{
		in.read(); //throwAwayFlagByte

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decrypter.decrypt(in, out);
		in.close();
		return new String(out.toByteArray(), "UTF-8");
	}

	public InputStreamWithSeek openInputStream(DatabaseKey key, MartusCrypto decrypter) throws
			IOException,
			MartusCrypto.CryptoException
	{
		try
		{
			File file = getFileForRecord(key);
			InputStreamWithSeek in = new FileInputStreamWithSeek(file);

			return convertToDecryptingStreamIfNecessary(in, decrypter);
		}
		catch(IOException e)
		{
			//System.out.println("FileDatabase.openInputStream: " + e);
		}
		catch(TooManyAccountsException e)
		{
			System.out.println("FileDatabase.openInputStream: " + e);
		}

		return null;
	}

	public void discardRecord(DatabaseKey key)
	{
		try
		{
			File file = getFileForRecord(key);
			file.delete();
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase.discardRecord: " + e);
		}
	}

	public boolean doesRecordExist(DatabaseKey key)
	{
		try
		{
			File file = getFileForRecord(key);
			return file.exists();
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase.doesRecordExist: " + e);
		}

		return false;
	}

	public void visitAllRecords(PacketVisitor visitor)
	{
		class AccountVisitorVisitor implements AccountVisitor
		{
			AccountVisitorVisitor(PacketVisitor visitor)
			{
				packetVisitor = visitor;
			}

			public void visit(String accountString)
			{
				visitAllRecordsForAccount(packetVisitor, accountString);
			}
			PacketVisitor packetVisitor;
		}

		AccountVisitorVisitor accountVisitor = new AccountVisitorVisitor(visitor);
		visitAllAccounts(accountVisitor);
	}

	public String getFolderForAccount(String accountString)
	{
		try
		{
			File dir = getAccountDirectory(accountString);
			return convertToRelativePath(dir.getPath());
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase:getFolderForAccount clientId=" + accountString + " : " + e);
		}
		return accountString;
	}

	public File getAbsoluteInterimFolderForAccount(String accountString) throws
		IOException
	{
		File accountFolder = new File(absoluteBaseDir, getFolderForAccount(accountString));
		File interimFolder = new File(accountFolder, INTERIM_FOLDER_NAME);
		interimFolder.mkdirs();
		return interimFolder;
	}

	public File getAbsoluteContactInfoFolderForAccount(String accountString) throws
		IOException
	{
		File accountFolder = new File(absoluteBaseDir, getFolderForAccount(accountString));
		File ContactFolder = new File(accountFolder, CONTACTINFO_FOLDER_NAME);
		ContactFolder.mkdirs();
		return ContactFolder;
	}

	public File getIncomingInterimFile(DatabaseKey key) throws
		IOException
	{
		File folder = getAbsoluteInterimFolderForAccount(key.getAccountId());
		return new File(folder, key.getLocalId()+".in");
	}

	public File getOutgoingInterimFile(DatabaseKey key) throws
		IOException
	{
		File folder = getAbsoluteInterimFolderForAccount(key.getAccountId());
		return new File(folder, key.getLocalId()+".out");
	}

	public File getContactInfoFile(String accountId) throws
		IOException
	{
		File folder = getAbsoluteContactInfoFolderForAccount(accountId);
		return new File(folder, "contactInfo.dat");
	}

	public boolean isInQuarantine(DatabaseKey key)
	{
		try
		{
			return getQuarantineFileForRecord(key).exists();
		}
		catch(Exception nothingWeCanDoAboutIt)
		{
			System.out.println("FileDatabase.isInQuarantine: " + nothingWeCanDoAboutIt);
			return false;
		}
	}

	public void moveRecordToQuarantine(DatabaseKey key)
	{
		try
		{
			File moveFrom = getFileForRecord(key);
			File moveTo = getQuarantineFileForRecord(key);

			moveTo.delete();
			moveFrom.renameTo(moveTo);
		}
		catch(Exception nothingWeCanDoAboutIt)
		{
			System.out.println("FileDatabase.moveRecordToQuarantine: " + nothingWeCanDoAboutIt);
		}
	}

	// end Database interface

	public void visitAllAccounts(AccountVisitor visitor)
	{
		Set accounts = accountMap.keySet();
		Iterator iterator = accounts.iterator();
		while(iterator.hasNext())
		{
			String accountString = (String)iterator.next();
			try
			{
				visitor.visit(accountString);
			}
			catch (RuntimeException nothingWeCanDoAboutIt)
			{
				// nothing we can do, so ignore it
			}
		}
	}

	public void visitAllRecordsForAccount(PacketVisitor visitor, String accountString)
	{
		File accountDir = null;
		try
		{
			accountDir = getAccountDirectory(accountString);
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase.visitAllPacketsForAccount: " + e);
			return;
		}

		String[] packetBuckets = accountDir.list();
		if(packetBuckets != null)
		{
			for(int packetBucket = 0; packetBucket < packetBuckets.length; ++packetBucket)
			{
				File bucketDir = new File(accountDir, packetBuckets[packetBucket]);
				if(INTERIM_FOLDER_NAME.equals(bucketDir.getName()))
					continue;
				if(CONTACTINFO_FOLDER_NAME.equals(bucketDir.getName()))
					continue;
				if(isQuarantineBucketDirectory(bucketDir))
					continue;

				String[] files = bucketDir.list();
				if(files != null)
				{
					for(int i=0; i < files.length; ++i)
					{
						UniversalId uid = UniversalId.createFromAccountAndLocalId(accountString, files[i]);
						DatabaseKey key = new DatabaseKey(uid);
						if(isDraftPacketBucket(packetBuckets[packetBucket]))
							key.setDraft();
						else
							key.setSealed();

						try
						{
							visitor.visit(key);
						}
						catch (RuntimeException nothingWeCanDoAboutIt)
						{
							// nothing we can do, so ignore it
						}
					}
				}
			}
		}
	}

	boolean isQuarantineBucketDirectory(File bucketDir)
	{
		if(bucketDir.getName().startsWith(draftQuarantinePrefix))
			return true;
		if(bucketDir.getName().startsWith(sealedQuarantinePrefix))
			return true;

		return false;
	}

	public void deleteAllPackets()
	{
		class AccountDeleter implements AccountVisitor
		{
			public void visit(String accountString)
			{
				File accountDir = getAbsoluteAccountDirectory(accountString);
				File[] subdirectories = accountDir.listFiles();
				for (int i = 0; i < subdirectories.length; i++)
				{
					deleteAllFilesInDirectory(subdirectories[i]);
				}

				File parentDir = accountDir.getParentFile();
				accountDir.delete();
				parentDir.delete();
			}

		}

		AccountDeleter deleter = new AccountDeleter();
		visitAllAccounts(deleter);
	}

	public File getAbsoluteAccountDirectory(String accountString)
	{
		return new File(absoluteBaseDir, (String)accountMap.get(accountString));
	}

	public File getFileForRecord(DatabaseKey key) throws IOException, TooManyAccountsException
	{
		return getFileForRecordWithPrefix(key, getBucketPrefix(key));
	}

	public File getFileForRecordWithPrefix(DatabaseKey key, String bucketPrefix)
		throws IOException, TooManyAccountsException
	{
		int hashValue = getHashValue(key.getLocalId()) & 0xFF;
		String bucketName = bucketPrefix + Integer.toHexString(0xb00 + hashValue);
		String accountString = key.getAccountId();
		File path = new File(getAccountDirectory(accountString), bucketName);
		path.mkdirs();
		return new File(path, key.getLocalId());
	}

	private File getQuarantineFileForRecord(DatabaseKey key)
		throws IOException, TooManyAccountsException
	{
		return getFileForRecordWithPrefix(key, getQuarantinePrefix(key));
	}

	private String getQuarantinePrefix(DatabaseKey key)
	{
		if(key.isDraft())
			return draftQuarantinePrefix;
		else
			return sealedQuarantinePrefix;
	}

	public class TooManyAccountsException extends Exception {}

	File getAccountDirectory(String accountString) throws IOException, TooManyAccountsException
	{
		String accountDir = (String)accountMap.get(accountString);
		if(accountDir == null)
			return generateAccount(accountString);
		return new File(absoluteBaseDir, accountDir);
	}

	synchronized File generateAccount(String accountString)
		throws IOException, TooManyAccountsException
	{
		int hashValue = getHashValue(accountString) & 0xFF;
		String bucketName = "/a" + Integer.toHexString(0xb00 + hashValue);
		File bucketDir = new File(absoluteBaseDir, bucketName);
		int countInBucket = 0;
		String[] existingAccounts = bucketDir.list();
		if(existingAccounts != null)
			countInBucket = existingAccounts.length;
		int tryValue = countInBucket;
		for(int index = 0; index < 100000000;++index)
		{
			String tryName = Integer.toHexString(0xa0000000 + tryValue);
			File accountDir = new File(bucketDir, tryName);
			if(!accountDir.exists())
			{
				accountDir.mkdirs();
				String relativeDirString = convertToRelativePath(accountDir.getPath());
				accountMap.put(accountString, relativeDirString);
				appendAccountToMapFile(accountString, relativeDirString);
				return accountDir;
			}
		}
		throw new TooManyAccountsException();
	}

	void appendAccountToMapFile(String accountString, String accountDir) throws IOException
	{
		FileOutputStream out = new FileOutputStream(accountMapFile.getPath(), true);
		UnicodeWriter writer = new UnicodeWriter(out);
		try
		{
			writer.writeln(accountDir + "=" + accountString);
		}
		finally
		{
			writer.flush();
			out.flush();
			out.getFD().sync();
			writer.close();

			try
			{
				signAccountMap();
			}
			catch (MartusSignatureException e)
			{
				System.out.println("FileDatabase.appendAccountToMapFile: " + e);
			}
		}
	}

	synchronized public void loadAccountMap() throws FileVerificationException, MissingAccountMapSignatureException
	{
		accountMap.clear();
		try
		{
			UnicodeReader reader = new UnicodeReader(accountMapFile);
			String entry = null;
			while( (entry = reader.readLine()) != null)
			{
				addParsedAccountEntry(accountMap, entry);
			}
			reader.close();
		}
		catch(FileNotFoundException e)
		{
			// not a problem--just use the empty map
		}
		catch(IOException e)
		{
			System.out.println("FileDatabase.loadMap: " + e);
			return;
		}
	}

	void addParsedAccountEntry(Map m, String entry)
	{
		if(entry.startsWith("#"))
			return;

		int splitAt = entry.indexOf("=");
		if(splitAt <= 0)
			return;

		String accountString = entry.substring(splitAt+1);
		String accountDir = entry.substring(0,splitAt);
		if(startsWithAbsolutePath(accountDir))
			accountDir = convertToRelativePath(accountDir);

		if(m.containsKey(accountString))
		{
			System.out.println("WARNING: Duplicate entries in account map: ");
			System.out.println(" " + accountDir + " and " + m.get(accountString));
		}
		m.put(accountString, accountDir);
	}

	boolean startsWithAbsolutePath(String accountDir)
	{
		return accountDir.startsWith(File.separator) || accountDir.startsWith(":",1);
	}

	public String convertToRelativePath(String absoluteAccountPath)
	{
		try
		{
			File dir = new File(absoluteAccountPath);
			File bucket = dir.getParentFile();
			return bucket.getName() + File.separator + dir.getName();
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase:getFolderForAccount clientId=" + absoluteAccountPath + " : " + e);
		}
		return absoluteAccountPath;
	}


	void deleteAllPacketsForAccount(File accountDir)
	{
		class PacketDeleter implements PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				discardRecord(key);
			}
		}

		PacketDeleter deleter = new PacketDeleter();
		visitAllRecordsForAccount(deleter, getAccountString(accountDir));

		accountDir.delete();
	}

	String getAccountString(File accountDir)
	{
		try
		{
			Set accountStrings = accountMap.keySet();
			Iterator iterator = accountStrings.iterator();
			while(iterator.hasNext())
			{
				String accountString = (String)iterator.next();
				if(getAccountDirectory(accountString).equals(accountDir))
					return accountString;
			}
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase.getAccountString: " + e);
		}
		return null;
	}

	private synchronized void writeRecordUsingCopier(DatabaseKey key, InputStream in, StreamFilter copier)
		throws IOException
	{
		if(key == null)
			throw new IOException("Null key");

		try
		{
			File file = getFileForRecord(key);
			FileOutputStream rawOut = new FileOutputStream(file);
			MartusUtilities.copyStreamWithFilter(in, rawOut, copier);
		}
		catch(TooManyAccountsException e)
		{
			// TODO: Make sure this case is tested!
			System.out.println("FileDatabase.writeRecord1b: " + e);
			throw new IOException("Too many accounts");
		}
	}

	public boolean isDraftPacketBucket(String folderName)
	{
		return false;
	}

	static int getHashValue(String inputString)
	{
		//Linux Elf hashing algorithm
		int result = 0;
		for(int i = 0; i < inputString.length(); ++i)
		{
			char c = inputString.charAt(i);
			result = (result << 4) + c;
			int x = result & 0xF0000000;
			if(x != 0)
				result ^= (x >> 24);
			result &= ~x;
		}
		return result;
	}

	protected String getBucketPrefix(DatabaseKey key)
	{
		return defaultBucketPrefix;
	}

	private static void deleteAllFilesInDirectory(File directory)
	{
		File[] files = directory.listFiles();
		if(files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				files[i].delete();
			}
		}
		directory.delete();
	}

	public void signAccountMap() throws IOException, MartusCrypto.MartusSignatureException
	{
		accountMapSignatureFile = MartusUtilities.createSignatureFileFromFile(accountMapFile, security);
	}

	public void verifyAccountMap() throws MartusUtilities.FileVerificationException, MissingAccountMapSignatureException
	{
		if( !accountMapSignatureFile.exists() )
		{
			throw new MissingAccountMapSignatureException();
		}

		MartusUtilities.verifyFileAndSignature(accountMapFile, accountMapSignatureFile, security, security.getPublicKeyString());
	}
	
	public File getAccountMapFile()
	{
		return accountMapFile;
	}

	protected static final String defaultBucketPrefix = "p";
	protected static final String draftQuarantinePrefix = "qd-p";
	protected static final String sealedQuarantinePrefix = "qs-p";
	protected static final String INTERIM_FOLDER_NAME = "interim";
	protected static final String CONTACTINFO_FOLDER_NAME = "contactInfo";
	protected static final String ACCOUNTMAP_FILENAME = "acctmap.txt";

	public MartusCrypto security;

	File absoluteBaseDir;
	Map accountMap;
	File accountMapFile;
	File accountMapSignatureFile;
}
