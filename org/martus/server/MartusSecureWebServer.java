package org.martus.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.xmlrpc.WebServer;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.X509V1CertificateGenerator;
import org.martus.common.*;

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
