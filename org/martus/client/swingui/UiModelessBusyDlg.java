/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

package org.martus.client.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import org.martus.client.core.MartusApp;

public class UiModelessBusyDlg extends JDialog
{

	public UiModelessBusyDlg(String message)
	{
		super();
		getContentPane().add(new JLabel(" "), BorderLayout.NORTH);
		getContentPane().add(new JLabel(" "), BorderLayout.SOUTH);
		getContentPane().add(new JLabel("     "), BorderLayout.EAST);
		getContentPane().add(new JLabel("     "), BorderLayout.WEST);
		getContentPane().add(new JLabel(message), BorderLayout.CENTER);
		getRootPane().setBorder(new LineBorder(Color.black, 5));
		setUndecorated(true);
		pack();
		Dimension size = getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(MartusApp.center(size, screen));
		setResizable(false);
		origCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		show();
	}
	
	public void endDialog()
	{
		setCursor(origCursor);
		dispose();
	}
	
	Cursor origCursor;
}
