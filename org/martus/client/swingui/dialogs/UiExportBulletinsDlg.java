/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.swingui.dialogs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.martus.client.core.BulletinXmlExporter;
import org.martus.client.swingui.UiFileChooser;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.packet.UniversalId;
import org.martus.swing.Utilities;
import org.martus.util.UnicodeWriter;

public class UiExportBulletinsDlg extends JDialog implements ActionListener
{
	public UiExportBulletinsDlg(UiMainWindow mainWindowToUse, UniversalId[] selectedBulletins)
	{
		mainWindow = mainWindowToUse;
		UiLocalization localization = mainWindow.getLocalization();

		bulletins = findBulletins(selectedBulletins);
		
		setModal(true);
		setTitle(localization.getWindowTitle("ExportBulletins"));

		includePrivate = new JCheckBox(localization.getFieldLabel("ExportPrivateData"));

		ok = new JButton(localization.getButtonLabel("Continue"));
		ok.addActionListener(this);

		cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		Box hBoxButtons = Box.createHorizontalBox();
		hBoxButtons.add(ok);
		hBoxButtons.add(cancel);
		hBoxButtons.add(Box.createHorizontalGlue());

		String[] titles = extractTitles(bulletins);
		JList bulletinList = new JList(titles);
		JScrollPane tocMsgAreaScrollPane = new JScrollPane(bulletinList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tocMsgAreaScrollPane.setPreferredSize(new Dimension(580, 100));

		Box upperStuff = Box.createVerticalBox();
		upperStuff.add(new JLabel(" "));
		upperStuff.add(new JLabel(localization.getFieldLabel("ExportDetails")));
		upperStuff.add(new JLabel(" "));
		upperStuff.add(tocMsgAreaScrollPane);
		upperStuff.add(new JLabel(" "));
		upperStuff.add(includePrivate);
		upperStuff.add(new JLabel(" "));

		Box upperStuffLeftAligned = Box.createHorizontalBox();
		upperStuffLeftAligned.add(upperStuff);
		upperStuffLeftAligned.add(Box.createHorizontalGlue());

		Box vBoxAll = Box.createVerticalBox();
		vBoxAll.add(upperStuffLeftAligned);
		vBoxAll.add(hBoxButtons);
		getContentPane().add(vBoxAll);

		pack();
		Dimension size = getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		setLocation(Utilities.center(size, screen));
		show();
	}

	Vector findBulletins(UniversalId[] selectedBulletins)
	{
		Vector bulletins = new Vector();
		for (int i = 0; i < selectedBulletins.length; i++)
		{
			UniversalId uid = selectedBulletins[i];
			Bulletin b = mainWindow.getStore().findBulletinByUniversalId(uid);
			bulletins.add(b);
		}
		return bulletins;
	}

	String[] extractTitles(Vector bulletins)
	{
		String[] titles = new String[bulletins.size()];
		for (int i = 0; i < titles.length; i++)
		{
			Bulletin b = (Bulletin)bulletins.get(i);
			titles[i] = b.get(BulletinConstants.TAGTITLE);
		}
		return titles;
	}

	File askForDestinationFile()
	{
		UiFileChooser chooser = new UiFileChooser();
		chooser.setDialogTitle(mainWindow.getLocalization().getWindowTitle("ExportBulletinsSaveAs"));
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return null;

		File destFile = chooser.getSelectedFile();
		if(destFile.exists())
			if(!mainWindow.confirmDlg(mainWindow, "OverWriteExistingFile"))
				return null;

		return destFile;
	}

	boolean userWantsToExportPrivate()
	{
		return includePrivate.isSelected();
	}

	void doExport(File destFile)
	{
		try
		{
			UnicodeWriter writer = new UnicodeWriter(destFile);
			BulletinXmlExporter.exportBulletins(writer, bulletins, userWantsToExportPrivate());
			writer.close();
			mainWindow.notifyDlg(mainWindow, "ExportComplete");
		}
		catch (IOException e)
		{
			mainWindow.notifyDlg(mainWindow, "ErrorWritingFile");
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(ok))
		{
			if(userWantsToExportPrivate())
			{
				if(!mainWindow.confirmDlg(null, "ExportPrivateData"))
					return;
			}

			File destFile = askForDestinationFile();
			if(destFile == null)
				return;

			doExport(destFile);
		}

		dispose();
	}

	UiMainWindow mainWindow;
	Vector bulletins;
	JCheckBox includePrivate;
	JButton ok;
	JButton cancel;
}
