package org.martus.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.DatabaseKey;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.UniversalId;
import org.martus.server.core.ServerFileDatabase;
import org.martus.server.forclients.MartusServer;
import org.martus.server.forclients.MartusServerUtilities;

public class ListPacketsForBulletin
{
	public static void main(String[] args)
	{
		String bulletinLocalId = null;
		String accountPublicKey = null;
		File packetDir = null;
		MartusSecurity security = null;
		File serverKeyPairFile = null;
		boolean prompt = true;
		
		for (int i = 0; i < args.length; i++)
		{			
			if(args[i].startsWith("--header-packet"))
			{
				bulletinLocalId = args[i].substring(args[i].indexOf("=") + 1);
			}
			
			if(args[i].startsWith("--packet-directory="))
			{
				packetDir = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--keypair"))
			{
				serverKeyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--account"))
			{
				accountPublicKey = args[i].substring(args[i].indexOf("=")+1);
			}
		}
		
		try
		{
			if(packetDir == null)
			{
				packetDir = new File(MartusServer.getDefaultDataDirectoryPath(), "packets" );
				if(! packetDir.exists())
				{
					throw new IllegalArgumentException();
				}
			}
			
			if(serverKeyPairFile == null)
			{
				File configDir = new File(MartusServer.getDefaultDataDirectoryPath(), "deleteOnStartup");
				serverKeyPairFile = new File(configDir, MartusServer.getKeypairFilename());
				if(! serverKeyPairFile.exists())
				{
					throw new IllegalArgumentException();
				}
			}
			
			if(bulletinLocalId == null || accountPublicKey == null)
			{
				throw new IllegalArgumentException();
			}
		}
		catch(IllegalArgumentException e)
		{
			System.err.println("Incorrect arguments: ListPacketsForBulletin [--no-prompt --packet-directory=<pathToPacketsDirectory> --keypair=<pathToKeyPair>] --public-key=<accountPublicKey> --header-packet=<universalId>");
			System.exit(3);
		}
		
		try
		{
			if(prompt)
			{
				System.out.print("Enter server passphrase:");
				System.out.flush();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			
			String passphrase = reader.readLine();
			security = (MartusSecurity) MartusServerUtilities.loadCurrentMartusSecurity(serverKeyPairFile, passphrase);			
			
			ServerFileDatabase db = new ServerFileDatabase(packetDir,security);
			db.initialize();
			
			DatabaseKey dbKey = new DatabaseKey(UniversalId.createFromAccountAndLocalId(accountPublicKey, bulletinLocalId));
			
			InputStreamWithSeek inForLoad = db.openInputStream(dbKey, security);
			BulletinHeaderPacket bhp = new BulletinHeaderPacket(bulletinLocalId);
			bhp.loadFromXml(inForLoad, security);
			inForLoad.close();

			DatabaseKey[] keys = MartusUtilities.getAllPacketKeys(bhp);
			
			for (int i = 0; i < keys.length; i++)
			{			
				String path = db.getFolderForAccount(accountPublicKey);
				System.out.println(path + "\\" + keys[i].getLocalId());
			}
		}
		catch (Exception e)
		{
			System.err.println("ListPacketsForBulletin.main: " + e);
			e.printStackTrace();
			System.exit(3);
		}
		System.exit(0);
	}
}
