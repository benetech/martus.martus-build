package org.martus.meta;

import java.util.Vector;

import org.martus.client.*;
import org.martus.common.*;
import org.martus.server.*;

public class TestRetrieveMyDraftsTableModel extends TestCaseEnhanced
{
	public TestRetrieveMyDraftsTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		MartusCrypto appSecurity = new MockMartusSecurity();
		appSecurity.createKeyPair();
		app = MockMartusApp.create(appSecurity);

		b0 = app.createBulletin();
		b0.set(b0.TAGTITLE, title1);
		b0.setDraft();
		b0.save();
		b1 = app.createBulletin();
		b1.set(b1.TAGTITLE, title1);
		b1.setSealed();
		b1.save();
		b2 = app.createBulletin();
		b2.set(b2.TAGTITLE, title2);
		b2.setDraft();
		b2.save();

		testServer = new MockServer();
		testServer.setSecurity(new MockMartusSecurity());
		
		testServerInterface = new ServerSideNetworkHandlerForNonSSL(testServer);
		testSSLServerInterface = new ServerSideNetworkHandler(testServer);
		app.setSSLServerForTesting(testSSLServerInterface);
		modelWithoutData = new RetrieveMyDraftsTableModel(app);
		app.getStore().deleteAllData();
		modelWithData = new RetrieveMyDraftsTableModel(app);
	}
	
	public void tearDown() throws Exception
	{
		testServer.deleteAllFiles();
    	app.deleteAllFiles();
	}

	public void testGetColumnName()
	{
		assertEquals(app.getFieldLabel("retrieveflag"), modelWithData.getColumnName(0));
		assertEquals(app.getFieldLabel(Bulletin.TAGTITLE), modelWithData.getColumnName(1));
	}
	
	public void testGetColumnCount()
	{
		assertEquals(2, modelWithoutData.getColumnCount());
		assertEquals(2, modelWithData.getColumnCount());
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
	}
	
	public void testGetColumnClass()
	{
		assertEquals(Boolean.class, modelWithData.getColumnClass(0));
		assertEquals(String.class, modelWithData.getColumnClass(1));
	}
	
	public void testGetAndSetValueAt()
	{
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
		modelWithData.setValueAt(new Boolean(false), 1, 0);
		Vector twoList = modelWithData.getUniversalIdList();
		assertEquals(1, twoList.size());
		assertEquals("b0 id", b0.getUniversalId(), twoList.get(0));
	}

	class MockServer extends MockMartusServer
	{
		MockServer() throws Exception
		{
			super();
		}
		
		public Vector listMyDraftBulletinIds(String authorAccountId) 
		{
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.OK);
			result.add(b0.getLocalId() + "= " + b0.get(b0.TAGTITLE));
			result.add(b2.getLocalId() + "= " + b2.get(b2.TAGTITLE));
			return result;
		}

	}
	
	String title1 = "This is a cool title";
	String title2 = "Even cooler";

	MockMartusServer testServer;
	NetworkInterfaceForNonSSL testServerInterface;
	NetworkInterface testSSLServerInterface;
	MockMartusApp app;
	Bulletin b0;
	Bulletin b1;
	Bulletin b2;

	RetrieveMyDraftsTableModel modelWithData;
	RetrieveMyDraftsTableModel modelWithoutData;
}
