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

package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSigninPanel;
import org.martus.swing.Utilities;



public class UiSigninDlg extends JDialog
{
	public UiSigninDlg(UiMainWindow window, JFrame owner, int mode)
	{
		this(window, owner, mode, "");
	}

	public UiSigninDlg(UiMainWindow window, JFrame owner, int mode, String username)
	{
		super(owner, true);
		initalize(window, owner, mode, username);
	}


	public void initalize(UiMainWindow window, JFrame owner, int mode, String username)
	{
		mainWindow = window;
		
		UiLocalization localization = mainWindow.getLocalization();

		String title = getTextForTitle(localization, mode);
		setTitle(title);
		
		signinPane = new UiSigninPanel(this, mode, username);

		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(ok);
		buttonBox.add(cancel);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.setBorder(new EmptyBorder(5,5,5,5));
		
		if(shouldUseTabs(mode))
		{
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.add(signinPane);
			tabbedPane.setTitleAt(0, "Sign In");  
			getContentPane().add(tabbedPane);
		}
		else
		{
			getContentPane().add(signinPane);
		}

		getContentPane().add(buttonBox, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(ok);
		signinPane.refreshForNewVirtualMode();
		Utilities.centerDlg(this);
		setResizable(true);
		show();
	}

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public boolean shouldUseTabs(int mode)
	{
		if(mode == INITIAL)
			return true;
			
		return false;
	}
	
	public static String getTextForTitle(UiLocalization localization, int mode)
	{
		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;
		String title = ""; 
		switch (mode)
		{
			case SECURITY_VALIDATE:
				title = localization.getWindowTitle("MartusSignInValidate"); 
				break;
			case RETYPE_USERNAME_PASSWORD:
				title = localization.getWindowTitle("MartusSignInRetypePassword"); 
				break;
			default:
				title = localization.getWindowTitle("MartusSignIn"); 
				break;
		}
		
		String completeTitle = title +" (" + versionInfo + ")";
		return completeTitle;
	}

	public boolean getResult()
	{
		return result;
	}

	public String getName()
	{
		return signinPane.getName();
	}

	public String getPassword()
	{
		return signinPane.getPassword();
	}
	
	public void sizeHasChanged()
	{
		Utilities.centerDlg(this);
	}
	
	public void virtualPasswordHasChanged()
	{
		getRootPane().setDefaultButton(ok);
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = true;
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}

	UiSigninPanel signinPane;
	private UiMainWindow mainWindow;
	private boolean result;
	private JButton ok;

	public final static int INITIAL = 1;
	public final static int TIMED_OUT = 2;
	public final static int SECURITY_VALIDATE = 3;
	public final static int RETYPE_USERNAME_PASSWORD = 4;
	public final static int CREATE_NEW = 5;
}

