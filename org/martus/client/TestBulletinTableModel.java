/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

package org.martus.client;

import junit.framework.TestCase;

import org.martus.common.MartusCrypto;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.UniversalId;

public class TestBulletinTableModel extends TestCase
{
    public TestBulletinTableModel(String name)
	{
        super(name);
    }

    public void setUp() throws Exception
    {
    	if(cryptoToUse == null)
	    	cryptoToUse = new MockMartusSecurity();
		app = MockMartusApp.create(cryptoToUse);
		app.store = new BulletinStore(new MockClientDatabase());
		store = app.getStore();
		store.setSignatureGenerator(cryptoToUse);
		app.loadSampleData();
		folder = app.getFolderSent();
    }

    public void tearDown() throws Exception
    {
		store.deleteAllData();
		app.deleteAllFiles();
	}

    public void testColumns()
    {
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folder);

		assertEquals(4, list.getColumnCount());
		assertEquals("Status", list.getColumnName(0));
		assertEquals("Date of Event", list.getColumnName(1));
		assertEquals("Title", list.getColumnName(2));
		assertEquals("Author", list.getColumnName(3));
	}

	public void testFieldNames()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folder);

		assertEquals(4, list.getColumnCount());
		assertEquals("status", list.getFieldName(0));
		assertEquals("eventdate", list.getFieldName(1));
		assertEquals("title", list.getFieldName(2));
		assertEquals("author", list.getFieldName(3));
	}

	public void testRows()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folder);

		assertEquals(store.getBulletinCount(), list.getRowCount());
		Bulletin b = list.getBulletin(2);
		assertEquals(b.get("author"), list.getValueAt(2, 3));

		b = list.getBulletin(4);
		String displayDate = app.convertStoredToDisplay(b.get("eventdate"));
		assertEquals(displayDate, list.getValueAt(4, 1));
    }

	public void testGetBulletin()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folder);
		for(int i = 0; i < folder.getBulletinCount(); ++i)
		{
			UniversalId folderBulletinId = folder.getBulletinSorted(i).getUniversalId();
			UniversalId listBulletinId = list.getBulletin(i).getUniversalId();
			assertEquals(i + "wrong bulletin?", folderBulletinId, listBulletinId);
		}
	}

	public void testGetValueAt()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folder);

		assertEquals("", list.getValueAt(1000, 0));

		Bulletin b = list.getBulletin(0);
		b.set("title", "xyz");
		b.save();

		assertEquals(Bulletin.STATUSSEALED, b.getStatus());
		assertEquals("Sealed", list.getValueAt(0,0));

		b.set("eventdate", "1999-04-15");
		b.save();
		String displayDate = app.convertStoredToDisplay("1999-04-15");
		assertEquals(displayDate, list.getValueAt(0,1));

		assertEquals("xyz", b.get("title"));
		assertEquals("xyz", list.getValueAt(0,2));

		b.setDraft();
		b.save();
		assertEquals("Draft", list.getValueAt(0,0));
		b.setSealed();
		b.save();
		assertEquals("Sealed", list.getValueAt(0,0));

	}

	public void testSetFolder()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		assertEquals(0, list.getRowCount());

		list.setFolder(folder);
		assertEquals(store.getBulletinCount(), folder.getBulletinCount());
		assertEquals(folder.getBulletinSorted(0).getLocalId(), list.getBulletin(0).getLocalId());

		BulletinFolder empty = store.createFolder("empty");
		assertEquals(0, empty.getBulletinCount());
		list.setFolder(empty);
		assertEquals(0, list.getRowCount());
	}

	public void testFindBulletin()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folder);

		assertEquals(-1, list.findBulletin(null));

		assertTrue("Need at least two sample bulletins", list.getRowCount() >= 2);
		int last = list.getRowCount()-1;
		Bulletin bFirst = list.getBulletin(0);
		Bulletin bLast = list.getBulletin(last);
		assertEquals(0, list.findBulletin(bFirst.getUniversalId()));
		assertEquals(last, list.findBulletin(bLast.getUniversalId()));

		Bulletin b = store.createEmptyBulletin();
		assertEquals(-1, list.findBulletin(b.getUniversalId()));
		b.save();
		assertEquals(-1, list.findBulletin(b.getUniversalId()));
	}

	public void testSortByColumn()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folder);

		String tag = "eventdate";
		int col = 1;
		assertEquals(tag, list.getFieldName(col));
		assertEquals(tag, folder.sortedBy());
		String first = (String)list.getValueAt(0, col);
		list.sortByColumn(col);
		assertEquals(tag, folder.sortedBy());
		assertEquals(false, first.equals(list.getValueAt(0,col)));
	}

	static MartusCrypto cryptoToUse;
	MockMartusApp app;
	BulletinStore store;
	BulletinFolder folder;

}
