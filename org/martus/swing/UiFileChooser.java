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

package org.martus.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

//This implementation is needed because of a Java bug 
//which by clicking on a directory changes the file name 
//to be saved to that of the directory name.

public class UiFileChooser extends JFileChooser
{
	public UiFileChooser()
	{
		addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY,new DirectoryChangeListener());
		addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, new FileSelectedChangeListener());
	}

	class DirectoryChangeListener implements PropertyChangeListener 
	{
		public void propertyChange(PropertyChangeEvent e) 
		{
			processDirChanged(e);
		}
	}

	public void processDirChanged(PropertyChangeEvent e) 
	{
		if ( previouslySelectedFile == null )
			return; 

		File newSelectedFile = new File(getCurrentDirectory().getPath() + File.separator + previouslySelectedFile.getName());
		previouslySelectedFile = newSelectedFile;
		setSelectedFile(previouslySelectedFile);
	}

	class FileSelectedChangeListener implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent e) 
		{
			processFileSelected(e);
		}
	}
	
	public void processFileSelected(PropertyChangeEvent e) 
	{
		File selectedFile = getSelectedFile();
		if ( previouslySelectedFile != null && ( selectedFile == null || selectedFile.isDirectory()) ) 
			setSelectedFile(previouslySelectedFile);
		else 
			previouslySelectedFile = getSelectedFile();
	} 

	public File previouslySelectedFile = null;
}
