package org.martus.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.martus.client.*;

import MartusJava.UiMainWindow;

public class UiStringInputDlg extends JDialog
{
	public UiStringInputDlg(UiMainWindow owner, String baseTag, String descriptionTag, String defaultText)
	{
		super(owner, "", true);
		mainWindow = owner;

		MartusApp app = owner.getApp();
		setTitle(app.getWindowTitle("input" + baseTag));

		JLabel label = new JLabel(app.getFieldLabel("input" + baseTag + "entry"));
		text = new JTextField(30);
		text.setText(defaultText);

		JButton ok = new JButton(app.getButtonLabel("input" + baseTag + "ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(app.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());

		getContentPane().setLayout(new ParagraphLayout());
		if(descriptionTag.length() > 0)
		{
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
			getContentPane().add(new JLabel(app.getFieldLabel(descriptionTag)));
		}
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(label);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(text);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		pack();
		Dimension size = getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(MartusApp.center(size, screen));
		setResizable(false);
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = text.getText();
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

	public String getResult()
	{
		return result;
	}

	JFrame mainWindow;
	JTextField text;
	String result = null;
}

