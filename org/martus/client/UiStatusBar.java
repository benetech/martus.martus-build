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
		backgroundProgressMeter = new UiProgressMeter(null);
		statusBarBox.add(backgroundProgressMeter);
		statusBarBox.add(Box.createHorizontalGlue());
		add(statusBarBox);
	}
	
	public UiProgressMeter getBackgroundProgressMeter()
	{
		return backgroundProgressMeter;
	}
	
	private UiProgressMeter backgroundProgressMeter;
	private Box statusBarBox;
}
