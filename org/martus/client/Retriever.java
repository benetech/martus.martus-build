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

import java.util.Vector;

import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.UniversalId;

public class Retriever 
{

	public Retriever(MartusApp appToUse, UiProgressRetrieveBulletinsDlg retrieve) 
	{
		super();
		app = appToUse;
		progressDlg = retrieve;
		result = NetworkInterfaceConstants.INCOMPLETE;
	}
	
	public void retrieveBulletins(Vector uidList, BulletinFolder retrievedFolder) 
	{
		if(!app.isSSLServerAvailable())
		{
			result = NetworkInterfaceConstants.NO_SERVER;
			return;
		}

		RetrieveThread worker = new RetrieveThread(uidList, retrievedFolder);
		worker.start();

		if(progressDlg == null)
			waitForThreadToTerminate(worker);
	}

	public void waitForThreadToTerminate(RetrieveThread worker) 
	{
		try 
		{
			worker.join();
		} 
		catch (InterruptedException e) 
		{
		}
	}
	
	public void finishedRetrieve()
	{
		if(progressDlg != null)
			progressDlg.finishedRetrieve();
	}

	public String getResult()
	{
		return result;
	}
	
	
	class RetrieveThread extends Thread
	{
		public RetrieveThread(Vector list, BulletinFolder folder)
		{
			uidList = list;
			retrievedFolder = folder;
		}
		
		public void run()
		{
			boolean gotAllBulletins = true;
			int i = 0;
			int size = uidList.size();
			UiProgressMeter progressMeter = null;
			if(progressDlg != null)
				progressMeter = progressDlg.getChunkCountMeter();
			for(i = 0; i < size; ++i)
			{
				try
				{
					if(progressDlg != null)
					{
						if(progressDlg.shouldExit())
							break;
						progressDlg.updateBulletinCountMeter(i, size);
					}
					UniversalId uid = (UniversalId)uidList.get(i);
					if(app.getStore().findBulletinByUniversalId(uid) != null)
						continue;
					app.retrieveOneBulletinToFolder(uid, retrievedFolder, progressMeter);
				}
				catch(Exception e)
				{
					//e.printStackTrace();
					gotAllBulletins = false;
				}
			}
		
			if(progressDlg != null)
				progressDlg.updateBulletinCountMeter(i, size);

			if(gotAllBulletins)
				result = NetworkInterfaceConstants.OK;
			else
				result = NetworkInterfaceConstants.INCOMPLETE;

			finishedRetrieve();
		}

		private Vector uidList;
		private BulletinFolder retrievedFolder;
	}
		
	private String result;
	private MartusApp app;
	public UiProgressRetrieveBulletinsDlg progressDlg;
}
