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
		serverUrl = "https://" + serverName + ":" + port + "/RPC2";
	}
	
	public void setExpectedPublicCode(String newExpectedPublicCode)
	{
		expectedPublicCode = newExpectedPublicCode; 
	}

	public void setExpectedPublicKey(String newExpectedPublicKey)
	{
		expectedPublicKey = newExpectedPublicKey; 
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
			//e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (SSLSocketSetupException e)
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}
	
	Object callServer(String method, Vector params) throws 
		XmlRpcException, IOException, SSLSocketSetupException
	{
		try
		{
			SimpleX509TrustManager tm = new SimpleX509TrustManager();
			if(expectedPublicKey != null)
				tm.setExpectedPublicKey(expectedPublicKey);
			else if(expectedPublicCode != null)
				tm.setExpectedPublicCode(expectedPublicCode);
			HttpsURLConnection.setDefaultSSLSocketFactory(MartusUtilities.createSocketFactory(tm));
			HttpsURLConnection.setDefaultHostnameVerifier(new SimpleHostnameVerifier());
		}
		catch (Exception e)
		{
			throw new SSLSocketSetupException();
		}
		// NOTE: We **MUST** create a new XmlRpcClient for each call, because
		// there is a memory leak in apache xmlrpc 1.1 that will cause out of 
		// memory exceptions if we reuse an XmlRpcClient object
		XmlRpcClient xmlRpc = new XmlRpcClient(serverUrl);
		return xmlRpc.execute(MirroringInterface.DEST_OBJECT_NAME + "." + method, params);
	}

	String serverUrl;
	String server;
	int port;
	String expectedPublicKey;
	String expectedPublicCode;
}
