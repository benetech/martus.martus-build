package org.martus.common;



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
	
	public void testConstructors() throws Exception
	{
		
	}

	public void testEqualsStrings() throws Exception
	{
		UniversalId uid1 = UniversalId.createDummyUniversalId();
		UniversalId uid2 = UniversalId.createDummyUniversalId();

		DatabaseKey key1 = new DatabaseKey(uid1);
		DatabaseKey key2 = new DatabaseKey(UniversalId.createFromAccountAndLocalId(uid1.getAccountId(), uid1.getLocalId()));
		DatabaseKey key3 = new DatabaseKey(uid2);
		DatabaseKey key4 = new DatabaseKey(uid1);
		key4.setDraft();
		assertEquals("self should match", key1, key1);
		assertEquals("never match null", false, key1.equals(null));
		assertEquals("never match uid", false, key1.equals(uid1));
		assertEquals("Keys should match", key1, key2);
		assertEquals("symmetrical equals", key2, key1);
		assertEquals("should not match", false, key1.equals(key3));
		assertEquals("symmetrical not equals", false, key3.equals(key1));
		assertNotEquals("status ignored?", key4, key1);

		assertEquals("hash self should match", key1.hashCode(), key1.hashCode());
		assertEquals("hash Keys should match", key1.hashCode(), key2.hashCode());
		assertNotEquals("hash didn't use status?", key1.hashCode(), key4.hashCode());
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

	public void testGetAccount() throws Exception
	{
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey key = new DatabaseKey(uid);
		assertEquals("wrong account?", uid.getAccountId(), key.getAccountId());
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
