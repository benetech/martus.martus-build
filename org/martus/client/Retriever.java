package org.martus.client;

import java.util.Vector;

import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.UniversalId;

public class Retriever 
{

	public Retriever(MartusApp appToUse, BulletinStore storeToUse) 
	{
		super();
		app = appToUse;
		store = storeToUse;
		result = NetworkInterfaceConstants.INCOMPLETE;
	}
	
	public void retrieveBulletins(Vector uidList, BulletinFolder retrievedFolder) 
	{
		if(!app.isSSLServerAvailable())
		{
			result = NetworkInterfaceConstants.NO_SERVER;
			return;
		}
		
		for(int i = 0; i < uidList.size(); ++i)
		{
			try
			{
				UniversalId uid = (UniversalId)uidList.get(i);
				if(store.findBulletinByUniversalId(uid) != null)
					continue;
		
				app.retrieveOneBulletin(uid, retrievedFolder);
			}
			catch(Exception e)
			{
				result = NetworkInterfaceConstants.INCOMPLETE;
				return;
			}
		}
		result = NetworkInterfaceConstants.OK;
	}
	
	public String getResult()
	{
		return result;
	}
	
	private String result;
	private MartusApp app;
	private BulletinStore store;	

}
