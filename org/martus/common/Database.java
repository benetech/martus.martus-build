package org.martus.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public interface Database
{
	public void deleteAllData();
	public void writeRecord(DatabaseKey key, String record) throws IOException;
	public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws IOException, MartusCrypto.CryptoException;
	public void writeRecord(DatabaseKey key, InputStream record) throws IOException;
	public void importFiles(HashMap entries) throws IOException;
	public InputStreamWithSeek openInputStream(DatabaseKey key, MartusCrypto decrypter) throws IOException, MartusCrypto.CryptoException;
	public String readRecord(DatabaseKey key, MartusCrypto decrypter) throws IOException, MartusCrypto.CryptoException;
	public void discardRecord(DatabaseKey key);
	public boolean doesRecordExist(DatabaseKey key);
	public void visitAllRecords(PacketVisitor visitor);
	public String getFolderForAccount(String accountString);
	public File getIncomingInterimFile(DatabaseKey key) throws IOException;
	public File getOutgoingInterimFile(DatabaseKey key) throws IOException;
	public File getContactInfoFile(String accountId) throws IOException;
	
	public boolean isInQuarantine(DatabaseKey key);
	public void moveRecordToQuarantine(DatabaseKey key);
	
	public interface PacketVisitor
	{
		void visit(DatabaseKey key);
	}
	
	public interface AccountVisitor
	{
		void visit(String accountString, File accountDir);
	}
}
