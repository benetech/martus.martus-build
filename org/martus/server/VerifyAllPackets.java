package org.martus.server;

import java.io.File;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.Packet;

public class VerifyAllPackets
{

	public VerifyAllPackets()
	{
		super();
	}

	public static void main(String[] args)
	{
		System.out.println("VerifyAllPackets Martus Database Integrity Checker");
		System.out.println("  Runs a SAFE, non-destructive, read-only test");
		
		if(args.length != 1)
		{
			System.out.println("  Usage: VerifyAllPackets <directory>");
			System.exit(1);
		}
		
		File dir = new File(args[0]);
		if(!dir.exists() || !dir.isDirectory())
		{
			System.out.println();
			System.out.println("Cannot find directory: " + args[0]);
			System.exit(2);
		}
		
		try
		{
			MartusSecurity security = new MartusSecurity();
			ServerFileDatabase db = new ServerFileDatabase(dir,security);
			verifyAllPackets(db, security);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
							System.out.println("      for account bucket: " + bhp.getAccountId());
						}
					}
				}
				
			}
			catch (Exception e)
			{
				System.out.println();
				System.out.println("Exception on packet: " + visitingLocalId);
				e.printStackTrace();
			}
			
		}

		Database db;
		MartusCrypto security;

	}
}
