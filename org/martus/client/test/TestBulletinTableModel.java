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

import junit.framework.TestCase;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.BulletinStore;
import org.martus.client.swingui.bulletintable.BulletinTableModel;
import org.martus.common.Bulletin;
import org.martus.common.MockClientDatabase;
import org.martus.common.UniversalId;

public class TestBulletinTableModel extends TestCase
{
    public TestBulletinTableModel(String name)
	{
        super(name);
    }

    public void setUp() throws Exception
    {
    	localization = new MockUiLocalization();
		app = MockMartusApp.create();
		app.store = new BulletinStore(new MockClientDatabase());
		app.store.setSignatureGenerator(app.getSecurity());
		app.loadSampleData();
		store = app.getStore();
		folderSent = app.getFolderSent();
    }

    public void tearDown() throws Exception
    {
		store.deleteAllData();
		app.deleteAllFiles();
	}
	
	public void test()
	{
		doTestColumns();
		doTestFieldNames();
		doTestRows();
		doTestGetBulletin();
		doTestGetValueAt();
		doTestSetFolder();
		doTestFindBulletin();
		doTestSortByColumn();
	}

    public void doTestColumns()
    {
		BulletinTableModel list = new BulletinTableModel(localization);
		list.setFolder(folderSent);

		assertEquals("column count", 4, list.getColumnCount());
		assertEquals(localization.getFieldLabel("status"), list.getColumnName(0));
		assertEquals(localization.getFieldLabel("eventdate"), list.getColumnName(1));
		assertEquals(localization.getFieldLabel("title"), list.getColumnName(2));
		assertEquals(localization.getFieldLabel("author"), list.getColumnName(3));
	}

	public void doTestFieldNames()
	{
		BulletinTableModel list = new BulletinTableModel(localization);
		list.setFolder(folderSent);

		assertEquals("row count", 4, list.getColumnCount());
		assertEquals("status", list.getFieldName(0));
		assertEquals("eventdate", list.getFieldName(1));
		assertEquals("title", list.getFieldName(2));
		assertEquals("author", list.getFieldName(3));
	}

	public void doTestRows()
	{
		BulletinTableModel list = new BulletinTableModel(localization);
		list.setFolder(folderSent);

		assertEquals(store.getBulletinCount(), list.getRowCount());
		Bulletin b = list.getBulletin(2);
		assertEquals(b.get("author"), list.getValueAt(2, 3));

		b = list.getBulletin(4);
		String displayDate = localization.convertStoredDateToDisplay(b.get("eventdate"));
		assertEquals(displayDate, list.getValueAt(4, 1));
    }

	public void doTestGetBulletin()
	{
		BulletinTableModel list = new BulletinTableModel(localization);
		list.setFolder(folderSent);
		for(int i = 0; i < folderSent.getBulletinCount(); ++i)
		{
			UniversalId folderBulletinId = folderSent.getBulletinSorted(i).getUniversalId();
			UniversalId listBulletinId = list.getBulletin(i).getUniversalId();
			assertEquals(i + "wrong bulletin?", folderBulletinId, listBulletinId);
		}
	}

	public void doTestGetValueAt()
	{
		BulletinTableModel list = new BulletinTableModel(localization);
		list.setFolder(folderSent);

		assertEquals("", list.getValueAt(1000, 0));

		Bulletin b = list.getBulletin(0);
		b.set("title", "xyz");
		store.saveBulletin(b);

		assertEquals(Bulletin.STATUSSEALED, b.getStatus());
		assertEquals(localization.getStatusLabel("sealed"), list.getValueAt(0,0));

		b.set("eventdate", "1999-04-15");
		store.saveBulletin(b);
		String displayDate = localization.convertStoredDateToDisplay("1999-04-15");
		assertEquals(displayDate, list.getValueAt(0,1));

		assertEquals("xyz", b.get("title"));
		assertEquals("xyz", list.getValueAt(0,2));

		b.setDraft();
		store.saveBulletin(b);
		assertEquals(localization.getStatusLabel("draft"), list.getValueAt(0,0));
		b.setSealed();
		store.saveBulletin(b);
		assertEquals(localization.getStatusLabel("sealed"), list.getValueAt(0,0));

	}

	public void doTestSetFolder()
	{
		BulletinTableModel list = new BulletinTableModel(localization);
		assertEquals(0, list.getRowCount());

		list.setFolder(folderSent);
		assertEquals(store.getBulletinCount(), folderSent.getBulletinCount());
		assertEquals(folderSent.getBulletinSorted(0).getLocalId(), list.getBulletin(0).getLocalId());

		BulletinFolder empty = store.createFolder("empty");
		assertEquals(0, empty.getBulletinCount());
		list.setFolder(empty);
		assertEquals(0, list.getRowCount());
	}

	public void doTestFindBulletin()
	{
		BulletinTableModel list = new BulletinTableModel(localization);
		list.setFolder(folderSent);

		assertEquals(-1, list.findBulletin(null));

		assertTrue("Need at least two sample bulletins", list.getRowCount() >= 2);
		int last = list.getRowCount()-1;
		Bulletin bFirst = list.getBulletin(0);
		Bulletin bLast = list.getBulletin(last);
		assertEquals(0, list.findBulletin(bFirst.getUniversalId()));
		assertEquals(last, list.findBulletin(bLast.getUniversalId()));

		Bulletin b = store.createEmptyBulletin();
		assertEquals(-1, list.findBulletin(b.getUniversalId()));
		store.saveBulletin(b);
		assertEquals(-1, list.findBulletin(b.getUniversalId()));
	}

	public void doTestSortByColumn()
	{
		BulletinTableModel list = new BulletinTableModel(localization);
		list.setFolder(folderSent);

		String tag = "eventdate";
		int col = 1;
		assertEquals(tag, list.getFieldName(col));
		assertEquals(tag, folderSent.sortedBy());
		String first = (String)list.getValueAt(0, col);
		list.sortByColumn(col);
		assertEquals(tag, folderSent.sortedBy());
		assertEquals(false, first.equals(list.getValueAt(0,col)));
	}

	MockMartusApp app;
	MockUiLocalization localization;
	BulletinStore store;
	BulletinFolder folderSent;

}
