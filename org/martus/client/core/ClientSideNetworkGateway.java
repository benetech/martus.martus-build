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

package org.martus.client.core;

import java.util.Vector;

import org.martus.client.swingui.UiConstants;
import org.martus.common.BulletinRetrieverGatewayInterface;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterface;
import org.martus.common.NetworkResponse;

public class ClientSideNetworkGateway implements BulletinRetrieverGatewayInterface
{
	public ClientSideNetworkGateway(NetworkInterface serverToUse)
	{
		server = serverToUse;
	}

	public NetworkResponse getServerInfo()
	{
		Vector parameters = new Vector();
		return new NetworkResponse(server.getServerInfo(parameters));
	}

	public NetworkResponse getUploadRights(MartusCrypto signer, String tryMagicWord) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(tryMagicWord);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getUploadRights(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getSealedBulletinIds(MartusCrypto signer, String authorAccountId, Vector retrieveTags) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(retrieveTags);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getSealedBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getDraftBulletinIds(MartusCrypto signer, String authorAccountId, Vector retrieveTags) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(retrieveTags);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getDraftBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getFieldOfficeAccountIds(MartusCrypto signer, String hqAccountId) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(hqAccountId);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getFieldOfficeAccountIds(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse putBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId,
			int totalSize, int chunkOffset, int chunkSize, String data) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(totalSize));
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(chunkSize));
		parameters.add(data);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.putBulletinChunk(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId,
					int chunkOffset, int maxChunkSize) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getBulletinChunk(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getPacket(MartusCrypto signer, String authorAccountId, String bulletinLocalId,
					String packetLocalId) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(packetLocalId);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getPacket(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse deleteServerDraftBulletins(MartusCrypto signer,
					String authorAccountId, String[] bulletinLocalIds) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(new Integer(bulletinLocalIds.length));
		for (int i = 0; i < bulletinLocalIds.length; i++)
		{
			parameters.add(bulletinLocalIds[i]);
		}
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.deleteDraftBulletins(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse	putContactInfo(MartusCrypto signer, String authorAccountId, Vector parameters) throws
			MartusCrypto.MartusSignatureException
	{
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.putContactInfo(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse	getNews(MartusCrypto signer) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(UiConstants.versionLabel);
		parameters.add(MartusUtilities.getVersionDate());
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getNews(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse	getServerCompliance(MartusCrypto signer) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getServerCompliance(signer.getPublicKeyString(), parameters, signature));
	}
	
	final static String defaultReservedString = "";

	NetworkInterface server;
}
