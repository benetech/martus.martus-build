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

package org.martus.common.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.net.ssl.KeyManager;

import org.martus.util.Base64;
import org.martus.util.InputStreamWithSeek;

public abstract class MartusCrypto
{
	// key pair stuff
	public abstract boolean hasKeyPair();
	public abstract void clearKeyPair();
	public abstract void createKeyPair();
	public abstract void writeKeyPair(OutputStream outputStream, String passPhrase) throws
		IOException;
	public abstract void readKeyPair(InputStream inputStream, String passPhrase) throws
		IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException;
	public abstract String getPublicKeyString();
	public abstract byte[] getDigestOfPartOfPrivateKey() throws CreateDigestException;
	public abstract String getSignatureOfPublicKey()
		throws Base64.InvalidBase64Exception, MartusCrypto.MartusSignatureException;

	// one-shot signature methods
	public abstract byte[] createSignatureOfStream(InputStream inputStream) throws
		MartusSignatureException;
	public abstract boolean isValidSignatureOfStream(String publicKeyString, InputStream inputStream, byte[] signature) throws
		MartusSignatureException;
	public abstract String createSignatureOfVectorOfStrings(Vector dataToSign) throws
			MartusCrypto.MartusSignatureException;
	public abstract boolean verifySignatureOfVectorOfStrings(Vector dataToTest, String signedBy, String sig);
	public abstract boolean verifySignatureOfVectorOfStrings(Vector dataToTestWithSignature, String signedBy);
		
	// multi-part signature methods
	public abstract void signatureInitializeSign() throws
		MartusSignatureException;
	public abstract void signatureDigestByte(byte b) throws
		MartusSignatureException;
	public abstract void signatureDigestBytes(byte[] bytes) throws
			MartusSignatureException;
	public abstract byte[] signatureGet() throws
		MartusSignatureException;
	public abstract void signatureInitializeVerify(String publicKey) throws
		MartusSignatureException;
	public abstract boolean signatureIsValid(byte[] sig) throws
		MartusSignatureException;

	// session keys
	public abstract byte[] createSessionKey() throws
			EncryptionException;
	public abstract byte[] encryptSessionKey(byte[] sessionKeyBytes, String publicKey) throws
		EncryptionException;
	public abstract byte[] decryptSessionKey(byte[] encryptedSessionKeyBytes) throws
		DecryptionException;

	// encrypt/decrypt
	public abstract void encrypt(InputStream plainStream, OutputStream cipherStream, byte[] sessionKeyBytes) throws
			EncryptionException,
			NoKeyPairException;
	public abstract void encrypt(InputStream plainStream, OutputStream cipherStream) throws
			NoKeyPairException,
			EncryptionException;
	public abstract void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream, byte[] sessionKeyBytes) throws
			DecryptionException;
	public abstract void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream) throws
			NoKeyPairException,
			DecryptionException;

	// cipher streams
	public abstract OutputStream createEncryptingOutputStream(OutputStream cipherStream, byte[] sessionKeyBytes)
		throws EncryptionException;
	public abstract InputStream createDecryptingInputStream(InputStreamWithSeek cipherStream, byte[] sessionKeyBytes)
		throws	DecryptionException;

	// other
	public abstract String createRandomToken();
	public abstract KeyManager [] createKeyManagers() throws Exception;
	
	// Secret Share of Private Key
//	logi put back in after 30 day wait	public abstract Vector getKeyShareBundles();
//	logi put back in after 30 day wait	public abstract void recoverFromKeyShareBundles(Vector shares) throws KeyShareException;

	// public codes
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

	public static String removeNonDigits(String userEnteredPublicCode)
	{
		String normalizedPublicCode = "";
		for (int i=0 ; i < userEnteredPublicCode.length(); ++i)
		{
			if ("0123456789".indexOf(userEnteredPublicCode.substring(i, i+1)) >= 0)
				normalizedPublicCode += userEnteredPublicCode.substring(i, i+1);
		}
		return normalizedPublicCode;
	}

	public static String getFormattedPublicCode(String nextAccountId)
		throws Base64.InvalidBase64Exception
	{
		return MartusCrypto.formatPublicCode(MartusCrypto.computePublicCode(nextAccountId));
	}


	// exceptions
	public static class CryptoException extends Exception{}
	public static class CryptoInitializationException extends CryptoException {}
	public static class InvalidKeyPairFileVersionException extends CryptoException {}
	public static class AuthorizationFailedException extends CryptoException {}
	public static class VerifySignatureException extends CryptoException {}
	public static class NoKeyPairException extends CryptoException {}
	public static class EncryptionException extends CryptoException {}
	public static class DecryptionException extends CryptoException {}
	public static class MartusSignatureException extends CryptoException {}
	public static class CreateDigestException extends CryptoException {}
	public static class KeyShareException extends Exception	{}

}
