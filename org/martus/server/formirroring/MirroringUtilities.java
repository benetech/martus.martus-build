package org.martus.server.formirroring;


public class MirroringUtilities
{

	public static CallerSideMirroringGateway createRealMirroringGateway(String ip, int port, String publicCode) throws CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException
	{
		CallerSideMirroringGatewayForXmlRpc xmlRpcGateway = new CallerSideMirroringGatewayForXmlRpc(ip, port); 
		xmlRpcGateway.setExpectedPublicCode(publicCode);
		return new CallerSideMirroringGateway(xmlRpcGateway);
	}
}
