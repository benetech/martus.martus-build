package org.martus.client;

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

public class UiBulletinPreview extends JScrollPane
{
    public UiBulletinPreview(UiMainWindow mainWindow)
	{
		view = new UiBulletinView(mainWindow);

		getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		getViewport().add(view);
	}

	public Bulletin getCurrentBulletin()
	{
		return currentBulletin;
	}

	public JComponent getView()
	{
		return view;
	}

	public void startPrintMode()
	{
		view.startPrintMode();	
	}
	
	public void endPrintMode()
	{
		view.endPrintMode();	
	}

	public void setCurrentBulletin(Bulletin b)
	{
		if(currentBulletin != null && b != null &&
				b.getUniversalId().equals(currentBulletin.getUniversalId()))
		{
			//System.out.println("UiBulletinPreview.refresh: skipping");
			return;
		}
		
		currentBulletin = b;
		try
		{
			view.copyDataFromBulletin(b);
		}
		catch(IOException e)
		{
			System.out.println("UiBulletinPreview.refresh: " + e);
		}

		boolean isEncrypted = false;
		if(b != null && b.isAllPrivate())
			isEncrypted = true;
		indicateEncrypted(isEncrypted);
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		if(currentBulletin == null)
			return;

		if(b.getLocalId().equals(currentBulletin.getLocalId()))
			setCurrentBulletin(b);
	}

	private void indicateEncrypted(boolean isEncrypted)
	{
		view.updateEncryptedIndicator(isEncrypted);
	}

	Bulletin currentBulletin;
	UiBulletinView view = null;
}
