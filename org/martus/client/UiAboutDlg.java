
package org.martus.client;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
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

		JLabel icon = new JLabel(new ImageIcon(UiAboutDlg.class.getResource("MartusLogo.gif")),JLabel.LEFT);

		String versionInfo = app.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;
		
		String buildDate = app.getFieldLabel("aboutDlgBuildDate");
		buildDate += " " + MartusUtilities.getVersionDate();
		
		JButton ok = new JButton(app.getButtonLabel("ok"));
		ok.addActionListener(this);
		ok.addKeyListener(new MakeEnterKeyExit());

		Box vBoxVersionInfo = Box.createVerticalBox();
		vBoxVersionInfo.add(new JLabel(versionInfo));
		vBoxVersionInfo.add(new JLabel(app.getFieldLabel("aboutDlgCopyright")));
		vBoxVersionInfo.add(new JLabel(buildDate));

		Box hBoxVersionAndIcon = Box.createHorizontalBox();
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(vBoxVersionInfo);
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(icon);
		
		Box hBoxOk = Box.createHorizontalBox();
		hBoxOk.add(Box.createHorizontalGlue());
		hBoxOk.add(ok);
		hBoxOk.add(Box.createHorizontalGlue());

		Box vBoxDetails = Box.createVerticalBox();		
		vBoxDetails.add(new JLabel(" "));
		vBoxDetails.add(new JLabel(app.getFieldLabel("aboutDlgLine3")));
		vBoxDetails.add(new JLabel(app.getFieldLabel("aboutDlgLine4")));
		vBoxDetails.add(new JLabel(" "));
		vBoxDetails.add(new JLabel(RSANOTICE));
		vBoxDetails.add(new JLabel(IBMNOTICE));
		vBoxDetails.add(new JLabel(" "));
		vBoxDetails.add(hBoxOk);

		Box hBoxDetails = Box.createHorizontalBox();
		hBoxDetails.add(vBoxDetails);
		
		Box vBoxAboutDialog = Box.createVerticalBox();
		vBoxAboutDialog.add(hBoxVersionAndIcon);
		vBoxAboutDialog.add(hBoxDetails);
		getContentPane().add(vBoxAboutDialog);

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
