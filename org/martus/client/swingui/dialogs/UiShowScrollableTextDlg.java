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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;



public class UiShowScrollableTextDlg extends JDialog implements ActionListener
{
	public UiShowScrollableTextDlg(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text)
	{
		this(owner, titleTag, okButtonTag, cancelButtonTag, descriptionTag, text, (Map)(new HashMap()));
	}
	
	public UiShowScrollableTextDlg(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text, Map tokenReplacement)
	{
		super(owner, "", true);
		mainWindow = owner;

		try 
		{
			UiLocalization localization = mainWindow.getLocalization();
			String windowTitle = localization.getWindowTitle(titleTag);
			setTitle(TokenReplacement.replaceTokens(windowTitle, tokenReplacement));
			String buttonLabel = localization.getButtonLabel(okButtonTag);
			ok = new JButton(TokenReplacement.replaceTokens(buttonLabel, tokenReplacement));
			ok.addActionListener(this);
			JButton cancel = null;
			if(cancelButtonTag.length() != 0)
			{
				buttonLabel = localization.getButtonLabel(cancelButtonTag);
				cancel = new JButton(TokenReplacement.replaceTokens(buttonLabel, tokenReplacement));
				cancel.addActionListener(this);
			}
			
			details = new UiTextArea(15, 65);
			details.setLineWrap(true);
			details.setWrapStyleWord(true);
			details.setEditable(false);
			JScrollPane detailScrollPane = new JScrollPane(details, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			details.setText(TokenReplacement.replaceTokens(text, tokenReplacement));
			
			getContentPane().setLayout(new ParagraphLayout());
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			String fieldLabel = localization.getFieldLabel(descriptionTag);
			fieldLabel = TokenReplacement.replaceTokens(fieldLabel, tokenReplacement);
			getContentPane().add(new UiWrappedTextArea(fieldLabel));
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			getContentPane().add(detailScrollPane);
			
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			getContentPane().add(ok);
			if(cancelButtonTag.length() != 0)
			{
				getContentPane().add(cancel);
			}
			
			getRootPane().setDefaultButton(ok);
			Utilities.centerDlg(this);
			show();
		} 
		catch (TokenInvalidException e) 
		{
			e.printStackTrace();
		}
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
