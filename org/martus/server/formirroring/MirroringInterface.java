package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.NetworkInterfaceConstants;

public interface MirroringInterface
{
	public static final int MARTUS_PORT_FOR_MIRRORING = 986;

	public final static String RESULT_OK = NetworkInterfaceConstants.OK;

	public final static String DEST_OBJECT_NAME = "MartusMirror";
	public final static String CMD_MIRRORING_PING = "mirroringPing";
	public final static String CMD_MIRRORING_LIST_ACCOUNTS = "mirroringListAccounts";
	public final static String CMD_MIRRORING_LIST_SEALED_BULLETINS = "mirroringListSealedBulletins";
	public final static String CMD_MIRRORING_GET_BULLETIN_UPLOAD_RECORD = "mirroringGetBulletinUploadRecord";
	public final static String CMD_MIRRORING_GET_BULLETIN_CHUNK = "mirrorintGetBulletinChunk";


	public Vector request(String callerAccountId, Vector parameters, String signature);
}
