package org.martus.meta;

import org.martus.client.*;
import org.martus.common.*;
import org.martus.server.*;



public class TestSSL extends TestCaseEnhanced 
{
	public TestSSL(String name) 
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		if(mockSecurityForServer == null)
		{
			int testport = 1987;
			mockSecurityForServer = new MockMartusSecurity();
			mockServer = new MockMartusServer();
			mockServer.setSecurity(mockSecurityForServer);
			mockServer.createSSLXmlRpcServerOnPort(testport);
			
//			XmlRpc.debug = true;
			proxy = new ClientSideNetworkHandlerUsingXmlRpc("localhost", testport);
		}
	}
	
	
	public void testBasics()
	{
		proxy.getSimpleX509TrustManager().setExpectedPublicKey(mockSecurityForServer.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.VERSION, proxy.ping());
	}
	
	static MockMartusSecurity mockSecurityForServer;
	static MockMartusServer mockServer;
	static ServerSideNetworkHandler mockSSLServerInterface;
	static ClientSideNetworkHandlerUsingXmlRpc proxy;
}
