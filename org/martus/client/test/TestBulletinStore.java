/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;

import org.martus.client.core.Bulletin;
import org.martus.client.core.BulletinFolder;
import org.martus.client.core.BulletinLoader;
import org.martus.client.core.BulletinSaver;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.MartusClientXml;
import org.martus.common.AttachmentProxy;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusXml;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;


public class TestBulletinStore extends TestCaseEnhanced
{

    public TestBulletinStore(String name) {
        super(name);
    }

	public void TRACE(String text)
	{
		//System.out.println(text);
	}


    public void setUp() throws Exception
    {
    	if(store == null)
    	{
    		store = createTempStore();
    		db = (MockDatabase)store.getDatabase();
			security = (MockMartusSecurity)store.getSignatureGenerator();
    	}

    	if(tempFile1 == null)
    	{
			tempFile1 = createTempFile(sampleBytes1);
			tempFile2 = createTempFile(sampleBytes2);
    	}
    }

    public void tearDown() throws Exception
    {
    	assertEquals("Still some mock streams open?", 0, db.getOpenStreamCount());
		store.deleteAllData();
    }

    public void testBasics()
    {
		TRACE("testBasics");

		BulletinFolder folder = store.createFolder("blah");
		assertEquals(false, (folder == null));

		Bulletin b = store.createEmptyBulletin();
		assertEquals("wrong author?", "", b.get("Author"));
		assertEquals("wrong account?", security.getPublicKeyString(), b.getAccount());
	}

	public void testGetAllBulletinUids()
	{
		TRACE("testGetAllBulletinUids");
		Vector empty = store.getAllBulletinUids();
		assertEquals("not empty?", 0, empty.size());

		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		Vector one = store.getAllBulletinUids();
		assertEquals("not one?", 1, one.size());
		UniversalId gotUid = (UniversalId)one.get(0);
		UniversalId bUid = b.getUniversalId();
		assertEquals("wrong uid 1?", bUid, gotUid);

		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		Vector two = store.getAllBulletinUids();
		assertEquals("not two?", 2, two.size());
		assertTrue("missing 1?", two.contains(b.getUniversalId()));
		assertTrue("missing 2?", two.contains(b2.getUniversalId()));
	}
	
	public void testVisitAllBulletins()
	{
		TRACE("testGetAllBulletinUids");
		
		class BulletinUidCollector implements Database.PacketVisitor
		{
			BulletinUidCollector(BulletinStore store)
			{
				store.visitAllBulletins(this);
			}
			
			public void visit(DatabaseKey key)
			{
				uids.add(key.getUniversalId());
			}
			
			Vector uids = new Vector();
		}
		
		assertEquals("not empty?", 0, new BulletinUidCollector(store).uids.size());
		
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		Vector one = new BulletinUidCollector(store).uids;
		assertEquals("not one?", 1, one.size());
		UniversalId gotUid = (UniversalId)one.get(0);
		UniversalId bUid = b.getUniversalId();
		assertEquals("wrong uid 1?", bUid, gotUid);

		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		Vector two = new BulletinUidCollector(store).uids;
		assertEquals("not two?", 2, two.size());
		assertTrue("missing 1?", two.contains(b.getUniversalId()));
		assertTrue("missing 2?", two.contains(b2.getUniversalId()));
	}
	
	public void testCaching()
	{
		int numBulletins = BulletinStore.maxCachedBulletinCount + 1;
		for(int i = 0; i < numBulletins; ++i)
		{
			Bulletin b = store.createEmptyBulletin();
			store.saveBulletin(b);
			store.findBulletinByUniversalId(b.getUniversalId());
		}
		
		assertEquals("cache too large?", true, store.bulletinCache.size() <= BulletinStore.maxCachedBulletinCount);
	}

	public void testDestroyBulletin() throws Exception
	{
		TRACE("testDestroyBulletin");
		int originalRecordCount = db.getRecordCount();

		Bulletin b = store.createEmptyBulletin();
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b.addPublicAttachment(a1);
		assertEquals("added one", 1, b.getPublicAttachments().length);

		b.addPrivateAttachment(a2);
		assertEquals("added 4", 1, b.getPrivateAttachments().length);
		
		store.saveBulletin(b);
		BulletinFolder f = store.createFolder("test");
		f.add(b);
		store.destroyBulletin(b);
		assertEquals(0, store.getBulletinCount());
		assertEquals(0, f.getBulletinCount());
		assertEquals(originalRecordCount, db.getRecordCount());
	}

	public void testGetFieldData() throws Exception
	{
		TRACE("testGetFieldData");
		assertEquals(0, store.getBulletinCount());
		String sampleSummary = "Summary Data";
		String sampleEventDate = "11-20-2002";
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, sampleSummary);
		b.set(Bulletin.TAGEVENTDATE, sampleEventDate);
		b.setDraft();
		store.saveBulletin(b);
		UniversalId uId = b.getUniversalId();
		assertEquals("Wrong summary?", sampleSummary, store.getFieldData(uId, Bulletin.TAGSUMMARY));
		assertEquals("Wrong event date?", sampleEventDate, store.getFieldData(uId, Bulletin.TAGEVENTDATE));
		assertEquals("Wrong status?", b.getStatus(), store.getFieldData(uId, Bulletin.TAGSTATUS));
		
	}
	public void testSaveBulletin() throws Exception
	{
		TRACE("testSaveBulletin");

		final String initialSummary = "New bulletin";

		assertEquals(0, store.getBulletinCount());

		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, initialSummary);
		store.saveBulletin(b);
		UniversalId uId = b.getUniversalId();
		assertEquals(1, store.getBulletinCount());
		assertEquals(false, (uId.toString().length() == 0));
		assertEquals("not saved initially?", initialSummary, store.findBulletinByUniversalId(uId).get(Bulletin.TAGSUMMARY));

		// re-saving the same bulletin replaces the old one
		UniversalId id = b.getUniversalId();
		store.saveBulletin(b);
		assertEquals(1, store.getBulletinCount());
		assertEquals("Saving should keep same id", id, b.getUniversalId());
		assertEquals("not still saved?", initialSummary, store.findBulletinByUniversalId(uId).get(Bulletin.TAGSUMMARY));

		// unsaved bulletin changes should not be in the store
		b.set(Bulletin.TAGSUMMARY, "not saved yet");
		assertEquals("saved without asking?", initialSummary, store.findBulletinByUniversalId(uId).get(Bulletin.TAGSUMMARY));

		// saving a new bulletin with a non-empty id should retain that id
		int oldCount = store.getBulletinCount();
		b = store.createEmptyBulletin();
		UniversalId uid = b.getBulletinHeaderPacket().getUniversalId();
		store.saveBulletin(b);
		assertEquals(oldCount+1, store.getBulletinCount());
		assertEquals("b uid?", uid, b.getBulletinHeaderPacket().getUniversalId());
		
		b = store.findBulletinByUniversalId(uid);
		assertEquals("store uid?", uid, b.getBulletinHeaderPacket().getUniversalId());

	}

	public void testFindBulletinById()
	{
		TRACE("testFindBulletinById");

		assertEquals(0, store.getBulletinCount());
		UniversalId uInvalidId = UniversalId.createDummyUniversalId();
		Bulletin b = store.findBulletinByUniversalId(uInvalidId);
		assertEquals(true, (b == null));

		b = store.createEmptyBulletin();
		store.saveBulletin(b);
		UniversalId id = b.getUniversalId();

		Bulletin b2 = store.findBulletinByUniversalId(id);
		assertEquals(false, (b2 == null));
		assertEquals(b.get("summary"), b2.get("summary"));
	}

	public void testDiscardBulletin()
	{
		TRACE("testDiscardBulletin");

		BulletinFolder f = store.getFolderSent();
		assertNotNull("Need Sent folder", f);
		BulletinFolder discarded = store.getFolderDiscarded();
		assertNotNull("Need Discarded folder", f);
		
		Bulletin start1 = store.createEmptyBulletin();
		store.saveBulletin(start1);
		f.add(start1);
		
		Bulletin b = f.getBulletinSorted(0);
		assertNotNull("Sent folder should have bulletins", b);

		assertEquals(true, f.contains(b));
		assertEquals(false, discarded.contains(b));
		store.discardBulletin(f, b);
		assertEquals("Bulletin wasn't discarded!", false, f.contains(b));
		assertEquals("Bulletin wasn't copied to Discarded", true, discarded.contains(b));

		Bulletin b2 = store.createEmptyBulletin();
		b2.set("subject", "amazing");
		store.saveBulletin(b2);
		BulletinFolder user1 = store.createFolder("1");
		BulletinFolder user2 = store.createFolder("2");
		user1.add(b2);
		user2.add(b2);

		assertEquals(true, user1.contains(b2));
		assertEquals(true, user2.contains(b2));
		assertEquals(false, discarded.contains(b2));
		store.discardBulletin(user1, b2);
		assertEquals("Bulletin wasn't discarded!", false, user1.contains(b2));
		assertEquals("Copy of bulletin accidentally discarded\n", true, user2.contains(b2));
		assertEquals("Should be in Discarded now", true, discarded.contains(b2));
		store.discardBulletin(user2, b2);
		assertEquals("Bulletin wasn't discarded!", false, user2.contains(b2));
		assertEquals("Should be in Discarded now", true, discarded.contains(b2));

		store.discardBulletin(discarded, b2);
		assertEquals("Should no longer be in Discarded", false, discarded.contains(b2));
		assertNull("Should no longer exist at all", store.findBulletinByUniversalId(b2.getUniversalId()));
	}

	public void testRemoveBulletinFromFolder()
	{
		TRACE("testRemoveBulletinFromFolder");

		BulletinFolder f = store.getFolderSent();
		assertNotNull("Need Sent folder", f);

		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		f.add(b1);
		assertEquals(true, f.contains(b1));
		store.removeBulletinFromFolder(b1, f);
		assertEquals(false, f.contains(b1));
	}

	public void testCreateFolder()
	{
		TRACE("testCreateFolder");

		BulletinFolder folder = store.createFolder("blah");
		assertEquals(false, (folder == null));

		BulletinFolder folder2 = store.createFolder("blah");
		assertNull("Can't create two folders with same name", folder2);
	}

	public void testCreateOrFindFolder()
	{
		TRACE("testCreateOrFindFolder");

		assertNull("x shouldn't exist", store.findFolder("x"));
		BulletinFolder folder = store.createOrFindFolder("x");
		assertNotNull("Create x", folder);

		BulletinFolder folder2 = store.createOrFindFolder("x");
		assertEquals(folder, folder2);
	}

	public void testCreateSystemFolders()
	{
		TRACE("testCreateSystemFolders");

		BulletinFolder fOutbox = store.getFolderOutbox();
		assertNotNull("Should have created Outbox folder", fOutbox);
		assertEquals("Outbox/Draft", false, fOutbox.canAdd(Bulletin.STATUSDRAFT));
		assertEquals("Outbox/Sealed", true, fOutbox.canAdd(Bulletin.STATUSSEALED));
//		assertEquals("Incorrect Outbox Name", BulletinStore.OUTBOX_FOLDER, fOutbox.getName());

		BulletinFolder fSent = store.getFolderSent();
		assertNotNull("Should have created Sent folder", fSent);
		assertEquals("Sent/Draft", false, fSent.canAdd(Bulletin.STATUSDRAFT));
		assertEquals("Sent/Sealed", true, fSent.canAdd(Bulletin.STATUSSEALED));

		BulletinFolder fDrafts = store.getFolderDrafts();
		assertNotNull("Should have created Drafts folder", fDrafts);
		assertEquals("Drafts/Draft", true, fDrafts.canAdd(Bulletin.STATUSDRAFT));
		assertEquals("Drafts/Sealed", false, fDrafts.canAdd(Bulletin.STATUSSEALED));

		BulletinFolder fDiscarded = store.getFolderDiscarded();
		assertNotNull("Should have created Discarded folder", fDiscarded);
		assertEquals("Discarded/Draft", true, fDiscarded.canAdd(Bulletin.STATUSDRAFT));
		assertEquals("Discarded/Sealed", true, fDiscarded.canAdd(Bulletin.STATUSSEALED));

		BulletinFolder fDraftOutbox = store.getFolderDraftOutbox();
		assertNotNull("Should have created DraftOutbox folder", fDraftOutbox);
		assertEquals("Discarded/Draft", true, fDraftOutbox.canAdd(Bulletin.STATUSDRAFT));
		assertEquals("Discarded/Sealed", false, fDraftOutbox.canAdd(Bulletin.STATUSSEALED));

	}

	public void testFindFolder()
	{
		TRACE("testFindFolder");

		int count = store.getFolderCount();

		store.createFolder("peter");
		store.createFolder("paul");
		store.createFolder("john");
		store.createFolder("ringo");
		assertEquals(count+4, store.getFolderCount());

		BulletinFolder folder = store.findFolder("paul");
		assertEquals(false, (folder==null));
	}

	public void testRenameFolder()
	{
		TRACE("testRenameFolder");

		assertEquals(false, store.renameFolder("a", "b"));

		BulletinFolder folder = store.createFolder("a");
		assertEquals(true, store.renameFolder("a", "b"));
		assertEquals(null, store.findFolder("a"));
		assertEquals(folder, store.findFolder("b"));

		BulletinFolder f2 = store.createFolder("a");
		assertEquals(false, store.renameFolder("a", "b"));
		assertEquals(folder, store.findFolder("b"));
		assertEquals(f2, store.findFolder("a"));
		
		assertEquals("allowed rename to *?", false, store.renameFolder("a", "*a"));
		for(char c = ' '; c < '0'; ++c)
		{
			char[] illegalPrefixChars = {c};
			String illegalPrefix = new String(illegalPrefixChars);
			assertEquals("allowed rename to " + illegalPrefix + "?", false, store.renameFolder("a", illegalPrefix + "a"));
		}
	}

	public void testDeleteFolder()
	{
		TRACE("testDeleteFolder");

		assertEquals(false, store.deleteFolder("a"));
		BulletinFolder folder = store.createFolder("a");
		assertEquals(true, store.deleteFolder("a"));

		folder = store.createFolder("a");
		assertNotNull("Couldn't create folder a", folder);
		folder.preventDelete();
		assertEquals(false, store.deleteFolder("a"));
		folder = store.findFolder("a");
		assertNotNull("Should have been non-deletable", folder);

		folder = store.createFolder("b");
		assertNotNull("Couldn't create folder b", folder);
		Bulletin b = store.createEmptyBulletin();
		b.set("subject", "golly");
		store.saveBulletin(b);
		folder.add(b);
		assertEquals(true, folder.contains(b));
		store.deleteFolder("b");
		folder = store.getFolderDiscarded();
		assertEquals("B should be in discarded", true, folder.contains(b));
	}

	public void testMoveBulletin()
	{
		TRACE("testMoveBulletin");

		BulletinFolder folderA = store.createFolder("a");
		BulletinFolder folderB = store.createFolder("b");
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		assertEquals("not in a", false, folderA.contains(b));
		assertEquals("not in b", false, folderB.contains(b));

		store.moveBulletin(b, folderA, folderB);
		assertEquals("still not in a", false, folderA.contains(b));
		assertEquals("moved into b", true, folderB.contains(b));

		store.moveBulletin(b, folderB, folderA);
		assertEquals("now in a", true, folderA.contains(b));
		assertEquals("no longer in b", false, folderB.contains(b));

		store.moveBulletin(b, folderA, folderA);
		assertEquals("still in a", true, folderA.contains(b));
		assertEquals("still not in b", false, folderB.contains(b));

		store.moveBulletin(b, folderB, folderB);
		assertEquals("still in a", true, folderA.contains(b));
		assertEquals("still not in b again", false, folderB.contains(b));
	}

	public void testAddBulletinToFolder()
	{
		TRACE("testAddBulletinToFolder");

		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		UniversalId id = b.getUniversalId();
		BulletinFolder folder = store.createFolder("test");
		store.addBulletinToFolder(id, folder);
		assertEquals("now in folder", true, folder.contains(b));
		store.addBulletinToFolder(id, folder);
		assertEquals("still in folder", true, folder.contains(b));
		UniversalId bFakeId = UniversalId.createFromAccountAndPrefix("aa", "abc");
		store.addBulletinToFolder(bFakeId, folder);
		UniversalId badId2 = UniversalId.createDummyUniversalId();
		assertEquals("bad bulletin", -1, folder.find(badId2));

	}

	public void testFolderToXml()
	{
		TRACE("testFolderToXml");

		BulletinFolder folder = store.createFolder("Test");
		String xml = store.folderToXml(folder);
		assertEquals(MartusClientXml.getFolderTagStart("Test") + MartusClientXml.getFolderTagEnd(), xml);

		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		folder.add(b);
		xml = store.folderToXml(folder);
		assertStartsWith(MartusClientXml.getFolderTagStart("Test"), xml);
		assertContains(MartusXml.getIdTag(folder.getBulletinSorted(0).getUniversalIdString()), xml);
		assertEndsWith(MartusClientXml.getFolderTagEnd(), xml);
		
	}

	public void testFoldersToXml()
	{
		TRACE("testFoldersToXml");

		int i;
		String expected;

		expected = MartusClientXml.getFolderListTagStart();
		Vector originalFolderNames = store.getAllFolderNames();
		for(i = 0; i < originalFolderNames.size(); ++i)
		{
			BulletinFolder folder = store.findFolder((String)originalFolderNames.get(i));
			expected += store.folderToXml(folder);
		}
		expected += MartusClientXml.getFolderListTagEnd();
		assertEquals(expected, store.foldersToXml());

		BulletinFolder f1 = store.createFolder("First");
		Bulletin b = store.createEmptyBulletin();
		f1.add(b);

		expected = MartusClientXml.getFolderListTagStart();
		Vector updatedFolderNames = store.getAllFolderNames();
		for(i = 0; i < updatedFolderNames.size(); ++i)
		{
			BulletinFolder folder = store.findFolder((String)updatedFolderNames.get(i));
			expected += store.folderToXml(folder);
		}
		expected += MartusClientXml.getFolderListTagEnd();
		assertEquals(expected, store.foldersToXml());
	}

	public void testLoadXmlNoFolders()
	{
		TRACE("testLoadXmlNoFolders");

		int count = store.getFolderCount();
		String xml = "<Folder name='fromxml'></Folder>";
		store.loadFolders(new StringReader(xml));
		assertEquals(0, store.getBulletinCount());
		assertEquals(count+1, store.getFolderCount());
		assertNotNull("not found?", store.findFolder("fromxml"));
	}

	public void testLoadXmlFolders()
	{
		TRACE("testLoadXmlFolders");

		int count = store.getFolderCount();
		String xml = "<FolderList><Folder name='one'></Folder><Folder name='two'></Folder></FolderList>";
		store.loadFolders(new StringReader(xml));
		assertEquals(count+2, store.getFolderCount());
		assertNotNull("Folder one must exist", store.findFolder("one"));
		assertNotNull("Folder two must exist", store.findFolder("two"));
		assertNull("Folder three must not exist", store.findFolder("three"));
	}

	public void testLoadXmlLegacyFolders()
	{
		TRACE("testLoadXmlFolders");
		
		int count = store.getFolderCount();
		String xml = "<FolderList><Folder name='Outbox'></Folder><Folder name='new two'></Folder></FolderList>";
		assertTrue("Legacy folder didn't return true on load", store.loadFolders(new StringReader(xml)));
		assertEquals(count+1, store.getFolderCount());
		assertNotNull("Folder %OutBox must exist", store.findFolder("%OutBox"));
		assertNull("Folder Outbox must not exist", store.findFolder("Outbox"));
		assertNotNull("Folder two new must exist", store.findFolder("new two"));
		assertNull("Folder three must not exist", store.findFolder("three"));
		xml = "<FolderList><Folder name='%OutBox'></Folder><Folder name='new two'></Folder></FolderList>";
		assertFalse("Not Legacy folder didn't return false on load", store.loadFolders(new StringReader(xml)));
	}

	/* missing tests:
		- invalid xml (empty, badly nested tags, two root nodes)
		- <Id> not nested within <Folder>
		- <Field> not nested within <Bulletin>
		- <Folder> or <Bulletin> outside <FolderList> or <BulletinList>
		- Missing folder name attribute, bulletin id attribute, field name attribute
		- Empty bulletin id
		- Illegal bulletin id
		- Duplicate bulletin id
		- Folder id that is blank or isn't a bulletin
		- Folder name blank or duplicate
		- Bulletin field name isn't one of our predefined field names
		- Confirm that attributes are case-sensitive
	*/

	public void testDatabaseBulletins() throws Exception
	{
		TRACE("testDatabaseBulletins");
		
		assertEquals("empty", 0, store.getBulletinCount());

		Bulletin b = store.createEmptyBulletin();
		final String author = "Mr. Peabody";
		b.set(Bulletin.TAGAUTHOR, author);
		store.saveBulletin(b);
		store.saveFolders();
		assertEquals("saving", 1, store.getBulletinCount());
		assertEquals("keys", 3*store.getBulletinCount(), db.getRecordCount());

		BulletinStore newStoreSameDatabase = new BulletinStore(db);
		newStoreSameDatabase.setSignatureGenerator(store.getSignatureGenerator());
		newStoreSameDatabase.loadFolders();
		assertEquals("loaded", 1, newStoreSameDatabase.getBulletinCount());
		Bulletin b2 = newStoreSameDatabase.findBulletinByUniversalId(b.getUniversalId());
		assertEquals("id", b.getLocalId(), b2.getLocalId());
		assertEquals("author", b.get(Bulletin.TAGAUTHOR), b2.get(Bulletin.TAGAUTHOR));
		assertEquals("wrong security?", store.getSignatureGenerator(), b2.getSignatureGenerator());

	}

	public void testDatabaseFolders() throws Exception
	{
		TRACE("testDatabaseFolders");

		final String folderName = "Gotta work";
		int systemFolderCount = store.getFolderCount();
		BulletinFolder f = store.createFolder(folderName);
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		f.add(b);
		store.saveFolders();

		assertEquals("keys", 3*store.getBulletinCount(), db.getRecordCount());

		store = new BulletinStore(db);
		store.setSignatureGenerator(new MockMartusSecurity());
		assertEquals("before load", systemFolderCount, store.getFolderCount());
		store.loadFolders();
		assertEquals("loaded", 1+systemFolderCount, store.getFolderCount());
		BulletinFolder f2 = store.findFolder(folderName);
		assertNotNull("folder", f2);
		assertEquals("bulletins in folder", 1, f2.getBulletinCount());
		assertEquals("contains", true, f2.contains(b));
	}
	
	public void testLoadAllDataWithErrors() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		FieldDataPacket fdp = b.getFieldDataPacket();
		DatabaseKey headerKey = new DatabaseKey(b.getUniversalId());
		UniversalId.createFromAccountAndLocalId(b.getAccount(), fdp.getLocalId());

		security.fakeSigVerifyFailure = true;
		store.loadFolders();

		security.fakeSigVerifyFailure = false;
		store.loadFolders();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		byte[] bytes = out.toByteArray();

		bytes[0] = '!';
		String invalidPacketString = new String(bytes, "UTF-8");
		db.writeRecord(headerKey, invalidPacketString);
		store.loadFolders();
	}

	public void testClearFolder()
	{
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		BulletinFolder folder = store.createFolder("blah");
		folder.add(b1);
		folder.add(b2);
		assertEquals(store.getBulletinCount(), folder.getBulletinCount());
		store.clearFolder("blah");
		assertEquals(0, folder.getBulletinCount());
	}

	public void testAutomaticSaving() throws Exception
	{
		Database db = store.getDatabase();
		DatabaseKey foldersKey = new DatabaseKey(UniversalId.createDummyUniversalId());

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			store.saveBulletin(b);

			assertEquals("save bulletin f ", false, store.getFoldersFile().exists());

			DatabaseKey bulletinKey = new DatabaseKey(b.getUniversalId());
			assertNotNull("save bulletin b ", store.getDatabase().readRecord(bulletinKey, security));
		}

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			store.createFolder("a");
			store.saveFolders();
			assertTrue("createFolder f ", store.getFoldersFile().exists());
			DatabaseKey bulletinKey = new DatabaseKey(b.getUniversalId());
			assertNull("createFolder b ", store.getDatabase().readRecord(bulletinKey, security));
		}

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			BulletinFolder drafts = store.getFolderDrafts();
			store.addBulletinToFolder(b.getUniversalId(), drafts);

			store.getDatabase().deleteAllData();
			store.clearFolder(drafts.getName());
			assertTrue("clearFolder f ", store.getFoldersFile().exists());
			DatabaseKey bulletinKey = new DatabaseKey(b.getUniversalId());
			assertNull("clearFolder b ", store.getDatabase().readRecord(bulletinKey, security));

			store.saveBulletin(b);
			store.destroyBulletin(b);
			assertNull("destroyBulletin b ", store.getDatabase().readRecord(bulletinKey, security));
		}

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			store.createFolder("x");
			db.discardRecord(foldersKey);
			store.renameFolder("x", "b");
			assertTrue("renameFolder f ", store.getFoldersFile().exists());
			DatabaseKey bulletinKey = new DatabaseKey(b.getUniversalId());
			assertNull("renameFolder b ", store.getDatabase().readRecord(bulletinKey, security));
		}

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			store.createFolder("z");
			db.discardRecord(foldersKey);
			store.deleteFolder("z");
			assertTrue("deleteFolder f ", store.getFoldersFile().exists());
			DatabaseKey bulletinKey = new DatabaseKey(b.getUniversalId());
			assertNull("deleteFolder b ", store.getDatabase().readRecord(bulletinKey, security));
		}
	}
	
	public void testImportZipFileWithAttachmentSealed() throws Exception
	{
		Bulletin original = store.createEmptyBulletin();
		DatabaseKey originalKey = new DatabaseKey(original.getUniversalId());
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.set(Bulletin.TAGTITLE, "abbc");
		original.set(Bulletin.TAGPRIVATEINFO, "priv");
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.setSealed();
		BulletinSaver.saveToDatabase(original, db, store.mustEncryptPublicData(), store.getSignatureGenerator());
		File zipFile = File.createTempFile("$$$MartusTestZipSealed", null);
		zipFile.deleteOnExit();
		Bulletin loaded = BulletinLoader.loadFromDatabase(store, originalKey);
		MockBulletin.saveToFile(db,loaded, zipFile, store.getSignatureVerifier());
		store.deleteAllData();
		assertEquals("still a record?", 0, db.getRecordCount());
		
		store.importZipFileToStoreWithSameUids(zipFile);
		assertEquals("Packet count incorrect", 5, db.getRecordCount());		
		
		DatabaseKey headerKey = new DatabaseKey(loaded.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey = new DatabaseKey(loaded.getFieldDataPacket().getUniversalId());;
		DatabaseKey privateKey = new DatabaseKey(loaded.getPrivateFieldDataPacket().getUniversalId());;
		AttachmentProxy gotAttachment = loaded.getPublicAttachments()[0];
		DatabaseKey attachmentKey = new DatabaseKey(gotAttachment.getUniversalId());;
		AttachmentProxy gotPrivateAttachment = loaded.getPrivateAttachments()[0];
		DatabaseKey attachmentPrivateKey = new DatabaseKey(gotPrivateAttachment.getUniversalId());;

		assertTrue("Header Packet missing", db.doesRecordExist(headerKey));
		assertTrue("Data Packet missing", db.doesRecordExist(dataKey));
		assertTrue("Private Packet missing", db.doesRecordExist(privateKey));
		assertTrue("Attachment Packet missing", db.doesRecordExist(attachmentKey));
		assertTrue("Attachment Private Packet missing", db.doesRecordExist(attachmentPrivateKey));
		
		Bulletin reloaded = BulletinLoader.loadFromDatabase(store, originalKey);
		assertEquals("public?", original.get(Bulletin.TAGTITLE), reloaded.get(Bulletin.TAGTITLE));
		assertEquals("private?", original.get(Bulletin.TAGPRIVATEINFO), reloaded.get(Bulletin.TAGPRIVATEINFO));
		
		File tempRawFilePublic = File.createTempFile("$$$MartusTestImpSealedZipRawPublic",null);
		tempRawFilePublic.deleteOnExit();
		BulletinSaver.extractAttachmentToFile(db, reloaded.getPublicAttachments()[0], security, tempRawFilePublic);
		byte[] rawBytesPublic = new byte[sampleBytes1.length];
		FileInputStream in = new FileInputStream(tempRawFilePublic);
		in.read(rawBytesPublic);
		in.close();
		assertEquals("wrong bytes", true, Arrays.equals(sampleBytes1, rawBytesPublic));

		File tempRawFilePrivate = File.createTempFile("$$$MartusTestImpSealedZipRawPrivate",null);
		tempRawFilePrivate.deleteOnExit();
		BulletinSaver.extractAttachmentToFile(db, reloaded.getPrivateAttachments()[0], security, tempRawFilePrivate);
		byte[] rawBytesPrivate = new byte[sampleBytes2.length];
		FileInputStream in2 = new FileInputStream(tempRawFilePrivate);
		in2.read(rawBytesPrivate);
		in2.close();
		assertEquals("wrong Private bytes", true, Arrays.equals(sampleBytes2, rawBytesPrivate));
	}

	public void testImportZipFileBulletin() throws Exception
	{
		File tempFile = createTempFile();

		Bulletin b = store.createEmptyBulletin();
		MockBulletin.saveToFile(db,b, tempFile, store.getSignatureVerifier());
		
		BulletinFolder folder = store.createFolder("test");
		folder.setStatusAllowed(Bulletin.STATUSSEALED);
		try
		{
			store.importZipFileBulletin(tempFile, folder, false);
			fail("allowed illegal import?");
		}
		catch(BulletinStore.StatusNotAllowedException ignoreExpectedException)
		{
		}
		assertEquals("imported even though the folder prevented it?", 0, store.getBulletinCount());
		
		folder.setStatusAllowed(null);
		store.importZipFileBulletin(tempFile, folder, false);
		assertEquals("not imported to store?", 1, store.getBulletinCount());
		assertEquals("not imported to folder?", 1, folder.getBulletinCount());
		assertNull("resaved with draft id?", store.findBulletinByUniversalId(b.getUniversalId()));
		
		store.deleteAllData();
		folder = store.createFolder("test2");

		b.setSealed();
		MockBulletin.saveToFile(db,b, tempFile, store.getSignatureVerifier());
		store.importZipFileBulletin(tempFile, folder, false);
		assertEquals("not imported to store?", 1, store.getBulletinCount());
		assertEquals("not imported to folder count?", 1, folder.getBulletinCount());
		assertEquals("not imported to folder uid?", 0, folder.find(b.getUniversalId()));
		assertNotNull("not saved with sealed id?", store.findBulletinByUniversalId(b.getUniversalId()));
		
		BulletinFolder folder2 = store.createFolder("another");
		store.importZipFileBulletin(tempFile, folder2, false);
		assertEquals("imported to store again?", 1, store.getBulletinCount());
		assertEquals("not imported to another folder uid?", 0, folder2.find(b.getUniversalId()));
	}
	
	public void testImportZipFileBulletinNotMine() throws Exception
	{
		File tempFile = createTempFile();

		Bulletin original = store.createEmptyBulletin();
		MockBulletin.saveToFile(db,original, tempFile, store.getSignatureVerifier());

		BulletinStore importer = createTempStore();
		BulletinFolder folder = importer.createFolder("test");
		importer.importZipFileBulletin(tempFile, folder, false);
		
		Bulletin imported = folder.getBulletinSorted(0);
		assertEquals("changed uid?", original.getUniversalId(), imported.getUniversalId());
	}
	
	public void testImportZipFileFieldOffice() throws Exception
	{
		File tempFile = createTempFile();

		BulletinStore hqStore = createTempStore();
		
		Bulletin original = store.createEmptyBulletin();
		original.setHQPublicKey(hqStore.getAccountId());
		original.setSealed();
		MockBulletin.saveToFile(db,original, tempFile, store.getSignatureVerifier());
		
		BulletinFolder folder = hqStore.createFolder("test");
		hqStore.importZipFileBulletin(tempFile, folder, false);
		
		Bulletin imported = folder.getBulletinSorted(0);
		assertEquals("changed uid?", original.getUniversalId(), imported.getUniversalId());
	}

	public void testImportZipFileBulletinToOutbox() throws Exception
	{
		BulletinStore creator = createTempStore();
		Bulletin b = creator.createEmptyBulletin();
		b.setSealed();

		File tempFile = File.createTempFile("$$$MartusTestStoreImportZip", null);
		tempFile.deleteOnExit();
		MockBulletin.saveToFile(db,b, tempFile, store.getSignatureVerifier());
		
		creator.importZipFileBulletin(tempFile, creator.getFolderOutbox(), false);
		assertEquals("Didn't fully import?", 1, creator.getBulletinCount());
		
		MockMartusApp thisApp = MockMartusApp.create();
		try
		{
			thisApp.getStore().importZipFileBulletin(tempFile, thisApp.getFolderOutbox(), false);
			fail("allowed illegal import?");
		}
		catch(BulletinStore.StatusNotAllowedException ignoreExpectedException)
		{
		}
		assertEquals("imported even though the folder prevented it?", 0, thisApp.getStore().getBulletinCount());
	}

	public void testImportDraftZipFile() throws Exception
	{
		File tempFile = createTempFile();

		Bulletin b = store.createEmptyBulletin();
		MockBulletin.saveToFile(db,b, tempFile, store.getSignatureVerifier());
		UniversalId originalUid = b.getUniversalId();
		
		BulletinFolder folder = store.createFolder("test");
		store.importZipFileBulletin(tempFile, folder, true);
		assertEquals("Didn't fully import?", 1, store.getBulletinCount());
		assertNotNull("Not same ID?", store.findBulletinByUniversalId(originalUid));

		store.importZipFileBulletin(tempFile, folder, false);
		assertEquals("Not different IDs?", 2, store.getBulletinCount());
		
	}

	public void testImportZipFileWithAttachmentDraft() throws Exception
	{
		Bulletin original = store.createEmptyBulletin();
		DatabaseKey originalKey = new DatabaseKey(original.getUniversalId());
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.set(Bulletin.TAGTITLE, "abc");
		original.set(Bulletin.TAGPRIVATEINFO, "private");
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		BulletinSaver.saveToDatabase(original, db, store.mustEncryptPublicData(), store.getSignatureGenerator());
		
		Bulletin loaded = BulletinLoader.loadFromDatabase(store, originalKey);

		File zipFile = File.createTempFile("$$$MartusTestZipDraft", null);
		zipFile.deleteOnExit();
		MockBulletin.saveToFile(db,loaded, zipFile, store.getSignatureVerifier());
		
		store.deleteAllData();
		assertEquals("still a record?", 0, db.getRecordCount());
		
		UniversalId savedAsId = store.importZipFileToStoreWithNewUids(zipFile);
		assertEquals("record count not 5?", 5, db.getRecordCount());		
		
		DatabaseKey headerKey = new DatabaseKey(loaded.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey = new DatabaseKey(loaded.getFieldDataPacket().getUniversalId());;
		DatabaseKey privateKey = new DatabaseKey(loaded.getPrivateFieldDataPacket().getUniversalId());;
		AttachmentProxy gotAttachment = loaded.getPublicAttachments()[0];
		AttachmentProxy gotAttachmentPrivate = loaded.getPrivateAttachments()[0];
		DatabaseKey attachmentKey = new DatabaseKey(gotAttachment.getUniversalId());;
		DatabaseKey attachmentPrivateKey = new DatabaseKey(gotAttachmentPrivate.getUniversalId());;

		assertEquals("Header Packet present?", false, db.doesRecordExist(headerKey));
		assertEquals("Data Packet present?", false, db.doesRecordExist(dataKey));
		assertEquals("Private Packet present?", false, db.doesRecordExist(privateKey));
		assertEquals("Attachment Public Packet present?", false, db.doesRecordExist(attachmentKey));
		assertEquals("Attachment Private Packet present?", false, db.doesRecordExist(attachmentPrivateKey));

		Bulletin reloaded = BulletinLoader.loadFromDatabase(store, new DatabaseKey(savedAsId));
		
		assertEquals("public?", original.get(Bulletin.TAGTITLE), reloaded.get(Bulletin.TAGTITLE));
		assertEquals("private?", original.get(Bulletin.TAGPRIVATEINFO), reloaded.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("attachment", true, db.doesRecordExist(new DatabaseKey(reloaded.getPublicAttachments()[0].getUniversalId())));
		assertEquals("attachment Private", true, db.doesRecordExist(new DatabaseKey(reloaded.getPrivateAttachments()[0].getUniversalId())));
		
		File tempRawFile = File.createTempFile("$$$MartusTestImpDraftZipRaw",null);
		tempRawFile.deleteOnExit();
		BulletinSaver.extractAttachmentToFile(db, reloaded.getPublicAttachments()[0], security, tempRawFile);
		
		byte[] rawBytes = new byte[sampleBytes1.length];
		FileInputStream in = new FileInputStream(tempRawFile);
		in.read(rawBytes);
		in.close();
		assertEquals("wrong bytes", true, Arrays.equals(sampleBytes1,rawBytes));

		File tempRawFilePrivate = File.createTempFile("$$$MartusTestImpDraftZipRawPrivate",null);
		tempRawFilePrivate.deleteOnExit();
		BulletinSaver.extractAttachmentToFile(db, reloaded.getPrivateAttachments()[0], security, tempRawFilePrivate);
		
		byte[] rawBytesPrivate = new byte[sampleBytes2.length];
		FileInputStream in2 = new FileInputStream(tempRawFilePrivate);
		in2.read(rawBytesPrivate);
		in2.close();
		assertEquals("wrong bytes Private", true, Arrays.equals(sampleBytes2, rawBytesPrivate));
	}
	
	public void testCanPutBulletinInFolder() throws Exception
	{
		Bulletin b1 = store.createEmptyBulletin();
		BulletinFolder outbox = store.getFolderOutbox();
		BulletinFolder discardedbox = store.getFolderDiscarded();
		assertEquals("draft b1 got put in outbox?", false, store.canPutBulletinInFolder(outbox, b1.getAccount(), b1.getStatus()));

		BulletinStore store2 = createTempStore();
		Bulletin b2 = store2.createEmptyBulletin();
		b2.setSealed();
		assertEquals("sealed b2 from another account got put in outbox?", false, store.canPutBulletinInFolder(outbox, b2.getAccount(), b2.getStatus()));
		assertEquals("sealed b2 from another account can't be put in discarded?", true, store.canPutBulletinInFolder(discardedbox, b2.getAccount(), b2.getStatus()));
	}

	public void testGetSetOfAllBulletinUniversalIds()
	{
		Set emptySet = store.getSetOfAllBulletinUniversalIds();
		assertTrue("not empty to start?", emptySet.isEmpty());
		
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		Set two = store.getSetOfAllBulletinUniversalIds();
		assertEquals("not two?", 2, two.size());
		assertTrue("Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("Missing b2?", two.contains(b2.getUniversalId()));
	}
	
	public void testGetSetOfBulletinUniversalIdsInFolders()
	{
		Set emptySet = store.getSetOfBulletinUniversalIdsInFolders();
		assertTrue("not empty to start?", emptySet.isEmpty());

		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		Set stillEmptySet = store.getSetOfBulletinUniversalIdsInFolders();
		assertTrue("not still empty", stillEmptySet.isEmpty());

		store.getFolderDrafts().add(b1);
		store.getFolderDiscarded().add(b1);
		store.getFolderDiscarded().add(b2);
		Set two = store.getSetOfBulletinUniversalIdsInFolders();
		
		assertEquals("not two?", 2, two.size());
		assertTrue("Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("Missing b2?", two.contains(b2.getUniversalId()));
	}
	
	public void testGetSetOfOrphanedBulletinUniversalIds()
	{
		Set emptySet = store.getSetOfOrphanedBulletinUniversalIds();
		assertTrue("not empty to start?", emptySet.isEmpty());

		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);

		Set two = store.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not two?", 2, two.size());
		assertTrue("two Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("two Missing b2?", two.contains(b2.getUniversalId()));

		store.getFolderDrafts().add(b1);
		store.getFolderDiscarded().add(b1);
		Set one = store.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not one?", 1, one.size());
		assertTrue("one Missing b2?", one.contains(b2.getUniversalId()));
		
		store.getFolderDiscarded().add(b2);
		Set emptyAgain = store.getSetOfOrphanedBulletinUniversalIds();
		assertTrue("not empty again?", emptyAgain.isEmpty());

	}
	
	public void testOrphansInHiddenFolders()
	{
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		
		store.getFolderDraftOutbox().add(b1);
		assertEquals("hidden-only not an orphan?", true, store.isOrphan(b1));

		store.getFolderDrafts().add(b2);
		store.getFolderDraftOutbox().add(b2);
		assertEquals("hidden-plus is an orphan?", false, store.isOrphan(b2));
	}

	public void testQuarantineUnreadableBulletinsSimple() throws Exception
	{
		assertEquals("found a bad bulletin in an empty database?", 0, store.quarantineUnreadableBulletins());
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		assertEquals("quarantined a good record?", 0, store.quarantineUnreadableBulletins());
		corruptBulletinHeader(b1);
		assertEquals("didn't claim to quarantine 1 record?", 1, store.quarantineUnreadableBulletins());
		DatabaseKey key = new DatabaseKey(b1.getUniversalId());
		assertTrue("didn't actually quarantine our record?", store.getDatabase().isInQuarantine(key));
	}
	
	public void testQuarantineUnreadableBulletinsMany() throws Exception
	{
		final int totalCount = 20;
		Bulletin bulletins[] = new Bulletin[totalCount];
		for (int i = 0; i < bulletins.length; i++) 
		{
			bulletins[i] = store.createEmptyBulletin();
			store.saveBulletin(bulletins[i]);
		}

		final int badCount = 4;
		DatabaseKey badKeys[] = new DatabaseKey[badCount];
		for (int i = 0; i < badKeys.length; i++) 
		{
			int bulletinIndex = i * (totalCount/badCount);
			Bulletin b = bulletins[bulletinIndex];
			badKeys[i] = new DatabaseKey(b.getUniversalId());
			corruptBulletinHeader(b);
		}

		assertEquals("wrong quarantine count?", badCount, store.quarantineUnreadableBulletins());
		for (int i = 0; i < badKeys.length; i++) 
			assertTrue("didn't quarantine " + i, store.getDatabase().isInQuarantine(badKeys[i]));
	}
	
	private void corruptBulletinHeader(Bulletin b) throws Exception
	{
		UniversalId uid = b.getUniversalId();
		DatabaseKey key = new DatabaseKey(uid);
		Database db = store.getDatabase();
		String goodData = db.readRecord(key, security);
		String badData = "x" + goodData;
		db.writeRecord(key, badData);
	}
		
	private BulletinStore createTempStore() throws Exception 
	{
		MartusSecurity tempSecurity = new MockMartusSecurity();
		tempSecurity.createKeyPair();
		BulletinStore tempStore = new BulletinStore(new MockClientDatabase());
		tempStore.setSignatureGenerator(tempSecurity);
		return tempStore;
	}
	
	private String getFieldEntity(Bulletin b, String fieldName)
	{
		return MartusXml.getFieldTagStart(fieldName) + b.get(fieldName) + MartusXml.getFieldTagEnd();
	}

	final int sampleRecordCount = 5;

	static BulletinStore store;
	static MockMartusSecurity security;
	static MockDatabase db;

	static File tempFile1;
	static File tempFile2;
	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
}
