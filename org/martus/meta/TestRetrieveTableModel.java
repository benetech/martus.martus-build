package org.martus.meta;

import java.util.Vector;

import org.martus.client.Bulletin;
import org.martus.client.BulletinSummary;
import org.martus.client.MartusApp;
import org.martus.client.MockMartusApp;
import org.martus.client.RetrieveHQDraftsTableModel;
import org.martus.client.RetrieveHQTableModel;
import org.martus.client.RetrieveMyDraftsTableModel;
import org.martus.client.RetrieveMyTableModel;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceForNonSSL;
import org.martus.common.TestCaseEnhanced;
import org.martus.server.MartusServer;
import org.martus.server.MockMartusServer;
import org.martus.server.ServerSideNetworkHandler;
import org.martus.server.ServerSideNetworkHandlerForNonSSL;

public class TestRetrieveTableModel extends TestCaseEnhanced
{

	public TestRetrieveTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		if(mockSecurityForApp == null)
			mockSecurityForApp = new MockMartusSecurity();
		
		if(mockSecurityForServer == null)
			mockSecurityForServer = new MockMartusSecurity();

		mockServer = new MockMartusServer();
		mockServer.setSecurity(mockSecurityForServer);
		mockSSLServerHandler = new MockServerInterfaceHandler(mockServer);

		appWithoutServer = MockMartusApp.create(mockSecurityForApp);
		MockServerNotAvailable mockServerNotAvailable = new MockServerNotAvailable();
		appWithoutServer.setSSLServerForTesting(new ServerSideNetworkHandler(mockServerNotAvailable));

		appWithServer = MockMartusApp.create(mockSecurityForApp);
		appWithServer.setServerInfo("mock", mockServer.getAccountId());
		appWithServer.setSSLServerForTesting(mockSSLServerHandler);
		
		appWithAccount = MockMartusApp.create(mockSecurityForApp);
		appWithAccount.setServerInfo("mock", mockServer.getAccountId());
		appWithAccount.setSSLServerForTesting(mockSSLServerHandler);

		mockServer.deleteAllData();
	}

	public void testGetMyBulletinSummariesNoServer() throws Exception
	{
		try
		{
			RetrieveMyTableModel model = new RetrieveMyTableModel(appWithoutServer, null);
			model.Initalize();
			model.getMySummaries();
			Vector failed = model.getResults();
			fail("Got valid summaries?");
		}
		catch(MartusApp.ServerErrorException ignoreExpectedException)
		{
		}
	}
	
	public void testGetMyBulletinSummariesErrors() throws Exception
	{
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.REJECTED);
		mockServer.listMyResponse = desiredResult;
		try
		{
			RetrieveMyTableModel model = new RetrieveMyTableModel(appWithServer, null);
			model.Initalize();
			Vector failed = model.getResults();
			fail("rejected didn't throw?");
		}
		catch(MartusApp.ServerErrorException ignoreExpectedException)
		{
		}
		mockServer.listMyResponse = null;
	}

	public void testGetMySummaries() throws Exception
	{
		String sampleSummary1 = "this is a basic summary";
		String sampleSummary2 = "another silly summary";
		String sampleSummary3 = "yet another!";
		
		Bulletin b1 = appWithAccount.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setSealed();
		
		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(true);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setDraft();
		
		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));

		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithAccount, null);
		model.Initalize();
		Vector result = model.getResults();
		assertEquals("wrong count?", 2, result.size());
		
		BulletinSummary s1 = (BulletinSummary)result.get(0);
		BulletinSummary s2 = (BulletinSummary)result.get(1);

		Bulletin bulletins[] = new Bulletin[] {b1, b2};
		BulletinSummary summaries[] = new BulletinSummary[] {s1, s2};
		boolean found[] = new boolean[bulletins.length];
		
		for(int i = 0; i < bulletins.length; ++i)
		{
			for(int j = 0; j < summaries.length; ++j)
			{
				Bulletin b = bulletins[i];
				BulletinSummary s = summaries[j];
				if(b.getLocalId().equals(s.getLocalId()))
				{
					assertEquals(b.get(b.TAGTITLE), s.getTitle());
					found[i] = true;
				}
			}
		}
		
		assertTrue("Missing 1?", found[0]);
		assertTrue("Missing 2?", found[1]);
	}

	public void testGetMyDraftBulletinSummariesErrors() throws Exception
	{
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.REJECTED);
		mockServer.listMyResponse = desiredResult;
		try
		{
			RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithServer, null);
			model.Initalize();
			Vector failed = model.getResults();
			fail("rejected didn't throw?");
		}
		catch(MartusApp.ServerErrorException ignoreExpectedException)
		{
		}
		mockServer.listMyResponse = null;
	}

	public void testGetMyDraftSummaries() throws Exception
	{
		String sampleSummary1 = "this is a basic summary";
		String sampleSummary2 = "another silly summary";
		String sampleSummary3 = "yet another!";
		
		Bulletin b1 = appWithAccount.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setDraft();
		
		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setDraft();
		
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(true);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setSealed();
		
		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));

		RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithAccount, null);
		model.Initalize();
		Vector result = model.getResults();
		assertEquals("wrong count?", 2, result.size());
		
		BulletinSummary s1 = (BulletinSummary)result.get(0);
		BulletinSummary s2 = (BulletinSummary)result.get(1);

		Bulletin bulletins[] = new Bulletin[] {b1, b2};
		BulletinSummary summaries[] = new BulletinSummary[] {s1, s2};
		boolean found[] = new boolean[bulletins.length];
		
		for(int i = 0; i < bulletins.length; ++i)
		{
			for(int j = 0; j < summaries.length; ++j)
			{
				Bulletin b = bulletins[i];
				BulletinSummary s = summaries[j];
				if(b.getLocalId().equals(s.getLocalId()))
				{
					assertEquals(b.get(b.TAGTITLE), s.getTitle());
					found[i] = true;
				}
			}
		}
		
		assertTrue("Missing 1?", found[0]);
		assertTrue("Missing 2?", found[1]);
	}
	
	public void testGetFieldOfficeBulletinSummariesNoServer() throws Exception
	{
		try
		{
			RetrieveHQTableModel model = new RetrieveHQTableModel(appWithoutServer, null);
			model.Initalize();
			model.getFieldOfficeSealedSummaries("");
			Vector failed = model.getResults();
			fail("Got valid sealed summaries?");
		}
		catch(MartusApp.ServerErrorException ignoreExpectedException)
		{
		}
		
		try
		{
			RetrieveHQDraftsTableModel model = new RetrieveHQDraftsTableModel(appWithoutServer, null);
			model.Initalize();
			model.getFieldOfficeDraftSummaries("");
			Vector failed = model.getResults();
			fail("Got valid draft summaries?");
		}
		catch(MartusApp.ServerErrorException ignoreExpectedException)
		{
		}
	}

	public void testGetFieldOfficeBulletinSummariesErrors() throws Exception
	{
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.REJECTED);
		mockServer.listFieldOfficeSummariesResponse = desiredResult;
		try
		{
			RetrieveHQTableModel model = new RetrieveHQTableModel(appWithServer, null);
			model.Initalize();
			model.getFieldOfficeSealedSummaries("");
			Vector failed = model.getResults();
			fail("rejected sealed didn't throw?");
		}
		catch(MartusApp.ServerErrorException ignoreExpectedException)
		{
		}

		try
		{
			RetrieveHQDraftsTableModel model = new RetrieveHQDraftsTableModel(appWithServer, null);
			model.Initalize();
			model.getFieldOfficeDraftSummaries("");
			Vector failed = model.getResults();
			fail("rejected draft didn't throw?");
		}
		catch(MartusApp.ServerErrorException ignoreExpectedException)
		{
		}

		mockServer.listFieldOfficeSummariesResponse = null;
	}

	public void testGetFieldOfficeSummaries() throws Exception
	{
		MockMartusSecurity hqSecurity = new MockMartusSecurity();	
		hqSecurity.createKeyPair();
		MockMartusApp hqApp = MockMartusApp.create(hqSecurity);
		hqApp.setServerInfo("mock", mockServer.getAccountId());
		hqApp.setSSLServerForTesting(mockSSLServerHandler);
		assertNotEquals("same public key?", appWithAccount.getAccountId(), hqApp.getAccountId());
		appWithAccount.setHQKey(hqApp.getAccountId());

		String sampleSummary1 = "this is a basic summary";
		String sampleSummary2 = "another silly summary";
		String sampleSummary3 = "Draft summary";
		
		Bulletin b1 = appWithAccount.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setSealed();
		appWithAccount.setHQKeyInBulletin(b1);
		
		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		appWithAccount.setHQKeyInBulletin(b2);
		
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(false);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setDraft();
		appWithAccount.setHQKeyInBulletin(b3);

		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));

		Vector desiredSealedResult = new Vector();
		desiredSealedResult.add(NetworkInterfaceConstants.OK);
		desiredSealedResult.add(b1.getLocalId() + "=" + b1.getFieldDataPacket().getLocalId());
		desiredSealedResult.add(b2.getLocalId() + "=" + b2.getFieldDataPacket().getLocalId());

		mockServer.listFieldOfficeSummariesResponse = desiredSealedResult;	

		RetrieveHQTableModel model = new RetrieveHQTableModel(hqApp, null);
		model.Initalize();
		Vector returnedSealedResults = model.getResults();
		assertEquals("Wrong size?", 2, returnedSealedResults.size());
		BulletinSummary s1 = (BulletinSummary)returnedSealedResults.get(0);
		BulletinSummary s2 = (BulletinSummary)returnedSealedResults.get(1);
		boolean found1 = false;
		boolean found2 = false;
		found1 = s1.getLocalId().equals(b1.getLocalId());
		if(!found1)
			found1 = s2.getLocalId().equals(b1.getLocalId());
		found2 = s1.getLocalId().equals(b2.getLocalId());
		if(!found2)
			found2 = s2.getLocalId().equals(b2.getLocalId());
		assertTrue("not found S1?", found1);
		assertTrue("not found S2?", found2);

		Vector desiredDraftResult = new Vector();
		desiredDraftResult.add(NetworkInterfaceConstants.OK);
		desiredDraftResult.add(b3.getLocalId() + "=" + b3.getFieldDataPacket().getLocalId());

		mockServer.listFieldOfficeSummariesResponse = desiredDraftResult;	

		RetrieveHQDraftsTableModel model2 = new RetrieveHQDraftsTableModel(hqApp, null);
		model2.Initalize();
		Vector returnedDraftResults = model2.getResults();
		assertEquals("Wrong draft size?", 1, returnedDraftResults.size());
		BulletinSummary s3 = (BulletinSummary)returnedDraftResults.get(0);
		boolean found3 = false;
		found3 = s3.getLocalId().equals(b3.getLocalId());
		assertTrue("not found S3?", found3);
		mockServer.listFieldOfficeSummariesResponse = null;
		hqApp.deleteAllFiles();
	}




	class MockServer extends MockMartusServer
	{
		MockServer() throws Exception
		{
			super();
		}
		
		public Vector listMySealedBulletinIds(String clientId)
		{
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.OK);
			result.add(b0.getLocalId() + "= " + b0.get(b0.TAGTITLE));
			result.add(b1.getLocalId() + "= " + b1.get(b1.TAGTITLE));
			result.add(b2.getLocalId() + "= " + b2.get(b2.TAGTITLE));
			return result;
		}
		
	}
	
	public class MockServerInterfaceHandler extends ServerSideNetworkHandler
	{
		MockServerInterfaceHandler(MartusServer serverToUse)
		{
			super(serverToUse);
		}
		
		public void nullGetFieldOfficeAccountIds(boolean shouldReturnNull)
		{
			nullGetFieldOfficeAccountIds = shouldReturnNull;
		}
		
		public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
		{
			if(nullGetFieldOfficeAccountIds)
				return null;
			return super.getFieldOfficeAccountIds(myAccountId, parameters, signature);
		}
		
		boolean nullGetFieldOfficeAccountIds;
	}

	public static class MockServerNotAvailable extends MockMartusServer
	{
		MockServerNotAvailable() throws Exception
		{
			super();
		}

		public String ping()
		{
			return null;
		}
		
	}


	Bulletin b0;
	Bulletin b1;
	Bulletin b2;

	String title1 = "This is a cool title";
	String title2 = "Even cooler";

	private static MockMartusSecurity mockSecurityForApp;
	private static MockMartusSecurity mockSecurityForServer;

	private MockMartusApp appWithServer;
	private MockMartusApp appWithoutServer;
	private MockMartusApp appWithAccount;

	private MockMartusServer mockServer;
	private MockServerInterfaceHandler mockSSLServerHandler;
}
