package org.martus.meta;

import java.io.StringWriter;
import java.util.Vector;

import org.martus.client.Bulletin;
import org.martus.client.MockBulletin;
import org.martus.client.MockMartusApp;
import org.martus.client.RetrieveHQDraftsTableModel;
import org.martus.common.FieldDataPacket;
import org.martus.common.MartusCrypto;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;
import org.martus.server.MockMartusServer;
import org.martus.server.ServerSideNetworkHandler;

public class TestRetrieveHQDraftsTableModel extends TestCaseEnhanced
{
	public TestRetrieveHQDraftsTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		MartusCrypto hqSecurity = new MockMartusSecurity();
		hqSecurity.createKeyPair();
		hqApp = MockMartusApp.create(hqSecurity);

		MartusCrypto fieldSecurity1 = new MockMartusSecurity();
		fieldSecurity1.createKeyPair();
		fieldApp1 = MockMartusApp.create(fieldSecurity1);

		MartusCrypto fieldSecurity2 = new MockMartusSecurity();
		fieldSecurity2.createKeyPair();
		fieldApp2 = MockMartusApp.create(fieldSecurity2);

		assertNotEquals("account Id's equal?", fieldApp1.getAccountId(), fieldApp2.getAccountId());

		b0 = fieldApp1.createBulletin();
		b0.set(b0.TAGTITLE, title0);
		b0.set(b0.TAGAUTHOR, author0);
		b0.setAllPrivate(true);
		b0.setHQPublicKey(hqApp.getAccountId());
		b0.setDraft();
		b0.save();

		b1 = fieldApp1.createBulletin();
		b1.set(b1.TAGTITLE, title1);
		b1.set(b1.TAGAUTHOR, author1);
		b1.setAllPrivate(false);
		b1.setHQPublicKey(hqApp.getAccountId());
		b1.setSealed();
		b1.save();

		b2 = fieldApp2.createBulletin();
		b2.set(b2.TAGTITLE, title2);
		b2.set(b2.TAGAUTHOR, author2);
		b2.setAllPrivate(true);
		b2.setHQPublicKey(hqApp.getAccountId());
		b2.setDraft();
		b2.save();
	
		testServer = new MockServer();
		testSSLServerInterface = new ServerSideNetworkHandler(testServer);
		hqApp.setSSLNetworkInterfaceHandlerForTesting(testSSLServerInterface);
		modelWithData = new RetrieveHQDraftsTableModel(hqApp);
		modelWithData.initialize(null);
		String z0 = MockBulletin.saveToZipString(b0);
		String z1 = MockBulletin.saveToZipString(b1);
		String z2 = MockBulletin.saveToZipString(b2);
		Bulletin hqB0 = hqApp.createBulletin();
		MockBulletin.loadFromZipString(hqB0, z0);
		hqB0.save();
		Bulletin hqB1 = hqApp.createBulletin();
		MockBulletin.loadFromZipString(hqB1, z1);
		hqB1.save();
		Bulletin hqB2 = hqApp.createBulletin();
		MockBulletin.loadFromZipString(hqB2, z2);
		hqB2.save();
		
		modelWithoutData = new RetrieveHQDraftsTableModel(hqApp);
		modelWithoutData.initialize(null);
	}
	
	public void tearDown() throws Exception
	{
		testServer.deleteAllFiles();
    	fieldApp1.deleteAllFiles();
    	fieldApp2.deleteAllFiles();
    	hqApp.deleteAllFiles();
	}

	public void testGetColumnName()
	{
		assertEquals(fieldApp1.getFieldLabel("retrieveflag"), modelWithData.getColumnName(0));
		assertEquals(fieldApp1.getFieldLabel(Bulletin.TAGTITLE), modelWithData.getColumnName(1));
		assertEquals(fieldApp1.getFieldLabel(Bulletin.TAGAUTHOR), modelWithData.getColumnName(2));
	}
	
	public void testGetColumnCount()
	{
		assertEquals(3, modelWithoutData.getColumnCount());
		assertEquals(3, modelWithData.getColumnCount());
	}
	
	public void testGetRowCount()
	{
		assertEquals(0, modelWithoutData.getRowCount());
		assertEquals(2, modelWithData.getRowCount());
	}
	
	public void testIsCellEditable()
	{
		assertEquals("flag", true, modelWithData.isCellEditable(1,0));
		assertEquals("title", false, modelWithData.isCellEditable(1,1));
		assertEquals("author", false, modelWithData.isCellEditable(1,2));
	}
	
	public void testGetColumnClass()
	{
		assertEquals(Boolean.class, modelWithData.getColumnClass(0));
		assertEquals(String.class, modelWithData.getColumnClass(1));
		assertEquals(String.class, modelWithData.getColumnClass(2));
	}
	
	public void testGetAndSetValueAt()
	{
		Vector authors = new Vector();
		authors.add((String)modelWithData.getValueAt(0,2));
		authors.add((String)modelWithData.getValueAt(1,2));
		assertContains("Author 0 missing?", b0.get(b0.TAGAUTHOR), authors);
		assertContains("Author 2 missing?", b2.get(b2.TAGAUTHOR), authors);
		
		assertEquals("start bool", false, ((Boolean)modelWithData.getValueAt(0,0)).booleanValue());
		modelWithData.setValueAt(new Boolean(true), 0,0);
		assertEquals("setget bool", true, ((Boolean)modelWithData.getValueAt(0,0)).booleanValue());

		assertEquals("start title", title2, modelWithData.getValueAt(1,1));
		modelWithData.setValueAt(title2+title2, 1,1);
		assertEquals("keep title", title2, modelWithData.getValueAt(1,1));
	}
	
	public void testSetAllFlags()
	{
		Boolean t = new Boolean(true);
		Boolean f = new Boolean(false);
		
		modelWithData.setAllFlags(true);
		for(int allTrueCounter = 0; allTrueCounter < modelWithData.getRowCount(); ++allTrueCounter)
			assertEquals("all true" + allTrueCounter, t, modelWithData.getValueAt(0,0));

		modelWithData.setAllFlags(false);
		for(int allFalseCounter = 0; allFalseCounter < modelWithData.getRowCount(); ++allFalseCounter)
			assertEquals("all false" + allFalseCounter, f, modelWithData.getValueAt(0,0));
	}
	
	public void testGetIdList()
	{
		modelWithData.setAllFlags(false);
		Vector emptyList = modelWithData.getUniversalIdList();
		assertEquals(0, emptyList.size());
		
		modelWithData.setAllFlags(true);

		Vector fullList = modelWithData.getUniversalIdList();
		assertEquals(2, fullList.size());
		assertNotEquals("hq account ID0?", hqApp.getAccountId(), ((UniversalId)fullList.get(0)).getAccountId());
		assertNotEquals("hq account ID2?", hqApp.getAccountId(), ((UniversalId)fullList.get(1)).getAccountId());

		assertContains("b0 Uid not in list?", b0.getUniversalId(), fullList);
		assertContains("b2 Uid not in list?", b2.getUniversalId(), fullList);

		modelWithData.setValueAt(new Boolean(false), 0, 0);
		Vector twoList = modelWithData.getUniversalIdList();

		String summary = (String)modelWithData.getValueAt(1,1);
		assertEquals("Not correct summary?", title2, summary);
		assertEquals(1, twoList.size());
		assertEquals("b2 id", fullList.get(1), twoList.get(0));
	}

	class MockServer extends MockMartusServer
	{
		MockServer() throws Exception
		{
			super();
			setSecurity(new MockMartusSecurity());
		}
		
		public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId) 
		{			
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.OK);
			if(authorAccountId.equals(b0.getAccount()))
				result.add(b0.getLocalId() + "=" + b0.getFieldDataPacket().getLocalId());
			if(authorAccountId.equals(b2.getAccount()))
				result.add(b2.getLocalId() + "=" + b2.getFieldDataPacket().getLocalId());
			return result;
		}

		public Vector listFieldOfficeAccounts(String hqAccountId) 
		{
			Vector v = new Vector();
			v.add(NetworkInterfaceConstants.OK);
			v.add(fieldApp1.getAccountId());
			v.add(fieldApp2.getAccountId());
			return v;			
		}

		public Vector getPacket(String hqAccountId, String authorAccountId, String bulletinLocalId, String packetLocalId)
		{
			Vector result = new Vector();
			try 
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, packetLocalId);
				FieldDataPacket fdp = null;
				MartusCrypto security = fieldApp1.getSecurity();
				if(uid.equals(b0.getFieldDataPacket().getUniversalId()))
					fdp = b0.getFieldDataPacket();
				if(uid.equals(b1.getFieldDataPacket().getUniversalId()))
					fdp = b1.getFieldDataPacket();
				if(uid.equals(b2.getFieldDataPacket().getUniversalId()))
				{
					fdp = b2.getFieldDataPacket();
					security = fieldApp2.getSecurity();
				}

				StringWriter writer = new StringWriter();
				fdp.writeXml(writer, security);
				result.add(NetworkInterfaceConstants.OK);
				result.add(writer.toString());
				writer.close();
			} 
			catch (Exception e) 
			{
				result.add(NetworkInterfaceConstants.SERVER_ERROR);
			}
			return result;
		}
	}
	
	String title0 = "cool title";
	String title1 = "This is a cool title";
	String title2 = "Even cooler";

	String author0 = "Fred 0";
	String author1 = "Betty 1";
	String author2 = "Donna 2";

	MockMartusServer testServer;
	NetworkInterface testSSLServerInterface;
	MockMartusApp fieldApp1;
	MockMartusApp fieldApp2;
	MockMartusApp hqApp;
	
	Bulletin b0;
	Bulletin b1;
	Bulletin b2;

	RetrieveHQDraftsTableModel modelWithData;
	RetrieveHQDraftsTableModel modelWithoutData;
}
