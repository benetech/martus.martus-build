package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;



public class UiDisplayFileDlg extends JDialog
{
	public UiDisplayFileDlg(UiMainWindow owner, String baseTag, InputStream fileStream)
	{
		super(owner, "", true);
		String message = "";
		if(fileStream == null)
		{
			System.out.println("UiDisplayFileDlg: null stream");
			dispose();
			return;
		}
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
			while(true)
			{
				String lineIn = reader.readLine();
				if(lineIn == null)
					break;
				message += lineIn;
				message += '\n';
			}
			reader.close();
		} 
		catch(IOException e) 
		{
			System.out.println("UiDisplayFileDlg: " + e);
			dispose();
			return;
		}
		
		MartusApp app = owner.getApp();
		setTitle(app.getWindowTitle(baseTag));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JLabel(" "), BorderLayout.NORTH);
		getContentPane().add(new JLabel("   "), BorderLayout.WEST);
		getContentPane().add(new JLabel(" "), BorderLayout.EAST);

		UiWrappedTextArea msgArea = new UiWrappedTextArea(owner, message);
		msgArea.addKeyListener(new TabToOkButton());
		msgArea.setRows(10);
		msgArea.setColumns(80);
		JScrollPane msgAreaScrollPane = new JScrollPane(msgArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Container msgPlacement = new Container();
		msgPlacement.setLayout(new ParagraphLayout());
		msgPlacement.add(msgAreaScrollPane, ParagraphLayout.NEW_PARAGRAPH);
		msgPlacement.add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(msgPlacement, BorderLayout.CENTER);

		ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		ok.addKeyListener(new MakeEnterKeyExit());
		Container okPlacement = new Container();
		okPlacement.setLayout(new GridLayout(1,5));
		okPlacement.add(new JLabel(""));
		okPlacement.add(new JLabel(""));
		okPlacement.add(ok);
		okPlacement.add(new JLabel(""));
		okPlacement.add(new JLabel(""));
		getContentPane().add(okPlacement,BorderLayout.SOUTH);
		getRootPane().setDefaultButton(ok);
		ok.requestFocus();

		owner.centerDlg(this);
		setResizable(true);
		show();
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	class MakeEnterKeyExit extends KeyAdapter 
	{
		public void keyPressed(KeyEvent ke) 
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				dispose();
		} 
	}

	class TabToOkButton extends KeyAdapter 
	{
		public void keyPressed(KeyEvent ke) 
		{
			if (ke.getKeyCode() == KeyEvent.VK_TAB)
			{
				ok.requestFocus();	
			}
		} 
	}
	JButton ok;
}
