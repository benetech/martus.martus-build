package org.martus.common;

import java.util.Vector;

public interface NetworkInterfaceConstants
{

	public static final String OK = "ok";
	public static final String CHUNK_OK = "chunk ok";
	public static final String NOT_FOUND = "not found";
	public static final String REJECTED = "account rejected";
	public static final String NOTYOURBULLETIN = "not your bulletin";
	public static final String DUPLICATE = "duplicate";
	public static final String SEALED_EXISTS = "sealed bulletin exists";
	public static final String INVALID_DATA = "invalid data";
	public static final String NO_SERVER = "no server";
	public static final String SERVER_DOWN = "server is down";
	public static final String SERVER_ERROR = "server error";
	public static final String SIG_ERROR = "signature error";
	public static final String INCOMPLETE = "incomplete result";
	public static final String VERSION = "MartusServer v0.20";
	public static final String TAG_BULLETIN_SIZE = "bulletin size";
	
	public static final int MAX_CHUNK_SIZE = 100 * 1024;
}
