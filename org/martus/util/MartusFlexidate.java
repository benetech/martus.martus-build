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

package org.martus.util;

import java.util.Calendar;
import java.util.Date;

import org.hrvd.util.date.Flexidate;
import org.hrvd.util.date.FlexidateFormat;

public class MartusFlexidate
{
	public MartusFlexidate(String dateStr)
	{
		parseString(dateStr);			
	}		
	
	public MartusFlexidate(Date bDate, Date eDate)
	{
		setDateRange(bDate, eDate);
	}
		
	private void setDate(Date date, int range)
	{
		flexiDate = new Flexidate(date, range);		
	}
	
	private void setDateRange(Date beginDate, Date endDate)
	{	
		flexiDate = new Flexidate(beginDate, endDate);	
	}
	
	private void parseString(String str)
	{
		int plus = str.indexOf(PLUS);
		String dateStr = null;
		int range =0;
		Date date = null;
			
		if (plus > 0)
		{
			dateStr = str.substring(0, plus);
			String rangeStr = str.substring(plus+1);
			range = new Integer(rangeStr).intValue();			
		}					
		flexiDate = new Flexidate(new Long(dateStr).longValue(), range);
	}	
	
	public String getMatusFlexidate()
	{		
		return flexiDate.getDateAsNumber()+PLUS+flexiDate.getRange();
	}	
	
	public Date getBegingDate()
	{	
		Calendar cal = flexiDate.getCalendarLow();	
						
		return (Date)cal.getTime();		
	}
	
	public Date getEndDate()
	{			
		Calendar cal = flexiDate.getCalendarHigh();
		
		return (Date) ((hasDateRange())? cal.getTime(): getBegingDate());
	}	
	
	private boolean hasDateRange()
	{
		return (flexiDate.getRange() > 0)? true:false;
	}	
	
	private FlexidateFormat getFormat()
	{
		return FlexidateFormat.getFormat();
	}
		
	Flexidate flexiDate;
	public static final String PLUS = "+";	
}
