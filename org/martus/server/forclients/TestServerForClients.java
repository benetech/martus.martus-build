package org.martus.server.forclients;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import org.martus.common.AttachmentProxy;
import org.martus.common.Base64;
import org.martus.common.Bulletin;
import org.martus.common.BulletinLoader;
import org.martus.common.BulletinSaver;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MockBulletin;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockServerDatabase;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UnicodeWriter;

public class TestServerForClients extends TestCaseEnhanced
{

	public TestServerForClients(String name)
	{
		super(name);
	}

	public static void main(String[] args)
	{
	}

	protected void setUp() throws Exception
	{
		TRACE_BEGIN("setUp");

		if(clientSecurity == null)
		{
			clientSecurity = new MartusSecurity();
			clientSecurity.createKeyPair(512);
			clientAccountId = clientSecurity.getPublicKeyString();
		}
		
		if(serverSecurity == null)
		{
			serverSecurity = new MartusSecurity();
			serverSecurity.createKeyPair(512);
		}
		
		if(testServerSecurity == null)
		{
			testServerSecurity = new MartusSecurity();
			testServerSecurity.createKeyPair(512);
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
		if(clientDatabase == null)
		{
			clientDatabase = new MockClientDatabase();
			b1 = new Bulletin(clientSecurity);
			b1.set(Bulletin.TAGTITLE, "Title1");
			b1.set(Bulletin.TAGPUBLICINFO, "Details1");
			b1.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
			File attachment = createTempFile();
			FileOutputStream out = new FileOutputStream(attachment);
			out.write(b1AttachmentBytes);
			out.close();
			b1.addPublicAttachment(new AttachmentProxy(attachment));
			b1.addPrivateAttachment(new AttachmentProxy(attachment));
			b1.setHQPublicKey(hqSecurity.getPublicKeyString());
			b1.setSealed();
			BulletinSaver.saveToDatabase(b1, clientDatabase, true, clientSecurity);
			b1 = BulletinLoader.loadFromDatabase(clientDatabase, DatabaseKey.createSealedKey(b1.getUniversalId()), clientSecurity);
	
			b2 = new Bulletin(clientSecurity);
			b2.set(Bulletin.TAGTITLE, "Title2");
			b2.set(Bulletin.TAGPUBLICINFO, "Details2");
			b2.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails2");
			b2.setSealed();
			BulletinSaver.saveToDatabase(b2, clientDatabase, true, clientSecurity);
			
			draft = new Bulletin(clientSecurity);
			draft.set(Bulletin.TAGPUBLICINFO, "draft public");
			draft.setDraft();
			BulletinSaver.saveToDatabase(draft, clientDatabase, true, clientSecurity);


			privateBulletin = new Bulletin(clientSecurity);
			privateBulletin.setAllPrivate(true);
			privateBulletin.set(Bulletin.TAGTITLE, "TitlePrivate");
			privateBulletin.set(Bulletin.TAGPUBLICINFO, "DetailsPrivate");
			privateBulletin.set(Bulletin.TAGPRIVATEINFO, "PrivateDetailsPrivate");
			privateBulletin.setSealed();
			BulletinSaver.saveToDatabase(privateBulletin, clientDatabase, true, clientSecurity);

			b1ZipString = MockBulletin.saveToZipString(clientDatabase, b1, clientSecurity);
			b1ZipBytes = Base64.decode(b1ZipString);
			b1ChunkBytes0 = new byte[100];
			b1ChunkBytes1 = new byte[b1ZipBytes.length - b1ChunkBytes0.length];
			System.arraycopy(b1ZipBytes, 0, b1ChunkBytes0, 0, b1ChunkBytes0.length);
			System.arraycopy(b1ZipBytes, b1ChunkBytes0.length, b1ChunkBytes1, 0, b1ChunkBytes1.length);
			b1ChunkData0 = Base64.encode(b1ChunkBytes0);
			b1ChunkData1 = Base64.encode(b1ChunkBytes1);
			
		}
		
		mockServer = new MockMartusServer(); 
		mockServer.verifyAndLoadConfigurationFiles();
		mockServer.setSecurity(testServerSecurity);
		testServer = mockServer.serverForClients;
		testServerInterface = new ServerSideNetworkHandler(testServer);
		serverDatabase = (MockServerDatabase)mockServer.getDatabase();

		TRACE_END();
	}

	protected void tearDown() throws Exception
	{
		TRACE_BEGIN("tearDown");

		assertEquals("isShutdownRequested", false, mockServer.isShutdownRequested());
		mockServer.deleteAllFiles();

		TRACE_END();
	}

	public void testBannedClients()
		throws Exception
	{
		TRACE_BEGIN("testBannedClients");

		String clientId = clientSecurity.getPublicKeyString();
		String hqId = hqSecurity.getPublicKeyString();
		File tempBanned = createTempFile();
		
		UnicodeWriter writer = new UnicodeWriter(tempBanned);
		writer.writeln(clientId);
		writer.close();
		
		String bogusStringParameter = "this is never used in this call. right?";

		testServer.allowUploads(clientId);
		testServer.loadBannedClients(tempBanned);

		Vector vecResult = null;
		vecResult = testServer.listMyDraftBulletinIds(clientId, new Vector());
		verifyErrorResult("listMyDraftBulletinIds", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listMyDraftBulletinIds", 0, testServer.getNumberActiveClients() );
		
		String strResult = testServer.requestUploadRights(clientId, bogusStringParameter);
		assertEquals("requestUploadRights", NetworkInterfaceConstants.REJECTED, strResult );
		assertEquals("requestUploadRights", 0, testServer.getNumberActiveClients() );
		
		strResult = uploadBulletinChunk(testServer, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter, clientSecurity);
		assertEquals("uploadBulletinChunk", NetworkInterfaceConstants.REJECTED, strResult );
		assertEquals("uploadBulletinChunk", 0, testServer.getNumberActiveClients() );

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

		vecResult = testServer.legacyListFieldOfficeSealedBulletinIds(hqId, clientId);
		verifyErrorResult("listFieldOfficeSealedBulletinIds1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeSealedBulletinIds1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeDraftBulletinIds(hqId, clientId, new Vector());
		verifyErrorResult("listFieldOfficeDraftBulletinIds1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeDraftBulletinIds1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeAccounts(hqId);
		verifyErrorResult("listFieldOfficeAccounts1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeAccounts1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.legacyListFieldOfficeSealedBulletinIds(clientId, clientId);
		verifyErrorResult("listFieldOfficeSealedBulletinIds2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeSealedBulletinIds2", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeDraftBulletinIds(clientId, clientId, new Vector());
		verifyErrorResult("listFieldOfficeDraftBulletinIds2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeDraftBulletinIds2", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeAccounts(clientId);
		verifyErrorResult("listFieldOfficeAccounts2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeAccounts2", 0, testServer.getNumberActiveClients() );

		TRACE_END();
	}

	public void testClientCounter()
	{
		TRACE_BEGIN("testClientCounter");

		assertEquals("getNumberActiveClients 1", 0, testServer.getNumberActiveClients());
		
		testServer.clientConnectionStart();
		testServer.clientConnectionStart();
		assertEquals("getNumberActiveClients 2", 2, testServer.getNumberActiveClients());
		
		testServer.clientConnectionExit();
		testServer.clientConnectionExit();
		assertEquals("getNumberActiveClients 3", 0, testServer.getNumberActiveClients());

		TRACE_END();
	}
	
	String uploadBulletinChunk(ServerForClients server, String authorId, String localId, int totalLength, int offset, int chunkLength, String data, MartusCrypto signer) throws Exception
	{
		String stringToSign = authorId + "," + localId + "," + Integer.toString(totalLength) + "," + 
					Integer.toString(offset) + "," + Integer.toString(chunkLength) + "," + data;
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = signer.createSignature(new ByteArrayInputStream(bytesToSign));
		String signature = Base64.encode(sigBytes);
		return server.uploadBulletinChunk(authorId, localId, totalLength, offset, chunkLength, data, signature);
	}
	
	void verifyErrorResult(String label, Vector vector, String expected )
	{
		assertTrue( label + " error size not at least 1?", vector.size() >= 1);
		assertEquals( label + " error wrong result code", expected, vector.get(0));
	}

	static MartusSecurity clientSecurity;
	static String clientAccountId;
	static MartusSecurity serverSecurity;
	static MartusSecurity testServerSecurity;
	static MartusSecurity hqSecurity;
	static MockClientDatabase clientDatabase;

	static Bulletin b1;
	static byte[] b1ZipBytes;
	static String b1ZipString;
	static byte[] b1ChunkBytes0;
	static byte[] b1ChunkBytes1;
	static String b1ChunkData0;
	static String b1ChunkData1;
	final static byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
	
	static Bulletin b2;
	static Bulletin privateBulletin;
	static Bulletin draft;

	static File tempFile;

	MockMartusServer mockServer; 
	ServerForClients testServer;
	NetworkInterface testServerInterface;
	MockServerDatabase serverDatabase;

}
