package org.martus.client;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.Packet;
import org.martus.common.UniversalId;

abstract class UiBulletinDropAdapter implements DropTargetListener
{
	UiBulletinDropAdapter(UiMainWindow mainWindow)
	{
		observer = mainWindow;
	}

	abstract public BulletinFolder getFolder(Point at);

	// DropTargetListener interface
	public void dragEnter(DropTargetDragEvent dtde) {}
	public void dragExit(DropTargetEvent dte) {}
	public void dropActionChanged(DropTargetDragEvent dtde) {}

	public void dragOver(DropTargetDragEvent dtde)
	{
		BulletinFolder folder = getFolder(dtde.getLocation());
		if(folder == null)
		{
			dtde.rejectDrag();
			return;
		}

		if(dtde.isDataFlavorSupported(TransferableBulletinList.getBulletinListDataFlavor()))
			dtde.acceptDrag(dtde.getDropAction());
		else if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			dtde.acceptDrag(dtde.getDropAction());
		else
			dtde.rejectDrag();
	}

	public void drop(DropTargetDropEvent dtde)
	{
		if(dtde.isDataFlavorSupported(TransferableBulletinList.getBulletinListDataFlavor()))
			dopTransferableBulletin(dtde);
		else if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			dropFile(dtde);
	}

	// private methods
	private void dopTransferableBulletin(DropTargetDropEvent dtde)
	{
		System.out.println("dropTransferableBulletin");
		BulletinFolder toFolder = getFolder(dtde.getLocation());
		if(toFolder == null)
		{
			dtde.rejectDrop();
			return;
		}
		Transferable t = dtde.getTransferable();
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(t);
		if(tb == null)
		{
			dtde.rejectDrop();
			return;
		}
		BulletinFolder fromFolder = tb.getFromFolder();
		if(fromFolder.equals(toFolder))
		{
			dtde.rejectDrop();
			return;
		}
		dtde.acceptDrop(dtde.getDropAction());
		System.out.println("dropTransferableBulletin: accepted");

		boolean worked = false;
		worked = attemptDropBulletins(tb.getBulletins(), toFolder);
		System.out.println("dropTransferableBulletin: Drop Complete!");

		if(worked)
		{
			BulletinStore store = observer.getStore();
			Bulletin[] wereDropped = tb.getBulletins();
			for (int i = 0; i < wereDropped.length; i++) 
			{
				Bulletin bulletin = wereDropped[i];
				UniversalId uId = bulletin.getUniversalId();
				Bulletin b = store.findBulletinByUniversalId(uId);
				if(b == null)
				{
					System.out.println("dropTransferableBulletin: null bulletin!!");
				}
				else
				{
					store.removeBulletinFromFolder(b, fromFolder);
					observer.folderContentsHaveChanged(fromFolder);
				}
			}
		}

		tb.dispose();
		dtde.dropComplete(worked);
		if(!worked)
		{
			observer.notifyDlg(observer, "illegaldrop");
		}
		
	}

	private void dropFile(DropTargetDropEvent dtde)
	{
		System.out.println("dropFile");
		BulletinFolder toFolder = getFolder(dtde.getLocation());
		if(toFolder == null)
		{
			System.out.println("dropFile: toFolder null");
			dtde.rejectDrop();
			return;
		}

		dtde.acceptDrop(dtde.getDropAction());

		Transferable t = dtde.getTransferable();
		List list = null;
		try
		{
			list = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
		}
		catch(Exception e)
		{
			System.out.println("dropFile exception: " + e);
			dtde.dropComplete(false);
			return;
		}

		if(list.size() == 0)
		{
			System.out.println("dropFile: list empty");
			dtde.dropComplete(false);
			return;
		}

		File file = (File)list.get(0);
		System.out.println(file.getPath());

		boolean worked = false;
		
		try
		{
			worked = attemptDropFile(file, toFolder);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("dropFile Exception:" + e);
		}
		catch(MartusCrypto.CryptoException e)
		{
			System.out.println("dropFile Exception:" + e);
		}
		catch(Packet.InvalidPacketException e)
		{
			System.out.println("dropFile Exception:" + e);
		}
		catch(Packet.SignatureVerificationException e)
		{
			e.printStackTrace();
			System.out.println("dropFile Exception:" + e);
		}
		
		dtde.dropComplete(worked);
		if(!worked)
		{
			observer.notifyDlg(observer, "illegaldrop");
		}
	}

	public boolean attemptDropFile(File file, BulletinFolder toFolder) throws 
		IOException,
		MartusCrypto.CryptoException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException
	
	{
		try
		{
			toFolder.getStore().importZipFileBulletin(file, toFolder, false);
		}
		catch(BulletinStore.StatusNotAllowedException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(MartusUtilities.DuplicatePacketException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(MartusUtilities.SealedPacketExistsException e)
		{
			e.printStackTrace();
			return false;
		}
		
		observer.folderContentsHaveChanged(toFolder);
		return true;
	}


	public boolean attemptDropBulletins(Bulletin[] bulletins, BulletinFolder toFolder)
	{
		System.out.println("attemptDropBulletin");

		BulletinStore store = toFolder.getStore();

		for (int i = 0; i < bulletins.length; i++) 
		{
			Bulletin bulletin = bulletins[i];
System.out.println("UiBulletinDropAdapter.attemptDropBulletins: " + bulletin.get(Bulletin.TAGTITLE));
			if(!store.canPutBulletinInFolder(toFolder, bulletin.getAccount(), bulletin.getStatus()))
				return false;
		}

		
		for (int i = 0; i < bulletins.length; i++)
		{
			Bulletin bulletin = bulletins[i];
			store.addBulletinToFolder(bulletin.getUniversalId(), toFolder);
		}
		store.saveFolders();
		
		observer.folderContentsHaveChanged(toFolder);
		return true;
	}

	UiMainWindow observer;
}

