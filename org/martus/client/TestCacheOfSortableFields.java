package org.martus.client;

import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;

public class TestCacheOfSortableFields extends TestCaseEnhanced 
{

	public TestCacheOfSortableFields(String name) 
	{
		super(name);
	}
	
	public void testGetAndSet()
	{
		CacheOfSortableFields cache = new CacheOfSortableFields();
		
		String summary1 = "1 Summary";
		String author1 = "1 Author";
		String eventdate1 = "1 11-20-2002";
		Bulletin b1 = new Bulletin((BulletinStore)null);
		UniversalId uid1 = b1.getUniversalId();
		b1.setDraft();
		b1.set(b1.TAGEVENTDATE, eventdate1);
		b1.set(b1.TAGSUMMARY, summary1);
		b1.set(b1.TAGAUTHOR, author1);
		cache.setFieldData(b1);
		assertEquals("wrong status?",b1.get(b1.TAGSTATUS), cache.getFieldData(uid1, b1.TAGSTATUS));
		assertEquals("event date not correct?",eventdate1, cache.getFieldData(uid1, b1.TAGEVENTDATE));
		assertEquals("summary not correct?",summary1, cache.getFieldData(uid1, b1.TAGSUMMARY));
		assertEquals("author not correct?",author1, cache.getFieldData(uid1, b1.TAGAUTHOR));
		
		String summary2 = "2 Summary";
		String author2 = "2 Author";
		String eventdate2 = "2 11-20-2002";
		Bulletin b2 = new Bulletin((BulletinStore)null);
		UniversalId uid2 = b2.getUniversalId();
		b2.setSealed();
		b2.set(b1.TAGEVENTDATE, eventdate2);
		b2.set(b1.TAGSUMMARY, summary2);
		b2.set(b1.TAGAUTHOR, author2);
		cache.setFieldData(b2);
		assertEquals("2 wrong status?",b2.get(b2.TAGSTATUS), cache.getFieldData(uid2, b2.TAGSTATUS));
		assertEquals("2 event date not correct?",eventdate2, cache.getFieldData(uid2, b2.TAGEVENTDATE));
		assertEquals("2 summary not correct?",summary2, cache.getFieldData(uid2, b2.TAGSUMMARY));
		assertEquals("2 author not correct?",author2, cache.getFieldData(uid2, b2.TAGAUTHOR));
	}

	public void testGetBadTag() 
	{
		CacheOfSortableFields cache = new CacheOfSortableFields();
		Bulletin b2 = new Bulletin((BulletinStore)null);
		UniversalId uid2 = b2.getUniversalId();
		cache.setFieldData(b2);
		assertNull("an invalid tag found in cache?", cache.getFieldData(uid2, "tag"));
	}

	public void testEmptyCache() 
	{
		CacheOfSortableFields cache = new CacheOfSortableFields();
		assertNull("found tag with no cache?", cache.getFieldData(null, "tag"));
	}

}
