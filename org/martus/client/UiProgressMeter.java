package org.martus.client;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

public class UiProgressMeter extends JPanel
{
	public UiProgressMeter(UiProgressRetrieveDlg dlg) 
	{
		super();
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
		parentDlg = dlg;
		statusMessage = new JLabel("     ", JLabel.LEFT );
		statusMessage.setMinimumSize(new Dimension(60, 25));

		progressMeter = new JProgressBar(0, 10);
		Dimension meterSize = new Dimension(100, 20);
		progressMeter.setMinimumSize(meterSize);
		progressMeter.setMaximumSize(meterSize);
		progressMeter.setPreferredSize(meterSize);
		progressMeter.setBorder( new BevelBorder( BevelBorder.LOWERED ));
		progressMeter.setStringPainted(true);

		add( statusMessage );
		add( progressMeter );
	}

	public void setStatusMessageAndHideMeter(String message)
	{
		setStatusMessage(message);
		hideProgressMeter();
	}

	public void setStatusMessage(String message)
	{
		statusMessage.setText(" " + message + " ");
	}
	
	public void updateProgressMeter(String message, int currentValue, int maxValue)
	{
		setStatusMessage(message);
		progressMeter.setValue(currentValue);
		progressMeter.setMaximum(maxValue);
		progressMeter.setVisible(true);
	}
	
	public void hideProgressMeter()
	{
		progressMeter.setVisible(false);
	}
	
	public boolean shouldExit()
	{
		if(parentDlg != null)
			return parentDlg.shouldExit();
		return false;
	}
	
	private JLabel statusMessage;
	private JProgressBar progressMeter;
	private UiProgressRetrieveDlg parentDlg;
}
