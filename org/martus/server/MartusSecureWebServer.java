package org.martus.server;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.xmlrpc.WebServer;
import org.martus.common.MartusCrypto;

public class MartusSecureWebServer extends WebServer
{
	public MartusSecureWebServer(int port) throws IOException
	{
		super(port, null);
	}
	
	public ServerSocket createServerSocket(int port, int backlog, java.net.InetAddress add)
			throws Exception
	{
		try
	    {
			SSLContext sslContext = createSSLContext();
			SSLServerSocketFactory sf = sslContext.getServerSocketFactory();

	    	ServerSocket ss = sf.createServerSocket( port );
	    	return ss;
	    }
	    catch(Exception e)
	    {
	    	System.out.println("createServerSocket: " + e);
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	SSLContext createSSLContext() throws Exception
	{
		SSLContext sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( security.createKeyManagers(), null, null );
		return sslContext;
	}
	
	
	public static MartusCrypto security;
}
