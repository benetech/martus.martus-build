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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.martus.client.swingui.UiLocalization;
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
		MartusFlexidate mfd = MartusFlexidate.createFromMartusDateString(newText);
		
		String rawBeginDate = MartusFlexidate.toStoredDateFormat(mfd.getBeginDate());
		String rawEndDate = MartusFlexidate.toStoredDateFormat(mfd.getEndDate());
		
		String beginDate = localization.convertStoredDateToDisplay(rawBeginDate);
		String endDate = localization.convertStoredDateToDisplay(rawEndDate);
				
		String display = "";
		
		if (mfd.hasDateRange())
			display = localization.getFieldLabel("DateRangeFrom")+ SPACE + 
				beginDate + SPACE + localization.getFieldLabel("DateRangeTo")+
				SPACE + endDate;		
		else
			display = beginDate;
				
		label.setText(SPACE + display + SPACE);
	}	

	public void disableEdits()
	{
	}

	UiLocalization localization;
	JLabel label;
	private static String	SPACE = " ";	
}
