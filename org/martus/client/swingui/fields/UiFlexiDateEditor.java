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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.martus.client.core.DateUtilities;
import org.martus.client.swingui.UiLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.ParagraphLayout;

public class UiFlexiDateEditor extends UiField
{
	public UiFlexiDateEditor(UiLocalization localization)
	{
		localizationToUse = localization;
		component = new JPanel();		
		component.setLayout(new ParagraphLayout());		
		Box boxDateSelection = Box.createHorizontalBox();				
		exactDateRB = new JRadioButton("Exact Date", true);			
		flexiDateRB = new JRadioButton("Date Range");
		
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(exactDateRB);
		radioGroup.add(flexiDateRB);		
	
		boxDateSelection.add(exactDateRB);
		boxDateSelection.add(flexiDateRB);	
		component.add(boxDateSelection);
			
		buildFlexiDatePanel();
		buildExactDatePanel();	
				
		flexiDateRB.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e)
			{
				if (flexiDateRB.isSelected())
				{							
					component.remove(extDatePanel);		
					buildFlexiDatePanel();
					component.add(flexiDatePanel, ParagraphLayout.NEW_PARAGRAPH);																	
					component.revalidate();								
				}				
			}
		});
		exactDateRB.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e)
			{
				if (exactDateRB.isSelected())
				{			
					component.remove(flexiDatePanel);
					buildExactDatePanel();					
					component.add(extDatePanel, ParagraphLayout.NEW_PARAGRAPH);											
					component.revalidate();				
				}															
			}
		});
							  							
		component.add(extDatePanel, ParagraphLayout.NEW_PARAGRAPH);
	}	
	
	private void buildFlexiDatePanel()
	{	
		if (flexiDatePanel == null)
			flexiDatePanel = new JPanel();
		else
			flexiDatePanel.removeAll();
			
		flexiDatePanel.setLayout(new ParagraphLayout());								
		flexiDatePanel.add(new JLabel("Between"));
		
		if (bgDateBox == null)
			buildBeginDateBox();	
					
		flexiDatePanel.add(bgDateBox);
					
		flexiDatePanel.add(new JLabel("and"), ParagraphLayout.NEW_PARAGRAPH);
		if (endDateBox == null)
			buildEndDateBox();
			
		flexiDatePanel.add(endDateBox);
	}
	
	private void buildExactDatePanel()
	{
		if (extDatePanel == null)
		{		
			extDatePanel = new JPanel();
			extDatePanel.setLayout(new ParagraphLayout());			
		}
		else	
			extDatePanel.removeAll();							
												
		if (bgDateBox == null)
			buildBeginDateBox();
			
		extDatePanel.add(new JLabel(" "));				
		extDatePanel.add( bgDateBox);			
	}
				
	private void buildBeginDateBox()
	{		
		bgDateBox = Box.createHorizontalBox();
	
		if (bgDayCombo == null)
		{									
			bgDayCombo = new JComboBox();	
			bgMonthCombo = new JComboBox(localizationToUse.getMonthLabels());
			bgYearCombo = new JComboBox();
		}				
		buildDate(bgDateBox, bgDayCombo, bgMonthCombo, bgYearCombo);											
	}

	private void buildEndDateBox()
	{
		endDateBox = Box.createHorizontalBox();
		endDayCombo = new JComboBox();	
		endMonthCombo = new JComboBox(localizationToUse.getMonthLabels());
		endYearCombo = new JComboBox();				
		buildDate(endDateBox, endDayCombo, endMonthCombo, endYearCombo);		
	}
	
	private void buildDate(Box box, JComboBox dayCombo, JComboBox monthCombo, JComboBox yearCombo)
	{						
		for(int day=1; day <= 31; ++day)
			dayCombo.addItem(new Integer(day).toString());
		
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
	}				

	public JComponent getComponent()
	{
		return component;
	}

	public JComponent[] getFocusableComponents()
	{
		return (flexiDateRB.isSelected())?
			new JComponent[]{exactDateRB, flexiDateRB, bgDayCombo, bgMonthCombo, bgYearCombo,
							endDayCombo, endMonthCombo, endYearCombo}:
			new JComponent[]{exactDateRB, flexiDateRB, bgDayCombo, bgMonthCombo, bgYearCombo,};
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
		Date today = new Date();
		if (getBeginDate().after(today))
		{
			bgDayCombo.requestFocus();	
			throw new DateFutureException();
		}			
	
		if (flexiDateRB.isSelected())
		{		
			if (getEndDate().after(today) || getEndDate().before(getBeginDate()))
			{
				bgDayCombo.requestFocus();	
				throw new DateFutureException();				
			}
		}		
	}

	public String getText()
	{
		DateFormat df = Bulletin.getStoredDateFormat();
		StringBuffer dateText = new StringBuffer();
		
		if (flexiDateRB.isSelected())		
			dateText.append(df.format(getBeginDate())).append(",").append(df.format(getEndDate()));		
		else
			dateText.append(df.format(getBeginDate()));
		
		return dateText.toString();
	}

	private Date getBeginDate() 
	{
		Calendar cal = new GregorianCalendar();
		cal.set(bgYearCombo.getSelectedIndex()+1900,
				bgMonthCombo.getSelectedIndex(),
				bgDayCombo.getSelectedIndex()+1);
		
		Date d = cal.getTime();
		return d;
	}
	
	private Date getEndDate() 
	{
		Calendar cal = new GregorianCalendar();
		cal.set(endYearCombo.getSelectedIndex()+1900,
				endMonthCombo.getSelectedIndex(),
				endDayCombo.getSelectedIndex()+1);
	
		Date d = cal.getTime();
		return d;
	}	

	public void setText(String newText)
	{		
		String bgDateText = newText;
		int comma = newText.indexOf(",");						
		if (comma > 0)
		{			
			flexiDateRB.setSelected(true);
			bgDateText = newText.substring(0,comma);
			setDate(newText.substring(comma+1), endDayCombo, endMonthCombo, endYearCombo); 									
		}			
		setDate(bgDateText, bgDayCombo, bgMonthCombo, bgYearCombo);			
	}
	
	private void setDate(String dateText, JComboBox dCombo, JComboBox mCombo, JComboBox yCombo)
	{
		DateFormat df = Bulletin.getStoredDateFormat();
		Date d;
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
		if (flexiDateRB.isSelected())
		{
			endYearCombo.setEnabled(false);
			endMonthCombo.setEnabled(false);
			endDayCombo.setEnabled(false);	
		}
				
		bgYearCombo.setEnabled(false);
		bgMonthCombo.setEnabled(false);
		bgDayCombo.setEnabled(false);		
	}	

	public void indicateEncrypted(boolean isEncrypted)
	{
	}	

	JComponent 					component;
	
	JComboBox 					bgMonthCombo;
	JComboBox 					bgDayCombo;
	JComboBox 					bgYearCombo;
	
	JComboBox 					endMonthCombo;
	JComboBox 					endDayCombo;
	JComboBox 					endYearCombo;
		
	private UiLocalization 		localizationToUse;	
	private JRadioButton 		exactDateRB;
	private JRadioButton 		flexiDateRB;
	private JPanel 				flexiDatePanel;
	private JPanel 				extDatePanel;
	private Box					bgDateBox;
	private Box 				endDateBox;		
}
