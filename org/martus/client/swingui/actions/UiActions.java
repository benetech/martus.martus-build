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

package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;

import org.martus.client.swingui.UiMainWindow;

public class UiActions
{
	static public UiMartusAction newActionCreate(UiMainWindow mainWindowToUse)
	{
		return new ActionCreate(mainWindowToUse);
	}

	static public UiMartusAction newActionModify(UiMainWindow mainWindowToUse)
	{
		return new ActionModify(mainWindowToUse);
	}

	static public UiMartusAction newActionSearch(UiMainWindow mainWindowToUse)
	{
		return new ActionSearch(mainWindowToUse);
	}

	static public UiMartusAction newActionPrint(UiMainWindow mainWindowToUse)
	{
		return new ActionPrint(mainWindowToUse);
	}


	static public UiMartusAction newActionMenuAbout(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuAbout(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuPrint(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuPrintBulletin(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuModifyBulletin(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuModifyBulletin(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuSelectAllBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuSelectAllBulletins(mainWindowToUse);
	}
	
	static public UiMartusAction newActionMenuCutBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuCutBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuCopyBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuCopyBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuPasteBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuPasteBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuDiscardBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuDiscardBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuRenameFolder(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuRenameFolder(mainWindowToUse);
	}
	
	static public UiMartusAction newActionMenuDeleteFolder(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuDeleteFolder(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuExit(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuExit(mainWindowToUse);
	}
	
	static public UiMartusAction newActionMenuCreateFolder(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuCreateFolder(mainWindowToUse);
	}
	
	static public UiMartusAction newActionMenuCreateNewBulletin(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuCreateNewBulletin(mainWindowToUse);
	}
		
	static public UiMartusAction newActionMenuExportFolder(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuExportFolder(mainWindowToUse);
	}
	
		static public UiMartusAction newActionMenuExportBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuExportBulletins(mainWindowToUse);
	}
	
	static public UiMartusAction newActionMenuBackupMyKeyPair(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuBackupMyKeyPair(mainWindowToUse);
	}
	
	static public UiMartusAction newActionMenuExportMyPublicKey(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuExportMyPublicKey(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuImportHeadquarterPublicKey(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuImportHeadquarterPublicKey(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuRemoveExistingHeadquaterPublicKey(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuRemoveExistingHeadquaterPublicKey(mainWindowToUse);
	}
	
	static public UiMartusAction newActionMenuSearch(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuSearch(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuRetrieveMySealedBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuRetrieveMySealedBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuRetrieveMyDraftBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuRetrieveMyDraftBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuDeleteMyServerDraftBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuDeleteMyServerDraftBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuRetrieveHQSealedBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuRetrieveHQSealedBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuRetrieveHQDraftBulletins(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuRetrieveHQDraftBulletins(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuSelectServer(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuSelectServer(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuPreferences(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuPreferences(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuContactInfo(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuContactInfo(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuDefaultDetailsFieldContent(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuDefaultDetailsFieldContent(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuChangeUserNamePassword(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuChangeUserNamePassword(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuHelp(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuHelp(mainWindowToUse);
	}

	static public UiMartusAction newActionMenuAccountDetails(UiMainWindow mainWindowToUse)
	{
		return new ActionMenuAccountDetails(mainWindowToUse);
	}

	///////////////////////////////////////////////////////////////
	private static class ActionCreate extends UiButtonAction
	{
		public ActionCreate(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "create");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.createBulletin();
		}
	}

	private static class ActionModify extends UiButtonAction
	{
		public ActionModify(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "modify");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doModifyBulletin();
		}
	}

	private static class ActionSearch extends UiButtonAction
	{
		public ActionSearch(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "search");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doSearch();
		}
	}

	private static class ActionPrint extends UiButtonAction
	{
		public ActionPrint(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "print");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doPrint();
		}
	}

	//////////////////////////////////////////////////////////////
	private static class ActionMenuExit extends UiMenuAction
	{
		public ActionMenuExit(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "exit");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.exitNormally();
		}
	}

	private static class ActionMenuCreateFolder extends UiMenuAction
	{
		public ActionMenuCreateFolder(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "CreateNewFolder");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doCreateFolder();
		}
	}

	private static class ActionMenuRenameFolder extends UiMenuAction
	{
		public ActionMenuRenameFolder(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "RenameFolder");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doRenameFolder();
		}

		public boolean isEnabled()
		{
			return mainWindow.canModifyCurrentFolder();
		}
	}

	private static class ActionMenuDeleteFolder extends UiMenuAction
	{
		public ActionMenuDeleteFolder(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "DeleteFolder");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doDeleteFolder();
		}

		public boolean isEnabled()
		{
			return mainWindow.canModifyCurrentFolder();
		}
	}

	private static class ActionMenuCreateNewBulletin extends UiMenuAction
	{
		public ActionMenuCreateNewBulletin(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "CreateNewBulletin");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.createBulletin();
		}
	}
	
	private static class ActionMenuExportFolder extends UiMenuAction
	{
		public ActionMenuExportFolder(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "ExportFolder");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doExportFolder();
		}
	}
	
	private static class ActionMenuExportBulletins extends UiMenuAction
	{
		public ActionMenuExportBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "ExportBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doExportBulletins();
		}
	}

	private static class ActionMenuPrintBulletin extends UiMenuAction
	{
		public ActionMenuPrintBulletin(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "printBulletin");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doPrint();
		}

		public boolean isEnabled()
		{
			return UiMainWindow.isOnlyOneBulletinSelected(mainWindow);
		}

	}

	private static class ActionMenuAbout extends UiMenuAction
	{
		public ActionMenuAbout(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "about");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.aboutMartus();
		}
	}

	private static class ActionMenuAccountDetails extends UiMenuAction
	{
		public ActionMenuAccountDetails(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "ViewMyAccountDetails");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.showAccountInfo();
		}
	}

	private static class ActionMenuHelp extends UiMenuAction
	{
		public ActionMenuHelp(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "helpMessage");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.displayHelpMessage();
		}
	}

	private static class ActionMenuModifyBulletin extends UiMenuAction
	{
		public ActionMenuModifyBulletin(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "modifyBulletin");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doModifyBulletin();
		}

		public boolean isEnabled()
		{
			return UiMainWindow.isOnlyOneBulletinSelected(mainWindow);
		}
	}

	private static class ActionMenuSelectAllBulletins extends UiMenuAction
	{

		public ActionMenuSelectAllBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "SelectAllBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doSelectAllBulletins();
		}

		public boolean isEnabled()
		{
			return !mainWindow.isCurrentFolderEmpty();
		}
	}

	private static class ActionMenuCutBulletins extends UiMenuAction
	{

		public ActionMenuCutBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "CutBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doCutBulletins();
		}

		public boolean isEnabled()
		{
			if(mainWindow.isDiscardedFolderSelected())
				return false;
			return UiMainWindow.isAnyBulletinSelected(mainWindow);
		}
	}

	private static class ActionMenuCopyBulletins extends UiMenuAction
	{
		public ActionMenuCopyBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "CopyBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doCopyBulletins();
		}

		public boolean isEnabled()
		{
			return UiMainWindow.isAnyBulletinSelected(mainWindow);
		}
	}

	private static class ActionMenuDiscardBulletins extends UiMenuAction
	{
		public ActionMenuDiscardBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "DiscardBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(isEnabled())
				mainWindow.doDiscardBulletins();
		}

		public boolean isEnabled()
		{
			updateName();
			return UiMainWindow.isAnyBulletinSelected(mainWindow);
		}

		public void updateName()
		{
			if(mainWindow.isDiscardedFolderSelected())
				putValue(ActionMenuDiscardBulletins.NAME, mainWindow.getLocalization().getMenuLabel("DeleteBulletins"));
			else
				putValue(ActionMenuDiscardBulletins.NAME, mainWindow.getLocalization().getMenuLabel("DiscardBulletins"));
		}
	}

	private static class ActionMenuPasteBulletins extends UiMenuAction
	{
		public ActionMenuPasteBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "PasteBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doPasteBulletins();
		}
		
		public boolean isEnabled()
		{
			return mainWindow.canPaste();
		}

	}

	private static class ActionMenuSearch extends UiMenuAction
	{
		public ActionMenuSearch(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "search");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doSearch();
		}
	}

	static class ActionMenuPreferences extends UiMenuAction
	{
		public ActionMenuPreferences(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "Preferences");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doLocalize();
		}
	}

	private static class ActionMenuContactInfo extends UiMenuAction
	{
		public ActionMenuContactInfo(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "contactinfo");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doContactInfo();
		}
	}

	private static class ActionMenuSelectServer extends UiMenuAction
	{
		public ActionMenuSelectServer(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "SelectServer");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doConfigureServer();
		}
	}

	private static class ActionMenuChangeUserNamePassword extends UiMenuAction
	{
		public ActionMenuChangeUserNamePassword(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "changeUserNamePassword");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doChangeUserNamePassword();
		}
	}

	private static class ActionMenuDefaultDetailsFieldContent extends UiMenuAction
	{
		public ActionMenuDefaultDetailsFieldContent(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "DefaultDetailsFieldContent");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.updateBulletinDetails(null);
		}
	}

	private static class ActionMenuRetrieveMySealedBulletins extends UiMenuAction
	{
		public ActionMenuRetrieveMySealedBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "RetrieveMySealedBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doRetrieveMySealedBulletins();
		}
	}

	private static class ActionMenuRetrieveMyDraftBulletins extends UiMenuAction
	{
		public ActionMenuRetrieveMyDraftBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "RetrieveMyDraftsBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doRetrieveMyDraftBulletins();
		}
	}
	
	private static class ActionMenuDeleteMyServerDraftBulletins extends UiMenuAction
	{
		public ActionMenuDeleteMyServerDraftBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "DeleteMyServerDrafts");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doDeleteServerDraftBulletins();
		}
	}

	private static class ActionMenuRetrieveHQSealedBulletins extends UiMenuAction
	{
		public ActionMenuRetrieveHQSealedBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "RetrieveHQSealedBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doRetrieveHQBulletins();
		}
	}

	private static class ActionMenuRetrieveHQDraftBulletins extends UiMenuAction
	{
		public ActionMenuRetrieveHQDraftBulletins(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "RetrieveHQDraftBulletins");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doRetrieveHQDraftsBulletins();
		}
	}

	private static class ActionMenuBackupMyKeyPair extends UiMenuAction
	{
		public ActionMenuBackupMyKeyPair(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "BackupMyKeyPair");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doBackupKeyPair();
		}
	}

	private static class ActionMenuExportMyPublicKey extends UiMenuAction
	{
		public ActionMenuExportMyPublicKey(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "ExportMyPublicKey");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doExportMyPublicKey();
		}
	}

	private static class ActionMenuImportHeadquarterPublicKey extends UiMenuAction
	{
		public ActionMenuImportHeadquarterPublicKey(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "ImportHQPublicKey");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doImportHQPublicKey();
		}
	}

	private static class ActionMenuRemoveExistingHeadquaterPublicKey extends UiMenuAction
	{
		public ActionMenuRemoveExistingHeadquaterPublicKey(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "RemoveExistingHQPublicKey");
		}

		public void actionPerformed(ActionEvent ae)
		{
			mainWindow.doClearPublicAccountInfo();
		}
	}
}
