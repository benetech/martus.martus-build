package org.martus.client;

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
import java.sql.Timestamp;
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

import org.martus.client.MartusApp.ServerErrorException;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.Packet;
import org.martus.common.Base64.InvalidBase64Exception;

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
			String title = "Error Starting Martus";
			String cause = "Unable to start Martus: " + e.getMessage();
			String ok = "OK";
			String[] buttons = { ok };
			JOptionPane pane = new JOptionPane(cause, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
									null, buttons);
			JDialog dialog = pane.createDialog(null, title);
			dialog.show();
			System.exit(1);
		}
		ImageIcon imageicon = new ImageIcon(this.getClass().getResource("Martus.jpg"));
		if(imageicon != null)
			setIconImage(imageicon.getImage());
		
		initalizeUiState();
	}

	public boolean run()
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		Timestamp expire = Timestamp.valueOf("2003-01-15 1:00:00.000000000");
		if(stamp.after(expire))
			notifyDlg(this, "BetaExpired");

		if(app.doesAccountExist())
		{
			if(!signIn(UiSigninDlg.INITIAL))
				return false;
		}
		else
		{
			if(!createAccount())
				return false;
		}

		try
		{
			app.loadConfigInfo();
		}
		catch (MartusApp.LoadConfigInfoException e)
		{
			notifyDlg(this, "corruptconfiginfo");
		}

		ConfigInfo info = app.getConfigInfo();
		if(!info.hasContactInfo())
			doContactInfo();
		else if(info.promptUserRequestSendToServer())
		{
			requestToUpdateContactInfoOnServerAndSaveInfo();
			info.clearPromptUserRequestSendToServer();
		}
		

		UiModelessBusyDlg waitingForBulletinsToLoad = new UiModelessBusyDlg(app.getFieldLabel("waitingForBulletinsToLoad"));
		int quarantineCount = app.quarantineUnreadableBulletins();
		app.loadFolders();
		int orphanCount = app.repairOrphans();

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowEventHandler());

		initializeViews();
		restoreState();
		waitingForBulletinsToLoad.endDialog();

		if(quarantineCount > 0)
			notifyDlg(this, "FoundDamagedBulletins");
		
		if(orphanCount > 0)
			notifyDlg(this, "FoundOrphans");
		
		show();

		inactivityDetector = new InactivityDetector();

		uploader = new java.util.Timer(true);
		uploader.schedule(new TickBackgroundUpload(), 0, BACKGROUND_UPLOAD_CHECK_SECONDS);

		timeoutChecker = new java.util.Timer(true);
		timeoutChecker.schedule(new TickTimeout(), 0, BACKGROUND_TIMEOUT_CHECK_EVERY_X_SECONDS);

		errorChecker = new javax.swing.Timer(10*1000, new UploadErrorChecker());
		errorChecker.start();
		return true;
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
		preview.setCurrentBulletin(b);
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		table.bulletinContentsHaveChanged(b);
		preview.bulletinContentsHaveChanged(b);
	}

	public void folderSelectionHasChanged(BulletinFolder f)
	{
		Cursor originalCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		table.setFolder(f);
		setCursor(originalCursor);
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

	public void selectFirstBulletin()
	{
		table.selectFirstBulletin();
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
		String title = app.getWindowTitle("notify" + baseTag);
		String cause = app.getFieldLabel("notify" + baseTag + "cause");
		String ok = app.getButtonLabel("ok");
		String[] contents = {cause};
		String[] buttons = {ok};
		
		UiNotifyDlg notify = new UiNotifyDlg(this, parent, title, contents, buttons);
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

	public ActionMenuPaste getActionMenuPaste()
	{
		return actionMenuPaste;
	}

	public ActionMenuDiscard getActionMenuDiscard()
	{
		return actionMenuDiscard;
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
		actionEdit = new ActionEdit();
		actionSearch = new ActionSearch();
		actionPrint = new ActionPrint();
	}

	public boolean isMacintosh()
	{
		return (UIManager.getSystemLookAndFeelClassName().indexOf("MacLookAndFeel") >= 0);
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
		catch(IOException e)
		{
			notifyDlg(null, "ErrorSavingState");
		}
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
		uiState.load(app.getUiStateFile());
		uiState.setCurrentLanguage(app.getCurrentLanguage());
		uiState.setCurrentDateFormat(app.getCurrentDateFormatCode());
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
		JMenu file = new JMenu(app.getButtonLabel("file"));
		file.add(new ActionMenuCreateFolder());
		file.add(new ActionMenuCreateBulletin());
		file.add(new ActionMenuPrintBulletin());
		file.addSeparator();
		file.add(new ActionMenuExit());

		JMenu edit = new JMenu(app.getButtonLabel("edit"));
		actionMenuEdit = new ActionMenuEdit();
		edit.add(actionMenuEdit);

		edit.addSeparator();
		actionMenuCut = new ActionMenuCut();
		edit.add(actionMenuCut);
		actionMenuCopy = new ActionMenuCopy();
		edit.add(actionMenuCopy);
		actionMenuPaste = new ActionMenuPaste();
		edit.add(actionMenuPaste);

		edit.addSeparator();
		actionMenuDiscard = new ActionMenuDiscard();
		edit.add(actionMenuDiscard);

		EditMenuListener menuListener = new EditMenuListener();
		edit.addMenuListener(menuListener);
		menuListener.initalize();

		edit.add(new ActionMenuSearch());

		JMenu tools = new JMenu(app.getButtonLabel("tools"));
		tools.add(new ActionMenuRetrieve());
		tools.add(new ActionMenuRetrieveDrafts());
		tools.addSeparator();
		tools.add(new ActionMenuRetrieveHQ());
		tools.add(new ActionMenuRetrieveHQDrafts());
		tools.addSeparator();
		tools.add(new ActionMenuDeleteServerDrafts());
		tools.addSeparator();
		tools.add(new ActionMenuExportPublicInfo());
		tools.add(new ActionMenuImportPublicInfo());
		tools.add(new ActionMenuClearPublicInfo());

		JMenu options = new JMenu(app.getButtonLabel("options"));
		options.add(new ActionMenuLanguage());
		options.add(new ActionMenuDateFormat());
		options.add(new ActionMenuContactInfo());
		options.add(new ActionMenuServerInfo());
		options.add(new ActionMenuChangeUserNamePassword());
		options.add(new ActionMenuBackupKeyPair());		
		options.add(new ActionMenuBulletinDetails());
		

		JMenu help = new JMenu(app.getButtonLabel("help"));
		help.add(new ActionMenuAbout());
		help.add(new ActionMenuAccount());
		help.add(new ActionMenuHelp());

		JMenuBar menubar = new JMenuBar();
		menubar.add(file);
		menubar.add(edit);
		menubar.add(tools);
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

	
	private void doEditBulletin()
	{
		table.doEditBulletin();
	}
	
	private void doCutBulletin()
	{
		table.doCutBulletin();
	}

	private void doCopyBulletin()
	{
		table.doCopyBulletin();
	}

	private void doPasteBulletin()
	{
		table.doPasteBulletin();
	}

	private void doDiscardBulletin()
	{
		table.doDiscardBulletin();
	}

	private void doSearch()
	{
		UiSearchDlg searchDlg = new UiSearchDlg(this);
		if(!searchDlg.getResults())
			return;
		Cursor originalCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		app.search(searchDlg.getSearchString(), searchDlg.getStartDate(), searchDlg.getEndDate());
		BulletinStore store = getStore();
		BulletinFolder searchFolder = store.findFolder(store.getSearchFolderName());
		folders.folderContentsHaveChanged(searchFolder);
		int bulletinsFound = searchFolder.getBulletinCount();
		setCursor(originalCursor);
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
		UiAboutDlg about = new UiAboutDlg(this);
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
		
		UiNotifyDlg notify = new UiNotifyDlg(this, this, title, contents, buttons);
	}
	
	private void displayHelpMessage()
	{

		InputStream helpStream = null;
		String helpFileShortName = app.getHelpFilename();
		File file = new File(MartusApp.getTranslationsDirectory(), helpFileShortName);
		try
		{
			if(file.exists())
				helpStream = new FileInputStream(file);
			else
				helpStream = getClass().getResourceAsStream(helpFileShortName);
			if(helpStream == null)
				helpStream = getClass().getResourceAsStream(app.getEnglishHelpFilename());
			UiDisplayFileDlg helpDlg = new UiDisplayFileDlg(this, "Help", helpStream);
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
					paper.setImageableArea(	area.getX(area.INCH) * FRACTIONS_INCH,
											area.getY(area.INCH) * FRACTIONS_INCH,
											area.getWidth(area.INCH) * FRACTIONS_INCH,
											area.getHeight(area.INCH) * FRACTIONS_INCH);
				}
				if(all[i].getCategory().equals(Media.class))
				{
					try 
					{
						MediaSizeName mediaSizeName = (MediaSizeName)(all[i]);
						MediaSize size = MediaSize.getMediaSizeForName(mediaSizeName);
						paper.setSize(	size.getX(size.INCH) * FRACTIONS_INCH,
										size.getY(size.INCH) * FRACTIONS_INCH);
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
					if(orientation.equals(orientation.LANDSCAPE))
						setOrientation(LANDSCAPE);
					if(orientation.equals(orientation.PORTRAIT))
						setOrientation(PORTRAIT);
					if(orientation.equals(orientation.REVERSE_LANDSCAPE))
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
		//System.out.println("Print");
		if(table.getSingleSelectedBulletin() == null)
			return;
		
		preview.startPrintMode();
		PrintPageFormat format = new PrintPageFormat();
		PrinterJob job = PrinterJob.getPrinterJob();
		JComponent view = preview.getView();
		JComponentVista vista = new JComponentVista(view, format);
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
			}
		}
		preview.endPrintMode();
		requestFocus(true);
	}


	private void doLocalize()
	{
		saveState();
		UiLocalizeDlg dlg = new UiLocalizeDlg(this);
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
			
			UiNotifyDlg notify = new UiNotifyDlg(this, currentActiveFrame, title, contents, buttons);
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
			ExitImmediately();
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
	
	private void doChangeBulletinDetails()
	{
		ConfigInfo info = app.getConfigInfo();
		UiTemplateDlg templateDlg = new UiTemplateDlg(this, info);
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
		String dlgTitleTag = "retrieve";
		String summariesProgressTag = "RetrieveMySealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveMySealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedSealed();

		RetrieveTableModel model = new RetrieveMyTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doRetrieveMyDraftBulletins()
	{
		String dlgTitleTag = "RetrieveDrafts";
		String summariesProgressTag = "RetrieveMyDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveMyDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedDraft();

		RetrieveTableModel model = new RetrieveMyDraftsTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doRetrieveHQBulletins()
	{
		String dlgTitleTag = "retrieveHQ";
		String summariesProgressTag = "RetrieveHQSealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQSealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeSealed();

		RetrieveTableModel model = new RetrieveHQTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doRetrieveHQDraftsBulletins()
	{
		String dlgTitleTag = "retrieveHQDrafts";
		String summariesProgressTag = "RetrieveHQDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeDraft();

		RetrieveTableModel model = new RetrieveHQDraftsTableModel(app);
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	private void doDeleteServerDraftBulletins()
	{
		String dlgTitleTag = "DeleteServerDrafts";
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
					notifyDlg(this, "retrievefailed");
				else
					notifyDlg(this, "retrieveworked");
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
		String deleteProgressTag = "DeleteServerDraftsProgress";

		try 
		{
			Vector uidList = displaySummariesDlg(model, dlgTitleTag, topMessageTag, okButtonTag, noneSelectedTag, summariesProgressTag);
			if(uidList == null)
				return;

			Cursor originalCursor = getCursor();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
				setCursor(originalCursor);
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
			notifyDlg(this, "retrievenoserver");
			return null;
		}
		UiProgressRetrieveSummariesDlg progressDlg = new UiProgressRetrieveSummariesDlg(this, summariesProgressTag);
		model.initialize(progressDlg);
		if(progressDlg.shouldExit())
			return null;
		UiServerSummariesDlg summariesDlg = new UiServerSummariesDlg(this, model, dlgTitleTag, topMessageTag, okButtonTag);

		// the following is required (for unknown reasons)
		// to get the window to redraw after the dialog
		// is closed. Yuck! kbs.
		repaint();
		
		if(!summariesDlg.getResult())
			return null;
		
		Vector uidList = summariesDlg.getUniversalIdList();
		if( uidList.size() == 0)
		{
			notifyDlg(this, noneSelectedTag);
			return null;
		}

		return uidList;
	}

	private void doExportPublicAccountInfo()
	{
		try 
		{
			String fileName = getStringInput("exportPublicInfo", "NameOfExportedFile", "");
			if(fileName == null)
				return;
			File export = app.getPublicInfoFile(fileName);
			
			//TODO check file exists and ask to over write.
			app.exportPublicInfo(export);
			String title = getApp().getWindowTitle("notifyExportPublicInfo");
			String msg = getApp().getFieldLabel("notifyExportPublicInfocause");
			String ok = app.getButtonLabel("ok");
			String[] contents = {msg, export.getCanonicalPath()};
			String[] buttons = {ok};
			UiNotifyDlg notify = new UiNotifyDlg(this, currentActiveFrame, title, contents, buttons);
		} 
		catch(Exception e) 
		{
			System.out.println("UiMainWindow.doExportPublicAccountInfo :" + e);
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
		chooser.setFileSelectionMode(chooser.FILES_AND_DIRECTORIES);
		chooser.setSelectedFile(new File(app.KEYPAIR_FILENAME));
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
		
	private void doImportPublicAccountInfo()
	{
		if(!reSignIn())
			return;
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new PublicInfoFileFilter());

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

			System.out.println("Entered:     " + userEnteredPublicCode);
			System.out.println("Normalized:   " + normalizedPublicCode);
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
		int width = objectSize.width;
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
		//TODO remove this for final release
		notifyDlg(this, "WarningBetaCopy");
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

	private void ExitNormally()
	{
		if(doUploadReminderOnExit())
			return;
		saveState();
		ExitImmediately();
	}
	
	private void ExitImmediately()
	{
		System.exit(0);
	}

	public void createBulletin() 
	{
		Bulletin b = app.createBulletin();
		editBulletin(b);
	}

	public void editBulletin(Bulletin b) 
	{
		setEnabled(false);
		currentActiveFrame = new UiEditBulletinDlg(b, this);
	}
	
	public void stopEditing()
	{
		setEnabled(true);
		currentActiveFrame = this;
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

	class ActionEdit extends AbstractAction
	{
		public ActionEdit()
		{
			super(app.getButtonLabel("edit"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doEditBulletin();
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
			ExitNormally();
		}
	}
	
	class ActionMenuCreateFolder extends AbstractAction
	{
		public ActionMenuCreateFolder()
		{
			super(app.getMenuLabel("newFolder"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			folders.createNewFolder();
		}
	}
	
	class ActionMenuCreateBulletin extends AbstractAction
	{
		public ActionMenuCreateBulletin()
		{
			super(app.getMenuLabel("createBulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			createBulletin();
		}
	}

	class ActionMenuPrintBulletin extends AbstractAction
	{
		public ActionMenuPrintBulletin()
		{
			super(app.getMenuLabel("printBulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doPrint();
		}
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

	class ActionMenuAccount extends AbstractAction
	{
		public ActionMenuAccount()
		{
			super(app.getMenuLabel("account"), null);
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

	static private boolean isAnyBulletinSelected(UiMainWindow window)
	{
		return (window.table.getSelectedBulletins().length > 0);
	}
	
	class ActionMenuEdit extends AbstractAction
	{
		public ActionMenuEdit()
		{
			super(app.getMenuLabel("editBulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doEditBulletin();
		}

		public boolean isEnabled()
		{
			return (table.getSingleSelectedBulletin() != null);
		}
	}
	
	class ActionMenuCut extends AbstractAction
	{
		public ActionMenuCut()
		{
			super(app.getMenuLabel("cutbulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doCutBulletin();
		}

		public boolean isEnabled()
		{
			return isAnyBulletinSelected(UiMainWindow.this);
		}
	}

	class ActionMenuCopy extends AbstractAction
	{
		public ActionMenuCopy()
		{
			super(app.getMenuLabel("copybulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doCopyBulletin();
		}

		public boolean isEnabled()
		{
			return isAnyBulletinSelected(UiMainWindow.this);
		}
	}

	class ActionMenuDiscard extends AbstractAction
	{
		public ActionMenuDiscard()
		{
			super(app.getMenuLabel("discardbulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doDiscardBulletin();
		}
		
		public boolean isEnabled()
		{
			updateName();
			return isAnyBulletinSelected(UiMainWindow.this);
		}

		public void updateName() 
		{
			if(isDiscardedFolderSelected())
				actionMenuDiscard.putValue(actionMenuDiscard.NAME, getApp().getMenuLabel("deletebulletin"));
			else
				actionMenuDiscard.putValue(actionMenuDiscard.NAME, getApp().getMenuLabel("discardbulletin"));
		}
	}

	class ActionMenuPaste extends AbstractAction
	{
		public ActionMenuPaste()
		{
			super(app.getMenuLabel("pastebulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doPasteBulletin();
		}
		public boolean isEnabled()
		{
			 return(getClipboardTransferableBulletin() != null);
		}
	}

	class EditMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuEdit.setEnabled(actionMenuEdit.isEnabled());
			actionMenuCut.setEnabled(actionMenuCut.isEnabled());
			actionMenuCopy.setEnabled(actionMenuCopy.isEnabled());
			actionMenuPaste.setEnabled(actionMenuPaste.isEnabled());
			actionMenuDiscard.setEnabled(actionMenuDiscard.isEnabled());
		}
		
		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuEdit.setEnabled(false);
			actionMenuCut.setEnabled(false);
			actionMenuCopy.setEnabled(false);
			actionMenuPaste.setEnabled(false);
			actionMenuDiscard.setEnabled(false);
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

	class ActionMenuLanguage extends AbstractAction
	{
		public ActionMenuLanguage()
		{
			super(app.getMenuLabel("language"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doLocalize();
		}
	}

	class ActionMenuDateFormat extends AbstractAction
	{
		public ActionMenuDateFormat()
		{
			super(app.getMenuLabel("dateformat"), null);
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

	class ActionMenuServerInfo extends AbstractAction
	{
		public ActionMenuServerInfo()
		{
			super(app.getMenuLabel("serverinfo"), null);
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
	
	class ActionMenuBulletinDetails extends AbstractAction
	{
		public ActionMenuBulletinDetails()
		{
			super(app.getMenuLabel("bulletinDetails"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doChangeBulletinDetails();
		}
	}

	class ActionMenuRetrieve extends AbstractAction
	{
		public ActionMenuRetrieve()
		{
			super(app.getMenuLabel("retrieve"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveMySealedBulletins();
		}
	}
	
	class ActionMenuRetrieveDrafts extends AbstractAction
	{
		public ActionMenuRetrieveDrafts()
		{
			super(app.getMenuLabel("RetrieveDrafts"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveMyDraftBulletins();
		}
	}
	class ActionMenuDeleteServerDrafts extends AbstractAction
	{
		public ActionMenuDeleteServerDrafts()
		{
			super(app.getMenuLabel("DeleteServerDrafts"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doDeleteServerDraftBulletins();
		}
	}

	class ActionMenuRetrieveHQ extends AbstractAction
	{
		public ActionMenuRetrieveHQ()
		{
			super(app.getMenuLabel("retrieveHQ"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveHQBulletins();
		}
	}
	
	class ActionMenuRetrieveHQDrafts extends AbstractAction
	{
		public ActionMenuRetrieveHQDrafts()
		{
			super(app.getMenuLabel("retrieveHQDrafts"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doRetrieveHQDraftsBulletins();
		}
	}
	
	class ActionMenuBackupKeyPair extends AbstractAction
	{
		public ActionMenuBackupKeyPair()
		{
			super(app.getMenuLabel("backupKeyPair"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doBackupKeyPair();
		}
	}
			
	class ActionMenuExportPublicInfo extends AbstractAction
	{
		public ActionMenuExportPublicInfo()
		{
			super(app.getMenuLabel("exportPublicAccountInfo"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doExportPublicAccountInfo();
		}
	}

	class ActionMenuImportPublicInfo extends AbstractAction
	{
		public ActionMenuImportPublicInfo()
		{
			super(app.getMenuLabel("importPublicAccountInfo"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doImportPublicAccountInfo();
		}
	}

	class ActionMenuClearPublicInfo extends AbstractAction
	{
		public ActionMenuClearPublicInfo()
		{
			super(app.getMenuLabel("clearPublicAccountInfo"), null);
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
			ExitNormally();
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
			try
			{
				if(!inConfigServer)
				{
					uploadResult = app.backgroundUpload(statusBar.getBackgroundProgressMeter());
					if(uploadResult != null)
					{
						System.out.println("UiMainWindow.Tick.run: " + uploadResult);
						folderContentsHaveChanged(getStore().getFolderSent());
						folderContentsHaveChanged(getStore().getFolderOutbox());
						folderContentsHaveChanged(getStore().getFolderDraftOutbox());
						folderContentsHaveChanged(getApp().createOrFindFolder(getStore().getNameOfFolderDamaged()));
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
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
				if(currentActiveFrame != UiMainWindow.this)
					UiMainWindow.this.setVisible(false);
				if(!signIn(UiSigninDlg.TIMED_OUT))
					ExitImmediately();
				if(currentActiveFrame != UiMainWindow.this)
					UiMainWindow.this.setVisible(true);
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
			if(uploadResult.equals(app.AUTHENTICATE_SERVER_FAILED) && !authenticationErrorShown)
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
	private ActionEdit actionEdit;
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
	private ActionMenuEdit actionMenuEdit;
	private ActionMenuCut actionMenuCut;
	private ActionMenuCopy actionMenuCopy;
	private ActionMenuPaste actionMenuPaste;
	private ActionMenuDiscard actionMenuDiscard;
	private UiStatusBar statusBar;

	private JFrame currentActiveFrame;	
	private boolean inConfigServer;

	private static final int MAX_KEYPAIRFILE_SIZE = 32000;
	private static final int TIMEOUT_SECONDS = (10 * 60);
	private static final int BACKGROUND_UPLOAD_CHECK_SECONDS = 5*1000;
	private static final int BACKGROUND_TIMEOUT_CHECK_EVERY_X_SECONDS = 5*1000;
	private int clearStatusMessage;
	private File lastAttachmentLoadDirectory;
	private File lastAttachmentSaveDirectory;
}
