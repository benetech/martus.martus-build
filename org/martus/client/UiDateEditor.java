package org.martus.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class UiDateEditor extends UiField
{
	public UiDateEditor(MartusApp appToUse)
	{
		app = appToUse;

		component = new JPanel();
		Box box = Box.createHorizontalBox();

		dayCombo = new JComboBox();
		for(int day=1; day <= 31; ++day)
			dayCombo.addItem(new Integer(day).toString());

		monthCombo = new JComboBox(app.getMonthLabels());

		yearCombo = new JComboBox();
		Calendar cal = new GregorianCalendar();
		int thisYear = cal.get(Calendar.YEAR);
		for(int year = 1900; year <= thisYear; ++year)
			yearCombo.addItem(new Integer(year).toString());

		String mdyOrder = MartusLocalization.getMdyOrder(app.getCurrentDateFormatCode());
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

	MartusApp app;
	JComponent component;
	JComboBox monthCombo;
	JComboBox dayCombo;
	JComboBox yearCombo;
}

