package org.martus.server;

import java.util.Vector;

public interface MirroringInterface
{
	public final static String CMD_PING = "pingForMirroring";
	public final static String CMD_LIST_ACCOUNTS_FOR_MIRRORING = "listAccountsForMirroring";
	public final static String CMD_LIST_BULLETINS_FOR_MIRRORING = "listBulletinsForMirroring";
	public final static String CMD_GET_BULLETIN_CHUNK_FOR_MIRRORING = "getBulletinChunkForMirroring";

	public static final String OK = "ok";
	public static final String SIG_ERROR = "signature error";
	public static final String BAD_PARAMETER = "bad parameter";
	public static final String UNKNOWN_COMMAND = "unknown command";
	public static final String NOT_AUTHORIZED = "not authorized";

	public Vector request(String callerAccountId, Vector parameters, String signature);
}
