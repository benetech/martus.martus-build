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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.ChoiceItem;
import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiUtilities;
import org.martus.common.Bulletin;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;

public class TestMartusApp_NoServer extends TestCaseEnhanced
{
	public TestMartusApp_NoServer(String name)
	{
		super(name);
		VERBOSE = false;
	}

	public void setUp() throws Exception
	{
		TRACE_BEGIN("setUp");

		if(mockSecurityForApp == null)
			mockSecurityForApp = new MockMartusSecurity();

		appWithAccount = MockMartusApp.create(mockSecurityForApp);
		appWithAccount.setSSLNetworkInterfaceHandlerForTesting(new ServerSideNetworkHandlerNotAvailable());

		File keyPairFile = appWithAccount.getKeyPairFile();
		keyPairFile.delete();
		new File(appWithAccount.getConfigInfoFilename()).delete();
		new File(appWithAccount.getConfigInfoSignatureFilename()).delete();

		TRACE_END();
	}

	public void tearDown() throws Exception
	{
		appWithAccount.deleteAllFiles();
	}

	public void testBasics()
	{
		TRACE_BEGIN("testBasics");

		BulletinStore store = appWithAccount.getStore();
		assertNotNull("BulletinStore", store);
		TRACE_END();
	}

	public void testDbInitializerExceptionForMissingAccountMap() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;

		try
		{
			fakeDataDirectory = File.createTempFile("$$$MartusTestApp", null);
			fakeDataDirectory.deleteOnExit();
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			try
			{
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory);
				app.doAfterSigninInitalization();
				fail("Should have thrown because map is missing");
			}
			catch(MartusApp.MartusAppInitializationException expectedException)
			{
				assertEquals("wrong message?", "ErrorMissingAccountMap", expectedException.getMessage());
			}
		}
		finally
		{
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testDbInitializerExceptionForMissingAccountMapSignature() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;
		File acctMap = null;

		try
		{
			fakeDataDirectory = File.createTempFile("$$$MartusTestApp", null);
			fakeDataDirectory.deleteOnExit();
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			acctMap = new File(packetDirectory,"acctmap.txt");
			acctMap.deleteOnExit();

			FileOutputStream out = new FileOutputStream(acctMap.getPath(), true);
			UnicodeWriter writer = new UnicodeWriter(out);
			writer.writeln("noacct=123456789");
			writer.flush();
			out.flush();
			writer.close();

			try
			{
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory);
				app.doAfterSigninInitalization();
			}
			catch(MartusApp.MartusAppInitializationException unExpectedException)
			{
				fail("Should not have thrown because of missing map signature");
			}
		}
		finally
		{
			if(acctMap != null )
				acctMap.delete();
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testDbInitializerExceptionForInvalidAccountMapSignature() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;
		File acctMap = null;
		File signatureFile = null;

		try
		{
			fakeDataDirectory = File.createTempFile("$$$MartusTestApp", null);
			fakeDataDirectory.deleteOnExit();
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			acctMap = new File(packetDirectory,"acctmap.txt");
			acctMap.deleteOnExit();

			UnicodeWriter writer = new UnicodeWriter(acctMap);
			writer.writeln("noacct=123456789");
			writer.flush();
			writer.close();

			signatureFile = new File(packetDirectory,"acctmap.txt.sig");
			signatureFile.deleteOnExit();

			writer = new UnicodeWriter(signatureFile);
			writer.writeln("a fake signature");
			writer.flush();
			writer.close();

			try
			{
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory);
				app.doAfterSigninInitalization();
			}
			catch(MartusApp.MartusAppInitializationException unExpectedException)
			{
				fail("Should not have thrown because of invalid map signature");
			}
		}
		finally
		{
			if(acctMap != null )
				acctMap.delete();
			if(signatureFile != null)
				signatureFile.delete();
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testGetClientId()
	{
		TRACE_BEGIN("testGetClientId");
		String securityAccount = mockSecurityForApp.getPublicKeyString();
		String appAccount = appWithAccount.getAccountId();
		assertEquals("mock account wrong?", securityAccount, appAccount);
		Bulletin b = appWithAccount.createBulletin();
		assertEquals("client id wrong?", b.getAccount(), appWithAccount.getAccountId());
		TRACE_END();
	}
	
	public void testIsOurBulletin() throws Exception
	{
		TRACE_BEGIN("testIsOurBulletin");
		Bulletin b = appWithAccount.createBulletin();
		assertTrue("not our bulletin?", appWithAccount.isOurBulletin(b));

		MockMartusApp appWithDifferentAccount;
		appWithDifferentAccount = MockMartusApp.create(mockSecurityForApp);
		appWithDifferentAccount.createAccount("bogusName","bogusPassword");
		assertFalse("This is our bulletin?", appWithDifferentAccount.isOurBulletin(b));
		TRACE_END();
	}

	public void testConfigInfo() throws Exception
	{
		TRACE_BEGIN("testConfigInfo");

		File file = new File(appWithAccount.getConfigInfoFilename());
		file.delete();
		assertEquals("delete didn't work", false, file.exists());
		appWithAccount.loadConfigInfo();

		ConfigInfo originalInfo = appWithAccount.getConfigInfo();
		assertEquals("should be empty", "", originalInfo.getAuthor());

		originalInfo.setAuthor("blah");
		assertEquals("should have been set", "blah", appWithAccount.getConfigInfo().getAuthor());
		appWithAccount.saveConfigInfo();
		assertEquals("should still be there", "blah", appWithAccount.getConfigInfo().getAuthor());
		assertEquals("save didn't work!", true, file.exists());

		originalInfo.setAuthor("something else");
		appWithAccount.loadConfigInfo();
		assertNotNull("ConfigInfo null", appWithAccount.getConfigInfo());
		assertEquals("should have reloaded", "blah", appWithAccount.getConfigInfo().getAuthor());

		File sigFile = new File(appWithAccount.getConfigInfoSignatureFilename());
		sigFile.delete();
		appWithAccount.saveConfigInfo();
		assertTrue("Missing Signature file", sigFile.exists());
		appWithAccount.loadConfigInfo();
		assertEquals("blah", appWithAccount.getConfigInfo().getAuthor());
		sigFile.delete();
		try
		{
			appWithAccount.loadConfigInfo();
			fail("Should not have verified");
		}
		catch (MartusApp.LoadConfigInfoException e)
		{
			//Expected
		}
		assertEquals("", appWithAccount.getConfigInfo().getAuthor());

		TRACE_END();

	}

	public void testCreateAccount() throws Exception
	{
		TRACE_BEGIN("testCreateAccount");

		mockSecurityForApp.clearKeyPair();
		try
		{

			File badFile = new File(BAD_FILENAME);
			appWithAccount.createAccountInternal(badFile, userName, userPassword);
			fail("Can't create an account if we can't write the file!");

		}
		catch(MartusApp.CannotCreateAccountFileException e)
		{
			// expected exception
		}
		assertEquals("store account not unset on error?", false, mockSecurityForApp.hasKeyPair());


		File keyPairFile = appWithAccount.getKeyPairFile();
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		keyPairFile.delete();
		backupKeyPairFile.delete();
		appWithAccount.createAccount(userName, userPassword);
		assertEquals("no key file?", true, keyPairFile.exists());
		assertEquals("no backup key file?", true, backupKeyPairFile.exists());
		assertEquals("store account not set?", mockSecurityForApp.getPublicKeyString(), appWithAccount.getStore().getAccountId());
		assertEquals("User name not set?",userName, appWithAccount.getUserName());
		verifySignInThatWorks(appWithAccount);

		try
		{
			appWithAccount.createAccount(userName, userPassword);
			fail("Can't create an account if one already exists!");
		}
		catch(MartusApp.AccountAlreadyExistsException e)
		{
			// expected exception
		}
		assertEquals("store account not kept if already exists?", mockSecurityForApp.getPublicKeyString(), appWithAccount.getStore().getAccountId());

		TRACE_END();
	}

	void verifySignInThatWorks(MartusApp appWithRealAccount) throws Exception
	{
		assertEquals("should work", true, appWithRealAccount.attemptSignIn(userName, userPassword));
		assertEquals("store account not set?", mockSecurityForApp.getPublicKeyString(), appWithAccount.getStore().getAccountId());
		assertEquals("wrong username?", userName, appWithRealAccount.getUserName());
	}

	public void testSetAndGetHQKey() throws Exception
	{
		File configFile = new File(appWithAccount.getConfigInfoFilename());
		configFile.deleteOnExit();
		assertEquals("already exists?", false, configFile.exists());
		String sampleHQKey = "abc123";
		appWithAccount.setHQKey(sampleHQKey);
		assertEquals("Incorrect public key", sampleHQKey, appWithAccount.getHQKey());
		assertEquals("Didn't save?", true, configFile.exists());
	}

	public void testClearHQKey() throws Exception
	{
		File configFile = new File(appWithAccount.getConfigInfoFilename());
		configFile.deleteOnExit();
		assertEquals("already exists?", false, configFile.exists());
		appWithAccount.clearHQKey();
		assertEquals("HQ key exists?", "", appWithAccount.getHQKey());
		assertEquals("Didn't save?", true, configFile.exists());

		String sampleHQKey = "abc123";
		appWithAccount.setHQKey(sampleHQKey);
		assertEquals("Incorrect public key", sampleHQKey, appWithAccount.getHQKey());
		appWithAccount.clearHQKey();
		assertEquals("HQ not cleared", "", appWithAccount.getHQKey());
	}

	public void testGetCombinedPassPhrase()
	{
		String combined1 = appWithAccount.getCombinedPassPhrase(userName, userPassword);
		String combined2 = appWithAccount.getCombinedPassPhrase(userName2, userPassword);
		String combined3 = appWithAccount.getCombinedPassPhrase(userName, userPassword2);
		assertNotEquals("username diff", combined1, combined2);
		assertNotEquals("password diff", combined1, combined3);

		String ab_c = appWithAccount.getCombinedPassPhrase("ab", "c");
		String a_bc = appWithAccount.getCombinedPassPhrase("a", "bc");
		assertNotEquals("abc diff", ab_c, a_bc);
	}

	public void testAttemptSignInBadKeyPairFile() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInBadKeyPairFile");

		File badFile = new File(BAD_FILENAME);
		assertEquals("bad file", false, appWithAccount.attemptSignInInternal(badFile, userName, userPassword));
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		assertEquals("non-blank username?", "", appWithAccount.getUserName());
		appWithAccount.security.createKeyPair();
		TRACE_END();
	}

	public void testAttemptSignInAuthorizationFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInAuthorizationFailure");

		mockSecurityForApp.fakeAuthorizationFailure = true;
		assertEquals("should fai1", false, appWithAccount.attemptSignIn(userName, userPassword));
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		mockSecurityForApp.fakeAuthorizationFailure = false;
		appWithAccount.security.createKeyPair();

		TRACE_END();
	}

	public void testAttemptSignInKeyPairVersionFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInKeyPairVersionFailure");

		mockSecurityForApp.fakeKeyPairVersionFailure = true;
		assertEquals("should fail2", false, appWithAccount.attemptSignIn(userName, userPassword));
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		mockSecurityForApp.fakeKeyPairVersionFailure = false;
		appWithAccount.security.createKeyPair();

		TRACE_END();
	}

	public void testDoesAccountExist() throws Exception
	{
		TRACE_BEGIN("testDoesAccountExist");

		File keyPairFile = appWithAccount.getKeyPairFile();
		keyPairFile.delete();
		assertEquals("account exists without a file?", false, appWithAccount.doesAccountExist());

		FileOutputStream out = new FileOutputStream(keyPairFile);
		out.write(0);
		out.close();

		assertEquals("account doesn't exist with a file?", true, appWithAccount.doesAccountExist());

		keyPairFile.delete();

		TRACE_END();
	}


	public void testCreateBulletin() throws Exception
	{
		TRACE_BEGIN("testCreateBulletin");
		mockSecurityForApp.loadSampleAccount();
		ConfigInfo info = appWithAccount.getConfigInfo();
		String source = "who?";
		String organization = "those guys";
		String template = "Was there a bomb?";
		info.setAuthor(source);
		info.setOrganization(organization);
		info.setTemplateDetails(template);
		Bulletin b = appWithAccount.createBulletin();
		assertNotNull("null Bulletin", b);
		assertEquals(source, b.get(Bulletin.TAGAUTHOR));
		assertEquals(organization, b.get(Bulletin.TAGORGANIZATION));
		assertEquals(template, b.get(Bulletin.TAGPUBLICINFO));
		assertEquals(Bulletin.STATUSDRAFT, b.getStatus());
		assertEquals("not automatically private?", true, b.isAllPrivate());
		TRACE_END();
	}

	public void testLoadBulletins() throws Exception
	{
		TRACE_BEGIN("testLoadBulletins");
		mockSecurityForApp.loadSampleAccount();

		appWithAccount.loadSampleData(); //SLOW!!!

		BulletinStore store = appWithAccount.getStore();
		int sampleCount = store.getBulletinCount();
		assertTrue("Should start with samples", sampleCount > 0);

		appWithAccount.loadFolders();
		assertEquals("Should have loaded samples", sampleCount, store.getBulletinCount());
		BulletinFolder sent = store.getFolderSent();
		assertEquals("Sent should have bulletins", sampleCount, sent.getBulletinCount());

		store.deleteAllData();
		assertEquals("Should have deleted samples", 0, store.getBulletinCount());
		appWithAccount.loadFolders();

		TRACE_END();
	}

	public void testSearch()
	{
		TRACE_BEGIN("testSearch");
		BulletinStore store = appWithAccount.getStore();
		String startDate = "1900-01-01";
		String endDate = "2099-12-31";
		assertNull("Search results already exists?", store.findFolder(store.getSearchFolderName()));

		appWithAccount.loadSampleData(); //SLOW!!!
		Bulletin b = store.findBulletinByUniversalId((UniversalId)store.getAllBulletinUids().get(0));
		appWithAccount.search(b.get("title"), startDate, endDate);
		assertNotNull("Search results should have been created", store.getSearchFolderName());

		appWithAccount.search("--not in any bulletin--", startDate, endDate);
		assertEquals("search should clear results folder", 0, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		assertTrue("not enough bulletins?", appWithAccount.getStore().getBulletinCount() >= 5);
		assertTrue("too many bulletins?", appWithAccount.getStore().getBulletinCount() <= 15);
		appWithAccount.search(b.get("author"), startDate, endDate);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		appWithAccount.search(b.get(""), startDate, endDate);
		assertEquals(10, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		startDate = "1999-01-19";
		endDate = startDate;
		appWithAccount.search(b.get(""), startDate, endDate);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		TRACE_END();
	}

	public void testFindBulletinInAllFolders() throws Exception
	{
		TRACE_BEGIN("testFindBulletinInAllFolders");
		MockMartusApp app = MockMartusApp.create();
		Bulletin b1 = app.createBulletin();
		Bulletin b2 = app.createBulletin();
		app.getStore().saveBulletin(b1);
		app.getStore().saveBulletin(b2);

		assertEquals("Found the bulletin already in a folder?", 0, app.findBulletinInAllVisibleFolders(b1).size());
		BulletinFolder f1 = app.createUniqueFolder();
		BulletinFolder f2 = app.createUniqueFolder();
		BulletinFolder f3 = app.createUniqueFolder();
		BulletinFolder f4 = app.getFolderDraftOutbox();
		f1.add(b1);
		f2.add(b2);
		f3.add(b1);
		f3.add(b2);
		f4.add(b2);
		BulletinFolder discarded = app.getFolderDiscarded();
		discarded.add(b2);

		Vector v1 = app.findBulletinInAllVisibleFolders(b1);
		Vector v2 = app.findBulletinInAllVisibleFolders(b2);
		assertEquals("Wrong # of folders for b1?", 2, v1.size());
		assertEquals("Wrong # of folders for b2?", 3, v2.size());
		assertTrue("Doesn't contain f1 for bulletin b1?", v1.contains(f1));
		assertEquals("Does contain f2 for bulletin b1?", false, v1.contains(f2));
		assertTrue("Doesn't contain f3 for bulletin b1?", v1.contains(f3));
		assertEquals("Does contain Discarded for bulletin b1?",false, v1.contains(discarded));

		assertEquals("Does contain f1 for bulletin b2?", false, v2.contains(f1));
		assertTrue("Doesn't contain f2 for bulletin b2?", v2.contains(f2));
		assertTrue("Doesn't contain f3 for bulletin b2?", v2.contains(f3));
		assertTrue("Doesn't contain Discarded for bulletin b2?", v2.contains(discarded));
		TRACE_END();
	}

	public void testGetPublicCodeFromAccount() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		String publicKeyString = security.getPublicKeyString();
		String publicCode = MartusUtilities.computePublicCode(publicKeyString);
		assertEquals("wrong code?", "71887634433124687372", publicCode);
	}

	public void testGetFileLength() throws Exception
	{
		class MockFile extends File
		{
			MockFile()
			{
				super(".");
			}

			public long length()
			{
				return mockLength;
			}

			long mockLength;
		}

		MockFile mockFile = new MockFile();
		final int normalLength = 555;
		mockFile.mockLength = normalLength;
		assertEquals(normalLength, MartusUtilities.getCappedFileLength(mockFile));

		mockFile.mockLength = 10L *1024*1024*1024;
		try
		{
			MartusUtilities.getCappedFileLength(mockFile);
			fail("Should have thrown too large for big number");
		}
		catch(MartusUtilities.FileTooLargeException ignoreExpectedException)
		{
		}


		mockFile.mockLength = -255;
		try
		{
			MartusUtilities.getCappedFileLength(mockFile);
			fail("Should have thrown too large for negative number");
		}
		catch(MartusUtilities.FileTooLargeException ignoreExpectedException)
		{
		}
	}

	public void testEncryptPublicData() throws Exception
	{
		TRACE_BEGIN("testEncryptPublicData");
		MockMartusApp app = MockMartusApp.create();
		assertEquals("App Not Encypting Public?", true, app.getStore().mustEncryptPublicData());
		app.deleteAllFiles();

		TRACE_END();
	}

	public void testExportPublicInfo() throws Exception
	{
		File temp = createTempFile();
		temp.delete();
		appWithAccount.exportPublicInfo(temp);
		assertTrue("not created?", temp.exists());
		UnicodeReader reader = new UnicodeReader(temp);
		String publicKey = reader.readLine();
		String signature = reader.readLine();
		reader.close();
		assertEquals("Public Key wrong?", appWithAccount.getSecurity().getPublicKeyString(), publicKey);
		appWithAccount.validatePublicInfo(publicKey, signature);
	}

	public void testExtractPublicInfo() throws Exception
	{
		File temp = createTempFile();
		temp.delete();
		appWithAccount.exportPublicInfo(temp);
		String publicKey = appWithAccount.extractPublicInfo(temp);
		assertEquals("Public Key wrong?", appWithAccount.getSecurity().getPublicKeyString(), publicKey);

		UnicodeWriter writer = new UnicodeWriter(temp);
		writer.write("flkdjfl");
		writer.close();
		try
		{
			appWithAccount.extractPublicInfo(temp);
			fail("Should have thrown exception");
		}
		catch (Exception ignoreExpectedException)
		{
		}
	}

	public void testCenter()
	{
		TRACE_BEGIN("testCenter");
		{
			Point upperLeft = UiUtilities.center(new Dimension(800, 600), new Rectangle(0, 0, 800, 600));
			assertEquals(0, upperLeft.x);
			assertEquals(0, upperLeft.y);
		}
		{
			Point upperLeft = UiUtilities.center(new Dimension(400, 300), new Rectangle(0, 0, 800, 600));
			assertEquals(200, upperLeft.x);
			assertEquals(150, upperLeft.y);
		}
		TRACE_END();
	}

	public void testFieldLabels()
	{
		TRACE_BEGIN("testFieldLabels");
		assertEquals("Keep ALL Information Private", appWithAccount.getFieldLabel("allprivate"));
		assertEquals("Author", appWithAccount.getFieldLabel("author"));
		assertEquals("Organization", appWithAccount.getFieldLabel("organization"));
		assertEquals("Title", appWithAccount.getFieldLabel("title"));
		assertEquals("Location", appWithAccount.getFieldLabel("location"));
		assertEquals("Date of Event", appWithAccount.getFieldLabel("eventdate"));
		assertEquals("Date Entered", appWithAccount.getFieldLabel("entrydate"));
		assertEquals("Keywords", appWithAccount.getFieldLabel("keywords"));
		assertEquals("Summary", appWithAccount.getFieldLabel("summary"));
		assertEquals("Details", appWithAccount.getFieldLabel("publicinfo"));
		assertEquals("Private", appWithAccount.getFieldLabel("privateinfo"));
		assertEquals("Language", appWithAccount.getFieldLabel("language"));
		TRACE_END();
	}

	public void testFolderLabels()
	{
		//assertEquals("Retrieved Bulletins", appWithAccount.getFolderLabel("%RetrievedMyBulletin"));
		//assertEquals("Field Desk Bulletins", appWithAccount.getFolderLabel("%RetrievedFieldOfficeBulletin"));
	}

	public void testLanguageNames()
	{
		TRACE_BEGIN("testLanguageNames");
		assertNotNull(appWithAccount.getLanguageName("Not a valid code"));
		assertEquals("English", appWithAccount.getLanguageName("en"));
		assertEquals("Arabic", appWithAccount.getLanguageName("ar"));
		assertEquals("Azerbaijani", appWithAccount.getLanguageName("az"));
		assertEquals("Bengali", appWithAccount.getLanguageName("bn"));
		assertEquals("Burmese", appWithAccount.getLanguageName("my"));
		assertEquals("Chinese", appWithAccount.getLanguageName("zh"));
		assertEquals("Dutch", appWithAccount.getLanguageName("nl"));
		assertEquals("Esperanto", appWithAccount.getLanguageName("eo"));
		assertEquals("French", appWithAccount.getLanguageName("fr"));
		assertEquals("German", appWithAccount.getLanguageName("de"));
		assertEquals("Gujarati", appWithAccount.getLanguageName("gu"));
		assertEquals("Hausa", appWithAccount.getLanguageName("ha"));
		assertEquals("Hebrew", appWithAccount.getLanguageName("he"));
		assertEquals("Hindi", appWithAccount.getLanguageName("hi"));
		assertEquals("Hungarian", appWithAccount.getLanguageName("hu"));
		assertEquals("Italian", appWithAccount.getLanguageName("it"));
		assertEquals("Japanese", appWithAccount.getLanguageName("ja"));
		assertEquals("Javanese", appWithAccount.getLanguageName("jv"));
		assertEquals("Kannada", appWithAccount.getLanguageName("kn"));
		assertEquals("Korean", appWithAccount.getLanguageName("ko"));
		assertEquals("Malayalam", appWithAccount.getLanguageName("ml"));
		assertEquals("Marathi", appWithAccount.getLanguageName("mr"));
		assertEquals("Oriya", appWithAccount.getLanguageName("or"));
		assertEquals("Panjabi", appWithAccount.getLanguageName("pa"));
		assertEquals("Polish", appWithAccount.getLanguageName("pl"));
		assertEquals("Portuguese", appWithAccount.getLanguageName("pt"));
		assertEquals("Romanian", appWithAccount.getLanguageName("ro"));
		assertEquals("Russian", appWithAccount.getLanguageName("ru"));
		assertEquals("Serbian", appWithAccount.getLanguageName("sr"));
		assertEquals("Sindhi", appWithAccount.getLanguageName("sd"));
		assertEquals("Sinhalese", appWithAccount.getLanguageName("si"));
		assertEquals("Spanish", appWithAccount.getLanguageName("es"));
		assertEquals("Tamil", appWithAccount.getLanguageName("ta"));
		assertEquals("Telugu", appWithAccount.getLanguageName("te"));
		assertEquals("Thai", appWithAccount.getLanguageName("th"));
		assertEquals("Turkish", appWithAccount.getLanguageName("tr"));
		assertEquals("Ukranian", appWithAccount.getLanguageName("uk"));
		assertEquals("Urdu", appWithAccount.getLanguageName("ur"));
		assertEquals("Vietnamese", appWithAccount.getLanguageName("vi"));
		TRACE_END();
	}

	public void testGetLanguageNameChoices()
	{
		TRACE_BEGIN("testWindowTitles");
		String[] testLanguageCodes = {"es", "en", "si"};
		ChoiceItem[] languageChoicesTest = appWithAccount.getLanguageNameChoices(testLanguageCodes);
		assertEquals(languageChoicesTest[0].toString(), appWithAccount.getLanguageName("en"));
		assertEquals(languageChoicesTest[1].toString(), appWithAccount.getLanguageName("si"));
		assertEquals(languageChoicesTest[2].toString(), appWithAccount.getLanguageName("es"));
		TRACE_END();
	}

	public void testWindowTitles()
	{
		TRACE_BEGIN("testWindowTitles");
		assertEquals("Martus Human Rights Bulletin System", appWithAccount.getWindowTitle("main"));
		TRACE_END();
	}

	public void testButtonLabels()
	{
		TRACE_BEGIN("testButtonLabels");
		assertEquals("Help", appWithAccount.getButtonLabel("help"));
		TRACE_END();
	}

	public void testMenuLabels()
	{
		TRACE_BEGIN("testMenuLabels");
		assertEquals("File", appWithAccount.getMenuLabel("file"));
		TRACE_END();
	}

	public void testCurrentLanguage()
	{
		TRACE_BEGIN("testCurrentLanguage");
		assertEquals("en", appWithAccount.getCurrentLanguage());
		assertEquals("MartusHelp-en.txt", appWithAccount.getHelpFilename());
		assertEquals("MartusHelp-en.txt", appWithAccount.getEnglishHelpFilename());
		appWithAccount.setCurrentLanguage("es");
		assertEquals("MartusHelp-es.txt", appWithAccount.getHelpFilename());
		assertEquals("es", appWithAccount.getCurrentLanguage());
		char iWithAccentInUtf8 = 237;
		char[] titleInSpanish = {'T', iWithAccentInUtf8, 't', 'u', 'l', 'o'};
		assertEquals(new String(titleInSpanish), appWithAccount.getFieldLabel("title"));
		appWithAccount.setCurrentLanguage("en");
		TRACE_END();
	}

	public void testDateConvert()
	{
		TRACE_BEGIN("testDateConvert");
		assertEquals("12/13/1987", appWithAccount.convertStoredToDisplay("1987-12-13"));
		assertEquals("", appWithAccount.convertStoredToDisplay("abc"));
		assertEquals("", appWithAccount.convertStoredToDisplay("1987-13-13"));
		TRACE_END();
	}

	public void testCurrentDateFormatCode()
	{
		TRACE_BEGIN("testCurrentDateFormatCode");
		assertEquals("MM/dd/yyyy", appWithAccount.getCurrentDateFormatCode());
		appWithAccount.setCurrentDateFormatCode("dd.MM.yyyy");
		assertEquals("dd.MM.yyyy", appWithAccount.getCurrentDateFormatCode());
		appWithAccount.setCurrentDateFormatCode("MM/dd/yyyy");
		assertEquals("MM/dd/yyyy", appWithAccount.getCurrentDateFormatCode());
		TRACE_END();
	}

	public void testMonthLabels()
	{
		TRACE_BEGIN("testMonthLabels");
		assertEquals("Mar", appWithAccount.getMonthLabel("mar"));
		String[] months = appWithAccount.getMonthLabels();
		assertEquals("Jan", months[0]);
		appWithAccount.setCurrentLanguage("es");
		months = appWithAccount.getMonthLabels();
		assertEquals("Ene", months[0]);
		appWithAccount.setCurrentLanguage("en");
		TRACE_END();
	}

	public void testStatusLabels()
	{
		TRACE_BEGIN("testStatusLabels");
		assertEquals("Draft", appWithAccount.getStatusLabel(Bulletin.STATUSDRAFT));
		assertEquals("Sealed", appWithAccount.getStatusLabel(Bulletin.STATUSSEALED));
		TRACE_END();
	}

	public void testCreateFolders() throws Exception
	{
		TRACE_BEGIN("testCreateFolders");
		final int MAXFOLDERS = 10;
		appWithAccount.setMaxNewFolders(MAXFOLDERS);
		assertNotNull("New Folder is null?", appWithAccount.createUniqueFolder());
		assertNotNull("Could not find first new folder", appWithAccount.store.findFolder("New Folder"));

		for(int i = 1; i < MAXFOLDERS; ++i)
		{
			assertNotNull("Folder"+i+" is null?", appWithAccount.createUniqueFolder());
			assertNotNull("Could not find new folder"+i, appWithAccount.store.findFolder("New Folder"+i));
		}
		assertNull("Max Folders reached, why is this not null?", appWithAccount.createUniqueFolder());
		assertNull("Found this folder"+MAXFOLDERS, appWithAccount.store.findFolder("New Folder"+MAXFOLDERS));
		TRACE_END();
	}

	public void testFormatPublicCode() throws Exception
	{
		TRACE_BEGIN("testCreateFolders");
		String clientId = appWithAccount.getAccountId();
		assertNotNull("clientId Null?", clientId);
		String publicCode = MartusUtilities.computePublicCode(clientId);
		assertNotNull("publicCode Null?", publicCode);
		String formattedCode = MartusUtilities.formatPublicCode(publicCode);
		assertNotEquals("formatted code is the same as the public code?", formattedCode, publicCode);
		assertEquals("Not formatted correctly", "1234.5678.9012.3456", MartusUtilities.formatPublicCode("1234567890123456"));
		TRACE_END();

	}

	public void testShouldShowSealedUploadReminderOnExit() throws Exception
	{
		TRACE_BEGIN("testShouldShowSealedUploadReminderOnExit");
		File file = appWithAccount.getUploadInfoFile();
		file.delete();
		BulletinStore store = appWithAccount.getStore();

		store.deleteAllData();
		BulletinFolder outbox = appWithAccount.getFolderOutbox();
		assertEquals("Outbox not empty on exit", 0, outbox.getBulletinCount());
		assertEquals("No file and outbox empty on exit", false,
			appWithAccount.shouldShowSealedUploadReminderOnExit());

		Bulletin b = appWithAccount.createBulletin();
		appWithAccount.getStore().saveBulletin(b);
		store.addBulletinToFolder(b.getUniversalId(), outbox);
		assertEquals("File got created somehow on exit?", false, file.exists());
		assertEquals("Outbox empty on exit", 1, outbox.getBulletinCount());
		assertEquals("No file and outbox contains data on exit", true,
						appWithAccount.shouldShowSealedUploadReminderOnExit());

		TRACE_END();
	}

	public void testRepairOrphans()
	{
		assertEquals("already have orphans?", 0, appWithAccount.repairOrphans());
		assertNull("Orphan Folder exists?", appWithAccount.getStore().findFolder(BulletinStore.RECOVERED_BULLETIN_FOLDER));
		Bulletin b1 = appWithAccount.createBulletin();
		appWithAccount.getStore().saveBulletin(b1);
		assertEquals("didn't find the orphan?", 1, appWithAccount.repairOrphans());
		assertEquals("didn't fix the orphan?", 0, appWithAccount.repairOrphans());

		BulletinFolder orphanFolder = appWithAccount.getStore().findFolder(BulletinStore.RECOVERED_BULLETIN_FOLDER);
		assertEquals("where did the orphan go?", 1, orphanFolder.getBulletinCount());
		assertTrue("wrong bulletin?", orphanFolder.contains(b1));

		appWithAccount.loadFolders();
		BulletinFolder orphanFolder2 = appWithAccount.getStore().findFolder(BulletinStore.RECOVERED_BULLETIN_FOLDER);
		assertEquals("forgot to save folders?", 1, orphanFolder2.getBulletinCount());
	}

	public void testSetBulletinHQKey() throws Exception
	{
		String key = "aabcc";
		appWithAccount.setHQKey(key);

		Bulletin b1 = appWithAccount.createBulletin();
		assertEquals("key already set?", "", b1.getHQPublicKey());
		appWithAccount.setHQKeyInBulletin(b1);
		assertEquals("Key not set?", key, b1.getHQPublicKey());
	}

	private static MockMartusSecurity mockSecurityForApp;

	private MockMartusApp appWithAccount;

	static final String userName = "testuser";
	static final String userName2 = "testuse!";
	static final String userPassword = "12345";
	static final String userPassword2 = "12347";
}
