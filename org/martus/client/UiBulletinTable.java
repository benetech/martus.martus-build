package org.martus.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.martus.client.BulletinStore.StatusNotAllowedException;
import org.martus.common.MartusCrypto.CryptoException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;


public class UiBulletinTable extends JTable implements ListSelectionListener, DragGestureListener, DragSourceListener
{
    public UiBulletinTable(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		bulletinsList = new BulletinsList(mainWindow.getApp());
		setModel(bulletinsList);

		// set widths for first two columns (status and date)
		setColumnWidthToHeaderWidth(0);
		setColumnWidthToHeaderWidth(1);

		addMouseListener(new TableMouseAdapter());
		addKeyListener(new TableKeyAdapter());
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().addMouseListener(new TableHeaderMouseAdapter());

		int mode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
		getSelectionModel().setSelectionMode(mode);
		getSelectionModel().addListSelectionListener(this);

		dragSource.createDefaultDragGestureRecognizer(this,
							DnDConstants.ACTION_COPY_OR_MOVE, this);

		dropAdapter = new UiBulletinTableDropAdapter(this, mainWindow);
	}

	public UiBulletinTableDropAdapter getDropAdapter()
	{
		return dropAdapter;
	}

	public BulletinFolder getFolder()
	{
		return bulletinsList.getFolder();
	}

	public void setFolder(BulletinFolder folder)
	{
		bulletinsList.setFolder(folder);
	}

	public Bulletin getSelectedBulletin()
	{
		int selectedRow = getSelectedRow();
		if(selectedRow < 0)
		{
			//System.out.println("There is no selected bulletin");
			return null;
		}

		return bulletinsList.getBulletin(selectedRow);
	}
	
	public Bulletin[] getSelectedBulletins()
	{
		int[] selectedRows = getSelectedRows();
		Bulletin[] bulletins = new Bulletin[selectedRows.length];
		
		for (int row = 0; row < selectedRows.length; row++) 
		{
			bulletins[row] = bulletinsList.getBulletin(selectedRows[row]);
		}
		
		return bulletins;
	}

	public Bulletin getSingleSelectedBulletin()
	{
		Bulletin[] selected = getSelectedBulletins();
		Bulletin b = null;
		if(selected.length == 1)
		{
			b = selected[0];
		}
		return b;
	}

	public void selectBulletin(Bulletin b)
	{
		selectRow(bulletinsList.findBulletin(b));
	}
	
	public void selectBulletins(Bulletin[] bulletins)
	{
		clearSelection();
		for (int i = 0; i < bulletins.length; i++)
		{
			int row = bulletinsList.findBulletin(bulletins[i]);
			if(row >= 0)
				addRowSelectionInterval(row, row);
		}
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		Bulletin[] selected = getSelectedBulletins();
		tableChanged(new TableModelEvent(bulletinsList));
		selectBulletins(selected);
	}

	// ListSelectionListener interface
	public void valueChanged(ListSelectionEvent e)
	{
		if(!e.getValueIsAdjusting() && bulletinsList != null)
		{
			mainWindow.bulletinSelectionHasChanged();
		}
		repaint();
	}

	// DragGestureListener interface
	public void dragGestureRecognized(DragGestureEvent dragGestureEvent)
	{
		if (getSelectedRowCount() == 0)
			return;

		Bulletin[] bulletinsBeingDragged = getSelectedBulletins();
		TransferableBulletinList dragable = new TransferableBulletinList(bulletinsBeingDragged, getFolder());
		dragGestureEvent.startDrag(DragSource.DefaultCopyDrop, dragable, this);
	}

	// DragSourceListener interface
	// we don't care when we enter or exit a drop target
	public void dragEnter (DragSourceDragEvent dsde)						{}
	public void dragExit(DragSourceEvent DragSourceEvent)					{}
	public void dragOver(DragSourceDragEvent DragSourceDragEvent)			{}
	public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent)	{}
	public void dragDropEnd(DragSourceDropEvent dsde)						{}

	public void doEditBulletin() 
	{
		Bulletin b = getSingleSelectedBulletin();
		if(b == null)
			return;
			
		boolean createClone = false;
		String bulletinAccountId = b.getAccount();
		String myAccountId = mainWindow.getApp().getAccountId();
		if(myAccountId.equals(bulletinAccountId))
		{
			if(!b.isDraft())
			{
				if(!mainWindow.confirmDlg(mainWindow, "CloneMySealedAsDraft"))
					return;
				createClone = true;
			}
		}
		else
		{
			if(!mainWindow.confirmDlg(mainWindow, "CloneBulletinAsMine"))
				return;
			createClone = true;
		}

		if(createClone)
		{
			BulletinStore store = mainWindow.getApp().getStore();
			Bulletin clone = store.createEmptyBulletin();
			try 
			{
				clone.pullDataFrom(b);
				b = clone;
			} 
			catch (Exception e) 
			{
				mainWindow.notifyDlg(mainWindow, "UnexpectedError");
				return;
			}
		}	
		mainWindow.editBulletin(b);
	}

	public void doCutBulletin()
	{
		doCopyBulletin();
		doDiscardBulletin();
	}

	public void doCopyBulletin()
	{
		Bulletin[] selected = getSelectedBulletins();
		BulletinFolder folder = getFolder();
		TransferableBulletinList tb = new TransferableBulletinList(selected, folder);

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		Transferable contents = clipboard.getContents(this);
		mainWindow.lostOwnership(clipboard, contents);

		clipboard.setContents(tb, mainWindow);
		try
		{
			System.out.println("Did copy :" + tb.getTransferData(DataFlavor.stringFlavor));
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		
		System.out.println("UiBulletinTable.doCopyBulletin:");
		Transferable t = clipboard.getContents(null);
	}

	public void doPasteBulletin()
	{
		BulletinFolder folder = getFolder();
		TransferableBulletinList tb = mainWindow.getClipboardTransferableBulletin();
	
		boolean worked = false;
		String resultMessageTag = null;
		if(tb == null)
		{
			File file = mainWindow.getClipboardTransferableFile();
			try 
			{
				if(file != null)
					dropAdapter.attemptDropFile(file, folder);
				worked = true;
				if(confirmDeletionOfFile(file.getPath()))
					file.delete();
			} 
			catch (InvalidPacketException e) 
			{
				resultMessageTag = "PasteError";
			} 
			catch (SignatureVerificationException e) 
			{
				resultMessageTag = "PasteError";
			} 
			catch (IOException e) 
			{
				resultMessageTag = "PasteError";
			} 
			catch (CryptoException e) 
			{
				resultMessageTag = "PasteError";
			} 
			catch (StatusNotAllowedException e) 
			{
				resultMessageTag = "PasteErrorNotAllowed";
			}
		}
		else
		{
			try
			{
				dropAdapter.attemptDropBulletins(tb.getBulletins(), folder);
				worked = true;
			}
			catch (StatusNotAllowedException e)
			{
				resultMessageTag = "PasteErrorNotAllowed";
			}
		}
		
		if(!worked)
		{
			Toolkit.getDefaultToolkit().beep();
			mainWindow.notifyDlg(mainWindow, resultMessageTag);
		}
	}

	public boolean confirmDeletionOfFile(String filePath) 
	{
		MartusApp app = mainWindow.getApp();
		String title = app.getWindowTitle("DeleteBulletinFile");
		String msg1 = app.getFieldLabel("DeleteBulletinFileMsg1");
		String msg2 = app.getFieldLabel("DeleteBulletinFileMsg2");
		String[] contents = {msg1, filePath, msg2};
		
		String delete = app.getButtonLabel("Delete");
		String leave = app.getButtonLabel("Leave");
		String[] buttons = {delete, leave};
		
		UiNotifyDlg notify = new UiNotifyDlg(mainWindow, mainWindow, title, contents, buttons);
		String result = notify.getResult();
		if(result != null && result.equals(delete))
			return true;
		return false;
	}

	public void doPopupMenu(JComponent component, int x, int y)
	{
		JPopupMenu menu = new JPopupMenu();
		menu.add(mainWindow.getActionMenuEdit());
		menu.addSeparator();
		menu.add(mainWindow.getActionMenuCut());
		menu.add(mainWindow.getActionMenuCopy());
		menu.add(mainWindow.getActionMenuPaste());
		menu.addSeparator();
		menu.add(mainWindow.getActionMenuDiscard());
		menu.show(component, x, y);
	}

	class TableHeaderMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if(!e.isPopupTrigger())
			{
				JTableHeader header = (JTableHeader)e.getSource();
				int col = header.columnAtPoint(e.getPoint());
				bulletinsList.sortByColumn(col);
				mainWindow.folderContentsHaveChanged(getFolder());
			}
		}
	}

	class TableKeyAdapter extends KeyAdapter
	{
		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() == e.VK_DELETE)
			{
				doDiscardBulletin();
			}
		}
	}

	class TableMouseAdapter extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			if(e.isPopupTrigger())
				handleRightClick(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			if(e.isPopupTrigger())
				handleRightClick(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() == 2)
				handleDoubleClick(e);
		}


		private void handleRightClick(MouseEvent e)
		{
			int thisRow = rowAtPoint(e.getPoint());
			boolean isInsideSelection = isRowSelected(thisRow);
			if(!isInsideSelection && !e.isShiftDown() && !e.isControlDown())
				selectRow(thisRow);

			doPopupMenu(UiBulletinTable.this, e.getX(), e.getY());
		}

		private void handleDoubleClick(MouseEvent e)
		{
			doEditBulletin();
		}
	}



	public void doDiscardBulletin()
	{
		boolean okToDiscard = true;
		Bulletin[] bulletins = getSelectedBulletins();
		if(bulletins.length == 1)
		{
			okToDiscard = confirmDiscardSingleBulletin(bulletins[0]);
		}
		else
		{
			okToDiscard = confirmDiscardMultipleBulletins();
		}

		if(okToDiscard)
		{
			discardAllSelectedBulletins();
		}
	}

	private void discardAllSelectedBulletins()
	{
		Bulletin[] bulletinsToDiscard = getSelectedBulletins();
		
		MartusApp app = mainWindow.getApp();
		BulletinFolder draftOutBox = app.getFolderDraftOutbox();
		BulletinFolder discardedFolder = app.getFolderDiscarded();
		BulletinFolder folderToDiscardFrom = getFolder();
		
		for (int i = 0; i < bulletinsToDiscard.length; i++)
		{
			Bulletin b = bulletinsToDiscard[i];
			draftOutBox.getStore().discardBulletin(draftOutBox, b);
			folderToDiscardFrom.getStore().discardBulletin(folderToDiscardFrom, b);
		}
		
		folderToDiscardFrom.getStore().saveFolders();
		mainWindow.folderContentsHaveChanged(folderToDiscardFrom);
		mainWindow.folderContentsHaveChanged(discardedFolder);
	}

	private boolean confirmDiscardSingleBulletin(Bulletin b)
	{
		BulletinFolder folderToDiscardFrom = getFolder();
		if(!isDiscardedFolder(folderToDiscardFrom))
			return true;

		MartusApp app = mainWindow.getApp();
		BulletinFolder draftOutBox = app.getFolderDraftOutbox();

		Vector visibleFoldersContainingThisBulletin = app.findBulletinInAllVisibleFolders(b);
		visibleFoldersContainingThisBulletin.remove(folderToDiscardFrom);

		String dialogTag = "";
		if(visibleFoldersContainingThisBulletin.size() > 0)
			dialogTag = "confirmDeleteDiscardedBulletinWithCopies";
		else if (b.isSealed())
			dialogTag = "confirmDiscardSealedBulletins";
		else if(draftOutBox.contains(b))
			dialogTag = "confirmDeleteDiscardedDraftBulletinWithOutboxCopy";
		else
			dialogTag = "confirmDiscardDraftBulletins";

		return confirmDeleteBulletins(dialogTag, visibleFoldersContainingThisBulletin);
	}

	private boolean confirmDiscardMultipleBulletins()
	{
		BulletinFolder folderToDiscardFrom = getFolder();
		if(!isDiscardedFolder(folderToDiscardFrom))
			return true;

		MartusApp app = mainWindow.getApp();
		Vector visibleFoldersContainingAnyBulletin = new Vector();
		Bulletin[] bulletins = getSelectedBulletins();
		for (int i = 0; i < bulletins.length; i++)
		{
			Bulletin b = bulletins[i];
			Vector visibleFoldersContainingThisBulletin = app.findBulletinInAllVisibleFolders(b);
			visibleFoldersContainingThisBulletin.remove(folderToDiscardFrom);
			visibleFoldersContainingAnyBulletin.addAll(visibleFoldersContainingThisBulletin);
		}
		
		String dialogTag = "";
		if(visibleFoldersContainingAnyBulletin.size() > 0)
			dialogTag = "confirmDeleteMultipleDiscardedBulletinsWithCopies";
		else
			dialogTag = "confirmDeleteMultipleDiscardedBulletins";

		return confirmDeleteBulletins(dialogTag, visibleFoldersContainingAnyBulletin);
	}

	private boolean confirmDeleteBulletins(String dialogTag, Vector foldersToList)
	{
		MartusApp app = mainWindow.getApp();
		String title = app.getWindowTitle(dialogTag);
		String cause = app.getFieldLabel(dialogTag + "cause");
		String folders = buildFolderNameList(foldersToList);
		String effect = app.getFieldLabel(dialogTag + "effect");
		String question = app.getFieldLabel("confirmquestion");
		String[] contents = {cause, "", effect, folders, "", question};
		return mainWindow.confirmDlg(mainWindow, title, contents);
	}
	
	private String buildFolderNameList(Vector visibleFoldersContainingThisBulletin)
	{
		MartusApp app = mainWindow.getApp();
		String names = "";
		for(int i = 0 ; i < visibleFoldersContainingThisBulletin.size() ; ++i)
		{
			BulletinFolder thisFolder = (BulletinFolder)visibleFoldersContainingThisBulletin.get(i);
			String thisFolderInternalName = thisFolder.getName();
			String thisFolderLocalizedName = app.getFolderLabel(thisFolderInternalName);
			names += thisFolderLocalizedName + "\n";
		}
		return names;
	}

	private boolean isDiscardedFolder(BulletinFolder f)
	{
		return f.equals(f.getStore().getFolderDiscarded());
	}

	void selectRow(int rowIndex)
	{
		if(rowIndex >= 0 && rowIndex < getRowCount())
			setRowSelectionInterval(rowIndex, rowIndex);
	}

	private void setColumnWidthToHeaderWidth(int colIndex)
	{
		TableColumnModel columnModel = getColumnModel();
		TableColumn statusColumn = columnModel.getColumn(colIndex);
		String padding = "    ";
		String value = (String)statusColumn.getHeaderValue() + padding;

		TableCellRenderer renderer = statusColumn.getHeaderRenderer();
		if(renderer == null)
		{
			JTableHeader header = getTableHeader();
			renderer = header.getDefaultRenderer();
		}
		Component c = renderer.getTableCellRendererComponent(this, value, true, true, -1, colIndex);
		Dimension size = c.getPreferredSize();

		statusColumn.setPreferredWidth(size.width);
		statusColumn.setMaxWidth(size.width);
	}


	private UiMainWindow mainWindow;
	private BulletinsList bulletinsList;
	private DragSource dragSource = DragSource.getDefaultDragSource();
	private UiBulletinTableDropAdapter dropAdapter;
}
