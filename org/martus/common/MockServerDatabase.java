package org.martus.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MockServerDatabase extends MockDatabase
{
	public void deleteAllData()
	{
		sealedPacketMap = new TreeMap();
		draftPacketMap = new TreeMap();
		super.deleteAllData();
	}
	
	public int getSealedRecordCount()
	{
		return getAllSealedKeys().size();
	}
	
	synchronized void internalDiscardRecord(DatabaseKey key)
	{
		Map map = getPacketMapFor(key);
		map.remove(key);
	}

	synchronized Set internalGetAllKeys()
	{
		Set keys = new HashSet();
		keys.addAll(getAllSealedKeys());
		keys.addAll(getAllDraftKeys());
		return keys;
	}
	
	synchronized void addKeyToMap(DatabaseKey key, String record) 
	{
		getPacketMapFor(key).put(key, record);
	}
	
	synchronized String readRecord(DatabaseKey key)
	{
		Map map = getPacketMapFor(key);
		return (String)map.get(key);
	}

	Set getAllSealedKeys()
	{
		return sealedPacketMap.keySet();
	}
	
	Set getAllDraftKeys()
	{
		return draftPacketMap.keySet();
	}
	
	Map getPacketMapFor(DatabaseKey key) 
	{
		Map map = sealedPacketMap;
		if(key.isDraft())
			map = draftPacketMap;
		return map;
	}

	Map sealedPacketMap;
	Map draftPacketMap;
}
