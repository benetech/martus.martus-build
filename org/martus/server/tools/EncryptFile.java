package org.martus.server.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;

import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusCrypto.CryptoInitializationException;

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
		
		try
		{
			InputStream plainStream = new BufferedInputStream(new FileInputStream(plainTextFile));
			OutputStream cipherStream = new BufferedOutputStream(new FileOutputStream(cryptoFile));
			
			byte[] buffer = MartusSecurity.geEncryptedFileIdentifier().getBytes();
			int len = buffer.length;
			cipherStream.write(buffer, 0, len);
			
			String publicKeyString = (String) MartusUtilities.importServerPublicKeyFromFile(publicKeyFile, security).get(0);
			PublicKey publicKey = MartusSecurity.extractPublicKey(publicKeyString);
			
			security.encrypt(plainStream, cipherStream, security.createSessionKey(), publicKey);
		}
		catch (Exception e)
		{
			System.err.println("EncryptFile.main: " + e);
			e.printStackTrace();
			System.exit(3);
		}

		System.exit(0);
	}
}
