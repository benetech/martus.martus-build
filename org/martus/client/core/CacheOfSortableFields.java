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

package org.martus.client.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.martus.common.Bulletin;
import org.martus.common.BulletinConstants;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.EncryptionException;

public class CacheOfSortableFields
{

	public CacheOfSortableFields()
	{
		bulletinIdsHashMap = new HashMap(1000);
	}

	public String getFieldData(UniversalId uid, String fieldTag)
	{
		HashMap dataHash = (HashMap)bulletinIdsHashMap.get(uid);
		if(dataHash == null)
			return null;
		return (String)dataHash.get(fieldTag);
	}

	public void setFieldData(Bulletin b)
	{
		HashMap dataHash = new HashMap();
		String[] tags = BulletinConstants.sortableFieldTags;
		for(int i = 0 ; i < tags.length; ++i)
		{
			dataHash.put(tags[i], b.get(tags[i]));
		}
		bulletinIdsHashMap.put(b.getUniversalId(), dataHash);
	}

	public void removeFieldData(UniversalId uid)
	{
		bulletinIdsHashMap.remove(uid);
	}

	public void save(OutputStream out, MartusCrypto security) throws IOException
	{
		try
		{
			byte[] sessionKeyBytes = security.createSessionKey();
			CipherOutputStream cipherOut = security.createCipherOutputStream(out, sessionKeyBytes);
			ObjectOutputStream dataOut = new ObjectOutputStream(cipherOut);
			dataOut.writeObject(bulletinIdsHashMap);
			dataOut.close();
		}
		catch (EncryptionException e)
		{
			throw new IOException("encryption exception");
		}
	}

	public void load(InputStreamWithSeek in, MartusCrypto security) throws IOException
	{
		try
		{
			CipherInputStream cipherIn = security.createCipherInputStream(in, null);
			ObjectInputStream dataIn = new ObjectInputStream(cipherIn);
			bulletinIdsHashMap = (HashMap)dataIn.readObject();
			dataIn.close();
		}
		catch (DecryptionException e)
		{
			bulletinIdsHashMap.clear();
			throw new IOException("decryption exception");
		}
		catch (ClassNotFoundException e)
		{
			bulletinIdsHashMap.clear();
			throw new IOException(e.getMessage());
		}
	}

	HashMap bulletinIdsHashMap;
}
