package org.martus.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.martus.common.MartusUtilities;
import org.martus.common.Base64.InvalidBase64Exception;

public class UiConfigServerDlg extends JDialog implements ActionListener
{
	public UiConfigServerDlg(UiMainWindow owner, ConfigInfo infoToUse)
	{
		super(owner, "", true);
		serverPublicKey = "";
		
		info = infoToUse;
		mainWindow = owner;
		app = owner.getApp();
		setTitle(app.getWindowTitle("ConfigServer"));
		getContentPane().setLayout(new ParagraphLayout());

		serverIP = new JTextField(25);
		serverPublicCode = new JTextField(25);

		getContentPane().add(new JLabel(app.getFieldLabel("ServerNameEntry")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(serverIP);
		serverIPAddress = info.getServerName();
		serverIP.setText(serverIPAddress);
		serverIP.requestFocus();
		
		getContentPane().add(new JLabel(app.getFieldLabel("ServerPublicCodeEntry")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(serverPublicCode);
		String serverPublicKey = info.getServerPublicKey();
		String serverCode = "";
		try 
		{
			if(serverPublicKey.length() > 0)
			{
				serverCode = MartusUtilities.computePublicCode(serverPublicKey);
				serverCode = MartusUtilities.formatPublicCode(serverCode);
			}
		} 
		catch (InvalidBase64Exception e) 
		{
		}
		serverPublicCode.setText(serverCode);
		
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);

		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(this);
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

	public String getServerIPAddress() 
	{
		return serverIPAddress;
	}

	public String getServerPublicKey() 
	{
		return serverPublicKey;
	}

	public void actionPerformed(ActionEvent ae) 
	{
		result = false;
		if(ae.getSource() == ok)
		{
			String name = serverIP.getText();
			String publicCode = serverPublicCode.getText();
			if(!ValidateInformation(name, publicCode))
				return;
			result = true;
		}
		dispose();
	}
	
	private boolean ValidateInformation(String serverName, String userEnteredPublicCode)
	{
		if(serverName.length() == 0)
			return errorMessage("InvalidServerName");

		String normalizedPublicCode = mainWindow.removeNonDigits(userEnteredPublicCode);
		if(normalizedPublicCode.length() == 0)
			return errorMessage("InvalidServerCode");

		if(!app.isNonSSLServerAvailable(serverName))
			return errorMessage("ConfigNoServer");

		String serverKey = null;
		String serverPublicCode = null;
		try
		{		
			serverKey = app.getServerPublicKey(serverName);
			serverPublicCode = MartusUtilities.computePublicCode(serverKey);
		}
		catch(Exception e)
		{
			System.out.println(e);
			return errorMessage("ServerInfoInvalid");
		}
		if(!serverPublicCode.equals(normalizedPublicCode))
			return errorMessage("ServerCodeWrong");

		serverIPAddress = serverName;
		serverPublicKey = serverKey;
		return true;
	}
	
	private boolean errorMessage(String messageTag)
	{
		mainWindow.notifyDlg(mainWindow, messageTag);		
		return false;
	}

	MartusApp app;
	UiMainWindow mainWindow;
	ConfigInfo info;

	JButton ok;
	JTextField serverIP;
	JTextField serverPublicCode;

	String serverIPAddress;
	String serverPublicKey;

	boolean result;
}
