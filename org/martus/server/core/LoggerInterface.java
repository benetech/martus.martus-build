package org.martus.server.core;

public interface LoggerInterface
{
	public void setServerName(String serverNameToUse);
	public void log(String message);
}
