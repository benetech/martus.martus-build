/* $Id: $ */
package org.martus.common;

import org.martus.common.*;

public class TestUniversalId extends TestCaseEnhanced
{

	public TestUniversalId(String name)
	{
		super(name);
	}
	
	public void testConstructorWithBothIds()
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		assertEquals("account?", sampleAccountId, uid.getAccountId());
		assertEquals("local?", sampleLocalId, uid.getLocalId());
	}
	
	public void testAccountId()
	{
		final String sampleAccountId1 = "an account id";
		UniversalId uid = UniversalId.createFromAccountAndPrefix(sampleAccountId1, "");
		assertEquals("wrong account?", sampleAccountId1, uid.getAccountId());

		final String sampleAccountId2 = "another silly account id";
		uid.setAccountId(sampleAccountId2);
		assertEquals("didn't set account?", sampleAccountId2, uid.getAccountId());
	}
	
	public void testLocalId()
	{
		UniversalId uid = UniversalId.createDummyUniversalId();

		assertNotNull("no local id?", uid.getLocalId());
		assertTrue("local id too short?", uid.getLocalId().length() > 20);
		assertTrue("local id too long?", uid.getLocalId().length() < 40);
		assertEquals("contructor didn't strip colons?", -1, uid.getLocalId().indexOf(":"));
		
		UniversalId uid2 = UniversalId.createDummyUniversalId();
		assertNotEquals("dupe?", uid.getLocalId(), uid2.getLocalId());
		
		uid.setLocalId(sampleLocalId);
		assertEquals("didn't set local?", sampleLocalId, uid.getLocalId());
		
		final String sampleLocalIdWithColons = "sample:with:colons";
		uid.setLocalId("This:That");
		assertEquals("setter didn't strip colons?", "This-That", uid.getLocalId());
	}
	
	public void testToString()
	{
		UniversalId uid = UniversalId.createFromAccountAndPrefix(sampleAccountId, "");

		String whole = uid.toString();
		assertContains("no account?", sampleAccountId, whole);
		assertContains("no local?", uid.getLocalId(), whole);
		assertEquals("toString didn't strip colons?", -1, whole.indexOf(":"));
	}
	
	public void testEquals()
	{
		UniversalId uid1 = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		assertEquals("equals said false1a?", true, uid1.equals(uid1));
		assertEquals("equals said true1b?", false, uid1.equals(null));
		assertEquals("equals said true1c?", false, uid1.equals(uid1.toString()));

		UniversalId uid2 = UniversalId.createFromAccountAndLocalId(new String(sampleAccountId), new String(sampleLocalId));
		assertEquals("equals said false2a?", true, uid1.equals(uid2));
		assertEquals("equals said false2b?", true, uid2.equals(uid1));
		
		UniversalId uid3 = UniversalId.createFromAccountAndLocalId(sampleAccountId+"x", sampleLocalId);
		assertEquals("equals said true3a?", false, uid1.equals(uid3));
		assertEquals("equals said true3b?", false, uid3.equals(uid1));
		
		UniversalId uid4 = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId+"x");
		assertEquals("equals said true4a?", false, uid1.equals(uid4));
		assertEquals("equals said true4b?", false, uid4.equals(uid1));
		
		assertEquals("hashCode 1 2", uid1.hashCode(), uid2.hashCode());
		assertNotEquals("hashCode 1 3", uid1.hashCode(), uid3.hashCode());
		assertNotEquals("hashCode 1 4", uid1.hashCode(), uid4.hashCode());
		assertNotEquals("hashCode 3 4", uid3.hashCode(), uid4.hashCode());
	}
	
	public void testCreateFromString() throws Exception
	{
		UniversalId uid1 = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		UniversalId uid2 = UniversalId.createFromString(uid1.toString());
		assertEquals("account?", uid1.getAccountId(), uid2.getAccountId());
		assertEquals("local?", uid1.getLocalId(), uid2.getLocalId());
		
		try
		{
			UniversalId uid3 = UniversalId.createFromString("lbisdjf");
			fail("Should have thrown!");
		}
		catch(UniversalId.NotUniversalIdException ignoreExpectedException)
		{
		}
	}

	final String sampleAccountId = "an account id";
	final String sampleLocalId = "a local id";
}
