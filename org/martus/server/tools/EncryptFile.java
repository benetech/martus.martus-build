package org.martus.server.tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;

import org.martus.common.Base64;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.server.forclients.MartusServerUtilities;

public class EncryptFile
{
	public static void main(String[] args)
	{
		File publicKeyFile = null;
		File plainTextFile = null;
		File cryptoFile = null;
		MartusSecurity security = null;
		
		for (int i = 0; i < args.length; i++)
		{			
			if(args[i].startsWith("--pubkey"))
			{
				publicKeyFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--plaintext-file"))
			{
				plainTextFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if(args[i].startsWith("--crypto-file"))
			{
				cryptoFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
		}
		
		if(publicKeyFile == null || plainTextFile == null)
		{
			System.err.println("Incorrect arguments: EncryptFile --pubkey=<pubkey.dat> --plaintext-file=<input> --crypto-file=<output>");
			System.exit(2);
		}
		
		try
		{
			security = new MartusSecurity();
		}
		catch (CryptoInitializationException e)
		{
			System.err.println("EncryptFile.main: " + e);
			System.exit(3);
		}
		
		InputStream plainStream = null;
		ByteArrayOutputStream cipherByteArrayOutputStream = null;
		try
		{
			plainStream = new BufferedInputStream(new FileInputStream(plainTextFile));
			cipherByteArrayOutputStream = new ByteArrayOutputStream();
			
			String publicKeyString = (String) MartusUtilities.importServerPublicKeyFromFile(publicKeyFile, security).get(0);
			PublicKey publicKey = MartusSecurity.extractPublicKey(publicKeyString);
			
			security.encrypt(plainStream, cipherByteArrayOutputStream, security.createSessionKey(), publicKey);
			String encodedEncryptedFile = Base64.encode(cipherByteArrayOutputStream.toByteArray());
			
			String fileContents = MartusServerUtilities.getFileContents(plainTextFile);
			String digest = MartusSecurity.createDigestString(fileContents);
			
			StringBuffer sbuffer = new StringBuffer();
			sbuffer.append(MartusSecurity.geEncryptedFileIdentifier());
			sbuffer.append("\n");
			sbuffer.append(publicKeyString);
			sbuffer.append("\n");
			sbuffer.append(digest);
			sbuffer.append("\n");
			sbuffer.append(encodedEncryptedFile);
			sbuffer.append("\n");
			
			writeEncryptedFile(cryptoFile, sbuffer.toString());
		}
		catch (Exception e)
		{
			System.err.println("EncryptFile.main: " + e);
			e.printStackTrace();
			System.exit(3);
		}
		finally
		{
			try
			{
				cipherByteArrayOutputStream.close();
				plainStream.close();
			}
			catch(IOException ignoredException)
			{}
		}

		System.exit(0);
	}
	
	public static void writeEncryptedFile(File cryptoFile, String fileContent)
		throws FileNotFoundException, IOException, UnsupportedEncodingException
	{
		FileOutputStream cipherStream = new FileOutputStream(cryptoFile);
		cipherStream.write(fileContent.getBytes("UTF-8"));
		cipherStream.close();
	}
}
