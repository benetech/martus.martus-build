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

package org.martus.common.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.util.Base64;
import org.martus.util.ByteArrayInputStreamWithSeek;


public class TestMartusSecurity extends TestCaseEnhanced
{
	public TestMartusSecurity(String name)
	{
		super(name);
		VERBOSE = false;
	}

	public void setUp() throws Exception
	{
		TRACE_BEGIN("setUp");
		if(security == null)
			security = new MartusSecurity();

		if(security.getKeyPair() == null)
		{
			security.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
			assertNotNull("setup1: KeyPair returned NULL", security.getKeyPair());

			MartusSecurity otherSecurity = new MartusSecurity();
			otherSecurity.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
			assertNotNull("setup2: KeyPair returned NULL", security.getKeyPair());

			invalidKeyPair = new KeyPair(security.getPublicKey(), otherSecurity.getPrivateKey());
			assertNotNull("setup3: KeyPair returned NULL", security.getKeyPair());
		}
		assertNotNull("setup4: KeyPair returned NULL", security.getKeyPair());
		if(securityWithoutKeyPair == null)
		{
			securityWithoutKeyPair = new MartusSecurity();
		}
		assertNotNull("setup: security NULL", security);
		assertNotNull("setup: KeyPair returned NULL", security.getKeyPair());
		assertNotNull("setup: Key returned NULL", security.getPrivateKey());
		TRACE_END();
	}

	public void tearDown()
	{
	}

	public void testGetDigestOfPartOfPrivateKey() throws Exception
	{
		MartusCrypto knownKey = MockMartusSecurity.createClient();
		String digest = Base64.encode(knownKey.getDigestOfPartOfPrivateKey());
		assertEquals("PY7HmxJgqLy76WNx3mKfaNnxFc8=", digest);
	}

	public void testPbe()
	{
		TRACE_BEGIN("testPbe");
		byte[] original = {65,66,67,78,79};
		String passPhrase = "secret";

		byte[] salt = security.createRandomSalt();

		byte[] encoded = security.pbeEncrypt(original, passPhrase, salt);
		byte[] decoded = security.pbeDecrypt(encoded, passPhrase, salt);
		assertTrue("should work", Arrays.equals(original, decoded));
		TRACE_END();
	}

	public void testCreateKeyPair()
	{
		TRACE_BEGIN("testCreateKeyPair");
		assertNull("start with no key pair", securityWithoutKeyPair.getKeyPair());

		KeyPair keyPair = security.getKeyPair();
		assertNotNull("got a key pair", keyPair);

		RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
		BigInteger publicExp = publicKey.getPublicExponent();
		assertTrue("public non-zero", publicExp.bitLength() != 0);

		RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
		BigInteger privateExp = privateKey.getPrivateExponent();
		assertTrue("private non-zero", privateExp.bitLength() != 0);

		String	account = security.getPublicKeyString();
		byte[] publicKeyBytes = publicKey.getEncoded();
		String publicKeyString = Base64.encode(publicKeyBytes);
		assertEquals("Public Key doesn't Match", publicKeyString, account);
		TRACE_END();
	}

	public void testWriteKeyPairBadStream()
	{
		TRACE_BEGIN("testWriteKeyPairBadStream");
		try
		{
			security.writeKeyPair(null, "whatever");
			fail("expected an exception");
		}
		catch(Exception e)
		{
			// expected
		}
		TRACE_END();
	}

	public void testWriteKeyPairFormat() throws Exception
	{
		TRACE_BEGIN("testWriteKeyPairFormat");
		String passPhrase = "whatever";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		security.writeKeyPair(outputStream, passPhrase);
		byte[] encryptedData = outputStream.toByteArray();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedData);
		byte versionPlaceHolder = (byte)inputStream.read();
		assertEquals("Expected 0", 0, versionPlaceHolder);

		byte[] plain = security.decryptKeyPair(inputStream, passPhrase);
		ByteArrayInputStream inputStream2 = new ByteArrayInputStream(plain);
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream2);
		objectInputStream.readObject();
		TRACE_END();
	}

	public void testReadKeyPairIncorrectVersion() throws Exception
	{
		TRACE_BEGIN("testReadKeyPairIncorrectVersion");
		String passPhrase = "newpassphase";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		security.writeKeyPair(outputStream, passPhrase);
		byte[] encryptedData = outputStream.toByteArray();

		MartusSecurity tempSecurity = new MartusSecurity();

		encryptedData[0] = 127;
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedData);
		try
		{
			tempSecurity.readKeyPair(inputStream, passPhrase);
			fail("Should have thrown exception incorrect version");
		}
		catch (MartusSecurity.InvalidKeyPairFileVersionException e)
		{
			//Expected Exception
		}

		TRACE_END();
	}

	public void testGetAndSetKeyPair() throws Exception
	{
		TRACE_BEGIN("testWriteAndReadKeyPair");
		KeyPair keyPair = security.getKeyPair();
		assertEquals("no change", keyPair, security.getKeyPair());
		byte[] data = security.getKeyPairData(keyPair);
		assertTrue("byte compare", Arrays.equals(data, security.getKeyPairData(keyPair)));

		MartusSecurity tempSecurity = new MartusSecurity();
		tempSecurity.setKeyPairFromData(data);
		KeyPair gotKeyPair = tempSecurity.getKeyPair();
		assertNotNull("get/set null", tempSecurity);
		assertEquals("get/set public", keyPair.getPublic(), gotKeyPair.getPublic());
		assertEquals("get/set private", keyPair.getPrivate(), gotKeyPair.getPrivate());

		String publicKeyString = security.getPublicKeyString();
		PublicKey publicKey = MartusSecurity.extractPublicKey(publicKeyString);
		assertEquals("get/extract failed?", publicKey, security.getPublicKey());
	}

	public void testWriteAndReadKeyPair() throws Exception
	{
		String passPhrase = "My dog has fleas";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		security.writeKeyPair(outputStream, passPhrase);
		byte[] bytes = outputStream.toByteArray();
		assertTrue("empty", bytes.length > 0);

		MartusSecurity tempSecurity = new MartusSecurity();

		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			tempSecurity.readKeyPair(inputStream, passPhrase);
			KeyPair oldKeyPair = security.getKeyPair();
			KeyPair gotKeyPair = tempSecurity.getKeyPair();
			assertNotNull("good null", gotKeyPair);
			assertEquals("good public", oldKeyPair.getPublic(), gotKeyPair.getPublic());
			assertEquals("good private", oldKeyPair.getPrivate(), gotKeyPair.getPrivate());

			try
			{
				tempSecurity.readKeyPair(inputStream, passPhrase);
				fail("Reading eof should have thrown an exception");
			}
			catch(Exception e)
			{
				//This is an expected exception
			}
			assertNull("past eof", tempSecurity.getKeyPair());
		}

		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			try
			{
				tempSecurity.readKeyPair(inputStream, passPhrase + passPhrase);
			}
			catch (MartusSecurity.AuthorizationFailedException e)
			{
				//Expected exception
			}
			assertNull("bad passphrase", tempSecurity.getKeyPair());
		}
		TRACE_END();
	}

	public void testVerifyDuringReadKeyPair() throws Exception
	{
		TRACE_BEGIN("testVerifyDuringReadKeyPair");
		String passPhrase = "Let's put on a show!";
		KeyPair mismatched = invalidKeyPair;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		security.writeKeyPair(outputStream, passPhrase, mismatched);

		MartusSecurity tempSecurity = new MartusSecurity();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		try
		{
			tempSecurity.readKeyPair(inputStream, passPhrase);
		}
		catch(MartusSecurity.AuthorizationFailedException e)
		{
			//Expecting exception here
		}
		assertNull("Shouldn't accept invalid pair", tempSecurity.getKeyPair());
		TRACE_END();
	}

	public void testIsKeyPairValid()
	{
		TRACE_BEGIN("testIsKeyPairValid");
		assertEquals("null", false, securityWithoutKeyPair.isKeyPairValid((KeyPair)null));
		assertEquals("created", true, security.isKeyPairValid(security.getKeyPair()));
		assertEquals("invalid", false, security.isKeyPairValid(invalidKeyPair));
		TRACE_END();
	}

	public void testPrivateKey()
	{
		TRACE_BEGIN("testPrivateKey");
		assertNull("No Key", securityWithoutKeyPair.getPrivateKey());
		assertNotNull("Key returned NULL", security.getPrivateKey());
		TRACE_END();
	}

	public void testPublicKey()
	{
		TRACE_BEGIN("testPublicKey");
		assertNull("no key should return null key", securityWithoutKeyPair.getPublicKey());
		assertNotNull("Key returned NULL?", security.getPublicKey());

		try
		{
			securityWithoutKeyPair.getPublicKeyString();
			fail("Should have thrown null exception");
		}
		catch(NullPointerException e)
		{
			//expected exception
		}

		String publicKeyString = security.getPublicKeyString();
		assertNotNull("no key string?", security.getPublicKeyString());
		PublicKey publicKey = MartusSecurity.extractPublicKey(publicKeyString);
		assertNotNull("extract failed?", publicKey);
		TRACE_END();
	}

	public void testExtractPublicKey() throws Exception
	{
		assertNull("not base64", MartusSecurity.extractPublicKey("not Base64"));
		assertNull("not valid key", MartusSecurity.extractPublicKey(Base64.encode(new byte[] {1,2,3})));
	}

	public void testCreateSignature() throws MartusSecurity.MartusSignatureException
	{
		TRACE_BEGIN("testCreateSignature");
		byte[] data = createSampleData(1000);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		try
		{
			securityWithoutKeyPair.createSignatureOfStream(inputStream);
			fail("Signature passed with no key pair");
		}
		catch (MartusSecurity.MartusSignatureException e)
		{
			//Expected Exception
		}

		byte[] signature = security.createSignatureOfStream(inputStream);
		assertNotNull("signature was null", signature);

		TRACE_END();
	}

	private byte[] createSampleData(int size)
	{
		byte[] data = new byte[size];
		for(int i = 0 ; i < data.length; ++i)
		{
			data[i]= (byte)(i%100);
		}
		return data;
	}

//	public void testBouncySignatureSpeed() throws Exception
//	{
//		SecureRandom rand = new SecureRandom();
//		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
//
//		int[] keySizes = {1024, 1200, 1400, 1600, 1800, 2048};
//		for(int i=0; i < keySizes.length; ++i)
//		{
//			keyPairGenerator.initialize(keySizes[i], rand);
//			KeyPair keyPair = keyPairGenerator.genKeyPair();
//
//			Signature sigEngine;
//			sigEngine = Signature.getInstance("SHA1WithRSA", "BC");
//			sigEngine.initSign(keyPair.getPrivate(), rand);
//			sigEngine.update((byte)0);
//			long start = System.currentTimeMillis();
//			sigEngine.sign();
//			long stop = System.currentTimeMillis();
//			System.out.println("3-step sig " + keySizes[i] + " of one byte: " + (stop - start) + " ms");
//		}
//	}

	public void testVerifySignature() throws Exception
	{
		TRACE_BEGIN("testVerifySignature");
		byte[] data = createRandomBytes(1000);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		byte[] signature = security.createSignatureOfStream(inputStream);
		assertNotNull("signature was null", signature);

		ByteArrayInputStream verifyStream = new ByteArrayInputStream(data);
		assertEquals("Verify failed", true, security.verifySignature(verifyStream, signature));

		data[0] = (byte)(~data[0]);
		ByteArrayInputStream corruptStream = new ByteArrayInputStream(data);
		assertEquals("Verify passed on corrupt data", false, security.verifySignature(corruptStream, signature));
		TRACE_END();
	}

	public void testSignatureInitializeVerify()
	{
		TRACE_BEGIN("testVerifySignature");
		try
		{
			security.signatureInitializeVerify("not a valid key string");
			fail("should have thrown");
		}
		catch(MartusSignatureException ignoreThisExpectedException)
		{
		}
		TRACE_END();
	}

	public void testEncryptWithoutKeyPair() throws Exception
	{
		TRACE_BEGIN("testEncryptWithoutKeyPair");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			securityWithoutKeyPair.encrypt(inputStream, outputStream);
			fail("encrypt without keypair worked?");
		}
		catch(MartusSecurity.NoKeyPairException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testEncryptBadStreams() throws Exception
	{
		TRACE_BEGIN("testEncryptBadStreams");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			security.encrypt(null, outputStream);
			fail("encrypt with null input worked?");
		}
		catch(MartusSecurity.EncryptionException e)
		{
			// expected exception
		}
		try
		{
			security.encrypt(inputStream, null);
			fail("encrypt with null output worked?");
		}
		catch(MartusSecurity.EncryptionException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testDecryptWithoutKeyPair() throws Exception
	{
		TRACE_BEGIN("testDecryptWithoutKeyPair");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			securityWithoutKeyPair.decrypt(inputStream, outputStream);
			fail("decrypt without keypair worked?");
		}
		catch(MartusSecurity.NoKeyPairException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testDecryptBadStreams() throws Exception
	{
		TRACE_BEGIN("testDecryptBadStreams");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			security.decrypt(null, outputStream);
			fail("decrypt with null input worked?");
		}
		catch(MartusSecurity.DecryptionException e)
		{
			// expected exception
		}
		try
		{
			security.decrypt(inputStream, null);
			fail("decrypt with null output worked?");
		}
		catch(MartusSecurity.DecryptionException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testEncryptAndDecrypt() throws Exception
	{
		TRACE_BEGIN("testEncryptAndDecrypt");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStream plainInputStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream cipherOutputStream = new ByteArrayOutputStream();

		security.encrypt(plainInputStream, cipherOutputStream);
		byte[] encrypted = cipherOutputStream.toByteArray();
		assertTrue("unreasonably short", encrypted.length > (9 * data.length) / 10);
		assertTrue("unreasonably long", encrypted.length < 3 * data.length);
		assertEquals("not encrypted?", false, Arrays.equals(data, encrypted));

		ByteArrayInputStreamWithSeek cipherInputStream = new ByteArrayInputStreamWithSeek(encrypted);
		ByteArrayOutputStream plainOutputStream = new ByteArrayOutputStream();
		security.decrypt(cipherInputStream, plainOutputStream);
		byte[] decrypted = plainOutputStream.toByteArray();
		assertEquals("got bad data back", true, Arrays.equals(data, decrypted));

		TRACE_END();
	}

	private byte[] createRandomBytes(int length)
	{
		byte[] data = new byte[length];
		for(int i = 0 ; i < data.length; ++i)
		{
			data[i]= (byte)(i%100);
		}

		return data;
	}


	public void testDigestString() throws Exception
	{
		final String textToDigest = "This is a some text";
		long start = System.currentTimeMillis();
		String digest = MartusSecurity.createDigestString(textToDigest);
		long stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertNotNull("null digest", digest);

		String digest2 = MartusSecurity.createDigestString(textToDigest);
		assertEquals("different?", digest, digest2);

		//String otherText = textToDigest.replaceFirst("i", "j");
		// rewrite above line in java 1.3 compatible form:
		String otherText = textToDigest; // first assume no match found
		int idx = textToDigest.indexOf("i");
		if (idx >= 0)
			otherText = textToDigest.substring(0, idx) + "j" + textToDigest.substring(idx+1);

		String digest3 = MartusSecurity.createDigestString(otherText);
		assertNotEquals("same?", digest, digest3);
	}

	public void testCreateRandomToken()
	{
		String token1 = security.createRandomToken();
		assertEquals("Invalid Length?", 24, token1.length());
		String token2 = security.createRandomToken();
		assertNotEquals("Same token?", token1, token2);
	}
/*
	public void testSignatures()
	{
		MartusSecurity security = new MartusSecurity(12345);

		assertEquals("default empty string", "", security.createSignature(null, null));

		final String textToSign = "This is just some stupid text";
		KeyPair keys = security.createKeyPair();
		long start = System.currentTimeMillis();
		String signature = security.createSignature(textToSign, keys.getPrivate());
		long stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertNotNull("null sig?", signature);

		start = System.currentTimeMillis();
		boolean shouldWork = security.verifySignature(textToSign, keys.getPublic(), signature);
		stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertTrue("should have worked", shouldWork);

		start = System.currentTimeMillis();
		boolean shouldFail = security.verifySignature("Not the same text", keys.getPublic(), signature);
		stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertTrue("should have failed", !shouldFail);

		start = System.currentTimeMillis();
		boolean shouldFail2 = security.verifySignature(textToSign, keys.getPublic(), "not the sig");
		stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertTrue("should have failed2", !shouldFail2);
	}
*/
/* experimental
	public void testCipherSimple() throws Exception
	{
		Key key = createSessionKey();

		byte[] original = {1,1,2,3,5,7,11};
		byte[] encrypted = MartusSecurity.encrypt(original, key);
		assertNotNull("null encrypt", encrypted);
		assertTrue("empty encrypt", encrypted.length > 0);
		byte[] decrypted = MartusSecurity.decrypt(encrypted, key);
		assertNotNull("null decrypt", decrypted);
		assertTrue("empty decrypt", decrypted.length > 0);

		assertTrue("symmetric encrypt/decrypt", Arrays.equals(original, decrypted));
	}

	public void testCipherLarge() throws Exception
	{
		Key key = createSessionKey();

		final int SIZE = 100000;
		byte[] original = new byte[SIZE];
		for(int b = 0; b < SIZE; ++b)
		{
			original[b] = (byte)(b%256);
		}

		long startE = System.currentTimeMillis();
		byte[] encrypted = MartusSecurity.encrypt(original, key);
		long stopE = System.currentTimeMillis();
		assertNotNull("null encrypt", encrypted);
		assertTrue("empty encrypt", encrypted.length > 0);
		long startD = System.currentTimeMillis();
		byte[] decrypted = MartusSecurity.decrypt(encrypted, key);
		long stopD = System.currentTimeMillis();
		assertNotNull("null decrypt", decrypted);
		assertTrue("empty decrypt", decrypted.length > 0);
		assertTrue("symmetric encrypt/decrypt", Arrays.equals(original, decrypted));
		//System.out.println("Encrypt " + SIZE + " bytes: " + (stopE - startE));
		//System.out.println("Decrypt " + SIZE + " bytes: " + (stopD - startD));
		assertTrue("encrypt slow", (stopE - startE) < 1000);
		assertTrue("decrypt slow", (stopD - startD) < 1000);
	}

	public void testCipherMany() throws Exception
	{
		Key key = createSessionKey();

		final int SIZE = 100;
		byte[] original = new byte[SIZE];
		for(int b = 0; b < SIZE; ++b)
		{
			original[b] = (byte)(b%256);
		}

		final int TIMES = 1000;
		long startE = System.currentTimeMillis();
		byte[] encrypted = null;
		for(int e = 0; e < TIMES; ++e)
			encrypted = MartusSecurity.encrypt(original, key);
		long stopE = System.currentTimeMillis();
		assertNotNull("null encrypt", encrypted);
		assertTrue("empty encrypt", encrypted.length > 0);
		byte[] decrypted = null;
		long startD = System.currentTimeMillis();
		for(int d = 0; d < TIMES; ++d)
			decrypted = MartusSecurity.decrypt(encrypted, key);
		long stopD = System.currentTimeMillis();
		assertNotNull("null decrypt", decrypted);
		assertTrue("empty decrypt", decrypted.length > 0);
		assertTrue("symmetric encrypt/decrypt", Arrays.equals(original, decrypted));
		//System.out.println("Encrypt " + TIMES + " times: " + (stopE - startE));
		//System.out.println("Decrypt " + TIMES + " times: " + (stopD - startD));
		assertTrue("encrypt slow", (stopE - startE) < 1000);
		assertTrue("decrypt slow", (stopD - startD) < 1000);
	}

	private Key createSessionKey()
	{
		MartusSecurity security = new MartusSecurity(12345);

		long start = System.currentTimeMillis();
		Key key = security.createSessionKey();
		long stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertNotNull("null session key", key);

		return key;
	}
*/

	private static MartusSecurity security;
	private static MartusSecurity securityWithoutKeyPair;
	private static KeyPair invalidKeyPair;
	final int SMALLEST_LEGAL_KEY_FOR_TESTING = 512;

}
