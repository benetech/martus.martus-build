/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.test;

import java.util.Vector;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.BulletinStore;
import org.martus.common.Bulletin;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;

public class TestBulletinFolder extends TestCaseEnhanced
{
    public TestBulletinFolder(String name)
	{
        super(name);
    }

    public void setUp() throws Exception
    {
    	if(store == null)
    	{
			store = new BulletinStore(new MockClientDatabase());
			store.setSignatureGenerator(new MockMartusSecurity());
			testFolder = store.createFolder("something");

			b = store.createEmptyBulletin();
    		store.saveBulletin(b);
			testFolder.add(b);

			b2 = store.createEmptyBulletin();
    		store.saveBulletin(b2);
			testFolder.add(b2);
    	}
    }

    public void testBasics() throws Exception
    {
		BulletinStore store = new MockBulletinStore();
		assertEquals(false, (store == null));

		// shouldn't normally create a folder this way!
		BulletinFolder folder = new BulletinFolder(store, "bad");
		assertEquals("Raw folder should start out empty\n", 0, folder.getBulletinCount());

		// this is the way to get a folder
		folder = store.createFolder("blah");
		assertEquals(false, (folder == null));
		assertEquals("Store folder should start out empty\n", 0, folder.getBulletinCount());

		assertEquals(store, folder.getStore());

	}

	public void testSetName() throws Exception
	{
		BulletinStore store = new MockBulletinStore();

		final String name = "Interesting folder name";
		BulletinFolder folder = store.createFolder(name);
		assertEquals(name, folder.getName());

		folder.setName("Boring");
		assertEquals("Boring", folder.getName());

		assertEquals(true, folder.canRename());
		folder.preventRename();
		assertEquals(false, folder.canRename());
		folder.setName("Different");
		assertEquals("Boring", folder.getName());
	}

	public void testCanDelete() throws Exception
	{
		BulletinStore store = new MockBulletinStore();
		BulletinFolder folder = store.createFolder("blah");
		assertEquals(true, folder.canDelete());
		folder.preventDelete();
		assertEquals(false, folder.canDelete());
	}

	public void testIsVisible() throws Exception
	{
		BulletinStore store = new MockBulletinStore();
		BulletinFolder normalFolder = store.createFolder("blah");
		assertEquals("not visible?", true, normalFolder.isVisible());

		BulletinFolder hiddenFolder = store.createFolder("*blah");
		assertEquals("visible?", false, hiddenFolder.isVisible());
	}

	public void testGetBulletin()
	{
		BulletinFolder folder = store.createFolder("blah");
		assertEquals(0, folder.getBulletinCount());

		Bulletin b = folder.getBulletinSorted(-1);
		assertEquals(null, b);

		b = folder.getBulletinSorted(0);
		assertEquals(null, b);

		b = folder.getBulletinSorted(folder.getBulletinCount());
		assertEquals(null, b);

		createEmptyBulletins(folder, 6);
		assertEquals(6, folder.getBulletinCount());

		b = folder.getBulletinSorted(folder.getBulletinCount());
		assertEquals(null, b);

		b = folder.getBulletinSorted(0);
		assertEquals(false, (b == null));
	}

	public void testStatusRules()
	{
		BulletinFolder folder = store.createFolder("a");

		assertNull("Should allow anything", folder.getStatusAllowed());
		assertEquals(true, folder.canAdd(Bulletin.STATUSDRAFT));
		assertEquals(true, folder.canAdd(Bulletin.STATUSSEALED));

		folder.setStatusAllowed(Bulletin.STATUSDRAFT);
		assertEquals(Bulletin.STATUSDRAFT, folder.getStatusAllowed());
		assertEquals(true, folder.canAdd(Bulletin.STATUSDRAFT));
		assertEquals(false, folder.canAdd(Bulletin.STATUSSEALED));


		folder.setStatusAllowed(Bulletin.STATUSSEALED);
		assertEquals(false, folder.canAdd(Bulletin.STATUSDRAFT));
		assertEquals(true, folder.canAdd(Bulletin.STATUSSEALED));
	}

	public void testAdd()
	{
		BulletinFolder folder = store.createFolder("a2");

		// can't add unsaved bulletin to a folder
		Bulletin b = store.createEmptyBulletin();
		assertTrue(b != null);
		folder.add(b);
		assertEquals(0, folder.getBulletinCount());

		final int count = 7;

		BulletinFolder scratchFolder = store.createFolder("b");
		createEmptyBulletins(scratchFolder, count);
		assertEquals(count, scratchFolder.getBulletinCount());
		assertEquals(0, folder.getBulletinCount());

		Vector v = store.getAllBulletinUids();
		UniversalId uid0 = (UniversalId)v.get(0);
		b = store.findBulletinByUniversalId(uid0);
		folder.add(b);
		assertEquals(1, folder.getBulletinCount());
		assertEquals(true, folder.contains(b));

		// adding has no effect if it's already there
		folder.add(b);
		assertEquals(1, folder.getBulletinCount());
		assertEquals(true, folder.contains(b));

		UniversalId uid1 = (UniversalId)v.get(1);
		b = store.findBulletinByUniversalId(uid1);
		assertEquals("This bulletin is not in the folder\n", false, folder.contains(b));

		Bulletin b2 = folder.getBulletinSorted(0);
		assertEquals("First bulletin in folder should be valid\n", false, (b2 == null));
	}

	public void testRemove()
	{
		assertEquals("start", 2, testFolder.getBulletinCount());
		UniversalId badId = UniversalId.createDummyUniversalId();
		testFolder.remove(badId);

		assertEquals("after non remove", 2, testFolder.getBulletinCount());

		testFolder.remove(b.getUniversalId());
		assertEquals(1, testFolder.getBulletinCount());
		testFolder.add(b);
	}

	public void testRemoveAll()
	{
		assertTrue("Need some samples", store.getBulletinCount() > 0);
		assertTrue("Shouldn't be empty", testFolder.getBulletinCount() >= 2);
		testFolder.removeAll();
		assertEquals(0, testFolder.getBulletinCount());
	}

	public void testFind()
	{
		BulletinFolder folder = store.createFolder("blah2");
		Bulletin b = store.createEmptyBulletin();
		assertEquals(-1, folder.find(b.getUniversalId()));

		createEmptyBulletins(folder, 3);
		b = folder.getBulletinSorted(2);
		assertNotNull("Can't find added bulletin", b);
		assertEquals(2, folder.find(b.getUniversalId()));
	}

	public void testSorting()
	{
		BulletinFolder folder = store.createFolder("blah3");

		assertEquals("eventdate", folder.sortedBy());

		Bulletin b = store.createEmptyBulletin();
		b.set("eventdate","20010101");
		b.set("author","billy bob");
		store.saveBulletin(b);
		folder.add(b);
		b = store.createEmptyBulletin();
		b.set("eventdate","20010201");
		b.set("author","tom tupa");
		store.saveBulletin(b);
		folder.add(b);
		b = store.createEmptyBulletin();
		b.set("eventdate","20010301");
		b.set("author","adam ant");
		store.saveBulletin(b);
		folder.add(b);
		b = store.createEmptyBulletin();
		b.set("eventdate","20010401");
		b.set("author","nervous nellie");
		store.saveBulletin(b);
		folder.add(b);
		assertEquals("initial count", 4, folder.getBulletinCount());
		b = folder.getBulletinSorted(0);
		assertEquals("20010101", b.get("eventdate"));

		// sort descending
		folder.sortBy("eventdate");
		assertEquals("reverse count", 4, folder.getBulletinCount());
		assertEquals("eventdate", folder.sortedBy());
		assertEquals("Not Decending?", folder.DESCENDING, folder.getSortDirection());
		b = folder.getBulletinSorted(0);
		assertEquals("20010401", b.get("eventdate"));

		// and back to ascending
		folder.sortBy("eventdate");
		assertEquals("new field count", 4, folder.getBulletinCount());
		assertEquals("eventdate", folder.sortedBy());
		assertEquals("Not Assending?", folder.ASCENDING, folder.getSortDirection());
		b = folder.getBulletinSorted(0);
		assertEquals("20010101", b.get("eventdate"));

		// sort by other field
		folder.sortBy("author");
		assertEquals("new field count", 4, folder.getBulletinCount());
		assertEquals("author", folder.sortedBy());
		b = folder.getBulletinSorted(0);
		assertEquals("adam ant", b.get("author"));
		// and descending
		folder.sortBy("author");
		assertEquals("second reverse count", 4, folder.getBulletinCount());
		assertEquals("author", folder.sortedBy());
		b = folder.getBulletinSorted(0);
		assertEquals("tom tupa", b.get("author"));

		// add while in descending mode
		b = store.createEmptyBulletin();
		b.set("eventdate","20010401");
		b.set("author","zippy zorro");
		store.saveBulletin(b);
		folder.add(b);
		b = folder.getBulletinSorted(0);
		assertEquals("zippy zorro", b.get("author"));
	}

	void createEmptyBulletins(BulletinFolder folder, int count)
	{
		BulletinStore store = folder.getStore();
		for(int i = 0; i < count; ++i)
		{
			Bulletin b = store.createEmptyBulletin();
			store.saveBulletin(b);
			folder.add(b);
		}
	}

	class MockBulletinStore extends BulletinStore
	{
		MockBulletinStore() throws Exception
		{
			super(new MockClientDatabase());
			setSignatureGenerator(new MockMartusSecurity());
		}
	}

	static BulletinStore store;
	static BulletinFolder testFolder;
	static Bulletin b;
	static Bulletin b2;
}
