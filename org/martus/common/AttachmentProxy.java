package org.martus.common;

import java.io.File;

import org.martus.common.*;

public class AttachmentProxy
{
	public AttachmentProxy(File fileToAttach)
	{
		file = fileToAttach;
		label = file.getName();
	}
	
	public AttachmentProxy(UniversalId universalIdToUse, String labelToUse, byte[] sessionKeyBytes)
	{
		setUniversalIdAndSessionKey(universalIdToUse, sessionKeyBytes);
		label = labelToUse;
	}
	
	public AttachmentProxy(String labelToUse)
	{
		label = labelToUse;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public byte[] getSessionKeyBytes()
	{
		return keyBytes;
	}
	
	public void setUniversalIdAndSessionKey(UniversalId universalId, byte[] sessionKeyString)
	{
		uid = universalId;
		keyBytes = sessionKeyString;
		file = null;
	}
	
	public UniversalId getUniversalId()
	{
		return uid;
	}
	
	String label;
	File file;
	byte[] keyBytes;
	UniversalId uid;
}
