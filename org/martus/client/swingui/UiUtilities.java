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

import org.martus.client.core.MartusApp;

public class UiUtilities
{
	static void updateIcon(JFrame window)
	{
		URL imageURL = window.getClass().getResource("Martus.png");
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

	static void notifyDlg(MartusApp app, JFrame parent, String baseTag, String titleTag)
	{
		String title = app.getWindowTitle(titleTag);
		String cause = app.getFieldLabel("notify" + baseTag + "cause");
		String ok = app.getButtonLabel("ok");
		String[] contents = {cause};
		String[] buttons = {ok};

		new UiNotifyDlg(parent, title, contents, buttons);
	}

	static void messageDlg(MartusApp app, JFrame parent, String baseTag, String message)
	{
		String title = app.getWindowTitle(baseTag);
		String cause = app.getFieldLabel("message" + baseTag + "cause");
		String ok = app.getButtonLabel("ok");
		String[] contents = {cause, "", message};
		String[] buttons = {ok};

		new UiNotifyDlg(parent, title, contents, buttons);
	}

	static boolean confirmDlg(MartusApp app, JFrame parent, String baseTag)
	{
		String title = app.getWindowTitle("confirm" + baseTag);
		String cause = app.getFieldLabel("confirm" + baseTag + "cause");
		String effect = app.getFieldLabel("confirm" + baseTag + "effect");
		String question = app.getFieldLabel("confirmquestion");
		String[] contents = {cause, "", effect, "", question};
		return confirmDlg(app, parent, title, contents);
	}

	static boolean confirmDlg(MartusApp app, JFrame parent, String title, String[] contents)
	{
		String yes = app.getButtonLabel("yes");
		String no = app.getButtonLabel("no");
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
		dlg.setLocation(MartusApp.center(size, screen));
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
