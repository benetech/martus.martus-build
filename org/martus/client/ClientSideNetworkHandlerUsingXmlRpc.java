package org.martus.client;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.xmlrpc.XmlRpcClient;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceXmlRpcConstants;

public class ClientSideNetworkHandlerUsingXmlRpc
	implements NetworkInterfaceConstants, NetworkInterfaceXmlRpcConstants, NetworkInterface
{

	public class SSLSocketSetupException extends Exception {}

	public ClientSideNetworkHandlerUsingXmlRpc(String serverName, int portToUse) throws SSLSocketSetupException
	{
		server = serverName;
		port = portToUse;
		try 
		{
			HttpsURLConnection.setDefaultSSLSocketFactory(createSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new SimpleHostnameVerifier());
		} 
		catch (Exception e) 
		{
			throw new SSLSocketSetupException();
		}
	}

	// begin ServerInterface
	public Vector getServerInfo(Vector reservedForFuture)
	{
		Vector params = new Vector();
		params.add(reservedForFuture);
		return (Vector)callServer(server, cmdGetServerInfo, params);
	}
	
	public Vector getUploadRights(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetUploadRights, params);
	}

	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetSealedBulletinIds, params);
	}
					
	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetDraftBulletinIds, params);
	}
					
	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetFieldOfficeAccountIds, params);
	}

	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdPutBulletinChunk, params);
	}
					
	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetBulletinChunk, params);
	}
					
	public Vector getPacket(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetPacket, params);
	}

	public Vector deleteDraftBulletins(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdDeleteDrafts, params);
	}
	
	public Vector putContactInfo(String myAccountId, Vector parameters, String signature) 
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdPutContactInfo, params);
	}


	public String ping()
	{
		Vector params = new Vector();
		return (String)callServer(server, CMD_PING, params);
	}

	public String requestUploadRights(String clientId, String tryMagicWord)
	{
		logging("ServerInterfaceXmlRpcHandler:requestUploadRights clientId=" + clientId + "tryMagicWord=" + tryMagicWord);
		Vector params = new Vector();
		params.add(clientId);
		params.add(tryMagicWord);
		return (String)callServer(server, CMD_UPLOAD_RIGHTS, params);
	}

	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		logging("ServerInterfaceXmlRpcHandler:uploadBulletinChunk clientId=" + authorAccountId + "bulletinId=" + bulletinLocalId);
		logging("totalSize=" + totalSize + ", chunk Offset=" + chunkOffset + ", chunk Size=" + chunkSize);
		Vector params = new Vector();
		params.add(authorAccountId);
		params.add(bulletinLocalId);
		params.add(new Integer(totalSize));
		params.add(new Integer(chunkOffset));
		params.add(new Integer(chunkSize));
		params.add(data);
		params.add(signature);
		return (String)callServer(server, CMD_UPLOAD_CHUNK, params);
	}	
	
	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature)
	{
		logging("ServerInterfaceXmlRpcHandler:downloadMyBulletinChunk clientId=" + authorAccountId + "bulletinId=" + bulletinLocalId);
		Vector params = new Vector();
		params.add(authorAccountId);
		params.add(bulletinLocalId);
		params.add(new Integer(chunkOffset));
		params.add(new Integer(maxChunkSize));
		params.add(signature);
		return (Vector)callServer(server, CMD_DOWNLOAD_CHUNK, params);
	}

	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature)
	{
		logging("ServerInterfaceXmlRpcHandler:downloadFieldOfficeBulletinChunk authorId=" + authorAccountId + "bulletinId=" + bulletinLocalId);
		Vector params = new Vector();
		params.add(authorAccountId);
		params.add(bulletinLocalId);
		params.add(hqAccountId);
		params.add(new Integer(chunkOffset));
		params.add(new Integer(maxChunkSize));
		params.add(signature);
		return (Vector)callServer(server, CMD_DOWNLOAD_FIELD_OFFICE_CHUNK, params);
	}
	
	public Vector downloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature)
	{
		logging("ServerInterfaceXmlRpcHandler:downloadAuthorizedPacket authorAccountId=" + authorAccountId + "packetLocalId=" + packetLocalId);
		logging("myAccountId=" + myAccountId);
		Vector params = new Vector();
		params.add(authorAccountId);
		params.add(packetLocalId);
		params.add(myAccountId);
		params.add(signature);
		return (Vector)callServer(server, CMD_DOWNLOAD_AUTHORIZED_PACKET, params);
	}

	public Vector listMyBulletinSummaries(String clientId)
	{
		logging("ServerInterfaceXmlRpcHandler:listMyBulletinSummaries clientId=" + clientId);
		Vector params = new Vector();
		params.add(clientId);
		return (Vector)callServer(server, CMD_MY_SUMMARIES, params);
	}

	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		logging("ServerInterfaceXmlRpcHandler:downloadFieldDataPacket authorAccountId=" + authorAccountId + "bulletinLocalId=" + bulletinLocalId);
		logging("packetLocalId=" + packetLocalId + "myAccountId=" + myAccountId);
		Vector params = new Vector();
		params.add(authorAccountId);
		params.add(packetLocalId);
		params.add(myAccountId);
		params.add(signature);
		return (Vector)callServer(server, CMD_DOWNLOAD_FIELD_DATA_PACKET, params);
	}
	
	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		logging("ServerInterfaceXmlRpcHandler:listFieldOfficeBulletinSummaries hqAccountId=" + hqAccountId);
		Vector params = new Vector();
		params.add(hqAccountId);
		params.add(authorAccountId);
		return (Vector)callServer(server, CMD_FIELD_OFFICE_SUMMARIES, params);
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		logging("ServerInterfaceXmlRpcHandler:listFieldOfficeAccounts hqAccountId=" + hqAccountId);
		Vector params = new Vector();
		params.add(hqAccountId);
		return (Vector)callServer(server, CMD_FIELD_OFFICE_ACCOUNTS, params);
	}

	public Object callServer(String serverName, String method, Vector params)
	{
		final String serverUrl = "https://" + serverName + ":" + port + "/RPC2";
		//System.out.println("ServerInterfaceXmlRpcHandler:callServer serverUrl=" + serverUrl);
		Object result = null;
		try
		{
			XmlRpcClient client = new XmlRpcClient(serverUrl);
			result = client.execute("MartusServer." + method, params);
		}
		catch (IOException e)
		{
			//TODO throw IOExceptions so caller can decide what to do.
			//This was added for connection refused: connect (no server connected)
			//System.out.println("ServerInterfaceXmlRpcHandler:callServer Exception=" + e);
			//e.printStackTrace();
		}
		catch (Exception e)
		{
			System.out.println("ServerInterfaceXmlRpcHandler:callServer Exception=" + e);
			e.printStackTrace();
		}
		return result;
	}

	SSLSocketFactory createSocketFactory() throws Exception
	{
		tm = new SimpleX509TrustManager();
		TrustManager []tma = {tm};
		SSLContext sslContext = SSLContext.getInstance( "TLS" );
		SecureRandom secureRandom = new SecureRandom();
		sslContext.init( null, tma, secureRandom);

		return sslContext.getSocketFactory();

	}
	
	public SimpleX509TrustManager getSimpleX509TrustManager() 
	{
		return tm;
	}

	private void logging(String message)
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		System.out.println(stamp + " " + message);
	}

	SimpleX509TrustManager tm;
	String server;
	int port;
}
