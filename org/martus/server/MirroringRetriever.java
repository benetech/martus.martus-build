package org.martus.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.martus.common.BulletinRetrieverGatewayInterface;
import org.martus.common.Database;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.NetworkInterfaceConstants;
import org.martus.common.UniversalId;
import org.martus.common.Base64.InvalidBase64Exception;
import org.martus.common.MartusCrypto.MartusSignatureException;
import org.martus.common.MartusUtilities.ServerErrorException;

public class MirroringRetriever
{
	MirroringRetriever(Database databaseToUse, BulletinRetrieverGatewayInterface gatewayToUse, MartusCrypto securityToUse)
	{
		db = databaseToUse;
		gateway = gatewayToUse;
		security = securityToUse;
	}
	
	public void retrieveOneBulletin(UniversalId uid) throws InvalidBase64Exception, IOException, MartusSignatureException, ServerErrorException
	{
		File tempFile = File.createTempFile("$$$MirroringRetriever", null);
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);

		int chunkSize = NetworkInterfaceConstants.MAX_CHUNK_SIZE;
		int totalLength = MartusUtilities.retrieveBulletinZipToStream(uid, out, chunkSize, gateway, security, null, null);

		out.close();

		if(tempFile.length() != totalLength)
		{
			System.out.println("file=" + tempFile.length() + ", returned=" + totalLength);
			throw new ServerErrorException("totalSize didn't match data length");
		}

		saveFileToDatabase(uid, tempFile);			
		tempFile.delete();
	}
	
	void saveFileToDatabase(UniversalId uid, File tempFile) throws IOException
	{
		ZipFile zip = new ZipFile(tempFile);
		try
		{
			MartusUtilities.importBulletinPacketsFromZipFileToDatabase(db, uid.getAccountId(), zip, security);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("MirroringRetriever.saveFileToDatabase: " + e);
		}
		finally
		{
			zip.close();
		}
	}

	Database db;	
	BulletinRetrieverGatewayInterface gateway;
	MartusCrypto security;
}
