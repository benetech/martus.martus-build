package org.martus.client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class SimpleHostnameVerifier implements HostnameVerifier 
{

	public SimpleHostnameVerifier() 
	{
		super();
	}

	public boolean verify(String hostName, SSLSession session) 
	{
		//This is called if the certificate CN doesn't match the URL
		//Our security relies on public keys, not IP addresses.
		return true;
	}
}
