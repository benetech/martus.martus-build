/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

package org.martus.client;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.martus.client.BulletinStore.StatusNotAllowedException;
import org.martus.common.MartusCrypto;
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
		Cursor originalCursor = observer.getCursor();
		observer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		if(dtde.isDataFlavorSupported(TransferableBulletinList.getBulletinListDataFlavor()))
			dropTransferableBulletins(dtde);
		else if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			dropFile(dtde);
			
		observer.setCursor(originalCursor);
	}

	// private methods
	private void dropTransferableBulletins(DropTargetDropEvent dtde)
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
		//System.out.println("dropTransferableBulletin: accepted");

		boolean worked = true;
		try
		{
			attemptDropBulletins(tb.getBulletins(), toFolder);
		}
		catch (StatusNotAllowedException e)
		{
			worked = false;
		}
		//System.out.println("dropTransferableBulletin: Drop Complete!");

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
			Toolkit.getDefaultToolkit().beep();
			observer.notifyDlg(observer, "DropErrorNotAllowed");
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

		String resultMessageTag = null;
		
		try
		{
			attemptDropFile(file, toFolder);
		} 
		catch (StatusNotAllowedException e) 
		{
			resultMessageTag = "DropErrorNotAllowed";
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("dropFile Exception:" + e);
			resultMessageTag = "DropErrors";
		}
		catch(MartusCrypto.CryptoException e)
		{
			e.printStackTrace();
			System.out.println("dropFile Exception:" + e);
			resultMessageTag = "DropErrors";
		}
		catch(Packet.InvalidPacketException e)
		{
			e.printStackTrace();
			System.out.println("dropFile Exception:" + e);
			resultMessageTag = "DropErrors";
		}
		catch(Packet.SignatureVerificationException e)
		{
			e.printStackTrace();
			System.out.println("dropFile Exception:" + e);
			resultMessageTag = "DropErrors";
		}

		boolean worked = (resultMessageTag == null);
		dtde.dropComplete(worked);

		if(!worked)
		{
			Toolkit.getDefaultToolkit().beep();
			observer.notifyDlg(observer, resultMessageTag);
		}
	}

	public void attemptDropFile(File file, BulletinFolder toFolder) throws 
		IOException,
		MartusCrypto.CryptoException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException,
		BulletinStore.StatusNotAllowedException
	{
		Cursor originalCursor = observer.getCursor();
		observer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try 
		{
			toFolder.getStore().importZipFileBulletin(file, toFolder, false);
			observer.folderContentsHaveChanged(toFolder);
		} 
		finally
		{
			observer.setCursor(originalCursor);
		}
	}


	public void attemptDropBulletins(Bulletin[] bulletins, BulletinFolder toFolder) throws
		BulletinStore.StatusNotAllowedException
	{
		System.out.println("attemptDropBulletin");

		BulletinStore store = toFolder.getStore();

		for (int i = 0; i < bulletins.length; i++) 
		{
			Bulletin bulletin = bulletins[i];
System.out.println("UiBulletinDropAdapter.attemptDropBulletins: " + bulletin.get(Bulletin.TAGTITLE));
			if(!store.canPutBulletinInFolder(toFolder, bulletin.getAccount(), bulletin.getStatus()))
				throw new BulletinStore.StatusNotAllowedException();
		}

		
		for (int i = 0; i < bulletins.length; i++)
		{
			Bulletin bulletin = bulletins[i];
			store.addBulletinToFolder(bulletin.getUniversalId(), toFolder);
		}
		store.saveFolders();
		
		observer.folderContentsHaveChanged(toFolder);
	}

	UiMainWindow observer;
}

