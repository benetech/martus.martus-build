package org.martus.client;

public class UiBulletinComponentViewSection extends UiBulletinComponentSection 
{

	public UiBulletinComponentViewSection(MartusApp appToUse, boolean encrypted) 
	{
		super(appToUse, encrypted);
		app = appToUse;
	}

	public UiField createDateField()
	{
		return new UiDateViewer(app);
	}

	public UiField createNormalField()
	{
		return new UiNormalTextViewer(app);
	}

	public UiField createMultilineField()
	{
		return new UiMultilineViewer(app);
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceViewer(choices);
	}

	MartusApp app;
}
