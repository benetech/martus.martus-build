package org.martus.server;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.BulletinRetrieverGatewayInterface;
import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MockMartusSecurity;
import org.martus.common.MockServerDatabase;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;

public class TestMirroringRetriever extends TestCaseEnhanced
{
	public TestMirroringRetriever(String name)
	{
		super(name);
	}
	
	public void testBasics() throws Exception
	{
		MockServerDatabase db = new MockServerDatabase();
		MartusSecurity security = new MockMartusSecurity();
		security.createKeyPair();
		
		FakeServerSupplier supplier = new FakeServerSupplier();
		supplier.authorizedCaller = security.getPublicKeyString();

		SupplierSideMirroringHandler handler = new SupplierSideMirroringHandler(supplier);
		FakeRetrieverGateway gateway = new FakeRetrieverGateway(handler);
		supplier.returnResultTag = NetworkInterfaceConstants.OK;
		
		FakeMirroringRetriever retriever = new FakeMirroringRetriever(db, gateway, security);
		UniversalId uid = UniversalId.createDummyUniversalId();
		retriever.retrieveOneBulletin(uid);
		assertEquals(uid.getAccountId(), supplier.gotAccount);
		assertEquals(uid.getLocalId(), supplier.gotLocalId);

		int expectedLength = Base64.decode(supplier.returnZipData).length;
		assertEquals("retriever wrong uid?", uid, retriever.savedUid);
		assertEquals("retriever wrong length?", expectedLength, retriever.savedFileLength);
	}
}


class FakeRetrieverGateway implements BulletinRetrieverGatewayInterface
{
	FakeRetrieverGateway(MirroringInterface handlerToUse)
	{
		handler = handlerToUse;
	}
	
	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int maxChunkSize) throws 
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_GET_BULLETIN_CHUNK_FOR_MIRRORING);
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));
		String signature = MartusUtilities.sign(parameters, signer);
		return new NetworkResponse(handler.request(signer.getPublicKeyString(), parameters, signature));
	}
					
	MirroringInterface handler;
}

class FakeMirroringRetriever extends MirroringRetriever
{
	FakeMirroringRetriever(Database databaseToUse, BulletinRetrieverGatewayInterface gatewayToUse, MartusCrypto securityToUse)
	{
		super(databaseToUse, gatewayToUse, securityToUse);
	}
	
	void saveFileToDatabase(UniversalId uid, File tempFile) throws IOException
	{
		savedUid = uid;
		savedFileLength = tempFile.length();
	}

	UniversalId savedUid;
	long savedFileLength;
}
