package org.martus.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class UiContactInfoDlg extends JDialog implements ActionListener
{
	public UiContactInfoDlg(UiMainWindow owner, ConfigInfo infoToUse)
	{
		super(owner, "", true);
		mainWindow = owner;
		info = infoToUse;

		MartusApp app = owner.getApp();
		setTitle(app.getWindowTitle("setupcontact"));
		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		source = new JTextField(50);
		organization = new JTextField(50);
		email = new JTextField(50);
		webpage = new JTextField(50);
		phone = new JTextField(50);
		address = new UiTextArea(5, 50);
		address.setLineWrap(true);
		address.setWrapStyleWord(true);
		JScrollPane addressScrollPane = new JScrollPane(address, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		source.setText(info.getAuthor());
		organization.setText(info.getOrganization());
		email.setText(info.getEmail());
		webpage.setText(info.getWebPage());
		phone.setText(info.getPhone());
		address.setText(info.getAddress());

		getContentPane().setLayout(new ParagraphLayout());
		JLabel space = new JLabel(" ");
		getContentPane().add(space, ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel());
		
		UiWrappedTextArea infoRequired = new UiWrappedTextArea(owner, app.getFieldLabel("ContactInfoRequiredFields"));
		infoRequired.setFont(space.getFont(), 60);
		getContentPane().add(infoRequired);
		
		String authorPrompt = app.getFieldLabel("AuthorRequired");
		getContentPane().add(new JLabel(authorPrompt), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(source);

		String organizationPrompt = app.getFieldLabel("OrganizationRequired");
		getContentPane().add(new JLabel(organizationPrompt), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(organization);
		getContentPane().add(new JLabel(app.getFieldLabel("email")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(email);
		getContentPane().add(new JLabel(app.getFieldLabel("webpage")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(webpage);
		getContentPane().add(new JLabel(app.getFieldLabel("phone")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(phone);
		getContentPane().add(new JLabel(app.getFieldLabel("address")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(addressScrollPane);
		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel(app.getFieldLabel("ContactInfoDescriptionOfFields")));

		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		UiWrappedTextArea infoFuture = new UiWrappedTextArea(owner, app.getFieldLabel("ContactInfoFutureUseOfFields"));
		infoFuture.setFont(space.getFont(), 60);
		getContentPane().add(infoFuture);

		getContentPane().add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(new JLabel(app.getFieldLabel("ContactInfoUpdateLater")));

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		mainWindow.centerDlg(this);
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
			info.setAuthor(source.getText());
			info.setOrganization(organization.getText());
			info.setEmail(email.getText());
			info.setWebPage(webpage.getText());
			info.setPhone(phone.getText());
			info.setAddress(address.getText());
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
