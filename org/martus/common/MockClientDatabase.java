package org.martus.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MockClientDatabase extends MockDatabase 
{
	public boolean mustEncryptLocalData()
	{
		return true;
	}

	public void deleteAllData()
	{
		packetMap = new TreeMap();
		super.deleteAllData();
	}
	
	synchronized void addKeyToMap(DatabaseKey key, String record)
	{
		DatabaseKey newKey = DatabaseKey.createLegacyKey(key.getUniversalId());
		packetMap.put(newKey, record);
	}
	
	synchronized String readRecord(DatabaseKey key)
	{
		DatabaseKey newKey = DatabaseKey.createLegacyKey(key.getUniversalId());
		return (String)packetMap.get(newKey);
	}
	
	synchronized void internalDiscardRecord(DatabaseKey key)
	{
		DatabaseKey newKey = DatabaseKey.createLegacyKey(key.getUniversalId());
		packetMap.remove(newKey);
	}

	Map getPacketMapFor(DatabaseKey key) 
	{
		return packetMap;
	}

	public synchronized Set internalGetAllKeys()
	{
		Set keys = new HashSet();
		keys.addAll(packetMap.keySet());
		return keys;
	}
	
	Map packetMap;
}
