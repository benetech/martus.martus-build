package org.martus.client;

import java.util.HashMap;

import org.martus.common.UniversalId;

public class CacheOfSortableFields 
{
	
	public CacheOfSortableFields()
	{
		bulletinIdsHashMap = new HashMap(1000);
	}
	
	public String getFieldData (UniversalId uid, String fieldTag)
	{
		HashMap dataHash = (HashMap)bulletinIdsHashMap.get(uid);
		if(dataHash == null)
			return null;
		return (String)dataHash.get(fieldTag);
	}

	public void setFieldData (Bulletin b)
	{
		HashMap dataHash = new HashMap();
		dataHash.put(b.TAGSTATUS, b.getStatus());
		dataHash.put(b.TAGEVENTDATE, b.get(b.TAGEVENTDATE));
		dataHash.put(b.TAGSUMMARY, b.get(b.TAGSUMMARY));
		dataHash.put(b.TAGAUTHOR, b.get(b.TAGAUTHOR));
		bulletinIdsHashMap.put(b.getUniversalId(), dataHash);
	}
	
	HashMap bulletinIdsHashMap;
}
