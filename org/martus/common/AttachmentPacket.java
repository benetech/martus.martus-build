package org.martus.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	public byte[] writeXmlToDatabase(Database db, boolean mustEncrypt, MartusCrypto signer) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		File temp = File.createTempFile("$$$MartusAttachment", null);
		temp.deleteOnExit();
		UnicodeWriter writer = new UnicodeWriter(temp);
		byte[] sig = writeXml(writer, signer);
		writer.close();
		
		DatabaseKey headerKey = new DatabaseKey(getUniversalId());
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(temp));
		db.writeRecord(headerKey, in);
		in.close();
		temp.delete();
		return sig;
	}

	public static void exportRawFileFromXml(InputStreamWithSeek xmlIn, byte[] sessionKeyBytes, MartusCrypto verifier, File destFile) throws
		IOException,
		org.martus.common.Packet.InvalidPacketException,
		org.martus.common.Packet.SignatureVerificationException,
		org.martus.common.Packet.WrongPacketTypeException,
		Base64.InvalidBase64Exception
	{
		AttachmentPacket ap = new AttachmentPacket(verifier);
		
		File base64File = File.createTempFile("MartusAttachExtract", null);
		base64File.deleteOnExit();

		FileOutputStream base64Out = new FileOutputStream(base64File);
		AttachmentExtractionHandler handler = new AttachmentExtractionHandler(base64Out);
		ap.loadFromXml(xmlIn, null, verifier, handler);
		base64Out.close();
		
		UnicodeReader base64Reader = new UnicodeReader(base64File);
		
		File encryptedFile = File.createTempFile("MartusEncryptedAtt", null);
		encryptedFile.deleteOnExit();
				
		FileOutputStream outEncrypted = new FileOutputStream(encryptedFile);
		Base64.decode(base64Reader, outEncrypted);
		outEncrypted.close();
		base64Reader.close();
		base64File.delete();

		InputStreamWithSeek inEncrypted = new FileInputStreamWithSeek(encryptedFile);
		FileOutputStream outRaw = new FileOutputStream(destFile);
		try 
		{
			verifier.decrypt(inEncrypted, outRaw, sessionKeyBytes);
		} 
		catch(Exception e) 
		{
			throw new IOException(e.toString());
		} 
		outRaw.close();
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

		File base64File = copyRawFileToBase64File(rawFile);
		dest.writeStartTag(MartusXml.AttachmentBytesElementName);
		
		UnicodeReader reader = new UnicodeReader(base64File);
		char[] buffer = new char[MartusConstants.streamBufferCopySize];
		int count = 0;
		while( (count = reader.read(buffer)) >= 0)
		{
			String chunk = new String(buffer, 0, count);
			dest.writeEncoded(chunk);
			dest.writeDirect(NEWLINE);
		}
		reader.close();

		dest.writeEndTag(MartusXml.AttachmentBytesElementName);
	}
	
	protected File copyRawFileToBase64File(File rawFile) throws 
		IOException
	
	{
		//System.out.println("AttachmentPacket.copyRawFileToBase64File begin");
		File encryptedFile = File.createTempFile("MartusEncryptedAtt",null);
		encryptedFile.deleteOnExit();
		InputStream inRaw = new BufferedInputStream(new FileInputStream(rawFile));
		OutputStream outEncrypted = new BufferedOutputStream(new FileOutputStream(encryptedFile));
		try 
		{
			security.encrypt(inRaw, outEncrypted, sessionKey);
		} 
		catch(Exception e) 
		{
			throw new IOException(e.getMessage());
		} 
		outEncrypted.close();
		inRaw.close();
		//System.out.println("AttachmentPacket.copyRawFileToBase64File encrypted");

		File base64File = File.createTempFile("MartusAtt",null);
		base64File.deleteOnExit();

		FileInputStream inEncrypted = new FileInputStream(encryptedFile);
		UnicodeWriter base64Writer = new UnicodeWriter(base64File);

		Base64.encode(inEncrypted, base64Writer);

		base64Writer.close();
		inEncrypted.close();
		//System.out.println("AttachmentPacket.copyRawFileToBase64File base64");
		return base64File;
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
