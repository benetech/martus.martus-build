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

package org.martus.client.swingui;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class UiMenuBar extends JMenuBar
{
	UiMenuBar(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		UiLocalization localization = mainWindow.getLocalization();
		
		createMenuActions();

		JMenu file = new JMenu(localization.getMenuLabel("file"));
		PrintMenuListener printMenuListener = new PrintMenuListener();
		file.addMenuListener(printMenuListener);
		printMenuListener.initalize();

		file.add(UiActions.newActionMenuCreateNewBulletin(mainWindow));
		file.add(actionMenuPrint);
		file.addSeparator();
		file.add(UiActions.newActionMenuBackupMyKeyPair(mainWindow));
		file.add(UiActions.newActionMenuExportMyPublicKey(mainWindow));
		file.addSeparator();
		file.add(UiActions.newActionMenuExportBulletins(mainWindow));
		file.addSeparator();
		file.add(UiActions.newActionMenuImportHeadquarterPublicKey(mainWindow));
		file.add(UiActions.newActionMenuRemoveExistingHeadquaterPublicKey(mainWindow));
		file.addSeparator();
		file.add(UiActions.newActionMenuExit(mainWindow));


		JMenu edit = new JMenu(localization.getMenuLabel("edit"));
		EditMenuListener menuListener = new EditMenuListener();
		edit.addMenuListener(menuListener);
		menuListener.initalize();

		edit.add(UiActions.newActionMenuSearch(mainWindow));
		edit.addSeparator();
		edit.add(actionMenuModifyBulletin);
		edit.addSeparator();
		edit.add(actionMenuCutBulletins);
		edit.add(actionMenuCopyBulletins);
		edit.add(actionMenuPasteBulletins);
		edit.add(actionMenuSelectAllBulletins);
		edit.addSeparator();
		edit.add(actionMenuDiscardBulletins);

		JMenu folders = new JMenu(localization.getMenuLabel("folders"));
		FoldersMenuListener menuFolderListener = new FoldersMenuListener();
		folders.addMenuListener(menuFolderListener);
		menuFolderListener.initalize();

		folders.add(UiActions.newActionMenuCreateFolder(mainWindow));
		folders.add(actionMenuRenameFolder);
		folders.add(actionMenuDeleteFolder);


		JMenu server = new JMenu(localization.getMenuLabel("server"));
		server.add(UiActions.newActionMenuRetrieveMySealedBulletins(mainWindow));
		server.add(UiActions.newActionMenuRetrieveMyDraftBulletins(mainWindow));
		server.add(UiActions.newActionMenuDeleteMyServerDraftBulletins(mainWindow));
		server.addSeparator();
		server.add(UiActions.newActionMenuRetrieveHQSealedBulletins(mainWindow));
		server.add(UiActions.newActionMenuRetrieveHQDraftBulletins(mainWindow));
		server.addSeparator();
		server.add(UiActions.newActionMenuSelectServer(mainWindow));


		JMenu options = new JMenu(localization.getMenuLabel("options"));
		options.add(UiActions.newActionMenuPreferences(mainWindow));
		options.add(UiActions.newActionMenuContactInfo(mainWindow));
		options.add(UiActions.newActionMenuDefaultDetailsFieldContent(mainWindow));
		options.add(UiActions.newActionMenuChangeUserNamePassword(mainWindow));

		JMenu tools = new JMenu(localization.getMenuLabel("tools"));
		tools.add(new ActionMenuQuickErase(mainWindow));
		
		JMenu help = new JMenu(localization.getMenuLabel("help"));
		help.add(UiActions.newActionMenuHelp(mainWindow));
		help.add(UiActions.newActionMenuAbout(mainWindow));
		help.addSeparator();
		help.add(UiActions.newActionMenuAccountDetails(mainWindow));

		add(file);
		add(edit);
		add(folders);
		add(server);
		add(options);
		add(tools);
		add(help);
	}

	class PrintMenuListener implements MenuListener
	{
		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuPrint.setEnabled(false);
		}

		public void menuSelected(MenuEvent e)
		{
			actionMenuPrint.setEnabled(actionMenuPrint.isEnabled());
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	class EditMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuModifyBulletin.setEnabled(actionMenuModifyBulletin.isEnabled());
			actionMenuSelectAllBulletins.setEnabled(actionMenuSelectAllBulletins.isEnabled());
			actionMenuCutBulletins.setEnabled(actionMenuCutBulletins.isEnabled());
			actionMenuCopyBulletins.setEnabled(actionMenuCopyBulletins.isEnabled());
			actionMenuPasteBulletins.setEnabled(actionMenuPasteBulletins.isEnabled());
			actionMenuDiscardBulletins.setEnabled(actionMenuDiscardBulletins.isEnabled());
		}

		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuModifyBulletin.setEnabled(false);
			actionMenuSelectAllBulletins.setEnabled(false);
			actionMenuCutBulletins.setEnabled(false);
			actionMenuCopyBulletins.setEnabled(false);
			actionMenuPasteBulletins.setEnabled(false);
			actionMenuDiscardBulletins.setEnabled(false);
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	class FoldersMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuRenameFolder.setEnabled(actionMenuRenameFolder.isEnabled());
			actionMenuDeleteFolder.setEnabled(actionMenuDeleteFolder.isEnabled());
		}

		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuRenameFolder.setEnabled(false);
			actionMenuDeleteFolder.setEnabled(false);
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	private void createMenuActions()
	{
		actionMenuPrint = UiActions.newActionMenuPrint(mainWindow);

		actionMenuModifyBulletin = UiActions.newActionMenuModifyBulletin(mainWindow);
		actionMenuSelectAllBulletins = UiActions.newActionMenuSelectAllBulletins(mainWindow);
		actionMenuCutBulletins = UiActions.newActionMenuCutBulletins(mainWindow);
		actionMenuCopyBulletins = UiActions.newActionMenuCopyBulletins(mainWindow);
		actionMenuPasteBulletins = UiActions.newActionMenuPasteBulletins(mainWindow);
		actionMenuDiscardBulletins = UiActions.newActionMenuDiscardBulletins(mainWindow);

		actionMenuRenameFolder = UiActions.newActionMenuRenameFolder(mainWindow);
		actionMenuDeleteFolder = UiActions.newActionMenuDeleteFolder(mainWindow);
	}

	UiMainWindow mainWindow;

	AbstractAction actionMenuPrint;
	AbstractAction actionMenuModifyBulletin;
	AbstractAction actionMenuSelectAllBulletins;
	AbstractAction actionMenuCutBulletins;
	AbstractAction actionMenuCopyBulletins;
	AbstractAction actionMenuPasteBulletins;
	AbstractAction actionMenuDiscardBulletins;
	AbstractAction actionMenuRenameFolder;
	AbstractAction actionMenuDeleteFolder;
}
