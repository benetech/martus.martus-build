package org.martus.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.List;

import org.martus.common.MartusSecurity;
import org.martus.common.MockClientDatabase;
import org.martus.common.TestCaseEnhanced;

public class TestTransferableBulletin extends TestCaseEnhanced
{
	final static String TITLE = "twinkiepie";
	final static String ICKYTITLE = "w*o:r+k'e`d";
	final static String LONGTITLE = "This wonderful title is longer than twenty characters";

	public TestTransferableBulletin(String name) 
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		if(security == null)
		{
			security = new MartusSecurity();
			security.createKeyPair(512);
		}
		store = new BulletinStore(new MockClientDatabase());
		store.setSignatureGenerator(security);
		folder = store.createFolder("Wow");
		drag = createTransferableBulletin(TITLE);
		dragId = drag.getBulletins()[0].getLocalId();
	}

	public void tearDown()
	{
		drag.dispose();
	}

	public void testBasics()
	{
		Bulletin b1 = store.createEmptyBulletin();
		Bulletin b2 = store.createEmptyBulletin();
		Bulletin[] bulletins = {b1, b2};
		TransferableBulletinList list = new TransferableBulletinList(bulletins, folder);
		
		Bulletin[] got = list.getBulletins();
		assertEquals("wrong count?", 2, got.length);
		assertEquals("missing b1?", b1.getUniversalId(), got[0].getUniversalId());
		assertEquals("missing b2?", b2.getUniversalId(), got[1].getUniversalId());
	}

	public void testFlavors()
	{
		DataFlavor[] flavors = drag.getTransferDataFlavors();
		assertEquals(2, flavors.length);

//		assertEquals(true, drag.isDataFlavorSupported(DataFlavor.stringFlavor));
		assertEquals(true, drag.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
		assertEquals(true, drag.isDataFlavorSupported(TransferableBulletinList.getBulletinListDataFlavor()));
//		assertEquals(true, drag.isDataFlavorSupported(drag.getMimeTextDataFlavor()));
	}

	public void testStringFlavor()
	{
		String data = (String)getData(drag, DataFlavor.stringFlavor);
		assertNull("found a stringFlavor?", data);
	}

	public void testFileFlavor()
	{
		File file = getFile(drag, "basic");
		int at = file.getName().indexOf(TITLE);
		assertEquals("bad filename?", 0, at);

		TransferableBulletinList icky = createTransferableBulletin(ICKYTITLE);
		file = getFile(icky, "ickyname");
		at = file.getName().indexOf("w o r k e d");
		assertEquals("icky", 0, at);
		assertEndsWith("wrong extension for icky?", ".mba", file.getName());
		file.delete();

		TransferableBulletinList longName = createTransferableBulletin(LONGTITLE);
		file = getFile(longName, "longname");
		at = file.getName().indexOf("This wonderful title");
		assertEquals("long", 0, at);
		assertEndsWith("wrong extension for long?", ".mba", file.getName());	
		file.delete();
	}

	public void testBulletinFlavor()
	{
		Object data = getData(drag, TransferableBulletinList.getBulletinListDataFlavor());
		assertNotNull("null bulletinFlavor?", data);
		TransferableBulletinList tb = (TransferableBulletinList)data;
		assertEquals("bad folder?", folder, tb.getFromFolder());
		assertEquals("bad id?", dragId, tb.getBulletins()[0].getLocalId());
		
		Bulletin[] bulletins = tb.getBulletins();
		assertNotNull("null bulletins?", bulletins);
		assertEquals("bad id?", dragId, bulletins[0].getLocalId());
	}

	public void testExtractFrom()
	{
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(drag);
		assertNotNull("unable to extract", tb);
		assertEquals("extracted it problem", drag.getBulletins()[0].getLocalId(), tb.getBulletins()[0].getLocalId());
		StringSelection string = new StringSelection("Some data");
		tb = TransferableBulletinList.extractFrom(string);
		assertNull("should not extract", tb);
	}

	public void testToFileName()
	{
		String alphaNumeric		= "123abcABC";
		String alphaSpaces		= "abc def";
		String alphaPunctIn		= "a.b";
		String alphaPunctOut	= "a b";
		String trailingPunctIn	= "abc!";
		String trailingPunctOut	= "abc";
		String leadingPunctIn	= "?abc";
		String leadingPunctOut	= "abc";
		String punctuation1		= "`-=[]\\;',./";
		String punctuation2		= "~!@#%^&*()_+";
		String punctuation3		= "{}|:\"<>?";
		String tooLong			= "abcdefghijklmnopqrstuvwxyz";
		String tooShort			= "ab";
		String minimumLength	= "abc";
		assertEquals(alphaNumeric, MartusApp.toFileName(alphaNumeric));
		assertEquals(alphaSpaces, MartusApp.toFileName(alphaSpaces));
		assertEquals(alphaPunctOut, MartusApp.toFileName(alphaPunctIn));
		assertEquals(trailingPunctOut, MartusApp.toFileName(trailingPunctIn));
		assertEquals(leadingPunctOut, MartusApp.toFileName(leadingPunctIn));
		assertEquals("Martus-", MartusApp.toFileName(punctuation1));
		assertEquals("Martus-", MartusApp.toFileName(punctuation2));
		assertEquals("Martus-", MartusApp.toFileName(punctuation3));
		assertEquals(tooLong.substring(0, 20), MartusApp.toFileName(tooLong));
		assertEquals(TITLE, MartusApp.toFileName(TITLE));
		assertEquals("Martus-" + tooShort, MartusApp.toFileName(tooShort));
		assertEquals(minimumLength, MartusApp.toFileName(minimumLength));
	}

	private TransferableBulletinList createTransferableBulletin(String title)
	{
		Bulletin b = store.createEmptyBulletin();
		b.setSealed();
		b.set(Bulletin.TAGTITLE, title);
		b.save();
		Bulletin[] bulletins = new Bulletin[] {b};
		TransferableBulletinList localTB = new TransferableBulletinList(bulletins, folder);
		Bulletin[] got = localTB.getBulletins();
		assertEquals("id after create", b.getLocalId(), got[0].getLocalId());
		return localTB;
	}

	private Object getData(TransferableBulletinList drag, DataFlavor flavor)
	{
		Object result = null;
		try
		{
			result = drag.getTransferData(flavor);
		}
		catch (UnsupportedFlavorException e)
		{
			result = null;
		}

		return result;
	}

	private File getFile(TransferableBulletinList tb, String debugText)
	{
		List list = (List)getData(tb, DataFlavor.javaFileListFlavor);
		assertNotNull(debugText + " null fileListFlavor?", list);
		assertEquals(debugText, 1, list.size());
		Object data = list.get(0);
		assertTrue(debugText + " not a file?", data instanceof File);
		File file = (File)data;
		assertTrue(debugText + " file should always exist", file.exists());
		return file;
	}

	BulletinStore store;
	BulletinFolder folder;
	TransferableBulletinList drag;
	String dragId;
	static MartusSecurity security;
}
