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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;

import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
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
import javax.swing.UIManager;
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
import org.martus.client.swingui.UiModifyBulletinDlg.DoNothingOnCancel;
import org.martus.client.swingui.UiModifyBulletinDlg.CancelHandler;
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
		updateIcon(this);

		initalizeUiState();
	}

	public void updateIcon(JFrame window)
	{
		URL imageURL = window.getClass().getResource("Martus.png");
		if(imageURL == null)
			return;
		ImageIcon imageicon = new ImageIcon(imageURL);
		if(imageicon != null)
			window.setIconImage(imageicon.getImage());
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
		String title = getApp().getWindowTitle("confirm" + baseTag);
		String cause = getApp().getFieldLabel("confirm" + baseTag + "cause");
		String effect = getApp().getFieldLabel("confirm" + baseTag + "effect");
		String question = getApp().getFieldLabel("confirmquestion");
		String[] contents = {cause, "", effect, "", question};
		return confirmDlg(parent, title, contents);
	}

	public boolean confirmDlg(JFrame parent, String title, String[] contents)
	{
		String yes = app.getButtonLabel("yes");
		String no = app.getButtonLabel("no");
		String[] buttons = {yes, no};

		UiNotifyDlg notify = new UiNotifyDlg(this, parent, title, contents, buttons);
		String result = notify.getResult();
		if(result == null)
			return false;
		return(result.equals(yes));
	}

	public void notifyDlg(JFrame parent, String baseTag)
	{
		notifyDlg(parent, baseTag, "notify" + baseTag);
	}

	public void notifyDlg(JFrame parent, String baseTag, String titleTag)
	{
		String title = app.getWindowTitle(titleTag);
		String cause = app.getFieldLabel("notify" + baseTag + "cause");
		String ok = app.getButtonLabel("ok");
		String[] contents = {cause};
		String[] buttons = {ok};

		new UiNotifyDlg(this, parent, title, contents, buttons);
	}

	public void messageDlg(JFrame parent, String baseTag, String message)
	{
		String title = app.getWindowTitle(baseTag);
		String cause = app.getFieldLabel("message" + baseTag + "cause");
		String ok = app.getButtonLabel("ok");
		String[] contents = {cause, "", message};
		String[] buttons = {ok};

		new UiNotifyDlg(this, parent, title, contents, buttons);
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
		UiStringInputDlg inputDlg = new UiStringInputDlg(this, baseTag, description, defaultText);
		inputDlg.show();
		return inputDlg.getResult();
	}

	public TransferableBulletinList getClipboardTransferableBulletin()
	{
		Transferable t = getTransferableFromClipboard();
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(t);
		return tb;
	}

	public File getClipboardTransferableFile()
	{
		Transferable t = getTransferableFromClipboard();
		File file = TransferableBulletinList.extractFileFrom(t);
		return file;
	}

	public Transferable getTransferableFromClipboard()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		return clipboard.getContents(this);
	}

	public ActionMenuModifyBulletin getActionMenuEdit()
	{
		return actionMenuModifyBulletin;
	}
	public ActionMenuCutBulletin getActionMenuCut()
	{
		return actionMenuCutBulletin;
	}
	public ActionMenuCopyBulletin getActionMenuCopy()
	{
		return actionMenuCopyBulletin;
	}
	public ActionMenuPasteBulletin getActionMenuPaste()
	{
		return actionMenuPasteBulletin;
	}

	public ActionMenuDiscardBulletin getActionMenuDiscard()
	{
		return actionMenuDiscardBulletin;
	}

	//ClipboardOwner Interface
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(contents);
		if(tb != null)
			tb.dispose();
	}


	private void createActions()
	{
		actionCreate = new ActionCreate();
		actionEdit = new ActionModify();
		actionSearch = new ActionSearch();
		actionPrint = new ActionPrint();
	}

	public boolean isMacintosh()
	{
		return (UIManager.getSystemLookAndFeelClassName().indexOf("MacLookAndFeel") >= 0);
	}

	public boolean isMSWindows()
	{
		return (UIManager.getSystemLookAndFeelClassName().indexOf("WindowsLookAndFeel") >= 0);
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

	public void centerDlg(JDialog dlg)
	{
		dlg.pack();
		Dimension size = dlg.getSize();
		Rectangle screen = new Rectangle(new Point(0, 0), getToolkit().getScreenSize());
		dlg.setLocation(MartusApp.center(size, screen));
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

	private JComponent createMenubar()
	{
		JMenu file = new JMenu(app.getMenuLabel("file"));
		actionMenuPrint = new ActionMenuPrintBulletin();
		file.addMenuListener(actionMenuPrint);

		file.add(new ActionMenuCreateNewBulletin());
		file.add(actionMenuPrint);
		file.addSeparator();
		file.add(new ActionMenuBackupMyKeyPair());
		file.add(new ActionMenuExportMyPublicKey());
		file.addSeparator();
//TODO: Uncomment when the export feature is completed
//		file.add(new ActionMenuExportBulletins());
//		file.addSeparator();
		file.add(new ActionMenuImportHeadquarterPublicKey());
		file.add(new ActionMenuRemoveExistingHeadquaterPublicKey());
		file.addSeparator();
		file.add(new ActionMenuExit());


		JMenu edit = new JMenu(app.getMenuLabel("edit"));
		actionMenuModifyBulletin = new ActionMenuModifyBulletin();
		actionMenuCutBulletin = new ActionMenuCutBulletin();
		actionMenuCopyBulletin = new ActionMenuCopyBulletin();
		actionMenuPasteBulletin = new ActionMenuPasteBulletin();
		actionMenuDiscardBulletin = new ActionMenuDiscardBulletin();
		EditMenuListener menuListener = new EditMenuListener();
		edit.addMenuListener(menuListener);
		menuListener.initalize();

		edit.add(new ActionMenuSearch());
		edit.addSeparator();
		edit.add(actionMenuModifyBulletin);
		edit.addSeparator();
		edit.add(actionMenuCutBulletin);
		edit.add(actionMenuCopyBulletin);
		edit.add(actionMenuPasteBulletin);
		edit.addSeparator();
		edit.add(actionMenuDiscardBulletin);

		JMenu folders = new JMenu(app.getMenuLabel("folders"));
		actionMenuRenameFolder = new ActionMenuRenameFolder();
		actionMenuDeleteFolder = new ActionMenuDeleteFolder();
		FoldersMenuListener menuFolderListener = new FoldersMenuListener();
		folders.addMenuListener(menuFolderListener);
		menuFolderListener.initalize();

		folders.add(new ActionMenuCreateFolder());
		folders.add(actionMenuRenameFolder);
		folders.add(actionMenuDeleteFolder);


		JMenu server = new JMenu(app.getMenuLabel("server"));
		server.add(new ActionMenuRetrieveMySealedBulletins());
		server.add(new ActionMenuRetrieveMyDraftBulletins());
		server.add(new ActionMenuDeleteMyServerDraftBulletins());
		server.addSeparator();
		server.add(new ActionMenuRetrieveHQSealedBulletins());
		server.add(new ActionMenuRetrieveHQDraftBulletins());
		server.addSeparator();
		server.add(new ActionMenuSelectServer());


		JMenu options = new JMenu(app.getMenuLabel("options"));
		options.add(new ActionMenuPreferences());
		options.add(new ActionMenuContactInfo());
		options.add(new ActionMenuDefaultDetailsFieldContent());
		options.add(new ActionMenuChangeUserNamePassword());


		JMenu help = new JMenu(app.getMenuLabel("help"));
		help.add(new ActionMenuHelp());
		help.add(new ActionMenuAbout());
		help.addSeparator();
		help.add(new ActionMenuAccountDetails());

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


	private void doModifyBulletin()
	{
		table.doModifyBulletin();
	}

	private void doCutBulletins()
	{
		table.doCutBulletins();
	}

	private void doCopyBulletins()
	{
		table.doCopyBulletins();
	}

	private void doPasteBulletin()
	{
		table.doPasteBulletin();
	}

	private void doDiscardBulletins()
	{
		table.doDiscardBulletins();
	}

	private void doSearch()
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

	private void aboutMartus()
	{
		new UiAboutDlg(this);
	}

	private void showAccountInfo()
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

		new UiNotifyDlg(this, this, title, contents, buttons);
	}

	private void displayHelpMessage()
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

	class PrintPageFormat extends PageFormat
	{

		private void setFromAttributes(HashPrintRequestAttributeSet attributes)
		{
			boolean otherMediaSet = false;
			boolean paperSizeSet = false;
			final int FRACTIONS_INCH = 72;
			Paper paper = new Paper();
			Attribute all[] = attributes.toArray();
			for(int i=0; i < all.length; ++i)
			{
				if(all[i].getCategory().equals(MediaPrintableArea.class))
				{
					MediaPrintableArea area = (MediaPrintableArea)(all[i]);
					paper.setImageableArea(	area.getX(MediaPrintableArea.INCH) * FRACTIONS_INCH,
											area.getY(MediaPrintableArea.INCH) * FRACTIONS_INCH,
											area.getWidth(MediaPrintableArea.INCH) * FRACTIONS_INCH,
											area.getHeight(MediaPrintableArea.INCH) * FRACTIONS_INCH);
				}
				if(all[i].getCategory().equals(Media.class))
				{
					try
					{
						MediaSizeName mediaSizeName = (MediaSizeName)(all[i]);
						MediaSize size = MediaSize.getMediaSizeForName(mediaSizeName);
						paper.setSize(	size.getX(MediaSize.INCH) * FRACTIONS_INCH,
										size.getY(MediaSize.INCH) * FRACTIONS_INCH);
						paperSizeSet = true;
					} catch (RuntimeException e)
					{
						otherMediaSet = true;
						//Not a MediaSizeName
					}
				}
				if(all[i].getCategory().equals(OrientationRequested.class))
				{
					OrientationRequested orientation = (OrientationRequested)(all[i]);
					if(orientation.equals(OrientationRequested.LANDSCAPE))
						setOrientation(LANDSCAPE);
					if(orientation.equals(OrientationRequested.PORTRAIT))
						setOrientation(PORTRAIT);
					if(orientation.equals(OrientationRequested.REVERSE_LANDSCAPE))
						setOrientation(REVERSE_LANDSCAPE);
				}
			}
			setPaper(paper);
			if(otherMediaSet && !paperSizeSet)
				mustWarnUser = true;
			else
				mustWarnUser = false;
		}
		private boolean mustWarnUser;
	}

	private void doPrint()
	{
		boolean printCancelled = false;
		Bulletin currentBulletin = table.getSingleSelectedBulletin();
		if(currentBulletin == null)
			return;

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
				requestFocus(true);
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
		requestFocus(true);
	}

	private void doLocalize()
	{
		saveState();
		new UiLocalizeDlg(this);
		initializeViews();
		restoreState();
		show();
	}

	private boolean doContactInfo()
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


	private void doConfigureServer()
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

			new UiNotifyDlg(this, currentActiveFrame, title, contents, buttons);
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


	private void doChangeUserNamePassword()
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

	private void doRetrieveMySealedBulletins()
	{
		String dlgTitleTag = "RetrieveMySealedBulletins";
		String summariesProgressTag = "RetrieveMySealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveMySealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedSealed();

		RetrieveTableModel model = new RetrieveMyTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doRetrieveMyDraftBulletins()
	{
		String dlgTitleTag = "RetrieveMyDraftBulletins";
		String summariesProgressTag = "RetrieveMyDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveMyDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedDraft();

		RetrieveTableModel model = new RetrieveMyDraftsTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doRetrieveHQBulletins()
	{
		String dlgTitleTag = "RetrieveHQSealedBulletins";
		String summariesProgressTag = "RetrieveHQSealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQSealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeSealed();

		RetrieveTableModel model = new RetrieveHQTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doRetrieveHQDraftsBulletins()
	{
		String dlgTitleTag = "RetrieveHQDraftBulletins";
		String summariesProgressTag = "RetrieveHQDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeDraft();

		RetrieveTableModel model = new RetrieveHQDraftsTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doDeleteServerDraftBulletins()
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

	private void doExportMyPublicKey()
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
			new UiNotifyDlg(this, currentActiveFrame, title, contents, buttons);
		}
		catch(Exception e)
		{
			System.out.println("UiMainWindow.doExportMyPublicKey :" + e);
		}
	}

	private void doBackupKeyPair()
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

	private void doImportHQPublicKey()
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

	private void doClearPublicAccountInfo()
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
			String normalizedPublicCode = removeNonDigits(userEnteredPublicCode);

			if(publicCode.equals(normalizedPublicCode))
				return true;

			//System.out.println("Entered:     " + userEnteredPublicCode);
			//System.out.println("Normalized:   " + normalizedPublicCode);
			notifyDlg(this, errorBaseTag);
		}
	}

	public String removeNonDigits(String userEnteredPublicCode)
	{
		String normalizedPublicCode = "";
		for (int i=0 ; i < userEnteredPublicCode.length(); ++i)
		{
			if ("0123456789".indexOf(userEnteredPublicCode.substring(i, i+1)) >= 0)
				normalizedPublicCode += userEnteredPublicCode.substring(i, i+1);
		}
		return normalizedPublicCode;
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
		if(isValidScreenPosition(screenSize, appDimension, appPosition))
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
			maximizeWindow(this);
		}
	}

	public void maximizeWindow(JFrame window)
	{
		window.setVisible(true);//required for setting maximized
		window.setExtendedState(MAXIMIZED_BOTH);
	}

	boolean isValidScreenPosition(Dimension screenSize, Dimension objectSize, Point objectPosition)
	{
		int height = objectSize.height;
		if(height == 0 )
			return false;
		if(objectPosition.x > screenSize.width - 100)
			return false;
		if(objectPosition.y > screenSize.height - 100)
			return false;
		if(objectPosition.x < -100 || objectPosition.y < -100)
			return false;
		return true;
	}

	private boolean signIn(int mode)
	{
		int seconds = 0;
		UiModelessBusyDlg busyDlg = null;
		while(true)
		{
			Delay delay = new Delay(seconds);
			delay.start();
			waitForThreadToTerminate(delay);
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

	public void waitForThreadToTerminate(Delay worker)
	{
		try
		{
			worker.join();
		}
		catch (InterruptedException e)
		{
			// We don't care if this gets interrupted
		}
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

	private void exitNormally()
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

	class Delay extends Thread
	{
		public Delay(int sec)
		{
			timeInMillis = sec * 1000;
		}

		public void run()
		{
			try
			{
				sleep(timeInMillis);
			}
			catch(InterruptedException e)
			{
				;
			}
		}

		private int timeInMillis;
	}

	static private boolean isAnyBulletinSelected(UiMainWindow window)
	{
		return (window.table.getSelectedBulletinUids().length > 0);
	}

	static private boolean isOnlyOneBulletinSelected(UiMainWindow window)
	{
		return (window.table.getSingleSelectedBulletin() != null);
	}

	class ActionCreate extends AbstractAction
	{
		public ActionCreate()
		{
			super(app.getButtonLabel("create"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			createBulletin();
		}
	}

	class ActionModify extends AbstractAction
	{
		public ActionModify()
		{
			super(app.getButtonLabel("modify"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doModifyBulletin();
		}
	}

	class ActionSearch extends AbstractAction
	{
		public ActionSearch()
		{
			super(app.getButtonLabel("search"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doSearch();
		}
	}

	class ActionPrint extends AbstractAction
	{
		public ActionPrint()
		{
			super(app.getButtonLabel("print"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doPrint();
		}
	}

	class ActionMenuExit extends AbstractAction
	{
		public ActionMenuExit()
		{
			super(app.getMenuLabel("exit"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			exitNormally();
		}
	}

	class ActionMenuCreateFolder extends AbstractAction
	{
		public ActionMenuCreateFolder()
		{
			super(app.getMenuLabel("CreateNewFolder"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			folders.createNewFolder();
		}
	}

	class ActionMenuRenameFolder extends AbstractAction
	{
		public ActionMenuRenameFolder()
		{
			super(app.getMenuLabel("RenameFolder"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			folders.renameCurrentFolder();
		}

		public boolean isEnabled()
		{
			BulletinFolder folder = folders.getSelectedFolder();
			if(folder != null && folder.canRename())
				return true;
			return false;
		}
	}

	class ActionMenuDeleteFolder extends AbstractAction
	{
		public ActionMenuDeleteFolder()
		{
			super(app.getMenuLabel("DeleteFolder"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			folders.deleteCurrentFolderIfPossible();
		}

		public boolean isEnabled()
		{
			BulletinFolder folder = folders.getSelectedFolder();
			if(folder != null && folder.canRename())
				return true;
			return false;
		}
	}

	class ActionMenuCreateNewBulletin extends AbstractAction
	{
		public ActionMenuCreateNewBulletin()
		{
			super(app.getMenuLabel("CreateNewBulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			createBulletin();
		}
	}

	class ActionMenuExportBulletins extends AbstractAction
	{
		public ActionMenuExportBulletins()
		{
			super(app.getMenuLabel("ExportBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doExportBulletins();
		}
	}

	class ActionMenuPrintBulletin extends AbstractAction implements MenuListener
	{
		public ActionMenuPrintBulletin()
		{
			super(app.getMenuLabel("printBulletin"), null);
			//Java Bug, menu items need to be disabled before correct behavior occures.
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doPrint();
		}

		public boolean isEnabled()
		{
			return isOnlyOneBulletinSelected(UiMainWindow.this);
		}

		public void menuSelected(MenuEvent e)
		{
			actionMenuPrint.setEnabled(actionMenuPrint.isEnabled());
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	class ActionMenuAbout extends AbstractAction
	{
		public ActionMenuAbout()
		{
			super(app.getMenuLabel("about"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			aboutMartus();
		}
	}

	class ActionMenuAccountDetails extends AbstractAction
	{
		public ActionMenuAccountDetails()
		{
			super(app.getMenuLabel("ViewMyAccountDetails"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			showAccountInfo();
		}
	}

	class ActionMenuHelp extends AbstractAction
	{
		public ActionMenuHelp()
		{
			super(app.getMenuLabel("helpMessage"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			displayHelpMessage();
		}
	}

	class ActionMenuModifyBulletin extends AbstractAction
	{
		public ActionMenuModifyBulletin()
		{
			super(app.getMenuLabel("modifyBulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doModifyBulletin();
		}

		public boolean isEnabled()
		{
			return isOnlyOneBulletinSelected(UiMainWindow.this);
		}
	}

	class ActionMenuCutBulletin extends AbstractAction
	{
		public ActionMenuCutBulletin()
		{
			super(app.getMenuLabel("CutBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doCutBulletins();
		}

		public boolean isEnabled()
		{
			if(isDiscardedFolderSelected())
				return false;
			return isAnyBulletinSelected(UiMainWindow.this);
		}
	}

	class ActionMenuCopyBulletin extends AbstractAction
	{
		public ActionMenuCopyBulletin()
		{
			super(app.getMenuLabel("CopyBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doCopyBulletins();
		}

		public boolean isEnabled()
		{
			return isAnyBulletinSelected(UiMainWindow.this);
		}
	}

	class ActionMenuDiscardBulletin extends AbstractAction
	{
		public ActionMenuDiscardBulletin()
		{
			super(app.getMenuLabel("DiscardBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doDiscardBulletins();
		}

		public boolean isEnabled()
		{
			updateName();
			return isAnyBulletinSelected(UiMainWindow.this);
		}

		public void updateName()
		{
			if(isDiscardedFolderSelected())
				actionMenuDiscardBulletin.putValue(ActionMenuDiscardBulletin.NAME, getApp().getMenuLabel("DeleteBulletins"));
			else
				actionMenuDiscardBulletin.putValue(ActionMenuDiscardBulletin.NAME, getApp().getMenuLabel("DiscardBulletins"));
		}
	}

	class ActionMenuPasteBulletin extends AbstractAction
	{
		public ActionMenuPasteBulletin()
		{
			super(app.getMenuLabel("PasteBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doPasteBulletin();
		}
		public boolean isEnabled()
		{
			boolean enable = (getClipboardTransferableBulletin() != null);
			if(!enable)
				enable = (getClipboardTransferableFile() != null);
			 return enable;
		}
	}

	class EditMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuModifyBulletin.setEnabled(actionMenuModifyBulletin.isEnabled());
			actionMenuCutBulletin.setEnabled(actionMenuCutBulletin.isEnabled());
			actionMenuCopyBulletin.setEnabled(actionMenuCopyBulletin.isEnabled());
			actionMenuPasteBulletin.setEnabled(actionMenuPasteBulletin.isEnabled());
			actionMenuDiscardBulletin.setEnabled(actionMenuDiscardBulletin.isEnabled());
		}

		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuModifyBulletin.setEnabled(false);
			actionMenuCutBulletin.setEnabled(false);
			actionMenuCopyBulletin.setEnabled(false);
			actionMenuPasteBulletin.setEnabled(false);
			actionMenuDiscardBulletin.setEnabled(false);
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

	class ActionMenuSearch extends AbstractAction
	{
		public ActionMenuSearch()
		{
			super(app.getMenuLabel("search"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doSearch();
		}
	}

	class ActionMenuPreferences extends AbstractAction
	{
		public ActionMenuPreferences()
		{
			super(app.getMenuLabel("Preferences"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doLocalize();
		}
	}

	class ActionMenuContactInfo extends AbstractAction
	{
		public ActionMenuContactInfo()
		{
			super(app.getMenuLabel("contactinfo"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doContactInfo();
		}
	}

	class ActionMenuSelectServer extends AbstractAction
	{
		public ActionMenuSelectServer()
		{
			super(app.getMenuLabel("SelectServer"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doConfigureServer();
		}
	}

	class ActionMenuChangeUserNamePassword extends AbstractAction
	{
		public ActionMenuChangeUserNamePassword()
		{
			super(app.getMenuLabel("changeUserNamePassword"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doChangeUserNamePassword();
		}
	}

	class ActionMenuDefaultDetailsFieldContent extends AbstractAction
	{
		public ActionMenuDefaultDetailsFieldContent()
		{
			super(app.getMenuLabel("DefaultDetailsFieldContent"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			updateBulletinDetails(null);
		}
	}

	class ActionMenuRetrieveMySealedBulletins extends AbstractAction
	{
		public ActionMenuRetrieveMySealedBulletins()
		{
			super(app.getMenuLabel("RetrieveMySealedBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveMySealedBulletins();
		}
	}

	class ActionMenuRetrieveMyDraftBulletins extends AbstractAction
	{
		public ActionMenuRetrieveMyDraftBulletins()
		{
			super(app.getMenuLabel("RetrieveMyDraftsBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveMyDraftBulletins();
		}
	}
	class ActionMenuDeleteMyServerDraftBulletins extends AbstractAction
	{
		public ActionMenuDeleteMyServerDraftBulletins()
		{
			super(app.getMenuLabel("DeleteMyServerDrafts"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doDeleteServerDraftBulletins();
		}
	}

	class ActionMenuRetrieveHQSealedBulletins extends AbstractAction
	{
		public ActionMenuRetrieveHQSealedBulletins()
		{
			super(app.getMenuLabel("RetrieveHQSealedBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveHQBulletins();
		}
	}

	class ActionMenuRetrieveHQDraftBulletins extends AbstractAction
	{
		public ActionMenuRetrieveHQDraftBulletins()
		{
			super(app.getMenuLabel("RetrieveHQDraftBulletins"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveHQDraftsBulletins();
		}
	}

	class ActionMenuBackupMyKeyPair extends AbstractAction
	{
		public ActionMenuBackupMyKeyPair()
		{
			super(app.getMenuLabel("BackupMyKeyPair"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doBackupKeyPair();
		}
	}

	class ActionMenuExportMyPublicKey extends AbstractAction
	{
		public ActionMenuExportMyPublicKey()
		{
			super(app.getMenuLabel("ExportMyPublicKey"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doExportMyPublicKey();
		}
	}

	class ActionMenuImportHeadquarterPublicKey extends AbstractAction
	{
		public ActionMenuImportHeadquarterPublicKey()
		{
			super(app.getMenuLabel("ImportHQPublicKey"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doImportHQPublicKey();
		}
	}

	class ActionMenuRemoveExistingHeadquaterPublicKey extends AbstractAction
	{
		public ActionMenuRemoveExistingHeadquaterPublicKey()
		{
			super(app.getMenuLabel("RemoveExistingHQPublicKey"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doClearPublicAccountInfo();
		}
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
	private ActionCreate actionCreate;
	private ActionModify actionEdit;
	private ActionSearch actionSearch;
	private ActionPrint actionPrint;
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
	private ActionMenuPrintBulletin actionMenuPrint;
	private ActionMenuModifyBulletin actionMenuModifyBulletin;
	private ActionMenuCutBulletin actionMenuCutBulletin;
	private ActionMenuCopyBulletin actionMenuCopyBulletin;
	private ActionMenuPasteBulletin actionMenuPasteBulletin;
	private ActionMenuDiscardBulletin actionMenuDiscardBulletin;
	private ActionMenuRenameFolder actionMenuRenameFolder;
	private ActionMenuDeleteFolder actionMenuDeleteFolder;


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
