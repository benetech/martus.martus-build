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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.martus.client.core.MartusApp;



public class UiLocalizeDlg extends JDialog implements ActionListener, ChangeListener
{
	public UiLocalizeDlg(UiMainWindow mainWindow)
	{
		super(mainWindow, "", true);
		owner = mainWindow;
		MartusApp app = owner.getApp();

		setTitle(app.getMenuLabel("Preferences"));

		dateFormatDropdown = new UiChoiceEditor(MartusLocalization.getDateFormats());
		dateFormatDropdown.setText(owner.getApp().getCurrentDateFormatCode());

		languageDropdown = new UiChoiceEditor(app.getUiLanguages());
		languageDropdown.setText(app.getCurrentLanguage());

		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		getContentPane().setLayout(new ParagraphLayout());

		getContentPane().add(new JLabel(app.getFieldLabel("language")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(languageDropdown.getComponent());

		getContentPane().add(new JLabel(app.getFieldLabel("dateformat")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(dateFormatDropdown.getComponent());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		owner.centerDlg(this);
		setResizable(true);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == ok)
		{
			owner.getApp().setCurrentDateFormatCode(dateFormatDropdown.getText());
			owner.getApp().setCurrentLanguage(languageDropdown.getText());
		}
		dispose();
	}

	// ChangeListener interface
	public void stateChanged(ChangeEvent event) {}

	private UiMainWindow owner;
	private UiChoiceEditor languageDropdown;
	private UiChoiceEditor dateFormatDropdown;
	private JButton ok;
	private JButton cancel;
}
