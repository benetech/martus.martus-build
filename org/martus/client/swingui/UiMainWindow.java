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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.BulletinHtmlGenerator;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.ConfigInfo;
import org.martus.client.core.CurrentUiState;
import org.martus.client.core.MartusApp;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.core.MartusApp.MartusAppInitializationException;
import org.martus.client.swingui.UiModifyBulletinDlg.CancelHandler;
import org.martus.client.swingui.UiModifyBulletinDlg.DoNothingOnCancel;
import org.martus.client.swingui.UiUtilities.Delay;
import org.martus.common.Bulletin;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.Packet;
import org.martus.common.UniversalId;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusUtilities.ServerErrorException;

public class UiMainWindow extends JFrame implements ClipboardOwner
{
    public UiMainWindow()
	{
		super();
		currentActiveFrame = this;
		try
		{
			app = new MartusApp();
		}
		catch(MartusApp.MartusAppInitializationException e)
		{
			initializationErrorDlg(e.getMessage());
		}
		UiUtilities.updateIcon(this);

		initalizeUiState();
	}

	public boolean run()
	{
		mainWindowInitalizing = true;
		boolean newAccount = false;
		if(app.doesAccountExist())
		{
			if(!signIn(UiSigninDlg.INITIAL))
				return false;
		}
		else
		{
			if(!createAccount())
				return false;
			newAccount = true;
		}
		UiModelessBusyDlg waitingForBulletinsToLoad = new UiModelessBusyDlg(app.getFieldLabel("waitingForBulletinsToLoad"));

		try
		{
			app.loadConfigInfo();
		}
		catch (MartusApp.LoadConfigInfoException e)
		{
			notifyDlg(this, "corruptconfiginfo");
		}

		ConfigInfo info = app.getConfigInfo();
		if(newAccount)
		{
			File defaultDetailsFile = app.getDefaultDetailsFile();
			if(defaultDetailsFile.exists())
				updateBulletinDetails(defaultDetailsFile);
		}

		try
		{
			app.doAfterSigninInitalization();
		}
		catch (MartusAppInitializationException e)
		{
			initializationErrorDlg(e.getMessage());
		}

		if(!info.hasContactInfo())
			doContactInfo();
		else if(info.promptUserRequestSendToServer())
		{
			requestToUpdateContactInfoOnServerAndSaveInfo();
			info.clearPromptUserRequestSendToServer();
		}

		int quarantineCount = app.quarantineUnreadableBulletins();

		if(uiState.getCurrentOperatingState().equals(CurrentUiState.OPERATING_STATE_OK))
		{
			uiState.setCurrentOperatingState(CurrentUiState.OPERATING_STATE_UNKNOWN);
			uiState.save(app.getUiStateFile());
		}

		app.loadFolders();
		int orphanCount = app.repairOrphans();

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowEventHandler());

		initializeViews();
		restoreState();

		if(quarantineCount > 0)
			notifyDlg(this, "FoundDamagedBulletins");

		if(orphanCount > 0)
			notifyDlg(this, "FoundOrphans");

		show();

		inactivityDetector = new InactivityDetector();

		uploader = new java.util.Timer(true);
		uploader.schedule(new TickBackgroundUpload(), 0, BACKGROUND_UPLOAD_CHECK_MILLIS);

		timeoutChecker = new java.util.Timer(true);
		timeoutChecker.schedule(new TickTimeout(), 0, BACKGROUND_TIMEOUT_CHECK_EVERY_X_MILLIS);

		errorChecker = new javax.swing.Timer(10*1000, new UploadErrorChecker());
		errorChecker.start();

		if(uiState.getCurrentOperatingState().equals(CurrentUiState.OPERATING_STATE_UNKNOWN))
		{
			uiState.setCurrentOperatingState(CurrentUiState.OPERATING_STATE_OK);
			uiState.save(app.getUiStateFile());
		}
		waitingForBulletinsToLoad.endDialog();
		mainWindowInitalizing = false;
		return true;
    }

    public boolean isMainWindowInitalizing()
    {
    	return mainWindowInitalizing;
    }

    public MartusApp getApp()
    {
		return app;
	}

	public BulletinStore getStore()
	{
		return getApp().getStore();
	}

	public void resetCursor(Cursor originalCursor)
	{
		setCursor(originalCursor);
	}

	public Cursor setWaitingCursor()
	{
		Cursor originalCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		return originalCursor;
	}

	public void bulletinSelectionHasChanged()
	{
		Bulletin b = table.getSingleSelectedBulletin();
		actionEdit.setEnabled(b != null);
		actionPrint.setEnabled(b != null);
		preview.setCurrentBulletin(b);
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		table.bulletinContentsHaveChanged(b);
		preview.bulletinContentsHaveChanged(b);
	}

	public void folderSelectionHasChanged(BulletinFolder f)
	{
		Cursor originalCursor = setWaitingCursor();
		table.setFolder(f);
		resetCursor(originalCursor);
	}

	public void folderContentsHaveChanged(BulletinFolder f)
	{
		folders.folderContentsHaveChanged(f);
		table.folderContentsHaveChanged(f);
	}

	public void folderTreeContentsHaveChanged()
	{
		folders.folderTreeContentsHaveChanged();
	}

	public boolean isDiscardedFolderSelected()
	{
		return folders.getSelectedFolderName().equals(app.getStore().getFolderDiscarded().getName());
	}

	public boolean isCurrentFolderEmpty()
	{
		if(table.getBulletinCount() == 0)
			return true;
		return false;
	}

	public boolean canPaste()
	{
		if(UiClipboardUtilities.getClipboardTransferableBulletin() != null)
			return true;

		if(UiClipboardUtilities.getClipboardTransferableFile() != null)
			return true;

		return false;
	}

	boolean canModifyCurrentFolder()
	{
		BulletinFolder folder = folders.getSelectedFolder();
		return canModifyFolder(folder);
	}

	boolean canModifyFolder(BulletinFolder folder)
	{
		if(folder == null)
			return false;
		return folder.canRename();
	}

	public void selectSentFolder()
	{
		BulletinStore store = getStore();
		BulletinFolder folder = store.getFolderSent();
		folders.selectFolder(folder.getName());
	}

	public void selectSearchFolder()
	{
		folders.selectFolder(getStore().getSearchFolderName());
	}

	public void selectNewCurrentBulletin(int currentPosition)
	{
		if(currentPosition == -1)
			table.selectLastBulletin();
		else
			table.setCurrentBulletinIndex(currentPosition);
	}

	public boolean confirmDlg(JFrame parent, String baseTag)
	{
		return UiUtilities.confirmDlg(getApp(), parent, baseTag);
	}

	public boolean confirmDlg(JFrame parent, String title, String[] contents)
	{
		return UiUtilities.confirmDlg(getApp(), parent, title, contents);
	}

	public void notifyDlg(JFrame parent, String baseTag)
	{
		notifyDlg(parent, baseTag, "notify" + baseTag);
	}

	public void notifyDlg(JFrame parent, String baseTag, String titleTag)
	{
		UiUtilities.notifyDlg(getApp(), parent, baseTag, titleTag);
	}

	public void messageDlg(JFrame parent, String baseTag, String message)
	{
		UiUtilities.messageDlg(getApp(), parent, baseTag, message);
	}

	private void initializationErrorDlg(String message)
	{
		String title = "Error Starting Martus";
		String cause = "Unable to start Martus: " + message;
		String ok = "OK";
		String[] buttons = { ok };
		JOptionPane pane = new JOptionPane(cause, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
								null, buttons);
		JDialog dialog = pane.createDialog(null, title);
		dialog.show();
		System.exit(1);
	}

	public String getStringInput(String baseTag, String description, String defaultText)
	{
		UiStringInputDlg inputDlg = new UiStringInputDlg(this, getApp(), baseTag, description, defaultText);
		inputDlg.show();
		return inputDlg.getResult();
	}

	public AbstractAction getActionMenuEdit()
	{
		return actionMenuModifyBulletin;
	}

	public AbstractAction getActionMenuSelectAll()
	{
		return actionMenuSelectAllBulletins;
	}

	public AbstractAction getActionMenuCut()
	{
		return actionMenuCutBulletins;
	}

	public AbstractAction getActionMenuCopy()
	{
		return actionMenuCopyBulletins;
	}

	public AbstractAction getActionMenuPaste()
	{
		return actionMenuPasteBulletins;
	}

	public AbstractAction getActionMenuDiscard()
	{
		return actionMenuDiscardBulletins;
	}

	//ClipboardOwner Interface
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		System.out.println("UiMainWindow: ClipboardOwner.lostOwnership");
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(contents);
		if(tb != null)
			tb.dispose();
	}


	public void setCurrentDefaultKeyboardVirtual(boolean keyboard)
	{
		uiState.setCurrentDefaultKeyboardVirtual(keyboard);
	}

	public boolean isCurrentDefaultKeyboardVirtual()
	{
		return uiState.isCurrentDefaultKeyboardVirtual();
	}

	Dimension getBulletinEditorDimension()
	{
		return uiState.getCurrentEditorDimension();
	}

	Point getBulletinEditorPosition()
	{
		return uiState.getCurrentEditorPosition();
	}

	boolean isBulletinEditorMaximized()
	{
		return uiState.isCurrentEditorMaximized();
	}

	void setBulletinEditorDimension(Dimension size)
	{
		uiState.setCurrentEditorDimension(size);
	}

	void setBulletinEditorPosition(Point position)
	{
		uiState.setCurrentEditorPosition(position);
	}

	void setBulletinEditorMaximized(boolean maximized)
	{
		uiState.setCurrentEditorMaximized(maximized);
	}

	public void saveCurrentUiState() throws IOException
	{
		uiState.save(app.getUiStateFile());
	}

	public void saveState()
	{
		try
		{
			saveStateWithoutPrompting();
		}
		catch(IOException e)
		{
			notifyDlg(null, "ErrorSavingState");
		}
	}

	void saveStateWithoutPrompting() throws IOException
	{
		String folderName = folders.getSelectedFolderName();
		BulletinFolder folder = getStore().findFolder(folderName);
		uiState.setCurrentFolder(folderName);
		uiState.setCurrentDateFormat(app.getCurrentDateFormatCode());
		uiState.setCurrentLanguage(app.getCurrentLanguage());
		if(folder != null)
		{
			uiState.setCurrentSortTag(folder.sortedBy());
			uiState.setCurrentSortDirection(folder.getSortDirection());
			uiState.setCurrentBulletinPosition(table.getCurrentBulletinIndex());
		}
		uiState.setCurrentPreviewSplitterPosition(previewSplitter.getDividerLocation());
		uiState.setCurrentFolderSplitterPosition(folderSplitter.getDividerLocation());
		uiState.setCurrentAppDimension(getSize());
		uiState.setCurrentAppPosition(getLocation());
		boolean isMaximized = getExtendedState()==MAXIMIZED_BOTH;
		uiState.setCurrentAppMaximized(isMaximized);
		saveCurrentUiState();
	}

	public void restoreState()
	{
		String folderName = uiState.getCurrentFolder();
		BulletinFolder folder = getStore().findFolder(folderName);

		if(folder == null)
		{
			selectSentFolder();
			return;
		}

		try
		{
			String sortTag = uiState.getCurrentSortTag();
			folder.sortBy(sortTag);
			if(folder.getSortDirection() != uiState.getCurrentSortDirection())
				folder.sortBy(sortTag);
			folders.selectFolder(folderName);
			if(!uiState.getCurrentOperatingState().equals(CurrentUiState.OPERATING_STATE_BAD))
				table.setCurrentBulletinIndex(uiState.getCurrentBulletinPosition());
		}
		catch(Exception e)
		{
			System.out.println("UiMainWindow.restoreState: " + e);
		}
	}

	private void initalizeUiState()
	{
		uiState = new CurrentUiState();
		File stateFile = app.getUiStateFile();
		uiState.load(stateFile);
		uiState.setCurrentLanguage(app.getCurrentLanguage());
		uiState.setCurrentDateFormat(app.getCurrentDateFormatCode());
		if(uiState.getCurrentOperatingState().equals(CurrentUiState.OPERATING_STATE_UNKNOWN))
		{
			uiState.setCurrentOperatingState(CurrentUiState.OPERATING_STATE_BAD);
			uiState.save(stateFile);
		}
	}

	public void selectBulletinInCurrentFolderIfExists(UniversalId id)
	{
		BulletinFolder currentFolder = app.getStore().findFolder(folders.getSelectedFolderName());
		int position = currentFolder.find(id);
		if(position != -1)
			table.setCurrentBulletinIndex(position);
	}

	private JComponent createTopStuff()
	{
		JPanel topStuff = new JPanel(false);
		topStuff.setLayout(new GridLayout(2, 1));
		topStuff.add(createMenubar());
		topStuff.add(createToolbar());
		return topStuff;
	}

	private void createActions()
	{
		actionCreate = UiActions.newActionCreate(this);
		actionEdit = UiActions.newActionModify(this);
		actionSearch = UiActions.newActionSearch(this);
		actionPrint = UiActions.newActionPrint(this);

		actionMenuPrint = UiActions.newActionMenuPrint(this);

		actionMenuModifyBulletin = UiActions.newActionMenuModifyBulletin(this);
		actionMenuSelectAllBulletins = UiActions.newActionMenuSelectAllBulletins(this);
		actionMenuCutBulletins = UiActions.newActionMenuCutBulletins(this);
		actionMenuCopyBulletins = UiActions.newActionMenuCopyBulletins(this);
		actionMenuPasteBulletins = UiActions.newActionMenuPasteBulletins(this);
		actionMenuDiscardBulletins = UiActions.newActionMenuDiscardBulletins(this);

		actionMenuRenameFolder = UiActions.newActionMenuRenameFolder(this);
		actionMenuDeleteFolder = UiActions.newActionMenuDeleteFolder(this);
	}

	private JComponent createMenubar()
	{
		JMenu file = new JMenu(app.getMenuLabel("file"));
		PrintMenuListener printMenuListener = new PrintMenuListener();
		file.addMenuListener(printMenuListener);
		printMenuListener.initalize();

		file.add(UiActions.newActionMenuCreateNewBulletin(this));
		file.add(actionMenuPrint);
		file.addSeparator();
		file.add(UiActions.newActionMenuBackupMyKeyPair(this));
		file.add(UiActions.newActionMenuExportMyPublicKey(this));
		file.addSeparator();
		file.add(UiActions.newActionMenuExportBulletins(this));
		file.addSeparator();
		file.add(UiActions.newActionMenuImportHeadquarterPublicKey(this));
		file.add(UiActions.newActionMenuRemoveExistingHeadquaterPublicKey(this));
		file.addSeparator();
		file.add(UiActions.newActionMenuExit(this));


		JMenu edit = new JMenu(app.getMenuLabel("edit"));
		EditMenuListener menuListener = new EditMenuListener();
		edit.addMenuListener(menuListener);
		menuListener.initalize();

		edit.add(UiActions.newActionMenuSearch(this));
		edit.addSeparator();
		edit.add(actionMenuModifyBulletin);
		edit.addSeparator();
		edit.add(actionMenuCutBulletins);
		edit.add(actionMenuCopyBulletins);
		edit.add(actionMenuPasteBulletins);
		edit.add(actionMenuSelectAllBulletins);
		edit.addSeparator();
		edit.add(actionMenuDiscardBulletins);

		JMenu folders = new JMenu(app.getMenuLabel("folders"));
		FoldersMenuListener menuFolderListener = new FoldersMenuListener();
		folders.addMenuListener(menuFolderListener);
		menuFolderListener.initalize();

		folders.add(UiActions.newActionMenuCreateFolder(this));
		folders.add(actionMenuRenameFolder);
		folders.add(actionMenuDeleteFolder);


		JMenu server = new JMenu(app.getMenuLabel("server"));
		server.add(UiActions.newActionMenuRetrieveMySealedBulletins(this));
		server.add(UiActions.newActionMenuRetrieveMyDraftBulletins(this));
		server.add(UiActions.newActionMenuDeleteMyServerDraftBulletins(this));
		server.addSeparator();
		server.add(UiActions.newActionMenuRetrieveHQSealedBulletins(this));
		server.add(UiActions.newActionMenuRetrieveHQDraftBulletins(this));
		server.addSeparator();
		server.add(UiActions.newActionMenuSelectServer(this));


		JMenu options = new JMenu(app.getMenuLabel("options"));
		options.add(UiActions.newActionMenuPreferences(this));
		options.add(UiActions.newActionMenuContactInfo(this));
		options.add(UiActions.newActionMenuDefaultDetailsFieldContent(this));
		options.add(UiActions.newActionMenuChangeUserNamePassword(this));


		JMenu help = new JMenu(app.getMenuLabel("help"));
		help.add(UiActions.newActionMenuHelp(this));
		help.add(UiActions.newActionMenuAbout(this));
		help.addSeparator();
		help.add(UiActions.newActionMenuAccountDetails(this));

		JMenuBar menubar = new JMenuBar();
		menubar.add(file);
		menubar.add(edit);
		menubar.add(folders);
		menubar.add(server);
		menubar.add(options);
		menubar.add(help);
		return menubar;
	}

	private JComponent createToolbar()
	{
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(actionCreate);
		toolbar.add(actionEdit);
		toolbar.add(actionSearch);
		toolbar.add(actionPrint);
		return toolbar;
	}


	void doModifyBulletin()
	{
		table.doModifyBulletin();
	}

	void doSelectAllBulletins()
	{
		table.doSelectAllBulletins();	
	}

	public void doCutBulletins()
	{
		table.doCutBulletins();
	}

	public void doCopyBulletins()
	{
		table.doCopyBulletins();
	}

	public void doPasteBulletins()
	{
		table.doPasteBulletins();
	}

	void doDiscardBulletins()
	{
		table.doDiscardBulletins();
	}
	
	void doCreateFolder()
	{
		folders.createNewFolder();
	}
	
	void doRenameFolder()
	{
		folders.renameCurrentFolder();
	}
	
	void doDeleteFolder()
	{
		folders.deleteCurrentFolderIfPossible();
	}
	
	void doSearch()
	{
		UiSearchDlg searchDlg = new UiSearchDlg(this);
		if(!searchDlg.getResults())
			return;
		Cursor originalCursor = setWaitingCursor();

		app.search(searchDlg.getSearchString(), searchDlg.getStartDate(), searchDlg.getEndDate());
		BulletinStore store = getStore();
		BulletinFolder searchFolder = store.findFolder(store.getSearchFolderName());
		folders.folderTreeContentsHaveChanged();
		folders.folderContentsHaveChanged(searchFolder);
		int bulletinsFound = searchFolder.getBulletinCount();
		resetCursor(originalCursor);
		if(bulletinsFound > 0)
		{
			selectSearchFolder();
			String title = getApp().getWindowTitle("notifySearchFound");
			String cause = getApp().getFieldLabel("notifySearchFoundcause");
			String ok = app.getButtonLabel("ok");
			String[] buttons = { ok };
			cause = cause + bulletinsFound;
			JOptionPane pane = new JOptionPane(cause, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
									null, buttons);
			JDialog dialog = pane.createDialog(this, title);
			dialog.show();
		}
		else
		{
			notifyDlg(this, "SearchFailed");
		}
	}

	void aboutMartus()
	{
		new UiAboutDlg(this);
	}

	void showAccountInfo()
	{
		String title = app.getWindowTitle("AccountInfo");
		String userName = app.getFieldLabel("AccountInfoUserName")
						  + app.getUserName();
		String keyDescription = app.getFieldLabel("AccountInfoPublicKey");
		String keyContents = app.getAccountId();
		String codeDescription = app.getFieldLabel("AccountInfoPublicCode");
		String codeContents = null;
		String formattedCodeContents = null;
		try
		{
			codeContents = MartusUtilities.computePublicCode(keyContents);
			formattedCodeContents = MartusUtilities.formatPublicCode(codeContents);
		}
		catch(InvalidBase64Exception e)
		{
		}
		String ok = app.getButtonLabel("ok");
		String[] contents = {userName, " ", keyDescription, keyContents," ", codeDescription, formattedCodeContents};
		String[] buttons = {ok};

		new UiNotifyDlg(this, title, contents, buttons);
	}

	void displayHelpMessage()
	{

		InputStream helpStream = null;
		InputStream helpStreamTOC = null;
		String helpFileShortName = app.getHelpFilename();
		String helpTOCFileShortName = app.getHelpTOCFilename();
		File helpFile = new File(MartusApp.getTranslationsDirectory(), helpFileShortName);
		File helpTOCFile = new File(MartusApp.getTranslationsDirectory(), helpTOCFileShortName);
		try
		{
			if(helpFile.exists())
				helpStream = new FileInputStream(helpFile);
			else
				helpStream = getClass().getResourceAsStream(helpFileShortName);
			if(helpStream == null)
				helpStream = getClass().getResourceAsStream(app.getEnglishHelpFilename());

			if(helpTOCFile.exists())
				helpStreamTOC = new FileInputStream(helpTOCFile);
			else
				helpStreamTOC = getClass().getResourceAsStream(helpTOCFileShortName);

			new UiDisplayFileDlg(this, "Help", helpStream, "OnlineHelpMessage", helpStreamTOC, "OnlineHelpTOCMessage");
		}
		catch (IOException e)
		{
			System.out.println("UiMainWIndow.displayHelpMessage " + e);
		}
	}

	void doPrint()
	{
		Bulletin currentBulletin = table.getSingleSelectedBulletin();
		if(currentBulletin == null)
			return;

		printBulletin(currentBulletin);
		requestFocus(true);
	}

	void printBulletin(Bulletin currentBulletin)
	{
		int width = preview.getView().getWidth();
		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(width, app);
		String html = generator.getHtmlString(currentBulletin);
		JComponent view = new JLabel(html);
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.pack();
		
		PrintPageFormat format = new PrintPageFormat();
		JComponentVista vista = new JComponentVista(view, format);
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPageable(vista);
		HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		boolean printCancelled = false;
		while(true)
		{
			if (job.printDialog(attributes))
			{
				format.setFromAttributes(attributes);
				if(format.mustWarnUser)
				{
					if(confirmDlg(this, "PrinterWarning"))
						continue;
				}
				vista.scaleToFitX();
				job.setPageable(vista);
				break;
			}
			else
			{
				printCancelled = true;
				break;
			}
		}
		if(!printCancelled)
		{
			try
			{
				job.print(attributes);
			}
			catch (PrinterException e)
			{
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}

	void doLocalize()
	{
		saveState();
		new UiLocalizeDlg(this);
		initializeViews();
		restoreState();
		show();
	}

	boolean doContactInfo()
	{
		ConfigInfo info = app.getConfigInfo();
		UiContactInfoDlg setupContactDlg = new UiContactInfoDlg(this, info);
		boolean pressedOk = setupContactDlg.getResult();
		if(pressedOk)
			requestToUpdateContactInfoOnServerAndSaveInfo();
		// the following is required (for unknown reasons)
		// to get the window to redraw after the dialog
		// is closed. Yuck! kbs.
		repaint();
		return pressedOk;
	}


	void doConfigureServer()
	{
		if(!reSignIn())
			return;
		inConfigServer = true;
		ConfigInfo info = app.getConfigInfo();
		UiConfigServerDlg serverInfoDlg = new UiConfigServerDlg(this, info);
		if(serverInfoDlg.getResult())
		{
			String serverIPAddress = serverInfoDlg.getServerIPAddress();
			boolean magicAccepted = false;
			app.setServerInfo(serverIPAddress, serverInfoDlg.getServerPublicKey());
			if(app.requestServerUploadRights(""))
				magicAccepted = true;
			else
			{
				while (true)
				{
					String magicWord = getStringInput("servermagicword", "", "");
					if(magicWord == null)
						break;
					if(app.requestServerUploadRights(magicWord))
					{
						magicAccepted = true;
						break;
					}
					notifyDlg(this, "magicwordrejected");
				}
			}

			String title = app.getWindowTitle("ServerSelectionResults");
			String serverSelected = app.getFieldLabel("ServerSelectionResults") + serverIPAddress;
			String uploadGranted = "";
			if(magicAccepted)
				uploadGranted = app.getFieldLabel("ServerAcceptsUploads");
			else
				uploadGranted = app.getFieldLabel("ServerDeclinesUploads");

			String ok = app.getButtonLabel("ok");
			String[] contents = {serverSelected, uploadGranted};
			String[] buttons = {ok};

			new UiNotifyDlg(currentActiveFrame, title, contents, buttons);
			if(magicAccepted)
				requestToUpdateContactInfoOnServerAndSaveInfo();
			inConfigServer = false;
		}
	}

	private void requestToUpdateContactInfoOnServerAndSaveInfo()
	{
		boolean sendInfo = confirmDlg(this, "RequestToSendContactInfoToServer");
		app.getConfigInfo().setSendContactInfoToServer(sendInfo);
		try
		{
			app.saveConfigInfo();
		}
		catch (MartusApp.SaveConfigInfoException e)
		{
			notifyDlg(this, "ErrorSavingConfig");
		}
	}

	private boolean reSignIn()
	{
		boolean signedIn = signIn(UiSigninDlg.SECURITY_VALIDATE);
		if(!app.isSignedIn())
			exitWithoutPrompting();
		return signedIn;
	}


	void doChangeUserNamePassword()
	{
		if(!reSignIn())
			return;
		String originalUserName = app.getUserName();
		UiCreateNewUserNameAndPasswordDlg newUserInfo = new UiCreateNewUserNameAndPasswordDlg(this, originalUserName);
		if(!newUserInfo.isDataValid())
			return;
		String userName = newUserInfo.getUserName();
		String userPassword = newUserInfo.getPassword();
		try
		{
			app.writeKeyPairFile(userName, userPassword);
		}
		catch(Exception e)
		{
			notifyDlg(this, "RewriteKeyPairFailed");
			return;
			//TODO eventually try to restore keypair from backup.
		}

		notifyDlg(this, "RewriteKeyPairWorked");
	}

	public void updateBulletinDetails(File defaultFile)
	{
		ConfigInfo info = app.getConfigInfo();
		UiTemplateDlg templateDlg = new UiTemplateDlg(this, info);
		try
		{
			if(defaultFile != null)
			{
				templateDlg.loadFile(defaultFile);
				notifyDlg(this, "ConfirmCorrectDefaultDetailsData");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}

		templateDlg.show();
		if(templateDlg.getResult())
		{
			try
			{
				app.saveConfigInfo();
			}
			catch (MartusApp.SaveConfigInfoException e)
			{
				System.out.println("doContactInfo: Unable to Save ConfigInfo" + e);
			}
		}
	}

	void doRetrieveMySealedBulletins()
	{
		String dlgTitleTag = "RetrieveMySealedBulletins";
		String summariesProgressTag = "RetrieveMySealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveMySealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedSealed();

		RetrieveTableModel model = new RetrieveMyTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	void doRetrieveMyDraftBulletins()
	{
		String dlgTitleTag = "RetrieveMyDraftBulletins";
		String summariesProgressTag = "RetrieveMyDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveMyDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedDraft();

		RetrieveTableModel model = new RetrieveMyDraftsTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	void doRetrieveHQBulletins()
	{
		String dlgTitleTag = "RetrieveHQSealedBulletins";
		String summariesProgressTag = "RetrieveHQSealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQSealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeSealed();

		RetrieveTableModel model = new RetrieveHQTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	void doRetrieveHQDraftsBulletins()
	{
		String dlgTitleTag = "RetrieveHQDraftBulletins";
		String summariesProgressTag = "RetrieveHQDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeDraft();

		RetrieveTableModel model = new RetrieveHQDraftsTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	void doDeleteServerDraftBulletins()
	{
		String dlgTitleTag = "DeleteMyDraftsFromServer";
		String summariesProgressTag = "RetrieveMyDraftBulletinSummaries";

		RetrieveTableModel model = new DeleteMyServerDraftsTableModel(app);
		deleteServerDrafts(model, dlgTitleTag, summariesProgressTag);
	}

	private void retrieveBulletins(RetrieveTableModel model, String folderName,
						String dlgTitleTag, String summariesProgressTag, String retrieverProgressTag)
	{
		String topMessageTag = "RetrieveSummariesMessage";
		String okButtonTag = "retrieve";
		String noneSelectedTag = "retrievenothing";

		try
		{
			Vector uidList = displaySummariesDlg(model, dlgTitleTag, topMessageTag, okButtonTag, noneSelectedTag, summariesProgressTag);
			if(uidList == null)
				return;

			BulletinFolder retrievedFolder = app.createOrFindFolder(folderName);
			app.getStore().saveFolders();

			UiProgressRetrieveBulletinsDlg progressDlg = new UiProgressRetrieveBulletinsDlg(this, retrieverProgressTag);
			Retriever retriever = new Retriever(app, progressDlg);
			retriever.retrieveBulletins(uidList, retrievedFolder);
			retriever.progressDlg.show();
			if(progressDlg.shouldExit())
				notifyDlg(this, "RetrieveCanceled");
			else
			{
				String result = retriever.getResult();
				if(!result.equals(NetworkInterfaceConstants.OK))
					notifyDlg(this, "retrievefailed", dlgTitleTag);
				else
					notifyDlg(this, "retrieveworked", dlgTitleTag);
			}

			folderTreeContentsHaveChanged();
			folders.folderContentsHaveChanged(retrievedFolder);
			folders.selectFolder(folderName);
		}
		catch(ServerErrorException e)
		{
			notifyDlg(this, "ServerError");
			return;
		}
	}

	private void deleteServerDrafts(RetrieveTableModel model,
						String dlgTitleTag, String summariesProgressTag)
	{
		String topMessageTag = "DeleteServerDraftsMessage";
		String okButtonTag = "DeleteServerDrafts";
		String noneSelectedTag = "DeleteServerDraftsNone";

		try
		{
			Vector uidList = displaySummariesDlg(model, dlgTitleTag, topMessageTag, okButtonTag, noneSelectedTag, summariesProgressTag);
			if(uidList == null)
				return;

			Cursor originalCursor = setWaitingCursor();
			try
			{
				String result = app.deleteServerDraftBulletins(uidList);
				if(!result.equals(NetworkInterfaceConstants.OK))
				{
					notifyDlg(this, "DeleteServerDraftsFailed");
					return;
				}

				notifyDlg(this, "DeleteServerDraftsWorked");
			}
			finally
			{
				resetCursor(originalCursor);
			}
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			notifyDlg(this, "UnexpectedError");
			return;
		}
		catch (Packet.WrongAccountException e)
		{
			notifyDlg(this, "UnexpectedError");
			return;
		}
		catch(ServerErrorException e)
		{
			notifyDlg(this, "ServerError");
			return;
		}
	}


	private Vector displaySummariesDlg(RetrieveTableModel model, String dlgTitleTag, String topMessageTag, String okButtonTag, String noneSelectedTag, String summariesProgressTag) throws
		ServerErrorException
	{
		if(!app.isSSLServerAvailable())
		{
			notifyDlg(this, "retrievenoserver", dlgTitleTag);
			return null;
		}
		UiProgressRetrieveSummariesDlg progressDlg = new UiProgressRetrieveSummariesDlg(this, summariesProgressTag);
		model.initialize(progressDlg);
		if(progressDlg.shouldExit())
			return null;
		try
		{
			model.checkIfErrorOccurred();
		}
		catch (ServerErrorException e)
		{
			notifyDlg(this, "RetrievedOnlySomeSummaries", dlgTitleTag);
		}
		UiServerSummariesDlg summariesDlg = new UiServerSummariesDlg(this, model, dlgTitleTag, topMessageTag, okButtonTag, noneSelectedTag);

		// the following is required (for unknown reasons)
		// to get the window to redraw after the dialog
		// is closed. Yuck! kbs.
		repaint();

		if(!summariesDlg.getResult())
			return null;

		return summariesDlg.getUniversalIdList();
	}

	void doExportMyPublicKey()
	{
		try
		{
			File export;
			do
			{
				String fileName = getStringInput("ExportMyPublicKey", "NameOfExportedFile", "");
				if(fileName == null)
					return;
				export = app.getPublicInfoFile(fileName);
				if(export.exists())
				{
					if(confirmDlg(this, "OverWriteExistingFile"))
						export.delete();
				}
			}while(export.exists());
			
			app.exportPublicInfo(export);
			String title = getApp().getWindowTitle("notifyExportMyPublicKey");
			String msg = getApp().getFieldLabel("notifyExportMyPublicKeycause");
			String ok = app.getButtonLabel("ok");
			String[] contents = {msg, export.getCanonicalPath()};
			String[] buttons = {ok};
			new UiNotifyDlg(currentActiveFrame, title, contents, buttons);
		}
		catch(Exception e)
		{
			System.out.println("UiMainWindow.doExportMyPublicKey :" + e);
		}
	}

	void doBackupKeyPair()
	{
		File keypairFile = app.getKeyPairFile();
		if(keypairFile.length() > MAX_KEYPAIRFILE_SIZE)
		{
			System.out.println("keypair file too large!");
			notifyDlg(this, "ErrorBackingupKeyPair");
			return;
		}

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(app.getWindowTitle("saveBackupKeyPair"));
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setSelectedFile(new File(MartusApp.KEYPAIR_FILENAME));
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File newBackupFile = chooser.getSelectedFile();
			if(newBackupFile.exists())
				if(!confirmDlg(this, "OverWriteExistingFile"))
					return;
			try
			{
				FileInputStream input = new FileInputStream(keypairFile);
				FileOutputStream output = new FileOutputStream(newBackupFile);

				int originalKeyPairFileSize = (int) keypairFile.length();
				byte[] inputArray = new byte[originalKeyPairFileSize];

				input.read(inputArray);
				output.write(inputArray);
				input.close();
				output.close();
			}
			catch (FileNotFoundException fnfe)
			{
				notifyDlg(this, "ErrorBackingupKeyPair");
			}
			catch (IOException ioe)
			{
				System.out.println(ioe.getMessage());
				notifyDlg(this, "ErrorBackingupKeyPair");
			}
		}
	}

	class PublicInfoFileFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			if(pathname.isDirectory())
				return true;
			return(pathname.getName().endsWith(MartusApp.PUBLIC_INFO_EXTENSION));
		}

		public String getDescription()
		{
			return app.getFieldLabel("PublicInformationFiles");
		}
	}

	void doImportHQPublicKey()
	{
		if(!reSignIn())
			return;
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText(app.getButtonLabel("inputImportPublicCodeok"));
		chooser.setFileFilter(new PublicInfoFileFilter());
		chooser.setDialogTitle(app.getWindowTitle("ImportHQPublicKey"));
    	chooser.setCurrentDirectory(new File(app.getDataDirectory()));
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			File importFile = chooser.getSelectedFile();
			try
			{
				String publicKey = app.extractPublicInfo(importFile);
				String publicCode = MartusUtilities.computePublicCode(publicKey);
				if(confirmPublicCode(publicCode, "ImportPublicCode", "AccountCodeWrong"))
				{
					if(confirmDlg(this, "SetImportPublicKey"))
						app.setHQKey(publicKey);
				}
			}
			catch(MartusApp.SaveConfigInfoException e)
			{
				notifyDlg(this, "ErrorSavingConfig");
			}
			catch(Exception e)
			{
				notifyDlg(this, "PublicInfoFileError");
			}
		}
	}

	void doClearPublicAccountInfo()
	{
		if(!reSignIn())
			return;
		try
		{
			if(confirmDlg(this, "ClearHQInformation"))
				app.clearHQKey();
		}
		catch(MartusApp.SaveConfigInfoException e)
		{
			notifyDlg(this, "ErrorSavingConfig");
		}
		catch(Exception e)
		{
			notifyDlg(this, "PublicInfoFileError");
		}
	}


	private boolean confirmPublicCode(String publicCode, String baseTag, String errorBaseTag)
	{
		String userEnteredPublicCode = "";
		//TODO remove prints before release
		//System.out.println("Public code required:" + publicCode);
		while(true)
		{
			userEnteredPublicCode = getStringInput(baseTag, "", userEnteredPublicCode);
			if(userEnteredPublicCode == null)
				return false; // user hit cancel
			String normalizedPublicCode = MartusUtilities.removeNonDigits(userEnteredPublicCode);

			if(publicCode.equals(normalizedPublicCode))
				return true;

			//System.out.println("Entered:     " + userEnteredPublicCode);
			//System.out.println("Normalized:   " + normalizedPublicCode);
			notifyDlg(this, errorBaseTag);
		}
	}

	private void initializeViews()
	{
		getContentPane().removeAll();

		setTitle(app.getWindowTitle("main"));
		createActions();

		preview = new UiBulletinPreview(this);
		table = new UiBulletinTablePane(this);
		folders = new UiFolderTreePane(this);
		getContentPane().add(createTopStuff(), BorderLayout.NORTH);

		previewSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, table, preview);
		previewSplitter.setDividerLocation(uiState.getCurrentPreviewSplitterPosition());

		folderSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, folders, previewSplitter);
		folderSplitter.setDividerLocation(uiState.getCurrentFolderSplitterPosition());

		getContentPane().add(folderSplitter);
		statusBar = new UiStatusBar();
		statusBar.getBackgroundProgressMeter().setStatusMessageAndHideMeter(app.getFieldLabel("StatusReady"));
		getContentPane().add(statusBar, BorderLayout.SOUTH );

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		Dimension appDimension = uiState.getCurrentAppDimension();
		Point appPosition = uiState.getCurrentAppPosition();
		boolean showMaximized = false;
		if(UiUtilities.isValidScreenPosition(screenSize, appDimension, appPosition))
		{
			setLocation(appPosition);
			setSize(appDimension);
			if(uiState.isCurrentAppMaximized())
				showMaximized = true;
		}
		else
			showMaximized = true;
		if(showMaximized)
		{
			setSize(screenSize.width - 50 , screenSize.height - 50);
			UiUtilities.maximizeWindow(this);
		}
	}

	private boolean signIn(int mode)
	{
		int seconds = 0;
		UiModelessBusyDlg busyDlg = null;
		while(true)
		{
			Delay delay = new Delay(seconds);
			delay.start();
			UiUtilities.waitForThreadToTerminate(delay);
			if( busyDlg != null )
			{
				busyDlg.endDialog();
			}

			seconds = seconds * 2 + 1;
			if(mode == UiSigninDlg.TIMED_OUT)
			{
				//Forces this dialog to the top of all windows in system by switching from iconified to normal, then just make the main window not visible	
				currentActiveFrame.setState(NORMAL);
				currentActiveFrame.setVisible(false);
			}
			UiSigninDlg signinDlg = new UiSigninDlg(this, currentActiveFrame, mode);
			if(!signinDlg.getResult())
				return false;
			if(!app.attemptSignIn(signinDlg.getName(), signinDlg.getPassword()))
			{
				notifyDlg(currentActiveFrame, "incorrectsignin");
				busyDlg = new UiModelessBusyDlg(app.getFieldLabel("waitAfterFailedSignIn"));
				continue;
			}
			break;
		}
		if(mode == UiSigninDlg.TIMED_OUT)
			currentActiveFrame.setVisible(true);
		
		return true;
	}

	private boolean createAccount()
	{
		notifyDlg(this, "WelcomeToMartus");
		UiCreateNewUserNameAndPasswordDlg newUserInfo = new UiCreateNewUserNameAndPasswordDlg(this, "");
		if(!newUserInfo.isDataValid())
			return false;
		String userName = newUserInfo.getUserName();
		String userPassword = newUserInfo.getPassword();

		UiModelessBusyDlg waitingForKeyPair = new UiModelessBusyDlg(app.getFieldLabel("waitingForKeyPairGeneration"));
		try
		{
			app.createAccount(userName ,userPassword);
		}
		catch(Exception e)
		{
			waitingForKeyPair.endDialog();
			notifyDlg(this, "createaccountfailed");
			return false;
		}
		waitingForKeyPair.endDialog();
		return true;
	}

	private boolean doUploadReminderOnExit()
	{
		boolean dontExitApplication = false;
		if(app.shouldShowSealedUploadReminderOnExit())
		{
			if(confirmDlg(this, "UploadReminder"))
				app.resetLastUploadRemindedTime();
			else
				dontExitApplication = true;
		}
		else if(app.shouldShowDraftUploadReminder())
		{
			if(!confirmDlg(this, "DraftUploadReminder"))
				dontExitApplication = true;
		}
		return dontExitApplication;
	}

	void exitNormally()
	{
		if(doUploadReminderOnExit())
			return;
		saveState();
		getStore().prepareToExit();
		System.exit(0);
	}

	private void exitWithoutPrompting()
	{
		try
		{
			saveStateWithoutPrompting();
		}
		catch (IOException e)
		{
			System.out.println("UiMainWindow.exitWithoutPrompting: " + e);
		}
		getStore().prepareToExit();
		System.exit(0);
	}

	public void createBulletin()
	{
		Bulletin b = app.createBulletin();
		modifyBulletin(b, new DoNothingOnCancel());
	}

	public boolean modifyBulletin(Bulletin b, CancelHandler cancelHandler)
	{
		modifyingBulletin = true;
		setEnabled(false);
		UiModifyBulletinDlg dlg = new UiModifyBulletinDlg(b, cancelHandler, this);
		currentActiveFrame = dlg;
		setVisible(false);
		return dlg.wasBulletinSaved();
	}

	public void doneModifyingBulletin()
	{
		modifyingBulletin = false;
		setEnabled(true);
		setVisible(true);
		currentActiveFrame = this;
	}

	public boolean isModifyingBulletin()
	{
		return modifyingBulletin;
	}

	public void doExportBulletins()
	{
		UniversalId[] uids = table.getSelectedBulletinUids();
		if(uids.length == 0)
		{
			notifyDlg(this, "ExportZeroBulletins");
			return;
		}

		new UiExportBulletinsDlg(this, uids);

	}

	public File getLastAttachmentLoadDirectory()
	{
		return lastAttachmentLoadDirectory;
	}

	public File getLastAttachmentSaveDirectory()
	{
		return lastAttachmentSaveDirectory;
	}

	public void setLastAttachmentLoadDirectory(File lastAttachmentLoadDirectory)
	{
		this.lastAttachmentLoadDirectory = lastAttachmentLoadDirectory;
	}

	public void setLastAttachmentSaveDirectory(File lastAttachmentSaveDirectory)
	{
		this.lastAttachmentSaveDirectory = lastAttachmentSaveDirectory;
	}

	static boolean isAnyBulletinSelected(UiMainWindow window)
	{
		return (window.table.getSelectedBulletinUids().length > 0);
	}

	static boolean isOnlyOneBulletinSelected(UiMainWindow window)
	{
		return (window.table.getSingleSelectedBulletin() != null);
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

	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			exitNormally();
		}
	}

	private class InactivityDetector implements AWTEventListener
	{
		public InactivityDetector()
		{
			java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(this,
					AWTEvent.KEY_EVENT_MASK |
					AWTEvent.MOUSE_EVENT_MASK |
					AWTEvent.MOUSE_MOTION_EVENT_MASK);
		}

		public long secondsSinceLastActivity()
		{
			return (now() - lastActivityAt) / 1000;
		}

		private void trackActivity()
		{
			lastActivityAt = now();
		}

		private long now()
		{
			return System.currentTimeMillis();
		}

		public void eventDispatched(AWTEvent event)
		{
			// a MOUSE_EXIT is automatically generated if
			// we hide the window, so always ignore them
			if(event.getID() == Event.MOUSE_EXIT)
				return;

			trackActivity();
		}

		long lastActivityAt = now();
	}

	class TickBackgroundUpload extends TimerTask
	{
		public TickBackgroundUpload()
		{
		}

		public void run()
		{
			if(inConfigServer)
				return;
			try
			{
				checkForNewsFromServer();
				try
				{
					uploadResult = app.backgroundUpload(statusBar.getBackgroundProgressMeter());
				}
				catch (MartusApp.DamagedBulletinException e)
				{
					ThreadedNotify damagedBulletin = new ThreadedNotify("DamagedBulletinMovedToDiscarded");
					SwingUtilities.invokeAndWait(damagedBulletin);
					folderContentsHaveChanged(getStore().getFolderOutbox());
					folderContentsHaveChanged(getStore().getFolderDraftOutbox());
					folderContentsHaveChanged(app.createOrFindFolder(getStore().getNameOfFolderDamaged()));
					folderTreeContentsHaveChanged();
				}
				if(uploadResult != null)
				{
					//System.out.println("UiMainWindow.Tick.run: " + uploadResult);
					folderContentsHaveChanged(getStore().getFolderSent());
					folderContentsHaveChanged(getStore().getFolderOutbox());
					folderContentsHaveChanged(getStore().getFolderDraftOutbox());
					folderContentsHaveChanged(getApp().createOrFindFolder(getStore().getNameOfFolderDamaged()));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		public void checkForNewsFromServer()
		{
			if(alreadyGotNews)
				return;
			Vector newsItems = app.getNewsFromServer();
			for (Iterator iter = newsItems.iterator(); iter.hasNext();)
			{
				String newsItem = (String) iter.next();
				ThreadedMessageDlg newsDlg = new ThreadedMessageDlg("ServerNews", newsItem);
				try
				{
					SwingUtilities.invokeAndWait(newsDlg);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			alreadyGotNews = true;
		}

		class ThreadedNotify implements Runnable
		{
			public ThreadedNotify(String tag)
			{
				notifyTag = tag;
			}

			public void run()
			{
				notifyDlg(UiMainWindow.this, notifyTag);
			}
			String notifyTag;
		}

		class ThreadedMessageDlg implements Runnable
		{
			public ThreadedMessageDlg(String tag, String message)
			{
				titleTag = tag;
				messageContents = message;
			}

			public void run()
			{
				messageDlg(UiMainWindow.this, titleTag, messageContents);
			}
			String titleTag;
			String messageContents;
		}
		boolean alreadyGotNews;
	}

	class TickTimeout extends TimerTask
	{
		public TickTimeout()
		{
		}

		public void run()
		{
			try
			{
				if(hasTimedOut())
				{
					System.out.println("Inactive");
					ThreadedSignin signin = new ThreadedSignin();
					SwingUtilities.invokeAndWait(signin);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		boolean hasTimedOut()
		{
			if(inactivityDetector.secondsSinceLastActivity() > TIMEOUT_SECONDS)
				return true;

			return false;
		}

		class ThreadedSignin implements Runnable
		{
			public void run()
			{
				currentActiveFrame.setState(ICONIFIED);
				if(!signIn(UiSigninDlg.TIMED_OUT))
					exitWithoutPrompting();
				currentActiveFrame.setState(NORMAL);
			}
		}
	}

	class UploadErrorChecker extends AbstractAction
	{
		public void actionPerformed(ActionEvent evt)
		{
			if(uploadResult == null)
				return;

			if(uploadResult.equals(NetworkInterfaceConstants.REJECTED) && !rejectedErrorShown)
			{
				notifyDlg(UiMainWindow.this, "uploadrejected");
				rejectedErrorShown = true;
			}
			if(uploadResult.equals(MartusApp.AUTHENTICATE_SERVER_FAILED) && !authenticationErrorShown)
			{
				notifyDlg(UiMainWindow.this, "AuthenticateServerFailed");
				authenticationErrorShown = true;
			}
		}
		boolean authenticationErrorShown;
		boolean rejectedErrorShown;
	}

	private MartusApp app;
	private CurrentUiState uiState;
	private UiBulletinPreview preview;
	private JSplitPane previewSplitter;
	private JSplitPane folderSplitter;
	private UiBulletinTablePane table;
	private UiFolderTreePane folders;
	private java.util.Timer uploader;
	private java.util.Timer timeoutChecker;
	private javax.swing.Timer errorChecker;
	private String uploadResult;
	private InactivityDetector inactivityDetector;
	private AbstractAction actionCreate;
	private AbstractAction actionEdit;
	private AbstractAction actionSearch;
	private AbstractAction actionPrint;
	private AbstractAction actionMenuPrint;
	private AbstractAction actionMenuModifyBulletin;
	private AbstractAction actionMenuSelectAllBulletins;
	private AbstractAction actionMenuCutBulletins;
	private AbstractAction actionMenuCopyBulletins;
	private AbstractAction actionMenuPasteBulletins;
	private AbstractAction actionMenuDiscardBulletins;
	private AbstractAction actionMenuRenameFolder;
	private AbstractAction actionMenuDeleteFolder;


	private UiStatusBar statusBar;

	private JFrame currentActiveFrame;
	private boolean inConfigServer;

	private static final int MAX_KEYPAIRFILE_SIZE = 32000;
	private static final int TIMEOUT_SECONDS = (10 * 60);
	private static final int BACKGROUND_UPLOAD_CHECK_MILLIS = 5*1000;
	private static final int BACKGROUND_TIMEOUT_CHECK_EVERY_X_MILLIS = 5*1000;
	private int clearStatusMessage;
	private File lastAttachmentLoadDirectory;
	private File lastAttachmentSaveDirectory;
	private boolean modifyingBulletin;
	private boolean mainWindowInitalizing;
}
