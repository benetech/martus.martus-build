package org.martus.server.tools;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

import org.martus.common.FileInputStreamWithSeek;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.server.forclients.MartusServerUtilities;

public class DecryptFile
{
	public static class IncorrectEncryptedFileIdentifierException extends Exception {};
	
	public static void main(String[] args)
	{
		File keyPairFile = null;
		File plainTextFile = null;
		File cryptoFile = null;
		MartusSecurity security = null;
		boolean prompt = true;
		
		for (int i = 0; i < args.length; i++)
		{			
			if(args[i].startsWith("--keypair"))
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--plaintext-file"))
			{
				plainTextFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if(args[i].startsWith("--crypto-file"))
			{
				cryptoFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--no-prompt"))
			{
				prompt = false;
			}
		}
		
		if(keyPairFile == null || cryptoFile == null)
		{
			System.err.println("Incorrect arguments: DecryptFile [--no-prompt] --keypair=<keypair.dat> --crypto-file=<input> --plaintext-file=<output>");
			System.exit(2);
		}
		
		if(prompt)
		{
			System.out.print("Enter passphrase: ");
			System.out.flush();
		}

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String passphrase = null;
		try
		{
			passphrase = stdin.readLine();
		}
		catch (IOException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(3);
		}

		try
		{
			security = (MartusSecurity) MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase);
			
			InputStreamWithSeek encryptedFileInput = new FileInputStreamWithSeek(cryptoFile);
			OutputStream plainTextOutput = new BufferedOutputStream(new FileOutputStream(plainTextFile));
			
			int len = MartusSecurity.geEncryptedFileIdentifier().getBytes().length;
			byte[] identifierBytesExpected = MartusSecurity.geEncryptedFileIdentifier().getBytes();
			byte[] identifierBytesRetrieved = new byte[len];
			
			encryptedFileInput.read(identifierBytesRetrieved, 0, len);

			if(! Arrays.equals(identifierBytesExpected, identifierBytesRetrieved))
			{
				throw new IncorrectEncryptedFileIdentifierException();
			}
			
			security.decrypt(encryptedFileInput, plainTextOutput);
		}
		catch (AuthorizationFailedException e)
		{
			System.err.println("Error: " + e.toString() );
			System.exit(1);
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.toString() );
			System.exit(3);
		}
		if(prompt)
		{
			System.out.println("File " + cryptoFile + " was decrypted to " + plainTextFile);
		}
		System.exit(0);
	}
}
