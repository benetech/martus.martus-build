package org.martus.client;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class UiStatusBar extends JPanel
{

	public UiStatusBar() 
	{
		super();
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
		
		statusBarBox = Box.createHorizontalBox();
		backgroundProgressMeter = new UiProgressMeter();
		foregroundProgressMeter = new UiProgressMeter();
		statusBarBox.add(foregroundProgressMeter);
		statusBarBox.add(backgroundProgressMeter);
		statusBarBox.add(Box.createHorizontalGlue());
		add(statusBarBox);

		foregroundProgressMeter.blankStatus();
		foregroundProgressMeter.hideProgressMeter();
		backgroundProgressMeter.blankStatus();
		backgroundProgressMeter.hideProgressMeter();
	}
	
	public UiProgressMeter getBackgroundProgressMeter()
	{
		return backgroundProgressMeter;
	}
	
	public UiProgressMeter getForegroundProgressMeter()
	{
		return foregroundProgressMeter;
	}

	private UiProgressMeter backgroundProgressMeter;
	private UiProgressMeter foregroundProgressMeter;
	private Box statusBarBox;
}
