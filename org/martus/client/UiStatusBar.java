package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
		statusMessage.setPreferredSize(new Dimension(50,20));
		horizontalBox.add( statusMessage );
		
		progressMeter = new JLabel( " ", JLabel.CENTER );
		progressMeter.setPreferredSize(new Dimension(50,20));
		progressMeter.setBorder( new BevelBorder( BevelBorder.LOWERED ));
		horizontalBox.add( progressMeter );
		
		blankStatus();
		add(horizontalBox);
	}
	
	public void setStatusMessage(String message)
	{
		statusMessage.setText(message);
	}
	
	public void blankStatus()
	{
		statusMessage.setText("          ");
		progressMeter.setText("          ");
	}
	
	private Box horizontalBox;
	private JLabel statusMessage;
	private JLabel progressMeter;
}
