package org.martus.client;

import java.util.Vector;

import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.UniversalId;

public class Retriever 
{

	public Retriever(MartusApp appToUse, UiProgressRetrieveDlg retrieve) 
	{
		super();
		app = appToUse;
		retrieveDlg = retrieve;
		result = NetworkInterfaceConstants.INCOMPLETE;
	}
	
	public void retrieveMyBulletins(Vector uidList)
	{
		BulletinFolder retrievedFolder = app.createFolderRetrieved();
		app.getStore().saveFolders();

		retrieveBulletins(uidList, retrievedFolder);
	}

	public void retrieveFieldOfficeBulletins(Vector uidList)
	{
		BulletinFolder retrievedFolder = app.createFolderRetrieved();
		app.getStore().saveFolders();

		retrieveBulletins(uidList, retrievedFolder);
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

		if(retrieveDlg == null)
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
		if(retrieveDlg != null)
			retrieveDlg.finishedRetrieve();
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
			int i = 0;
			int size = uidList.size();
			UiProgressMeter progressMeter = null;
			if(retrieveDlg != null)
				progressMeter = retrieveDlg.getChunkCountMeter();
			for(i = 0; i < size; ++i)
			{
				try
				{
					if(retrieveDlg != null)
						retrieveDlg.updateBulletinCountMeter(i, size);
					UniversalId uid = (UniversalId)uidList.get(i);
					if(app.getStore().findBulletinByUniversalId(uid) != null)
						continue;
					app.retrieveOneBulletin(uid, retrievedFolder, progressMeter);
				}
				catch(Exception e)
				{
					result = NetworkInterfaceConstants.INCOMPLETE;
					finishedRetrieve();
					return;
				}
			}
			result = NetworkInterfaceConstants.OK;
			if(retrieveDlg != null)
				retrieveDlg.updateBulletinCountMeter(i, size);
			finishedRetrieve();
		}

		private Vector uidList;
		private BulletinFolder retrievedFolder;
	}
		
	private String result;
	private MartusApp app;
	public UiProgressRetrieveDlg retrieveDlg;
}
