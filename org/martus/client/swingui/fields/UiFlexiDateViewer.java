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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.martus.client.swingui.UiLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.util.MartusFlexidate;


public class UiFlexiDateViewer extends UiField
{
	public UiFlexiDateViewer(UiLocalization localizationToUse)
	{
		localization = localizationToUse;
		label = new JLabel();
	}

	public JComponent getComponent()
	{
		return label;
	}

	public String getText()
	{	
		return "";
	}

	public void setText(String newText)
	{
		value = localization.convertStoredDateToDisplay(newText);
		int dateBreak = newText.indexOf(MartusFlexidate.DATE_RANGE_SEPARATER);
		int datePlus = newText.indexOf(MartusFlexidate.FLEXIDATE_RANGE_DELIMITER);
		
		if (dateBreak > 0 && newText.charAt(datePlus+1) != '0')			
		{
			String beginDate = newText.substring(0,dateBreak);
			String endDate = convertEndDate(newText.substring(dateBreak+1));						
			label.setText("  "+localization.getFieldLabel("DateRangeFrom")+" "+ beginDate+ 
					" "+localization.getFieldLabel("DateRangeTo")+" "+ endDate +"  ");				
		}
		else
			label.setText("  " + value + "  ");
	}
	
	private String convertEndDate(String endDate)	
	{
//		if (endDate.indexOf(MartusFlexidate.FLEXIDATE_RANGE_DELIMITER) <=0)
//			return endDate;
			
		MartusFlexidate mf = new MartusFlexidate(endDate);
		DateFormat df = Bulletin.getStoredDateFormat();				

		return df.format(mf.getEndDate());
	}

	public void disableEdits()
	{
	}

	UiLocalization localization;
	JLabel label;
	String value;	
}
