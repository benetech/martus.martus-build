package org.martus.client;

import java.awt.KeyboardFocusManager;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class UiTextArea extends JTextArea
{
	public UiTextArea(int rows, int cols)
	{
		super(rows, cols);
		SetTabKeyForFocusEvents();
	}
	
	public UiTextArea(String text)
	{
		super(text);
		SetTabKeyForFocusEvents();
	}

	private void SetTabKeyForFocusEvents()
	{
		Set set = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
	    set.clear(); 
	    set.add(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0));
	    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
	    set.clear(); 
	    set.add(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB,
	    		 java.awt.event.InputEvent.SHIFT_MASK));
	    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);
	}
}
