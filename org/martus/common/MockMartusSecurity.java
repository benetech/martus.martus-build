package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.Arrays;

import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.EncryptionException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusCrypto.NoKeyPairException;

public class MockMartusSecurity extends MartusSecurity
{
	public MockMartusSecurity() throws Exception
	{
		loadSampleAccount();
	}
	
	public void readKeyPair(InputStream inputStream, String passPhrase) throws
		IOException,
		InvalidKeyPairFileVersionException,
		AuthorizationFailedException
	{
		if(fakeKeyPairVersionFailure)
			throw new InvalidKeyPairFileVersionException();

		if(fakeAuthorizationFailure)
			throw new AuthorizationFailedException();
			
		super.readKeyPair(inputStream, passPhrase);
	}
	
	public void createKeyPair()
	{
		createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}
	
	public void createKeyPair(int publicKeyBits)
	{
		//System.out.println("WARNING: Calling MockMartusSecurity.createKeyPair " + publicKeyBits);
		super.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}
	
	public void signatureInitializeSign() throws 
		MartusSignatureException
	{
		checksum = 0x12345678;
	}

	public void signatureDigestByte(byte b) throws 
		MartusSignatureException
	{
		checksum += b;
	}

	public void signatureDigestBytes(byte[] bytes)
		throws MartusSignatureException
	{
		for (int i = 0; i < bytes.length; i++)
			signatureDigestByte(bytes[i]);
	}

	public byte[] signatureGet() throws 
		MartusSignatureException
	{
		int result = checksum;
		return Integer.toHexString(result).getBytes();
	}
		
	public void signatureInitializeVerify(String publicKey) throws
		MartusSignatureException
	{
		checksum = 0x12345678;
	}
	
	public boolean signatureIsValid(byte[] sig) throws MartusSignatureException
	{
		if(fakeSigVerifyFailure)
			return false;
			
		return Arrays.equals(sig, signatureGet());
	}

	public void encrypt(InputStream plainStream, OutputStream cipherStream) throws
			NoKeyPairException,
			EncryptionException
	{
		encrypt(plainStream, cipherStream, new byte[] {0x7F});
	}

	public void encrypt(InputStream plainStream, OutputStream cipherStream, byte[] sessionKey) throws
			NoKeyPairException,
			EncryptionException
	{
		int sessionKeyByte = sessionKey[0];
		try
		{
			cipherStream.write(sessionKeyByte);
			int theByte = 0;
			while( (theByte = plainStream.read()) != -1)
				cipherStream.write(theByte ^ sessionKeyByte);
		}
		catch(IOException e)
		{
			throw new EncryptionException();
		}
	}

	public byte[] encryptSessionKey(byte[] sessionKeyBytes, String publicKey) throws 
		EncryptionException
	{
			byte[] encryptedKeyBytes = new byte[sessionKeyBytes.length];
			System.arraycopy(sessionKeyBytes, 0, encryptedKeyBytes, 0, sessionKeyBytes.length);
			encryptedKeyBytes[0] ^= 0xFF; 	
			return encryptedKeyBytes;
	}
	

	public void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream) throws
			NoKeyPairException,
			DecryptionException
	{
		decrypt(cipherStream, plainStream, null);
	}

	private byte[] readSessionKey(InputStreamWithSeek cipherStream) throws DecryptionException 
	{
		byte[] sessionKey = new byte[1];
		try
		{
			cipherStream.read(sessionKey);
		}
		catch(IOException e)
		{
			throw new DecryptionException();
		}
		return sessionKey;
	}
	
	public void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream, byte[] sessionKey) throws
			DecryptionException
	{
		try
		{
			byte[] storedSessionKey = readSessionKey(cipherStream);
			if(sessionKey == null)
				sessionKey = storedSessionKey;
				
			int sessionKeyByte = sessionKey[0];
			int theByte = 0;
			while( (theByte = cipherStream.read()) != -1)
				plainStream.write(theByte ^ sessionKeyByte);
		}
		catch(IOException e)
		{
			throw new DecryptionException();
		}
	}

	public byte[] decryptSessionKey(byte[] encryptedSessionKeyBytes) throws 
		DecryptionException
	{
			byte[] sessionKeyBytes = new byte[encryptedSessionKeyBytes.length];
			System.arraycopy(encryptedSessionKeyBytes, 0, sessionKeyBytes, 0, encryptedSessionKeyBytes.length);
			sessionKeyBytes[0] ^= 0xFF; 	
			return sessionKeyBytes;
	}
	// end MartusCrypto interface

	KeyPair createSunKeyPair(int bitsInKey) throws Exception 
	{
		int smallKeySizeForTesting = 1024;
		return super.createSunKeyPair(smallKeySizeForTesting);
	}
	
	public void loadSampleAccount() throws Exception
	{
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(sampleKeyPair));
		readKeyPair(in, samplePassphrase);
		in.close();
	}

	static final int SMALLEST_LEGAL_KEY_FOR_TESTING = 512;

	public boolean fakeSigVerifyFailure;
	public boolean fakeAuthorizationFailure;
	public boolean fakeKeyPairVersionFailure;
	
	private static int checksum;

	final static String samplePassphrase = "test";
	final static String sampleKeyPair = 
		"AOfo8lBxtmZAo0c2D9bffO1z97+VK0z8nJ+uOL6RoFA9z6VnFdxq4azR8w5Kkz" +
		"x/twX1NcSuKg7BJRpWLzQgZR+FR4MITQLRKDUA85L5Z7QP5+plPQ26R43rmRXz" +
		"14Qom/QXheNEpZ96IWRt4hWPlZrmAfCwqDRUQEeewdM/0Q7GYj57nuAee3vBJl" +
		"S+QIyxxrIKGiXgfK2x4Y5jOjAgyyXZRu4y+17nUiUJ0OyDFp0OILLWcXzihDZE" +
		"er64dbHgEMa6cWx18Duu57S+ko+q/KZiupDXVVx13HHSJrYUrK/uw/UQSQ7S3O" +
		"VzVSw9qVVleyzsRKv755Vk9zv5NmuGvYaPrZMq9pZhxwrGX8SRrimXUilx8v4R" +
		"9e6LcnzNL2ASWAlmn0BpcxjZnI01navVZOa4lRN3RAoCcco1VxpFG+9xZDnZKO" +
		"1OIQuVwK48R3jQBZNMsmRc31iJgKy3dhgn/R2vFKfYIgF4L2NoeZvHhXLPLml5" +
		"tpXGK+RQmbvU4hKv/PCNA69XP3lEBGf1N8fg9JOrAezm7Lqzfb7Gzp0dlv2+Mv" +
		"NSLrJjVQqApRhQhWHmcI/G+wuRk4HkiiJ5HfL8pAcdp/5vwMlorj+h8WNupOh5" +
		"5N957R7nXMWiUK0Iw9N9Pzt5u7PV3Bqik3uyIb5ncLM6HMIgFfSPdRY7nku/rZ" +
		"T7oOhnBWrBZThnzYpW08a3jw9F7chpS078jEw50E2b/yIyldNpEgTr2yUr4unQ" +
		"pUvh7MreLYoLb2JibdOlkWSQsfPUHQMrlFD6raFi56qGZovywFDew31XdIUPJf" +
		"FEFdiWLjvgHn0FVq3tHL34LhylNaEnfJRMt8dDRixe8nx5ZBSitPaB8VbBS6KZ" +
		"O+3ZwLZrwpJVrYIjJtr5oqImyse2RCPrOAZ+D+F184nW+d7Vn1hrx00zRPBQge" +
		"pPVvU+mHu0EwKYs9reJ8QNo4P7v0jl1EdknwLB7PptS22bJjjE07a0/1msHidt" +
		"hCBs7nkKC8GzlUpmXoLjBTTLA5WmmsRA4ugZjcIYuT6zOSWpyGrhj/Ujg9qyS/" +
		"62KVB4GXhFmfsqCltwsG6GJaN0ZDjMEfz7EOucYGrWcfhSZfqBihf5U4YDQOHQ" +
		"Up/g69cy1MqarXHqIt92WWFBjAjPtqlEaeklYE9ZnStkEntW8bASdzIQXCv9xM" +
		"5uDOOGen0rHwxNfwJHu7s2ZNa52Z6yOzqD9wFkXXxZJrG4j2XaA2z+YMHPGrqH" +
		"k8KMde1HxTUnmUkR3Oq+KjIV2W//lDglYJL+yK3sghH5eFdpFvMSVpyX/uoirN" +
		"0R321qAY0s70tlb0LlsJ8fMBQ7XqCw1aSgTz63IfMc0ZmRJR0i+3Fn0evkAcYV" +
		"86qNfzovdE/xFWZ1emZCj5VGOjbiY1+a1TumFIUvqdRqnz3Rwj7lnw90NU+mS+" +
		"2M7RzRrMGnKHgnbFOITKzB7tOrRImFMxyZlqC4YjsYEEoIXxXaAe/ph/oPQoQQ" +
		"X4S9oQIvulUE0WCzoIBro1LHUHG+Q6X3nA2z1YUCb7y//id3wR29ochWAMx9Ox" +
		"9y789sEdesjVE0kK4JJZT760tCKpmpJe2wno/ULSVJ62Ps8Ad/nLyPwiVcnhxI" +
		"H1d8S+h49I1IWt1SaDshy2V2TwibcYkwcDxw4p3fGe3UCQ3GWmQKMiKf66rFmx" +
		"iv0F52a8aPs7W9w41mWtBYMDTlC9h/oXJKYvByAu9fABlYTANlSiT2l6mXB86b" +
		"uzXglJ3HEsq4/xGl7xEUwpz+08Yu5GsBvdB1QluL0XKIKLI8ophzp1QGwnt8Wj" +
		"GV6pWSTMsTnfp32yTiuvw7KZ7SVy71TcvXcQixI76I8zUNMEB6u3S2NQbUyQJW" +
		"afN53kQooWFpkYHGKka+XxG5LPojCu33Ojti90xILjHoKYEdjNB4Au8ypDW76F" +
		"PV++E27dkYq0jfZ8E192Bk4LTk3ITIDRc++Yr8qQutMHcoDOb3pZlASAkurnjD" +
		"heNpUIP8UA+u8oKqMoM9LI9SOa5Ut+dCQSWxweq27YScBV9PFOxyBSTZ8cE7QZ" +
		"HPQbiiSPJT9WykAyMWj2CmspubD9kaswNnBdhx9c42cSt7L97b3g4H4PXMkQJI" +
		"HHp3Ut3jmnusSRz3uqkJ4cH+Va5RuMc6ZXEy8Ejw4WnGvkBtDy0SP29UfbkWr2" +
		"vKpilplduzg0SNkdqfQbxdcT2/YnbQOMZ+H3PgEmEhtPpRcj9g9YN1kQeCCHAE" +
		"l5EZBlylomHXEI++fSZBoh4lt33QJgjlnmgCp6grnid2f3Y5F0LbRLpK9WMAkK" +
		"2AIow0u5qGO7JNuZHMQogfsSvOjOylux/cNl8eQfkbUh1v4ZRCA+dhTlNkRB+w" +
		"IGI9503z3vrHg8x85a/d2G8DwloXa2bD8Ww1AqPIlrMXUhiP7evISuIt1ijxi4" +
		"wa3N3srq6RX+d+sgV3xkT6SBTbwOS+3gDkUs7PMnmLNMzOZoboNGHB44rbr8D6" +
		"b9rplgWLxBeBvKKet25Icx1lhq8EHJOfxVzOIbKAREkQxuYrB5rqcz6q27q7VP" +
		"Pikfi4CO8=";

}
