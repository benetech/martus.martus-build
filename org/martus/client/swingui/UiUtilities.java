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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.martus.swing.UiNotifyDlg;

public class UiUtilities
{
	static void notifyDlg(UiLocalization localization, JFrame parent, String baseTag, String titleTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(localization, parent, baseTag, titleTag, emptyTokenReplacement);
	}

	static void notifyDlg(UiLocalization localization, JFrame parent, String baseTag, String titleTag, Map tokenReplacement)
	{
		String title = localization.getWindowTitle(titleTag);
		String cause = localization.getFieldLabel("notify" + baseTag + "cause");
		String ok = localization.getButtonLabel("ok");
		String[] contents = {cause};
		String[] buttons = {ok};

		new UiNotifyDlg(parent, title, contents, buttons, tokenReplacement);
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

	public static boolean confirmDlg(UiLocalization localization, JFrame parent, String baseTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		return confirmDlg(localization, parent, baseTag, emptyTokenReplacement);
	}
	
	public static boolean confirmDlg(UiLocalization localization, JFrame parent, String baseTag, Map tokenReplacement)
	{
		String title = localization.getWindowTitle("confirm" + baseTag);
		String cause = localization.getFieldLabel("confirm" + baseTag + "cause");
		String effect = localization.getFieldLabel("confirm" + baseTag + "effect");
		String question = localization.getFieldLabel("confirmquestion");
		String[] contents = {cause, "", effect, "", question};
		return confirmDlg(localization, parent, title, contents);
	}

	public static boolean confirmDlg(UiLocalization localization, JFrame parent, String title, String[] contents)
	{
		HashMap emptyTokenReplacement = new HashMap();
		return confirmDlg(localization, parent, title, contents, emptyTokenReplacement);
	}

	public static boolean confirmDlg(UiLocalization localization, JFrame parent, String title, String[] contents, Map tokenReplacement)
	{
		String yes = localization.getButtonLabel("yes");
		String no = localization.getButtonLabel("no");
		String[] buttons = {yes, no};

		return confirmDlg(parent, title, contents, buttons, tokenReplacement);
	}

	public static boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons) 
	{
		HashMap emptyTokenReplacement = new HashMap();
		return confirmDlg(parent, title, contents, buttons, emptyTokenReplacement); 
	}

	public static boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons, Map tokenReplacement) 
	{
		UiNotifyDlg notify = new UiNotifyDlg(parent, title, contents, buttons, tokenReplacement);
		String result = notify.getResult();
		if(result == null)
			return false;
		return(result.equals(buttons[0]));
	}

	
	static public void updateIcon(JFrame window)
	{
		URL imageURL = UiMainWindow.class.getResource("Martus.png");
		if(imageURL == null)
			return;
		ImageIcon imageicon = new ImageIcon(imageURL);
		if(imageicon != null)
			window.setIconImage(imageicon.getImage());
	}

}
