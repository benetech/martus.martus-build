package org.martus.server.forclients;

import java.util.Vector;

import org.martus.common.NetworkInterfaceForNonSSL;


public class ServerSideNetworkHandlerForNonSSL implements NetworkInterfaceForNonSSL
{

	public ServerSideNetworkHandlerForNonSSL(ServerForNonSSLClientsInterface serverToUse)
	{
		server = serverToUse;
	}

	public String ping()
	{
		server.clientConnectionStart();
		String strResponse = server.ping();
		server.clientConnectionExit();
		return strResponse;
	}

	public Vector getServerInformation()
	{
		server.clientConnectionStart();
		Vector vecResponse = server.getServerInformation();
		server.clientConnectionExit();
		return vecResponse;
	}

	ServerForNonSSLClientsInterface server;
}