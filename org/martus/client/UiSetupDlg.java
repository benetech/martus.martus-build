package org.martus.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;



import java.io.File;

class UiSetupDlg extends JDialog implements ActionListener
{
	UiSetupDlg(UiMainWindow owner, ConfigInfo infoToUse)
	{
		super(owner, "", true);
		mainWindow = owner;
		info = infoToUse;

		MartusApp app = owner.getApp();
		setTitle(app.getWindowTitle("setup"));
		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		UiTextArea description = new UiTextArea(app.getFieldLabel("setupdescription"));
		description.setEditable(false);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		source = new JTextField(50);
		organization = new JTextField(50);
		email = new JTextField(50);
		webpage = new JTextField(50);
		phone = new JTextField(50);
		address = new UiTextArea(4, 50);
		address.setLineWrap(true);
		address.setWrapStyleWord(true);

		getContentPane().setLayout(new ParagraphLayout());
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(description);

		getContentPane().add(new JLabel(app.getFieldLabel("author")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(source);

		getContentPane().add(new JLabel(app.getFieldLabel("organization")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(organization);
		getContentPane().add(new JLabel(app.getFieldLabel("email")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(email);
		getContentPane().add(new JLabel(app.getFieldLabel("webpage")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(webpage);
		getContentPane().add(new JLabel(app.getFieldLabel("phone")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(phone);
		getContentPane().add(new JLabel(app.getFieldLabel("address")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(address);

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		owner.centerDlg(this);
		setResizable(true);
		show();
	}

	public boolean getResult()
	{
		return result;
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
		{
			result = true;
		}
		dispose();
	}

	UiMainWindow mainWindow;
	ConfigInfo info;

	boolean result;

	JTextField source;
	JTextField organization;
	JTextField email;
	JTextField webpage;
	JTextField phone;
	UiTextArea address;

	JButton ok;
}
