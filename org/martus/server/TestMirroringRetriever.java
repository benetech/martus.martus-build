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
		CallerSideMirroringGateway gateway = new CallerSideMirroringGateway(handler);
		supplier.returnResultTag = NetworkInterfaceConstants.OK;
		
		MirroringRetriever retriever = new MirroringRetriever(db, gateway, security);
		UniversalId uid = UniversalId.createDummyUniversalId();
		File gotFile = retriever.retrieveOneBulletin(uid);
		assertEquals(uid.getAccountId(), supplier.gotAccount);
		assertEquals(uid.getLocalId(), supplier.gotLocalId);

		int expectedLength = Base64.decode(supplier.returnZipData).length;
		assertEquals("file wrong length?", expectedLength, gotFile.length());
	}
}


class CallerSideMirroringGateway implements BulletinRetrieverGatewayInterface
{
	CallerSideMirroringGateway(MirroringInterface handlerToUse)
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

