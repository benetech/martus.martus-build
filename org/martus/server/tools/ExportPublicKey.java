package org.martus.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.server.forclients.MartusServerUtilities;

public class ExportPublicKey
{
	public static void main(String[] args)
	{
		File keypair = null;
		File outputfile = null;
		boolean prompt = true;
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--no-prompt"))
			{
				prompt = false;
			}
			
			if(args[i].startsWith("--keypair"))
			{
				keypair = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--file"))
			{
				outputfile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
		}
		
		if(keypair == null || outputfile == null)
		{
			System.err.println("Incorrect arguments: ExportPublicKey [--no-prompt] --keypair=keypair.dat --file=pubkey.dat\n");
			System.exit(2);
		}
		
		if(!keypair.exists())
		{
			System.err.println("Unable to find keypair\n");
			System.exit(3);
		}
		
		if(prompt)
		{
			System.out.print("Enter server passphrase:");
			System.out.flush();
		}
		
		MartusCrypto security = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			String passphrase = reader.readLine();
			security = MartusServerUtilities.loadCurrentMartusSecurity(keypair, passphrase);
		}
		catch(Exception e)
		{
			System.err.println("ExportPublicKey.main: " + e + "\n");
			System.exit(3);
		}
		
		try
		{
			MartusUtilities.exportServerPublicKey(security, outputfile);
		}
		catch (Exception e)
		{
			System.err.println("ExportPublicKey.main: " + e + "\n");
			System.exit(3);
		}

		if(prompt)
		{
			System.out.println("Public key exported to file " + outputfile.getAbsolutePath() + "\n");
			System.out.flush();
		}
		System.exit(0);
	}
}
