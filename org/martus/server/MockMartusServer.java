package org.martus.server;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MockServerDatabase;
import org.martus.common.NetworkInterfaceConstants;

public class MockMartusServer extends MartusServer
{
	public MockMartusServer() throws Exception
	{
		this(new TempDirectory());
	}
	
	public MockMartusServer(File dataDir) throws Exception
	{
		super(dataDir);
		setDatabase(new MockServerDatabase());
	}
	
	public void setSecurity(MartusCrypto securityToUse)
	{
		security = securityToUse;
	}
	
	public void setDatabase(Database databaseToUse)
	{
		database = databaseToUse;
	}
	
	public void initialize()
	{
		loadConfigurationFiles();
	}
	
	public String ping()
	{
		return "" + NetworkInterfaceConstants.VERSION;
	}
	
	public Vector getServerInformation()
	{
		if(infoResponse != null)
			return new Vector(infoResponse);
		return (Vector)(super.getServerInformation()).clone();
	}
	
	public String authenticateServer(String tokenToSign)
	{
		if(authenticateResponse != null)
			return new String(authenticateResponse);
		return "" + super.authenticateServer(tokenToSign);
	}
	
	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		lastClientId = authorAccountId;
		lastUploadedBulletinId = bulletinLocalId;				

		if(uploadResponse != null)
			return new String(uploadResponse);

		return "" + super.uploadBulletin(authorAccountId, bulletinLocalId, data);
	}
	
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature) 
	{
		lastClientId = authorAccountId;
		lastUploadedBulletinId = bulletinLocalId;				

		if(uploadResponse != null)
			return new String(uploadResponse);
		return "" + super.uploadBulletinChunk(authorAccountId, bulletinLocalId, totalSize, chunkOffset, chunkSize, data, signature);
	}

	public String putBulletinChunk(String uploaderAccountId, String authorAccountId, String bulletinLocalId,
								int chunkOffset, int chunkSize, int totalSize, String data) 
	{
		lastClientId = authorAccountId;
		lastUploadedBulletinId = bulletinLocalId;				

		if(uploadResponse != null)
			return new String(uploadResponse);

		return "" + super.putBulletinChunk(uploaderAccountId, authorAccountId, bulletinLocalId,
										chunkOffset, chunkSize, totalSize, data);
	}

	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		lastClientId = authorAccountId;

		if(downloadResponse != null)
			return new Vector(downloadResponse);

		return (Vector)(super.downloadBulletin(authorAccountId, bulletinLocalId)).clone();
	}
	
	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinId, int chunkOffset, int maxChunkSize, String signature)
	{
		lastClientId = authorAccountId;

		if(downloadResponse != null)
			return new Vector(downloadResponse);

		return (Vector)(super.downloadMyBulletinChunk(authorAccountId, bulletinId, chunkOffset, maxChunkSize, signature)).clone();
	}
	
	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int maxChunkSize) 
	{
		lastClientId = authorAccountId;

		if(downloadResponse != null)
			return new Vector(downloadResponse);

		return super.getBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, 
			chunkOffset, maxChunkSize);
	}

	public Vector listMySealedBulletinIds(String clientId, Vector retrieveTags)
	{
		lastClientId = clientId;
		if(listMyResponseNull)	
			return null;
		if(listMyResponse != null)
			return new Vector(listMyResponse);
		return (Vector)(super.listMySealedBulletinIds(clientId, retrieveTags)).clone();
		
	}
	
	public Vector listMyDraftBulletinIds(String clientId, Vector retrieveTags)
	{
		lastClientId = clientId;
		if(listMyResponseNull)	
			return null;
		if(listMyResponse != null)
			return new Vector(listMyResponse);
		return (Vector)(super.listMyDraftBulletinIds(clientId, retrieveTags)).clone();
		
	}
	
	public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		lastClientId = hqAccountId;
		if(listFieldOfficeSummariesResponseNull)	
			return null;
		if(listFieldOfficeSummariesResponse != null)
			return new Vector(listFieldOfficeSummariesResponse);
		return (Vector)(super.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId, retrieveTags)).clone();
	}

	public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		lastClientId = hqAccountId;
		if(listFieldOfficeSummariesResponseNull)	
			return null;
		if(listFieldOfficeSummariesResponse != null)
			return new Vector(listFieldOfficeSummariesResponse);
		return (Vector)(super.listFieldOfficeDraftBulletinIds(hqAccountId, authorAccountId, retrieveTags)).clone();
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		lastClientId = hqAccountId;
		if(listFieldOfficeAccountsResponseNull)	
			return null;
		if(listFieldOfficeAccountsResponse != null)
			return new Vector(listFieldOfficeAccountsResponse);
		return (Vector)(super.listFieldOfficeAccounts(hqAccountId)).clone();
	}

	public void setDownloadResponseNotFound()
	{
		downloadResponse = new Vector();
		downloadResponse.add(NetworkInterfaceConstants.NOT_FOUND);
	}
	
	public void setDownloadResponseOk()
	{
		downloadResponse = new Vector();
		downloadResponse.add(NetworkInterfaceConstants.OK);
	}
	
	public void setDownloadResponseReal()
	{
		downloadResponse = null;
	}
	
	public void setAuthenticateResponse(String response)
	{
		authenticateResponse = response;	
	}
	
	public void deleteAllData() throws Exception
	{
		getDatabase().deleteAllData();
		lastClientId = null;
		lastUploadedBulletinId = null;
	}
	
	public void nullListMyResponse(boolean nullResponse)
	{ 
		listMyResponseNull = nullResponse;
	}
	
	public void nullListFieldOfficeSummariesResponse(boolean nullResponse)
	{ 
		listFieldOfficeSummariesResponseNull = nullResponse;
	}
	
	public void nullListFieldOfficeAccountsResponse(boolean nullResponse)
	{ 
		listFieldOfficeAccountsResponseNull = nullResponse;
	}
	
	public void serverExit(int exitCode) throws Exception
	{
		throw new Exception("MockServer serverExit called");
	}
	
	public int getMaxFailedUploadAllowedAttempts()
	{
		return 2;
	}
	
	public long getUploadRequestTimerInterval()
	{
		return 100 * 1000;
	}
	
	public synchronized void subtractMaxFailedUploadAttemptsFromCounter()
	{
		return;
	}
	
	public synchronized void subtractMaxFailedUploadAttemptsFromServerCounter()
	{
		super.subtractMaxFailedUploadAttemptsFromCounter();
	}

	static class TempDirectory extends File
	{
		public TempDirectory() throws IOException
		{
			super(File.createTempFile("$$$MockMartusServer", null).getPath());
			deleteOnExit();
			delete();
			mkdir();
		}
	}
	
	public void deleteAllFiles() throws IOException
	{
		allowUploadFile.delete();
		if(allowUploadFile.exists())
			throw new IOException("allowUploadFile");
		magicWordsFile.delete();
		if(magicWordsFile.exists())
			throw new IOException("magicWordsFile");
		keyPairFile.delete();
		if(keyPairFile.exists())
			throw new IOException("keyPairFile");
			
		File uploadSig = MartusUtilities.getSignatureFileFromFile(allowUploadFile);
		if(uploadSig.exists())
			uploadSig.delete();
			
		File magicSig = MartusUtilities.getSignatureFileFromFile(magicWordsFile);
		if(magicSig.exists())
			magicSig.delete();

		if(triggerDirectory.exists())
			triggerDirectory.delete();
		if(startupConfigDirectory.exists())
			startupConfigDirectory.delete();

		dataDirectory.delete();
		if(dataDirectory.exists())
			throw new IOException("dataDirectory");
	}

	public Vector getNews(String accountId)
	{
		if(newsResponse != null)	
			return newsResponse;
		return super.getNews(accountId);
	}

	public Vector getPacket(
		String myAccountId,
		String authorAccountId,
		String bulletinLocalId,
		String packetLocalId)
	{
		if(countDownToGetPacketFailure == 1)
		{
			countDownToGetPacketFailure = 0;
			Vector result = new Vector();
			result.add(SERVER_ERROR);		
			return result;
		}
		if (countDownToGetPacketFailure > 0)
			--countDownToGetPacketFailure;
		
		return super.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
	}

	public int countDownToGetPacketFailure;
	public Vector newsResponse;
	public Vector infoResponse;
	public String uploadResponse;
	public Vector downloadResponse;
	public Vector listMyResponse;
	public Vector listFieldOfficeSummariesResponse;
	public Vector listFieldOfficeAccountsResponse;
	
	public String lastClientId;
	public String lastUploadedBulletinId;
	private boolean listMyResponseNull;
	private boolean listFieldOfficeSummariesResponseNull;
	private boolean listFieldOfficeAccountsResponseNull;
	
	private String authenticateResponse;

}
