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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;



public class UiShowScrollableTextDlg extends JDialog implements ActionListener
{
	public UiShowScrollableTextDlg(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text)
	{
		super(owner, "", true);
		mainWindow = owner;

		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(titleTag));
		ok = new JButton(localization.getButtonLabel(okButtonTag));
		ok.addActionListener(this);
		JButton cancel = new JButton(localization.getButtonLabel(cancelButtonTag));
		cancel.addActionListener(this);

		details = new UiTextArea(15, 65);
		details.setLineWrap(true);
		details.setWrapStyleWord(true);
		JScrollPane detailScrollPane = new JScrollPane(details, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		details.setText(text);

		getContentPane().setLayout(new ParagraphLayout());
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new UiWrappedTextArea(localization.getFieldLabel(descriptionTag)));
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(detailScrollPane);

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);
		UiUtilities.centerDlg(this);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
			result = true;
		dispose();
	}

	public boolean getResult()
	{
		return result;
	}
	
	JButton ok;
	UiTextArea details;
	boolean result;
	UiMainWindow mainWindow;
}
