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
//logi put back in after 30 day wait
/*
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;

import org.martus.client.core.MartusApp;
import org.martus.common.MartusConstants;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto.KeyShareException;
import org.martus.swing.UiFileChooser;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
*/
public class UiBackupRecoverKeyPair 
{
	public UiBackupRecoverKeyPair(UiMainWindow windowToUse)
	{
		mainWindow = windowToUse;
		localization = mainWindow.getLocalization();
	}
//	logi put back in after 30 day wait
/*	
	public static Map getTokenReplacement() 
	{
		HashMap map = new HashMap();
		map.put("#M#", Integer.toString(MartusConstants.numberOfFilesInShare));
		map.put("#N#", Integer.toString(MartusConstants.minNumberOfFilesNeededToRecreateSecret));
		return map;
	}

	public boolean recoverKeyPairFromMultipleUnencryptedFiles()
	{
		mainWindow.notifyDlg(mainWindow, "RecoveryProcessKeyShare",UiBackupRecoverKeyPair.getTokenReplacement());
		
		File firstShareFile = getRecoveryDriveToUse();
		if(firstShareFile == null)
			return false;
 
		Vector shares = recoverMinimumKeySharesNeededFromFiles(firstShareFile);
		if(shares == null)
			return false;
		
		try 
		{
			mainWindow.getApp().getSecurity().recoverFromKeyShareBundles(shares);
		} 
		catch (KeyShareException e) 
		{
			e.printStackTrace();
			if(mainWindow.confirmDlg(mainWindow, "RecoveredKeyShareFailedTryAgain"))
				return recoverKeyPairFromMultipleUnencryptedFiles();
			return false;			
		}

		return keyPairRecoveredNewUserAndPasswordRequired();
	}

	public void backupKeyPairToMultipleUnencryptedFiles() 
	{
		String message = localization.getFieldLabel("BackupKeyPairToMultipleUnencryptedFilesInformation");
		mainWindow.displayScrollableMessage("BackupKeyPairToMultipleUnencryptedFilesInformation", message, "Continue", getTokenReplacement());

		String defaultFileName = getDefaultKeyShareFileName();
		if(defaultFileName == null)
			return;
		
		Vector keyShareBundles = mainWindow.getApp().getSecurity().getKeyShareBundles();
		if(keyShareBundles == null)
		{
			mainWindow.notifyDlg(mainWindow,"ErrorBackingUpKeyShare");
			return;
		}

		String driveToUse = getBackupKeyShareDriveToUse(defaultFileName);
		if(driveToUse == null)
			return;

		if(!writeAndVerifyKeySharesToDisks(defaultFileName, keyShareBundles, driveToUse))
			return;

		message = localization.getFieldLabel("BackupKeyShareCompleteInformation");
		mainWindow.displayScrollableMessage("BackupKeyShareCompleteInformation", message, "ok", getTokenReplacement());
	}

	private Vector recoverMinimumKeySharesNeededFromFiles(File firstShareFile) 
	{
		String defaultShareFileName = getRootKeyShareFileName(firstShareFile);
		if(defaultShareFileName == null)
			return null;

		int maxFiles = MartusConstants.numberOfFilesInShare;
		int minNumber = MartusConstants.minNumberOfFilesNeededToRecreateSecret;
		Vector shares = new Vector();

		for(int disk = 1; disk <= minNumber; ++disk )
		{
			while(true)
			{
				String[] filesMatching = firstShareFile.getParentFile().list(new BackupShareFilenameFilter(defaultShareFileName, MartusApp.SHARE_KEYPAIR_FILENAME_EXTENSION));
				
				if(filesMatching == null || filesMatching.length == 0)
				{
					if(!insertDisk("ErrorRecoverNoAppropriateFileFound", disk, minNumber, "CancelShareRecover"))
						return null;
					continue;
				}
				File shareFile = new File(firstShareFile.getParent(), filesMatching[0]);
				if(shareFile == null || !shareFile.isFile())
				{
					if(!insertDisk("ErrorRecoverNoAppropriateFileFound", disk, minNumber, "CancelShareRecover"))
						return null;
					continue;
				}

				try 
				{
					UnicodeReader reader = new UnicodeReader(shareFile);
					shares.add(reader.readAll(6));
					reader.close();

					if(disk == minNumber)
						break;

					if(!insertDisk("RecoverShareKeyPair", disk+1, minNumber, "CancelShareRecover"))
						return null;
					break;
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					if(!insertDisk("ErrorRecoverShareDisk", disk, minNumber, "CancelShareRecover"))
						return null;
					continue;
				}
			}
		}
		return shares;
	}

	private String getRootKeyShareFileName(File file) 
	{
		String completeFileName = file.getName();
		int index = completeFileName.lastIndexOf("-");
		if(index == -1)
			return null;
		return completeFileName.substring(0,index);
	}

	private File getRecoveryDriveToUse() 
	{
		File firstShareFile = null;
		while(true)
		{
			UiFileChooser chooser = new UiFileChooser();
			String windowTitle = localization.getWindowTitle("SaveShareKeyPair");
			chooser.setDialogTitle(windowTitle);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setSelectedFile(new File("", " "));
			if (chooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
			{
				firstShareFile = chooser.getSelectedFile();
				if(firstShareFile != null && firstShareFile.isFile())
				{
					if(getRootKeyShareFileName(firstShareFile) != null)
						break;

					if(!mainWindow.confirmDlg(mainWindow, "ErrorRecoverIvalidFileName"))
						return null;
					continue;
				}
			}
			if(mainWindow.confirmDlg(mainWindow, "CancelShareRecover"))
				return null;
		}
		return firstShareFile;
	}

	private boolean keyPairRecoveredNewUserAndPasswordRequired() 
	{
		mainWindow.notifyDlg(mainWindow, "RecoveredKeyShareSucceededNewUserNamePasswordRequired");
		
		while(true)
		{
			MartusApp app = mainWindow.getApp();
			if(mainWindow.getAndSaveUserNamePassword(app.getKeyPairFile(app.getMartusDataRootDirectory())))
			{					
				mainWindow.notifyDlg(mainWindow, "RecoveryOfKeyShareComplete");
				return true;
			}	
			if(mainWindow.confirmDlg(mainWindow, "CancelShareRecover"))
				return false;
		}	
	}

	private String getDefaultKeyShareFileName() 
	{
		String defaultInputText = "";
		String enteredFileName = mainWindow.getStringInput("GetShareFileName","GetShareFileNameDescription",defaultInputText);
		if(enteredFileName == null)
			return null;
		String defaultFileName = MartusUtilities.createValidFileName(enteredFileName);
		return defaultFileName;
	}

	private String getBackupKeyShareDriveToUse(String defaultFileName) 
	{
		while(true)
		{
			UiFileChooser chooser = new UiFileChooser();
			String windowTitle = localization.getWindowTitle("SaveShareKeyPair");
			chooser.setDialogTitle(windowTitle);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			String fileName = defaultFileName + "-1" + MartusApp.SHARE_KEYPAIR_FILENAME_EXTENSION;
			chooser.setSelectedFile(new File("", fileName));
			if (chooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
				return chooser.getSelectedFile().getParent();
			if(mainWindow.confirmDlg(mainWindow, "CancelShareBackup"))
				break;
		}	
		return null;
	}

	private boolean writeAndVerifyKeySharesToDisks(String defaultFileName, Vector keyShareBundles, String driveToUse) 
	{
		Vector shareFiles = new Vector();
		int maxFiles = MartusConstants.numberOfFilesInShare;
		for(int disk = 1; disk <= maxFiles; ++disk )
		{
			while(true)
			{
				String fileName = defaultFileName + "-" + Integer.toString(disk) + MartusApp.SHARE_KEYPAIR_FILENAME_EXTENSION;
				File currentShareFile = new File(driveToUse, fileName);
				String[] otherBackupFiles = currentShareFile.getParentFile().list(new BackupShareFilenameFilter(defaultFileName, MartusApp.SHARE_KEYPAIR_FILENAME_EXTENSION));
				if(otherBackupFiles != null && otherBackupFiles.length > 0)
				{
					if(!insertDisk("ErrorPreviousBackupShareExists", disk, maxFiles, "CancelShareBackup"))
						return false;
					continue;
				}
		
				if(!writeSharePieceToFile(currentShareFile, (String) keyShareBundles.get(disk - 1)))
				{
					if(!insertDisk("ErrorBackingupKeyPair", disk, maxFiles, "CancelShareBackup"))
						return false;
					continue;
				}
				
				shareFiles.add(currentShareFile);
				if(disk == maxFiles)
					break;
					
				if(!insertDisk("SaveShareKeyPair", disk+1, maxFiles, "CancelShareBackup"))
					return false;
				break;
			}
		}
			
		verifyKeyShareDisks(keyShareBundles, shareFiles, MartusConstants.numberOfFilesInShare);

		return true;
	}

	private void verifyKeyShareDisks(Vector keyShareBundles, Vector shareFiles,	int maxFiles) 
	{
		boolean verifiedAll = false;
		if(mainWindow.confirmDlg(mainWindow,"BackupKeyShareVerifyDisks"))
		{
			for(int disk = 1; disk <= maxFiles; ++disk )
			{
				if(!insertDisk("VerifyingKeyPairShare", disk, maxFiles, "CancelShareVerify"))
					break;
				boolean exitVerification = false;
				while(true)
				{
					if(!verifySharePieceFromFile((File)shareFiles.get(disk-1), (String) keyShareBundles.get(disk - 1)))
					{
						if(!insertDisk("ErrorVerifyingKeyPairShare", disk, maxFiles, "CancelShareVerify"))
						{
							exitVerification = true;
							break;
						}
						continue;
					}
					if(disk == maxFiles)
						verifiedAll = true;
					break;
				}
				if(exitVerification)
					break;
			}	
			if(verifiedAll)
				mainWindow.notifyDlg(mainWindow, "VerifyKeyPairSharePassed");	
		}
	}

	private boolean verifySharePieceFromFile(File shareFile, String dataToCompare) 
	{
		try
		{
			UnicodeReader reader = new UnicodeReader(shareFile);
			String contents = reader.readAll(6);
			reader.close();
			if(contents.compareTo(dataToCompare)==0)
				return true;
		}
		catch (IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
		return false;
	}

	private boolean insertDisk(String titleMessageTag, int diskNumber, int totalNumberOfDisks, String confirmCancelTag)
	{
		String windowTitle = localization.getWindowTitle(titleMessageTag);

		String message1 = localization.getFieldLabel(titleMessageTag);
		String message2 = localization.getFieldLabel("BackupRecoverKeyPairInsertNextDiskMessage") +
							" " + Integer.toString(diskNumber) + " " +	
		localization.getWindowTitle("SaveRecoverShareKeyPairOf") + " " +
		Integer.toString(totalNumberOfDisks);
		String insertNextDiskMessage[] = {message1, message2};

		String buttons[] = {localization.getButtonLabel("ok"), localization.getButtonLabel("cancel")};			

		if(!mainWindow.confirmDlg(mainWindow, windowTitle, insertNextDiskMessage, buttons))
		{
			if(mainWindow.confirmDlg(mainWindow, confirmCancelTag))
				return false;
		}
		return true;
	}
	
	private boolean writeSharePieceToFile(File newBackupFile, String dataToSave) 
	{
		try
		{
			UnicodeWriter output = new UnicodeWriter(newBackupFile);
			output.write(dataToSave);
			output.close();
			
			return verifySharePieceFromFile(newBackupFile, dataToSave);
		}
		catch (IOException ioe)
		{
			System.out.println(ioe.getMessage());
			return false;
		}
	}

	public class BackupShareFilenameFilter implements FilenameFilter
	{
		BackupShareFilenameFilter(String name, String extension)
		{
			defaultName = name;
			defaultExtension = extension;
		}
		
		public boolean accept(File dir, String name)
		{
			if(name.startsWith(defaultName) && name.endsWith(defaultExtension))
				return true;
			return false;
		}
		String defaultName;
		String defaultExtension;
	}
*/
	UiMainWindow mainWindow;
	UiLocalization localization;
}
