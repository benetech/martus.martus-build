package org.martus.server.forclients;

import java.util.Vector;

public interface ServerForNonSSLClientsInterface
{
	public String ping();
	public Vector getServerInformation();

	public String getPublicCode(String clientId);
	public void logging(String message);
	public void clientConnectionStart();
	public void clientConnectionExit();

}
