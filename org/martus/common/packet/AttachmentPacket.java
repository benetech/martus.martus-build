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

package org.martus.common.packet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.martus.common.Base64XmlOutputStream;
import org.martus.common.MartusXml;
import org.martus.common.XmlWriterFilter;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.util.Base64;
import org.martus.util.FileInputStreamWithSeek;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AttachmentPacket extends Packet
{
	public AttachmentPacket(String account, byte[] sessionKeyBytes, File fileToAttach, MartusCrypto crypto) throws
		IOException

	{
		super(createUniversalId(account));
		sessionKey = sessionKeyBytes;
		rawFile = fileToAttach;
		security = crypto;
	}

	private AttachmentPacket(MartusCrypto crypto)
	{
		super(UniversalId.createDummyUniversalId());
		security = crypto;
	}

	public static UniversalId createUniversalId(String accountId)
	{
		return UniversalId.createFromAccountAndPrefix(accountId, prefix);
	}

	public static boolean isValidLocalId(String localId)
	{
		return localId.startsWith(prefix);
	}

	public byte[] writeXmlToClientDatabase(Database db, boolean mustEncrypt, MartusCrypto signer) throws
			IOException,
			MartusCrypto.CryptoException
	{
		File temp = File.createTempFile("$$$MartusAttachment", null);
		temp.deleteOnExit();
		
		byte[] sig = writeAttachmentXmlFile(signer, temp);

		DatabaseKey headerKey = new DatabaseKey(getUniversalId());
		HashMap importMap = new HashMap();
		importMap.put(headerKey, temp);
		try
		{
			db.importFiles(importMap);
		}
		catch (RecordHiddenException e)
		{
			e.printStackTrace();
			throw new IOException(e.toString());
		}
		temp.delete();
		return sig;
	}

	public byte[] writeAttachmentXmlFile(MartusCrypto signer, File temp)
		throws IOException
	{
		UnicodeWriter writer = new UnicodeWriter(temp);
		byte[] sig = writeXml(writer, signer);
		writer.close();
		return sig;
	}

	public static void exportRawFileFromXml(InputStreamWithSeek xmlIn, byte[] sessionKeyBytes, MartusCrypto verifier, OutputStream out) throws
		IOException,
		org.martus.common.packet.Packet.InvalidPacketException,
		org.martus.common.packet.Packet.SignatureVerificationException,
		org.martus.common.packet.Packet.WrongPacketTypeException,
		Base64.InvalidBase64Exception
	{
		AttachmentPacket ap = new AttachmentPacket(verifier);

		File base64File = File.createTempFile("$$$MartusAttachExtract", null);
		base64File.deleteOnExit();

		FileOutputStream base64Out = new FileOutputStream(base64File);
		AttachmentExtractionHandler handler = new AttachmentExtractionHandler(base64Out);
		ap.loadFromXml(xmlIn, null, verifier, handler);
		base64Out.close();

		UnicodeReader base64Reader = new UnicodeReader(base64File);

		File encryptedFile = File.createTempFile("$$$MartusEncryptedAtt", null);
		encryptedFile.deleteOnExit();

		FileOutputStream outEncrypted = new FileOutputStream(encryptedFile);
		Base64.decode(base64Reader, outEncrypted);
		outEncrypted.close();
		base64Reader.close();
		base64File.delete();

		InputStreamWithSeek inEncrypted = new FileInputStreamWithSeek(encryptedFile);
		try
		{
			verifier.decrypt(inEncrypted, out, sessionKeyBytes);
		}
		catch(Exception e)
		{
			throw new IOException(e.toString());
		}
		inEncrypted.close();
		encryptedFile.delete();
	}

	protected String getPacketRootElementName()
	{
		return MartusXml.AttachmentPacketElementName;
	}

	protected void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		super.internalWriteXml(dest);

		dest.writeStartTag(MartusXml.AttachmentBytesElementName);

		InputStream inRaw = new BufferedInputStream(new FileInputStream(rawFile));
		OutputStream outXml = new Base64XmlOutputStream(dest);
		try
		{
			security.encrypt(inRaw, outXml, sessionKey);
		}
		catch (Exception e)
		{
			throw new IOException(e.toString());
		}
		outXml.close();
		inRaw.close();

		dest.writeEndTag(MartusXml.AttachmentBytesElementName);
	}

	static final String NEWLINE = "\n";
	byte[] sessionKey;
	File rawFile;
	MartusCrypto security;
	private static final String prefix = "A-";

}

class AttachmentExtractionHandler extends DefaultHandler
{
	public AttachmentExtractionHandler(OutputStream dest) throws FileNotFoundException
	{
		out = dest;
	}

	public void startElement(String namespaceURI, String sName, String qName,
			Attributes attrs) throws SAXException
	{
		if(qName.equals(MartusXml.AttachmentBytesElementName))
		{
			writing = true;
		}

	}

	public void endElement(String namespaceURI, String sName, String qName) throws SAXException
	{
		if(qName.equals(MartusXml.AttachmentBytesElementName))
		{
			writing = false;
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException
	{
		if(!writing)
			return;
		char[] bufWithoutNewlines = new char[buf.length];
		int dest =0;
		char curChar;
		for(int i = offset; i < offset + len; ++i)
		{
			curChar = buf[i];
			if(curChar >= ' ')
				bufWithoutNewlines[dest++] = curChar;
		}

		String data = new String(bufWithoutNewlines, 0, dest);
		byte[] bytes = data.getBytes();
		try
		{
			out.write(bytes);
		}
		catch(IOException e)
		{
			throw new SAXException("IO Exception: " + e.getMessage());
		}
	}
	OutputStream out;
	boolean writing;
}
