package org.martus.common;

import java.io.*;
import java.util.*;

import org.martus.common.*;

public class TestDatabaseKey extends TestCaseEnhanced
{
	public TestDatabaseKey(String name)
	{
		super(name);
	}

	public void setUp()
	{
	}

	public void TRACE(String text)
	{
		//System.out.println(text);
	}

	public void testEqualsStrings() throws Exception
	{
		String keyValue1 = "blah-yada";
		String keyValue2 = "woo-woo!";

		DatabaseKey key1 = new DatabaseKey(keyValue1);
		DatabaseKey key2 = new DatabaseKey(new String(keyValue1));
		DatabaseKey key3 = new DatabaseKey(keyValue2);
		DatabaseKey key4 = new DatabaseKey(keyValue1);
		key4.setDraft();
		assertEquals("self should match", key1, key1);
		assertEquals("never match null", false, key1.equals(null));
		assertEquals("never match string", false, key1.equals(keyValue1));
		assertEquals("Keys should match", key1, key2);
		assertEquals("symmetrical equals", key2, key1);
		assertEquals("should not match", false, key1.equals(key3));
		assertEquals("symmetrical not equals", false, key3.equals(key1));
		assertNotEquals("status ignored?", key4, key1);

		assertEquals("hash self should match", key1.hashCode(), key1.hashCode());
		assertEquals("hash Keys should match", key1.hashCode(), key2.hashCode());
		assertNotEquals("hash didn't use status?", key1.hashCode(), key4.hashCode());
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId("blah", "yada");
		assertEquals("uid?", uid, key1.getUniversalId());
	}

	public void testEquals() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();

		DatabaseKey key1 = new DatabaseKey(uid);
		DatabaseKey key2 = new DatabaseKey(uid);
		DatabaseKey key3 = new DatabaseKey(UniversalId.createDummyUniversalId());
		assertEquals("self should match", key1, key1);
		assertEquals("never match null", false, key1.equals(null));
		assertEquals("never match string", false, key1.equals(uid));
		assertEquals("Keys should match", key1, key2);
		assertEquals("symmetrical equals", key2, key1);
		assertEquals("should not match", false, key1.equals(key3));
		assertEquals("symmetrical not equals", false, key3.equals(key1));

		assertEquals("hash self should match", key1.hashCode(), key1.hashCode());
		assertEquals("hash Keys should match", key1.hashCode(), key2.hashCode());
	}

	public void testGetString() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey key1 = new DatabaseKey(uid.toString());
		DatabaseKey key2 = new DatabaseKey(uid);
		assertEquals("bad string?", key1.getString(), key2.getString());
	}
	
	public void testGetAccount() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey key = new DatabaseKey(uid);
		assertEquals("wrong account?", uid.getAccountId(), key.getAccountId());
		
		String value = "acct-local";
		DatabaseKey key2 = new DatabaseKey(value);
		assertEquals("account?", "acct", key2.getAccountId());
		assertEquals("local?", "local", key2.getLocalId());
	}

	public void testStatus() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey key = new DatabaseKey(uid);
		// TODO: should default to unknown, really, but that would break a lot of code right now
		assertEquals("Default not sealed?", true, key.isSealed());
		assertEquals("Default was draft?", false, key.isDraft());
		key.setDraft();
		assertEquals("Sealed still set?", false, key.isSealed());
		assertEquals("Draft not set?", true, key.isDraft());
		key.setSealed();
		assertEquals("Sealed not set?", true, key.isSealed());
		assertEquals("Draft still set?", false, key.isDraft());
		
	}
}
