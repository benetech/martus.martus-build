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

import java.awt.BorderLayout;
import java.awt.Dimension;
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
		component.setLayout(new BorderLayout());		
				
		Box boxDateSelection = Box.createHorizontalBox();				
		exactDateRB = new JRadioButton(localizationToUse.getFieldLabel("DateExact"), true);			
		flexiDateRB = new JRadioButton(localizationToUse.getFieldLabel("DateRange"));		

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(exactDateRB);
		radioGroup.add(flexiDateRB);		

		boxDateSelection.add(exactDateRB);
		boxDateSelection.add(flexiDateRB);			
						
		flexiDateRB.addItemListener(new RadioItemListener());
		exactDateRB.addItemListener(new RadioItemListener());
		
		component.add(boxDateSelection, BorderLayout.NORTH);
		component.add(buildExactDatePanel(), BorderLayout.CENTER);	
	}
	
	private JPanel buildFlexiDatePanel()
	{	
		flexiDatePanel = new JPanel();
		flexiDatePanel.setLayout(new ParagraphLayout());	
											
		flexiDatePanel.add(new JLabel(localizationToUse.getFieldLabel("DateRangeFrom")));		
		flexiDatePanel.add(buildBeginDateBox());
				
		flexiDatePanel.add(new JLabel(localizationToUse.getFieldLabel("DateRangeTo")));			
		flexiDatePanel.add(buildEndDateBox());
		
		return flexiDatePanel;
	}
	
	private JPanel buildExactDatePanel()
	{		
		extDatePanel = new JPanel();		
		extDatePanel.setLayout(new ParagraphLayout());
																
		JLabel dummy = new JLabel(localizationToUse.getFieldLabel("DateRangeFrom"));		
		extDatePanel.add(dummy);					
		extDatePanel.add( buildBeginDateBox());			
		dummy.setForeground(component.getBackground());		
				
		return extDatePanel;			
	}
				
	private Box buildBeginDateBox()
	{				
		if (bgDateBox  == null)
		{	
			bgDateBox = Box.createHorizontalBox();								
			bgDayCombo = new JComboBox();	
			bgMonthCombo = new JComboBox(localizationToUse.getMonthLabels());
			bgYearCombo = new JComboBox();					
			UiDateEditor.buildDate(bgDateBox, localizationToUse, bgYearCombo, bgMonthCombo, bgDayCombo);
		}		
		
		return bgDateBox;											
	}

	private Box buildEndDateBox()
	{		
		boolean needToSetDefaultValue=false;		
		if (endDateBox == null)
		{
			endDateBox = Box.createHorizontalBox();		
			endDayCombo = new JComboBox();	
			endMonthCombo = new JComboBox(localizationToUse.getMonthLabels());
			endYearCombo = new JComboBox();
			needToSetDefaultValue=true;						
			UiDateEditor.buildDate(endDateBox, localizationToUse, endYearCombo, endMonthCombo, endDayCombo);
		}
		
		if (needToSetDefaultValue)
		{				
			endYearCombo.setSelectedItem((String) bgYearCombo.getSelectedItem());
			endMonthCombo.setSelectedItem((String) bgMonthCombo.getSelectedItem());
			endDayCombo.setSelectedItem((String) bgDayCombo.getSelectedItem());			
		}
			
		return endDateBox;		
	}
		
	public JComponent getComponent()
	{
		return component;
	}

	public JComponent[] getFocusableComponents()
	{
		return (flexiDateRB.isSelected())? loadFlexidatePanelComponents():loadExactDatePanelComponents();				
	}
	
	private JComponent[] loadFlexidatePanelComponents()
	{
		return new JComponent[]{exactDateRB, flexiDateRB, bgDayCombo, bgMonthCombo, bgYearCombo,
		endDayCombo, endMonthCombo, endYearCombo};
	}
	
	private JComponent[] loadExactDatePanelComponents()
	{
		return new JComponent[]{exactDateRB, flexiDateRB, bgDayCombo, bgMonthCombo, bgYearCombo,};
	}

	private final class RadioItemListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			if (isFlexiDate())														
				removeExactDatePanel();									
			
			if (isExactDate())				
				removeFlexidatePanel();					
		}
	}
	
	private void removeExactDatePanel()
	{
		component.remove(extDatePanel);						
		component.add(buildFlexiDatePanel());																	
		component.revalidate();		
	}
	
	private void removeFlexidatePanel()
	{
		Dimension d = component.getSize();									
		component.remove(flexiDatePanel);						
		component.add(buildExactDatePanel());
		component.setPreferredSize(d);									
		component.revalidate();		
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
		String dateText = df.format(getBeginDate())+ MartusFlexidate.DATE_RANGE_SEPARATER+
						MartusFlexidate.toFlexidateFormat(getBeginDate(), (isFlexiDate())? getEndDate():getBeginDate());						
		return dateText;
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
		MartusFlexidate mfd = MartusFlexidate.createFromMartusDateString(newText);
		UiDateEditor.setDate(MartusFlexidate.toStoredDateFormat(mfd.getBeginDate()), bgYearCombo, bgMonthCombo, bgDayCombo);
			
		if (mfd.hasDateRange())
		{
			flexiDateRB.setSelected(true);
			UiDateEditor.setDate(MartusFlexidate.toStoredDateFormat(mfd.getEndDate()), endYearCombo, endMonthCombo, endDayCombo);
		}		
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
	private Box					bgDateBox = null;
	private Box					endDateBox = null;	
}
