package org.martus.server.tools;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

import org.martus.common.Base64;
import org.martus.common.ByteArrayInputStreamWithSeek;
import org.martus.common.FileInputStreamWithSeek;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusSecurity;
import org.martus.common.UnicodeReader;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
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

		InputStreamWithSeek encryptedFileInput = null;
		UnicodeReader encryptedFileReader = null;
		OutputStream plainTextOutput = null;
		ByteArrayInputStreamWithSeek inEncrypted = null;
		try
		{
			security = (MartusSecurity) MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase);
			
			encryptedFileInput = new FileInputStreamWithSeek(cryptoFile);
			encryptedFileReader = new UnicodeReader(encryptedFileInput);
			plainTextOutput = new BufferedOutputStream(new FileOutputStream(plainTextFile));

			byte[] identifierBytesExpected = MartusSecurity.geEncryptedFileIdentifier().getBytes();
			byte[] identifierBytesRetrieved = encryptedFileReader.readLine().getBytes();

			if(! Arrays.equals(identifierBytesExpected, identifierBytesRetrieved))
			{
				throw new IncorrectEncryptedFileIdentifierException();
			}
			
			String RetrievedPublicKeyString = encryptedFileReader.readLine();
			byte[] rpbByteArray = Base64.decode(RetrievedPublicKeyString);
			
			String publicKeyString = security.getPublicKeyString();
			byte[] pbByteArray = publicKeyString.getBytes();
			
			if(! Arrays.equals(rpbByteArray, pbByteArray))
			{
				throw new IncorrectPublicKeyException();
			}
			
			String retrievedDigest = encryptedFileReader.readLine();
			
			String retrievedEncryptedText = encryptedFileReader.readLine();
			byte[] encryptedBytes = Base64.decode(retrievedEncryptedText);
			inEncrypted = new ByteArrayInputStreamWithSeek(encryptedBytes);

			security.decrypt(inEncrypted, plainTextOutput);
			
			String plainText = MartusServerUtilities.getFileContents(plainTextFile);			
			String calculatedDigest = MartusSecurity.createDigestString(plainText);

			if(calculatedDigest.compareTo(retrievedDigest) != 0)
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
			plainTextOutput.close();
			encryptedFileReader.close();
			encryptedFileInput.close();
			inEncrypted.close();
		}
		if(prompt)
		{
			System.out.println("File " + cryptoFile + " was decrypted to " + plainTextFile);
		}
		System.exit(0);
	}
}
