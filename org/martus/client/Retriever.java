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
