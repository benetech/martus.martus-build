package org.martus.common;

import java.io.IOException;
import java.io.Writer;

public class XmlWriterFilter
{
	public XmlWriterFilter(Writer writerToUse)
	{
		writer = writerToUse;
	}
	
	public void writeStartTag(String text) throws IOException
	{
		writeDirect("<" + text + ">");
	}

	public void writeEndTag(String text) throws IOException
	{
		writeStartTag("/" + text);
		writeDirect("\n");
	}

	public void writeEncoded(String text) throws IOException
	{
		writeDirect(MartusUtilities.getXmlEncoded(text));
	}

	public void writeDirect(String s) throws IOException
	{
		if(sigGen != null)
		{
			try
			{
				byte[] bytes = s.getBytes("UTF-8");
				sigGen.signatureDigestBytes(bytes);
			}
			catch(MartusCrypto.MartusSignatureException e)
			{
				throw new IOException("Signature Exception: " + e.getMessage());
			}
		}
		writer.write(s);
	}
	
	public void startSignature(MartusCrypto sigGenToUse) throws 
				MartusCrypto.MartusSignatureException
	{
		sigGen = sigGenToUse;
		sigGen.signatureInitializeSign();
	}
	
	public byte[] getSignature() throws 
				MartusCrypto.MartusSignatureException
	{
		if(sigGen == null)
			throw new MartusCrypto.MartusSignatureException();
			
		byte[] sig = sigGen.signatureGet();
		sigGen = null;
		return sig;
	}

	private Writer writer;
	private MartusCrypto sigGen;
}
