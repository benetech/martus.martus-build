package org.martus.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;


public class TransferableBulletinList implements Transferable
{
	public TransferableBulletinList(Bulletin thisBulletin, BulletinFolder fromFolder)
	{
		bulletins = new Bulletin[] {thisBulletin};
		folder = fromFolder;
	}
	
	public TransferableBulletinList(Bulletin[] bulletinsToUse, BulletinFolder fromFolder)
	{
		bulletins = bulletinsToUse;
		folder = fromFolder;
	}
	
	boolean createTransferableZipFile() 
	{
// TODO: Remove when multiple select is working
if(bulletins.length > 1)
System.out.println("TransferableBulletinList.createTransferableZipFile: USING JUST FIRST BULLETIN!");
		try
		{
			Bulletin bulletin = bulletins[0];
			String summary = MartusApp.toFileName(bulletin.get(bulletin.TAGTITLE));
			file = File.createTempFile(summary, BULLETIN_FILE_EXTENSION);
			file.deleteOnExit();
			Database db = bulletin.getStore().getDatabase();
			DatabaseKey headerKey = DatabaseKey.createKey(bulletin.getUniversalId(), bulletin.getStatus());
			MartusCrypto security = bulletin.getStore().getSignatureGenerator();
			MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, file, security);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("TransferableBulletin createTransferableZipFile: " + e);
			return false;
		}
		return true;
	}

	public void dispose()
	{
		try
		{
			if(file != null)	
				file.delete();
		}
		catch(Exception e)
		{
			System.out.println("TransferableBulletin.dispose ignoring: " + e);
		}
	}

	public BulletinFolder getFromFolder()
	{
		return folder;
	}

	public Bulletin[] getBulletins()
	{
		return bulletins;
	}

	// Transferable interface
	public Object getTransferData(DataFlavor flavor) throws
						UnsupportedFlavorException
	{
		if(flavor.equals(bulletinListDataFlavor))
			return this;

		if(flavor.equals(DataFlavor.javaFileListFlavor))
		{
			if(file == null)
			{
				//System.out.println("TransferableBulletin.getTransferData : creatingZipFile");
				if (!createTransferableZipFile())
					throw new UnsupportedFlavorException(flavor);
			}

			LinkedList list = new LinkedList();
			list.add(file);
			return list;
		}

		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		DataFlavor[] flavorArray = {
					bulletinListDataFlavor,
					DataFlavor.javaFileListFlavor,
//					DataFlavor.stringFlavor,
//					mimeTextDataFlavor,
// TODO remove all trace of mime and string flavors
// Warning: adding when there was String and mimeText flavors 
//			dragging to the desktop failed silently.
					};
		return flavorArray;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		DataFlavor[] flavors = getTransferDataFlavors();

		for(int i = 0; i < flavors.length; ++i)
			if(flavor.equals(flavors[i]))
				return true;
		return false;
	}

	public File getFile()
	{
		return file;
	}

	static public DataFlavor getBulletinListDataFlavor()
	{
		return bulletinListDataFlavor;
	}
	
	static public File extractFileFrom(Transferable t)
	{
		if(!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			return null;
		try 
		{
			Collection fileList = (Collection)t.getTransferData(DataFlavor.javaFileListFlavor);
			if(fileList.size() != 1)
				return null;

			Iterator iterator = fileList.iterator();
			File file = (File)iterator.next();	
			return file;
		} 
		catch (Exception e) 
		{
			System.out.println("extractFileFrom :" + e);
			return null;
		}
	}

	static public TransferableBulletinList extractFrom(Transferable t)
	{
		if(t == null)
			return null;

		DataFlavor flavor = getBulletinListDataFlavor();

		if(!t.isDataFlavorSupported(flavor))
		{
			return null;
		}
		TransferableBulletinList tb = null;
		try
		{
			tb = (TransferableBulletinList)t.getTransferData(flavor);
		}
		catch(UnsupportedFlavorException e)
		{
			System.out.println("TransferableBulletin.extractFrom unsupported flavor");
		}
		catch(IOException e)
		{
			System.out.println("TransferableBulletin.extractFrom IOException");
		}
		catch(Exception e)
		{
			System.out.println("TransferableBulletin.extractFrom " + e);
		}
		return tb;
	}

	private static final String BULLETIN_FILE_EXTENSION = ".mba";
	static DataFlavor bulletinListDataFlavor = new DataFlavor(TransferableBulletinList.class, "Martus Bulletins");
	File file;
	BulletinFolder folder;
	Bulletin[] bulletins;
}
