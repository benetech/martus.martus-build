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

	public Bulletin getBulletin()
	{
		return bulletin;
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

	public void refresh(Bulletin b)
	{
		if(bulletin != null && b != null &&
				b.getUniversalId().equals(bulletin.getUniversalId()))
		{
			//System.out.println("UiBulletinPreview.refresh: skipping");
			return;
		}
		
		bulletin = b;
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

	public void bulletinHasChanged(Bulletin b)
	{
		if(bulletin == null)
			return;

		if(b.getLocalId().equals(bulletin.getLocalId()))
			refresh(b);
	}

	private void indicateEncrypted(boolean isEncrypted)
	{
		view.updateEncryptedIndicator(isEncrypted);
	}

	Bulletin bulletin;
	UiBulletinView view = null;
}
