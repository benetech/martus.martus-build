package org.martus.client;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;



public class UiSigninDlg extends JDialog implements VirtualKeyboardHandler
{
	public UiSigninDlg(UiMainWindow window, JFrame owner, int mode)
	{
		super(owner, window.getApp().getWindowTitle("MartusSignIn"), true);
		Initalize(window, owner, mode, "");
	}

	public UiSigninDlg(UiMainWindow window, JFrame owner, int mode, String username)
	{
		super(owner, window.getApp().getWindowTitle("MartusSignIn"), true);
		Initalize(window, owner, mode, username);
	}


	public void Initalize(UiMainWindow window, JFrame owner, int mode, String username)
	{
		mainWindow = window;
		app = mainWindow.getApp();
		getContentPane().setLayout(new ParagraphLayout());
		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		if(mode == TIMED_OUT)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel timedOutNote1 = new JLabel(app.getFieldLabel("timedout1"));
			getContentPane().add(timedOutNote1);
			
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel timedOutNote2 = new JLabel(app.getFieldLabel("timedout2"));
			getContentPane().add(timedOutNote2);
		}
		else if(mode == SECURITY_VALIDATE)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel securityServerConfigValidate = new JLabel(app.getFieldLabel("securityServerConfigValidate"));
			getContentPane().add(securityServerConfigValidate);
		}
		else if(mode == RETYPE_USERNAME_PASSWORD)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel retypeUserNamePassword = new JLabel(app.getFieldLabel("RetypeUserNameAndPassword"));
			getContentPane().add(retypeUserNamePassword);
		}
		else if(mode == CREATE_NEW)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			JLabel createNewUserNamePassword = new JLabel(app.getFieldLabel("CreateNewUserNamePassword"));
			getContentPane().add(createNewUserNamePassword);
			
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			UiWrappedTextArea helpOnCreatingPassword = new UiWrappedTextArea(window, app.getFieldLabel("HelpOnCreatingNewPassword"), 45);
			helpOnCreatingPassword.setFont(createNewUserNamePassword.getFont());
			getContentPane().add(helpOnCreatingPassword);
			
		}

		userNameDescription = new JLabel("");
		passwordDescription = new JLabel("");
		
		getContentPane().add(new JLabel(app.getFieldLabel("username")), ParagraphLayout.NEW_PARAGRAPH);
		nameField = new JTextField(20);
		nameField.setText(username);
		getContentPane().add(userNameDescription);
		getContentPane().add(nameField);
		
		getContentPane().add(new JLabel(app.getFieldLabel("password")), ParagraphLayout.NEW_PARAGRAPH);
		passwordField = new JPasswordField(20);

		switchToNormalKeyboard = new JButton(app.getButtonLabel("VirtualKeyboardSwitchToNormal"));
		switchToNormalKeyboard.addActionListener(new switchKeyboardHandler());
		passwordArea = new JPanel();
		getContentPane().add(passwordArea);
		keyboard = new UiVirtualKeyboard(mainWindow, this);
		UpdatePasswordArea();

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);
		if(username.length() > 0)
			passwordField.requestFocus();
		mainWindow.centerDlg(this);
		setResizable(true);
		show();
	}

	public boolean getResult()
	{
		return result;
	}

	public String getName()
	{
		return name;
	}

	public String getPassword()
	{
		return password;
	}

	public class switchKeyboardHandler extends AbstractAction 
	{
		public void actionPerformed(ActionEvent e)
		{
			SwitchKeyboards();
		}
	}
	public void setPassword(String virtualPassword)
	{
		passwordField.setText(virtualPassword);
		passwordField.updateUI();
		ok.requestFocus();
	}
	
	public void SwitchKeyboards()
	{
		boolean viewingVirtualKeyboard = mainWindow.isCurrentDefaultKeyboardVirtual();
		if(viewingVirtualKeyboard)
		{
			if(!mainWindow.confirmDlg(null, "WarningSwitchToNormalKeyboard"))
				return;
		}
		mainWindow.setCurrentDefaultKeyboardVirtual(!viewingVirtualKeyboard);
		try 
		{
			mainWindow.saveCurrentUiState();
		} 
		catch(IOException e) 
		{
			System.out.println("UiSigninDialog SwitchKeyboards :" + e);
		}		
		
		UpdatePasswordArea();
	}

	public void UpdatePasswordArea()
	{
		boolean viewingVirtualKeyboard = mainWindow.isCurrentDefaultKeyboardVirtual();
		if(viewingVirtualKeyboard)
			displayPasswordAreaUsingVirtualKeyboard();
		else
			displayPasswordAreaUsingNormalKeyboard();
	}	

	public void addKeyboard(JPanel keyboard)
	{
		virtualKeyboardPanel = keyboard;
	}

	public void displayPasswordAreaUsingVirtualKeyboard()
	{
		passwordArea.removeAll();
		userNameDescription.setText(app.getFieldLabel("VirtualUserNameDescription"));
		passwordDescription.setText(app.getFieldLabel("VirtualPasswordDescription"));
		
		passwordArea.setLayout(new ParagraphLayout());
		passwordArea.setBorder(new LineBorder(Color.black, 2));
		passwordArea.add(new JLabel(""));
		passwordArea.add(passwordDescription);
		passwordField.setEditable(false);
		passwordArea.add(passwordField);

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		passwordArea.add(virtualKeyboardPanel);

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		switchToNormalKeyboard.setText(app.getButtonLabel("VirtualKeyboardSwitchToNormal"));
		passwordArea.add(switchToNormalKeyboard);
		updateUI();
		mainWindow.centerDlg((JDialog)this);
	}
	
	public void displayPasswordAreaUsingNormalKeyboard()
	{
		passwordArea.removeAll();
		passwordArea.updateUI();
		userNameDescription.setText("");
		passwordDescription.setText("");
		passwordArea.setLayout(new ParagraphLayout());
		passwordArea.setBorder(new LineBorder(Color.black, 2));

		passwordField.setEditable(true);
		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		passwordArea.add(passwordField);

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		JLabel warningNormalKeyboard = new JLabel(app.getFieldLabel("NormalKeyboardMsg1"));
		warningNormalKeyboard.setFont(warningNormalKeyboard.getFont().deriveFont(Font.BOLD));
		passwordArea.add(warningNormalKeyboard);
		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		passwordArea.add(new JLabel(app.getFieldLabel("NormalKeyboardMsg2")));

		passwordArea.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		switchToNormalKeyboard.setText(app.getButtonLabel("VirtualKeyboardSwitchToVirtual"));
		passwordArea.add(switchToNormalKeyboard);
		updateUI();
		mainWindow.centerDlg(this);
	}
	public void updateUI() 
	{
		passwordArea.updateUI();
		userNameDescription.updateUI();
		getRootPane().setDefaultButton(ok);
		nameField.requestFocus();
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = true;
			name = nameField.getText();
			password = new String(passwordField.getPassword());
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}

	private MartusApp app;
	private UiMainWindow mainWindow;
	private boolean result;
	private String name;
	private String password;
	private UiVirtualKeyboard keyboard;
	private JPanel passwordArea;
	private JTextField nameField;
	private JPasswordField passwordField;
	private JPanel virtualKeyboardPanel;
	private JLabel userNameDescription;
	private JLabel passwordDescription;
	private JButton switchToNormalKeyboard;
	private JButton ok;

	public final static int INITIAL = 1;
	public final static int TIMED_OUT = 2;
	public final static int SECURITY_VALIDATE = 3;
	public final static int RETYPE_USERNAME_PASSWORD = 4;
	public final static int CREATE_NEW = 5;
}

