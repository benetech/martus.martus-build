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

		int mode = ListSelectionModel.SINGLE_SELECTION;
//		int mode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
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
		if(tb == null)
		{
			File file = mainWindow.getClipboardTransferableFile();
			try 
			{
				if(file != null)
					dropAdapter.attemptDropFile(file, folder);
			} 
			catch (InvalidPacketException e) 
			{
			} catch (SignatureVerificationException e) {
			} catch (IOException e) {
			} catch (CryptoException e) {
			} catch (StatusNotAllowedException e) {
			}
		}
		else
		{
			try
			{
				dropAdapter.attemptDropBulletins(tb.getBulletins(), folder);
			}
			catch (StatusNotAllowedException e)
			{
			}
		}
		
		if(!worked)
			Toolkit.getDefaultToolkit().beep();
	}


	public void doPopupMenu(JComponent component, int x, int y)
	{
		AbstractAction edit = new ActionEditBulletin();
		AbstractAction cut = new ActionCutBulletin();
		AbstractAction copy = new ActionCopyBulletin();

		Bulletin b = getSingleSelectedBulletin();
		if(b == null)
		{
			edit.setEnabled(false);
			cut.setEnabled(false);
			copy.setEnabled(false);
		}

		JPopupMenu menu = new JPopupMenu();
		menu.add(edit);
		menu.add(cut);
		menu.add(copy);
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
			boolean extend = e.isShiftDown();
			boolean toggle = e.isControlDown();
			if(extend || toggle)
			{
				changeSelection(thisRow, 0, toggle, extend);
			}
			else
			{
				if(!isRowSelected(thisRow))
					selectRow(thisRow);

				doPopupMenu(UiBulletinTable.this, e.getX(), e.getY());
			}
		}

		private void handleDoubleClick(MouseEvent e)
		{
			doEditBulletin();
		}
	}


	class ActionEditBulletin extends AbstractAction
	{
		public ActionEditBulletin()
		{
			super(mainWindow.getApp().getMenuLabel("editBulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doEditBulletin();
		}
	}

	class ActionCutBulletin extends AbstractAction
	{
		public ActionCutBulletin()
		{
			super(mainWindow.getApp().getMenuLabel("cutbulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doCutBulletin();
		}
	}

	class ActionCopyBulletin extends AbstractAction
	{
		public ActionCopyBulletin()
		{
			super(mainWindow.getApp().getMenuLabel("copybulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doCopyBulletin();
		}
	}

	class ActionDiscardBulletin extends AbstractAction
	{
		public ActionDiscardBulletin()
		{
			super(mainWindow.getApp().getMenuLabel("discardbulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doDiscardBulletin();
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
			okToDiscard = false;
		}

		if(okToDiscard)
		{
			discardAllSelectedBulletins();
		}
	}

	private void discardAllSelectedBulletins()
	{
		Bulletin[] bulletinsToDiscard = getSelectedBulletins();
		
		BulletinFolder draftOutBox = mainWindow.getApp().getFolderDraftOutbox();
		BulletinFolder folderToDiscardFrom = getFolder();
		
		for (int i = 0; i < bulletinsToDiscard.length; i++)
		{
			Bulletin b = bulletinsToDiscard[i];
			draftOutBox.getStore().discardBulletin(draftOutBox, b);
			folderToDiscardFrom.getStore().discardBulletin(folderToDiscardFrom, b);
		}
		
		folderToDiscardFrom.getStore().saveFolders();
		mainWindow.folderContentsHaveChanged(folderToDiscardFrom);
	}

	private boolean confirmDiscardSingleBulletin(Bulletin b)
	{
		BulletinFolder folderToDiscardFrom = getFolder();
		if(!isDiscardedFolder(folderToDiscardFrom))
			return true;

		boolean okToDiscard = true;
		BulletinFolder draftOutBox = mainWindow.getApp().getFolderDraftOutbox();

		Vector visibleFoldersContainingThisBulletin = mainWindow.getApp().findBulletinInAllVisibleFolders(b);
		visibleFoldersContainingThisBulletin.remove(folderToDiscardFrom.getName());
		Vector localizedFoldersContainingThisBulletin = new Vector();
		for(int i = 0 ; i < visibleFoldersContainingThisBulletin.size() ; ++i)
		{
			localizedFoldersContainingThisBulletin.add(mainWindow.getApp().getFolderLabel((String)(visibleFoldersContainingThisBulletin.get(i))));
		}

		if(localizedFoldersContainingThisBulletin.size() == 0)
		{
			String dialogTag = "";
			if (b.isSealed())
				dialogTag = "DiscardSealedBulletins";
			else if(draftOutBox.contains(b))
				dialogTag = "DeleteDiscardedDraftBulletinWithOutboxCopy";
			else
				dialogTag = "DiscardDraftBulletins";
		
			okToDiscard = mainWindow.confirmDlg(mainWindow, dialogTag);
		}
		else
		{
			String title = mainWindow.getApp().getWindowTitle("confirmDeleteDiscardedBulletinWithCopies");
			String cause = mainWindow.getApp().getFieldLabel("confirmDeleteDiscardedBulletinWithCopiescause");
			String folders = localizedFoldersContainingThisBulletin.toString();
			String effect = mainWindow.getApp().getFieldLabel("confirmDeleteDiscardedBulletinWithCopieseffect");
			String question = mainWindow.getApp().getFieldLabel("confirmquestion");
			String[] contents = {cause, "", effect, folders, "", question};
			okToDiscard = mainWindow.confirmDlg(mainWindow, title, contents);
		}
		return okToDiscard;
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
