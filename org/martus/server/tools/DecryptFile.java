package org.martus.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.martus.common.Base64;
import org.martus.common.ByteArrayInputStreamWithSeek;
import org.martus.common.MartusSecurity;
import org.martus.common.UnicodeReader;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.NoKeyPairException;
import org.martus.server.forclients.MartusServerUtilities;

public class DecryptFile
{
	public static class IncorrectEncryptedFileIdentifierException extends Exception {};
	public static class IncorrectPublicKeyException extends Exception {};
	public static class DigestFailedException extends Exception {};
	
	public static void main(String[] args) throws IOException
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

		UnicodeReader encryptedFileReader = null;
		OutputStream plainTextOutput = null;
		try
		{
			security = (MartusSecurity) MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase);
			
			encryptedFileReader = new UnicodeReader(cryptoFile);
			String identifierBytesRetrieved = encryptedFileReader.readLine();
			String retrievedPublicKeyString = encryptedFileReader.readLine();
			String retrievedDigest = encryptedFileReader.readLine();
			String retrievedEncryptedText = encryptedFileReader.readLine();
			
			String publicKeyString = security.getPublicKeyString();
			if(! publicKeyString.equals(retrievedPublicKeyString))
			{
				throw new IncorrectPublicKeyException();
			}
			
			String identifierBytesExpected =  MartusSecurity.geEncryptedFileIdentifier();
			if(! identifierBytesExpected.equals(identifierBytesRetrieved))
			{
				throw new IncorrectEncryptedFileIdentifierException();
			}
			
			plainTextOutput = new FileOutputStream(plainTextFile);
			decryptToFile(security, plainTextOutput, retrievedEncryptedText);
			plainTextOutput.close();
			
			byte [] plainFileContents = MartusServerUtilities.getFileContents(plainTextFile);			
			String calculatedDigest = Base64.encode(MartusSecurity.createDigest(plainFileContents));
			if(! calculatedDigest.equals(retrievedDigest))
			{
				throw new DigestFailedException();
			}			
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
		finally
		{
			encryptedFileReader.close();
		}
		if(prompt)
		{
			System.out.println("File " + cryptoFile + " was decrypted to " + plainTextFile);
		}
		System.exit(0);
	}

	public static void decryptToFile(MartusSecurity security, OutputStream plainTextOutput, String retrievedEncryptedText)
		throws InvalidBase64Exception, NoKeyPairException, DecryptionException
	{
		byte[] encryptedBytes = Base64.decode(retrievedEncryptedText);
		ByteArrayInputStreamWithSeek inEncrypted = new ByteArrayInputStreamWithSeek(encryptedBytes);
		security.decrypt(inEncrypted, plainTextOutput);
	}
}
