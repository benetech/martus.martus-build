/* $Id: UiBoolEditor.java,v 1.3 2002/04/05 21:34:34 kevins Exp $ */
package org.martus.client;

import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.event.*;

public class UiBoolEditor extends UiField implements ChangeListener
{
	public UiBoolEditor(ChangeListener listener)
	{
		observer = listener;
		widget = new JCheckBox("");
		widget.addChangeListener(this);
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public String getText()
	{
		if(widget.isSelected())
			return TRUESTRING;

		return FALSESTRING;
	}

	public void setText(String newText)
	{
		boolean selected = (newText.equals(TRUESTRING));
		widget.setSelected(selected);
	}

	public void disableEdits()
	{
		widget.setEnabled(false);
	}

	public void stateChanged(ChangeEvent event)
	{
		if(observer != null)
			observer.stateChanged(event);
	}

	JCheckBox widget;
	ChangeListener observer;
}

