package org.martus.meta;

import java.util.Vector;

import org.martus.client.Bulletin;
import org.martus.client.BulletinStore;
import org.martus.client.BulletinSummary;
import org.martus.client.MockMartusApp;
import org.martus.client.RetrieveHQDraftsTableModel;
import org.martus.client.RetrieveHQTableModel;
import org.martus.client.RetrieveMyDraftsTableModel;
import org.martus.client.RetrieveMyTableModel;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.server.forclients.MartusServer;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerSideNetworkHandler;

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
		mockServer.initialize();
		mockServer.setSecurity(mockSecurityForServer);
		mockSSLServerHandler = new MockServerInterfaceHandler(mockServer);

		appWithoutServer = MockMartusApp.create(mockSecurityForApp);
		MockServerNotAvailable mockServerNotAvailable = new MockServerNotAvailable();
		appWithoutServer.setSSLNetworkInterfaceHandlerForTesting(new ServerSideNetworkHandler(mockServerNotAvailable));

		appWithServer = MockMartusApp.create(mockSecurityForApp);
		appWithServer.setServerInfo("mock", mockServer.getAccountId());
		appWithServer.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);
		
		appWithAccount = MockMartusApp.create(mockSecurityForApp);
		appWithAccount.setServerInfo("mock", mockServer.getAccountId());
		appWithAccount.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);

		mockServer.deleteAllData();
		
		mockServerNotAvailable.deleteAllFiles();
	}

	public void tearDown() throws Exception
	{
		appWithoutServer.deleteAllFiles();
		appWithServer.deleteAllFiles();
		appWithAccount.deleteAllFiles();
		mockServer.deleteAllFiles();
	}
	
	public void testGetMyBulletinSummariesWithServerError() throws Exception
	{
		String sampleSummary1 = "this is a basic summary";
		String sampleSummary2 = "another silly summary";
		String sampleSummary3 = "yet another!";

		Bulletin b1 = appWithAccount.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setSealed();
		b1.save();

		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		b2.save();

		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(true);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setSealed();
		b3.save();

		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));

		BulletinStore store = appWithAccount.getStore();
		store.destroyBulletin(b1);
		store.destroyBulletin(b2);
		store.destroyBulletin(b3);

		mockServer.countDownToGetPacketFailure = 2;

		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithAccount);
		try
		{
			model.initialize(null);
			model.checkIfErrorOccurred();
			fail("Didn't throw");
		}
		catch (ServerErrorException expectedExceptionToIgnore)
		{
		}
		Vector result = model.getDownloadableSummaries();
		assertEquals("wrong count?", 2, result.size());

		BulletinSummary s1 = (BulletinSummary)result.get(0);
		BulletinSummary s2 = (BulletinSummary)result.get(1);
		assertNotEquals(s1.getLocalId(), s2.getLocalId());
	}

	public void testGetMyBulletinSummariesNoServer() throws Exception
	{
		try
		{
			RetrieveMyTableModel model = new RetrieveMyTableModel(appWithoutServer);
			model.initialize(null);
			model.getMySummaries();
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}

		try
		{
			RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithoutServer);
			model.initialize(null);
			model.getMyDraftSummaries();
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid draft summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
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
			RetrieveMyTableModel model = new RetrieveMyTableModel(appWithServer);
			model.initialize(null);
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
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
		b1.save();
		
		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		b2.save();
		
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(true);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setDraft();
		b3.save();
		
		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));
		
		BulletinStore store = appWithAccount.getStore();
		store.destroyBulletin(b1);
		store.destroyBulletin(b2);
		store.destroyBulletin(b3);

		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithAccount);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector result = model.getDownloadableSummaries();
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
					assertEquals(b.get(Bulletin.TAGTITLE), s.getTitle());
					found[i] = true;
				}
			}
		}
		
		assertTrue("Missing 1?", found[0]);
		assertTrue("Missing 2?", found[1]);
	}

	public void testGetAllMySummaries() throws Exception
	{
		String sampleSummary1 = "1 basic summary";
		String sampleSummary2 = "2 silly summary";
		String sampleSummary3 = "3 yet another!";
		
		appWithAccount.getStore().deleteAllData();
		
		Bulletin b1 = appWithAccount.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setSealed();
		b1.save();
		
		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		b2.save();
				
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(true);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setSealed();
		b3.save();
		
		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));

		appWithAccount.getStore().destroyBulletin(b1);
		appWithAccount.getStore().destroyBulletin(b2);

		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithAccount);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector allResult = model.getAllSummaries();
		assertEquals("wrong all summaries count?", 3, allResult.size());

		BulletinSummary allS1 = (BulletinSummary)allResult.get(0);
		BulletinSummary allS2 = (BulletinSummary)allResult.get(1);
		BulletinSummary allS3 = (BulletinSummary)allResult.get(2);
		Bulletin allBulletins[] = new Bulletin[] {b1, b2, b3};
		BulletinSummary allSummaries[] = new BulletinSummary[] {allS1, allS2, allS3};
		
		for(int i = 0; i < allBulletins.length; ++i)
		{
			for(int j = 0; j < allSummaries.length; ++j)
			{
				Bulletin b = allBulletins[i];
				BulletinSummary s = allSummaries[j];
				if(b.getLocalId().equals(s.getLocalId()))
				{
					if(b.equals(b1))
						assertTrue("B1 not downloadable?", s.isDownloadable());
					if(b.equals(b2))
						assertTrue("B2 not downloadable?", s.isDownloadable());
					if(b.equals(b3))
						assertFalse("B3 downloadable?", s.isDownloadable());
				}
			}
		}
		model.checkIfErrorOccurred();
		Vector result = model.getDownloadableSummaries();
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
					assertEquals(b.get(Bulletin.TAGTITLE), s.getTitle());
					found[i] = true;
				}
				assertTrue("Not downloadable?", s.isDownloadable());
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
			RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithServer);
			model.initialize(null);
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
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
		b1.save();
		
		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setDraft();
		b2.save();
		
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(true);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setSealed();
		b3.save();
		
		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));

		BulletinStore store = appWithAccount.getStore();
		store.destroyBulletin(b1);
		store.destroyBulletin(b2);
		store.destroyBulletin(b3);

		RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithAccount);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector result = model.getDownloadableSummaries();
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
					assertEquals(b.get(Bulletin.TAGTITLE), s.getTitle());
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
			RetrieveHQTableModel model = new RetrieveHQTableModel(appWithoutServer);
			model.initialize(null);
			model.getFieldOfficeSealedSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid sealed summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		
		try
		{
			RetrieveHQDraftsTableModel model = new RetrieveHQDraftsTableModel(appWithoutServer);
			model.initialize(null);
			model.getFieldOfficeDraftSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid draft summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
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
			RetrieveHQTableModel model = new RetrieveHQTableModel(appWithServer);
			model.initialize(null);
			model.getFieldOfficeSealedSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected sealed didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}

		try
		{
			RetrieveHQDraftsTableModel model = new RetrieveHQDraftsTableModel(appWithServer);
			model.initialize(null);
			model.getFieldOfficeDraftSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected draft didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
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
		hqApp.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);
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
		b1.save();
		
		Bulletin b2 = appWithAccount.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		appWithAccount.setHQKeyInBulletin(b2);
		b2.save();
		
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setAllPrivate(false);
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setDraft();
		appWithAccount.setHQKeyInBulletin(b3);
		b3.save();

		mockServer.allowUploads(appWithAccount.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b1, null));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b2, null));
		assertEquals("failed upload3?", NetworkInterfaceConstants.OK, appWithAccount.uploadBulletin(b3, null));

		Vector desiredSealedResult = new Vector();
		desiredSealedResult.add(NetworkInterfaceConstants.OK);
		Vector list = new Vector();
		list.add(b1.getLocalId() + "=" + b1.getFieldDataPacket().getLocalId()+"=2000");
		list.add(b2.getLocalId() + "=" + b2.getFieldDataPacket().getLocalId()+"=2000");
		desiredSealedResult.add(list);
		mockServer.listFieldOfficeSummariesResponse = desiredSealedResult;	

		RetrieveHQTableModel model = new RetrieveHQTableModel(hqApp);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector returnedSealedResults = model.getDownloadableSummaries();
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
		Vector list2 = new Vector();
		list2.add(b3.getLocalId() + "=" + b3.getFieldDataPacket().getLocalId()+"=3400");
		desiredDraftResult.add(list2);
		mockServer.listFieldOfficeSummariesResponse = desiredDraftResult;	

		RetrieveHQDraftsTableModel model2 = new RetrieveHQDraftsTableModel(hqApp);
		model2.initialize(null);
		model2.checkIfErrorOccurred();
		Vector returnedDraftResults = model2.getDownloadableSummaries();
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
		
		public Vector listMySealedBulletinIds(String clientId, Vector retrieveTags)
		{
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.OK);
			Vector list = new Vector();
			list.add(b0.getLocalId() + "= " + b0.get(Bulletin.TAGTITLE) + "=3000");
			list.add(b1.getLocalId() + "= " + b1.get(Bulletin.TAGTITLE) + "=3200");
			list.add(b2.getLocalId() + "= " + b2.get(Bulletin.TAGTITLE) + "=3100");
			result.add(list);
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
