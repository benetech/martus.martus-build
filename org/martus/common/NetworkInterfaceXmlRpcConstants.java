package org.martus.common;
public interface NetworkInterfaceXmlRpcConstants
{
	public static final int MARTUS_PORT_FOR_NON_SSL = 988;
	public static final int MARTUS_PORT_FOR_SSL = 987;

	public static final String cmdGetServerInfo = "getServerInfo";
	public static final String cmdGetUploadRights = "getUploadRights";
	public static final String cmdGetSealedBulletinIds = "getSealedBulletinIds";
	public static final String cmdGetDraftBulletinIds = "getDraftBulletinIds";
	public static final String cmdGetFieldOfficeAccountIds = "getFieldOfficeAccountIds";
	public static final String cmdPutBulletinChunk = "putBulletinChunk";
	public static final String cmdGetBulletinChunk = "getBulletinChunk";
	public static final String cmdGetPacket = "getPacket";

	// legacy!
	public static final String CMD_PING = "ping";
	public static final String CMD_UPLOAD_RIGHTS = "requestUploadRights";
	public static final String CMD_UPLOAD_CHUNK = "uploadBulletinChunk";
	public static final String CMD_DOWNLOAD_CHUNK = "downloadMyBulletinChunk";
	public static final String CMD_DOWNLOAD_FIELD_OFFICE_CHUNK = "downloadFieldOfficeBulletinChunk";
	public static final String CMD_MY_SUMMARIES = "listMyBulletinSummaries";
	public static final String CMD_FIELD_OFFICE_SUMMARIES = "listFieldOfficeBulletinSummaries";
	public static final String CMD_FIELD_OFFICE_ACCOUNTS = "listFieldOfficeAccounts";
	public static final String CMD_DOWNLOAD_FIELD_DATA_PACKET = "downloadFieldDataPacket";

	// legacy!
	public static final String CMD_SERVER_INFO = "getServerInformation";
	public static final String CMD_AUTHENTICATE_SERVER = "authenticateServer";
	public static final String CMD_UPLOAD = "uploadBulletin";
	public static final String CMD_DOWNLOAD = "downloadBulletin";
	public static final String CMD_DOWNLOAD_PACKET = "downloadPacket";
	public static final String CMD_DOWNLOAD_AUTHORIZED_PACKET = "downloadAuthorizedPacket";

}
