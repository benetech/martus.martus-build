package org.martus.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class UiSearchDlg extends JDialog  implements ActionListener
{
	public UiSearchDlg(UiMainWindow owner)
	{
		super(owner, "", true);
		MartusApp app = owner.getApp();
		
		setTitle(app.getWindowTitle("search"));
		search = new JButton(app.getButtonLabel("search"));
		search.addActionListener(this);
		getRootPane().setDefaultButton(search);
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		getContentPane().setLayout(new ParagraphLayout());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new UiWrappedTextArea(owner, app.getFieldLabel("SearchBulletinRules")));

		searchField = new JTextField(40);
		searchField.setText(searchString);
		getContentPane().add(new JLabel(app.getFieldLabel("SearchEntry")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(searchField);

		startDateEditor = new UiDateEditor(app);
		if(startDate.length() == 0)
			startDate = DEFAULT_SEARCH_START_DATE;
		startDateEditor.setText(startDate);
		getContentPane().add(new JLabel(app.getFieldLabel("SearchStartDate")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(startDateEditor.getComponent());
		
		endDateEditor = new UiDateEditor(app);
		if(endDate.length() == 0)
			endDate = Bulletin.getLastDayOfThisYear();
		endDateEditor.setText(endDate);
		getContentPane().add(new JLabel(app.getFieldLabel("SearchEndDate")), ParagraphLayout.NEW_PARAGRAPH);		
		getContentPane().add(endDateEditor.getComponent());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(search);
		getContentPane().add(cancel);
		
		owner.centerDlg(this);
		setResizable(true);
		show();

	}

	public void actionPerformed(ActionEvent ae) 
	{
		if(ae.getSource() == search)
		{
			searchString = searchField.getText();
			startDate = startDateEditor.getText();
			endDate = endDateEditor.getText();
			result = true;
		}
		dispose();
	}


	public boolean getResults()
	{
		return result;	
	}
	
	public String getSearchString()
	{
		return searchString;	
	}
	
	public String getStartDate()
	{
		return startDate;	
	}
	
	public String getEndDate()
	{
		return endDate;	
	}
	
	boolean result;
	static String searchString = "";
	static String startDate = "";
	static String endDate = "";
	
	JButton search;
	JTextField searchField;
	UiDateEditor startDateEditor;
	UiDateEditor endDateEditor;

	final String DEFAULT_SEARCH_START_DATE = "1900-01-01";
}
