/* $Id: UiLocalizeDlg.java,v 1.8 2002/09/25 22:07:14 kevins Exp $ */
package org.martus.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;



public class UiLocalizeDlg extends JDialog implements ActionListener, ChangeListener
{
	public UiLocalizeDlg(UiMainWindow mainWindow)
	{
		super(mainWindow, "", true);
		owner = mainWindow;
		MartusApp app = owner.getApp();

		setTitle(app.getWindowTitle("options"));

		dateFormatDropdown = new UiChoiceEditor(MartusLocalization.getDateFormats());
		dateFormatDropdown.setText(owner.getApp().getCurrentDateFormatCode());

		languageDropdown = new UiChoiceEditor(app.getUiLanguages());
		languageDropdown.setText(app.getCurrentLanguage());

		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		getContentPane().setLayout(new ParagraphLayout());

		getContentPane().add(new JLabel(app.getFieldLabel("language")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(languageDropdown.getComponent());

		getContentPane().add(new JLabel(app.getFieldLabel("dateformat")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(dateFormatDropdown.getComponent());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		pack();
		Dimension size = getSize();

		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(MartusApp.center(size, screen));
		setResizable(false);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == ok)
		{
			owner.getApp().setCurrentDateFormatCode(dateFormatDropdown.getText());
			owner.getApp().setCurrentLanguage(languageDropdown.getText());
		}
		dispose();
	}

	// ChangeListener interface
	public void stateChanged(ChangeEvent event) {}

	private UiMainWindow owner;
	private UiChoiceEditor languageDropdown;
	private UiChoiceEditor dateFormatDropdown;
	private JButton ok;
	private JButton cancel;
}
