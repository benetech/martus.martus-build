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

package org.martus.client.swingui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class UiUtilities
{
	static void updateIcon(JFrame window)
	{
		URL imageURL = UiMainWindow.class.getResource("Martus.png");
		if(imageURL == null)
			return;
		ImageIcon imageicon = new ImageIcon(imageURL);
		if(imageicon != null)
			window.setIconImage(imageicon.getImage());
	}

	static boolean isMacintosh()
	{
		return (UIManager.getSystemLookAndFeelClassName().indexOf("MacLookAndFeel") >= 0);
	}

	static boolean isMSWindows()
	{
		return (UIManager.getSystemLookAndFeelClassName().indexOf("WindowsLookAndFeel") >= 0);
	}

	static void maximizeWindow(JFrame window)
	{
		window.setVisible(true);//required for setting maximized
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	static boolean isValidScreenPosition(Dimension screenSize, Dimension objectSize, Point objectPosition)
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

	public static void waitForThreadToTerminate(Delay worker)
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

	static void notifyDlg(UiLocalization localization, JFrame parent, String baseTag, String titleTag)
	{
		String title = localization.getWindowTitle(titleTag);
		String cause = localization.getFieldLabel("notify" + baseTag + "cause");
		String ok = localization.getButtonLabel("ok");
		String[] contents = {cause};
		String[] buttons = {ok};

		new UiNotifyDlg(parent, title, contents, buttons);
	}

	static void messageDlg(UiLocalization localization, JFrame parent, String baseTag, String message)
	{
		String title = localization.getWindowTitle(baseTag);
		String cause = localization.getFieldLabel("message" + baseTag + "cause");
		String ok = localization.getButtonLabel("ok");
		String[] contents = {cause, "", message};
		String[] buttons = {ok};

		new UiNotifyDlg(parent, title, contents, buttons);
	}

	static boolean confirmDlg(UiLocalization localization, JFrame parent, String baseTag)
	{
		String title = localization.getWindowTitle("confirm" + baseTag);
		String cause = localization.getFieldLabel("confirm" + baseTag + "cause");
		String effect = localization.getFieldLabel("confirm" + baseTag + "effect");
		String question = localization.getFieldLabel("confirmquestion");
		String[] contents = {cause, "", effect, "", question};
		return confirmDlg(localization, parent, title, contents);
	}

	static boolean confirmDlg(UiLocalization localization, JFrame parent, String title, String[] contents)
	{
		String yes = localization.getButtonLabel("yes");
		String no = localization.getButtonLabel("no");
		String[] buttons = {yes, no};

		UiNotifyDlg notify = new UiNotifyDlg(parent, title, contents, buttons);
		String result = notify.getResult();
		if(result == null)
			return false;
		return(result.equals(yes));
	}

	public static void centerDlg(JDialog dlg)
	{
		dlg.pack();
		Dimension size = dlg.getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
		dlg.setLocation(UiUtilities.center(size, screen));
	}

	public static Point center(Dimension inner, Rectangle outer)
	{
		int x = (outer.width - inner.width) / 2;
		int y = (outer.height - inner.height) / 2;
		return new Point(x, y);
	}

	static class Delay extends Thread
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
				;
			}
		}

		private int timeInMillis;
	}


}
