package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.Database.PacketVisitor;

import MartusJava.MartusCrypto;

public class MockDatabase implements Database
{
	public MockDatabase()
	{
		deleteAllData();
	}

	// Database interface
	public void deleteAllData()
	{
		sealedPacketMap = new TreeMap();
		draftPacketMap = new TreeMap();
		incomingInterimMap = new TreeMap();
		outgoingInterimMap = new TreeMap();
	}

	public void writeRecord(DatabaseKey key, String record) throws IOException
	{
		if(key == null || record == null)
			throw new IOException("Null parameter");
		if(key.isDraft())
			draftPacketMap.put(key, record);
		else
			sealedPacketMap.put(key, record);
	}
	
	public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws 
			IOException
	{
		writeRecord(key, record);
	}

	//TODO try BufferedInputStream
	public void writeRecord(DatabaseKey key, InputStream record) throws IOException
	{
		if(key == null || record == null)
			throw new IOException("Null parameter");

		String data = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int theByte = 0;
		while( (theByte = record.read()) >= 0)
			out.write(theByte);

		byte[] bytes = out.toByteArray();
		data = new String(bytes, "UTF-8");

		writeRecord(key, data);
	}

	public InputStream openInputStream(DatabaseKey key, MartusCrypto decrypter)
	{
		String data = readRecord(key, decrypter);
		if(data == null)
			return null;
			
		try 
		{
			byte[] bytes = data.getBytes("UTF-8");
			return new ByteArrayInputStream(bytes);
		} 
		catch(Exception e) 
		{
			System.out.println("MockDatabase.openInputStream: " + e);
			return null;
		}
	}
	
	public String readRecord(DatabaseKey key, MartusCrypto decrypter)
	{
		if(key.isDraft())
			return (String)draftPacketMap.get(key);
		else
			return (String)sealedPacketMap.get(key);
	}

	public void discardRecord(DatabaseKey key)
	{
		if(key.isDraft())
			draftPacketMap.remove(key);
		else
			sealedPacketMap.remove(key);
	}

	public boolean doesRecordExist(DatabaseKey key)
	{
		if(key.isDraft())
			return draftPacketMap.containsKey(key);
		else
			return sealedPacketMap.containsKey(key);
	}

	public void visitAllRecords(PacketVisitor visitor)
	{
		Set keys = new HashSet();
		keys.addAll(getAllSealedKeys());
		keys.addAll(getAllDraftKeys());

		Iterator iterator = keys.iterator();
		while(iterator.hasNext())
		{
			DatabaseKey key = (DatabaseKey)iterator.next();
			visitor.visit(key);
		}
	}

	public String getFolderForAccount(String accountString)
	{
		return accountString;
	}

	public File getIncomingInterimFile(DatabaseKey key)
	{
		if(incomingInterimMap.containsKey(key))
			return (File)incomingInterimMap.get(key);
			
		try 
		{
			File interimFile = File.createTempFile("$$$MockDbInterim", null);
			interimFile.deleteOnExit();
			interimFile.delete();
			incomingInterimMap.put(key, interimFile);
			return interimFile;	
		} 
		catch (IOException e) 
		{
			return null;
		}
	}

	public File getOutgoingInterimFile(DatabaseKey key)
	{
		if(outgoingInterimMap.containsKey(key))
			return (File)outgoingInterimMap.get(key);
			
		try 
		{
			File interimFile = File.createTempFile("$$$MockDbInterim", null);
			interimFile.deleteOnExit();
			interimFile.delete();
			outgoingInterimMap.put(key, interimFile);
			return interimFile;	
		} 
		catch (IOException e) 
		{
			return null;
		}
	}
	
	// end Database interface

	public int getSealedRecordCount()
	{
		return getAllSealedKeys().size();
	}
	
	public Set getAllSealedKeys()
	{
		return sealedPacketMap.keySet();
	}
	
	public Set getAllDraftKeys()
	{
		return draftPacketMap.keySet();
	}
	
	Map sealedPacketMap;
	Map draftPacketMap;
	Map incomingInterimMap;
	Map outgoingInterimMap;
}
