package org.martus.client;

import java.awt.Color;
import java.util.Vector;

public class UiBulletinView extends UiBulletinComponent
{
	UiBulletinView(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		mainWindow = mainWindowToUse;
		bulletinViewSections = new Vector();
		Initalize();
		viewColors = new UiColors();
		disableEdits();
		// ensure that attachmentViewer gets initialized
	}

	public UiBulletinComponentSection createBulletinComponentSection(MartusApp app, boolean encrypted)
	{
		UiBulletinComponentViewSection section = new UiBulletinComponentViewSection(this, mainWindow, app, encrypted);
		bulletinViewSections.add(section);
		return section;
	}
	
	public void startPrintMode()
	{
		viewColors.publicBackground = publicStuff.getBackground();
		viewColors.publicForeground = publicStuff.getForeground();
		viewColors.privateBackground = privateStuff.getBackground();
		viewColors.privateForeground = privateStuff.getForeground();

		publicStuff.setBackground(Color.white);
		publicStuff.setForeground(Color.black);
		privateStuff.setBackground(Color.white);
		privateStuff.setForeground(Color.black);
		for(int i = 0 ; i< bulletinViewSections.size(); ++i)
		{
			((UiBulletinComponentViewSection)(bulletinViewSections.get(i))).attachmentViewer.startPrintMode();
		}	
	}
	
	public void endPrintMode()
	{
		publicStuff.setBackground(viewColors.publicBackground);
		publicStuff.setForeground(viewColors.publicForeground);
		privateStuff.setBackground(viewColors.privateBackground);
		privateStuff.setForeground(viewColors.privateForeground);
		for(int i = 0 ; i< bulletinViewSections.size(); ++i)
		{
			((UiBulletinComponentViewSection)(bulletinViewSections.get(i))).attachmentViewer.endPrintMode();
		}	
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

	class UiColors
	{
		public Color publicForeground;
		public Color publicBackground;
		public Color privateForeground;
		public Color privateBackground;
	}
	
	private UiColors viewColors;
	private Vector bulletinViewSections;
}
