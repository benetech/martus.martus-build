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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.martus.common.Bulletin;

public class UiDateEditor extends UiField
{
	public UiDateEditor(MartusLocalization localizationToUse)
	{
		component = new JPanel();
		Box box = Box.createHorizontalBox();

		dayCombo = new JComboBox();
		for(int day=1; day <= 31; ++day)
			dayCombo.addItem(new Integer(day).toString());

		monthCombo = new JComboBox(localizationToUse.getMonthLabels());

		yearCombo = new JComboBox();
		Calendar cal = new GregorianCalendar();
		int thisYear = cal.get(Calendar.YEAR);
		for(int year = 1900; year <= thisYear; ++year)
			yearCombo.addItem(new Integer(year).toString());

		String mdyOrder = DateUtilities.getMdyOrder(localizationToUse.getCurrentDateFormatCode());
		for(int i = 0; i < mdyOrder.length(); ++i)
		{
			switch(mdyOrder.charAt(i))
			{
				case 'd': box.add(dayCombo);	break;
				case 'm': box.add(monthCombo);	break;
				case 'y': box.add(yearCombo);	break;
			}
		}

		component.add(box);
	}

	public JComponent getComponent()
	{
		return component;
	}

	public String getText()
	{
		Calendar cal = new GregorianCalendar();
		cal.set(yearCombo.getSelectedIndex()+1900,
				monthCombo.getSelectedIndex(),
				dayCombo.getSelectedIndex()+1);

		Date d = cal.getTime();
		DateFormat df = Bulletin.getStoredDateFormat();
		return df.format(d);
	}

	public void setText(String newText)
	{
		DateFormat df = Bulletin.getStoredDateFormat();
		Date d;
		try
		{
			d = df.parse(newText);
			Calendar cal = new GregorianCalendar();
			cal.setTime(d);
			yearCombo.setSelectedItem( (new Integer(cal.get(Calendar.YEAR))).toString());
			monthCombo.setSelectedIndex(cal.get(Calendar.MONTH));
			dayCombo.setSelectedItem( (new Integer(cal.get(Calendar.DATE))).toString());
		}
		catch(ParseException e)
		{
			System.out.println(e);
		}
	}

	public void disableEdits()
	{
		yearCombo.setEnabled(false);
		monthCombo.setEnabled(false);
		dayCombo.setEnabled(false);
	}

	public void indicateEncrypted(boolean isEncrypted)
	{
	}

	JComponent component;
	JComboBox monthCombo;
	JComboBox dayCombo;
	JComboBox yearCombo;
}

