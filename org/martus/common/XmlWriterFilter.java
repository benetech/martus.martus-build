/* $Id: $ */
package org.martus.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.martus.common.*;

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
		StringBuffer buf = new StringBuffer(text);
		for(int i = 0; i < buf.length(); ++i)
		{
			char c = buf.charAt(i);
			if(c == '&')
			{
				buf.replace(i, i+1, "&amp;");
			}
			else if(c == '<')
			{
				buf.replace(i, i+1, "&lt;");
			}
			else if(c == '>')
			{
				buf.replace(i, i+1, "&gt;");
			}
		}
		writeDirect(new String(buf));
	}
	
	public void writeDirect(String s) throws IOException
	{
		if(sigGen != null)
		{
			try
			{
				byte[] bytes = s.getBytes("UTF-8");
				for(int i=0; i < bytes.length; ++i)
					sigGen.signatureDigestByte(bytes[i]);
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
