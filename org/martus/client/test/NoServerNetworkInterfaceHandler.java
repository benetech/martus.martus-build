/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

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

	public Vector getServerCompliance(String myAccountId, Vector parameters, String signature)
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
