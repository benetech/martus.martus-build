package org.martus.meta;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.client.Bulletin;
import org.martus.client.BulletinStore;
import org.martus.client.MockBulletin;
import org.martus.common.AttachmentProxy;
import org.martus.common.Base64;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceXmlRpcConstants;
import org.martus.common.StringInputStream;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.server.MartusServer;
import org.martus.server.MockMartusServer;
import org.martus.server.ServerSideNetworkHandler;
import org.martus.server.ServerSideNetworkHandlerForNonSSL;


public class TestMartusServer extends TestCaseEnhanced implements NetworkInterfaceConstants
{
	public TestMartusServer(String name) throws Exception
	{
		super(name);
		

/*
 * This code creates a key pair and prints it, so you can 
 * use it to hard code in a test */
//		MartusSecurity security = new MartusSecurity(12345);
//		security.createKeyPair();
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		security.writeKeyPair(out, "test");
//		System.out.println(Base64.encode(out.toByteArray()));

	}

	public void setUp() throws Exception
	{
		if(clientSecurity == null)
		{
			clientSecurity = new MartusSecurity();
			clientSecurity.createKeyPair(512);
		}
		
		if(serverSecurity == null)
		{
			serverSecurity = new MartusSecurity();
			serverSecurity.createKeyPair(512);
		}

		if(hqSecurity == null)
		{
			hqSecurity = new MartusSecurity();
			hqSecurity.createKeyPair(512);
		}
		if(tempFile == null)
		{
			tempFile = File.createTempFile("$$$MartusTestMartusServer", null);
			tempFile.deleteOnExit();
			tempFile.delete();
		}
		if(store == null)
		{
			store = new BulletinStore(new MockServerDatabase());
			store.setSignatureGenerator(clientSecurity);
		}
		if(b1 == null)
		{
			b1 = store.createEmptyBulletin();
			b1.set(b1.TAGTITLE, "Title1");
			b1.set(b1.TAGPUBLICINFO, "Details1");
			b1.set(b1.TAGPRIVATEINFO, "PrivateDetails1");
			File attachment = createTempFile();
			FileOutputStream out = new FileOutputStream(attachment);
			out.write(b1AttachmentBytes);
			out.close();
			b1.addPublicAttachment(new AttachmentProxy(attachment));
			b1.addPrivateAttachment(new AttachmentProxy(attachment));
			b1.setHQPublicKey(hqSecurity.getPublicKeyString());
			b1.setSealed();
			b1.save();
			b1 = Bulletin.loadFromDatabase(store, DatabaseKey.createSealedKey(b1.getUniversalId()));
	
			b2 = store.createEmptyBulletin();
			b2.set(b2.TAGTITLE, "Title2");
			b2.set(b2.TAGPUBLICINFO, "Details2");
			b2.set(b1.TAGPRIVATEINFO, "PrivateDetails2");
			b2.setSealed();
			b2.save();
			
			draft = store.createEmptyBulletin();
			draft.set(draft.TAGPUBLICINFO, "draft public");
			draft.setDraft();
			draft.save();


			privateBulletin = store.createEmptyBulletin();
			privateBulletin.setAllPrivate(true);
			privateBulletin.set(privateBulletin.TAGTITLE, "TitlePrivate");
			privateBulletin.set(privateBulletin.TAGPUBLICINFO, "DetailsPrivate");
			privateBulletin.set(privateBulletin.TAGPRIVATEINFO, "PrivateDetailsPrivate");
			privateBulletin.setSealed();
			privateBulletin.save();

			b1ZipString = MockBulletin.saveToZipString(b1);
			b1ZipBytes = Base64.decode(b1ZipString);
			b1ChunkBytes0 = new byte[100];
			b1ChunkBytes1 = new byte[b1ZipBytes.length - b1ChunkBytes0.length];
			System.arraycopy(b1ZipBytes, 0, b1ChunkBytes0, 0, b1ChunkBytes0.length);
			System.arraycopy(b1ZipBytes, b1ChunkBytes0.length, b1ChunkBytes1, 0, b1ChunkBytes1.length);
			b1ChunkData0 = Base64.encode(b1ChunkBytes0);
			b1ChunkData1 = Base64.encode(b1ChunkBytes1);
			
		}
		
		testServer = new MockMartusServer();
		testServerInterface = new ServerSideNetworkHandler(testServer);
		db = (MockServerDatabase)testServer.getDatabase();

	}
	
	public void tearDown() throws Exception
	{
		testServer.deleteAllFiles();
	}
	
	public void testLegacyApiMethodNamesNonSSL()
	{
		Method[] methods = ServerSideNetworkHandlerForNonSSL.class.getMethods();
		Vector names = new Vector();
		for(int i=0; i < methods.length; ++i)
			names.add(methods[i].getName());
		//TODO use real quoted strings instead.
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_PING, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_SERVER_INFO, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_UPLOAD_RIGHTS, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_UPLOAD, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_DOWNLOAD, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_MY_SUMMARIES, names);
	}

	public void testLegacyApiMethodNamesSSL()
	{
		Method[] methods = ServerSideNetworkHandler.class.getMethods();
		Vector names = new Vector();
		for(int i=0; i < methods.length; ++i)
			names.add(methods[i].getName());

		//TODO use real quoted strings instead.
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_PING, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_UPLOAD_RIGHTS, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_UPLOAD_CHUNK, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_DOWNLOAD_CHUNK, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_MY_SUMMARIES, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_DOWNLOAD_FIELD_OFFICE_CHUNK, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_FIELD_OFFICE_SUMMARIES, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_FIELD_OFFICE_ACCOUNTS, names);
		assertContains(NetworkInterfaceXmlRpcConstants.CMD_DOWNLOAD_FIELD_DATA_PACKET, names);
	}


	public void testPing() throws Exception
	{
		testServer.setSecurity(serverSecurity);
		assertEquals(NetworkInterfaceConstants.VERSION, testServer.ping());
	}
	
	public void testCreateInterimBulletinFile() throws Exception
	{
		File zipFile = createTempFile("$$$MartusServerBulletinZip");
		File zipSignature = MartusUtilities.getSignatureFileFromFile(zipFile);
		zipSignature.deleteOnExit();
		assertFalse("Null files verified?", testServer.verifyBulletinInterimFile(zipFile, zipSignature));
		
		File file = createTempFile();
		FileOutputStream out = new FileOutputStream(file);
		out.write(file1Bytes);
		out.close();
		assertFalse("Null zip files verified?", testServer.verifyBulletinInterimFile(file, zipSignature));

		zipSignature = MartusUtilities.createSignatureFileFromFile(file, serverSecurity);
		zipSignature.deleteOnExit();
		assertTrue("Did not verify?", testServer.verifyBulletinInterimFile(file, zipSignature));
		
		File file2 = createTempFile();
		FileOutputStream out2 = new FileOutputStream(file2);
		out2.write(file2Bytes);
		out2.close();
		assertFalse("File1's signature verified with File2?", testServer.verifyBulletinInterimFile(file2, zipSignature));
	}
	
	public void testPutContactInfo() throws Exception
	{
		Vector contactInfo = new Vector();
		String clientId = store.getAccountId();
		String resultIncomplete = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Empty ok?", INVALID_DATA, resultIncomplete);

		contactInfo.add("bogus data");
		resultIncomplete = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Incorrect not Incomplete?", INVALID_DATA, resultIncomplete);
		
		contactInfo.clear();
		contactInfo.add(clientId);
		contactInfo.add(new Integer(1));
		contactInfo.add("Data");
		contactInfo.add("invalid Signature");
		String invalidSig = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Invalid Signature", SIG_ERROR, invalidSig);		

		contactInfo.clear();
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		contactInfo.add("Data");
		contactInfo.add("Data2");
		String signature = MartusUtilities.sign(contactInfo, clientSecurity);
		contactInfo.add(signature);
		String incorrectAccoutResult = testServer.putContactInfo("differentAccountID", contactInfo);
		assertEquals("Incorrect Accout ", INVALID_DATA, incorrectAccoutResult);		

		File contactFile = testServer.getContactInfoFileForAccount(clientId);
		assertFalse("Contact File already exists?", contactFile.exists());		
		String correctResult = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Correct Signature", OK, correctResult);		

		assertTrue("File Doesn't exist?", contactFile.exists());
		assertTrue("Size too small", contactFile.length() > 200);

		FileInputStream contactFileInputStream = new FileInputStream(contactFile);
		DataInputStream in = new DataInputStream(contactFileInputStream);

		String inputPublicKey = in.readUTF();
		int inputDataCount = in.readInt();
		String inputData =  in.readUTF();
		String inputData2 =  in.readUTF();
		String inputSig = in.readUTF();
		in.close();

		assertEquals("Public key doesn't match", clientId, inputPublicKey);
		assertEquals("data size not two?", 2, inputDataCount);
		assertEquals("data not correct?", "Data", inputData);
		assertEquals("data2 not correct?", "Data2", inputData2);
		assertEquals("signature doesn't match?", signature, inputSig);		

		contactFile.delete();
		contactFile.getParentFile().delete();
	}
	
	public void testPutContactInfoThroughHandler() throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(store.getAccountId());
		parameters.add(new Integer(1));
		parameters.add("Data");
		String signature = MartusUtilities.sign(parameters, clientSecurity);
		parameters.add(signature);

		String sig = MartusUtilities.sign(parameters, clientSecurity);
		String clientId = store.getAccountId();
		
		Vector result = testServerInterface.putContactInfo(clientId, parameters, sig);
		File contactFile = testServer.getContactInfoFileForAccount(clientId);
		assertEquals("Result size?", 1, result.size());
		assertEquals("Result not ok?", OK, result.get(0));

		contactFile.delete();
		contactFile.getParentFile().delete();
	}


	public void testGetAccountInformation() throws Exception
	{
		testServer.setSecurity(serverSecurity);

		String knownAccountId = testServer.getAccountId();
		assertNotNull("null account?", knownAccountId);
		
		Vector serverInfo = testServer.getServerInformation();
		assertEquals(3, serverInfo.size());
		assertEquals(NetworkInterfaceConstants.OK, serverInfo.get(0));

		String accountId = (String)serverInfo.get(1);
		String sig = (String)serverInfo.get(2);

		assertEquals("Got wrong account back?", knownAccountId, accountId);
		verifyAccountInfo("bad test sig?", accountId, sig);
	}
	
	public void testGetAccountInformationNoAccount() throws Exception
	{
		testServer.security.clearKeyPair();

		Vector errorInfo = testServer.getServerInformation();
		assertEquals(2, errorInfo.size());
		assertEquals(NetworkInterfaceConstants.SERVER_ERROR, errorInfo.get(0));

	}

	private void verifyAccountInfo(String label, String accountId, String sig) throws 
			UnsupportedEncodingException, 
			MartusSignatureException, 
			InvalidBase64Exception 
	{
		byte[] accountIdBytes = Base64.decode(accountId);

		ByteArrayInputStream in = new ByteArrayInputStream(accountIdBytes);
		byte[] expectedSig = serverSecurity.createSignature(in);
		assertEquals(label + " encoded sig wrong?", Base64.encode(expectedSig), sig);

		ByteArrayInputStream dataInClient = new ByteArrayInputStream(accountIdBytes);
		boolean ok1 = clientSecurity.isSignatureValid(accountId, dataInClient, Base64.decode(sig));
		assertEquals(label + " client verifySig failed", true, ok1);

		ByteArrayInputStream dataInServer = new ByteArrayInputStream(accountIdBytes);
		boolean ok2 = serverSecurity.isSignatureValid(accountId, dataInServer, Base64.decode(sig));
		assertEquals(label + " server verifySig failed", true, ok2);
	}
	
	public void testUploadBulletinUnauthorizedAccount() throws Exception
	{
		testServer.clientsThatCanUpload.clear();

		String wrongAccount = clientSecurity.getPublicKeyString();
		assertEquals(NetworkInterfaceConstants.REJECTED, testServer.uploadBulletin(wrongAccount, b1.getLocalId(), b1ZipString));
		assertEquals(NetworkInterfaceConstants.REJECTED, uploadBulletinChunk(testServerInterface, wrongAccount, b1.getLocalId(), 10000, 0, 10000, b1ZipString, clientSecurity));
	}
	
	public void testUploadBulletinNotYourBulletin() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		String wrongAccount = serverSecurity.getPublicKeyString();
		testServer.allowUploads(wrongAccount);
		assertEquals(NetworkInterfaceConstants.NOTYOURBULLETIN, testServer.uploadBulletin(wrongAccount, b1.getLocalId(), b1ZipString));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, wrongAccount, b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, serverSecurity));
		assertEquals(NetworkInterfaceConstants.NOTYOURBULLETIN, uploadBulletinChunk(testServerInterface, wrongAccount, b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, serverSecurity));
	}
	
	public void testUploadBulletin() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString));

		Database db = testServer.getDatabase();
		assertNotNull("no database?", db);
		DatabaseKey key = DatabaseKey.createSealedKey(b1.getUniversalId());
		Bulletin got = Bulletin.loadFromDatabase(store, key);
		assertEquals("id", b1.getLocalId(), got.getLocalId());

		assertEquals(NetworkInterfaceConstants.DUPLICATE, testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString));
	}

	public void testUploadBulletinOneChunkOnly() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));

		Database db = testServer.getDatabase();
		assertNotNull("no database?", db);
		DatabaseKey key = DatabaseKey.createSealedKey(b1.getUniversalId());
		Bulletin got = Bulletin.loadFromDatabase(store, key);
		assertEquals("id", b1.getLocalId(), got.getLocalId());

		assertEquals(NetworkInterfaceConstants.DUPLICATE, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));
	}
	
	public void testUploadBulletinChunks() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
	}	

	public void testUploadTwoBulletinsByChunks() throws Exception
	{
		String b2ZipString = MockBulletin.saveToZipString(b2);

		byte[] b2ZipBytes = Base64.decode(b2ZipString);
		byte[] b2ChunkBytes0 = new byte[100];
		byte[] b2ChunkBytes1 = new byte[b2ZipBytes.length - b2ChunkBytes0.length];
		System.arraycopy(b2ZipBytes, 0, b2ChunkBytes0, 0, b2ChunkBytes0.length);
		System.arraycopy(b2ZipBytes, b2ChunkBytes0.length, b2ChunkBytes1, 0, b2ChunkBytes1.length);
		String b2ChunkData0 = Base64.encode(b2ChunkBytes0);
		String b2ChunkData1 = Base64.encode(b2ChunkBytes1);

		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b2.getLocalId(), b2ZipBytes.length, 0, b2ChunkBytes0.length, b2ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b2.getLocalId(), b2ZipBytes.length, b2ChunkBytes0.length, b2ChunkBytes1.length, b2ChunkData1, clientSecurity));
	}

	public void testUploadBulletinChunkAtZeroRestarts() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
	}

	public void testUploadBulletinChunkTooLarge() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), NetworkInterfaceConstants.MAX_CHUNK_SIZE*2, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), NetworkInterfaceConstants.MAX_CHUNK_SIZE*2, b1ChunkBytes0.length, NetworkInterfaceConstants.MAX_CHUNK_SIZE+1, b1ChunkData1, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, NetworkInterfaceConstants.MAX_CHUNK_SIZE+1, b1ChunkData1, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

	}

	public void testUploadBulletinTotalSizeWrong() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), 90, 0, 100, "", clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length-1, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
	}
	
	public void testUploadBulletinChunkInvalidOffset() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals("1 chunk invalid offset -1",NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, -1, b1ZipBytes.length, b1ZipString, clientSecurity));
		assertEquals("1 chunk invalid offset 1",NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 1, b1ZipBytes.length, b1ZipString, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length-1, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
			
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length+1, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
	}
	
	public void testUploadBulletinChunkDataLengthIncorrect() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length-1, b1ChunkData1, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length+1, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
	}
	
	public void testUploadChunkBadRequestSignature() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		String authorId = clientSecurity.getPublicKeyString();
		testServer.allowUploads(authorId);

		String localId = b1.getLocalId();
		int totalLength = b1ZipBytes.length;
		int chunkLength = b1ChunkBytes0.length;
		assertEquals("allowed bad sig?", NetworkInterfaceConstants.SIG_ERROR, testServer.uploadBulletinChunk(authorId, localId, totalLength, 0, chunkLength, b1ChunkData0, "123"));

		String stringToSign = authorId + "," + localId + "," + Integer.toString(totalLength) + "," + 
					Integer.toString(0) + "," + Integer.toString(chunkLength) + "," + b1ChunkData0;
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = serverSecurity.createSignature(new ByteArrayInputStream(bytesToSign));
		String signature = Base64.encode(sigBytes);
		assertEquals("allowed wrong sig?", NetworkInterfaceConstants.SIG_ERROR, testServer.uploadBulletinChunk(authorId, localId, totalLength, 0, chunkLength, b1ChunkData0, signature));
	}
	
	public void testUploadChunkIOError()
	{
		//TODO implement this
		//Should return SERVER_ERROR not INVALID_DATA;
	}

	class MockDraftDatabase extends MockServerDatabase
	{
		
		public void writeRecord(DatabaseKey key, InputStream record)
			throws IOException 
		{
			if(!key.isDraft())
				throw new IOException("Invalid Status");
			super.writeRecord(key, record);
		}
	}

	class MockSealedDatabase extends MockServerDatabase
	{
		
		public void writeRecord(DatabaseKey key, InputStream record)
			throws IOException 
		{
			if(!key.isSealed())
				throw new IOException("Invalid Status");
			super.writeRecord(key, record);
		}
	}

	public void testUploadDraft() throws Exception
	{
		Bulletin draft = store.createEmptyBulletin();
		draft.set(draft.TAGTITLE, "Title1");
		draft.set(draft.TAGPUBLICINFO, "Details1");
		draft.set(draft.TAGPRIVATEINFO, "PrivateDetails1");
		draft.setHQPublicKey(hqSecurity.getPublicKeyString());
		draft.save();
		draft = Bulletin.loadFromDatabase(store, new DatabaseKey(draft.getUniversalId()));
		String draftZipString = MockBulletin.saveToZipString(draft);
		byte[] draftZipBytes = Base64.decode(draftZipString);

		Database originalDatabase = testServer.getDatabase();
		testServer.setDatabase(new MockDraftDatabase());
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), draft.getLocalId(), draftZipBytes.length, 0, draftZipBytes.length, draftZipString, clientSecurity));
		testServer.setDatabase(originalDatabase);
	}


	public void testUploadDuplicates() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(b.TAGTITLE, "Title1");
		b.set(b.TAGPUBLICINFO, "Details1");
		b.set(b.TAGPRIVATEINFO, "PrivateDetails1");
		File attachment = createTempFile();
		FileOutputStream out = new FileOutputStream(attachment);
		out.write(b1AttachmentBytes);
		out.close();
		b.addPublicAttachment(new AttachmentProxy(attachment));
		b.setHQPublicKey(hqSecurity.getPublicKeyString());
		b.setDraft();
		b.save();
		b = Bulletin.loadFromDatabase(store, new DatabaseKey(b.getUniversalId()));
		UniversalId attachmentUid1 = b.getPublicAttachments()[0].getUniversalId();
		DatabaseKey draftHeader1 = new DatabaseKey(b.getUniversalId());
		draftHeader1.setDraft();
		DatabaseKey attachmentKey1 = new DatabaseKey(attachmentUid1);
		attachmentKey1.setDraft();
		String draft1ZipString = MockBulletin.saveToZipString(b);
		byte[] draft1ZipBytes = Base64.decode(draft1ZipString);

		b.clearPublicAttachments();
		FileOutputStream out2 = new FileOutputStream(attachment);
		out2.write(b1AttachmentBytes);
		out2.close();
		b.addPublicAttachment(new AttachmentProxy(attachment));
		b.save();
		b = Bulletin.loadFromDatabase(store, new DatabaseKey(b.getUniversalId()));
		UniversalId attachmentUid2 = b.getPublicAttachments()[0].getUniversalId();
		DatabaseKey attachmentKey2 = new DatabaseKey(attachmentUid2);
		DatabaseKey draftHeader2 = new DatabaseKey(b.getUniversalId());
		draftHeader2.setDraft();
		attachmentKey2.setDraft();
		String draft2ZipString = MockBulletin.saveToZipString(b);
		byte[] draft2ZipBytes = Base64.decode(draft2ZipString);

		b.clearPublicAttachments();
		FileOutputStream out3 = new FileOutputStream(attachment);
		out3.write(b1AttachmentBytes);
		out3.close();
		b.addPublicAttachment(new AttachmentProxy(attachment));
		b.setSealed();
		b.save();
		b = Bulletin.loadFromDatabase(store, new DatabaseKey(b.getUniversalId()));
		UniversalId attachmentUid3 = b.getPublicAttachments()[0].getUniversalId();
		DatabaseKey attachmentKey3 = new DatabaseKey(attachmentUid3);
		DatabaseKey sealedHeader3 = new DatabaseKey(b.getUniversalId());
		sealedHeader3.setSealed();
		attachmentKey3.setSealed();
		String sealedZipString = MockBulletin.saveToZipString(b);
		byte[] sealedZipBytes = Base64.decode(sealedZipString);

		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		Database db = testServer.getDatabase();

		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), draft1ZipBytes.length, 0, 
			draft1ZipBytes.length, draft1ZipString, clientSecurity));

		assertEquals("Attachment 1 does not exists?", true, db.doesRecordExist(attachmentKey1));
		assertEquals("Attachment 2 exists?", false, db.doesRecordExist(attachmentKey2));
		assertEquals("Attachment 3 exists?", false, db.doesRecordExist(attachmentKey3));
		assertEquals("Header 1 does not exists?", true, db.doesRecordExist(draftHeader1));
		assertEquals("Header 2 does not exists?", true, db.doesRecordExist(draftHeader2));
		assertEquals("Header 3 exists?", false, db.doesRecordExist(sealedHeader3));

		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), draft2ZipBytes.length, 0, 
			draft2ZipBytes.length, draft2ZipString, clientSecurity));

		assertEquals("Attachment 1 still exists?", false, db.doesRecordExist(attachmentKey1));
		assertEquals("Attachment 2 does not exists?", true, db.doesRecordExist(attachmentKey2));
		assertEquals("Attachment 3 exists?", false, db.doesRecordExist(attachmentKey3));
		assertEquals("Header 1 does not exists?", true, db.doesRecordExist(draftHeader1));
		assertEquals("Header 2 does not exists?", true, db.doesRecordExist(draftHeader2));
		assertEquals("Header 3 exists?", false, db.doesRecordExist(sealedHeader3));

		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), sealedZipBytes.length, 0, 
			sealedZipBytes.length, sealedZipString, clientSecurity));

		assertEquals("Attachment 1 still exists?", false, db.doesRecordExist(attachmentKey1));
		assertEquals("Attachment 2 still exists?", false, db.doesRecordExist(attachmentKey2));
		assertEquals("Attachment 3 does not exist?", true, db.doesRecordExist(attachmentKey3));
		assertEquals("Header 1 exists?", false, db.doesRecordExist(draftHeader1));
		assertEquals("Header 2 exists?", false, db.doesRecordExist(draftHeader2));
		assertEquals("Header 3 does not exists?", true, db.doesRecordExist(sealedHeader3));

		assertEquals(NetworkInterfaceConstants.SEALED_EXISTS, uploadBulletinChunk(testServerInterface, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), draft1ZipBytes.length, 0, 
			draft1ZipBytes.length, draft1ZipString, clientSecurity));

		assertEquals(NetworkInterfaceConstants.DUPLICATE, uploadBulletinChunk(testServerInterface, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), sealedZipBytes.length, 0, 
			sealedZipBytes.length, sealedZipString, clientSecurity));
	}

	public void testUploadSealedStatus() throws Exception
	{

		Database originalDatabase = testServer.getDatabase();
		testServer.setDatabase(new MockSealedDatabase());
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));
		testServer.setDatabase(originalDatabase);
	}

	public void testBadlySignedBulletinUpload() throws Exception
	{
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		MockMartusSecurity mockServerSecurity = new MockMartusSecurity();
		mockServerSecurity.fakeSigVerifyFailure = true;
		testServer.security = mockServerSecurity;

		assertEquals("didn't verify sig?", NetworkInterfaceConstants.SIG_ERROR, testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString));

		assertEquals("didn't verify sig for 1 chunk?", NetworkInterfaceConstants.SIG_ERROR, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals("didn't verify sig for chunks?", NetworkInterfaceConstants.SIG_ERROR, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		testServer.security = serverSecurity;
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
	}
	
	public void testInvalidDataUpload() throws Exception
	{
		String authorClientId = clientSecurity.getPublicKeyString();
		testServer.allowUploads(authorClientId);
		String bulletinLocalId = b1.getLocalId();

		assertEquals("not base64", NetworkInterfaceConstants.INVALID_DATA, testServer.uploadBulletin(authorClientId, bulletinLocalId, "not a valid bulletin!"));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals("not base64 chunk", NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, 100, "not a valid bulletin!", clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		assertEquals("empty fullupload", NetworkInterfaceConstants.INVALID_DATA, testServer.uploadBulletin(authorClientId, bulletinLocalId, ""));
		assertEquals("empty chunk", NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServerInterface, authorClientId, bulletinLocalId, 1, 0, 0, "", clientSecurity));
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		ZipOutputStream zipWithBadEntry = new ZipOutputStream(out2);
		ZipEntry badEntry = new ZipEntry("blah");
		zipWithBadEntry.putNextEntry(badEntry);
		zipWithBadEntry.write(5);
		zipWithBadEntry.close();
		String zipWithBadEntryString = Base64.encode(out2.toByteArray());
		assertEquals("zip bad entry", NetworkInterfaceConstants.INVALID_DATA, testServer.uploadBulletin(authorClientId, "yah", zipWithBadEntryString));
	}

	public void testDownloadBulletinFail() throws Exception
	{
		Vector result = testServer.downloadBulletin("clientid","bulletinid");
		assertNotNull("result null", result);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.NOT_FOUND, result.get(0));
	}

	public void testDownloadBulletinOk() throws Exception
	{
		testServer.setSecurity(serverSecurity);
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		Bulletin bulletin = store.createEmptyBulletin();
		bulletin.set(bulletin.TAGPUBLICINFO, "public info");
		bulletin.set(bulletin.TAGPRIVATEINFO, "private info");
		bulletin.setSealed();
		String data = MockBulletin.saveToZipString(bulletin);
		assertEquals(NetworkInterfaceConstants.OK, testServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletin.getLocalId(), data));
		Vector result = testServer.downloadBulletin(clientSecurity.getPublicKeyString(), bulletin.getLocalId());
		assertNotNull("result null", result);
		assertEquals(2, result.size());
		assertEquals(NetworkInterfaceConstants.OK, result.get(0));
		String gotString = (String)result.get(1);
		
		Bulletin got = store.createEmptyBulletin();
		MockBulletin.loadFromZipString(got, gotString);
		assertEquals("id", bulletin.getLocalId(), got.getLocalId());
		assertEquals("public", bulletin.get(bulletin.TAGPUBLICINFO), got.get(got.TAGPUBLICINFO));
		assertEquals("private ", bulletin.get(bulletin.TAGPRIVATEINFO), got.get(got.TAGPRIVATEINFO));
	}
	
	public void testDownloadBulletinChunkBadId() throws Exception
	{
		Vector result = downloadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), "bulletinid", 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		assertNotNull("result null", result);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.NOT_FOUND, result.get(0));
	}
	
	public void testDownloadBulletinChunkBadRequestSignature() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));

		Vector result1 = testServer.downloadMyBulletinChunk(b1.getAccount(), b1.getLocalId(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE, "123");
		assertNotNull("result null", result1);
		assertEquals(1, result1.size());
		assertEquals(NetworkInterfaceConstants.SIG_ERROR, result1.get(0));

		String stringToSign = b1.getAccount() + "," + b1.getLocalId() + "," + 
					Integer.toString(0) + "," + Integer.toString(NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = serverSecurity.createSignature(new ByteArrayInputStream(bytesToSign));
		String signature = Base64.encode(sigBytes);
		Vector result2 = testServer.downloadMyBulletinChunk(b1.getAccount(), b1.getLocalId(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE, signature);
		assertNotNull("result2 null", result2);
		assertEquals(1, result2.size());
		assertEquals(NetworkInterfaceConstants.SIG_ERROR, result2.get(0));
	}

	public void testDownloadFieldOfficeBulletinChunkBadId() throws Exception
	{
		Vector result = downloadFieldOfficeBulletinChunk(testServerInterface, hqSecurity, clientSecurity.getPublicKeyString(), "bulletinid", 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		assertNotNull("result null", result);
		assertEquals(1, result.size());
		assertEquals(NetworkInterfaceConstants.NOT_FOUND, result.get(0));
	}
	
	public void testDownloadFieldOfficeBulletinChunkBadRequestSignature() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));
		
		Vector result1 = testServer.downloadFieldOfficeBulletinChunk(b1.getAccount(), b1.getLocalId(), hqSecurity.getPublicKeyString(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE, "123");
		assertNotNull("result null", result1);
		assertEquals(1, result1.size());
		assertEquals(NetworkInterfaceConstants.SIG_ERROR, result1.get(0));

		String stringToSign = b1.getAccount() + "," + b1.getLocalId() + "," + hqSecurity.getPublicKeyString() + "," +
					Integer.toString(0) + "," + Integer.toString(NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		String signature = MartusUtilities.createSignature(stringToSign, serverSecurity);

		Vector result2 = testServer.downloadFieldOfficeBulletinChunk(b1.getAccount(), b1.getLocalId(), hqSecurity.getPublicKeyString(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE, signature);
		assertNotNull("result2 null", result2);
		assertEquals(1, result2.size());
		assertEquals(NetworkInterfaceConstants.SIG_ERROR, result2.get(0));
	}
	
	
	public void testDownloadFieldOfficeBulletinChunkNotAuthorized() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));

		String notMineStringToSign = b1.getAccount() + "," + b1.getLocalId() + "," + clientSecurity.getPublicKeyString() + "," +
					Integer.toString(0) + "," + Integer.toString(NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		String notMineSignature = MartusUtilities.createSignature(notMineStringToSign, clientSecurity);

		Vector notMineResult = testServer.downloadFieldOfficeBulletinChunk(b1.getAccount(), b1.getLocalId(), clientSecurity.getPublicKeyString(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE, notMineSignature);
		assertNotNull("result2 null", notMineResult);
		assertEquals(1, notMineResult.size());
		assertEquals(NetworkInterfaceConstants.NOTYOURBULLETIN, notMineResult.get(0));
	}

	public void testExtractPacketsToZipStream() throws Exception
	{
		uploadSampleBulletin();
		DatabaseKey[] packetKeys = MartusUtilities.getAllPacketKeys(b1.getBulletinHeaderPacket());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MartusUtilities.extractPacketsToZipStream(b1.getAccount(), testServer.getDatabase(), packetKeys, out, serverSecurity);
		assertEquals("wrong length?", b1ZipBytes.length, out.toByteArray().length);
		
		String zipString = Base64.encode(out.toByteArray());
		assertEquals("zips different?", getZipEntryNamesAndCrcs(b1ZipString), getZipEntryNamesAndCrcs(zipString));
	}

	String getZipEntryNamesAndCrcs(String zipString) throws 
		IOException, 
		InvalidBase64Exception, 
		ZipException 
	{
		String result = "";
		File tempFile = Base64.decodeToTempFile(zipString);
		ZipFile zip = new ZipFile(tempFile);
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			result += entry.getName();
			result += ":";
			result += new Long(entry.getCrc()).toString();
			result += ",";
		}
		return result;
	}
		
	public void testDownloadBulletinChunkDraftWholeBulletin() throws Exception
	{
		String draftZipString = uploadSampleDraftBulletin(draft);
		byte[] draftZipBytes = Base64.decode(draftZipString);
		
		Vector result = downloadBulletinChunk(testServerInterface, draft.getAccount(), draft.getLocalId(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		assertEquals("failed?", NetworkInterfaceConstants.OK, result.get(0));
		assertEquals("wrong total size?", new Integer(draftZipBytes.length), result.get(1));
		assertEquals("wrong chunk size?", new Integer(draftZipBytes.length), result.get(2));
		String zipString = (String)result.get(3);
		assertEquals("bad zip?", getZipEntryNamesAndCrcs(draftZipString), getZipEntryNamesAndCrcs(zipString));
		Bulletin loaded = store.createEmptyBulletin();
		MockBulletin.loadFromZipString(loaded, zipString);
	}

	public void testDownloadBulletinChunkWholeBulletin() throws Exception
	{
		uploadSampleBulletin();
		Vector result = downloadBulletinChunk(testServerInterface, b1.getAccount(), b1.getLocalId(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		assertEquals("failed?", NetworkInterfaceConstants.OK, result.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result.get(1));
		assertEquals("wrong chunk size?", new Integer(b1ZipBytes.length), result.get(2));
		String zipString = (String)result.get(3);
		assertEquals("bad zip?", getZipEntryNamesAndCrcs(b1ZipString), getZipEntryNamesAndCrcs(zipString));
		Bulletin loaded = store.createEmptyBulletin();
		MockBulletin.loadFromZipString(loaded, zipString);
	}

	public void testDownloadBulletinChunkWholeBulletinLegacySignature() throws Exception
	{
		uploadSampleBulletin();
		String legacyStringToSign = b1.getAccount() + "," + b1.getLocalId() + "," + "0" + "," +
					Integer.toString(0) + "," + Integer.toString(NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		String legacySignature = MartusUtilities.createSignature(legacyStringToSign, clientSecurity);

		Vector result = testServer.downloadMyBulletinChunk(b1.getAccount(), b1.getLocalId(), 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE, legacySignature);
		assertEquals("failed?", NetworkInterfaceConstants.OK, result.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result.get(1));
		assertEquals("wrong chunk size?", new Integer(b1ZipBytes.length), result.get(2));
	}

	public void testDownloadBulletinChunk2LegacySignature() throws Exception
	{
		uploadSampleBulletin();
		int chunkSize = b1ZipBytes.length / 5 * 2;
		String legacyStringToSign = b1.getAccount() + "," + b1.getLocalId() + "," + 
					Integer.toString(b1ZipBytes.length) + "," + 
					Integer.toString(chunkSize) + "," +
					Integer.toString(chunkSize);
		String legacySignature = MartusUtilities.createSignature(legacyStringToSign, clientSecurity);

		Vector result = testServer.downloadMyBulletinChunk(b1.getAccount(), b1.getLocalId(), chunkSize, chunkSize, legacySignature);
		assertEquals("failed?", NetworkInterfaceConstants.CHUNK_OK, result.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result.get(1));
		assertEquals("wrong chunk size?", new Integer(chunkSize), result.get(2));
	}

	public void testDownloadBulletinThreeChunks() throws Exception
	{
		uploadSampleBulletin();
		int chunkSize = b1ZipBytes.length / 5 * 2;
		Vector result1 = downloadBulletinChunk(testServerInterface, b1.getAccount(), b1.getLocalId(), 0, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.CHUNK_OK, result1.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result1.get(1));
		assertEquals("wrong chunk size?", new Integer(chunkSize), result1.get(2));
		
		Vector result2 = downloadBulletinChunk(testServerInterface, b1.getAccount(), b1.getLocalId(), chunkSize, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.CHUNK_OK, result2.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result2.get(1));
		assertEquals("wrong chunk size?", new Integer(chunkSize), result2.get(2));

		Vector result3 = downloadBulletinChunk(testServerInterface, b1.getAccount(), b1.getLocalId(), 2*chunkSize, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.OK, result3.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result3.get(1));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Base64.decode(new StringReader((String)result1.get(3)), out);
		Base64.decode(new StringReader((String)result2.get(3)), out);
		Base64.decode(new StringReader((String)result3.get(3)), out);
		assertEquals("wrong amount of data?", b1ZipBytes.length, out.toByteArray().length);
		String zipString = Base64.encode(out.toByteArray());
		
		assertEquals("bad zip?", getZipEntryNamesAndCrcs(b1ZipString), getZipEntryNamesAndCrcs(zipString));
		Bulletin loaded = store.createEmptyBulletin();
		MockBulletin.loadFromZipString(loaded, zipString);
	}
	
	public void testDownloadFieldOfficeBulletinThreeChunks() throws Exception
	{
		uploadSampleBulletin();

		int chunkSize = b1ZipBytes.length / 5 * 2;
		Vector result1 = downloadFieldOfficeBulletinChunk(testServerInterface, hqSecurity, b1.getAccount(), b1.getLocalId(), 0, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.CHUNK_OK, result1.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result1.get(1));
		assertEquals("wrong chunk size?", new Integer(chunkSize), result1.get(2));
		
		Vector result2 = downloadFieldOfficeBulletinChunk(testServerInterface, hqSecurity, b1.getAccount(), b1.getLocalId(), chunkSize, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.CHUNK_OK, result2.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result2.get(1));
		assertEquals("wrong chunk size?", new Integer(chunkSize), result2.get(2));

		Vector result3 = downloadFieldOfficeBulletinChunk(testServerInterface, hqSecurity, b1.getAccount(), b1.getLocalId(), 2*chunkSize, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.OK, result3.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result3.get(1));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Base64.decode(new StringReader((String)result1.get(3)), out);
		Base64.decode(new StringReader((String)result2.get(3)), out);
		Base64.decode(new StringReader((String)result3.get(3)), out);
		assertEquals("wrong amount of data?", b1ZipBytes.length, out.toByteArray().length);
		String zipString = Base64.encode(out.toByteArray());
		
		assertEquals("bad zip?", getZipEntryNamesAndCrcs(b1ZipString), getZipEntryNamesAndCrcs(zipString));
		Bulletin loaded = store.createEmptyBulletin();
		MockBulletin.loadFromZipString(loaded, zipString);
	}
	
	// TODO: This is only needed to support the Guatemala HQ's. It should be removed
	// after all those have been updated to newer software!
	public void testLegacyDownloadFieldOfficeBulletinAsMyBulletin() throws Exception
	{
		uploadSampleBulletin();

		int chunkSize = b1ZipBytes.length / 5 * 2;
		Vector result1 = legacyDownloadFieldOfficeBulletinChunkAsMyBulletin(testServerInterface, hqSecurity, b1.getAccount(), b1.getLocalId(), 0, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.CHUNK_OK, result1.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result1.get(1));
		assertEquals("wrong chunk size?", new Integer(chunkSize), result1.get(2));
		
		Vector result2 = legacyDownloadFieldOfficeBulletinChunkAsMyBulletin(testServerInterface, hqSecurity, b1.getAccount(), b1.getLocalId(), chunkSize, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.CHUNK_OK, result2.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result2.get(1));
		assertEquals("wrong chunk size?", new Integer(chunkSize), result2.get(2));

		Vector result3 = legacyDownloadFieldOfficeBulletinChunkAsMyBulletin(testServerInterface, hqSecurity, b1.getAccount(), b1.getLocalId(), 2*chunkSize, chunkSize);
		assertEquals("failed?", NetworkInterfaceConstants.OK, result3.get(0));
		assertEquals("wrong total size?", new Integer(b1ZipBytes.length), result3.get(1));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Base64.decode(new StringReader((String)result1.get(3)), out);
		Base64.decode(new StringReader((String)result2.get(3)), out);
		Base64.decode(new StringReader((String)result3.get(3)), out);
		assertEquals("wrong amount of data?", b1ZipBytes.length, out.toByteArray().length);
		String zipString = Base64.encode(out.toByteArray());
		
		assertEquals("bad zip?", getZipEntryNamesAndCrcs(b1ZipString), getZipEntryNamesAndCrcs(zipString));
		Bulletin loaded = store.createEmptyBulletin();
		MockBulletin.loadFromZipString(loaded, zipString);
	}
	
	public void testListFieldOfficeSealedBulletinIds() throws Exception
	{
		testServer.security = serverSecurity;

		MartusSecurity fieldSecurity1 = clientSecurity;
		testServer.allowUploads(fieldSecurity1.getPublicKeyString());

		MartusSecurity nonFieldSecurity = new MockMartusSecurity();
		nonFieldSecurity.createKeyPair();
		testServer.allowUploads(nonFieldSecurity.getPublicKeyString());

		BulletinStore nonFieldStore = new BulletinStore(new MockServerDatabase());
		nonFieldStore.setSignatureGenerator(nonFieldSecurity);

		Vector list1 = testServer.listFieldOfficeSealedBulletinIds(hqSecurity.getPublicKeyString(), fieldSecurity1.getPublicKeyString());
		assertNotNull("testListFieldOfficeBulletinSummaries returned null", list1);
		assertEquals("wrong length list 1", 1, list1.size());
		assertNotNull("null id1 [0] list 1", list1.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list1.get(0));

		Bulletin bulletinSealed = store.createEmptyBulletin();
		bulletinSealed.setHQPublicKey(hqSecurity.getPublicKeyString());
		bulletinSealed.setSealed();
		bulletinSealed.save();
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinSealed.getLocalId(), MockBulletin.saveToZipString(bulletinSealed));

		Bulletin bulletinDraft = store.createEmptyBulletin();
		bulletinDraft.setHQPublicKey(hqSecurity.getPublicKeyString());
		bulletinDraft.setDraft();
		bulletinDraft.save();
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinDraft.getLocalId(), MockBulletin.saveToZipString(bulletinDraft));

		privateBulletin.setHQPublicKey(hqSecurity.getPublicKeyString());
		
		privateBulletin.save();
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), privateBulletin.getLocalId(), MockBulletin.saveToZipString(privateBulletin));
				
		Vector list2 = testServer.listFieldOfficeSealedBulletinIds(hqSecurity.getPublicKeyString(), fieldSecurity1.getPublicKeyString());
		assertEquals("wrong length list2", 3, list2.size());
		assertNotNull("null id1 [0] list2", list2.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list2.get(0));
		String b1Summary = bulletinSealed.getLocalId() + "=" + bulletinSealed.getFieldDataPacket().getLocalId();
		String privateBulletinSummary = privateBulletin.getLocalId() + "=" + privateBulletin.getFieldDataPacket().getLocalId();
		assertContains("missing b1?",b1Summary , list2);
		assertContains("missing privateBulletin?",privateBulletinSummary, list2);
	}

	public void testListFieldOfficeDraftBulletinIds() throws Exception
	{
		testServer.security = serverSecurity;

		MartusSecurity fieldSecurity1 = clientSecurity;
		testServer.allowUploads(fieldSecurity1.getPublicKeyString());

		MartusSecurity nonFieldSecurity = new MockMartusSecurity();
		nonFieldSecurity.createKeyPair();
		testServer.allowUploads(nonFieldSecurity.getPublicKeyString());

		BulletinStore nonFieldStore = new BulletinStore(new MockServerDatabase());
		nonFieldStore.setSignatureGenerator(nonFieldSecurity);

		Vector list1 = testServer.listFieldOfficeSealedBulletinIds(hqSecurity.getPublicKeyString(), fieldSecurity1.getPublicKeyString());
		assertNotNull("testListFieldOfficeBulletinSummaries returned null", list1);
		assertEquals("wrong length list 1", 1, list1.size());
		assertNotNull("null id1 [0] list1", list1.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list1.get(0));

		Bulletin bulletinSealed = store.createEmptyBulletin();
		bulletinSealed.setHQPublicKey(hqSecurity.getPublicKeyString());
		bulletinSealed.setSealed();
		bulletinSealed.setAllPrivate(true);
		bulletinSealed.save();
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinSealed.getLocalId(), MockBulletin.saveToZipString(bulletinSealed));

		Bulletin bulletinDraft = store.createEmptyBulletin();
		bulletinDraft.setHQPublicKey(hqSecurity.getPublicKeyString());
		bulletinDraft.setDraft();
		bulletinDraft.save();
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinDraft.getLocalId(), MockBulletin.saveToZipString(bulletinDraft));

		Vector list2 = testServer.listFieldOfficeDraftBulletinIds(hqSecurity.getPublicKeyString(), fieldSecurity1.getPublicKeyString());
		assertEquals("wrong length list2", 2, list2.size());
		assertNotNull("null id1 [0] list2", list2.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list2.get(0));
		String b1Summary = bulletinDraft.getLocalId() + "=" + bulletinDraft.getFieldDataPacket().getLocalId();
		assertContains("missing bulletin Draft?",b1Summary , list2);
	}


	public void testListFieldOfficeAccountsErrorCondition() throws Exception
	{
		class MyMock extends MartusSecurity
		{
			public MyMock() throws Exception
			{
			}

			public boolean signatureIsValid(byte[] sig) throws MartusSignatureException
			{
				if(!shouldFailNext)
					return super.signatureIsValid(sig);
				shouldFailNext = false;
				return false;						
			}

			public boolean isSignatureValid(PublicKey publicKey, InputStream inputStream, byte[] signature) throws
					MartusSignatureException
			{
				if(!shouldFailNext)
					return super.isSignatureValid(publicKey, inputStream, signature);
				shouldFailNext = false;
				return false;						
			}			
			boolean shouldFailNext;
		}
		MyMock myMock = new MyMock();
		testServer.security = serverSecurity;
		
		MartusSecurity fieldSecurity1 = clientSecurity;
		testServer.allowUploads(fieldSecurity1.getPublicKeyString());
		Bulletin bulletin = store.createEmptyBulletin();
		bulletin.setHQPublicKey(hqSecurity.getPublicKeyString());
		bulletin.setSealed();
		bulletin.save();
		String result2 = testServer.uploadBulletin(bulletin.getAccount(), bulletin.getLocalId(), MockBulletin.saveToZipString(bulletin));

		privateBulletin.setHQPublicKey(hqSecurity.getPublicKeyString());
		privateBulletin.save();
		testServer.uploadBulletin(privateBulletin.getAccount(), privateBulletin.getLocalId(), MockBulletin.saveToZipString(privateBulletin));

		testServer.security = myMock;
		myMock.shouldFailNext = true;
		Vector list2 = testServer.listFieldOfficeAccounts(hqSecurity.getPublicKeyString());
		assertEquals("wrong length", 2, list2.size());
		assertNotNull("null id1 [0]", list2.get(0));
		assertEquals(NetworkInterfaceConstants.SERVER_ERROR, list2.get(0));
	}

	public void testListFieldOfficeAccounts() throws Exception
	{
		testServer.security = serverSecurity;

		MartusSecurity nonFieldSecurity = new MockMartusSecurity();
		nonFieldSecurity.createKeyPair();
		testServer.allowUploads(nonFieldSecurity.getPublicKeyString());

		BulletinStore nonFieldStore = new BulletinStore(new MockServerDatabase());
		nonFieldStore.setSignatureGenerator(nonFieldSecurity);
		Bulletin b = nonFieldStore.createEmptyBulletin();
		b.set(b.TAGTITLE, "Tifdfssftle3");
		b.set(b.TAGPUBLICINFO, "Detasdfsdfils1");
		b.set(b.TAGPRIVATEINFO, "PrivasdfsdfteDetails1");
		b.save();
		testServer.uploadBulletin(nonFieldSecurity.getPublicKeyString(), b.getLocalId(), MockBulletin.saveToZipString(b));

		Vector list1 = testServer.listFieldOfficeAccounts(hqSecurity.getPublicKeyString());
		assertNotNull("listFieldOfficeAccounts returned null", list1);
		assertEquals("wrong length", 1, list1.size());
		assertNotNull("null id1 [0]", list1.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list1.get(0));

		MartusSecurity fieldSecurity1 = clientSecurity;
		testServer.allowUploads(fieldSecurity1.getPublicKeyString());
		Bulletin bulletin = store.createEmptyBulletin();
		bulletin.setHQPublicKey(hqSecurity.getPublicKeyString());
		bulletin.save();
		testServer.uploadBulletin(fieldSecurity1.getPublicKeyString(), bulletin.getLocalId(), MockBulletin.saveToZipString(bulletin));

		privateBulletin.setHQPublicKey(hqSecurity.getPublicKeyString());
		privateBulletin.save();
		testServer.uploadBulletin(fieldSecurity1.getPublicKeyString(), privateBulletin.getLocalId(), MockBulletin.saveToZipString(privateBulletin));
				
		Vector list2 = testServer.listFieldOfficeAccounts(hqSecurity.getPublicKeyString());
		assertEquals("wrong length", 2, list2.size());
		assertEquals(NetworkInterfaceConstants.OK, list2.get(0));
		assertEquals("Wrong Key?", fieldSecurity1.getPublicKeyString(), list2.get(1));
		
	}
	public void testListMyBulletinSummaries() throws Exception
	{
		testServer.security = serverSecurity;
		testServer.allowUploads(clientSecurity.getPublicKeyString());

		Vector list1 = testServer.listMySealedBulletinIds(clientSecurity.getPublicKeyString());
		assertNotNull("listMyBulletinSummaries returned null", list1);
		assertEquals("wrong length", 1, list1.size());
		assertNotNull("null id1 [0]", list1.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list1.get(0));

		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString);
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), privateBulletin.getLocalId(), MockBulletin.saveToZipString(privateBulletin));

		Vector list2 = testServer.listMySealedBulletinIds(clientSecurity.getPublicKeyString());
		assertNotNull("listMyBulletinSummaries returned null", list2);
		assertEquals("wrong length", 3, list2.size());
		assertNotNull("null id1 [0]", list2.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list2.get(0));

		String gotSummary1 = (String)list2.get(1);
		assertNotNull("1 was null", gotSummary1);

		String gotSummary2 = (String)list2.get(2);
		assertNotNull("2 was null", gotSummary2);
		
		int at1 = gotSummary1.indexOf("=");
		assertTrue("no = in at1?", at1 >= 0);
		String id1 = gotSummary1.substring(0, at1);
		String summary1 = gotSummary1.substring(at1+1);

		int at2 = gotSummary2.indexOf("=");
		assertTrue("no = in at2?", at2 >= 0);
		String id2 = gotSummary2.substring(0, at2);
		String summary2 = gotSummary2.substring(at2+1);

		if(!id1.equals(b1.getLocalId()))
		{
			String tempId = id1;
			id1 = id2;
			id2 = tempId;
			
			String tempSummary = summary1;
			summary1 = summary2;
			summary2 = tempSummary;
		}

		assertEquals("id1", b1.getLocalId(), id1);
		assertEquals("summary1", b1.getFieldDataPacket().getLocalId(), summary1);
		assertEquals("id2", privateBulletin.getLocalId(), id2);
		assertEquals("summary2", privateBulletin.getFieldDataPacket().getLocalId(), summary2);
	}
	
	public void testDownloadFieldDataPacketWrongSig() throws Exception
	{
		uploadSampleBulletin();
		String authorId = b1.getAccount();
		String headerPacketLocalId = b1.getLocalId();
		String fieldDataPacketLocalId = b1.getFieldDataPacket().getLocalId();
		
		String hqAccountId = hqSecurity.getPublicKeyString();
		String stringToSign = authorId + "," + headerPacketLocalId + "," + fieldDataPacketLocalId + "," + hqAccountId;
		String signature1 = MartusUtilities.createSignature(stringToSign, serverSecurity);
		Vector result1 = testServer.downloadFieldDataPacket(authorId, headerPacketLocalId, fieldDataPacketLocalId, hqAccountId, signature1);
		assertEquals("Allowed download signed by wrong account?", NetworkInterfaceConstants.SIG_ERROR, result1.get(0));

		String signature2 = MartusUtilities.createSignature(stringToSign+"x", hqSecurity);
		Vector result2 = testServer.downloadFieldDataPacket(authorId, headerPacketLocalId, fieldDataPacketLocalId, hqAccountId, signature1);
		assertEquals("Allowed download signed wrong data?", NetworkInterfaceConstants.SIG_ERROR, result2.get(0));
	}

	public void testDownloadFieldDataPacketNotFieldDataPacket() throws Exception
	{
		uploadSampleBulletin();
		String authorId = b1.getAccount();
		String headerPacketLocalId = b1.getLocalId();
		
		String hqAccountId = hqSecurity.getPublicKeyString();
		String stringToSign = authorId + "," + headerPacketLocalId + "," + headerPacketLocalId + "," + hqAccountId;
		String signature = MartusUtilities.createSignature(stringToSign, hqSecurity);
		Vector result = testServer.downloadFieldDataPacket(authorId, headerPacketLocalId, headerPacketLocalId, hqAccountId, signature);
		assertEquals("Allowed download non-field packet?", NetworkInterfaceConstants.INVALID_DATA, result.get(0));
	}

	public void testDownloadFieldDataPacketNotFound() throws Exception
	{
		uploadSampleBulletin();
		String authorId = b1.getAccount();
		String headerPacketLocalId = b1.getLocalId();
		String fieldDataPacketLocalId = b1.getFieldDataPacket().getLocalId();
		
		String hqAccountId = hqSecurity.getPublicKeyString();
		String wrongHeaderLocalId = headerPacketLocalId + "x";
		String wrongHeaderStringToSign = authorId + "," + wrongHeaderLocalId + "," + fieldDataPacketLocalId + "," + hqAccountId;
		String wrongHeaderSignature = MartusUtilities.createSignature(wrongHeaderStringToSign, hqSecurity);
		Vector result1 = testServer.downloadFieldDataPacket(authorId, wrongHeaderLocalId, fieldDataPacketLocalId, hqAccountId, wrongHeaderSignature);
		assertEquals("Allowed download when header not found?", NetworkInterfaceConstants.NOT_FOUND, result1.get(0));

		String wrongDataLocalId = fieldDataPacketLocalId + "x";
		String wrongDataStringToSign = authorId + "," + headerPacketLocalId + "," + wrongDataLocalId + "," + hqAccountId;
		String wrongDataSignature = MartusUtilities.createSignature(wrongDataStringToSign, hqSecurity);
		Vector result2 = testServer.downloadFieldDataPacket(authorId, headerPacketLocalId, wrongDataLocalId, hqAccountId, wrongDataSignature);
		assertEquals("Allowed download when data not found?", NetworkInterfaceConstants.NOT_FOUND, result2.get(0));
	}

	public void testDownloadFieldDataPacketNotAuthorized() throws Exception
	{
		uploadSampleBulletin();
		String authorId = b1.getAccount();
		String headerPacketLocalId = b1.getLocalId();
		String fieldDataPacketLocalId = b1.getFieldDataPacket().getLocalId();
		
		String myAccountId = serverSecurity.getPublicKeyString();
		String stringToSign = authorId + "," + headerPacketLocalId + "," + fieldDataPacketLocalId + "," + myAccountId;
		String signature = MartusUtilities.createSignature(stringToSign, serverSecurity);
		Vector result = testServer.downloadFieldDataPacket(authorId, headerPacketLocalId, fieldDataPacketLocalId, myAccountId, signature);
		assertEquals("Allowed non-author, non-hq to download packet?", NetworkInterfaceConstants.NOTYOURBULLETIN, result.get(0));
	}

	public void testDownloadFieldDataPacketByAuthor() throws Exception
	{
		uploadSampleBulletin();
		String authorId = b1.getAccount();
		String headerPacketLocalId = b1.getLocalId();
		FieldDataPacket originalFdp = b1.getFieldDataPacket();
		String fieldDataPacketLocalId = originalFdp.getLocalId();
		
		String myAccountId = clientSecurity.getPublicKeyString();
		String stringToSign = authorId + "," + headerPacketLocalId + "," + fieldDataPacketLocalId + "," + myAccountId;
		String signature = MartusUtilities.createSignature(stringToSign, clientSecurity);
		Vector result = testServer.downloadFieldDataPacket(authorId, headerPacketLocalId, fieldDataPacketLocalId, myAccountId, signature);
		assertNotNull(result);
		assertEquals("not OK?", NetworkInterfaceConstants.OK, result.get(0));

		InputStreamWithSeek in = new StringInputStream((String)result.get(1));
		FieldDataPacket gotPacket = new FieldDataPacket(originalFdp.getUniversalId(), originalFdp.getFieldTags());
		gotPacket.loadFromXml(in, clientSecurity);
		assertEquals("wrong data?", b1.get(b1.TAGPUBLICINFO), gotPacket.get(b1.TAGPUBLICINFO));

	}

	public void testDownloadFieldDataPacketByHq() throws Exception
	{
		uploadSampleBulletin();
		String authorId = b1.getAccount();
		String headerPacketLocalId = b1.getLocalId();
		FieldDataPacket originalFdp = b1.getFieldDataPacket();
		String fieldDataPacketLocalId = originalFdp.getLocalId();
		
		String myAccountId = hqSecurity.getPublicKeyString();
		String stringToSign = authorId + "," + headerPacketLocalId + "," + fieldDataPacketLocalId + "," + myAccountId;
		String signature = MartusUtilities.createSignature(stringToSign, hqSecurity);
		Vector result = testServer.downloadFieldDataPacket(authorId, headerPacketLocalId, fieldDataPacketLocalId, myAccountId, signature);
		assertNotNull(result);
		assertEquals("not OK?", NetworkInterfaceConstants.OK, result.get(0));
		
		InputStreamWithSeek in = new StringInputStream((String)result.get(1));
		FieldDataPacket gotPacket = new FieldDataPacket(originalFdp.getUniversalId(), originalFdp.getFieldTags());
		gotPacket.loadFromXml(in, clientSecurity);
		assertEquals("wrong data?", b1.get(b1.TAGPUBLICINFO), gotPacket.get(b1.TAGPUBLICINFO));
	}

	public void testDownloadAuthorizedPacket() throws Exception
	{
		uploadSampleBulletin();

		String clientId = clientSecurity.getPublicKeyString();
		String badLocalId = b1.getFieldDataPacket().getLocalId() + "x";
		
		Vector badSigResult = testServer.legacyDownloadAuthorizedPacket(clientId, badLocalId, clientId, "This is not a valid Signature");
		assertNotNull(badSigResult);
		assertEquals("Not Bad sig?", NetworkInterfaceConstants.SIG_ERROR, badSigResult.get(0));

		String stringToSign = clientId + "," + badLocalId + "," + clientId;

		String wrongSignature = MartusUtilities.createSignature(stringToSign, serverSecurity);
		Vector wrongSigResult = testServer.legacyDownloadAuthorizedPacket(clientId, badLocalId, clientId, wrongSignature);
		assertNotNull(wrongSigResult);
		assertEquals("Not wrong sig?", NetworkInterfaceConstants.SIG_ERROR, wrongSigResult.get(0));

		String signature = MartusUtilities.createSignature(stringToSign, clientSecurity);
		
		Vector notFoundResult = testServer.legacyDownloadAuthorizedPacket(clientId, badLocalId, clientId, signature);
		assertNotNull(notFoundResult);
		assertEquals("Not notfound?", NetworkInterfaceConstants.NOT_FOUND, notFoundResult.get(0));
		
		String zip = MockBulletin.saveToZipString(privateBulletin);
		testServer.uploadBulletin(clientId, privateBulletin.getLocalId(), zip);

		String localId =privateBulletin.getFieldDataPacket().getLocalId();
		stringToSign = clientId + "," + localId + "," + clientId;
		signature = MartusUtilities.createSignature(stringToSign, clientSecurity);
		Vector foundResult = testServer.legacyDownloadAuthorizedPacket(clientId, localId, clientId, signature);
		assertNotNull(foundResult);
		assertEquals("not OK?", NetworkInterfaceConstants.OK, foundResult.get(0));
		
		File tempFile = File.createTempFile("$$$MartusTestSrvrDnldPkt", null);
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(Base64.decode(zip));
		out.close();
		
		ZipFile zipFile = new ZipFile(tempFile);
		ZipEntry entry = zipFile.getEntry(localId);
		InputStream in = new BufferedInputStream(zipFile.getInputStream(entry));
		byte[] packetBytes = new byte[(int)entry.getSize()];
		int gotCount = in.read(packetBytes);
		assertEquals("didn't read enough?", entry.getSize(), gotCount);
		in.close();
		zipFile.close();
		tempFile.delete();
		
		String fdpXml = new String(packetBytes);
		assertEquals("wrong data?", foundResult.get(1), fdpXml);
	}


	public void testDownloadPacket() throws Exception
	{
		uploadSampleBulletin();

		String clientId = clientSecurity.getPublicKeyString();
		Vector notFoundResult = testServer.legacyDownloadPacket(clientId, "123bad");
		assertNotNull(notFoundResult);
		assertEquals("Not notfound?", NetworkInterfaceConstants.NOT_FOUND, notFoundResult.get(0));
		
		String zip = MockBulletin.saveToZipString(privateBulletin);
		testServer.uploadBulletin(clientId, privateBulletin.getLocalId(), zip);

		String localId =privateBulletin.getFieldDataPacket().getLocalId();
		Vector foundResult = testServer.legacyDownloadPacket(clientId, localId);
		assertNotNull(foundResult);
		assertEquals("not OK?", NetworkInterfaceConstants.OK, foundResult.get(0));
		
		File tempFile = File.createTempFile("$$$MartusTestSrvrDnldPkt", null);
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(Base64.decode(zip));
		out.close();
		
		ZipFile zipFile = new ZipFile(tempFile);
		ZipEntry entry = zipFile.getEntry(localId);
		InputStream in = new BufferedInputStream(zipFile.getInputStream(entry));
		byte[] packetBytes = new byte[(int)entry.getSize()];
		int gotCount = in.read(packetBytes);
		assertEquals("didn't read enough?", entry.getSize(), gotCount);
		in.close();
		zipFile.close();
		tempFile.delete();
		
		String fdpXml = new String(packetBytes);
		assertEquals("wrong data?", foundResult.get(1), fdpXml);
	}

	public void testDownloadPacketAttachment()
	{
		uploadSampleBulletin();
		
		String attachmentLocalId = b1.getBulletinHeaderPacket().getPublicAttachmentIds()[0];
		
		Vector result = testServer.legacyDownloadPacket(b1.getAccount(), attachmentLocalId);
		assertEquals("didn't return invalid data?", NetworkInterfaceConstants.INVALID_DATA, result.get(0));
	}

	public void testDeleteDraftBulletinsEmptyList() throws Exception
	{
		String[] allIds = {};
		String resultAllOk = testServer.deleteDraftBulletins(store.getAccountId(), allIds);
		assertEquals("Empty not ok?", OK, resultAllOk);
	}
	
	public void testDeleteDraftBulletinsThroughHandler() throws Exception
	{
		String[] allIds = uploadSampleDrafts();
		Vector parameters = new Vector();
		parameters.add(new Integer(allIds.length));
		for (int i = 0; i < allIds.length; i++)
			parameters.add(allIds[i]);

		String sig = MartusUtilities.sign(parameters, clientSecurity);
		Vector result = testServerInterface.deleteDraftBulletins(store.getAccountId(), parameters, sig);
		assertEquals("Result size?", 1, result.size());
		assertEquals("Result not ok?", OK, result.get(0));
	}
		
	public void testDeleteDraftBulletins() throws Exception
	{
		String[] allIds = uploadSampleDrafts();
		String resultAllOk = testServer.deleteDraftBulletins(store.getAccountId(), allIds);
		assertEquals("Good 3 not ok?", OK, resultAllOk);
		assertEquals("Didn't delete all?", 0, db.getRecordCount());
		
		String[] twoGoodOneBad = uploadSampleDrafts();
		twoGoodOneBad[1] = "Not a valid local id";
		String resultOneBad = testServer.deleteDraftBulletins(store.getAccountId(), twoGoodOneBad);
		assertEquals("Two good one bad not incomplete?", INCOMPLETE, resultOneBad);
		assertEquals("Didn't delete two?", 1*3, db.getRecordCount());
		
		uploadSampleBulletin();
		int newRecordCount = db.getRecordCount();
		assertNotEquals("Didn't upload?", 1*3, newRecordCount);
		String[] justSealed = new String[] {b1.getLocalId()};
		String resultSealed = testServer.deleteDraftBulletins(store.getAccountId(), justSealed);
		assertEquals("Sealed not ok?", OK, resultAllOk);
		assertEquals("Deleted sealed?", newRecordCount, db.getRecordCount());
	}

	String[] uploadSampleDrafts() throws Exception
	{
		assertEquals("db not empty?", 0, db.getRecordCount());
		Bulletin draft1 = store.createEmptyBulletin();
		String zip1 = uploadSampleDraftBulletin(draft1);
		assertEquals("Didn't save 1?", 1*3, db.getRecordCount());
		Bulletin draft2 = store.createEmptyBulletin();
		String zip2 = uploadSampleDraftBulletin(draft2);
		assertEquals("Didn't save 2?", 2*3, db.getRecordCount());
		Bulletin draft3 = store.createEmptyBulletin();
		String zip3 = uploadSampleDraftBulletin(draft3);
		assertEquals("Didn't save 3?", 3*3, db.getRecordCount());

		return new String[] {draft1.getLocalId(), draft2.getLocalId(), draft3.getLocalId()};
	}

	public void testKeyBelongsToClient()
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId("a", "b");
		DatabaseKey key = DatabaseKey.createSealedKey(uid);
		assertEquals("doesn't belong ", false, MartusServer.keyBelongsToClient(key, "b"));
		assertEquals("belongs ", true, MartusServer.keyBelongsToClient(key, "a"));
	}

	public void testAllowUploads() throws Exception
	{
		testServer.clientsThatCanUpload.clear();

		String clientId = "some client";
		String clientId2 = "another client";

		assertEquals("clientId default", false, testServer.canClientUpload(clientId));
		assertEquals("clientId2 default", false, testServer.canClientUpload(clientId2));
		assertEquals("empty default", false, testServer.canClientUpload(""));

		testServer.allowUploads(clientId);
		assertEquals("clientId in", true, testServer.canClientUpload(clientId));
		assertEquals("clientId2 still not in", false, testServer.canClientUpload(clientId2));
		assertEquals("empty still out", false, testServer.canClientUpload(""));

		testServer.allowUploads(clientId2);
		assertEquals("clientId2", true, testServer.canClientUpload(clientId2));
		assertEquals("clientId still", true, testServer.canClientUpload(clientId));
	}

	public void testLoadUploadList() throws Exception
	{
		testServer.clientsThatCanUpload.clear();

		String clientId = "some client";
		String clientId2 = "another client";
		assertEquals("clientId default", false, testServer.canClientUpload(clientId));
		assertEquals("clientId2 default", false, testServer.canClientUpload(clientId2));

		String testFileContents = "blah blah\n" + clientId2 + "\nYeah yeah\n\n";
		testServer.loadCanUploadList(new BufferedReader(new StringReader(testFileContents)));
		assertEquals("clientId still out", false, testServer.canClientUpload(clientId));
		assertEquals("clientId2 now in", true, testServer.canClientUpload(clientId2));
		assertEquals("empty still out", false, testServer.canClientUpload(""));
	}
	
	public void testAllowUploadsWritingToDisk() throws Exception
	{
		testServer.clientsThatCanUpload.clear();
		
		File file = File.createTempFile("$$$MartusTestServer", null);
		file.deleteOnExit();

		String clientId1 = "slidfj";
		String clientId2 = "woeiruwe";
		testServer.allowUploadFile = file;
		testServer.allowUploads(clientId1);
		testServer.allowUploads(clientId2);
		long lastUpdate = file.lastModified();
		Thread.sleep(1000);
		
		boolean got1 = false;
		boolean got2 = false;
		UnicodeReader reader = new UnicodeReader(file);
		while(true)
		{
			String line = reader.readLine();
			if(line == null)
				break;
				
			if(line.equals(clientId1))
				got1 = true;
			else if(line.equals(clientId2))
				got2 = true;
			else
				fail("unknown id found!");
		}
		reader.close();
		assertTrue("missing id1?", got1);
		assertTrue("missing id2?", got2);
		
		BufferedReader reader2 = new BufferedReader(new UnicodeReader(file));
		testServer.loadCanUploadList(reader2);
		reader2.close();
		assertEquals("reading changed the file?", lastUpdate, file.lastModified());
		
		file.delete();
	}
	
	public void testAllowUploadsPersistToNextSession() throws Exception
	{
		String dataDirectory = testServer.dataDirectoryString;

		testServer.clientsThatCanUpload.clear();
		
		String sampleId = "2345235";
		
		testServer.allowUploads(sampleId);
		
		MockMartusServer other = new MockMartusServer(new File(dataDirectory));
		assertEquals("didn't get saved/loaded?", true, other.canClientUpload(sampleId));
	}
	
	public void testRequestUploadRights() throws Exception
	{
		String sampleId = "384759896";
		String sampleMagicWord = "bliflfji";

		testServer.clientsThatCanUpload.clear();
		testServer.setMagicWord(sampleMagicWord);
		
		String failed = testServer.requestUploadRights(sampleId, sampleMagicWord + "x");
		assertEquals("didn't work?", NetworkInterfaceConstants.REJECTED, failed);
		
		String worked = testServer.requestUploadRights(sampleId, sampleMagicWord);
		assertEquals("didn't work?", NetworkInterfaceConstants.OK, worked);
	}
	
	public void testLoadingMagicWords() throws Exception
	{
		String dataDirectory = testServer.dataDirectoryString;
		
		String sampleMagicWord = "kef7873n2";
		
		File file = testServer.magicWordsFile;
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(sampleMagicWord);
		writer.close();
		
		MockMartusServer other = new MockMartusServer(new File(dataDirectory));
		
		String worked = other.requestUploadRights("whatever", sampleMagicWord);
		assertEquals("didn't work?", NetworkInterfaceConstants.OK, worked);
	}

	public void testGetAllPacketKeysSealed() throws Exception
	{
		BulletinHeaderPacket bhp = b1.getBulletinHeaderPacket();
		DatabaseKey[] keys = MartusUtilities.getAllPacketKeys(bhp);
		
		assertNotNull("null ids?", keys);
		assertEquals("count?", 5, keys.length);
		boolean foundHeader = false;
		boolean foundPublicData = false;
		boolean foundPrivateData = false;
		boolean foundPublicAttachment = false;
		boolean foundPrivateAttachment = false;

		for(int i=0; i < keys.length; ++i)
		{
			assertEquals("Key " + i + " not sealed?", true, keys[i].isSealed());
			if(keys[i].getLocalId().equals(bhp.getLocalId()))
				foundHeader = true;
			if(keys[i].getLocalId().equals(bhp.getFieldDataPacketId()))
				foundPublicData = true;
			if(keys[i].getLocalId().equals(bhp.getPrivateFieldDataPacketId()))
				foundPrivateData = true;
			if(keys[i].getLocalId().equals(bhp.getPublicAttachmentIds()[0]))
				foundPublicAttachment = true;
			if(keys[i].getLocalId().equals(bhp.getPrivateAttachmentIds()[0]))
				foundPrivateAttachment = true;
		
		}		
		assertTrue("header id?", foundHeader);
		assertTrue("data id?", foundPublicData);
		assertTrue("private id?", foundPrivateData);
		assertTrue("attachment public id?", foundPublicAttachment);
		assertTrue("attachment private id?", foundPrivateAttachment);
	}
	
	public void testGetAllPacketKeysDraft() throws Exception
	{
		BulletinHeaderPacket bhp = draft.getBulletinHeaderPacket();
		DatabaseKey[] keys = MartusUtilities.getAllPacketKeys(bhp);
		
		assertNotNull("null ids?", keys);
		assertEquals("count?", 3, keys.length);
		boolean foundHeader = false;
		boolean foundPublicData = false;
		boolean foundPrivateData = false;

		for(int i=0; i < keys.length; ++i)
		{
			assertEquals("Key " + i + " not draft?", true, keys[i].isDraft());
			if(keys[i].getLocalId().equals(bhp.getLocalId()))
				foundHeader = true;
			if(keys[i].getLocalId().equals(bhp.getFieldDataPacketId()))
				foundPublicData = true;
			if(keys[i].getLocalId().equals(bhp.getPrivateFieldDataPacketId()))
				foundPrivateData = true;
		
		}		
		assertTrue("header id?", foundHeader);
		assertTrue("data id?", foundPublicData);
		assertTrue("private id?", foundPrivateData);
	}

	public void testAuthenticateServer() throws Exception
	{
		String notBase64 = "this is not base 64 ";
		String result = testServer.authenticateServer(notBase64);
		assertEquals("error not correct?", NetworkInterfaceConstants.INVALID_DATA, result);

		MockMartusServer server = new MockMartusServer();
		String base64data = Base64.encode(new byte[]{1,2,3});
		result = server.authenticateServer(base64data);
		assertEquals("did not return server error?", NetworkInterfaceConstants.SERVER_ERROR, result);

		server.setSecurity(serverSecurity);
		result = server.authenticateServer(base64data);
		byte[] signature = Base64.decode(result);
		InputStream in = new ByteArrayInputStream(Base64.decode(base64data));
		assertTrue("Invalid signature?", clientSecurity.isSignatureValid(server.security.getPublicKeyString(), in, signature));
		
	}
	
	public void testBannedClients()
		throws Exception
	{
		String clientId = clientSecurity.getPublicKeyString();
		String hqId = hqSecurity.getPublicKeyString();
		File tempBanned = createTempFile();
		
		UnicodeWriter writer = new UnicodeWriter(tempBanned);
		writer.writeln(clientId);
		writer.close();
		
		String bogusStringParameter = "this is never used in this call. right?";

		testServer.allowUploads(clientId);
		testServer.loadBannedClients(tempBanned);

		Vector vecResult = testServer.listMySealedBulletinIds(clientId);
		verifyErrorResult("listMySealedBulletinIds", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listMySealedBulletinIds", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listMyDraftBulletinIds(clientId);
		verifyErrorResult("listMyDraftBulletinIds", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listMyDraftBulletinIds", 0, testServer.getNumberActiveClients() );
		
		String strResult = testServer.requestUploadRights(clientId, bogusStringParameter);
		assertEquals("requestUploadRights", NetworkInterfaceConstants.REJECTED, strResult );
		assertEquals("requestUploadRights", 0, testServer.getNumberActiveClients() );
		
		strResult = uploadBulletinChunk(testServerInterface, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter, clientSecurity);
		assertEquals("uploadBulletinChunk", NetworkInterfaceConstants.REJECTED, strResult );
		assertEquals("uploadBulletinChunk", 0, testServer.getNumberActiveClients() );

		vecResult = downloadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), bogusStringParameter, 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		verifyErrorResult("downloadBulletinChunk", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("downloadBulletinChunk", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.listMySealedBulletinIds(clientSecurity.getPublicKeyString());
		verifyErrorResult("listMySealedBulletinIds", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listMySealedBulletinIds", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.legacyDownloadAuthorizedPacket(clientId, bogusStringParameter, clientId, bogusStringParameter);
		verifyErrorResult("legacyDownloadAuthorizedPacket", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("legacyDownloadAuthorizedPacket", 0, testServer.getNumberActiveClients() );

		strResult = testServer.putBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter);
		assertEquals("putBulletinChunk", NetworkInterfaceConstants.REJECTED, strResult);
		assertEquals("putBulletinChunk", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.getBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0);
		verifyErrorResult("getBulletinChunk", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("getBulletinChunk", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.getPacket(clientId, bogusStringParameter, bogusStringParameter, bogusStringParameter);
		verifyErrorResult("getPacket", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("getPacket", 0, testServer.getNumberActiveClients() );

		strResult = testServer.deleteDraftBulletins(clientId, new String[] {bogusStringParameter} );
		assertEquals("deleteDraftBulletins", NetworkInterfaceConstants.REJECTED, strResult);
		assertEquals("deleteDraftBulletins", 0, testServer.getNumberActiveClients() );

		strResult = testServer.putContactInfo(clientId, new Vector() );
		assertEquals("putContactInfo", NetworkInterfaceConstants.REJECTED, strResult);		
		assertEquals("putContactInfo", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.downloadFieldDataPacket(hqId, bogusStringParameter, bogusStringParameter, clientId, bogusStringParameter);
		verifyErrorResult("downloadFieldDataPacket", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("downloadFieldDataPacket", 0, testServer.getNumberActiveClients() );		

		vecResult = testServer.listFieldOfficeSealedBulletinIds(hqId, clientId);
		verifyErrorResult("listFieldOfficeSealedBulletinIds1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeSealedBulletinIds1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeDraftBulletinIds(hqId, clientId);
		verifyErrorResult("listFieldOfficeDraftBulletinIds1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeDraftBulletinIds1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeAccounts(hqId);
		verifyErrorResult("listFieldOfficeAccounts1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeAccounts1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeSealedBulletinIds(clientId, clientId);
		verifyErrorResult("listFieldOfficeSealedBulletinIds2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeSealedBulletinIds2", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeDraftBulletinIds(clientId, clientId);
		verifyErrorResult("listFieldOfficeDraftBulletinIds2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeDraftBulletinIds2", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeAccounts(clientId);
		verifyErrorResult("listFieldOfficeAccounts2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeAccounts2", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.downloadFieldOfficeBulletinChunk(bogusStringParameter, bogusStringParameter, clientId, 0, 0, bogusStringParameter);
		verifyErrorResult("downloadFieldOfficeBulletinChunk2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("downloadFieldOfficeBulletinChunk2", 0, testServer.getNumberActiveClients() );	
	}
	
	public void testServerShutdown() throws Exception
	{
		boolean result;
		String clientId = clientSecurity.getPublicKeyString();
		String hqId = hqSecurity.getPublicKeyString();	
		String bogusStringParameter = "this is never used in this call. right?";
		
		result = testServer.isShutdownRequested();
		assertEquals("isShutdownRequested 1", false, result );
		
		assertEquals("testServerShutdown: incrementActiveClientsCounter 1", 0, testServer.getNumberActiveClients() );
		
		testServer.incrementActiveClientsCounter();
		assertEquals("testServerShutdown: incrementActiveClientsCounter 2", 1, testServer.getNumberActiveClients() );
		File exitFile = new File(testServer.getDataDirectory(), "exit");
		exitFile.createNewFile();
		
		testServer.allowUploads(clientId);

		result = testServer.isShutdownRequested();
		assertEquals("isShutdownRequested 2", true, result );

		Vector vecResult = testServer.listMySealedBulletinIds(clientId);
		verifyErrorResult("listMySealedBulletinIds", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("listMySealedBulletinIds", 1, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listMyDraftBulletinIds(clientId);
		verifyErrorResult("listMyDraftBulletinIds", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("listMyDraftBulletinIds", 1, testServer.getNumberActiveClients() );
		
		String strResult = testServer.requestUploadRights(clientId, bogusStringParameter);
		assertEquals("requestUploadRights", NetworkInterfaceConstants.SERVER_DOWN, strResult );
		assertEquals("requestUploadRights", 1, testServer.getNumberActiveClients() );
		
		strResult = uploadBulletinChunk(testServerInterface, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter, clientSecurity);
		assertEquals("uploadBulletinChunk", NetworkInterfaceConstants.SERVER_DOWN, strResult );
		assertEquals("uploadBulletinChunk", 1, testServer.getNumberActiveClients() );

		vecResult = downloadBulletinChunk(testServerInterface, clientSecurity.getPublicKeyString(), bogusStringParameter, 0, NetworkInterfaceConstants.MAX_CHUNK_SIZE);
		verifyErrorResult("downloadBulletinChunk", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("downloadBulletinChunk", 1, testServer.getNumberActiveClients() );

		vecResult = testServer.listMySealedBulletinIds(clientSecurity.getPublicKeyString());
		verifyErrorResult("listMySealedBulletinIds", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("listMySealedBulletinIds", 1, testServer.getNumberActiveClients() );

		vecResult = testServer.legacyDownloadAuthorizedPacket(clientId, bogusStringParameter, clientId, bogusStringParameter);
		verifyErrorResult("legacyDownloadAuthorizedPacket", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("legacyDownloadAuthorizedPacket", 1, testServer.getNumberActiveClients() );

		strResult = testServer.putBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter);
		assertEquals("putBulletinChunk", NetworkInterfaceConstants.SERVER_DOWN, strResult);
		assertEquals("putBulletinChunk", 1, testServer.getNumberActiveClients() );

		vecResult = testServer.getBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0);
		verifyErrorResult("getBulletinChunk", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("getBulletinChunk", 1, testServer.getNumberActiveClients() );

		vecResult = testServer.getPacket(clientId, bogusStringParameter, bogusStringParameter, bogusStringParameter);
		verifyErrorResult("getPacket", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("getPacket", 1, testServer.getNumberActiveClients() );

		strResult = testServer.deleteDraftBulletins(clientId, new String[] {bogusStringParameter} );
		assertEquals("deleteDraftBulletins", NetworkInterfaceConstants.SERVER_DOWN, strResult);
		assertEquals("deleteDraftBulletins", 1, testServer.getNumberActiveClients() );

		strResult = testServer.putContactInfo(clientId, new Vector() );
		assertEquals("putContactInfo", NetworkInterfaceConstants.SERVER_DOWN, strResult);		
		assertEquals("putContactInfo", 1, testServer.getNumberActiveClients() );

		vecResult = testServer.downloadFieldDataPacket(hqId, bogusStringParameter, bogusStringParameter, clientId, bogusStringParameter);
		verifyErrorResult("downloadFieldDataPacket", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("downloadFieldDataPacket", 1, testServer.getNumberActiveClients() );

		vecResult = testServer.listFieldOfficeSealedBulletinIds(hqId, clientId);
		verifyErrorResult("listFieldOfficeSealedBulletinIds", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("listFieldOfficeSealedBulletinIds", 1, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeDraftBulletinIds(hqId, clientId);
		verifyErrorResult("listFieldOfficeDraftBulletinIds", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("listFieldOfficeDraftBulletinIds", 1, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeAccounts(clientId);
		verifyErrorResult("listFieldOfficeAccounts", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("listFieldOfficeAccounts", 1, testServer.getNumberActiveClients() );
		
		exitFile.delete();
				
		testServer.decrementActiveClientsCounter();
		assertEquals("testServerShutdown: clientCount", 0, testServer.getNumberActiveClients() );	
	}
	
	public void testClientCounter()
	{
		assertEquals("getNumberActiveClients 1", 0, testServer.getNumberActiveClients());
		
		testServer.incrementActiveClientsCounter();
		testServer.incrementActiveClientsCounter();
		assertEquals("getNumberActiveClients 2", 2, testServer.getNumberActiveClients());
		
		testServer.decrementActiveClientsCounter();
		testServer.decrementActiveClientsCounter();
		assertEquals("getNumberActiveClients 3", 0, testServer.getNumberActiveClients());
	}
	
	public void testServerConnectionDuringShutdown() throws Exception
	{
		Vector reply;
		
		testServer.incrementActiveClientsCounter();
		File exitFile = new File(testServer.getDataDirectory(), "exit");
		exitFile.createNewFile();
		
		reply = testServer.getServerInformation();
		assertEquals("getServerInformation", NetworkInterfaceConstants.SERVER_DOWN, reply.get(0) );
		
		testServer.decrementActiveClientsCounter();
		exitFile.delete();
	}
	
	void verifyErrorResult(String label, Vector vector, String expected )
	{
		assertEquals( label + " error size not 1?", 1, vector.size());
		assertEquals( label + " error wrong result code", expected, vector.get(0));
	}
	void uploadSampleBulletin() 
	{
		testServer.security = serverSecurity;
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString);
	}
	
	String uploadSampleDraftBulletin(Bulletin draft) throws Exception
	{
		testServer.security = serverSecurity;
		testServer.clientsThatCanUpload.clear();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		
		String draftZipString = MockBulletin.saveToZipString(draft);
		String result = testServer.uploadBulletin(clientSecurity.getPublicKeyString(), draft.getLocalId(), draftZipString);
		assertEquals("upload failed?", OK, result);
		return draftZipString;
	}
	
	String uploadBulletinChunk(NetworkInterface server, String authorId, String localId, int totalLength, int offset, int chunkLength, String data, MartusCrypto signer) throws Exception
	{
		String stringToSign = authorId + "," + localId + "," + Integer.toString(totalLength) + "," + 
					Integer.toString(offset) + "," + Integer.toString(chunkLength) + "," + data;
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = signer.createSignature(new ByteArrayInputStream(bytesToSign));
		String signature = Base64.encode(sigBytes);
		return server.uploadBulletinChunk(authorId, localId, totalLength, offset, chunkLength, data, signature);
	}
	
	Vector downloadBulletinChunk(NetworkInterface server, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize) throws Exception
	{
		String stringToSign = authorAccountId + "," + bulletinLocalId + "," + 
					Integer.toString(chunkOffset) + "," + Integer.toString(maxChunkSize);
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = clientSecurity.createSignature(new ByteArrayInputStream(bytesToSign));
		String signature = Base64.encode(sigBytes);
		return server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize, signature);
	}
	
	Vector downloadFieldOfficeBulletinChunk(NetworkInterface server, MartusCrypto hqSecurity, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize) throws Exception
	{
		String hqAccountId = hqSecurity.getPublicKeyString();
		String stringToSign = authorAccountId + "," + bulletinLocalId + "," + hqAccountId + "," +
					Integer.toString(chunkOffset) + "," + Integer.toString(maxChunkSize);
		String signature = MartusUtilities.createSignature(stringToSign, hqSecurity);
		return server.downloadFieldOfficeBulletinChunk(authorAccountId, bulletinLocalId, hqAccountId, chunkOffset, maxChunkSize, signature);
	}
	
	// TODO: This is only needed to support the Guatemala HQ's. It should be removed
	// after all those have been updated to newer software!
	Vector legacyDownloadFieldOfficeBulletinChunkAsMyBulletin(NetworkInterface server, MartusCrypto hqSecurity, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize) throws Exception
	{
		String hqAccountId = hqSecurity.getPublicKeyString();
		String stringToSign = authorAccountId + "," + bulletinLocalId + "," + 
					Integer.toString(chunkOffset) + "," + Integer.toString(maxChunkSize);
		String signature = MartusUtilities.createSignature(stringToSign, hqSecurity);
		return server.downloadMyBulletinChunk(authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize, signature);
	}
	
	
	public int TESTSERVERTESTPORT = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_NON_SSL + 35;
	
	static File tempFile;
	static Bulletin b1;
	static String b1ZipString;
	static byte[] b1ZipBytes;
	static byte[] b1ChunkBytes0;
	static byte[] b1ChunkBytes1;
	static String b1ChunkData0;
	static String b1ChunkData1;

	static Bulletin b2;
	static Bulletin privateBulletin;

	static Bulletin draft;

	static MartusSecurity clientSecurity;
	static MartusSecurity serverSecurity;
	static MartusSecurity hqSecurity;
	static BulletinStore store;

	final static byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
	final static byte[] file1Bytes = {1,2,3,4,4,3,2,1};
	final static byte[] file2Bytes = {1,2,3,4,4,3,2,1,0};
	
	MockMartusServer testServer;
	NetworkInterface testServerInterface;
	MockServerDatabase db;
}
