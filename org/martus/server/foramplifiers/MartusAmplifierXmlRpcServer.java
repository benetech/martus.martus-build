package org.martus.server.foramplifiers;

import org.martus.server.core.MartusSecureWebServer;

public class MartusAmplifierXmlRpcServer
{
	public static void createSSLXmlRpcServer(Object server, int port)
	{
		try
		{
			MartusSecureWebServer secureWebServer = new MartusSecureWebServer(port);
			secureWebServer.addHandler("MartusAmplifierServer", server);
		}
		catch (Exception e)
		{
			System.err.println("MartusAmplifierXmlRpcServer " + port + ": " + e);
			System.err.println( e.getMessage() );
			e.printStackTrace();
		}
	}
}
