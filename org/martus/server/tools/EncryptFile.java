package org.martus.server.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.Vector;

import org.martus.common.Base64;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.UnicodeWriter;
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

		InputStream plainStream = null;
		ByteArrayOutputStream cipherByteArrayOutputStream = null;
		try
		{
			security = new MartusSecurity();
			
			byte [] plainFileContents = MartusServerUtilities.getFileContents(plainTextFile);
			String digest = Base64.encode(MartusSecurity.createDigest(plainFileContents));
			
			plainStream = new FileInputStream(plainTextFile);		
			cipherByteArrayOutputStream = new ByteArrayOutputStream();
			
			Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(publicKeyFile, security);
			String publicKeyString = (String) publicInfo.get(0);
			PublicKey publicKey = MartusSecurity.extractPublicKey(publicKeyString);
			
			security.encrypt(plainStream, cipherByteArrayOutputStream, security.createSessionKey(), publicKey);
			String encodedEncryptedFile = Base64.encode(cipherByteArrayOutputStream.toByteArray());
					
			UnicodeWriter writer = new UnicodeWriter(cryptoFile);
			writer.writeln(MartusSecurity.geEncryptedFileIdentifier());
			writer.writeln(publicKeyString);
			writer.writeln(digest);
			writer.writeln(encodedEncryptedFile);
			
			writer.close();
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
}
