package org.martus.server.core;

import java.net.Socket;

public class XmlRpcThread extends Thread
{
	public XmlRpcThread(ThreadGroup group, Runnable runnable, Socket socketToUse)
	{
		super(group, runnable);
	    socket = socketToUse;
	}
	      
	public String getClientAddress()
	{
		return getClientIp() + ":" + getClientPort();
	}
	
	public String getClientIp()
	{
		String ip = socket.getInetAddress().getHostAddress();
		return ip;
	}
	
	public int getClientPort()
	{
	    int port = socket.getPort();
		return port;
	}
	  
	Socket socket;
}
