package org.martus.meta;

import java.util.Vector;

import org.martus.client.Bulletin;
import org.martus.client.DeleteMyServerDraftsTableModel;
import org.martus.client.MockMartusApp;
import org.martus.common.MartusCrypto;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceForNonSSL;
import org.martus.common.TestCaseEnhanced;
import org.martus.server.MockMartusServer;
import org.martus.server.ServerSideNetworkHandler;
import org.martus.server.ServerSideNetworkHandlerForNonSSL;

public class TestDeleteDraftsTableModel extends TestCaseEnhanced
{

	public TestDeleteDraftsTableModel(String name)
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
		b1 = app.createBulletin();
		b1.set(b1.TAGTITLE, title1);
		b2 = app.createBulletin();
		b2.set(b2.TAGTITLE, title2);
		
		testServer = new MockServer();
		testServer.setSecurity(new MockMartusSecurity());
		ServerSideNetworkHandler testSSLServerInterface = new ServerSideNetworkHandler(testServer);
		
		app.setSSLNetworkInterfaceHandlerForTesting(testSSLServerInterface);

		testServer.hasData = false;
		modelWithoutData = new DeleteMyServerDraftsTableModel(app);
		modelWithoutData.initialize(null);
		
		testServer.hasData = true;
		modelWithData = new DeleteMyServerDraftsTableModel(app);
		modelWithData.initialize(null);
	}
	
	public void tearDown() throws Exception
	{
		testServer.deleteAllFiles();
    	app.deleteAllFiles();
	}

	public void testGetColumnCount()
	{
		assertEquals(2, modelWithoutData.getColumnCount());
		assertEquals(2, modelWithData.getColumnCount());
	}
	
	public void testGetColumnName()
	{
		assertEquals(app.getFieldLabel("DeleteFlag"), modelWithData.getColumnName(0));
		assertEquals(app.getFieldLabel(Bulletin.TAGTITLE), modelWithData.getColumnName(1));
	}
	
	public void testGetColumnClass()
	{
		assertEquals(Boolean.class, modelWithData.getColumnClass(0));
		assertEquals(String.class, modelWithData.getColumnClass(1));
	}
	
	public void testRowCount()
	{
		assertEquals(0, modelWithoutData.getRowCount());
		assertEquals(3, modelWithData.getRowCount());
	}
	
	public void testGetAndSetValueAt()
	{
		assertEquals("start bool", false, ((Boolean)modelWithData.getValueAt(0,0)).booleanValue());
		modelWithData.setValueAt(new Boolean(true), 0,0);
		assertEquals("setget bool", true, ((Boolean)modelWithData.getValueAt(0,0)).booleanValue());

		assertEquals("start title", title2, modelWithData.getValueAt(2,1));
		modelWithData.setValueAt(title2+title2, 2,1);
		assertEquals("keep title", title2, modelWithData.getValueAt(2,1));
	}
	
	class MockServer extends MockMartusServer
	{
		MockServer() throws Exception
		{
			super();
		}
		
		public Vector listMyDraftBulletinIds(String clientId, Vector retrieveTags)
		{
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.OK);
			Vector list = new Vector();
			if(hasData)
			{
				list.add(b0.getLocalId() + "= " + b0.get(b0.TAGTITLE));
				list.add(b1.getLocalId() + "= " + b1.get(b1.TAGTITLE));
				list.add(b2.getLocalId() + "= " + b2.get(b2.TAGTITLE));
			}
			result.add(list);
			return result;
		}
		
		public boolean hasData;
		
	}
	
	final static String title1 = "This is a cool title";
	final static String title2 = "Even cooler";

	MockMartusApp app;
	MockServer testServer;
	DeleteMyServerDraftsTableModel modelWithData;
	DeleteMyServerDraftsTableModel modelWithoutData;
	
	Bulletin b0;
	Bulletin b1;
	Bulletin b2;
}
