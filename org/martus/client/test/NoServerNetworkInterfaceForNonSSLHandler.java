package org.martus.client.test;

import java.util.Vector;

import org.martus.common.NetworkInterfaceForNonSSL;

public class NoServerNetworkInterfaceForNonSSLHandler
	implements NetworkInterfaceForNonSSL
{

	public String ping()
	{
		return null;
	}

	public Vector getServerInformation()
	{
		return null;
	}

	public String requestUploadRights(
		String authorAccountId,
		String tryMagicWord)
	{
		return null;
	}

	public String uploadBulletin(
		String authorAccountId,
		String bulletinLocalId,
		String data)
	{
		return null;
	}

	public String uploadBulletinChunk(
		String authorAccountId,
		String bulletinLocalId,
		int totalSize,
		int chunkOffset,
		int chunkSize,
		String data,
		String signature)
	{
		return null;
	}

	public Vector downloadBulletin(
		String authorAccountId,
		String bulletinLocalId)
	{
		return null;
	}

	public Vector downloadMyBulletinChunk(
		String authorAccountId,
		String bulletinLocalId,
		int chunkOffset,
		int maxChunkSize,
		String signature)
	{
		return null;
	}

	public Vector listMyBulletinSummaries(String authorAccountId)
	{
		return null;
	}

	public Vector listFieldOfficeBulletinSummaries(
		String hqAccountId,
		String authorAccountId)
	{
		return null;
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		return null;
	}

	public Vector downloadPacket(String authorAccountId, String packetLocalId)
	{
		return null;
	}

	public String authenticateServer(String tokenToSign)
	{
		return null;
	}

}
