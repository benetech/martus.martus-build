package org.martus.client;

import junit.framework.TestCase;

import org.martus.common.MockClientDatabase;

public class TestFolderList extends TestCase
{
    public TestFolderList(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
		app = MockMartusApp.create();
		app.store = new BulletinStore(new MockClientDatabase());
		app.store.setSignatureGenerator(app.getSecurity());
    }
    
    public void tearDown() throws Exception
    {
    	app.deleteAllFiles();
    }

	public void testBasics()
	{
		app.loadSampleData();
		BulletinStore store = app.getStore();
		FolderList list = new FolderList(app);
		list.loadFolders(store);

		int baseCount = getVisibleFolderCount(store);
		assertEquals("Initial count", baseCount, list.getCount());

		FolderTreeNode node = list.findFolderByLocalizedName("lisjf;lisjef");
		assertNull("Find folder that isn't there", node);

		BulletinFolder folder = store.createFolder("test");
		list.loadFolders(store);
		assertEquals(baseCount+1, list.getCount());
		System.out.println(list.getName(0));
		assertEquals("Outbox not first?", app.getFolderLabel(app.getFolderOutbox().getName()), list.getName(0));

		node = list.getNode(baseCount);
		assertEquals("test", node.toString());
		node = list.findFolderByLocalizedName("test");
		assertEquals("test", node.toString());

		store.renameFolder("test", "new");
		list.loadFolders(store);
		assertEquals(getVisibleFolderCount(store), list.getCount());
		assertEquals("new", list.getName(list.getCount()-1));
		node = list.findFolderByLocalizedName("test");
		assertNull("Find deleted folder", node);
		node = list.findFolderByLocalizedName("new");
		assertEquals("new", node.toString());

		store.deleteFolder("new");
		list.loadFolders(store);
		assertEquals(baseCount, list.getCount());
	}

	public void testLocalizedFolders()
	{
		app.loadSampleData();
		BulletinStore store = app.getStore();
		FolderList list = new FolderList(app);
		list.loadFolders(store);

		int baseCount = getVisibleFolderCount(store);
		assertEquals("Initial count", baseCount, list.getCount());

		BulletinFolder folder = store.createFolder(app.getNameOfFolderRetrieved());
		list.loadFolders(store);
		assertEquals(baseCount+1, list.getCount());
		assertEquals("Outbox not first?", app.getFolderLabel(app.getFolderOutbox().getName()), list.getName(0));

		FolderTreeNode node = list.getNode(baseCount);
		assertEquals(app.getNameOfFolderRetrieved(), node.getInternalName());

		assertEquals(app.getFolderLabel(app.getNameOfFolderRetrieved()), node.getLocalizedName());
		store.deleteFolder(app.getNameOfFolderRetrieved());
		list.loadFolders(store);
		assertEquals(baseCount, list.getCount());
		
	}

	public void testLoadFolders()
	{
		app.loadSampleData();
		BulletinStore store = app.getStore();
		assertTrue("Need sample folders", getVisibleFolderCount(store) > 0);

		FolderList ourList = new FolderList(app);
		ourList.loadFolders(store);
		assertEquals("Didn't load properly", getVisibleFolderCount(store), ourList.getCount());
		ourList.loadFolders(store);
		assertEquals("Reload failed", getVisibleFolderCount(store), ourList.getCount());

	}

	private int getVisibleFolderCount(BulletinStore store)
	{
		return store.getVisibleFolderNames().size();
	}

	MockMartusApp app;
}

