package org.martus.server.formirroring;

import java.io.IOException;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.martus.common.MartusUtilities;
import org.martus.common.SimpleHostnameVerifier;
import org.martus.common.SimpleX509TrustManager;

public class CallerSideMirroringGatewayForXmlRpc implements MirroringInterface
{
	public static class SSLSocketSetupException extends Exception {}

	public CallerSideMirroringGatewayForXmlRpc(String serverName, int portToUse) throws SSLSocketSetupException
	{
		server = serverName;
		port = portToUse;
		try
		{
			tm = new SimpleX509TrustManager();
			HttpsURLConnection.setDefaultSSLSocketFactory(MartusUtilities.createSocketFactory(tm));
			HttpsURLConnection.setDefaultHostnameVerifier(new SimpleHostnameVerifier());
			final String serverUrl = "https://" + serverName + ":" + port + "/RPC2";
			xmlRpc = new XmlRpcClient(serverUrl);
		}
		catch (Exception e)
		{
			throw new SSLSocketSetupException();
		}
	}
	
	public void setExpectedPublicCode(String expectedPublicCode)
	{
		tm.setExpectedPublicCode(expectedPublicCode);
	}

	public void setExpectedPublicKey(String expectedPublicKey)
	{
		tm.setExpectedPublicKey(expectedPublicKey);
	}

	public Vector request(String callerAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(callerAccountId);
		params.add(parameters);
		params.add(signature);
		try
		{
			return (Vector)callServer("request", params);
		}
		catch (XmlRpcException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	Object callServer(String method, Vector params) throws 
		XmlRpcException, IOException
	{
		return xmlRpc.execute(MirroringInterface.DEST_OBJECT_NAME + "." + method, params);
	}

	XmlRpcClient xmlRpc;
	SimpleX509TrustManager tm;
	String server;
	int port;
}
