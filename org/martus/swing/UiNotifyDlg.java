/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class UiNotifyDlg extends JDialog implements ActionListener
{

	public UiNotifyDlg(JFrame owner, String title, String[] contents, String[] buttons)
	{
		super(owner, title , true);
		getContentPane().add(new JLabel("      "), BorderLayout.WEST);
		getContentPane().add(new JLabel("      "), BorderLayout.EAST);

		Box vbox = Box.createVerticalBox();
		vbox.add(new JLabel(" "));
		for(int i = 0 ; i < contents.length ; ++i)
			vbox.add(createWrappedTextArea(contents[i]));
		vbox.add(new JLabel(" "));

		ok = new JButton(buttons[0]);
		ok.addActionListener(this);
		Box hbox = Box.createHorizontalBox();
		hbox.add(ok);
		JButton button = null;
		for(int j = 1 ; j < buttons.length; ++j)
		{
			button = new JButton(buttons[j]);
			button.addActionListener(this);
			hbox.add(button);
		}
		vbox.add(hbox);
		vbox.add(new JLabel(" "));

		getContentPane().add(vbox, BorderLayout.CENTER);
		Utilities.centerDlg(this);
		setResizable(true);
		getRootPane().setDefaultButton(ok);
		ok.requestFocus(true);
		show();
	}

	private JTextArea createWrappedTextArea(String message)
	{
		UiWrappedTextArea msgArea = new UiWrappedTextArea(message);
		msgArea.addKeyListener(new TabToOkButton());
		return msgArea;
	}

	public class TabToOkButton extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_TAB)
			{
				ok.requestFocus();
			}
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		exit();
	}

	public void exit()
	{
		if(ok.hasFocus())
			result = ok.getText();
		else
			result = "";
		dispose();
	}

	public String getResult()
	{
		return result;
	}

	String result;
	JButton ok;
}
