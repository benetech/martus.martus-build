package org.martus.meta;

import org.martus.client.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.TestCaseEnhanced;
import org.martus.server.MockMartusServer;
import org.martus.server.ServerSideNetworkHandler;



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
	
	public void tearDown() throws Exception
	{
		mockServer.deleteAllFiles();
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
