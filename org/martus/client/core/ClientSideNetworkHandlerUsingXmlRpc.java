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

package org.martus.client.core;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceXmlRpcConstants;
import org.martus.common.SimpleHostnameVerifier;
import org.martus.common.SimpleX509TrustManager;

public class ClientSideNetworkHandlerUsingXmlRpc
	implements NetworkInterfaceConstants, NetworkInterfaceXmlRpcConstants, NetworkInterface
{

	static class SSLSocketSetupException extends Exception {}

	public ClientSideNetworkHandlerUsingXmlRpc(String serverName, int portToUse) throws SSLSocketSetupException
	{
		server = serverName;
		port = portToUse;
		try
		{
			tm = new SimpleX509TrustManager();
			HttpsURLConnection.setDefaultSSLSocketFactory(MartusUtilities.createSocketFactory(tm));
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

	public Vector getNews(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetNews, params);
	}
	
	public Vector getServerCompliance(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(server, cmdGetServerCompliance, params);
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
		catch (XmlRpcException e)
		{
			if(e.getMessage().indexOf("NoSuchMethodException") < 0)
			{
				System.out.println("ServerInterfaceXmlRpcHandler:callServer XmlRpcException=" + e);
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			System.out.println("ServerInterfaceXmlRpcHandler:callServer Exception=" + e);
			e.printStackTrace();
		}
		return result;
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
