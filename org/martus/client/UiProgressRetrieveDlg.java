package org.martus.client;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JDialog;

public class UiProgressRetrieveDlg extends JDialog 
{

	public UiProgressRetrieveDlg(UiMainWindow window)
	{
		super(window, "Retrieve Bulletin", true);
		meter = new UiProgressMeter();
		getContentPane().add(meter);
		window.centerDlg((JDialog)this);
		setResizable(true);
	}

	public void updateProgressMeter(String message, int currentValue, int maxValue)
	{
		meter.updateProgressMeter(message, currentValue, maxValue);	
	}
	
	private UiProgressMeter meter;
}
