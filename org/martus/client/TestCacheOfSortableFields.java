package org.martus.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import org.martus.common.ByteArrayInputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
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
		
		String title1 = "1 Title";
		String author1 = "1 Author";
		String eventdate1 = "1 11-20-2002";
		Bulletin b1 = new Bulletin((BulletinStore)null);
		UniversalId uid1 = b1.getUniversalId();
		b1.setDraft();
		b1.set(b1.TAGEVENTDATE, eventdate1);
		b1.set(b1.TAGTITLE, title1);
		b1.set(b1.TAGAUTHOR, author1);
		cache.setFieldData(b1);
		assertEquals("wrong status?",b1.get(b1.TAGSTATUS), cache.getFieldData(uid1, b1.TAGSTATUS));
		assertEquals("event date not correct?",eventdate1, cache.getFieldData(uid1, b1.TAGEVENTDATE));
		assertEquals("Title not correct?",title1, cache.getFieldData(uid1, b1.TAGTITLE));
		assertEquals("author not correct?",author1, cache.getFieldData(uid1, b1.TAGAUTHOR));
		
		String title2 = "2 Title";
		String author2 = "2 Author";
		String eventdate2 = "2 11-20-2002";
		Bulletin b2 = new Bulletin((BulletinStore)null);
		UniversalId uid2 = b2.getUniversalId();
		b2.setSealed();
		b2.set(b2.TAGEVENTDATE, eventdate2);
		b2.set(b2.TAGTITLE, title2);
		b2.set(b2.TAGAUTHOR, author2);
		cache.setFieldData(b2);
		assertEquals("2 wrong status?",b2.get(b2.TAGSTATUS), cache.getFieldData(uid2, b2.TAGSTATUS));
		assertEquals("2 event date not correct?",eventdate2, cache.getFieldData(uid2, b2.TAGEVENTDATE));
		assertEquals("2 title not correct?",title2, cache.getFieldData(uid2, b2.TAGTITLE));
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

	public void testRemoveFieldData() 
	{
		CacheOfSortableFields cache = new CacheOfSortableFields();
		Bulletin b2 = new Bulletin((BulletinStore)null);
		b2.set(b2.TAGEVENTDATE, "1020");
		UniversalId uid2 = b2.getUniversalId();
		cache.setFieldData(b2);
		assertEquals("Date not found in cache?", "1020", cache.getFieldData(uid2, b2.TAGEVENTDATE));
		cache.removeFieldData(uid2);
		assertNull("Date still in cache?", cache.getFieldData(uid2, b2.TAGEVENTDATE));
	}
	
	public void testSaveAndLoadCache() throws Exception
	{
		MartusSecurity security = new MartusSecurity();
		security.createKeyPair(512);
		
		CacheOfSortableFields cache = new CacheOfSortableFields();
		Bulletin b = new Bulletin((BulletinStore)null);
		b.set(b.TAGEVENTDATE, "1020");
		UniversalId uid = b.getUniversalId();
		cache.setFieldData(b);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		cache.save(out, security);
		byte[] savedBytes = out.toByteArray();
		CacheOfSortableFields cache2 = new CacheOfSortableFields();
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(savedBytes);
		cache2.load(in, security);
		assertEquals("Data not saved?", "1020", cache2.getFieldData(uid, b.TAGEVENTDATE));
		
		ByteArrayInputStream nonCipherIn = new ByteArrayInputStream(savedBytes);
		try 
		{
			ObjectInputStream dataIn = new ObjectInputStream(nonCipherIn);
			dataIn.readObject();
			fail("This should have thrown, should be encrypted");
		} 
		catch (Exception expectedException) 
		{
		}
	}
}
