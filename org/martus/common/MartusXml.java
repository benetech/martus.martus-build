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

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class MartusXml
{
	public static String getFieldTagStart(String name)	{ return getTagStart(tagField, attrField, name); }
	public static String getFieldTagEnd()				{ return getTagEnd(tagField); }

	public static String getIdTag(String id)			{ return "<Id>" + id + "</Id>\n"; }

	public static String getAttachmentTagStart(String name)
	{
		return getTagStart(tagAttachment, attrAttachmentName, name);
	}

	public static String getAttachmentTagEnd()
	{
		return getTagEnd(tagAttachment);
	}



	public static String getTagStart(String tagName)
	{
		return "<" + tagName + ">";
	}

	public static String getTagStart(String tagName, String attrName, String attrValue)
	{
		return "<" + tagName + " " + attrName + "='" + attrValue + "'>";
	}

	public static String getTagStart(String tagName, String attr1Name, String attr1Value, String attr2Name, String attr2Value)
	{
		return "<" + tagName + " " + attr1Name + "='" + attr1Value +
				"' " + attr2Name + "='" + attr2Value + "'>";
	}

	public static String getTagEnd(String tagName)
	{
		return "</" + tagName + ">\n";
	}

	static public String loadXml(Reader xmlReader, DefaultHandler handler)
	{
		String error = null;
        try
        {
        	loadXmlWithExceptions(xmlReader, handler);
		}
		catch(SAXParseException e)
		{
			error = e.getMessage() + ", " + e.getLineNumber() + ":" + e.getColumnNumber();
			System.out.println("SAX Parse Exception: " + error);
			e.printStackTrace();
		}
		catch(SAXException e)
		{
			error = e.toString();
			System.out.println("SAX Exception: " + error);
			e.printStackTrace();
        }
        catch (Throwable t)
        {
			error = "Unknown throwable: " + t.toString();
            t.printStackTrace();
        }

        return error;
	}

	static public void loadXmlWithExceptions(Reader xmlReader, DefaultHandler handler) throws
			ParserConfigurationException,
			SAXParseException,
			SAXException,
			IOException
	{
        // Parse the input
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(new InputSource(xmlReader), handler);
	}

	// NOTE: Change the version any time the packet format changes in 
	// a "substantial" way. It must start with ;
	public final static String packetFormatVersion = ";1000";
	
	public final static String packetStartCommentStart = "<!--MartusPacket;";
	public final static String packetStartCommentSigLen = "siglen=";
	public final static String packetStartCommentEnd = ";-->";

	public final static String tagField = "Field";
	public final static String attrField = "name";
	public final static String tagAttachment = "Attachment";
	public final static String attrAttachmentName = "name";

	public final static String newLine = "\n";
	public final static String packetSignatureStart = "<!--sig=";
	public final static String packetSignatureEnd = "-->";

	public final static String PacketElementName = "Packet";
	public final static String FieldListElementName = "FieldList";
	public final static String FieldDataPacketElementName = "FieldDataPacket";
	public final static String AttachmentPacketElementName = "AttachmentPacket";
	public final static String BulletinHeaderPacketElementName = "BulletinHeaderPacket";
	public final static String BulletinStatusElementName = "BulletinStatus";
	public final static String LastSavedTimeElementName = "LastSavedTime";
	public final static String AllPrivateElementName = "AllPrivate";
	public final static String PacketIdElementName = "PacketId";
	public final static String PublicAttachmentIdElementName = "AttachmentId";
	public final static String PrivateAttachmentIdElementName = "PrivateAttachmentId";
	public final static String AccountElementName = "Account";
	public final static String EncryptedFlagElementName = "Encrypted";
	public final static String HQSessionKeyElementName = "HQSessionKey";
	public final static String HQPublicKeyElementName = "HQPublicKey";
	public final static String EncryptedDataElementName = "EncryptedData";
	public final static String DataPacketIdElementName = "DataPacketId";
	public final static String PrivateDataPacketIdElementName = "PrivateDataPacketId";
	public final static String DataPacketSigElementName = "DataPacketSig";
	public final static String PrivateDataPacketSigElementName = "PrivateDataPacketSig";
	public final static String FieldElementPrefix = "Field-";
	public final static String AttachmentLabelElementName = "AttachmentLabel";
	public final static String AttachmentLocalIdElementName = "AttachmentLocalId";
	public final static String AttachmentElementName = "Attachment";
	public final static String AttachmentBytesElementName = "AttachmentData";
	public final static String AttachmentKeyElementName = "AttachmentSessionKey";

	private static SAXParserFactory factory = SAXParserFactory.newInstance();
}
