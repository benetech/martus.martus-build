package org.martus.common;

import java.util.*;
import java.io.*;
// this class is (more or less) as presented in
// the book "Java Cryptography" by Jonathan Knudsen,
// which is copyright 1998 O'Reilly & Associates

public class Base64
{
	public static class InvalidBase64Exception extends Exception {}

	public final static int BYTESPERLINE = 45;

	public static String encode(byte[] raw)
	{
		return encode(raw, 0, raw.length);
	}

	public static String encode(byte[] raw, int start, int length)
	{
		StringBuffer encoded = new StringBuffer();

		int stopAt = start + length;
		for(int i = start; i < stopAt; i += 3)
		{
			encoded.append(encodeBlock(raw, i, length));
			length -= 3;
		}

		return encoded.toString();
	}

	public static String encodeLineWrapped(byte[] raw)
	{
		final int WRAPWIDTH = BYTESPERLINE * 4 / 3;
		StringBuffer encoded = new StringBuffer();

		for(int i = 0; i < raw.length; i += BYTESPERLINE)
		{
			int length = Math.min(BYTESPERLINE, raw.length - i);
			encoded.append(encode(raw, i, length));
			encoded.append("\r\n");
		}

		return encoded.toString();
	}
	
	public static byte[] decode(String base64) throws InvalidBase64Exception
	{
		try
		{
			int pad = 0;
			for(int i = base64.length() - 1; base64.charAt(i) == '='; --i)
				pad++;

			int length = base64.length() * 6 / 8 - pad;
			byte[] raw = new byte[length];
			int rawIndex = 0;
			for(int i = 0; i < base64.length(); i += 4)
			{
				int block =
					(getValue(base64.charAt(i)) << 18) +
					(getValue(base64.charAt(i+1)) << 12) +
					(getValue(base64.charAt(i+2)) << 6) +
					getValue(base64.charAt(i+3));
	
				for(int j = 0; j < 3 && rawIndex + j < raw.length; ++j)
					raw[rawIndex+j] = (byte)((block >> (8 * (2-j))) & 0xff);
	
				rawIndex += 3;
			}
	
			return raw;
		}
		catch (Exception e)
		{
			throw new InvalidBase64Exception();
		}
	}

	public static byte[] decodeLineWrapped(String base64) throws InvalidBase64Exception
	{
		Vector decoded = new Vector();
		BufferedReader reader = new BufferedReader(new StringReader(base64));
		while (true)
		{
			String currentLine = null;
			try
			{
				currentLine = reader.readLine();
			}
			catch(Exception e)
			{
				//currentLine will be null
			}
			if(currentLine == null)
				break;
			byte[] readInBytes = decode(currentLine);
			for(int i = 0 ; i < readInBytes.length ; ++i)
			{
				decoded.add(new Byte(readInBytes[i]));
			}
		}
		byte[] decodedBytes = new byte[decoded.size()];
		for(int i = 0 ; i < decoded.size() ; ++i)
		{
			Byte b = (Byte)decoded.get(i);
			decodedBytes[i] = b.byteValue();
		}
		return decodedBytes;
	}
	
	public static File decodeToTempFile(String base64) throws IOException, InvalidBase64Exception
	{
		File tempFile = File.createTempFile("$$$Martus-base64decode", null);
		tempFile.deleteOnExit();
	
		OutputStream outputStream = new FileOutputStream(tempFile);
		outputStream.write(decode(base64));
		outputStream.close();
		
		return tempFile;
	}

	public static void encode(InputStream rawInput, Writer encodedOut) throws
		IOException
	{
		final int chunkSize = 3 * MartusConstants.streamBufferCopySize;
		BufferedWriter writer = new BufferedWriter(encodedOut);
		BufferedInputStream in = new BufferedInputStream(rawInput);
		byte[] data = new byte[chunkSize];
		while(true)
		{
			int got = in.read(data);
			if(got < 1)
				break;
				
			String encoded = encode(data, 0, got);
			writer.write(encoded);
		}
		writer.flush();
	}
	
	public static void decode(Reader encodedIn, OutputStream rawOut) throws 
		IOException,
		InvalidBase64Exception
	{
		BufferedReader bufferedIn = new BufferedReader(encodedIn);
		BufferedOutputStream bufferedOut = new BufferedOutputStream(rawOut);
		final int blockSize = 4;
		char[] data = new char[blockSize];
		while(true)
		{
			int got = bufferedIn.read(data);
			if(got < 1)
				break;
				
			if(got != data.length)
				throw new InvalidBase64Exception();

			byte[] bytes = decode(new String(data));
			bufferedOut.write(bytes);
		}
		bufferedOut.flush();
	}


	protected static char[] encodeBlock(byte[] raw, int offset, int length)
	{
		int block = 0;
		int slack = length - 1;
		int end = (slack >= 2) ? 2 : slack;
		for(int i = 0; i <= end; ++i)
		{
			byte b = raw[offset + i];
			int neuter = (b < 0) ? b + 256 : b;
			block += neuter << (8 * (2 - i));
		}

		char[] base64 = new char[4];
		for(int i = 0; i < 4; ++i)
		{
			int sixBit = (block >>> (6 * (3 - i))) & 0x3f;
			base64[i] = getChar(sixBit);
		}

		if(slack < 1)
			base64[2] = '=';
		if(slack < 2)
			base64[3] = '=';

		return base64;
	}

	public static char getChar(int sixBit)
	{
		if(sixBit >= 0 && sixBit <= 25)
			return (char)('A' + sixBit);

		if(sixBit >= 26 && sixBit <= 51)
			return (char)('a' + (sixBit-26));

		if(sixBit >= 52 && sixBit <= 61)
			return (char)('0' + (sixBit-52));

		if(sixBit == 62)
			return '+';

		if(sixBit == 63)
			return '/';

		return '?';
	}

	public static int getValue(char c) throws InvalidBase64Exception
	{
		if(c >= 'A' && c <= 'Z')
			return c - 'A';

		if(c >= 'a' && c <= 'z')
			return c - 'a' + 26;

		if(c >= '0' && c <= '9')
			return c - '0' + 52;

		if(c == '+')
			return 62;

		if(c == '/')
			return 63;

		if(c == '=')
			return 0;

		throw new InvalidBase64Exception();
	}
}
