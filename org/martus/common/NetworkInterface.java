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

package org.martus.common;

import java.util.Vector;

public interface NetworkInterface
{
	public Vector getServerInfo(Vector reservedForFuture);
	public Vector getUploadRights(String myAccountId, Vector parameters, String signature);
	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature);
	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature);
	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature);
	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature);
	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature);
	public Vector getPacket(String myAccountId, Vector parameters, String signature);
	public Vector deleteDraftBulletins(String myAccountId, Vector parameters, String signature);
	public Vector putContactInfo(String myAccountId, Vector parameters, String signature);
	public Vector getNews(String myAccountId, Vector parameters, String signature);
	public Vector getServerCompliance(String myAccountId, Vector parameters, String signature);
	
	// TODO: Delete everything below this line after all clients have been upgraded to 2002-08-28 or later
	public Vector downloadMyBulletinChunk(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize, String signature);
	public Vector downloadFieldOfficeBulletinChunk(String authorAccountId, String bulletinLocalId, String hqAccountId, int chunkOffset, int maxChunkSize, String signature);
	public Vector listMyBulletinSummaries(String authorAccountId);
	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature);
	public Vector listFieldOfficeBulletinSummaries(String hqAccountId, String authorAccountId);
	public Vector listFieldOfficeAccounts(String hqAccountId);

	// TODO: Delete this after all clients have been upgraded to 2002-08-24 or later
	public Vector downloadAuthorizedPacket(String authorAccountId, String packetLocalId, String myAccountId, String signature);
}
