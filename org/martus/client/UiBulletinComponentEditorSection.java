package org.martus.client;

public class UiBulletinComponentEditorSection extends UiBulletinComponentSection 
{

	public UiBulletinComponentEditorSection( MartusApp appToUse, boolean encrypted) 
	{
		super(appToUse, encrypted);
		app = appToUse;
	}

	public UiField createNormalField()
	{
		return new UiNormalTextEditor(app);
	}

	public UiField createMultilineField()
	{
		return new UiMultilineTextEditor(app);
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceEditor(choices);
	}

	public UiField createDateField()
	{
		return new UiDateEditor(app);
	}

	MartusApp app;

}
