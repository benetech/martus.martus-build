package org.martus.client;

import java.util.Vector;

public class UiBulletinView extends UiBulletinComponent
{
	UiBulletinView(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		mainWindow = mainWindowToUse;
		bulletinViewSections = new Vector();
		Initalize();
		disableEdits();
		// ensure that attachmentViewer gets initialized
	}

	public UiBulletinComponentSection createBulletinComponentSection(MartusApp app, boolean encrypted)
	{
		UiBulletinComponentViewSection section = new UiBulletinComponentViewSection(this, mainWindow, app, encrypted);
		bulletinViewSections.add(section);
		return section;
	}
	
	public void disableEdits()
	{
		publicStuff.disableEdits();
		privateStuff.disableEdits();
	}
		
	public UiField createBoolField()
	{
		return new UiBoolViewer(getApp());
	}

	private Vector bulletinViewSections;
}
