/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.net.ssl.KeyManager;

public interface MartusCrypto
{
	public boolean hasKeyPair();
	public void clearKeyPair();
	public void createKeyPair();

	public void writeKeyPair(OutputStream outputStream, String passPhrase) throws
		IOException;

	public void readKeyPair(InputStream inputStream, String passPhrase) throws
		IOException,
		InvalidKeyPairFileVersionException,
		AuthorizationFailedException;
		
	public String getPublicKeyString();

	public byte[] createSignature(InputStream inputStream) throws
		MartusSignatureException;

	public boolean isSignatureValid(String publicKeyString, InputStream inputStream, byte[] signature) throws
		MartusSignatureException;

	public void signatureInitializeSign() throws 
		MartusSignatureException;

	public void signatureDigestByte(byte b) throws 
		MartusSignatureException;

	public void signatureDigestBytes(byte[] bytes) throws
			MartusSignatureException;

	public byte[] signatureGet() throws 
		MartusSignatureException;
		
	public void signatureInitializeVerify(String publicKey) throws
		MartusSignatureException;
	
	public boolean signatureIsValid(byte[] sig) throws 
		MartusSignatureException;
	

	public byte[] createSessionKey() throws
			EncryptionException;
			
	public void encrypt(InputStream plainStream, OutputStream cipherStream, byte[] sessionKeyBytes) throws 
			EncryptionException,
			NoKeyPairException;
			
	public void encrypt(InputStream plainStream, OutputStream cipherStream) throws
			NoKeyPairException,
			EncryptionException;

	public byte[] encryptSessionKey(byte[] sessionKeyBytes, String publicKey) throws 
		EncryptionException;
		
	public void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream, byte[] sessionKeyBytes) throws 
			DecryptionException; 

	public void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream) throws
			NoKeyPairException,
			DecryptionException;

	public byte[] decryptSessionKey(byte[] encryptedSessionKeyBytes) throws 
		DecryptionException;

	public CipherOutputStream createCipherOutputStream(OutputStream cipherStream, byte[] sessionKeyBytes)
		throws EncryptionException;

	public CipherInputStream createCipherInputStream(InputStreamWithSeek cipherStream, byte[] sessionKeyBytes)
		throws	DecryptionException;

	public String createRandomToken();
	
	public KeyManager [] createKeyManagers() throws Exception;

	public static class CryptoException extends Exception{}
	public static class CryptoInitializationException extends CryptoException {}
	public static class InvalidKeyPairFileVersionException extends CryptoException {}
	public static class AuthorizationFailedException extends CryptoException {}
	public static class VerifySignatureException extends CryptoException {}
	public static class NoKeyPairException extends CryptoException {}
	public static class EncryptionException extends CryptoException {}
	public static class DecryptionException extends CryptoException {}
	public static class MartusSignatureException extends CryptoException {}

}
