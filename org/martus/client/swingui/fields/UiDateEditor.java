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

package org.martus.client.swingui.fields;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.martus.client.core.DateUtilities;
import org.martus.client.swingui.UiLocalization;
import org.martus.common.bulletin.Bulletin;

public class UiDateEditor extends UiField
{
	public UiDateEditor(UiLocalization localizationToUse)
	{				
		component = new JPanel();
		Box box = Box.createHorizontalBox();
		dayCombo = new JComboBox();
		monthCombo = new JComboBox(localizationToUse.getMonthLabels());
		yearCombo = new JComboBox();
		
		buildDate(box, localizationToUse, yearCombo, monthCombo, dayCombo);
		
		component.add(box);
	}
	
	public static void buildDate(Box box, UiLocalization localizationToUse,
			JComboBox yCombo, JComboBox mCombo, JComboBox dCombo)
	{						
		for(int day=1; day <= 31; ++day)
			dCombo.addItem(new Integer(day).toString());
	
		Calendar cal = new GregorianCalendar();
		int thisYear = cal.get(Calendar.YEAR);
		for(int year = 1900; year <= thisYear; ++year)
			yCombo.addItem(new Integer(year).toString());

		String mdyOrder = DateUtilities.getMdyOrder(localizationToUse.getCurrentDateFormatCode());
		for(int i = 0; i < mdyOrder.length(); ++i)
		{
			switch(mdyOrder.charAt(i))
			{
				case 'd': box.add(dCombo);	break;
				case 'm': box.add(mCombo);	break;
				case 'y': box.add(yCombo);	break;
			}
		}			
	}			

	public JComponent getComponent()
	{
		return component;
	}

	public JComponent[] getFocusableComponents()
	{
		return new JComponent[]{dayCombo, monthCombo, yearCombo};
	}

	public static class DateFutureException extends UiField.DataInvalidException
	{
		public DateFutureException()
		{
			super();
		}
		public DateFutureException(String tag)
		{
			super(tag);
		}
	}
	
	public void validate() throws UiField.DataInvalidException 
	{
		Date value = getDate(yearCombo, monthCombo, dayCombo);
		Date today = new Date();
		if (value.after(today))
		{
			dayCombo.requestFocus();	
			throw new DateFutureException();
		}
	}

	public String getText()
	{
		Date date = getDate(yearCombo, monthCombo, dayCombo);
		DateFormat df = Bulletin.getStoredDateFormat();
		return df.format(date);
	}

	public static Date getDate(JComboBox yCombo, JComboBox mCombo, JComboBox dCombo) 
	{
		Calendar cal = new GregorianCalendar();
		cal.set(yCombo.getSelectedIndex()+1900,
				mCombo.getSelectedIndex(),
				dCombo.getSelectedIndex()+1);
		
		return cal.getTime();
	}

	public void setText(String newText)
	{
		setDate(newText, yearCombo, monthCombo, dayCombo);			
	}
	
	public static void setDate(String dateText, JComboBox yCombo, JComboBox mCombo, JComboBox dCombo)
	{
		DateFormat df = Bulletin.getStoredDateFormat();
		Date d = null;
		try
		{
			d = df.parse(dateText);
			Calendar cal = new GregorianCalendar();
			cal.setTime(d);
		
		yCombo.setSelectedItem( (new Integer(cal.get(Calendar.YEAR))).toString());
		mCombo.setSelectedIndex(cal.get(Calendar.MONTH));
		dCombo.setSelectedItem( (new Integer(cal.get(Calendar.DATE))).toString());

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

