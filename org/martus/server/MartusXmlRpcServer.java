package org.martus.server;

public class MartusXmlRpcServer
{
	public static void createNonSSLXmlRpcServer(Object server, int port)
	{
		try
		{
			WebServerWithClientId webServer = new WebServerWithClientId(port);
			webServer.addHandler("MartusServer", server);
		}
		catch (Exception e)
		{
			System.err.println("createNonSSLXmlRpcServer " + port + ": " + e.toString());
			e.printStackTrace();
		}
	}
	
	public static void createSSLXmlRpcServer(Object server, int port)
	{
		try
		{
			MartusSecureWebServer secureWebServer = new MartusSecureWebServer(port);
			secureWebServer.addHandler("MartusServer", server);
		}
		catch (Exception e)
		{
			System.err.println("createSSLXmlRpcServer " + port + ": " + e);
			System.err.println( e.getMessage() );
			e.printStackTrace();
		}
	}
}
