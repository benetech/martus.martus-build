/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

package org.martus.client.swingui;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.martus.client.core.BackgroundUploader;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.ClientSideNetworkGateway;
import org.martus.client.core.MartusApp;
import org.martus.client.core.Exceptions.ServerCallFailedException;
import org.martus.client.core.Exceptions.ServerNotAvailableException;

class BackgroundUploadTimerTask extends TimerTask
{
	public BackgroundUploadTimerTask(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		UiProgressMeter progressMeter = mainWindow.statusBar.getBackgroundProgressMeter();
		uploader = new BackgroundUploader(mainWindow.getApp(), progressMeter);
	}

	public void run()
	{
		if(mainWindow.inConfigServer)
			return;
		if(inComplianceDialog)
			return;
		try
		{
			checkComplianceStatement();
			checkForNewsFromServer();
			doUploading();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doUploading()
		throws InterruptedException, InvocationTargetException
	{
		try
		{
			mainWindow.uploadResult = uploader.backgroundUpload();
		}
		catch (MartusApp.DamagedBulletinException e)
		{
			ThreadedNotify damagedBulletin = new ThreadedNotify("DamagedBulletinMovedToDiscarded");
			SwingUtilities.invokeAndWait(damagedBulletin);
			mainWindow.folderContentsHaveChanged(getStore().getFolderOutbox());
			mainWindow.folderContentsHaveChanged(getStore().getFolderDraftOutbox());
			mainWindow.folderContentsHaveChanged(getApp().createOrFindFolder(getStore().getNameOfFolderDamaged()));
			mainWindow.folderTreeContentsHaveChanged();
		}
		if(mainWindow.uploadResult != null)
		{
			//System.out.println("UiMainWindow.Tick.run: " + uploadResult);
			mainWindow.folderContentsHaveChanged(getStore().getFolderSent());
			mainWindow.folderContentsHaveChanged(getStore().getFolderOutbox());
			mainWindow.folderContentsHaveChanged(getStore().getFolderDraftOutbox());
			mainWindow.folderContentsHaveChanged(getApp().createOrFindFolder(getStore().getNameOfFolderDamaged()));
		}
	}
		
	public void checkComplianceStatement()
	{
		if(alreadyCheckedCompliance)
			return;
		try
		{
			ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();
			String compliance = getApp().getServerCompliance(gateway);
			alreadyCheckedCompliance = true;
			if(!compliance.equals(getApp().getConfigInfo().getServerCompliance()))
			{
				ThreadedServerComplianceDlg dlg = new ThreadedServerComplianceDlg(compliance);
				SwingUtilities.invokeAndWait(dlg);
			}
		}
		catch (ServerCallFailedException userAlreadyKnows)
		{
			alreadyCheckedCompliance = true;
			return;
		}
		catch (ServerNotAvailableException weWillTryAgainLater)
		{
			return;
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		} 
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	public void checkForNewsFromServer()
	{
		if(alreadyGotNews)
			return;
		Vector newsItems = getApp().getNewsFromServer();
		for (Iterator iter = newsItems.iterator(); iter.hasNext();)
		{
			String newsItem = (String) iter.next();
			ThreadedMessageDlg newsDlg = new ThreadedMessageDlg("ServerNews", newsItem);
			try
			{
				SwingUtilities.invokeAndWait(newsDlg);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		alreadyGotNews = true;
	}

	class ThreadedNotify implements Runnable
	{
		public ThreadedNotify(String tag)
		{
			notifyTag = tag;
		}

		public void run()
		{
			mainWindow.notifyDlg(mainWindow, notifyTag);
		}
		String notifyTag;
	}
		
	class ThreadedServerComplianceDlg implements Runnable
	{
		public ThreadedServerComplianceDlg(String newComplianceToUse)
		{
			newCompliance = newComplianceToUse;
		}
			
		public void run()
		{
			inComplianceDialog = true;
			if(mainWindow.confirmServerCompliance("ServerComplianceChangedDescription", newCompliance))
			{
				String serverAddress = getApp().getConfigInfo().getServerName();
				String serverKey = getApp().getConfigInfo().getServerPublicKey();
				getApp().setServerInfo(serverAddress, serverKey, newCompliance);
			}
			else
			{
				getApp().setServerInfo("", "", "");
				mainWindow.notifyDlg(mainWindow, "ExistingServerRemoved");
			}
			inComplianceDialog = false;
		}
			
		String newCompliance;
	}
		
	class ThreadedMessageDlg implements Runnable
	{
		public ThreadedMessageDlg(String tag, String message)
		{
			titleTag = tag;
			messageContents = message;
		}

		public void run()
		{
			mainWindow.messageDlg(mainWindow, titleTag, messageContents);
		}
		String titleTag;
		String messageContents;
	}
		
	MartusApp getApp()
	{
		return mainWindow.getApp();
	}
		
	BulletinStore getStore()
	{
		return getApp().getStore();
	}

	UiMainWindow mainWindow;
	BackgroundUploader uploader;
	boolean alreadyCheckedCompliance;
	boolean inComplianceDialog;
	boolean alreadyGotNews;
}

