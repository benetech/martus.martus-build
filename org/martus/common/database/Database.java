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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.packet.UniversalId;
import org.martus.util.ByteArrayInputStreamWithSeek;
import org.martus.util.InputStreamWithSeek;

abstract public class Database
{
	public static class RecordHiddenException extends Exception {}

	public interface PacketVisitor
	{
		void visit(DatabaseKey key);
	}

	public interface AccountVisitor
	{
		void visit(String accountString);
	}

	protected Database()
	{
		final int expectedMaxEntries = 100;
		hiddenPacketUids = Collections.synchronizedSet(new HashSet(expectedMaxEntries));
	}
	
	abstract public void deleteAllData() throws Exception;
	abstract public void initialize() throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException;
	abstract public void writeRecord(DatabaseKey key, String record) throws IOException, RecordHiddenException;
	abstract public void writeRecord(DatabaseKey key, InputStream record) throws IOException, RecordHiddenException;
	abstract public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws IOException, RecordHiddenException, MartusCrypto.CryptoException;
	abstract public void importFiles(HashMap entries) throws IOException, RecordHiddenException;
	abstract public InputStreamWithSeek openInputStream(DatabaseKey key, MartusCrypto decrypter) throws IOException, MartusCrypto.CryptoException;
	abstract public String readRecord(DatabaseKey key, MartusCrypto decrypter) throws IOException, MartusCrypto.CryptoException;
	abstract public void discardRecord(DatabaseKey key);
	abstract public boolean doesRecordExist(DatabaseKey key);
	abstract public int getRecordSize(DatabaseKey key) throws IOException, RecordHiddenException;
	abstract public void visitAllRecords(PacketVisitor visitor);
	abstract public void visitAllAccounts(AccountVisitor visitor);
	abstract public void visitAllRecordsForAccount(PacketVisitor visitor, String accountString);
	abstract public File getIncomingInterimFile(DatabaseKey key) throws IOException, RecordHiddenException;
	abstract public File getOutgoingInterimFile(DatabaseKey key) throws IOException, RecordHiddenException;
	abstract public File getOutgoingInterimPublicOnlyFile(DatabaseKey key) throws IOException, RecordHiddenException;

	abstract public String getFolderForAccount(String accountString);
	abstract public File getContactInfoFile(String accountId) throws IOException;

	abstract public boolean isInQuarantine(DatabaseKey key) throws RecordHiddenException;
	abstract public void moveRecordToQuarantine(DatabaseKey key) throws RecordHiddenException;
	abstract public void verifyAccountMap() throws MartusUtilities.FileVerificationException, MissingAccountMapSignatureException;

	abstract public void signAccountMap() throws IOException, MartusCrypto.MartusSignatureException;
	abstract public void scrubRecord(DatabaseKey key) throws IOException, RecordHiddenException;

	public void hide(UniversalId uid)
	{
		hiddenPacketUids.add(uid);
	}
	
	public boolean isHidden(UniversalId uid)
	{
		return hiddenPacketUids.contains(uid);
	}
	
	public boolean isHidden(DatabaseKey key)
	{
		return isHidden(key.getUniversalId());
	}

	public boolean mustEncryptLocalData()
	{
		return false;
	}
	
	public boolean doesAccountMapExist()
	{
		return false;
	}
	public boolean doesAccountMapSignatureExist()
	{
		return false;
	}

	boolean isEncryptedRecordStream(InputStreamWithSeek in) throws
			IOException
	{
		int flagByte = in.read();
		in.seek(0);
		boolean isEncrypted = false;
		if(flagByte == 0)
			isEncrypted = true;
		return isEncrypted;
	}

	InputStreamWithSeek convertToDecryptingStreamIfNecessary(
		InputStreamWithSeek in,
		MartusCrypto decrypter)
		throws IOException, NoKeyPairException, DecryptionException
	{
		if(!isEncryptedRecordStream(in))
			return in;

		in.read(); //throwAwayFlagByte
		ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
		decrypter.decrypt(in, decryptedOut);
		in.close();

		byte[] bytes = decryptedOut.toByteArray();
		return new ByteArrayInputStreamWithSeek(bytes);
	}

	protected void throwIfAnyRecordsHidden(HashMap fileMapping) throws RecordHiddenException
	{
		Iterator lookForHiddenKeys = fileMapping.keySet().iterator();
		while(lookForHiddenKeys.hasNext())
		{
			DatabaseKey key = (DatabaseKey) lookForHiddenKeys.next();
			if(isHidden(key))
				throw new RecordHiddenException();
		}
	}

	protected void throwIfRecordIsHidden(DatabaseKey key) throws RecordHiddenException
	{
		if(isHidden(key))
			throw new RecordHiddenException();
	}

	Set hiddenPacketUids;
}
