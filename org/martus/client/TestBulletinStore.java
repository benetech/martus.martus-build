package org.martus.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;

import org.martus.common.AttachmentProxy;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusXml;
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
			tempFile1 = createSampleFile(sampleBytes1);
			tempFile2 = createSampleFile(sampleBytes2);
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
		b.save();
		Vector one = store.getAllBulletinUids();
		assertEquals("not one?", 1, one.size());
		UniversalId gotUid = (UniversalId)one.get(0);
		UniversalId bUid = b.getUniversalId();
		assertEquals("wrong uid 1?", bUid, gotUid);

		Bulletin b2 = store.createEmptyBulletin();
		b2.save();
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
		b.save();
		Vector one = new BulletinUidCollector(store).uids;
		assertEquals("not one?", 1, one.size());
		UniversalId gotUid = (UniversalId)one.get(0);
		UniversalId bUid = b.getUniversalId();
		assertEquals("wrong uid 1?", bUid, gotUid);

		Bulletin b2 = store.createEmptyBulletin();
		b2.save();
		Vector two = new BulletinUidCollector(store).uids;
		assertEquals("not two?", 2, two.size());
		assertTrue("missing 1?", two.contains(b.getUniversalId()));
		assertTrue("missing 2?", two.contains(b2.getUniversalId()));
	}
	
	public void testCaching()
	{
		int numBulletins = store.maxCachedBulletinCount + 1;
		for(int i = 0; i < numBulletins; ++i)
		{
			Bulletin b = store.createEmptyBulletin();
			b.save();
			store.findBulletinByUniversalId(b.getUniversalId());
		}
		
		assertEquals("cache too large?", true, store.bulletinCache.size() <= store.maxCachedBulletinCount);
	}

	public void testDestroyBulletin()
	{
		TRACE("testDestroyBulletin");

		Bulletin b = store.createEmptyBulletin();
		b.save();
		BulletinFolder f = store.createFolder("test");
		f.add(b.getUniversalId());
		store.destroyBulletin(b);
		assertEquals(0, store.getBulletinCount());
		assertEquals(0, f.getBulletinCount());
	}

	public void testSaveBulletin() throws Exception
	{
		TRACE("testSaveBulletin");

		assertEquals(0, store.getBulletinCount());

		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, "New bulletin");
		store.saveBulletin(b);
		UniversalId uId = b.getUniversalId();
		assertEquals(1, store.getBulletinCount());
		assertEquals(false, (uId.toString().length() == 0));
		assertEquals("not saved initially?", "New bulletin", store.findBulletinByUniversalId(uId).get(Bulletin.TAGSUMMARY));

		// re-saving the same bulletin replaces the old one
		UniversalId id = b.getUniversalId();
		store.saveBulletin(b);
		assertEquals(1, store.getBulletinCount());
		assertEquals("Saving should keep same id", id, b.getUniversalId());
		assertEquals("not still saved?", "New bulletin", store.findBulletinByUniversalId(uId).get(Bulletin.TAGSUMMARY));

		// saving a bulletin with the same id replaces the old one
		Bulletin b2 = new Bulletin(b);
		b2.set(Bulletin.TAGSUMMARY, "Replaced bulletin");
		store.saveBulletin(b2);
		assertEquals(1, store.getBulletinCount());
		assertEquals(id, b2.getUniversalId());
		assertEquals("not replaced?", "Replaced bulletin", store.findBulletinByUniversalId(uId).get(Bulletin.TAGSUMMARY));

		// unsaved bulletin changes should not be in the store
		b.set(Bulletin.TAGSUMMARY, "not saved yet");
		assertEquals("saved without asking?", "Replaced bulletin", store.findBulletinByUniversalId(uId).get(Bulletin.TAGSUMMARY));

		// saving a new bulletin with a non-empty id should retain that id
		int oldCount = store.getBulletinCount();
		b = store.createEmptyBulletin();
		UniversalId uid = b.getBulletinHeaderPacket().getUniversalId();
		b.save();
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
		b.save();
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
		start1.save();
		f.add(start1.getUniversalId());
		
		Bulletin b = f.getBulletinSorted(0);
		assertNotNull("Sent folder should have bulletins", b);

		assertEquals(true, f.contains(b));
		assertEquals(false, discarded.contains(b));
		store.discardBulletin(f, b);
		assertEquals("Bulletin wasn't discarded!", false, f.contains(b));
		assertEquals("Bulletin wasn't copied to Discarded", true, discarded.contains(b));

		Bulletin b2 = store.createEmptyBulletin();
		b2.set("subject", "amazing");
		b2.save();
		BulletinFolder user1 = store.createFolder("1");
		BulletinFolder user2 = store.createFolder("2");
		user1.add(b2.getUniversalId());
		user2.add(b2.getUniversalId());

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
		b1.save();
		f.add(b1.getUniversalId());
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

	public void testGetFolder()
	{
		TRACE("testGetFolder");

		int count = store.getFolderCount();

		BulletinFolder f1 = store.createFolder("testing");
		assertEquals(count+1, store.getFolderCount());

		BulletinFolder f2 = store.getFolder(count);
		assertEquals(f1, f2);

		assertEquals(null, store.getFolder(-1));
		assertEquals(null, store.getFolder(store.getFolderCount()));

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
		b.save();
		folder.add(b.getUniversalId());
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
		b.save();
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
		b.save();
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
		assertEquals(MartusXml.getFolderTagStart("Test") + MartusXml.getFolderTagEnd(), xml);

		Bulletin b = store.createEmptyBulletin();
		b.save();
		folder.add(b.getUniversalId());
		xml = store.folderToXml(folder);
		assertStartsWith(MartusXml.getFolderTagStart("Test"), xml);
		assertContains(MartusXml.getIdTag(folder.getBulletinSorted(0).getUniversalIdString()), xml);
		assertEndsWith(MartusXml.getFolderTagEnd(), xml);
	}

	public void testFoldersToXml()
	{
		TRACE("testFoldersToXml");

		int i;
		String expected;

		expected = MartusXml.getFolderListTagStart();
		for(i = 0; i < store.getFolderCount(); ++i)
			expected += store.folderToXml(store.getFolder(i));
		expected += MartusXml.getFolderListTagEnd();
		assertEquals(expected, store.foldersToXml());

		BulletinFolder f1 = store.createFolder("First");
		Bulletin b = store.createEmptyBulletin();
		f1.add(b.getUniversalId());

		expected = MartusXml.getFolderListTagStart();
		for(i = 0; i < store.getFolderCount(); ++i)
			expected += store.folderToXml(store.getFolder(i));
		expected += MartusXml.getFolderListTagEnd();
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
		assertEquals("fromxml", store.getFolder(count).getName());
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
		b.set(b.TAGAUTHOR, author);
		b.save();
		store.saveFolders();
		assertEquals("saving", 1, store.getBulletinCount());
		assertEquals("keys", 3*store.getBulletinCount(), db.getSealedRecordCount());

		BulletinStore newStoreSameDatabase = new BulletinStore(db);
		newStoreSameDatabase.setSignatureGenerator(store.getSignatureGenerator());
		newStoreSameDatabase.loadFolders();
		assertEquals("loaded", 1, newStoreSameDatabase.getBulletinCount());
		Bulletin b2 = newStoreSameDatabase.findBulletinByUniversalId(b.getUniversalId());
		assertEquals("id", b.getLocalId(), b2.getLocalId());
		assertEquals("author", b.get(b.TAGAUTHOR), b2.get(b2.TAGAUTHOR));
		assertEquals("Store is null", newStoreSameDatabase, b2.getStore());

	}

	public void testDatabaseFolders() throws Exception
	{
		TRACE("testDatabaseFolders");

		final String folderName = "Gotta work";
		int systemFolderCount = store.getFolderCount();
		BulletinFolder f = store.createFolder(folderName);
		Bulletin b = store.createEmptyBulletin();
		b.save();
		f.add(b.getUniversalId());
		store.saveFolders();

		assertEquals("keys", 3*store.getBulletinCount(), db.getSealedRecordCount());

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
		b.save();
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		FieldDataPacket fdp = b.getFieldDataPacket();
		DatabaseKey headerKey = new DatabaseKey(b.getUniversalId());
		UniversalId dataUid = UniversalId.createFromAccountAndLocalId(b.getAccount(), fdp.getLocalId());
		DatabaseKey dataKey = new DatabaseKey(dataUid);
		int packetCount = db.getSealedRecordCount();

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
		b1.save();
		Bulletin b2 = store.createEmptyBulletin();
		b2.save();
		BulletinFolder folder = store.createFolder("blah");
		folder.add(b1.getUniversalId());
		folder.add(b2.getUniversalId());
		assertEquals(store.getBulletinCount(), folder.getBulletinCount());
		store.clearFolder("blah");
		assertEquals(0, folder.getBulletinCount());
	}

	public void testAutomaticSaving() throws Exception
	{
		Database db = store.getDatabase();
		DatabaseKey foldersKey = new DatabaseKey("-folders");

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();

			b.save();
			assertEquals("save bulletin f ", false, store.getFoldersFile().exists());

			DatabaseKey bulletinKey = new DatabaseKey(b.getUniversalId());
			assertNotNull("save bulletin b ", store.getDatabase().readRecord(bulletinKey, security));
		}

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			BulletinFolder folder1 = store.createFolder("a");
			store.saveFolders();
			assertTrue("createFolder f ", store.getFoldersFile().exists());
			DatabaseKey bulletinKey = new DatabaseKey(b.getLocalId());
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
			DatabaseKey bulletinKey = new DatabaseKey(b.getLocalId());
			assertNull("clearFolder b ", store.getDatabase().readRecord(bulletinKey, security));

			b.save();
			store.destroyBulletin(b);
			assertNull("destroyBulletin b ", store.getDatabase().readRecord(bulletinKey, security));
		}

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			BulletinFolder folder2 = store.createFolder("x");

			db.discardRecord(foldersKey);
			store.renameFolder("x", "b");
			assertTrue("renameFolder f ", store.getFoldersFile().exists());
			DatabaseKey bulletinKey = new DatabaseKey(b.getLocalId());
			assertNull("renameFolder b ", store.getDatabase().readRecord(bulletinKey, security));
		}

		{
			store.deleteAllData();
			Bulletin b = store.createEmptyBulletin();
			BulletinFolder folder2 = store.createFolder("z");

			db.discardRecord(foldersKey);
			store.deleteFolder("z");
			assertTrue("deleteFolder f ", store.getFoldersFile().exists());
			DatabaseKey bulletinKey = new DatabaseKey(b.getLocalId());
			assertNull("deleteFolder b ", store.getDatabase().readRecord(bulletinKey, security));
		}
	}
	
	public void testImportZipFileWithAttachmentSealed() throws Exception
	{
		Bulletin original = store.createEmptyBulletin();
		DatabaseKey originalKey = new DatabaseKey(original.getUniversalId());
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.set(original.TAGTITLE, "abbc");
		original.set(original.TAGPRIVATEINFO, "priv");
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.setSealed();
		original.saveToDatabase(db);
		File zipFile = File.createTempFile("$$$MartusTestZipSealed", null);
		zipFile.deleteOnExit();
		Bulletin loaded = Bulletin.loadFromDatabase(store, originalKey);
		loaded.saveToFile(zipFile);
		store.deleteAllData();
		assertEquals("still a record?", 0, db.getSealedRecordCount());
		
		store.importZipFileToStoreWithSameUids(zipFile);
		assertEquals("Packet count incorrect", 5, db.getSealedRecordCount());		
		
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
		
		Bulletin reloaded = Bulletin.loadFromDatabase(store, originalKey);
		assertEquals("public?", original.get(original.TAGTITLE), reloaded.get(reloaded.TAGTITLE));
		assertEquals("private?", original.get(original.TAGPRIVATEINFO), reloaded.get(reloaded.TAGPRIVATEINFO));
		
		File tempRawFilePublic = File.createTempFile("$$$MartusTestImpSealedZipRawPublic",null);
		tempRawFilePublic.deleteOnExit();
		reloaded.extractAttachmentToFile(reloaded.getPublicAttachments()[0], security, tempRawFilePublic);
		byte[] rawBytesPublic = new byte[sampleBytes1.length];
		FileInputStream in = new FileInputStream(tempRawFilePublic);
		in.read(rawBytesPublic);
		in.close();
		assertEquals("wrong bytes", true, Arrays.equals(sampleBytes1, rawBytesPublic));

		File tempRawFilePrivate = File.createTempFile("$$$MartusTestImpSealedZipRawPrivate",null);
		tempRawFilePrivate.deleteOnExit();
		reloaded.extractAttachmentToFile(reloaded.getPrivateAttachments()[0], security, tempRawFilePrivate);
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
		b.saveToFile(tempFile);
		
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
		b.saveToFile(tempFile);
		store.importZipFileBulletin(tempFile, folder, false);
		assertEquals("not imported to store?", 1, store.getBulletinCount());
		assertEquals("not imported to folder?", 1, folder.getBulletinCount());
		assertNotNull("not saved with sealed id?", store.findBulletinByUniversalId(b.getUniversalId()));
		
	}
	
	public void testImportZipFileBulletinNotMine() throws Exception
	{
		File tempFile = createTempFile();

		Bulletin original = store.createEmptyBulletin();
		original.saveToFile(tempFile);

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
		original.saveToFile(tempFile);
		
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
		b.saveToFile(tempFile);
		
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
		b.saveToFile(tempFile);
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
		original.set(original.TAGTITLE, "abc");
		original.set(original.TAGPRIVATEINFO, "private");
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.saveToDatabase(db);
		
		Bulletin loaded = Bulletin.loadFromDatabase(store, originalKey);

		File zipFile = File.createTempFile("$$$MartusTestZipDraft", null);
		zipFile.deleteOnExit();
		loaded.saveToFile(zipFile);
		
		store.deleteAllData();
		assertEquals("still a record?", 0, db.getSealedRecordCount());
		
		UniversalId savedAsId = store.importZipFileToStoreWithNewUids(zipFile);
		assertEquals("record count not 5?", 5, db.getSealedRecordCount());		
		
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

		Bulletin reloaded = Bulletin.loadFromDatabase(store, new DatabaseKey(savedAsId));
		
		assertEquals("public?", original.get(original.TAGTITLE), reloaded.get(reloaded.TAGTITLE));
		assertEquals("private?", original.get(original.TAGPRIVATEINFO), reloaded.get(reloaded.TAGPRIVATEINFO));
		assertEquals("attachment", true, db.doesRecordExist(new DatabaseKey(reloaded.getPublicAttachments()[0].getUniversalId())));
		assertEquals("attachment Private", true, db.doesRecordExist(new DatabaseKey(reloaded.getPrivateAttachments()[0].getUniversalId())));
		
		File tempRawFile = File.createTempFile("$$$MartusTestImpDraftZipRaw",null);
		tempRawFile.deleteOnExit();
		reloaded.extractAttachmentToFile(reloaded.getPublicAttachments()[0], security, tempRawFile);
		
		byte[] rawBytes = new byte[sampleBytes1.length];
		FileInputStream in = new FileInputStream(tempRawFile);
		in.read(rawBytes);
		in.close();
		assertEquals("wrong bytes", true, Arrays.equals(sampleBytes1,rawBytes));

		File tempRawFilePrivate = File.createTempFile("$$$MartusTestImpDraftZipRawPrivate",null);
		tempRawFilePrivate.deleteOnExit();
		reloaded.extractAttachmentToFile(reloaded.getPrivateAttachments()[0], security, tempRawFilePrivate);
		
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
		b1.save();
		Bulletin b2 = store.createEmptyBulletin();
		b2.save();
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
		b1.save();
		Bulletin b2 = store.createEmptyBulletin();
		b2.save();
		Set stillEmptySet = store.getSetOfBulletinUniversalIdsInFolders();
		assertTrue("not still empty", stillEmptySet.isEmpty());

		store.getFolderDrafts().add(b1.getUniversalId());
		store.getFolderDiscarded().add(b1.getUniversalId());
		store.getFolderDiscarded().add(b2.getUniversalId());
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
		b1.save();
		Bulletin b2 = store.createEmptyBulletin();
		b2.save();

		Set two = store.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not two?", 2, two.size());
		assertTrue("two Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("two Missing b2?", two.contains(b2.getUniversalId()));

		store.getFolderDrafts().add(b1.getUniversalId());
		store.getFolderDiscarded().add(b1.getUniversalId());
		Set one = store.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not one?", 1, one.size());
		assertTrue("one Missing b2?", one.contains(b2.getUniversalId()));
		
		store.getFolderDiscarded().add(b2.getUniversalId());
		Set emptyAgain = store.getSetOfOrphanedBulletinUniversalIds();
		assertTrue("not empty again?", emptyAgain.isEmpty());

	}
	
	public void testOrphansInHiddenFolders()
	{
		Bulletin b1 = store.createEmptyBulletin();
		b1.save();
		Bulletin b2 = store.createEmptyBulletin();
		b2.save();
		
		store.getFolderDraftOutbox().add(b1.getUniversalId());
		assertEquals("hidden-only not an orphan?", true, store.isOrphan(b1));

		store.getFolderDrafts().add(b2.getUniversalId());
		store.getFolderDraftOutbox().add(b2.getUniversalId());
		assertEquals("hidden-plus is an orphan?", false, store.isOrphan(b2));
	}

	public void testQuarantineUnreadableBulletinsSimple() throws Exception
	{
		assertEquals("found a bad bulletin in an empty database?", 0, store.quarantineUnreadableBulletins());
		Bulletin b1 = store.createEmptyBulletin();
		b1.save();
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
			bulletins[i].save();
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
		Database db = b.getStore().getDatabase();
		String goodData = db.readRecord(key, security);
		String badData = "x" + goodData;
		db.writeRecord(key, badData);
	}
		
	private BulletinStore createTempStore() throws Exception 
	{
		MartusSecurity tempSecurity = new MockMartusSecurity();
		tempSecurity.createKeyPair();
		BulletinStore tempStore = new BulletinStore(new MockDatabase());
		tempStore.setSignatureGenerator(tempSecurity);
		return tempStore;
	}
	
	private String getFieldEntity(Bulletin b, String fieldName)
	{
		return MartusXml.getFieldTagStart(fieldName) + b.get(fieldName) + MartusXml.getFieldTagEnd();
	}

	private File createSampleFile(byte[] data) throws Exception
	{
		File tempFile = createTempFile();
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(data);
		out.close();
		return tempFile;
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
