package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.Database.PacketVisitor;


public class MockDatabase implements Database
{
	public MockDatabase()
	{
		deleteAllData();
	}
	
	public int getOpenStreamCount()
	{
		return streamsThatAreOpen.size();
	}

	// Database interface
	public void deleteAllData()
	{
		sealedPacketMap = new TreeMap();
		draftPacketMap = new TreeMap();
		incomingInterimMap = new TreeMap();
		outgoingInterimMap = new TreeMap();
		draftQuarantine = new TreeMap();
		sealedQuarantine = new TreeMap();
	}

	public void writeRecord(DatabaseKey key, String record) throws IOException
	{
		if(key == null || record == null)
			throw new IOException("Null parameter");
			
		Map map = getCorrectMap(key);
		map.put(key, record);
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
			return new MockRecordInputStream(key, bytes, streamsThatAreOpen);
		} 
		catch(Exception e) 
		{
			System.out.println("MockDatabase.openInputStream: " + e);
			return null;
		}
	}
	
	public String readRecord(DatabaseKey key, MartusCrypto decrypter)
	{
		return readRecord(key);
	}

	public void discardRecord(DatabaseKey key)
	{
		Map map = getCorrectMap(key);
		map.remove(key);
	}

	public boolean doesRecordExist(DatabaseKey key)
	{
		return (readRecord(key) != null);
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
	
	public boolean isInQuarantine(DatabaseKey key)
	{
		Map quarantine = getQuarantineFor(key);
		return quarantine.containsKey(key);
	}
	
	public void moveRecordToQuarantine(DatabaseKey key)
	{
		if(!doesRecordExist(key))
			return;
			
		String data = readRecord(key);
		Map quarantine = getQuarantineFor(key);
		quarantine.put(key, data);
		discardRecord(key);
	}
	
	// end Database interface
	
	private Map getQuarantineFor(DatabaseKey key)
	{
		if(key.isDraft())
			return draftQuarantine;
		else
			return sealedQuarantine;
	}

	private String readRecord(DatabaseKey key)
	{
		Map map = getCorrectMap(key);
		return (String)map.get(key);
	}

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
	
	private Map getCorrectMap(DatabaseKey key) 
	{
		Map map = sealedPacketMap;
		if(key.isDraft())
			map = draftPacketMap;
		return map;
	}

	Map sealedPacketMap;
	Map draftPacketMap;
	Map incomingInterimMap;
	Map outgoingInterimMap;
	Map draftQuarantine;
	Map sealedQuarantine;
	
	HashMap streamsThatAreOpen = new HashMap();
}

class MockRecordInputStream extends ByteArrayInputStream
{
	MockRecordInputStream(DatabaseKey key, byte[] inputBytes, Map observer)
	{
		super(inputBytes);
		streamsThatAreOpen = observer;
		streamsThatAreOpen.put(this, key);
	}
	
	public void close()
	{
		streamsThatAreOpen.remove(this);
	}
	
	Map streamsThatAreOpen;
}
	
