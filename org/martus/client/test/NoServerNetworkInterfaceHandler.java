package org.martus.client.test;

import java.util.Vector;

import org.martus.common.NetworkInterface;

public class NoServerNetworkInterfaceHandler implements NetworkInterface
{

	public Vector getServerInfo(Vector reservedForFuture)
	{
		return null;
	}

	public Vector getUploadRights(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector getSealedBulletinIds(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector getDraftBulletinIds(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector getFieldOfficeAccountIds(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector putBulletinChunk(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector getBulletinChunk(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector getPacket(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector deleteDraftBulletins(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector putContactInfo(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector getNews(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public String ping()
	{
		return null;
	}

	public String requestUploadRights(
		String authorAccountId,
		String tryMagicWord)
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

	public Vector downloadMyBulletinChunk(
		String authorAccountId,
		String bulletinLocalId,
		int chunkOffset,
		int maxChunkSize,
		String signature)
	{
		return null;
	}

	public Vector downloadFieldOfficeBulletinChunk(
		String authorAccountId,
		String bulletinLocalId,
		String hqAccountId,
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

	public Vector downloadFieldDataPacket(
		String authorAccountId,
		String bulletinLocalId,
		String packetLocalId,
		String myAccountId,
		String signature)
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

	public Vector downloadAuthorizedPacket(
		String authorAccountId,
		String packetLocalId,
		String myAccountId,
		String signature)
	{
		return null;
	}

}
