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
		Bulletin b = getSelectedBulletin();
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
		Bulletin selected = getSelectedBulletin();
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
		System.out.println(t);
		DataFlavor[] flavors = t.getTransferDataFlavors();
		for(int i=0; i < flavors.length; ++i)
			System.out.println(flavors[i].getHumanPresentableName());
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
		AbstractAction discard;
		if(mainWindow.isDiscardedFolderSelected())
			discard = new ActionDeleteBulletin();
		else
			discard = new ActionDiscardBulletin();
		
		Bulletin b = getSelectedBulletin();
		if(b == null)
		{
			edit.setEnabled(false);
			cut.setEnabled(false);
			copy.setEnabled(false);
			discard.setEnabled(false);
		}

		JPopupMenu menu = new JPopupMenu();
		menu.add(edit);
		menu.add(cut);
		menu.add(copy);
		menu.add(mainWindow.getActionMenuPaste());
		menu.addSeparator();
		menu.add(discard);
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
			super.mousePressed(e);
			handleRightClick(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			super.mouseReleased(e);
			handleRightClick(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			super.mouseClicked(e);
			handleRightClick(e);
			if(e.getClickCount() == 2)
				handleDoubleClick(e);
		}


		private void handleRightClick(MouseEvent e)
		{
			if(!e.isPopupTrigger())
				return;

			int thisRow = rowAtPoint(e.getPoint());
			if(getSelectedRowCount() != 1 || getSelectedRow() != thisRow)
				selectRow(thisRow);
			
			if(getSelectedRowCount() == 0)
			{
				System.out.println("Table clicked on non-selected row");
				return;
			}
			doPopupMenu(UiBulletinTable.this, e.getX(), e.getY());
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

	class ActionDeleteBulletin extends AbstractAction
	{
		public ActionDeleteBulletin()
		{
			super(mainWindow.getApp().getMenuLabel("deletebulletin"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			doDiscardBulletin();
		}
	}

	private void doDiscardBulletin()
	{
		Bulletin b = getSelectedBulletin();
		BulletinFolder f = getFolder();
		if(f.equals(f.getStore().getFolderDiscarded()))
		{
			Vector visibleFoldersContainingThisBulletin = mainWindow.getApp().findBulletinInAllVisibleFolders(b);
			final int justTheDiscardedFolder = 1;
			if(visibleFoldersContainingThisBulletin.size() == justTheDiscardedFolder)
			{
				if (b.isSealed())
				{
					if(!mainWindow.confirmDlg(mainWindow, "DiscardSealedBulletins"))
						return;
				}				
				else
				{
					BulletinFolder draftOutBox = mainWindow.getApp().getFolderDraftOutbox();
					if(draftOutBox.contains(b))
					{
						if(!mainWindow.confirmDlg(mainWindow, "DeleteDiscardedDraftBulletinWithOutboxCopy"))
							return;
						draftOutBox.getStore().discardBulletin(draftOutBox, b);
					}
					else if(!mainWindow.confirmDlg(mainWindow, "DiscardDraftBulletins"))
						return;
				}
			}
			else
			{
				visibleFoldersContainingThisBulletin.remove(f.getName());
				String title = mainWindow.getApp().getWindowTitle("confirmDeleteDiscardedBulletinWithCopies");
				String cause = mainWindow.getApp().getFieldLabel("confirmDeleteDiscardedBulletinWithCopiescause");
				String folders = visibleFoldersContainingThisBulletin.toString();
				String effect = mainWindow.getApp().getFieldLabel("confirmDeleteDiscardedBulletinWithCopieseffect");
				String question = mainWindow.getApp().getFieldLabel("confirmquestion");
				String[] contents = {cause, "", effect, folders, "", question};
				if(!mainWindow.confirmDlg(mainWindow, title, contents))
					return;
			}
		}
		f.getStore().discardBulletin(f, b);
		f.getStore().saveFolders();
		mainWindow.folderContentsHaveChanged(f);
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
