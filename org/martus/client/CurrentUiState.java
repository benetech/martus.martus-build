package org.martus.client;

import java.awt.Dimension;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class CurrentUiState implements Serializable
{
	public CurrentUiState()
	{
		currentFolderName = "";
		currentDateFormat = "";
		currentLanguage = "";
		currentSortTag = "";
		currentAppDimension = new Dimension();
		currentAppPosition = new Point();
		currentEditorDimension = new Dimension();
		currentEditorPosition = new Point();
		currentOperatingState = OPERATING_STATE_OK;
	}
	
	public void setCurrentFolder(String folderName)
	{
		currentFolderName = folderName;	
	}

	public String getCurrentFolder()
	{
		return currentFolderName;
	}

	public void setCurrentSortTag(String sortTag)
	{
		currentSortTag = sortTag;
	}

	public String getCurrentSortTag()
	{
		return currentSortTag;
	}
	
	public void setCurrentSortDirection(int sortDirection)
	{
		currentSortDirection = sortDirection;
	}

	public int getCurrentSortDirection()
	{
		return currentSortDirection;
	}

	public void setCurrentBulletinPosition(int currentPosition)
	{
		currentBulletinPosition = currentPosition;
	}

	public int getCurrentBulletinPosition()
	{
		return currentBulletinPosition;
	}

	public boolean isCurrentDefaultKeyboardVirtual() 
	{
		return currentDefaultKeyboardIsVirtual;
	}
	
	public void setCurrentDefaultKeyboardVirtual(boolean on)
	{
		currentDefaultKeyboardIsVirtual = on;	
	}

	public String getCurrentDateFormat() 
	{
		return currentDateFormat;
	}

	public String getCurrentLanguage() 
	{
		return currentLanguage;
	}

	public void setCurrentDateFormat(String currentDateFormat) 
	{
		this.currentDateFormat = currentDateFormat;
	}

	public void setCurrentLanguage(String currentLanguage) 
	{
		this.currentLanguage = currentLanguage;
	}

	public int getCurrentFolderSplitterPosition() 
	{
		return currentFolderSplitterPosition;
	}

	public void setCurrentFolderSplitterPosition(int currentFolderSplitterPosition) 
	{
		this.currentFolderSplitterPosition = currentFolderSplitterPosition;
	}

	public int getCurrentPreviewSplitterPosition() 
	{
		return currentPreviewSplitterPosition;
	}

	public void setCurrentPreviewSplitterPosition(int currentPreviewSplitterPosition) 
	{
		this.currentPreviewSplitterPosition = currentPreviewSplitterPosition;
	}

	public Dimension getCurrentAppDimension() 
	{
		return currentAppDimension;
	}

	public boolean isCurrentAppMaximized() 
	{
		return currentAppMaximized;
	}

	public Point getCurrentAppPosition() 
	{
		return currentAppPosition;
	}

	public Dimension getCurrentEditorDimension() 
	{
		return currentEditorDimension;
	}

	public boolean isCurrentEditorMaximized() 
	{
		return currentEditorMaximized;
	}

	public Point getCurrentEditorPosition() 
	{
		return currentEditorPosition;
	}

	public String getCurrentOperatingState() 
	{
		return currentOperatingState;
	}

	public void setCurrentAppDimension(Dimension currentAppDimension) 
	{
		this.currentAppDimension = currentAppDimension;
	}

	public void setCurrentAppMaximized(boolean currentAppMaximized) 
	{
		this.currentAppMaximized = currentAppMaximized;
	}

	public void setCurrentAppPosition(Point currentAppPosition) 
	{
		this.currentAppPosition = currentAppPosition;
	}

	public void setCurrentEditorDimension(Dimension currentEditorDimension) 
	{
		this.currentEditorDimension = currentEditorDimension;
	}

	public void setCurrentEditorMaximized(boolean currentEditorMaximized) 
	{
		this.currentEditorMaximized = currentEditorMaximized;
	}

	public void setCurrentEditorPosition(Point currentEditorPosition) 
	{
		this.currentEditorPosition = currentEditorPosition;
	}

	public void setCurrentOperatingState(String currentOperatingState) 
	{
		this.currentOperatingState = currentOperatingState;
	}

	public void save(File file)
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(file);
			DataOutputStream out = new DataOutputStream(outputStream);
			out.writeInt(uiStateFirstIntegerInFile);
			out.writeShort(VERSION);
			out.writeUTF(currentFolderName);
			out.writeUTF(currentSortTag);
			out.writeInt(currentSortDirection);
			out.writeInt(currentBulletinPosition);
			out.writeBoolean(currentDefaultKeyboardIsVirtual);
			out.writeUTF(currentDateFormat);
			out.writeUTF(currentLanguage);

			out.writeInt(currentPreviewSplitterPosition);
			out.writeInt(currentFolderSplitterPosition);

			out.writeInt(currentAppDimension.height);
			out.writeInt(currentAppDimension.width);
			out.writeInt(currentAppPosition.x);
			out.writeInt(currentAppPosition.y);
			out.writeBoolean(currentAppMaximized);

			out.writeInt(currentEditorDimension.height);
			out.writeInt(currentEditorDimension.width);
			out.writeInt(currentEditorPosition.x);
			out.writeInt(currentEditorPosition.y);
			out.writeBoolean(currentEditorMaximized);

			out.writeUTF(currentOperatingState);
						
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			System.out.println("CurrentUiState.save error: " + e);
		}
	}

	
	public void load(File file)
	{
		try
		{
			FileInputStream inputStream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(inputStream);
			if(isCorrectFileFormat(in))
			{
				short version = in.readShort();
				currentFolderName = in.readUTF();
				currentSortTag = in.readUTF();
				currentSortDirection = in.readInt();
				currentBulletinPosition = in.readInt();
				currentDefaultKeyboardIsVirtual = in.readBoolean();
				currentDateFormat = in.readUTF();
				currentLanguage = in.readUTF();
				if(version > 1)
				{
					currentPreviewSplitterPosition = in.readInt(); 
					currentFolderSplitterPosition = in.readInt();
					if(version > 2)
					{
						currentAppDimension.height = in.readInt();
						currentAppDimension.width = in.readInt();
						currentAppPosition.x = in.readInt();
						currentAppPosition.y = in.readInt();
						currentAppMaximized = in.readBoolean();
	
						currentEditorDimension.height = in.readInt();
						currentEditorDimension.width = in.readInt();
						currentEditorPosition.x = in.readInt();
						currentEditorPosition.y = in.readInt();
						currentEditorMaximized = in.readBoolean();
						if(version > 3)
						{
							currentOperatingState = in.readUTF();
						}
					}
				}
			}		
			in.close();
		}
		catch (Exception e)
		{
			//System.out.println("CurrentUiState.load " + e);
		}
	}
	
	private boolean isCorrectFileFormat(DataInputStream in) throws IOException
	{
		int firstIntegerIn = 0;
		firstIntegerIn = in.readInt();
		return (firstIntegerIn == uiStateFirstIntegerInFile);	
	}
	
	private static final short VERSION = 4;
	//Initial Version
	protected static int uiStateFirstIntegerInFile = 2002;
	protected String currentFolderName;
	protected String currentSortTag;
	protected int currentSortDirection;
	protected int currentBulletinPosition;
	protected boolean currentDefaultKeyboardIsVirtual = true;
	protected String currentDateFormat;
	protected String currentLanguage;

	//Version 1
	protected int currentPreviewSplitterPosition = 100;
	protected int currentFolderSplitterPosition = 180;

	//Version 2
	protected Dimension currentAppDimension;
	protected Point currentAppPosition;
	protected boolean currentAppMaximized;

	//Version 3
	protected Dimension currentEditorDimension;
	protected Point currentEditorPosition;
	protected boolean currentEditorMaximized;
	
	//Version 4
	protected String currentOperatingState;
	static final String OPERATING_STATE_OK = "OK";
	static final String OPERATING_STATE_UNKNOWN = "UNKNOWN";
	static final String OPERATING_STATE_BAD = "BAD";

}
