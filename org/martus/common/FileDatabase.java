package org.martus.common;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.Database.AccountVisitor;
import org.martus.common.Database.PacketVisitor;


public class FileDatabase implements Database
{
	public FileDatabase(File directory) throws MissingAccountMapException
	{
		accountMap = new TreeMap();
		absoluteBaseDir = directory;
		accountMapFile = new File(absoluteBaseDir, "acctmap.txt");
		if(absoluteBaseDir.exists() && !accountMapFile.exists())
		{
			int fileCount = absoluteBaseDir.list().length;
			if(fileCount > 0)
				throw new MissingAccountMapException();
		}
		loadAccountMap();
	}

	public static class MissingAccountMapException extends Exception {}

	// Database interface
	public void deleteAllData()
	{
		deleteAllPackets();

		accountMapFile.delete();
		loadAccountMap();
	}

	public void writeRecord(DatabaseKey key, String record) throws IOException
	{
		writeRecord(key, MartusUtilities.openStringInputStream(record));
	}
	
	public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		if(encrypter == null)
			throw new IOException("Null encrypter");

		writeRecordUsingCopier(key, new StringToStreamCopierWithEncryption(record, encrypter));
	}

	public void writeRecord(DatabaseKey key, InputStream record) throws IOException
	{
		writeRecordUsingCopier(key, new StreamToStreamCopier(record));
	}
	
	public String readRecord(DatabaseKey key, MartusCrypto decrypter) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		InputStream in = openInputStream(key, decrypter);
		if(in == null)
			return null;
			
		boolean isEncrypted = isEncryptedRecordStream(in);
			
		if(isEncrypted)
			return decryptRecord(in, decrypter);
			
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

	public boolean isEncryptedRecordStream(InputStream in) throws 
			IOException 
	{
		in.mark(1);
		int flagByte = in.read();
		in.reset();
		boolean isEncrypted = false;
		if(flagByte == 0)
			isEncrypted = true;
		return isEncrypted;
	}
	
	private String decryptRecord(InputStream in, MartusCrypto decrypter) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		int throwAwayFlagByte = in.read();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decrypter.decrypt(in, out);
		in.close();
		return new String(out.toByteArray(), "UTF-8");
	}
	
	public InputStream openInputStream(DatabaseKey key, MartusCrypto decrypter) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		try
		{
			File file = getFileForRecord(key);
			InputStream in = null;
			in = new FileInputStreamWithReset(file);
			
			if(!isEncryptedRecordStream(in))
				return in;
				
			int throwAwayFlagByte = in.read();
			ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
			decrypter.decrypt(in, decryptedOut);
			in.close();
			
			byte[] bytes = decryptedOut.toByteArray();
			return new ByteArrayInputStream(bytes);
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
			
			public void visit(String accountString, File accountDir)
			{
				visitAllPacketsForAccount(packetVisitor, accountString);
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
			File accountDir = new File(absoluteBaseDir, (String)accountMap.get(accountString));
			visitor.visit(accountString, accountDir);
		}
	}
	
	public void visitAllPacketsForAccount(PacketVisitor visitor, String accountString)
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
						visitor.visit(key);
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
			public void visit(String accountString, File accountDir)
			{
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
		}
	}

	synchronized void loadAccountMap()
	{
		accountMap.clear();
		boolean foundAbsolutePath = false;
		try
		{
			UnicodeReader reader = new UnicodeReader(accountMapFile);
			String entry = null;
			while( (entry = reader.readLine()) != null)
			{
				if(startsWithAbsolutePath(entry))
					foundAbsolutePath = true;
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
		if(foundAbsolutePath)
		{
			//One time migration of old AccountMap.txt files
			File backupFile = new File(accountMapFile.getPath() + ".bak");
			accountMapFile.renameTo(backupFile);
			try 
			{
				Set accountStrings = accountMap.keySet();
				Iterator iterator = accountStrings.iterator();
				while(iterator.hasNext())
				{
					String accountString = (String)iterator.next();
					String accountDir = (String)accountMap.get(accountString);
					appendAccountToMapFile(accountString, accountDir);
				}
			} 
			catch (IOException e) 
			{
				System.out.println("FileDatabase.loadMap: migration " + e);
				accountMapFile.delete();
				backupFile.renameTo(accountMapFile);
			}
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
		visitAllPacketsForAccount(deleter, getAccountString(accountDir));
		
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

	private interface ToStreamCopier
	{
		public void copyToStream(OutputStream out) throws IOException;
	}
	
	private static class StringToStreamCopierWithEncryption implements ToStreamCopier
	{
		StringToStreamCopierWithEncryption(String dataToCopy, MartusCrypto cryptoToUse)
		{
			data = dataToCopy;
			crypto = cryptoToUse;
		}
		
		public void copyToStream(OutputStream out) throws IOException
		{
			try
			{
				out.write(0);
				crypto.encrypt(MartusUtilities.openStringInputStream(data), out);
			}
			catch(MartusCrypto.CryptoException e)
			{
				throw new IOException("MartusCrypto exception");
			}
		}

		String data;
		MartusCrypto crypto;
	}
	
	private static class StreamToStreamCopier implements ToStreamCopier
	{
		StreamToStreamCopier(InputStream source)
		{
			in = source;
		}
		
		public void copyToStream(OutputStream out) throws IOException
		{
			if(in == null)
				throw new IOException("Null InputStream");
			int got;
			byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
			while( (got=in.read(bytes)) >= 0)
				out.write(bytes, 0, got);
		}
		
		InputStream in;
	}

	private synchronized void writeRecordUsingCopier(DatabaseKey key, ToStreamCopier copier)
		throws IOException 
	{
		if(key == null)
			throw new IOException("Null key");

		FileOutputStream rawOut = openRecordOutputStream(key);
		BufferedOutputStream out = (new BufferedOutputStream(rawOut));
		try
		{
			copier.copyToStream(out);
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

	private FileOutputStream openRecordOutputStream(DatabaseKey key) throws 
			IOException 
	{
		try
		{
			File file = getFileForRecord(key);
			return new FileOutputStream(file);
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

	protected static final String defaultBucketPrefix = "p";
	protected static final String draftQuarantinePrefix = "qd-p";
	protected static final String sealedQuarantinePrefix = "qs-p";
	protected static final String INTERIM_FOLDER_NAME = "interim";
	
	File absoluteBaseDir;
	Map accountMap;
	File accountMapFile;
}
