package org.martus.client;

import javax.swing.JComponent;

abstract public class UiField
{
	abstract public JComponent getComponent();
	abstract public String getText();
	abstract public void setText(String newText);
	abstract public void disableEdits();

	public static final String TRUESTRING = "1";
	public static final String FALSESTRING = "0";

}

