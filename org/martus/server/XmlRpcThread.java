package org.martus.server;

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
		String ip = socket.getInetAddress().getHostAddress();
	    int port = socket.getPort();
		return ip + ":" + port;
	}
	  
	Socket socket;
}
