package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



public class UiDisplayFileDlg extends JDialog
{
	public UiDisplayFileDlg(UiMainWindow owner, String baseTag, InputStream fileStream, InputStream fileStreamToc)
	{
		super(owner, "", true);
		message = "";
		
		message = getFileContents(fileStream);
		if(message == null)
		{
			dispose();
			return;	
		}

		Vector messageTOC = null;
		messageTOC = getFileVectorContents(fileStreamToc);

		MartusApp app = owner.getApp();
		setTitle(app.getWindowTitle(baseTag));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JLabel(" "), BorderLayout.NORTH);
		getContentPane().add(new JLabel("   "), BorderLayout.WEST);
		getContentPane().add(new JLabel(" "), BorderLayout.EAST);

		if(messageTOC != null)
		{
			tocList = new JList(messageTOC);
			tocList.addListSelectionListener(new ListHandler());
			JScrollPane tocMsgAreaScrollPane = new JScrollPane(tocList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			tocMsgAreaScrollPane.setPreferredSize(new Dimension(600, 100));
			Container msgPlacement = new Container();
			msgPlacement.setLayout(new ParagraphLayout());
			msgPlacement.add(tocMsgAreaScrollPane, ParagraphLayout.NEW_PARAGRAPH);
			msgPlacement.add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
			getContentPane().add(msgPlacement, BorderLayout.NORTH);
		}

		msgArea = new UiWrappedTextArea(owner, message);
		msgArea.addKeyListener(new TabToOkButton());
		msgArea.setRows(10);
		msgArea.setColumns(80);
		msgAreaScrollPane = new JScrollPane(msgArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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

	public String getFileContents(InputStream fileStream)
	{
		String message = "";
		if(fileStream == null)
		{
			System.out.println("UiDisplayFileDlg: null stream");
			return null;
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
			return null;
		}
		return message;
	}

	public Vector getFileVectorContents(InputStream fileStream)
	{
		Vector message = new Vector();
		if(fileStream == null)
		{
			System.out.println("UiDisplayFileDlg: null stream");
			return null;
		}
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
			while(true)
			{
				String lineIn = reader.readLine();
				if(lineIn == null)
					break;
				message.add(lineIn);
			}
			reader.close();
		}
		catch(IOException e)
		{
			System.out.println("UiDisplayFileDlg: " + e);
			return null;
		}
		return message;
	}

	
	public void findAndScrollToItem()
	{
		msgArea.setCaretPosition(message.length());
		msgAreaScrollPane.getVerticalScrollBar().setValue(msgAreaScrollPane.getVerticalScrollBar().getMaximum());
		int foundAt = message.indexOf((String)tocList.getSelectedValue());
		if(foundAt < 0)
			foundAt = 0;
		msgArea.setCaretPosition(foundAt);
	}

	class ListHandler implements ListSelectionListener 
	{
		public void valueChanged(ListSelectionEvent arg0)
		{
			findAndScrollToItem();
		}
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
	String message;
	JButton ok;
	JList tocList;
	UiWrappedTextArea msgArea;
	JScrollPane msgAreaScrollPane;
}
