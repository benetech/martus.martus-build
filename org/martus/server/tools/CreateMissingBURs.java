/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002,2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.server.tools;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.FileDatabase.TooManyAccountsException;
import org.martus.common.MartusCrypto.CreateDigestException;
import org.martus.server.core.ServerFileDatabase;
import org.martus.server.forclients.MartusServerUtilities;

public class CreateMissingBURs
{
	public static void main(String[] args)
	{
		new CreateMissingBURs(args);
	}

	public CreateMissingBURs(String[] args)
	{
		processArgs(args);
		if(prompt)
			System.out.println("CreateMissingBURs");
		security = MartusServerUtilities.loadKeyPair(keyPairFileName, prompt);
		initializeDatabase();
		createBURs();
	}

	static class BurAlreadyExistedException extends Exception {}
	
	static void createBulletinUploadRecord(ServerFileDatabase db, DatabaseKey key, MartusCrypto security)
		throws CreateDigestException, BurAlreadyExistedException, IOException, TooManyAccountsException
	{
		String timeStamp = db.getTimeStamp(key);
		String bur = MartusServerUtilities.createBulletinUploadRecordWithSpecificTimeStamp(key.getLocalId(), timeStamp, security);
		DatabaseKey burKey = MartusServerUtilities.getBurKey(key);
		if(db.doesRecordExist(burKey))
			throw new BurAlreadyExistedException();
		db.writeRecord(burKey, bur);
	}

	void createBURs()
	{
		BulletinVisitor visitor = new BulletinVisitor(); 
		db.visitAllRecords(visitor);
		Vector keys = visitor.foundKeys;
		ensureNoBursExist(keys);

		try
		{
			for (Iterator iter = keys.iterator(); iter.hasNext();)
			{
				DatabaseKey key = (DatabaseKey) iter.next();
				String timeStamp = db.getTimeStamp(key);
				if(noisy)
					System.out.println(key.getLocalId() + ": " + timeStamp);
				createBulletinUploadRecord(db, key, security);
			}
			if(noisy)
				System.out.println();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error: " + e);
			System.exit(3);
		}
	}

	private void ensureNoBursExist(Vector keys)
	{
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			DatabaseKey key = (DatabaseKey) iter.next();
			if(db.doesRecordExist(MartusServerUtilities.getBurKey(key)))
			{
				System.out.println("Error: BUR already exists: " + key.getLocalId());
				System.exit(3);
			}
		}
	}

	void initializeDatabase()
	{
		File packetDir = new File(packetDirName);
		if(! packetDir.exists())
		{
			System.out.println("Error packets directory not found");
			System.exit(3);
		}
		
		db = new ServerFileDatabase(packetDir, security);
		try
		{
			db.initialize();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Error initializing database: " + e);
			System.exit(3);
		}
	}

	void processArgs(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			String value = args[i].substring(args[i].indexOf("=")+1);
			if(args[i].startsWith("--no-prompt"))
				prompt = false;
		
			if(args[i].startsWith("--noisy"))
				noisy = true;

			if(args[i].startsWith("--packet-directory="))
				packetDirName = value;
			
			if(args[i].startsWith("--keypair"))
				keyPairFileName = value;
		}

		if(packetDirName == null || keyPairFileName == null)
		{
			System.err.println("Incorrect arguments: CreateMissingBURs [--no-prompt] [--noisy] --packet-directory=<packetdir> --keypair-file=<keypair>\n");
			System.exit(2);
		}
	}

	static class BulletinVisitor implements Database.PacketVisitor
	{
		public void visit(DatabaseKey key)
		{
			if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				return;
					
			foundKeys.add(key);
		}
			
		public Vector foundKeys = new Vector();
		Exception thrownException;
	}
		
	boolean prompt = true;
	boolean noisy;
	String packetDirName;
	String keyPairFileName;
	MartusCrypto security;
	ServerFileDatabase db;
}
