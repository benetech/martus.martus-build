package org.martus.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public class UiLocalizeDlg extends JDialog implements ActionListener, ChangeListener
{
	public UiLocalizeDlg(UiMainWindow mainWindow)
	{
		super(mainWindow, "", true);
		owner = mainWindow;
		MartusApp app = owner.getApp();

		setTitle(app.getMenuLabel("Preferences"));

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

		owner.centerDlg(this);
		setResizable(true);
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
