/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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

package org.martus.client.test;

import java.awt.Dimension;
import java.awt.Point;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.martus.client.core.CurrentUiState;
import org.martus.common.TestCaseEnhanced;


public class TestCurrentUiState extends TestCaseEnhanced
{

	public TestCurrentUiState(String name)
	{
		super(name);
	}

	public void testDefaultValues() throws Exception
	{
		CurrentUiState state = new CurrentUiState();
		assertEquals("Current Version not 4 - more tests needed?", 4, CurrentUiState.VERSION);

		assertEquals("Default Keyboard not Virtual?", true, state.isCurrentDefaultKeyboardVirtual());
		assertEquals("Default PreviewSplitterPosition not 100?", 100, state.getCurrentPreviewSplitterPosition());
		assertEquals("Default FolderSplitterPosition not 180?", 180, state.getCurrentFolderSplitterPosition());
		assertEquals("Default state not OK?", CurrentUiState.OPERATING_STATE_OK, state.getCurrentOperatingState());
	}

	public void testSaveAndLoadState() throws Exception
	{
		String sampleFolder = "myFolder";
		String sampleTag = "lfdlsj";
		int sampleDir = 991;
		int sampleBulletinPosition = 556;
		boolean sampleDefaultKeyboardVirtual = true;
		String sampleDateFormat = "DD/mm/YYYY";
		String sampleLanguage = "It";
		int samplePreviewSplitterPosition = 420;
		int sampleFolderSplitterPosition = 820;
		Dimension sampleAppDimension = new Dimension(323,444);	
		Point sampleAppPosition = new Point(3, 8);
		boolean sampleAppMaximized = false;
		
		Dimension sampleEditorDimension = new Dimension(123, 43);	
		Point sampleEditorPosition = new Point(2, 99);
		boolean sampleEditorMaximized = true;
		String sampleOperationState = CurrentUiState.OPERATING_STATE_UNKNOWN;

		CurrentUiState state = new CurrentUiState();

		state.setCurrentFolder(sampleFolder);	
		assertEquals("Not the same folder name?", sampleFolder, state.getCurrentFolder());
		state.setCurrentSortTag(sampleTag);
		state.setCurrentSortDirection(sampleDir);
		state.setCurrentBulletinPosition(sampleBulletinPosition);
		state.setCurrentDefaultKeyboardVirtual(sampleDefaultKeyboardVirtual);
		state.setCurrentDateFormat(sampleDateFormat);
		state.setCurrentLanguage(sampleLanguage);
		state.setCurrentFolderSplitterPosition(sampleFolderSplitterPosition);
		state.setCurrentPreviewSplitterPosition(samplePreviewSplitterPosition);

		state.setCurrentAppDimension(sampleAppDimension);
		state.setCurrentAppPosition(sampleAppPosition);
		state.setCurrentAppMaximized(sampleAppMaximized);

		state.setCurrentEditorDimension(sampleEditorDimension);
		state.setCurrentEditorPosition(sampleEditorPosition);
		state.setCurrentEditorMaximized(sampleEditorMaximized);
		
		state.setCurrentOperatingState(sampleOperationState);

		File file = File.createTempFile("$$$TestCurrentFolder",null);
		file.deleteOnExit();
		state.save(file);
		CurrentUiState loaded = new CurrentUiState();
		loaded.load(file);
		assertEquals("Wrong folder name?", sampleFolder, loaded.getCurrentFolder());
		assertEquals("Wrong sort tag?", sampleTag, loaded.getCurrentSortTag());
		assertEquals("Wrong sort dir?", sampleDir, loaded.getCurrentSortDirection());
		assertEquals("Wrong bulletin position?", sampleBulletinPosition, loaded.getCurrentBulletinPosition());
		assertEquals("Wrong Keyboard?", sampleDefaultKeyboardVirtual, loaded.isCurrentDefaultKeyboardVirtual());
		assertEquals("Wrong Date?", sampleDateFormat, loaded.getCurrentDateFormat());
		assertEquals("Wrong Language?", sampleLanguage, loaded.getCurrentLanguage());
		assertEquals("Wrong FolderSplitterPosition?", sampleFolderSplitterPosition, loaded.getCurrentFolderSplitterPosition());
		assertEquals("Wrong PreviewSplitterPosition?", samplePreviewSplitterPosition, loaded.getCurrentPreviewSplitterPosition());

		assertEquals("Wrong App Dimension?", sampleAppDimension, loaded.getCurrentAppDimension());
		assertEquals("Wrong App Position?", sampleAppPosition, loaded.getCurrentAppPosition());
		assertEquals("Wrong App Maximized?", sampleAppMaximized, loaded.isCurrentAppMaximized());

		assertEquals("Wrong Editor Dimension?", sampleEditorDimension, loaded.getCurrentEditorDimension());
		assertEquals("Wrong Editor Position?", sampleEditorPosition, loaded.getCurrentEditorPosition());
		assertEquals("Wrong Editor Maximized?", sampleEditorMaximized, loaded.isCurrentEditorMaximized());
		
		assertEquals("Wrong Operating State?", sampleOperationState, loaded.getCurrentOperatingState());
	}

	public void testLoadAndSaveErrors() throws Exception
	{
		File file = File.createTempFile("$$$TestCurrentFolder2",null);
		file.deleteOnExit();
		file.delete();
		CurrentUiState loaded = new CurrentUiState();
		loaded.load(file);
		assertNotNull("State was null?", loaded);
		assertEquals("Wrong default folder", "", loaded.getCurrentFolder());
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));		
		out.writeObject(new Integer(6));
		out.close();
		loaded.load(file);
		assertNotNull("State was null2?", loaded);
	}
	
	public class OldVersionUiState extends CurrentUiState
	{
		public void save(File file) 
		{
			try
			{
				FileOutputStream outputStream = new FileOutputStream(file);
				DataOutputStream out = new DataOutputStream(outputStream);
				out.writeInt(uiStateFirstIntegerInFile);
				out.writeShort(1);
				out.writeUTF(currentFolderName);
				out.writeUTF(currentSortTag);
				out.writeInt(currentSortDirection);
				out.writeInt(currentBulletinPosition);
				out.writeBoolean(currentDefaultKeyboardIsVirtual);
				out.writeUTF(currentDateFormat);
				out.writeUTF(currentLanguage);
				out.close();
			}
			catch(Exception e)
			{
				System.out.println("CurrentUiState.save error: " + e);
			}
		}
	}	

	public void testOldUiStateFile() throws Exception
	{
		String sampleFolder = "oldFolder";
		String sampleTag = "jj";
		int sampleDir = 3;
		int sampleBulletinPosition = 6;
		boolean sampleDefaultKeyboardVirtual = false;
		String sampleDateFormat = "dd/mm/yyyy";
		String sampleLanguage = "en";
		
		File file = createTempFile();
		OldVersionUiState oldStateFile = new OldVersionUiState();

		oldStateFile.setCurrentFolder(sampleFolder);	
		oldStateFile.setCurrentSortTag(sampleTag);
		oldStateFile.setCurrentSortDirection(sampleDir);
		oldStateFile.setCurrentBulletinPosition(sampleBulletinPosition);
		oldStateFile.setCurrentDefaultKeyboardVirtual(sampleDefaultKeyboardVirtual);
		oldStateFile.setCurrentDateFormat(sampleDateFormat);
		oldStateFile.setCurrentLanguage(sampleLanguage);

		oldStateFile.save(file);
		CurrentUiState loaded = new CurrentUiState();
		loaded.load(file);
		
		assertEquals("Wrong folder name?", sampleFolder, loaded.getCurrentFolder());
		assertEquals("Wrong sort tag?", sampleTag, loaded.getCurrentSortTag());
		assertEquals("Wrong sort dir?", sampleDir, loaded.getCurrentSortDirection());
		assertEquals("Wrong bulletin position?", sampleBulletinPosition, loaded.getCurrentBulletinPosition());
		assertEquals("Wrong Keyboard?", sampleDefaultKeyboardVirtual, loaded.isCurrentDefaultKeyboardVirtual());
		assertEquals("Wrong Date?", sampleDateFormat, loaded.getCurrentDateFormat());
		assertEquals("Wrong Language?", sampleLanguage, loaded.getCurrentLanguage());
		assertEquals("Wrong Initial PreviewSplitterPosition?", 100, loaded.getCurrentPreviewSplitterPosition());
		assertEquals("Wrong Initial FolderSplitterPosition?", 180, loaded.getCurrentFolderSplitterPosition());
	}

	public class BadVersionUiState extends CurrentUiState
	{
		public void save(File file) 
		{
			try
			{
				FileOutputStream outputStream = new FileOutputStream(file);
				DataOutputStream out = new DataOutputStream(outputStream);
				out.writeUTF("bad data");
				out.writeUTF("more bad data");
				out.close();
			}
			catch(Exception e)
			{
				System.out.println("CurrentUiState.save error: " + e);
			}
		}
	}	

	public void testBadUiStateFile() throws Exception
	{
		boolean sampleDefaultKeyboardVirtual = false;
		String sampleDateFormat = "dd/mm/yyyy";
		String sampleLanguage = "en";
		int samplePreviewSplitterPosition = 120;
		int sampleFolderSplitterPosition = 320;

		File file = createTempFile();
		BadVersionUiState badStateFile = new BadVersionUiState();

		badStateFile.setCurrentDefaultKeyboardVirtual(sampleDefaultKeyboardVirtual);
		badStateFile.setCurrentDateFormat(sampleDateFormat);
		badStateFile.setCurrentLanguage(sampleLanguage);
		badStateFile.setCurrentFolderSplitterPosition(sampleFolderSplitterPosition);
		badStateFile.setCurrentPreviewSplitterPosition(samplePreviewSplitterPosition);

		badStateFile.save(file);
		CurrentUiState loaded = new CurrentUiState();
		loaded.load(file);
		
		assertEquals("Didn't get Default Keyboard?", true, loaded.isCurrentDefaultKeyboardVirtual());
		assertEquals("Didn't get Default PreviewSplitterPosition?", 100, loaded.getCurrentPreviewSplitterPosition());
		assertEquals("Didn't get Default FolderSplitterPosition?", 180, loaded.getCurrentFolderSplitterPosition());
	}
}
