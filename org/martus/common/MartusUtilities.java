package org.martus.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.martus.common.MartusCrypto.MartusSignatureException;

public class MartusUtilities 
{
	public static class FileTooLargeException extends Exception {}
	
	public static int getCappedFileLength(File file) throws FileTooLargeException
	{
		long rawLength = file.length();
		if(rawLength >= Integer.MAX_VALUE || rawLength < 0)
			throw new FileTooLargeException();
			
		return (int)rawLength;
	}

	public static String getVersionDate(java.lang.Class classToUse)
	{
		String versionDate = "";
		InputStream versionStream = null;
		String fileVersionInfo = "version.txt";
		versionStream = classToUse.getResourceAsStream(fileVersionInfo);
		if(versionStream != null)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(versionStream));
			try 
			{
				versionDate = reader.readLine();
				reader.close();
			} 
			catch(IOException ifNoDateAvailableLeaveItBlank) 
			{
			}
		}
		return versionDate;
	}

	public static String createSignature(String stringToSign, MartusCrypto security)
		throws UnsupportedEncodingException, MartusSignatureException 
	{
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = security.createSignature(new ByteArrayInputStream(bytesToSign));
		String signature = Base64.encode(sigBytes);
		return signature;
	}

	public static boolean verifySignature(Vector dataToSign, MartusCrypto verifier, String signedBy, String sig)
	{
		try
		{
			verifier.signatureInitializeVerify(signedBy);
			for(int element = 0; element < dataToSign.size(); ++element)
			{
				String thisElement = dataToSign.get(element).toString();
				byte[] bytesToSign = thisElement.getBytes("UTF-8");
				//TODO: might want to optimize this for speed
				for(int b = 0; b < bytesToSign.length; ++b)
					verifier.signatureDigestByte(bytesToSign[b]);
				verifier.signatureDigestByte((byte)0);
			}
			byte[] sigBytes = Base64.decode(sig);
			return verifier.signatureIsValid(sigBytes);
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static String sign(Vector dataToSign, MartusCrypto signer) throws 
			MartusCrypto.MartusSignatureException
	{
		try
		{
			signer.signatureInitializeSign();
			for(int element = 0; element < dataToSign.size(); ++element)
			{
				String thisElement = dataToSign.get(element).toString();
				byte[] bytesToSign = thisElement.getBytes("UTF-8");
				//TODO: might want to optimize this for speed
				for(int b = 0; b < bytesToSign.length; ++b)
					signer.signatureDigestByte(bytesToSign[b]);
				signer.signatureDigestByte((byte)0);
			}
			return Base64.encode(signer.signatureGet());
		}
		catch(Exception e)
		{
			// TODO: Needs tests!
			System.out.println("ServerProxy.sign: " + e);
			throw new MartusCrypto.MartusSignatureException();
		}
	}

	public static String formatPublicCode(String publicCode) 
	{
		String formatted = "";
		while(publicCode.length() > 0)
		{
			String portion = publicCode.substring(0, 4);
			formatted += portion + "." ;
			publicCode = publicCode.substring(4);
		}
		if(formatted.endsWith("."))
			formatted = formatted.substring(0,formatted.length()-1);
		return formatted;
	}
	
	public static String computePublicCode(String publicKeyString) throws 
		Base64.InvalidBase64Exception
	{
		String digest = null;
		try
		{
			digest = MartusSecurity.createDigestString(publicKeyString);
		}
		catch(Exception e)
		{
			System.out.println("MartusApp.computePublicCode: " + e);
			return "";
		}
		
		final int codeSizeChars = 20;
		char[] buf = new char[codeSizeChars];
		int dest = 0;
		for(int i = 0; i < codeSizeChars/2; ++i)
		{
			int value = Base64.getValue(digest.charAt(i));
			int high = value >> 3;
			int low = value & 0x07;
			
			buf[dest++] = (char)('1' + high);
			buf[dest++] = (char)('1' + low);
		}
		return new String(buf);
	}
}
