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
import javax.swing.JTextField;

import org.martus.common.Bulletin;

public class UiSearchDlg extends JDialog  implements ActionListener
{
	public UiSearchDlg(UiMainWindow owner)
	{
		super(owner, "", true);
		UiLocalization localization = owner.getLocalization();

		setTitle(localization.getWindowTitle("search"));
		search = new JButton(localization.getButtonLabel("search"));
		search.addActionListener(this);
		getRootPane().setDefaultButton(search);
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		getContentPane().setLayout(new ParagraphLayout());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new UiWrappedTextArea(localization.getFieldLabel("SearchBulletinRules")));

		searchField = new JTextField(40);
		searchField.setText(searchString);
		getContentPane().add(new JLabel(localization.getFieldLabel("SearchEntry")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(searchField);

		startDateEditor = new UiDateEditor(localization);
		if(startDate.length() == 0)
			startDate = DEFAULT_SEARCH_START_DATE;
		startDateEditor.setText(startDate);
		getContentPane().add(new JLabel(localization.getFieldLabel("SearchStartDate")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(startDateEditor.getComponent());

		endDateEditor = new UiDateEditor(localization);
		if(endDate.length() == 0)
			endDate = Bulletin.getLastDayOfThisYear();
		endDateEditor.setText(endDate);
		getContentPane().add(new JLabel(localization.getFieldLabel("SearchEndDate")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(endDateEditor.getComponent());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(search);
		getContentPane().add(cancel);

		UiUtilities.centerDlg(this);
		setResizable(true);
		show();

	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == search)
		{
			searchString = searchField.getText();
			startDate = startDateEditor.getText();
			endDate = endDateEditor.getText();
			result = true;
		}
		dispose();
	}


	public boolean getResults()
	{
		return result;
	}

	public String getSearchString()
	{
		return searchString;
	}

	public String getStartDate()
	{
		return startDate;
	}

	public String getEndDate()
	{
		return endDate;
	}

	boolean result;
	static String searchString = "";
	static String startDate = "";
	static String endDate = "";

	JButton search;
	JTextField searchField;
	UiDateEditor startDateEditor;
	UiDateEditor endDateEditor;

	final String DEFAULT_SEARCH_START_DATE = "1900-01-01";
}
