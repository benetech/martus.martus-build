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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.martus.swing.*;



public class UiDisplayFileDlg extends JDialog
{
	public UiDisplayFileDlg(UiMainWindow owner, String baseTag, InputStream fileStream, String tagMessage, InputStream fileStreamToc, String tagTOCMessage)
	{
		super(owner, "", true);
		tocList = null;
		UiLocalization localization = owner.getLocalization();

		setTitle(localization.getWindowTitle(baseTag));
		getContentPane().setLayout(new ParagraphLayout());

		message = getFileContents(fileStream);
		if(message == null)
		{
			dispose();
			return;
		}

		msgArea = new UiWrappedTextArea(message);
		msgArea.addKeyListener(new TabToOkButton());
		msgArea.setRows(14);
		msgArea.setColumns(80);
		msgAreaScrollPane = new JScrollPane(msgArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		Vector messageTOC = getFileVectorContents(fileStreamToc);
		if(messageTOC != null)
		{
			tocList = new JList(messageTOC);
			tocList.addListSelectionListener(new ListHandler());
			JScrollPane tocMsgAreaScrollPane = new JScrollPane(tocList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			tocMsgAreaScrollPane.setPreferredSize(new Dimension(580, 100));
			getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
			getContentPane().add(new JLabel(localization.getFieldLabel(tagTOCMessage+"Description")));
			getContentPane().add(new JLabel(localization.getFieldLabel(tagTOCMessage)), ParagraphLayout.NEW_PARAGRAPH);
			getContentPane().add(tocMsgAreaScrollPane);
			tocList.setSelectedIndex(0);
		}

		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		ok.addKeyListener(new MakeEnterKeyExit());

		getContentPane().add(new JLabel(localization.getFieldLabel(tagMessage)), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(msgAreaScrollPane);
		getContentPane().add(ok, ParagraphLayout.NEW_PARAGRAPH);
		getRootPane().setDefaultButton(ok);
		ok.requestFocus();

		Utilities.centerDlg(this);
		setResizable(true);
		show();
	}

	public String getFileContents(InputStream fileStream)
	{
		StringBuffer message = new StringBuffer();
		if(fileStream == null)
		{
			System.out.println("UiDisplayFileDlg: null stream");
			return null;
		}
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
			while(true)
			{
				String lineIn = reader.readLine();
				if(lineIn == null)
					break;
				message.append(lineIn);
				message.append('\n');
			}
			reader.close();
		}
		catch(IOException e)
		{
			System.out.println("UiDisplayFileDlg: " + e);
			return null;
		}
		return new String(message);
	}

	public Vector getFileVectorContents(InputStream fileStream)
	{
		Vector message = new Vector();
		if(fileStream == null)
		{
			System.out.println("UiDisplayFileDlg: null stream");
			return null;
		}
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
			while(true)
			{
				String lineIn = reader.readLine();
				if(lineIn == null)
					break;
				message.add(lineIn);
			}
			reader.close();
		}
		catch(IOException e)
		{
			System.out.println("UiDisplayFileDlg: " + e);
			return null;
		}
		return message;
	}


	public void findAndScrollToItem()
	{
		msgArea.setCaretPosition(message.length());
		msgAreaScrollPane.getVerticalScrollBar().setValue(msgAreaScrollPane.getVerticalScrollBar().getMaximum());
		int foundAt = message.indexOf("-\n" + (String)tocList.getSelectedValue());
		if(foundAt < 0)
			foundAt = 0;
		msgArea.setCaretPosition(foundAt);
	}

	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent arg0)
		{
			findAndScrollToItem();
		}
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	class MakeEnterKeyExit extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				dispose();
		}
	}

	class TabToOkButton extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_TAB)
			{
				ok.requestFocus();
			}
		}
	}
	String message;
	JButton ok;
	JList tocList;
	UiWrappedTextArea msgArea;
	JScrollPane msgAreaScrollPane;
}
