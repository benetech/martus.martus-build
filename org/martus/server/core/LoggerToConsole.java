package org.martus.server.core;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LoggerToConsole implements LoggerInterface
{
	public LoggerToConsole()
	{
	}
	
	public void setServerName(String serverNameToUse)
	{
		serverName = serverNameToUse;
	}

	public void log(String message)
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat formatDate = new SimpleDateFormat("EE MM/dd HH:mm:ss z");
		String threadId = getCurrentClientAddress();
		if(threadId == null)
			threadId = "";
		else
			threadId = threadId + ": ";
		String logEntry = formatDate.format(stamp) + " " + threadId + message;
		System.out.println(logEntry);
	}

	protected String getCurrentClientAddress()
	{
		Thread currThread = Thread.currentThread();
		if( XmlRpcThread.class.getName() == currThread.getClass().getName() )
		{
			String ip = ((XmlRpcThread) Thread.currentThread()).getClientAddress();
			return ip;
		}
		return null;
	}

	String getServerName()
	{
		if(serverName == null)
			return "host/address";
		return serverName;
	}

	String serverName;
}
