package org.martus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.Packet;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.CryptoInitializationException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;

public class VerifyAllPackets
{

	public VerifyAllPackets()
	{
		super();
	}

	public static void main(String[] args)
	{		
		File dir = null;
		File keyPairFile = null;
		boolean prompt = true;

		System.out.println("VerifyAllPackets Martus Database Integrity Checker");
		System.out.println("  Runs a SAFE, non-destructive, read-only test");
				
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--keypair"))
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--packet-directory="))
			{
				dir = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if( args[i].startsWith("--no-prompt") )
			{
				prompt = false;
			}
		}

		if(dir == null || keyPairFile == null )
		{
			System.err.println("\nUsage: VerifyAllPackets --packet-directory=<directory> --keypair=<pathToKeyPairFile> [--no-prompt]");
			System.exit(2);
		}
		
		if(!dir.exists() || !dir.isDirectory())
		{
			System.err.println("Cannot find directory: " + dir);
			System.exit(3);
		}
		
		if(!keyPairFile.exists() || !keyPairFile.isFile())
		{
			System.err.println("Cannot find file: " + keyPairFile);
			System.exit(3);
		}
		
		MartusCrypto security = null;
		
		if(prompt)
		{
			System.out.print("Enter server passphrase:");
			System.out.flush();
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			String passphrase = reader.readLine();
			security = loadCurrentMartusSecurity(keyPairFile, passphrase);
		}
		catch(Exception e)
		{
			System.err.println("FileSignerAndVerifier.main: " + e);
			System.exit(3);
		}
		
		try
		{
			ServerFileDatabase db = new ServerFileDatabase(dir,security);
			db.initialize();
			verifyAllPackets(db, security);
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
			System.exit(3);
		}
		
		System.exit(0);
	}
	
	static void verifyAllPackets(Database db, MartusCrypto security)
	{
		PacketVerifier verifier = new PacketVerifier(db, security);
		db.visitAllRecords(verifier);
		System.out.println();
		System.out.println("DONE");
	}
	
	private static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, String passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}

	static class PacketVerifier implements Database.PacketVisitor
	{
		PacketVerifier(Database databaseToUse, MartusCrypto securityToUse)
		{
			db = databaseToUse;
			security = securityToUse;
		}
		
		public void visit(DatabaseKey visitingKey)
		{
			System.out.print(".");
			System.out.flush();

			String visitingLocalId = visitingKey.getLocalId();
			try
			{
				InputStreamWithSeek inForValidate = db.openInputStream(visitingKey, security);
				Packet.validateXml(inForValidate, visitingKey.getAccountId(), visitingLocalId, null, security);
				inForValidate.close();
				
				if(BulletinHeaderPacket.isValidLocalId(visitingLocalId))
				{
					InputStreamWithSeek inForLoad = db.openInputStream(visitingKey, security);
					BulletinHeaderPacket bhp = new BulletinHeaderPacket(visitingKey.getAccountId());
					bhp.loadFromXml(inForLoad, security);
					inForLoad.close();
					
					DatabaseKey[] keys = MartusUtilities.getAllPacketKeys(bhp);
					for (int i = 0; i < keys.length; i++)
					{
						if(!db.doesRecordExist(keys[i]))
						{
							System.out.println();
							System.out.println("Missing packet: " + keys[i].getLocalId());
							System.out.println("  for header: " + visitingLocalId);
							
							FileDatabase fdb = (FileDatabase) db;
							File bucket = fdb.getFileForRecord(keys[i]);
							String path = bucket.getParent().substring(bucket.getParent().indexOf("packets"));
							System.out.println("      for account bucket: " + path);
						}
					}
				}
				
			}
			catch (Exception e)
			{
				System.err.println("Exception on packet: " + visitingLocalId);
				e.printStackTrace();
			}
			
		}

		Database db;
		MartusCrypto security;
	}
}
