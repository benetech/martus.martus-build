package org.martus.client;

import java.io.File;
import java.io.InputStream;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.common.UniversalId;

public class TestClientFileDatabase extends TestCaseEnhanced 
{
	public TestClientFileDatabase(String name) 
	{
		super(name);
	}
	
	
	public void testFindLegacyRecords() throws Exception
	{
		Database mockDatabase = new MockClientDatabase();
		MartusCrypto security = new MockMartusSecurity();
		
		File tempDir = createTempFile();
		tempDir.delete();
		tempDir.mkdir();
		Database clientFileDatabase = new ClientFileDatabase(tempDir, security);
		clientFileDatabase.initialize();
		
		internalTestFindLegacyRecords(mockDatabase);
		internalTestFindLegacyRecords(clientFileDatabase);

	}
	
	
	private void internalTestFindLegacyRecords(Database db) throws Exception
	{
		MartusCrypto security = new MockMartusSecurity();
		
		UniversalId uid = UniversalId.createDummyUniversalId();
		DatabaseKey legacyKey = DatabaseKey.createLegacyKey(uid);
		db.writeRecord(legacyKey, smallString);
		InputStream inLegacy = db.openInputStream(legacyKey, security);
		assertNotNull("legacy not found?", inLegacy);
		inLegacy.close();
		
		DatabaseKey draftKey = DatabaseKey.createDraftKey(uid);
		InputStream inDraft = db.openInputStream(legacyKey, security);
		assertNotNull("draft not found?", inDraft);
		inDraft.close();

		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		InputStream inSealed = db.openInputStream(sealedKey, security);
		assertNotNull("sealed not found?", inSealed);
		inSealed.close();
		
		db.deleteAllData();
	}

	private static final String smallString = "some text";
}
