package org.martus.server;

import java.util.Vector;

public interface MirroringInterface
{
	public static final int MARTUS_PORT_FOR_MIRRORING = 986;

	public final static String CMD_PING = "pingForMirroring";
	public final static String CMD_LIST_ACCOUNTS_FOR_MIRRORING = "listAccountsForMirroring";
	public final static String CMD_LIST_BULLETINS_FOR_MIRRORING = "listBulletinsForMirroring";
	public final static String CMD_GET_BULLETIN_CHUNK_FOR_MIRRORING = "getBulletinChunkForMirroring";


	public Vector request(String callerAccountId, Vector parameters, String signature);
}
