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

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusUtilities;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiAboutDlg extends JDialog implements ActionListener
{
	public UiAboutDlg(UiMainWindow owner)
		throws HeadlessException
	{
		super(owner, "" , true);
		UiLocalization localization = owner.getLocalization();
		
		setTitle(localization.getWindowTitle("about"));

		JLabel icon = new JLabel(new ImageIcon(UiAboutDlg.class.getResource("MartusLogo.gif")),JLabel.LEFT);

		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;

		String buildDate = localization.getFieldLabel("aboutDlgBuildDate");
		buildDate += " " + MartusUtilities.getVersionDate();

		JButton ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		ok.addKeyListener(new MakeEnterKeyExit());

		Box vBoxVersionInfo = Box.createVerticalBox();
		vBoxVersionInfo.add(new JLabel(versionInfo));
		vBoxVersionInfo.add(new JLabel(localization.getFieldLabel("aboutDlgCopyright")));
		vBoxVersionInfo.add(new JLabel(buildDate));

		Box hBoxVersionAndIcon = Box.createHorizontalBox();
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(vBoxVersionInfo);
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(icon);

		Box hBoxOk = Box.createHorizontalBox();
		hBoxOk.add(Box.createHorizontalGlue());
		hBoxOk.add(ok);
		hBoxOk.add(Box.createHorizontalGlue());

		final String disclaimer = localization.getFieldLabel("aboutDlgDisclaimer");
		final String credits = localization.getFieldLabel("aboutDlgCredits");
		final String notice = "\n" + disclaimer + "\n\n" + credits + "\n\n" +
					RSANOTICE + "\n" + IBMNOTICE + "\n" + APACHENOTICE;

		Box vBoxDetails = Box.createVerticalBox();
		vBoxDetails.add(new UiWrappedTextArea(notice));
		vBoxDetails.add(hBoxOk);

		Box hBoxDetails = Box.createHorizontalBox();
		hBoxDetails.add(vBoxDetails);

		Box vBoxAboutDialog = Box.createVerticalBox();
		vBoxAboutDialog.add(hBoxVersionAndIcon);
		vBoxAboutDialog.add(hBoxDetails);
		getContentPane().add(vBoxAboutDialog);

		pack();
		Dimension size = getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(Utilities.center(size, screen));
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

	public class MakeEnterKeyExit extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				dispose();
		}
	}
	final String RSANOTICE = "This product includes code licensed from RSA Security, Inc.";
	final String IBMNOTICE = "Some portions licensed from IBM are available at http://oss.software.ibm.com/icu4j/.";
	final String APACHENOTICE = "This product includes software developed by the Apache Software Foundation (http://www.apache.org/).";
}
