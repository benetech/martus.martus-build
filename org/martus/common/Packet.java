package org.martus.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.martus.common.MartusCrypto.MartusSignatureException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Packet
{
	public static class InvalidPacketException extends SAXException
	{
		public InvalidPacketException(String message)
		{
			super(message);
		}
	}
	
	public static class WrongPacketTypeException extends SAXException
	{
		public WrongPacketTypeException(String message)
		{
			super(message);
		}
	}

	public static class SignatureVerificationException extends SAXException
	{
		public SignatureVerificationException()
		{
			super("Signature verification exception");
		}
	}
		
	
	public static class WrongAccountException extends Exception
	{
	}

	public Packet()
	{
		this(UniversalId.createFromAccountAndLocalId("Packet()", ""));
	}
	
	public Packet(UniversalId universalIdToUse)
	{
		uid = universalIdToUse;
	}
	
	public UniversalId getUniversalId()
	{
		return uid;
	}
	
	public String getAccountId()
	{
		return uid.getAccountId();
	}
	
	private void setAccountId(String accountString)
	{
		uid.setAccountId(accountString);
	}

	public String getLocalId()
	{
		return uid.getLocalId();
	}
	
	public void setUniversalId(UniversalId newUid)
	{
		uid = newUid;
	}

	private void setPacketId(String newPacketId)
	{
		uid.setLocalId(newPacketId.replace(':', '-'));
	}
	
	public boolean isPublicData()
	{
		return false;	
	}
	
	public byte[] writeXml(OutputStream out, MartusCrypto signer) throws IOException
	{
		UnicodeWriter writer = new UnicodeWriter(out);
		byte[] sig = writeXml(writer, signer);
		writer.flush();
		return sig;
	}
	
	public byte[] writeXml(Writer writer, MartusCrypto signer) throws IOException
	{
		try 
		{
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			XmlWriterFilter dest = new XmlWriterFilter(bufferedWriter);
			synchronized(signer)
			{
				dest.startSignature(signer);
	
				dest.writeDirect(MartusXml.packetStartComment + MartusXml.newLine);			
				dest.writeStartTag(getPacketRootElementName());
				internalWriteXml(dest);
				dest.writeEndTag(getPacketRootElementName());
				
				byte[] sig = dest.getSignature();
				dest.writeDirect(MartusXml.packetSignatureStart);
				dest.writeDirect(Base64.encode(sig));
				dest.writeDirect(MartusXml.packetSignatureEnd + MartusXml.newLine);
	
				bufferedWriter.flush();
				return sig;
			}
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			e.printStackTrace();
			throw new IOException("Signature creation exception: " + e.getMessage());
		}
	}


	public byte[] writeXmlToDatabase(Database db, boolean mustEncrypt, MartusCrypto signer) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		StringWriter headerWriter = new StringWriter();
		byte[] sig = writeXml(headerWriter, signer);
		DatabaseKey headerKey = new DatabaseKey(getUniversalId());
		if(mustEncrypt && isPublicData())
			db.writeRecordEncrypted(headerKey, headerWriter.toString(), signer);
		else
			db.writeRecord(headerKey, headerWriter.toString());
		return sig;
	}

	public void loadFromXmlInternal(InputStreamWithSeek inputStream, byte[] expectedSig, MartusCrypto verifier) throws 
		IOException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		MartusCrypto.DecryptionException,
		MartusCrypto.NoKeyPairException
	{
		XmlHandler handler = new XmlHandler(getPacketRootElementName());
		loadFromXml(inputStream, expectedSig, verifier, handler);
		if(!handler.gotStartTag)
			throw new InvalidPacketException("No root tag");
	}
	
	static public void validateXml(InputStreamWithSeek inputStream, String accountId, String localId, byte[] expectedSig, MartusCrypto verifier) throws 
		IOException,
		InvalidPacketException,
		SignatureVerificationException,
		WrongAccountException,
		MartusCrypto.DecryptionException
	{
		verifyPacketSignature(inputStream, expectedSig, verifier);
		XmlValidateHandler handler = new XmlValidateHandler();
		BufferedReader reader = new UnicodeReader(inputStream);
		try 
		{
			MartusXml.loadXmlWithExceptions(reader, handler);
		}
		catch (Exception e)
		{
			throw new InvalidPacketException(e.toString());
		}
		if(!accountId.equals(handler.accountId))
			throw new WrongAccountException();
		if(!localId.equals(handler.localId))
			throw new InvalidPacketException("Wrong Local ID: expected " + localId + " but was " + handler.localId);
	}

	protected static void verifyPacketSignature(InputStreamWithSeek inputStream, MartusCrypto verifier) throws
			IOException,
			InvalidPacketException,
			SignatureVerificationException
	{
		verifyPacketSignature(inputStream, null, verifier);
	}
	
	public static void verifyPacketSignature(InputStreamWithSeek in, byte[] expectedSig, MartusCrypto verifier) throws
			IOException,
			InvalidPacketException,
			SignatureVerificationException
	{
		final long totalBytes = in.available();
		UnicodeReader reader = new UnicodeReader(in);
		
		final String startComment = reader.readLine();
		if(startComment == null || !startComment.equals(MartusXml.packetStartComment))
			throw new InvalidPacketException("No start comment");

		final String packetType = reader.readLine();
		final String accountLine = reader.readLine();
		final String publicKey = extractPublicKeyFromXmlLine(accountLine);
		
		try
		{
			synchronized(verifier)
			{
				verifier.signatureInitializeVerify(publicKey);
				
				digestOneLine(startComment, verifier);
				digestOneLine(packetType, verifier);
				digestOneLine(accountLine, verifier);
				
				String sigLine = null;
				String line = null;
				while( (line=reader.readLine()) != null)
				{
					if(line.startsWith(MartusXml.packetSignatureStart))
					{
						sigLine = line;
						break;
					}
					
					digestOneLine(line, verifier);
				}
				
				byte[] sigBytes = extractSigFromXmlLine(sigLine);
				if(expectedSig != null && !Arrays.equals(expectedSig, sigBytes))
					throw new SignatureVerificationException();

				if(!verifier.signatureIsValid(sigBytes))
					throw new SignatureVerificationException();
			}
		}
		catch (MartusSignatureException e)
		{
			throw new SignatureVerificationException();
		}
		catch (UnsupportedEncodingException e)
		{
			throw new SignatureVerificationException();
		}

		in.seek(0);
	}

	static void digestOneLine(final String packetType, MartusCrypto verifier)
		throws MartusSignatureException, UnsupportedEncodingException
	{
		verifier.signatureDigestBytes(packetType.getBytes("UTF-8"));
		verifier.signatureDigestBytes(newlineBytes);
	}

	static byte[] extractSigFromXmlLine(String sigLine)
		throws InvalidPacketException
	{
		if(sigLine == null)
			throw new InvalidPacketException("No signature start");
		if(!sigLine.endsWith(MartusXml.packetSignatureEnd))
			throw new InvalidPacketException("No signature end");

		final int sigOverhead = MartusXml.packetSignatureStart.length() + 
								MartusXml.packetSignatureEnd.length();
		final int sigLen = sigLine.length() - sigOverhead;
		final int actualSigStart = sigLine.indexOf("=") + 1;
		final int actualSigEnd = actualSigStart + sigLen;
		
		byte[] sigBytes = null;
		try
		{
			sigBytes = Base64.decode(sigLine.substring(actualSigStart, actualSigEnd));
		}
		catch(Base64.InvalidBase64Exception e)
		{
			throw new InvalidPacketException("Signature not valid Base64");
		}
		return sigBytes;
	}

	static String extractPublicKeyFromXmlLine(final String accountLine)
		throws InvalidPacketException
	{
		if(accountLine == null)		
			throw new InvalidPacketException("No Account Tag");

		final String accountTag = MartusXml.getTagStart(MartusXml.AccountElementName);
		if(!accountLine.startsWith(accountTag))
			throw new InvalidPacketException("No Account Tag");
		
		int startIndex = accountLine.indexOf(">");
		int endIndex = accountLine.indexOf("</");	
		if(startIndex < 0 || endIndex < 0)
			throw new InvalidPacketException("Invalid Account Element");
		++startIndex;
		final String publicKey = accountLine.substring(startIndex, endIndex);
		return publicKey;
	}

	protected String getPacketRootElementName()
	{
		return null;
	}
	
	protected void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		writeElement(dest, MartusXml.PacketIdElementName, getLocalId());
		writeElement(dest, MartusXml.AccountElementName, getAccountId());
	}
	
	protected void setFromXml(String elementName, String data) throws 
			Base64.InvalidBase64Exception
	{
		if(elementName.equals(MartusXml.PacketIdElementName))
		{
			setPacketId(data);
		}
		else if(elementName.equals(MartusXml.AccountElementName))
		{
			setAccountId(data);
		}
	}
	
	class XmlHandler extends DefaultHandler
	{
		XmlHandler(String expectedRootTag)
		{
			rootTag = expectedRootTag;
		}
		
		public void startElement(String namespaceURI, String sName, String qName,
				Attributes attrs) throws SAXException
		{
			if(!gotStartTag)
			{
				if(rootTag != null && !qName.equals(rootTag))
					throw new WrongPacketTypeException("Incorrect root tag");
			}
			gotStartTag = true;
			currentElementName = qName;
			data = new StringBuffer();
		}

		public void endElement(String namespaceURI, String sName, String qName)
		{
			if(!gotStartTag)
				return;
			try 
			{
				setFromXml(qName, new String(data));
			} 
			catch(Base64.InvalidBase64Exception e)
			{
				System.out.println("Packet.endelement: " + e);
			}
			currentElementName = "";				
			data = new StringBuffer();
		}

		public void characters(char buf[], int offset, int len) throws SAXException
		{
			if(!gotStartTag)
				return;

			data.append(buf,offset, len);
		}

		boolean gotStartTag;
		String rootTag;
		String currentElementName;
		StringBuffer data;
	}
	
	static class XmlValidateHandler extends DefaultHandler
	{
		XmlValidateHandler()
		{
		}
		
		public void startElement(String namespaceURI, String sName, String qName,
				Attributes attrs) throws SAXException
		{
			currentElementName = qName;
			data = new StringBuffer();
		}

		public void endElement(String namespaceURI, String sName, String qName)
		{
			String raw = new String(data);
			byte[] bytes = raw.getBytes();
			try 
			{
				if(currentElementName.equals(MartusXml.AccountElementName))
					accountId = new String(bytes,"UTF-8");
				if(currentElementName.equals(MartusXml.PacketIdElementName))
					localId = new String(bytes,"UTF-8");
			} 
			catch(UnsupportedEncodingException e) 
			{
				System.out.println("PacketValidate.endelement: " + e);
			}
			currentElementName = "";				
			data = new StringBuffer();
		}

		public void characters(char buf[], int offset, int len) throws SAXException
		{
			if(currentElementName.equals(MartusXml.AccountElementName) ||
				currentElementName.equals(MartusXml.PacketIdElementName))
			{
				data.append(buf,offset, len);
			}
		}

		String currentElementName;
		StringBuffer data;
		String accountId;
		String localId;
	}

	protected void writeElement(XmlWriterFilter dest, String tag, String data) throws IOException
	{
		dest.writeStartTag(tag);		
		dest.writeEncoded(data);
		dest.writeEndTag(tag);		
	}
	
	public void loadFromXml(InputStreamWithSeek inputStream, byte[] expectedSig, MartusCrypto verifier) throws 
		IOException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		MartusCrypto.DecryptionException,
		MartusCrypto.NoKeyPairException
	{
		throw new WrongPacketTypeException("Can't call loadFromXml directly on a Packet object!");
	}
	
	protected void loadFromXml(InputStreamWithSeek inputStream, byte[] expectedSig, MartusCrypto verifier, DefaultHandler handler) throws 
		IOException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException
	{
		if(verifier != null)
			verifyPacketSignature(inputStream, expectedSig, verifier);
		BufferedReader reader = new UnicodeReader(inputStream);
		try 
		{
			MartusXml.loadXmlWithExceptions(reader, handler);
		}
		catch(WrongPacketTypeException e)
		{
			throw(e);
		}
		catch(SAXParseException e)
		{
			//System.out.println("Packet.loadFromXml: " + e);
			throw new InvalidPacketException("SAXParseException " + e.getMessage());
		}
		catch(ParserConfigurationException e)
		{
			throw new InvalidPacketException("ParserConfigurationException");
		}
		catch(SAXException e)
		{
			throw new InvalidPacketException("SAXException");
		}
		finally
		{
			reader.close();
		}
	}

	final static byte[] newlineBytes = "\n".getBytes();
	UniversalId uid;
}
