package org.martus.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamEncryptor implements StreamFilter
{
	StreamEncryptor(MartusCrypto cryptoToUse)
	{
		crypto = cryptoToUse;
	}
	
	public void copyStream(InputStream in, OutputStream out) throws IOException
	{
		try
		{
			out.write(0);
			crypto.encrypt(in, out);
		}
		catch(MartusCrypto.CryptoException e)
		{
			throw new IOException("MartusCrypto exception");
		}
	}

	MartusCrypto crypto;
}
