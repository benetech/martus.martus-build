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

package org.martus.client.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.common.*;
import org.martus.common.Base64;
import org.martus.common.Bulletin;
import org.martus.common.BulletinConstants;
import org.martus.common.BulletinZipUtilities;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.NetworkResponse;
import org.martus.common.Packet;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.CryptoException;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusCrypto.NoKeyPairException;
import org.martus.common.MartusUtilities.FileTooLargeException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;
import org.martus.common.Packet.WrongPacketTypeException;

public class BackgroundUploader
{
	public BackgroundUploader(MartusApp appToUse, ProgressMeterInterface progressMeterToUse)
	{
		app = appToUse;
		progressMeter = progressMeterToUse;
	}

	public UploadResult backgroundUpload() throws
		MartusApp.DamagedBulletinException
	{
		UploadResult uploadResult = new UploadResult();
		uploadResult.result = NetworkInterfaceConstants.OK;
	
		BulletinFolder folderOutbox = app.getFolderOutbox();
		BulletinFolder folderDraftOutbox = app.getFolderDraftOutbox();
		if(folderOutbox.getBulletinCount() > 0)
			uploadResult = backgroundUploadOneSealedBulletin(folderOutbox);
		else if(folderDraftOutbox.getBulletinCount() > 0)
			uploadResult = backgroundUploadOneDraftBulletin(folderDraftOutbox);
		else if(app.getConfigInfo().shouldContactInfoBeSentToServer())
			sendContactInfoToServer();
	
		if(uploadResult.isHopelesslyDamaged)
			throw new MartusApp.DamagedBulletinException(uploadResult.exceptionThrown);
		return uploadResult;
	}

	public String uploadBulletin(Bulletin b) throws
			InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException, CryptoException, FileNotFoundException, MartusSignatureException, FileTooLargeException, IOException
	{
		File tempFile = File.createTempFile("$$$MartusUploadBulletin", null);
		try
		{
			UniversalId uid = b.getUniversalId();

			Database db = app.getStore().getDatabase();
			DatabaseKey headerKey = DatabaseKey.createKey(uid, b.getStatus());
			MartusCrypto security = app.getSecurity();
			BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, tempFile, security);
			
			String tag = getUploadProgressTag(b);
			progressMeter.setStatusMessageTag(tag);
			return uploadBulletinZipFile(uid, tempFile);
		}
		finally
		{
			tempFile.delete();
		}
	}
	
	private String getUploadProgressTag(Bulletin b)
	{
		if(b.isDraft())
			return "UploadingDraftBulletin";
		else
			return "UploadingSealedBulletin";
	}
	
	private String uploadBulletinZipFile(UniversalId uid, File tempFile)
		throws
			FileTooLargeException,
			FileNotFoundException,
			IOException,
			MartusSignatureException
	{
		int totalSize = MartusUtilities.getCappedFileLength(tempFile);
		int offset = 0;
		byte[] rawBytes = new byte[app.serverChunkSize];
		FileInputStream inputStream = new FileInputStream(tempFile);
		String result = null;
		while(true)
		{
			if(progressMeter != null)
				progressMeter.updateProgressMeter(offset, totalSize);
			int chunkSize = inputStream.read(rawBytes);
			if(chunkSize <= 0)
				break;
			byte[] chunkBytes = new byte[chunkSize];
			System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);
		
			String authorId = uid.getAccountId();
			String bulletinLocalId = uid.getLocalId();
			String encoded = Base64.encode(chunkBytes);
		
			NetworkResponse response = app.getCurrentNetworkInterfaceGateway().putBulletinChunk(app.getSecurity(),
								authorId, bulletinLocalId, totalSize, offset, chunkSize, encoded);
			result = response.getResultCode();
			if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
				break;
			offset += chunkSize;
		}
		inputStream.close();
		return result;
	}

	BackgroundUploader.UploadResult uploadOneBulletin(
		BulletinFolder uploadFromFolder)
	{
		BackgroundUploader.UploadResult uploadResult = new BackgroundUploader.UploadResult();
	
		if(!app.isSSLServerAvailable())
			return uploadResult;
	
		int randomBulletin = new Random().nextInt(uploadFromFolder.getBulletinCount());
		Bulletin b = uploadFromFolder.getBulletinSorted(randomBulletin);
		uploadResult.uid = b.getUniversalId();
		try
		{
			uploadResult.result = uploadBulletin(b);
		}
		catch (Packet.InvalidPacketException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (Packet.WrongPacketTypeException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (Packet.SignatureVerificationException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusCrypto.DecryptionException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusUtilities.FileTooLargeException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusCrypto.NoKeyPairException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (FileNotFoundException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (MartusCrypto.MartusSignatureException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (MartusCrypto.CryptoException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (IOException e)
		{
			uploadResult.exceptionThrown = e.toString();
		}
		return uploadResult;
	}

	UploadResult backgroundUploadOneSealedBulletin(BulletinFolder uploadFromFolder) throws
		MartusApp.DamagedBulletinException
	{
		UploadResult uploadResult = uploadOneBulletin(uploadFromFolder);
		
		if(uploadResult.result != null)
		{
			if(uploadResult.result.equals(NetworkInterfaceConstants.OK) || uploadResult.result.equals(NetworkInterfaceConstants.DUPLICATE))
			{
				UniversalId uid = uploadResult.uid;
				Bulletin b = app.store.findBulletinByUniversalId(uid);
				uploadFromFolder.remove(uid);
				app.store.moveBulletin(b, uploadFromFolder, app.getFolderSent());
				app.store.saveFolders();
				app.resetLastUploadedTime();
				if(app.logUploads)
				{
					try
					{
						File file = new File(app.getUploadLogFilename());
						UnicodeWriter log = new UnicodeWriter(file, UnicodeWriter.APPEND);
						log.writeln(uid.getLocalId());
						log.writeln(app.getConfigInfo().getServerName());
						log.writeln(b.get(BulletinConstants.TAGTITLE));
						log.close();
						log = null;
					}
					catch(Exception e)
					{
						System.out.println("MartusApp.backgroundUpload: " + e);
					}
				}
			}
			return uploadResult;
		}
	
		if(uploadResult.isHopelesslyDamaged)
			app.moveBulletinToDamaged(uploadFromFolder, uploadResult.uid);
		return uploadResult;
	}

	UploadResult backgroundUploadOneDraftBulletin(BulletinFolder uploadFromFolder) throws
		MartusApp.DamagedBulletinException
	{
		UploadResult uploadResult = uploadOneBulletin(uploadFromFolder);
		
		if(uploadResult.result != null)
		{
			if(uploadResult.result.equals(NetworkInterfaceConstants.OK))
			{
				uploadFromFolder.remove(uploadResult.uid);
				app.getStore().saveFolders();
			}
			return uploadResult;
		}
	
		if(uploadResult.isHopelesslyDamaged)
			app.moveBulletinToDamaged(uploadFromFolder, uploadResult.uid);
		return uploadResult;
	}

	public String putContactInfoOnServer(Vector info)  throws
			MartusCrypto.MartusSignatureException
	{
		ClientSideNetworkGateway gateway = app.getCurrentNetworkInterfaceGateway();
		NetworkResponse response = gateway.putContactInfo(app.getSecurity(), app.getAccountId(), info);
		return response.getResultCode();
	}

	void sendContactInfoToServer()
	{
		if(!app.isSSLServerAvailable())
			return;
	
		ConfigInfo info = app.getConfigInfo();
		String result = "";
		try
		{
			result = putContactInfoOnServer(info.getContactInfo(app.security));
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			System.out.println("MartusApp.sendContactInfoToServer :" + e);
			return;
		}
		if(!result.equals(NetworkInterfaceConstants.OK))
		{
			System.out.println("MartusApp.sendContactInfoToServer failure:" + result);
			return;
		}
		System.out.println("Contact info successfully sent to server");
	
		try
		{
			info.setSendContactInfoToServer(false);
			app.saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			System.out.println("MartusApp:putContactInfoOnServer Failed to save configinfo locally:" + e);
		}
	}

	public static class UploadResult
	{
		public UniversalId uid;
		public String result;
		public String exceptionThrown;
		public boolean isHopelesslyDamaged;
	}

	MartusApp app;
	ProgressMeterInterface progressMeter;
}
