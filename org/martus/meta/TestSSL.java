package org.martus.meta;

import java.util.Vector;

import org.martus.client.core.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.common.MockMartusSecurity;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.TestCaseEnhanced;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerSideNetworkHandler;



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
			int testPort2 = 1986;
			mockSecurityForServer = MockMartusSecurity.createServer();
			mockServer = new MockMartusServer();
			mockServer.verifyAndLoadConfigurationFiles();
			mockServer.setSecurity(mockSecurityForServer);
			mockServer.createSSLXmlRpcServerOnPort(testport);
			mockServer.createMirroringSupplierXmlRpcServer(testPort2);
			
//			XmlRpc.debug = true;
			proxy1 = new ClientSideNetworkHandlerUsingXmlRpc("localhost", testport);
//			proxy2 = new ClientSideNetworkHandlerUsingXmlRpc("localhost", testport);
		}
	}
	
	public void tearDown() throws Exception
	{
		mockServer.deleteAllFiles();
	}

	
	public void testBasics()
	{
		verifyBadCertBeforeGoodCertHasBeenAccepted();
		verifyGoodCertAndItWillNotBeReverifiedThisSession();

// TODO: After the callerSide of mirroring is available, hook up this test!
//		proxy2.getSimpleX509TrustManager().setExpectedPublicKey(mockSecurityForServer.getPublicKeyString());
//		assertEquals(NetworkInterfaceConstants.VERSION, proxy2.pingForMirroring());
	}
	
	public void verifyBadCertBeforeGoodCertHasBeenAccepted()
	{
		proxy1.getSimpleX509TrustManager().setExpectedPublicCode("Not a valid code");
		assertNull("accepted bad cert?", proxy1.getServerInfo(new Vector()));
	}
	
	public void verifyGoodCertAndItWillNotBeReverifiedThisSession()
	{
		proxy1.getSimpleX509TrustManager().setExpectedPublicKey(mockSecurityForServer.getPublicKeyString());

		assertEquals(NetworkInterfaceConstants.VERSION, proxy1.ping());

		NetworkResponse response = new NetworkResponse(proxy1.getServerInfo(new Vector()));
		assertEquals(NetworkInterfaceConstants.OK, response.getResultCode());
		assertEquals(NetworkInterfaceConstants.VERSION, response.getResultVector().get(0));
	}
	
	static MockMartusSecurity mockSecurityForServer;
	static MockMartusServer mockServer;
	static ServerSideNetworkHandler mockSSLServerInterface;
	static ClientSideNetworkHandlerUsingXmlRpc proxy1;
//	static ClientSideNetworkHandlerUsingXmlRpc proxy2;
}
