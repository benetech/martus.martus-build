package org.martus.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MockClientDatabase extends MockDatabase 
{
	public void deleteAllData()
	{
		packetMap = new TreeMap();
		super.deleteAllData();
	}
	
	public Set getAllKeys()
	{
		Set keys = new HashSet();
		keys.addAll(packetMap.keySet());
		return keys;
	}
	
	Map getPacketMapFor(DatabaseKey key) 
	{
		return packetMap;
	}

	Map packetMap;
}
