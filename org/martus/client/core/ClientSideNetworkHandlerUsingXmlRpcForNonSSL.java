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
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.apache.xmlrpc.XmlRpcException;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkInterfaceForNonSSL;
import org.martus.common.NetworkInterfaceXmlRpcConstants;

public class ClientSideNetworkHandlerUsingXmlRpcForNonSSL implements NetworkInterfaceConstants, NetworkInterfaceXmlRpcConstants, NetworkInterfaceForNonSSL
{
	public ClientSideNetworkHandlerUsingXmlRpcForNonSSL(String serverName)
	{
		server = serverName;
		ports = NetworkInterfaceXmlRpcConstants.defaultNonSSLPorts;
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

	// end MartusXmlRpc interface

	public Object callServer(String serverName, String method, Vector params)
	{
		for(int i=0; i < ports.length; ++i)
		{
			int port = ports[i];
			try
			{
				return callServerAtPort(serverName, method, params, port);
			}
			catch(ConnectException e)
			{
				continue;
			}
			catch(Exception e)
			{
				logging("MartusServerProxyViaXmlRpc:callServer Exception=" + e);
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	private Object callServerAtPort(String serverName, String method, Vector params, int port)
		throws MalformedURLException, XmlRpcException, IOException
	{
		final String serverUrl = "http://" + serverName + ":" + port + "/RPC2";
		logging("MartusServerProxyViaXmlRpc:callServer serverUrl=" + serverUrl);
		XmlRpcClient client = new XmlRpcClientLite(serverUrl);
		return client.execute("MartusServer." + method, params);
	}

	private void logging(String message)
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		System.out.println(stamp + " " + message);
	}

	String server;
	int[] ports;
	boolean debugMode;
}
