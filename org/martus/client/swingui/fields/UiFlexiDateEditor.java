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
import java.util.Date;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.martus.client.swingui.UiLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.ParagraphLayout;
import org.martus.util.MartusFlexidate;

public class UiFlexiDateEditor extends UiField
{
	public UiFlexiDateEditor(UiLocalization localization)
	{
		localizationToUse = localization;
		init();
	}	
	
	private void init()
	{
		component = new JPanel();		
		component.setLayout(new ParagraphLayout());		
		Box boxDateSelection = Box.createHorizontalBox();				
		exactDateRB = new JRadioButton(localizationToUse.getFieldLabel("DateExact"), true);			
		flexiDateRB = new JRadioButton(localizationToUse.getFieldLabel("DateRange"));		

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(exactDateRB);
		radioGroup.add(flexiDateRB);		

		boxDateSelection.add(exactDateRB);
		boxDateSelection.add(flexiDateRB);	
		component.add(boxDateSelection);
						
		flexiDateRB.addItemListener(new RadioItemListener());
		exactDateRB.addItemListener(new RadioItemListener());
					  							
		component.add(buildExactDatePanel(), ParagraphLayout.NEW_PARAGRAPH);
	}
	
	private JPanel buildFlexiDatePanel()
	{			
		flexiDatePanel = new JPanel();		
			
		flexiDatePanel.setLayout(new ParagraphLayout());								
		flexiDatePanel.add(new JLabel(localizationToUse.getFieldLabel("DateRangeFrom")));		
		flexiDatePanel.add(buildBeginDateBox());
					
		flexiDatePanel.add(new JLabel(localizationToUse.getFieldLabel("DateRangeTo")), ParagraphLayout.NEW_PARAGRAPH);							
		flexiDatePanel.add(buildEndDateBox());
		
		return flexiDatePanel;
	}
	
	private JPanel buildExactDatePanel()
	{		
		extDatePanel = new JPanel();		
		extDatePanel.setLayout(new ParagraphLayout());
																
		JLabel dummy1 = new JLabel(localizationToUse.getFieldLabel("DateRangeFrom"));		
		extDatePanel.add(dummy1);					
		extDatePanel.add( buildBeginDateBox());
		JLabel dummy2 = new JLabel(localizationToUse.getFieldLabel("DateRangeFrom"));
		extDatePanel.add(dummy2, ParagraphLayout.NEW_PARAGRAPH);
		dummy1.setForeground(extDatePanel.getBackground());
		dummy2.setForeground(extDatePanel.getBackground());
				
		return extDatePanel;			
	}
				
	private Box buildBeginDateBox()
	{		
		Box bgDateBox = Box.createHorizontalBox();
		if (bgDayCombo  == null)
		{									
			bgDayCombo = new JComboBox();	
			bgMonthCombo = new JComboBox(localizationToUse.getMonthLabels());
			bgYearCombo = new JComboBox();
		}
		UiDateEditor.buildDate(bgDateBox, localizationToUse, bgYearCombo, bgMonthCombo, bgDayCombo);
		return bgDateBox;											
	}

	private Box buildEndDateBox()
	{
		Box endDateBox = Box.createHorizontalBox();
		if (endDayCombo == null)
		{		
			endDayCombo = new JComboBox();	
			endMonthCombo = new JComboBox(localizationToUse.getMonthLabels());
			endYearCombo = new JComboBox();
		}				
		UiDateEditor.buildDate(endDateBox, localizationToUse, endYearCombo, endMonthCombo, endDayCombo);
		return endDateBox;		
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

	private final class RadioItemListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			if (isFlexiDate())
			{											
				component.remove(extDatePanel);						
				component.add(buildFlexiDatePanel(), ParagraphLayout.NEW_PARAGRAPH);																	
				component.revalidate();								
			}
			
			if (isExactDate())
			{									
				component.remove(flexiDatePanel);						
				component.add(buildExactDatePanel(), ParagraphLayout.NEW_PARAGRAPH);											
				component.revalidate();				
			}			
		}
	}

	public void validate() throws UiField.DataInvalidException 
	{
		Date today = new Date();
		if (getBeginDate().after(today))
		{
			bgDayCombo.requestFocus();	
			throw new UiDateEditor.DateFutureException();
		}			
	
		if (isFlexiDate())
		{		
			if (getEndDate().after(today) || getEndDate().before(getBeginDate()))
			{
				bgDayCombo.requestFocus();	
				throw new UiDateEditor.DateFutureException();				
			}
		}		
	}
	
	private boolean isFlexiDate()
	{
		return flexiDateRB.isSelected();
	}
	
	private boolean isExactDate()
	{
		return exactDateRB.isSelected();
	}

	public String getText()
	{
		DateFormat df = Bulletin.getStoredDateFormat();				
		String dateText = df.format(getBeginDate());
		
		if (isFlexiDate())	
			dateText += MartusFlexidate.DATE_RANGE_SEPARATER + toFlexidate();							
		return dateText;
	}	
	
	private String toFlexidate()
	{
		MartusFlexidate mf = new MartusFlexidate(getBeginDate(), getEndDate());
		return mf.getMatusFlexidate();
	}
	
	private String fromFlexidate(String date)
	{
		if (date.indexOf(MartusFlexidate.FLEXIDATE_RANGE_DELIMITER) <=0 )
			return date;
			
		MartusFlexidate mf = new MartusFlexidate(date);				
		DateFormat df = Bulletin.getStoredDateFormat();				
					
		return df.format(mf.getEndDate());	
	}

	private Date getBeginDate() 
	{		
		return UiDateEditor.getDate(bgYearCombo, bgMonthCombo, bgDayCombo);
	}
	
	private Date getEndDate() 
	{				
		return UiDateEditor.getDate(endYearCombo, endMonthCombo, endDayCombo);
	}	
		
	public void setText(String newText)
	{		
		String bgDateText = newText;
		int comma = newText.indexOf(MartusFlexidate.DATE_RANGE_SEPARATER);						
		if (comma > 0)
		{			
			flexiDateRB.setSelected(true);
			bgDateText = newText.substring(0,comma);
			String endDateText = newText.substring(comma+1);
			
			UiDateEditor.setDate(fromFlexidate(endDateText), endYearCombo, endMonthCombo, endDayCombo); 									
		}			
		UiDateEditor.setDate(bgDateText, bgYearCombo, bgMonthCombo, bgDayCombo);			
	}
		
	public void disableEdits()
	{
		if (isFlexiDate())
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
}
