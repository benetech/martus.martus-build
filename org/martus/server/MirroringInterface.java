package org.martus.server;

import java.util.Vector;

public interface MirroringInterface
{
	public final static String CMD_PING = "ping";
	public final static String CMD_GET_ACCOUNTS = "getAccounts";

	public static final String OK = "ok";
	public static final String SIG_ERROR = "signature error";
	public static final String UNKNOWN_COMMAND = "unknown command";

	public Vector request(String callerAccountId, Vector parameters, String signature);
}
