package org.martus.server.forclients;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.Packet;
import org.martus.common.UniversalId;
import org.martus.common.MartusCrypto.AuthorizationFailedException;
import org.martus.common.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.MartusCrypto.CryptoInitializationException;

public class MartusServerUtilities
{
	public static void saveZipFileToDatabase(Database db, String authorAccountId, File zipFile, MartusCrypto verifier)
		throws
			ZipException,
			IOException,
			Packet.InvalidPacketException,
			Packet.SignatureVerificationException,
			MartusServer.SealedPacketExistsException,
			MartusServer.DuplicatePacketException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException
	{
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(zipFile);
			MartusServerUtilities.validateZipFilePacketsForServerImport(db, authorAccountId, zip, verifier);
			MartusUtilities.importBulletinPacketsFromZipFileToDatabase(db, authorAccountId, zip, verifier);
		}
		finally
		{
			if(zip != null)
				zip.close();
		}
	}

	public static void validateZipFilePacketsForServerImport(Database db, String authorAccountId, ZipFile zip, MartusCrypto security)
		throws
			Packet.InvalidPacketException,
			IOException,
			Packet.SignatureVerificationException,
			MartusServer.SealedPacketExistsException,
			MartusServer.DuplicatePacketException,
			Packet.WrongAccountException,
			MartusCrypto.DecryptionException 
	{
		MartusUtilities.validateIntegrityOfZipFilePackets(authorAccountId, zip, security);

		Enumeration entries = zip.entries();
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, entry.getName());
			DatabaseKey trySealedKey = new DatabaseKey(uid);
			trySealedKey.setSealed();
			if(db.doesRecordExist(trySealedKey))
			{
				DatabaseKey newKey = MartusUtilities.createKeyWithHeaderStatus(header, uid);
				if(newKey.isDraft())
					throw new MartusServer.SealedPacketExistsException(entry.getName());
				else
					throw new MartusServer.DuplicatePacketException(entry.getName());
			}
		}
	}

	public static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, String passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}
}