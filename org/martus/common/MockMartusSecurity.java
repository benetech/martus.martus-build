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
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;

public class MockMartusSecurity extends MartusSecurity
{
	public MockMartusSecurity() throws Exception
	{
		loadSampleAccount();
	}

	public void speedWarning(String message)
	{
		//System.out.println("MockMartusSecurity.speedWarning: " + message);
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
		speedWarning("Calling MockMartusSecurity.createKeyPair " + publicKeyBits);
		super.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
	}

	public boolean signatureIsValid(byte[] sig) throws MartusSignatureException
	{
		if(fakeSigVerifyFailure)
			return false;

		return super.signatureIsValid(sig);
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
		setKeyPairFromData(Base64.decode(nonEncryptedSampleKeyPair));
	}

	static final int SMALLEST_LEGAL_KEY_FOR_TESTING = 512;

	public boolean fakeSigVerifyFailure;
	public boolean fakeAuthorizationFailure;
	public boolean fakeKeyPairVersionFailure;

	private static int checksum;

	final static String nonEncryptedSampleKeyPair = 
		"rO0ABXNyABVqYXZhLnNlY3VyaXR5LktleVBhaXKXAww60s0SkwIAAkwACnByaX" + 
		"ZhdGVLZXl0ABpMamF2YS9zZWN1cml0eS9Qcml2YXRlS2V5O0wACXB1YmxpY0tl" +
		"eXQAGUxqYXZhL3NlY3VyaXR5L1B1YmxpY0tleTt4cHNyADFvcmcuYm91bmN5Y2" +
		"FzdGxlLmpjZS5wcm92aWRlci5KQ0VSU0FQcml2YXRlQ3J0S2V5bLqHzgJzVS4C" +
		"AAZMAA5jcnRDb2VmZmljaWVudHQAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjtMAA" +
		"5wcmltZUV4cG9uZW50UHEAfgAFTAAOcHJpbWVFeHBvbmVudFFxAH4ABUwABnBy" +
		"aW1lUHEAfgAFTAAGcHJpbWVRcQB+AAVMAA5wdWJsaWNFeHBvbmVudHEAfgAFeH" +
		"IALm9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZGVyLkpDRVJTQVByaXZhdGVL" +
		"ZXmyNYtAHTGFVgIABEwAB21vZHVsdXNxAH4ABUwAEHBrY3MxMkF0dHJpYnV0ZX" +
		"N0ABVMamF2YS91dGlsL0hhc2h0YWJsZTtMAA5wa2NzMTJPcmRlcmluZ3QAEkxq" +
		"YXZhL3V0aWwvVmVjdG9yO0wAD3ByaXZhdGVFeHBvbmVudHEAfgAFeHBzcgAUam" +
		"F2YS5tYXRoLkJpZ0ludGVnZXKM/J8fqTv7HQMABkkACGJpdENvdW50SQAJYml0" +
		"TGVuZ3RoSQATZmlyc3ROb256ZXJvQnl0ZU51bUkADGxvd2VzdFNldEJpdEkABn" +
		"NpZ251bVsACW1hZ25pdHVkZXQAAltCeHIAEGphdmEubGFuZy5OdW1iZXKGrJUd" +
		"C5TgiwIAAHhw///////////////+/////gAAAAF1cgACW0Ks8xf4BghU4AIAAH" +
		"hwAAAAgIbZPktljeCh3opk2hs84uU3zZK9Dd/Yu9pSU4nC6Y5BMN158f0KXBqd" +
		"/LhLa2xWaPAFwl0YPsfIEWdleKAIhKQsg0iE6oAgvPzgxquTiQ3/MDCppoP+4s" +
		"lXe4DjyOvmEZbJ0D7BgprZfrydQQr4KgdGEhNqu0Sq6c+3NQ1IqiP5eHNyABNq" +
		"YXZhLnV0aWwuSGFzaHRhYmxlE7sPJSFK5LgDAAJGAApsb2FkRmFjdG9ySQAJdG" +
		"hyZXNob2xkeHA/QAAAAAAACHcIAAAAAwAAAAB4c3IAEGphdmEudXRpbC5WZWN0" +
		"b3LZl31bgDuvAQIAA0kAEWNhcGFjaXR5SW5jcmVtZW50SQAMZWxlbWVudENvdW" +
		"50WwALZWxlbWVudERhdGF0ABNbTGphdmEvbGFuZy9PYmplY3Q7eHAAAAAAAAAA" +
		"AHVyABNbTGphdmEubGFuZy5PYmplY3Q7kM5YnxBzKWwCAAB4cAAAAApwcHBwcH" +
		"BwcHBwc3EAfgAK///////////////+/////gAAAAF1cQB+AA4AAACAR2PzzZAd" +
		"72TBHBdGSqfDakq4III0hZDb7A13hSr0HiKDSBNh/m7ld4DRFkYLsdNku05X1u" +
		"630y2u3GLlgeZkViRcwjzhAHO7lMQhnlmom3dykfNMv0cB4z1BRc3VLC+74oCa" +
		"baQyTpDqnYIwiXp7w3Y4U7p1tugfWG+wSiTuvEV4c3EAfgAK//////////////" +
		"/+/////gAAAAF1cQB+AA4AAABAa0tFUKBgvpZouwpHA3N134myWvAZyFtE6xOo" +
		"vPz7I8rd/GsJPH/8aO2S1s/MmHuehKuNbf/aYV2ft5/bhCKjEHhzcQB+AAr///" +
		"////////////7////+AAAAAXVxAH4ADgAAAECTHJOB858hABjyTaTOXO9vA3lQ" +
		"3HsemzhYNr+H4KpR5vPWUD5SbHCCsKRCda8foH3qKmE1d7bN+8QI5OjzG67ZeH" +
		"NxAH4ACv///////////////v////4AAAABdXEAfgAOAAAAQHTskqjRMy6fRqba" +
		"jbjSr4zCBI0osNI+lTmDIEK0EZhiKnsafuzP0EaOHe1jbx5uvtbYiCdMYqdcWK" +
		"xLckYoJDl4c3EAfgAK///////////////+/////gAAAAF1cQB+AA4AAABA0Gh7" +
		"osPMGWrOAe3+zwOoh++Wh+MDwLE6fPg6AH5GnrHZb5xYShmfY8+TXia4F3iyYR" +
		"FfC77to89Vt0RKAxHiX3hzcQB+AAr///////////////7////+AAAAAXVxAH4A" +
		"DgAAAEClpHpvKF3XYaQXCvNwf84HaDEdTvp/Lf4RecMJKcOX4GbZEDPPe7xj8/" +
		"+694gVx45bCBY3rDZtGChJauHjY4ineHNxAH4ACv///////////////v////4A" +
		"AAABdXEAfgAOAAAAARF4c3IALW9yZy5ib3VuY3ljYXN0bGUuamNlLnByb3ZpZG" +
		"VyLkpDRVJTQVB1YmxpY0tleSUiag5b+myEAgACTAAHbW9kdWx1c3EAfgAFTAAO" +
		"cHVibGljRXhwb25lbnRxAH4ABXhwcQB+AA1xAH4AIw==";
	
	public byte[] signatureGet() throws MartusSignatureException
	{
		speedWarning("signatureGet");
		return super.signatureGet();
	}

}
