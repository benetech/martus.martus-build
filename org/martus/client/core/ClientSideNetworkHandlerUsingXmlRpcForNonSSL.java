/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

import java.sql.Timestamp;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.martus.common.*;

public class ClientSideNetworkHandlerUsingXmlRpcForNonSSL implements NetworkInterfaceConstants, NetworkInterfaceXmlRpcConstants, NetworkInterfaceForNonSSL
{
	public ClientSideNetworkHandlerUsingXmlRpcForNonSSL(String serverName, int portToUse)
	{
		server = serverName;
		port = portToUse;
	}

	// begin MartusXmlRpc interface
	public String ping()
	{
		Vector params = new Vector();
		return (String)callServer(server, CMD_PING, params);
	}

	public Vector getServerInformation()
	{
		logging("MartusServerProxyViaXmlRpc:getServerInformation");
		Vector params = new Vector();
		return (Vector)callServer(server, CMD_SERVER_INFO, params);
	}
	
	public String requestUploadRights(String clientId, String tryMagicWord)
	{
		logging("MartusServerProxyViaXmlRpc:requestUploadRights clientId=" + clientId + "tryMagicWord=" + tryMagicWord);
		Vector params = new Vector();
		params.add(clientId);
		params.add(tryMagicWord);
		return (String)callServer(server, CMD_UPLOAD_RIGHTS, params);
	}
	
	public String uploadBulletin(String clientId, String bulletinId, String text)
	{
		logging("MartusServerProxyViaXmlRpc:uploadBulletin clientId=" + clientId + "bulletinId=" + bulletinId);
		Vector params = new Vector();
		params.add(clientId);
		params.add(bulletinId);
		params.add(text);
		return (String)callServer(server, CMD_UPLOAD, params);
	}
	
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		logging("MartusServerProxyViaXmlRpc:uploadBulletinChunk clientId=" + authorAccountId + "bulletinId=" + bulletinLocalId);
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


	public Vector downloadBulletin(String authorAccountId, String bulletinLocalId)
	{
		logging("MartusServerProxyViaXmlRpc:downloadBulletin clientId=" + authorAccountId + "bulletinId=" + bulletinLocalId);
		Vector params = new Vector();
		params.add(authorAccountId);
		params.add(bulletinLocalId);
		return (Vector)callServer(server, CMD_DOWNLOAD, params);
	}

	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature)
	{
		logging("MartusServerProxyViaXmlRpc:downloadBulletinChunk clientId=" + authorAccountId + "bulletinId=" + bulletinLocalId);
		Vector params = new Vector();
		params.add(authorAccountId);
		params.add(bulletinLocalId);
		params.add(new Integer(chunkOffset));
		params.add(new Integer(maxChunkSize));
		params.add(signature);
		return (Vector)callServer(server, CMD_DOWNLOAD_CHUNK, params);
	}

	public Vector listMyBulletinSummaries(String clientId)
	{
		logging("MartusServerProxyViaXmlRpc:listMyBulletinSummaries clientId=" + clientId);
		Vector params = new Vector();
		params.add(clientId);
		return (Vector)callServer(server, CMD_MY_SUMMARIES, params);
	}

	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId)
	{
		logging("MartusServerProxyViaXmlRpc:listFieldOfficeBulletinSummaries hqAccountId=" + hqAccountId);
		Vector params = new Vector();
		params.add(hqAccountId);
		params.add(authorAccountId);
		return (Vector)callServer(server, CMD_FIELD_OFFICE_SUMMARIES, params);
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		logging("MartusServerProxyViaXmlRpc:listFieldOfficeAccounts hqAccountId=" + hqAccountId);
		Vector params = new Vector();
		params.add(hqAccountId);
		return (Vector)callServer(server, CMD_FIELD_OFFICE_ACCOUNTS, params);
	}

	public Vector downloadPacket(String clientId, String packetId)
	{
		logging("MartusServerProxyViaXmlRpc:downloadPacket clientId=" + clientId);
		logging("  packetId=" + packetId);
		Vector params = new Vector();
		params.add(clientId);
		params.add(packetId);
		return (Vector)callServer(server, CMD_DOWNLOAD_PACKET, params);
	}
	
	public String authenticateServer(String tokenToSign)
	{
		logging("MartusServerProxyViaXmlRpc:authenticateServer");
		Vector params = new Vector();
		params.add(tokenToSign);
		return (String)callServer(server, CMD_AUTHENTICATE_SERVER, params);
	}
	
	// end MartusXmlRpc interface

	public Object callServer(String serverName, String method, Vector params)
	{
		final String serverUrl = "http://" + serverName + ":" + port + "/RPC2";
		logging("MartusServerProxyViaXmlRpc:callServer serverUrl=" + serverUrl);
		Object result = null;
		try
		{
			XmlRpcClient client = new XmlRpcClientLite(serverUrl);
			result = client.execute("MartusServer." + method, params);
		}
		catch (Exception e)
		{
			logging("MartusServerProxyViaXmlRpc:callServer Exception=" + e);
			e.printStackTrace();
		}

		return result;
	}

	private void logging(String message)
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		System.out.println(stamp + " " + message);
	}

	String server;
	int port;
	boolean debugMode;
}
