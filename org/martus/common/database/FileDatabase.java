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

package org.martus.common.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.StreamEncryptor;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.packet.UniversalId;
import org.martus.util.FileInputStreamWithSeek;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.StreamCopier;
import org.martus.util.StreamFilter;
import org.martus.util.StringInputStream;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


abstract public class FileDatabase extends Database
{
	abstract public void verifyAccountMap() throws MartusUtilities.FileVerificationException, MissingAccountMapSignatureException;

	public FileDatabase(File directory, MartusCrypto securityToUse)
	{
		security = securityToUse;
		absoluteBaseDir = directory;
		accountMapFile = new File(absoluteBaseDir, ACCOUNTMAP_FILENAME);
		accountMapSignatureFile = MartusUtilities.getSignatureFileFromFile(accountMapFile);
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
		loadAccountMap();
		if(isAccountMapExpected(absoluteBaseDir) && !accountMapFile.exists())
		{
			throw new MissingAccountMapException();
		}
	}

	public static boolean isAccountMapExpected(File baseDirectory)
	{
		if(!baseDirectory.exists())
			return false;
			
		File files[] = baseDirectory.listFiles();
		for (int i = 0; i < files.length; i++) 
		{
			File thisFile = files[i];
			if(thisFile.isDirectory() && thisFile.getName().startsWith("a"))
				return true; 
		}

		return false;
	}

	public void writeRecord(DatabaseKey key, String record) 
			throws IOException, RecordHiddenException
	{
		writeRecord(key, new StringInputStream(record));
	}

	public int getRecordSize(DatabaseKey key) 
		throws IOException, RecordHiddenException
	{
		throwIfRecordIsHidden(key);

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

	public void importFiles(HashMap fileMapping) 
			throws IOException, RecordHiddenException
	{
		throwIfAnyRecordsHidden(fileMapping);

		Iterator keys = fileMapping.keySet().iterator();
		while(keys.hasNext())
		{
			DatabaseKey key = (DatabaseKey) keys.next();
			File fromFile = (File) fileMapping.get(key);
			File toFile = getFileForRecord(key);
			toFile.delete();
			fromFile.renameTo(toFile);
			if(!toFile.exists())
				throw new IOException("renameTo failed");
		}
	}

	public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws
			IOException,
			RecordHiddenException, 			
			MartusCrypto.CryptoException
	{
		if(encrypter == null)
			throw new IOException("Null encrypter");

		InputStream in = new StringInputStream(record);
		writeRecordUsingCopier(key, in, new StreamEncryptor(encrypter));
	}

	public void writeRecord(DatabaseKey key, InputStream in) 
			throws IOException, RecordHiddenException
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

	public InputStreamWithSeek openInputStream(DatabaseKey key, MartusCrypto decrypter) throws
			IOException,
			MartusCrypto.CryptoException
	{
		if(isHidden(key))
			return null;

		try
		{
			File file = getFileForRecord(key);
			InputStreamWithSeek in = new FileInputStreamWithSeek(file);

			return convertToDecryptingStreamIfNecessary(in, decrypter);
		}
		catch(TooManyAccountsException e)
		{
			System.out.println("FileDatabase.openInputStream: " + e);
		}
		catch(IOException e)
		{
			//System.out.println("FileDatabase.openInputStream: " + e);
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
		if(isHidden(key))
			return false;

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
		IOException, RecordHiddenException
	{
		return createInterimFile(key, "in");
	}

	public File getOutgoingInterimFile(DatabaseKey key) throws
		IOException, RecordHiddenException
	{
		return createInterimFile(key, "out");
	}

	public File getOutgoingInterimPublicOnlyFile(DatabaseKey key) throws
		IOException, RecordHiddenException
	{
		return createInterimFile(key, "public");
	}

	private File createInterimFile(DatabaseKey key, String extension) throws RecordHiddenException, IOException
	{
		throwIfRecordIsHidden(key);
		File folder = getAbsoluteInterimFolderForAccount(key.getAccountId());
		return new File(folder, key.getLocalId()+"."+extension);
	}


	public File getContactInfoFile(String accountId) throws
		IOException
	{
		File folder = getAbsoluteContactInfoFolderForAccount(accountId);
		return new File(folder, "contactInfo.dat");
	}

	public boolean isInQuarantine(DatabaseKey key) throws RecordHiddenException
	{
		throwIfRecordIsHidden(key);
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

	public void moveRecordToQuarantine(DatabaseKey key) throws RecordHiddenException
	{
		throwIfRecordIsHidden(key);
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
				// TODO: nothing we can do, so ignore it
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
						if(isHidden(uid))
							continue;
						if(uid.getLocalId().startsWith("BUR-"))
							continue;
							
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
	
	public void scrubRecord(DatabaseKey key) 
			throws IOException, RecordHiddenException
	{
		File file = getFileForRecord(key);		
		RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
		randomFile.seek(0);
			
		for (int i=0; i< randomFile.length();++i)
		{
			randomFile.write(0x55);
		}
			
		randomFile.close();		
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

	public class TooManyAccountsException extends IOException {}

	public File getAccountDirectory(String accountString) throws IOException, TooManyAccountsException
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
		if(!accountMapFile.exists())
			return;
		try
		{
			verifyAccountMap();
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

	public void addParsedAccountEntry(Map m, String entry)
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

	public String getAccountString(File accountDir)
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
		throws IOException, RecordHiddenException
	{
		if(key == null)
			throw new IOException("Null key");

		throwIfRecordIsHidden(key);

		try
		{
			File file = getFileForRecord(key);
			OutputStream rawOut = createOutputStream(file);
			MartusUtilities.copyStreamWithFilter(in, rawOut, copier);
		}
		catch(TooManyAccountsException e)
		{
			// TODO: Make sure this case is tested!
			System.out.println("FileDatabase.writeRecord1b: " + e);
			throw new IOException("Too many accounts");
		}
	}
	protected OutputStream createOutputStream(File file)
		throws IOException
	{
		return new FileOutputStream(file);
	}

	public boolean isDraftPacketBucket(String folderName)
	{
		return false;
	}

	public static int getHashValue(String inputString)
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

	static void deleteAllFilesInDirectory(File directory)
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

	public File absoluteBaseDir;
	Map accountMap;
	public File accountMapFile;
	public File accountMapSignatureFile;
}
