package org.martus.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

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
		dataHash.put(b.TAGSTATUS, b.getStatus());
		dataHash.put(b.TAGEVENTDATE, b.get(b.TAGEVENTDATE));
		dataHash.put(b.TAGSUMMARY, b.get(b.TAGSUMMARY));
		dataHash.put(b.TAGAUTHOR, b.get(b.TAGAUTHOR));
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
