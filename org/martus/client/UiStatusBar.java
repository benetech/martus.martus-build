package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

public class UiStatusBar extends JPanel
{

	public UiStatusBar() 
	{
		super();
		setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS) );
		
		horizontalBox = Box.createHorizontalBox();
		statusMessage = new JLabel( " ", JLabel.LEFT );
		statusMessage.setBorder( new BevelBorder( BevelBorder.LOWERED ));
		horizontalBox.add( statusMessage );
		
		progressMeter = new JProgressBar(0, 10);
		progressMeter.setMaximumSize(new Dimension(100, 20));
		progressMeter.setStringPainted(true);
		progressMeter.setBorder( new BevelBorder( BevelBorder.LOWERED ));
		horizontalBox.add( progressMeter );
	
		blankStatus();
		hideProgressMeter();
		add(horizontalBox);
	}
	
	public void setStatusMessage(String message)
	{
		statusMessage.setText(message);
	}
	
	public void updateProgressMeter(String message, int currentValue, int maxValue)
	{
		statusMessage.setText(message);
		progressMeter.setValue(currentValue);
		progressMeter.setMaximum(maxValue);
		progressMeter.setVisible(true);
	}
	
	public void blankStatus()
	{
		statusMessage.setText("          ");
	}

	
	public void hideProgressMeter()
	{
		progressMeter.setVisible(false);
	}
	
	private Box horizontalBox;
	private JLabel statusMessage;
	private JProgressBar progressMeter;
}
