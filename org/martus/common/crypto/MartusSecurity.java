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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
//logi put back in after 30 day wait import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
//logi put back in after 30 day wait import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.X509V1CertificateGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
//logi put back in after 30 day waitimport org.logi.crypto.Crypto;
//logi put back in after 30 day waitimport org.logi.crypto.secretshare.PolySecretShare;
//logi put back in after 30 day waitimport org.logi.crypto.secretshare.SecretSharingException;
import org.martus.common.MartusConstants;
import org.martus.util.Base64;

import org.martus.util.InputStreamWithSeek;
//logi put back in after 30 day waitimport
/*import org.martus.util.ByteArrayInputStreamWithSeek;
import org.martus.util.StringInputStream;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeStringWriter;
*/
import com.isnetworks.provider.random.InfiniteMonkeyProvider;

public class MartusSecurity extends MartusCryptoImplementation
{
	public MartusSecurity() throws CryptoInitializationException
	{
		if(rand == null)
		{
			Provider monkeys = new InfiniteMonkeyProvider();
			java.security.Security.insertProviderAt(monkeys, 1);
			if(monkeys.getInfo().indexOf("SecureRandom") < 0)
			{
				System.out.println("ERROR: Not using fast random seeding!");
				throw new CryptoInitializationException();
			}
			rand = new SecureRandom();
		}
		initialize(rand);
	}

	synchronized void initialize(SecureRandom rand)throws CryptoInitializationException
	{
		Security.addProvider(new BouncyCastleProvider());

		try
		{
			sigEngine = Signature.getInstance(SIGN_ALGORITHM, "BC");
			rsaCipherEngine = Cipher.getInstance(RSA_ALGORITHM, "BC");
			pbeCipherEngine = Cipher.getInstance(PBE_ALGORITHM, "BC");
			sessionCipherEngine = Cipher.getInstance(SESSION_ALGORITHM, "BC");
			keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM_NAME, "BC");
			sessionKeyGenerator = KeyGenerator.getInstance(SESSION_ALGORITHM_NAME, "BC");
			keyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM, "BC");
		}
		catch(Exception e)
		{
			throw new CryptoInitializationException();
		}
	}

	// begin MartusCrypto interface
	public boolean hasKeyPair()
	{
		return (jceKeyPair != null);
	}

	public void clearKeyPair()
	{
		jceKeyPair = null;
	}

	public void createKeyPair()
	{
		if(bitsInPublicKey != 2048)
			System.out.println("Creating full size public key: " + bitsInPublicKey);
		createKeyPair(bitsInPublicKey);
	}

	public void writeKeyPair(OutputStream outputStream, String passPhrase) throws
			IOException
	{
		writeKeyPair(outputStream, passPhrase, jceKeyPair);
	}

	public void readKeyPair(InputStream inputStream, String passPhrase) throws
		IOException,
		InvalidKeyPairFileVersionException,
		AuthorizationFailedException
	{
		jceKeyPair = null;
		byte versionPlaceHolder = (byte)inputStream.read();
		if(versionPlaceHolder != 0)
			throw (new InvalidKeyPairFileVersionException());

		byte[] plain = decryptKeyPair(inputStream, passPhrase);
		setKeyPairFromData(plain);
	}

	public String getPublicKeyString()
	{
		PublicKey publicKey = getPublicKey();
		return(getKeyString(publicKey));
	}

	public String getPrivateKeyString()
	{
		PrivateKey privateKey = getPrivateKey();
		return(getKeyString(privateKey));
	}

	public byte[] createSignatureOfStream(InputStream inputStream) throws
			MartusSignatureException
	{
		return createSignature(getPrivateKey(), inputStream);
	}

	public boolean verifySignature(InputStream inputStream, byte[] signature) throws
			MartusSignatureException
	{
		return isValidSignatureOfStream(getPublicKey(), inputStream, signature);
	}

	public boolean isValidSignatureOfStream(String publicKeyString, InputStream inputStream, byte[] signature) throws
			MartusSignatureException
	{
		return isValidSignatureOfStream(extractPublicKey(publicKeyString), inputStream, signature);
	}
/*	logi put back in after 30 day wait
	public Vector buildShares(byte[] secretToShare) throws SecretSharingException
	{
		Vector shares = new Vector();
		Crypto.initRandom();
		byte[] paddedSecret = new byte[secretToShare.length + 1];
		System.arraycopy(secretToShare,0,paddedSecret,1,secretToShare.length);
		//We need to pad the secret beginning with a 1 because of a bug found 
		//in the logi encryption algorithm that any secret
		//beginning with a 0 or beginning with a byte > 127 will fail.
		paddedSecret[0] = 1; 
		int minNumber = MartusConstants.minNumberOfFilesNeededToRecreateSecret;
		int numberShares = MartusConstants.numberOfFilesInShare;
		PolySecretShare[] polyShares = PolySecretShare.share(minNumber, numberShares, paddedSecret, 512);
		for (int i = 0 ; i < numberShares; ++i)
		{
			shares.add(polyShares[i].toString());
		}
		return shares;
	}

	public byte[] recoverShares(Vector shares) throws SecretSharingException
	{
		try 
		{
			int numShares = shares.size();
			PolySecretShare[] polyShares = new PolySecretShare[numShares];
			for(int i = 0; i < numShares; ++i)
			{
				polyShares[i] = (PolySecretShare)PolySecretShare.fromString((String)shares.get(i));
			}
			byte[] recoveredSecret = PolySecretShare.retrieve(polyShares);
			//We needed to pad the secret beginning with a 1 because of a bug found 
			//in the logi encryption algorithm that any secret
			//beginning with a 0 or beginning with a byte > 127 will fail.
			int unpaddedLength = recoveredSecret.length - 1;
			byte[] unpaddedSecret = new byte[unpaddedLength];
			System.arraycopy(recoveredSecret,1,unpaddedSecret,0,unpaddedLength);
			return unpaddedSecret;
		} 
		catch (Exception e) 
		{
			throw new SecretSharingException(e.toString());
		}
	}

	private static class KeyShareBundle
	{
		public KeyShareBundle(String publicKeyToUse, byte[] payloadToUse)
		{
			id = MartusConstants.martusSecretShareFileID;
			timeStamp = (new Timestamp(new Date().getTime())).toString();		
			publicKey = publicKeyToUse;
			payload = Base64.encode(payloadToUse);
		}
				
		public KeyShareBundle(String bundleString) throws IOException, KeyShareException
		{
			InputStream in = new StringInputStream(bundleString);
			UnicodeReader reader = new UnicodeReader(in);

			id = reader.readLine();
			if(!id.equals(MartusConstants.martusSecretShareFileID))
				throw new KeyShareException();

			timeStamp = reader.readLine();
			publicKey = reader.readLine();
			sharePiece = reader.readLine();
			payload = reader.readLine();

			in.close();
			reader.close();
		}
		
		public String createBundleString(String sharePieceToWrite) throws IOException
		{
			sharePiece = sharePieceToWrite;
			
			UnicodeStringWriter writer = UnicodeStringWriter.create();
			writer.writeln(id);
			writer.writeln(timeStamp);
			writer.writeln(publicKey);
			writer.writeln(sharePiece);
			writer.writeln(payload);
			writer.close();
			return writer.toString();
		}

		public String id;
		public String timeStamp;
		public String publicKey;
		public String sharePiece;
		public String payload;
	}

	public Vector getKeyShareBundles() 
	{
		Vector shareBundles = new Vector();
		try 
		{
			byte[] sessionKey = createSessionKey();
			Vector sessionKeyShares = buildShares(sessionKey);
			
			ByteArrayInputStream in = new ByteArrayInputStream(getKeyPairData(getKeyPair()));
			ByteArrayOutputStream encryptedKeypair = new ByteArrayOutputStream();
			encrypt(in,encryptedKeypair,sessionKey);	
			encryptedKeypair.close();
			
			KeyShareBundle bundle = new KeyShareBundle(getPublicKeyString(), encryptedKeypair.toByteArray());
			for(int i = 0; i < sessionKeyShares.size(); ++i)
			{
				String thisSharePiece = (String)(sessionKeyShares.get(i));
				shareBundles.add(bundle.createBundleString(thisSharePiece));
			}			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		
		return shareBundles;
	}

	public void recoverFromKeyShareBundles(Vector bundles) throws KeyShareException 
	{
		clearKeyPair();
		
		if(bundles == null)
			throw new KeyShareException();
		if(bundles.size() < MartusConstants.minNumberOfFilesNeededToRecreateSecret)
			throw new KeyShareException();
			
		try 
		{
			Vector shares = new Vector();
			shares = getSharesFromBundles(bundles);
			byte[] encryptedKeyPair = getEncryptedKeyPairFromBundles(bundles);
			decryptAndSetKeyPair(shares, encryptedKeyPair);
		}
		catch  (KeyShareException e)
		{
			throw e;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new KeyShareException();
		}
	}
	
	private Vector getSharesFromBundles(Vector bundles)
		throws  UnsupportedEncodingException, 
				IOException, 
				KeyShareException 
		{
			Vector shares = new Vector();
			for(int i = 0; i < bundles.size(); ++i)
			{
				KeyShareBundle bundle = new KeyShareBundle((String) bundles.get(i));
				shares.add(bundle.sharePiece);
			}
			return shares;
	}
	
	private byte[] getEncryptedKeyPairFromBundles(Vector bundles)
		throws KeyShareException
	{
		InputStream in;
		try 
		{
			KeyShareBundle bundle = new KeyShareBundle((String) bundles.get(0));
			return Base64.decode(bundle.payload);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new KeyShareException();
		}
	}

	private void decryptAndSetKeyPair(Vector shares, byte[] keyPairEncrypted) throws 
						SecretSharingException, 
						DecryptionException,
						IOException,
						AuthorizationFailedException 
	{
		byte[] recoveredSessionKey = recoverShares(shares);
		ByteArrayInputStreamWithSeek inEncryptedKeyPair = new ByteArrayInputStreamWithSeek(keyPairEncrypted);
		ByteArrayOutputStream outDecryptedKeyPair = new ByteArrayOutputStream();
		decrypt( inEncryptedKeyPair, outDecryptedKeyPair, recoveredSessionKey);
		outDecryptedKeyPair.close();
		inEncryptedKeyPair.close();
		setKeyPairFromData(outDecryptedKeyPair.toByteArray());
	}
*/
	public void encrypt(InputStream plainStream, OutputStream cipherStream) throws
			NoKeyPairException,
			EncryptionException
	{
		encrypt(plainStream, cipherStream, createSessionKey());
	}

	public synchronized void encrypt(InputStream plainStream, OutputStream cipherStream, byte[] sessionKeyBytes) throws
			EncryptionException,
			NoKeyPairException
	{
		encrypt(plainStream, cipherStream, sessionKeyBytes, getPublicKey());
	}

	public synchronized void encrypt(InputStream plainStream, OutputStream cipherStream, byte[] sessionKeyBytes, PublicKey publicKey) throws
			EncryptionException,
			NoKeyPairException
	{
		if(publicKey == null)
			throw new NoKeyPairException();

		CipherOutputStream cos = createCipherOutputStream(cipherStream, sessionKeyBytes, getKeyString(publicKey));
		try
		{
			InputStream bufferedPlainStream = new BufferedInputStream(plainStream);

			byte[] buffer = new byte[MartusConstants.streamBufferCopySize];
			int count = 0;
			while( (count = bufferedPlainStream.read(buffer)) >= 0)
			{
				cos.write(buffer, 0, count);
			}

			cos.close();
		}
		catch(Exception e)
		{
			//System.out.println("MartusSecurity.encrypt: " + e);
			throw new EncryptionException();
		}
	}

	public OutputStream createEncryptingOutputStream(OutputStream cipherStream, byte[] sessionKeyBytes)
		throws EncryptionException
	{
		return createCipherOutputStream(cipherStream, sessionKeyBytes, getPublicKeyString());
	}

	public CipherOutputStream createCipherOutputStream(OutputStream cipherStream, byte[] sessionKeyBytes, String publicKeyString)
		throws EncryptionException
	{
		try
		{
			byte[] ivBytes = new byte[IV_BYTE_COUNT];
			rand.nextBytes(ivBytes);

			byte[] encryptedKeyBytes = encryptSessionKey(sessionKeyBytes, publicKeyString);

			SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, SESSION_ALGORITHM_NAME);
			IvParameterSpec spec = new IvParameterSpec(ivBytes);
			sessionCipherEngine.init(Cipher.ENCRYPT_MODE, sessionKey, spec, rand);

			OutputStream bufferedCipherStream = new BufferedOutputStream(cipherStream);
			DataOutputStream output = new DataOutputStream(bufferedCipherStream);
			output.writeInt(encryptedKeyBytes.length);
			output.write(encryptedKeyBytes);
			output.writeInt(ivBytes.length);
			output.write(ivBytes);

			CipherOutputStream cos = new CipherOutputStream(output, sessionCipherEngine);
			return cos;
		}
		catch(Exception e)
		{
			//System.out.println("MartusSecurity.createCipherOutputStream: " + e);
			throw new EncryptionException();
		}
	}

	public synchronized byte[] encryptSessionKey(byte[] sessionKeyBytes, String publicKey) throws
		EncryptionException
	{
		try
		{
			rsaCipherEngine.init(Cipher.ENCRYPT_MODE, extractPublicKey(publicKey), rand);
			byte[] encryptedKeyBytes = rsaCipherEngine.doFinal(sessionKeyBytes);
			return encryptedKeyBytes;
		}
		catch (Exception e)
		{
			//System.out.println("MartusSecurity.encryptSessionKey: " + e);
			throw new EncryptionException();
		}
	}

	public void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream) throws
			NoKeyPairException,
			DecryptionException
	{
		if(getPrivateKey() == null)
			throw new NoKeyPairException();

		decrypt(cipherStream, plainStream, null);
	}

	byte[] readSessionKey(DataInputStream dis) throws DecryptionException
	{
		byte[] encryptedKeyBytes = null;

		try
		{
			int keyByteCount = dis.readInt();
			encryptedKeyBytes = new byte[keyByteCount];
			dis.readFully(encryptedKeyBytes);
		}
		catch(Exception e)
		{
			//System.out.println("MartusSecurity.decrypt: " + e);
			//e.printStackTrace();
			throw new DecryptionException();
		}
		return encryptedKeyBytes;
	}

	public synchronized byte[] decryptSessionKey(byte[] encryptedSessionKeyBytes) throws
		DecryptionException
	{
		try
		{
			byte[] sessionKeyBytes;
			rsaCipherEngine.init(Cipher.DECRYPT_MODE, getPrivateKey(), rand);
			sessionKeyBytes = rsaCipherEngine.doFinal(encryptedSessionKeyBytes);
			return sessionKeyBytes;
		}
		catch(Exception e)
		{
			//System.out.println("MartusSecurity.decryptSessionKey: " + e);
			//e.printStackTrace();
			throw new DecryptionException();
		}
	}

	public synchronized void decrypt(InputStreamWithSeek cipherStream, OutputStream plainStream, byte[] sessionKeyBytes) throws
			DecryptionException
	{
		InputStream cis = createDecryptingInputStream(cipherStream, sessionKeyBytes);
		BufferedOutputStream bufferedPlainStream = new BufferedOutputStream(plainStream);
		try
		{
			final int SIZE = MartusConstants.streamBufferCopySize;
			byte[] chunk = new byte[SIZE];
			int count = 0;
			while((count = cis.read(chunk)) != -1)
			{
				bufferedPlainStream.write(chunk, 0, count);
			}
			cis.close();
			bufferedPlainStream.flush();
		}
		catch(Exception e)
		{
			//System.out.println("MartusSecurity.decrypt: " + e);
			throw new DecryptionException();
		}
	}

	public InputStream createDecryptingInputStream(InputStreamWithSeek cipherStream, byte[] sessionKeyBytes)
		throws	DecryptionException
	{
		try
		{	
			DataInputStream dis = new DataInputStream(cipherStream);
			byte[] storedSessionKey = readSessionKey(dis);
			if(sessionKeyBytes == null)
			{
				sessionKeyBytes = decryptSessionKey(storedSessionKey);
			}

			int ivByteCount = dis.readInt();
			byte[] iv = new byte[ivByteCount];
			dis.readFully(iv);

			SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, SESSION_ALGORITHM_NAME);
			IvParameterSpec spec = new IvParameterSpec(iv);

			sessionCipherEngine.init(Cipher.DECRYPT_MODE, sessionKey, spec, rand);
			CipherInputStream cis = new CipherInputStream(dis, sessionCipherEngine);

			return cis;
		}
		catch(Exception e)
		{
			//System.out.println("MartusSecurity.createCipherInputStream: " + e);
			throw new DecryptionException();
		}
	}

	public synchronized byte[] createSessionKey() throws
			EncryptionException
	{
		sessionKeyGenerator.init(bitsInSessionKey, rand);
		return sessionKeyGenerator.generateKey().getEncoded();
	}

	public synchronized void signatureInitializeSign() throws
			MartusSignatureException
	{
		try
		{
			sigEngine.initSign(getPrivateKey());
		}
		catch(InvalidKeyException e)
		{
			//System.out.println("signatureInitialize :" + e);
			throw(new MartusSignatureException());
		}
	}

	public synchronized void signatureInitializeVerify(String publicKeyString) throws
			MartusSignatureException
	{
		PublicKey publicKey = extractPublicKey(publicKeyString);
		try
		{
			sigEngine.initVerify(publicKey);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			//System.out.println("signatureInitialize :" + e);
			//System.out.println("PublicKeyString : " + publicKeyString);
			throw(new MartusSignatureException());
		}
	}

	public static PublicKey extractPublicKey(String base64PublicKey)
	{
		//System.out.println("key=" + base64PublicKey);
		try
		{
			EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(base64PublicKey));
			KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM_NAME);
			PublicKey publicKey = factory.generatePublic(keySpec);
			return publicKey;
		}
		catch(NoSuchAlgorithmException e)
		{
			System.out.println("MartusSecurity.extractPublicKey: " + e);
		}
		catch(InvalidKeySpecException e)
		{
			//System.out.println("MartusSecurity.extractPublicKey: " + e);
		}
		catch(Base64.InvalidBase64Exception e)
		{
			//System.out.println("MartusSecurity.extractPublicKey: " + e);
		}

		return null;
	}

	public synchronized byte[] signatureGet() throws
			MartusSignatureException
	{
		try
		{
			return sigEngine.sign();
		}
		catch(SignatureException e)
		{
			//System.out.println("signatureGet:" + e);
			throw(new MartusSignatureException());
		}
	}

	public synchronized boolean signatureIsValid(byte[] sig) throws
		MartusSignatureException
	{
		try
		{
			return sigEngine.verify(sig);
		}
		catch(SignatureException e)
		{
			//System.out.println("signatureGet:" + e);
			throw(new MartusSignatureException());
		}
	}

	public synchronized void signatureDigestByte(byte b) throws
			MartusSignatureException
	{
		try
		{
			sigEngine.update(b);
		}
		catch(SignatureException e)
		{
			//System.out.println("signatureGet:" + e);
			throw(new MartusSignatureException());
		}
	}

	public synchronized void signatureDigestBytes(byte[] bytes) throws
			MartusSignatureException
	{
		try
		{
			sigEngine.update(bytes);
		}
		catch(SignatureException e)
		{
			//System.out.println("signatureGet:" + e);
			throw(new MartusSignatureException());
		}
	}

	public String createRandomToken()
	{
		byte[] token = new byte[TOKEN_BYTE_COUNT];
		rand.nextBytes(token);

		return Base64.encode(token);
	}

	public KeyManager [] createKeyManagers() throws Exception
	{
		String passphrase = "this passphrase is never saved to disk";

		KeyStore keyStore = KeyStore.getInstance("BKS", "BC");
		keyStore.load(null, null );
		KeyPair sunKeyPair = createSunKeyPair(bitsInPublicKey);
		RSAPublicKey sslPublicKey = (RSAPublicKey) sunKeyPair.getPublic();
		RSAPrivateCrtKey sslPrivateKey = (RSAPrivateCrtKey)sunKeyPair.getPrivate();


		RSAPublicKey serverPublicKey = (RSAPublicKey)getPublicKey();
		RSAPrivateCrtKey serverPrivateKey = (RSAPrivateCrtKey)getPrivateKey();

		X509Certificate cert0 = createCertificate(sslPublicKey, sslPrivateKey );
		X509Certificate cert1 = createCertificate(sslPublicKey, serverPrivateKey);
		X509Certificate cert2 = createCertificate(serverPublicKey, serverPrivateKey);


		Certificate[] chain = {cert0, cert1, cert2};
		keyStore.setKeyEntry( "cert", sslPrivateKey, passphrase.toCharArray(), chain );

		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		kmf.init( keyStore, passphrase.toCharArray() );
		return kmf.getKeyManagers();
	}

	// end interface

	public PrivateKey getPrivateKey()
	{
		KeyPair pair = getKeyPair();
		if(pair == null)
			return null;
		return pair.getPrivate();
	}

	public PublicKey getPublicKey()
	{
		KeyPair pair = getKeyPair();
		if(pair == null)
			return null;
		return pair.getPublic();
	}

	public void writeKeyPair(OutputStream outputStream, String passPhrase, KeyPair keyPair) throws
			IOException
	{
		byte[] randomSalt = createRandomSalt();
		byte[] keyPairData = getKeyPairData(keyPair);
		byte[] cipherText = pbeEncrypt(keyPairData, passPhrase, randomSalt);
		if(cipherText == null)
			return;
		byte versionPlaceHolder = 0;
		outputStream.write(versionPlaceHolder);
		outputStream.write(randomSalt);
		outputStream.write(cipherText);
		outputStream.flush();
	}

	public byte[] getKeyPairData(KeyPair keyPair) throws IOException
	{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(data);
		objectOutputStream.writeObject(keyPair);
		return data.toByteArray();
	}

	public void setKeyPairFromData(byte[] data) throws
		AuthorizationFailedException
	{
		jceKeyPair = null;
		try
		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			KeyPair candidatePair = (KeyPair)objectInputStream.readObject();
			if(!isKeyPairValid(candidatePair))
				throw (new AuthorizationFailedException());
			jceKeyPair = candidatePair;
		}
		catch(Exception e)
		{
			//System.out.println("setKeyPairFromData: " + e);
			throw (new AuthorizationFailedException());
		}
	}

	public KeyPair getKeyPair()
	{
		return jceKeyPair;
	}


	public byte[] createRandomSalt()
	{
		byte[] salt = new byte[SALT_BYTE_COUNT];
		rand.nextBytes(salt);
		return salt;
	}

	public synchronized boolean isKeyPairValid(KeyPair candidatePair)
	{
		if(candidatePair == null)
			return false;

		try
		{
			rsaCipherEngine.init(Cipher.ENCRYPT_MODE, candidatePair.getPublic(), rand);
			byte[] samplePlainText = {1,2,3,4,127};
			byte[] cipherText = rsaCipherEngine.doFinal(samplePlainText);
			rsaCipherEngine.init(Cipher.DECRYPT_MODE, candidatePair.getPrivate(), rand);
			byte[] result = rsaCipherEngine.doFinal(cipherText);
			if(!Arrays.equals(samplePlainText, result))
				return false;
		}
		catch(Exception e)
		{
			return false;
		}

		return true;
	}

	public byte[] decryptKeyPair(InputStream inputStream, String passPhrase) throws IOException
	{
		byte[] salt = new byte[SALT_BYTE_COUNT];
		inputStream.read(salt);

		byte[] cipherText = new byte[inputStream.available()];
		inputStream.read(cipherText);

		return pbeDecrypt(cipherText, passPhrase, salt);
	}

	public byte[] pbeEncrypt(byte[] inputText, String passPhrase, byte[] salt)
	{
		return pbeEncryptDecrypt(Cipher.ENCRYPT_MODE, inputText, passPhrase, salt);
	}

	public byte[] pbeDecrypt(byte[] inputText, String passPhrase, byte[] salt)
	{
		return pbeEncryptDecrypt(Cipher.DECRYPT_MODE, inputText, passPhrase, salt);
	}

	private synchronized byte[] pbeEncryptDecrypt(int mode, byte[] inputText, String passPhrase, byte[] salt)
	{
		char[] passPhraseChars = new char[passPhrase.length()];
		passPhrase.getChars(0, passPhrase.length(), passPhraseChars, 0);

		try
		{
			PBEKeySpec keySpec = new PBEKeySpec(passPhraseChars);
			SecretKey key = keyFactory.generateSecret(keySpec);
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATION_COUNT);

			pbeCipherEngine.init(mode, key, paramSpec, rand);
			byte[] outputText = pbeCipherEngine.doFinal(inputText);
			return outputText;
		}
		catch(Exception e)
		{
			//System.out.println("pbeEncryptDecrypt: " + e);
		}

		return null;
	}

	public synchronized void createKeyPair(int publicKeyBits)
	{
		try
		{
			jceKeyPair = null;
			keyPairGenerator.initialize(publicKeyBits, rand);
			jceKeyPair = keyPairGenerator.genKeyPair();
		}
		catch(Exception e)
		{
			System.out.println("createKeyPair " + e);
		}

	}

	BigInteger createCertificateSerialNumber()
	{
		return new BigInteger(128, rand);
	}

	synchronized byte[] createSignature(PrivateKey privateKey, InputStream inputStream) throws
			MartusSignatureException
	{
		try
		{
			sigEngine.initSign(privateKey);
			accumulateForSignOrVerify(inputStream);
			return sigEngine.sign();
		}
		catch (Exception e)
		{
			//System.out.println("createSignature :" + e);
			throw(new MartusSignatureException());
		}
	}

	public synchronized boolean isValidSignatureOfStream(PublicKey publicKey, InputStream inputStream, byte[] signature) throws
			MartusSignatureException
	{
		try
		{
			sigEngine.initVerify(publicKey);
			accumulateForSignOrVerify(inputStream);
			return sigEngine.verify(signature);
		}
		catch (Exception e)
		{
			//System.out.println("verifySignature :" + e);
			throw(new MartusSignatureException());
		}
	}

	synchronized KeyPair createSunKeyPair(int bitsInKey) throws Exception
	{
		KeyPairGenerator sunKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
    	sunKeyPairGenerator.initialize( bitsInKey );
		KeyPair sunKeyPair = sunKeyPairGenerator.genKeyPair();
		return sunKeyPair;
	}

	public X509Certificate createCertificate(RSAPublicKey publicKey, RSAPrivateCrtKey privateKey)
			throws SecurityException, SignatureException, InvalidKeyException
	{
		Hashtable attrs = new Hashtable();

		Vector ord = new Vector();
		Vector values = new Vector();

		ord.addElement(X509Principal.C);
		ord.addElement(X509Principal.O);
		ord.addElement(X509Principal.L);
		ord.addElement(X509Principal.ST);
		ord.addElement(X509Principal.EmailAddress);

		final String certificateCountry = "US";
		final String certificateOrganization = "Benetech";
		final String certificateLocation = "Palo Alto";
		final String certificateState = "CA";
		final String certificateEmail = "martus@benetech.org";

		values.addElement(certificateCountry);
		values.addElement(certificateOrganization);
		values.addElement(certificateLocation);
		values.addElement(certificateState);
		values.addElement(certificateEmail);

		attrs.put(X509Principal.C, certificateCountry);
		attrs.put(X509Principal.O, certificateOrganization);
		attrs.put(X509Principal.L, certificateLocation);
		attrs.put(X509Principal.ST, certificateState);
		attrs.put(X509Principal.EmailAddress, certificateEmail);

		// create a certificate
		X509V1CertificateGenerator  certGen1 = new X509V1CertificateGenerator();

		certGen1.setSerialNumber(createCertificateSerialNumber());
		certGen1.setIssuerDN(new X509Principal(ord, attrs));
		certGen1.setNotBefore(new Date(System.currentTimeMillis() - 50000));
		certGen1.setNotAfter(new Date(System.currentTimeMillis() + 50000));
		certGen1.setSubjectDN(new X509Principal(ord, values));
		certGen1.setPublicKey( publicKey );
		certGen1.setSignatureAlgorithm("MD5WithRSAEncryption");

		// self-sign it
		X509Certificate cert = certGen1.generateX509Certificate( privateKey );
		return cert;
	}

	public static String createDigestString(String inputText) throws CreateDigestException 
	{
		try
		{
			byte[] bytesToDigest = inputText.getBytes("UTF-8");
			byte[] result = createDigest(bytesToDigest);
			return Base64.encode(result);
		}
		catch (Exception e)
		{
			throw new CreateDigestException();
		}
	}

	public static byte[] createDigest(byte[] bytesToDigest)
		throws NoSuchAlgorithmException, IOException
	{
		ByteArrayInputStream in = new ByteArrayInputStream(bytesToDigest);
		byte[] result = createDigest(in);
		in.close();
		return result;
	}

	private static byte[] createDigest(ByteArrayInputStream in)
		throws NoSuchAlgorithmException, IOException
	{
		MessageDigest digester = MessageDigest.getInstance(DIGEST_ALGORITHM);
		digester.reset();
		int got;
		byte[] bytes = new byte[MartusConstants.digestBufferSize];
		while( (got=in.read(bytes)) >= 0)
			digester.update(bytes, 0, got);
		return digester.digest();
	}

	public byte[] getDigestOfPartOfPrivateKey() throws CreateDigestException
	{
		byte[] privateKey = getPrivateKey().getEncoded();
		byte[] quarter = new byte[privateKey.length / 4];
		for(int i=0; i < quarter.length; ++i)
			quarter[i] = privateKey[i*4];
		try
		{
			return createDigest(quarter);
		}
		catch (Exception e)
		{
			throw new CreateDigestException();
		}
	}
	
	static public String getKeyString(Key key)
	{
		return Base64.encode(key.getEncoded());
	}
	
	static public String geEncryptedFileIdentifier()
	{
		return ENCRYPTED_FILE_VERSION_IDENTIFIER;
	}

	protected synchronized void accumulateForSignOrVerify(InputStream in) throws
					IOException,
					MartusSignatureException
	{
		try
		{
			int got;
			byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
			while( (got=in.read(bytes)) >= 0)
				sigEngine.update(bytes, 0, got);
		}
		catch(java.security.SignatureException e)
		{
			throw new MartusSignatureException();
		}
	}

	private static final String SESSION_ALGORITHM_NAME = "AES";
	private static final String SESSION_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final String RSA_ALGORITHM_NAME = "RSA";
	private static final String RSA_ALGORITHM = "RSA/NONE/PKCS1Padding";
	private static final String PBE_ALGORITHM = "PBEWithSHAAndTwofish-CBC";
	private static final String SIGN_ALGORITHM = "SHA1WithRSA";
	private static final String DIGEST_ALGORITHM = "SHA1";
	private static final String ENCRYPTED_FILE_VERSION_IDENTIFIER = "Martus Encrypted File Version 001";

	private static final int bitsInSessionKey = 256;
	private static final int bitsInPublicKey = 2048;
	private static final int SALT_BYTE_COUNT = 8;
    private static final int ITERATION_COUNT = 1000;
	private static final int IV_BYTE_COUNT = 16;	// from the book
	private static final int TOKEN_BYTE_COUNT = 16; //128 bits
	private static SecureRandom rand;
	private KeyPair jceKeyPair;

	private Signature sigEngine;
	private Cipher rsaCipherEngine;
	private Cipher pbeCipherEngine;
	private Cipher sessionCipherEngine;
	private KeyGenerator sessionKeyGenerator;
	private KeyPairGenerator keyPairGenerator;
	private SecretKeyFactory keyFactory;

}
