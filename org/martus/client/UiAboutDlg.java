
package org.martus.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.martus.common.MartusUtilities;

public class UiAboutDlg extends JDialog implements ActionListener
{
	private MartusApp app;
	
	public UiAboutDlg(UiMainWindow owner)
		throws HeadlessException
	{
		super(owner, "" , true);
		app = owner.getApp();
		setTitle(app.getWindowTitle("about"));
		getContentPane().setLayout(new BorderLayout());
		JLabel icon = new JLabel(new ImageIcon(UiAboutDlg.class.getResource("MartusLogo.gif")),JLabel.LEFT);
		getContentPane().add(icon, BorderLayout.NORTH);

		getContentPane().add(new JLabel("   "), BorderLayout.WEST);
		getContentPane().add(new JLabel("   "), BorderLayout.EAST);
		
		String versionInfo = app.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += MartusUtilities.getVersionDate(getClass());
		
		Container info = new Container();
		info.setLayout(new GridLayout(10,1));
		info.add(new JLabel(""));
		info.add(new JLabel(versionInfo, JLabel.CENTER));
		info.add(new JLabel(app.getFieldLabel("aboutDlgCopyright"), JLabel.CENTER));
		info.add(new JLabel(""));
		info.add(new JLabel(app.getFieldLabel("aboutDlgLine3")));
		info.add(new JLabel(app.getFieldLabel("aboutDlgLine4")));
		info.add(new JLabel(""));
		info.add(new JLabel(RSANOTICE));
		info.add(new JLabel(IBMNOTICE));
		info.add(new JLabel(""));
		getContentPane().add(info, BorderLayout.CENTER);

		JButton ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		ok.addKeyListener(new MakeEnterKeyExit());
		Container okPlacement = new Container();
		okPlacement.setLayout(new GridLayout(1,5));
		okPlacement.add(new JLabel(""));
		okPlacement.add(new JLabel(""));
		okPlacement.add(ok);
		okPlacement.add(new JLabel(""));
		okPlacement.add(new JLabel(""));
		getContentPane().add(okPlacement, BorderLayout.SOUTH);

		pack();
		Dimension size = getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(MartusApp.center(size, screen));
		setResizable(false);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}
	
	public class MakeEnterKeyExit extends KeyAdapter 
	{
		public void keyPressed(KeyEvent ke) 
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				dispose();
		} 
	}
	final String RSANOTICE = "This product includes code licensed from RSA Security, Inc.";
	final String IBMNOTICE = "Some portions licensed from IBM are available at http://oss.software.ibm.com/icu4j/";
}
