package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.NetworkInterfaceConstants;

public interface MirroringInterface
{
	public static final int MARTUS_PORT_FOR_MIRRORING = 986;

	public final static String RESULT_OK = NetworkInterfaceConstants.OK;

	public final static String CMD_PING_FOR_MIRRORING = "pingForMirroring";
	public final static String CMD_LIST_ACCOUNTS_FOR_MIRRORING = "listAccountsForMirroring";
	public final static String CMD_LIST_BULLETINS_FOR_MIRRORING = "listBulletinsForMirroring";
	public final static String CMD_GET_BULLETIN_CHUNK_FOR_MIRRORING = "getBulletinChunkForMirroring";


	public Vector request(String callerAccountId, Vector parameters, String signature);
}
