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

package org.martus.common.bulletin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.Base64;
import org.martus.util.InputStreamWithSeek;

public class AttachmentProxy
{
	public AttachmentProxy(File fileToAttach)
	{
		file = fileToAttach;
		setLabel(file.getName());
	}

	public AttachmentProxy(UniversalId universalIdToUse, String labelToUse, byte[] sessionKeyBytes)
	{
		setUniversalIdAndSessionKey(universalIdToUse, sessionKeyBytes);
		setLabel(labelToUse);
	}

	public AttachmentProxy(String labelToUse)
	{
		setLabel(labelToUse);
	}

	public static AttachmentProxy createFileProxyFromAttachmentPacket(InputStreamWithSeek attachmentIn, AttachmentProxy oldProxy, MartusCrypto verifier)
		throws
			IOException,
			Packet.InvalidPacketException,
			Packet.SignatureVerificationException,
			Packet.WrongPacketTypeException,
			Base64.InvalidBase64Exception
	{
		byte[] sessionKeyBytes = oldProxy.getSessionKeyBytes();
		File tempFile = File.createTempFile("$$$MartusImportAttachment", null);
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		AttachmentPacket.exportRawFileFromXml(attachmentIn, sessionKeyBytes, verifier, out);
		out.close();
		AttachmentProxy ap = new AttachmentProxy(tempFile);
		ap.setLabel(oldProxy.getLabel());
		return ap;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String labelToUse)
	{
		label = labelToUse;
	}

	public File getFile()
	{
		return file;
	}

	public byte[] getSessionKeyBytes()
	{
		return keyBytes;
	}

	public void setUniversalIdAndSessionKey(UniversalId universalId, byte[] sessionKeyString)
	{
		uid = universalId;
		keyBytes = sessionKeyString;
		file = null;
	}

	public UniversalId getUniversalId()
	{
		return uid;
	}

	String label;
	File file;
	byte[] keyBytes;
	UniversalId uid;
}
