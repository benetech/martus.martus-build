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
package org.martus.client.test;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.test.TestCaseEnhanced;
import org.martus.util.MartusFlexidate;

public class TestMartusFlexidate extends TestCaseEnhanced
{
	
	public TestMartusFlexidate(String name)
	{
		super(name);
	}
	
	public void testFlexiDate()
	{
		MartusFlexidate mf = new MartusFlexidate("20030105+2");		
		assertEquals("20030105+2", mf.getMatusFlexidate());
		
		DateFormat df = Bulletin.getStoredDateFormat();						
		assertEquals("2003-01-05", df.format(mf.getBeginDate()));
		assertEquals("2003-01-07", df.format(mf.getEndDate()));																
	}
		
	public void testFlexiDateOverMonths()
	{
		MartusFlexidate mf = new MartusFlexidate("20030105+120");		
		assertEquals("20030105+120", mf.getMatusFlexidate());

		DateFormat df = Bulletin.getStoredDateFormat();						
		assertEquals("2003-01-05", df.format(mf.getBeginDate()));
		assertEquals("2003-05-05", df.format(mf.getEndDate()));
	
	}
	
	public void testFlexiDateOverYear()
	{
		MartusFlexidate mf = new MartusFlexidate("20020105+366");		
		assertEquals("20020105+366", mf.getMatusFlexidate());

		DateFormat df = Bulletin.getStoredDateFormat();						
		assertEquals("2002-01-05", df.format(mf.getBeginDate()));
		assertEquals("2003-01-06", df.format(mf.getEndDate()));		
	}
	
	
	public void testExactDate()
	{
		MartusFlexidate mf = new MartusFlexidate("20030105");
		
		assertEquals("20030105+0", mf.getMatusFlexidate());
		DateFormat df = Bulletin.getStoredDateFormat();				
		
		assertEquals("2003-01-05", df.format(mf.getBeginDate()));
		assertEquals("2003-01-05", df.format(mf.getEndDate()));			
	}	
	
	public void testDateRange()
	{
		Date beginDate = getDate(2000,Calendar.JANUARY,10);
		Date endDate = getDate(2000,Calendar.JANUARY, 15);
						
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
		
		assertEquals("20000110+5", mf.getMatusFlexidate());	
		
		DateFormat df = Bulletin.getStoredDateFormat();										
		assertEquals("2000-01-10", df.format(mf.getBeginDate()));
		assertEquals("2000-01-15", df.format(mf.getEndDate()));			
	}	
	
	public void testSameDateRange()
	{
		Date beginDate = getDate(2000,Calendar.JANUARY,10);
		Date endDate = getDate(2000,Calendar.JANUARY, 10);
		
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);

		assertEquals("20000110+0", mf.getMatusFlexidate());	

		DateFormat df = Bulletin.getStoredDateFormat();										
		assertEquals("2000-01-10", df.format(mf.getBeginDate()));
		assertEquals("2000-01-10", df.format(mf.getEndDate()));
		
		mf = new MartusFlexidate("20030105+0");		
		assertEquals("20030105+0", mf.getMatusFlexidate());

		df = Bulletin.getStoredDateFormat();						
		assertEquals("2003-01-05", df.format(mf.getBeginDate()));
		assertEquals("2003-01-05", df.format(mf.getEndDate()));			
	}
	
	public void testDateRangeSwap()
	{
		Date beginDate = getDate(2000,Calendar.JANUARY,10);
		Date endDate = getDate(1999,Calendar.JANUARY, 15);
					
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
	
		assertEquals("19990115+360", mf.getMatusFlexidate());	
	
		DateFormat df = Bulletin.getStoredDateFormat();										
		assertEquals("1999-01-15", df.format(mf.getBeginDate()));
		assertEquals("2000-01-10", df.format(mf.getEndDate()));	
	}
	
	public void testCreateFromMartusString()
	{
		MartusFlexidate mfd = MartusFlexidate.createFromMartusDateString("2000-01-10");
		DateFormat df = Bulletin.getStoredDateFormat();										
		assertEquals("2000-01-10", df.format(mfd.getBeginDate()));	
		
		mfd = MartusFlexidate.createFromMartusDateString("2000-01-10,20000101+0");
		df = Bulletin.getStoredDateFormat();										
		assertEquals("single begin", "2000-01-01", df.format(mfd.getBeginDate()));
		assertEquals("single end", "2000-01-01", df.format(mfd.getEndDate()));
		
		mfd = MartusFlexidate.createFromMartusDateString("2000-01-10,20001203+5");
		df = Bulletin.getStoredDateFormat();										
		assertEquals("range begin","2000-12-03", df.format(mfd.getBeginDate()));
		assertEquals("range end","2000-12-08", df.format(mfd.getEndDate()));						
	}
	
	public void testCreateInvalidDateFromMartusString()
	{
		MartusFlexidate mfd = MartusFlexidate.createFromMartusDateString("185[01-10");
		DateFormat df = Bulletin.getStoredDateFormat();										
		assertEquals("1900-01-01", df.format(mfd.getBeginDate()));					
	}

	private Date getDate(int year, int month, int day)
	{			
		Calendar cal = new GregorianCalendar();
		cal.set(year,month,day);		
						
		return (Date) cal.getTime();
	}	
}
