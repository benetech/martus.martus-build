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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class Utilities 
{
	static public boolean isMacintosh()
	{
		return (UIManager.getSystemLookAndFeelClassName().indexOf("MacLookAndFeel") >= 0);
	}

	static public boolean isMSWindows()
	{
		return (UIManager.getSystemLookAndFeelClassName().indexOf("WindowsLookAndFeel") >= 0);
	}

	static public void maximizeWindow(JFrame window)
	{
		window.setVisible(true);//required for setting maximized
		window.setExtendedState(Frame.MAXIMIZED_BOTH);
	}

	static public boolean isValidScreenPosition(Dimension screenSize, Dimension objectSize, Point objectPosition)
	{
		int height = objectSize.height;
		if(height == 0 )
			return false;
		if(objectPosition.x > screenSize.width - 100)
			return false;
		if(objectPosition.y > screenSize.height - 100)
			return false;
		if(objectPosition.x < -100 || objectPosition.y < -100)
			return false;
		return true;
	}

	static public void waitForThreadToTerminate(Delay worker)
	{
		try
		{
			worker.join();
		}
		catch (InterruptedException e)
		{
			// We don't care if this gets interrupted
		}
	}

	static public void centerDlg(JDialog dlg)
	{
		dlg.pack();
		Dimension size = dlg.getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
		dlg.setLocation(center(size, screen));
	}

	static public Point center(Dimension inner, Rectangle outer)
	{
		int x = (outer.width - inner.width) / 2;
		int y = (outer.height - inner.height) / 2;
		return new Point(x, y);
	}

	public static class Delay extends Thread
	{
		public Delay(int sec)
		{
			timeInMillis = sec * 1000;
		}

		public void run()
		{
			try
			{
				sleep(timeInMillis);
			}
			catch(InterruptedException e)
			{
			}
		}

		private int timeInMillis;
	}


}
