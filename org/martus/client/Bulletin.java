package org.martus.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.AttachmentPacket;
import org.martus.common.AttachmentProxy;
import org.martus.common.Base64;
import org.martus.common.BulletinConstants;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FieldDataPacket;
import org.martus.common.MartusConstants;
import org.martus.common.MartusCrypto;
import org.martus.common.Packet;
import org.martus.common.UnicodeWriter;
import org.martus.common.UniversalId;
import org.martus.common.ZipEntryInputStream;
import org.martus.common.MartusCrypto.CryptoException;
import org.martus.common.MartusCrypto.DecryptionException;
import org.martus.common.MartusCrypto.EncryptionException;
import org.martus.common.MartusCrypto.NoKeyPairException;
import org.martus.common.Packet.InvalidPacketException;
import org.martus.common.Packet.SignatureVerificationException;
import org.martus.common.Packet.WrongAccountException;
import org.martus.common.Packet.WrongPacketTypeException;


public class Bulletin implements BulletinConstants
{
	
	public static class DamagedBulletinException extends Exception
	{
	}

	public Bulletin(BulletinStore bulletinStore)
	{
		store = bulletinStore;
		String accountId = "";
		if(store != null)
			accountId = store.getAccountId();
		UniversalId headerUid = BulletinHeaderPacket.createUniversalId(accountId);
		UniversalId dataUid = FieldDataPacket.createUniversalId(accountId);
		UniversalId privateDataUid = FieldDataPacket.createUniversalId(accountId);

		createMemberVariables(headerUid, dataUid, privateDataUid);
		
		clear();
	}

	public Bulletin(Bulletin other) throws 
		IOException,
		MartusCrypto.EncryptionException
	{
		BulletinHeaderPacket otherHeader = other.getBulletinHeaderPacket();
		FieldDataPacket otherData = other.getFieldDataPacket();
		FieldDataPacket otherPrivateData = other.getPrivateFieldDataPacket();
		
		store = other.getStore();
		UniversalId headerUid = otherHeader.getUniversalId();
		UniversalId dataUid = otherData.getUniversalId();
		UniversalId privateDataUid = otherPrivateData.getUniversalId();

		createMemberVariables(headerUid, dataUid, privateDataUid);
		
		pullDataFrom(other);
	}

	public BulletinStore getStore()
	{
		return store;
	}

	public void setStore(BulletinStore newStore)
	{
		store = newStore;
	}
	
	public UniversalId getUniversalId()
	{
		return getBulletinHeaderPacket().getUniversalId();
	}
	
	public String getUniversalIdString()
	{
		return getUniversalId().toString();
	}
	
	public String getAccount()
	{
		return getBulletinHeaderPacket().getAccountId();
	}

	public String getLocalId()
	{
		return getBulletinHeaderPacket().getLocalId();
	}
	
	public boolean isValid()
	{
		return isValidFlag;
	}

	public boolean isFromDatabase()
	{
		return (getDatabase() != null);
	}
	
	public Database getDatabase()
	{
		return db;
	}
	
	public long getLastSavedTime()
	{
		return getBulletinHeaderPacket().getLastSavedTime();
	}

	public boolean mustEncryptPublicData()
	{
		return store.mustEncryptPublicData();	
	}

	public boolean isDraft()
	{
		return getStatus().equals(STATUSDRAFT);
	}

	public boolean isSealed()
	{
		return getStatus().equals(STATUSSEALED);
	}

	public void setDraft()
	{
		setStatus(STATUSDRAFT);
	}

	public void setSealed()
	{
		setStatus(STATUSSEALED);
	}

	public void setStatus(String newStatus)
	{
		getBulletinHeaderPacket().setStatus(newStatus);
	}

	public String getStatus()
	{
		return getBulletinHeaderPacket().getStatus();
	}

	public void set(String fieldName, String value)
	{
		if(isStandardField(fieldName))
			fieldData.set(fieldName, value);
		else
			privateFieldData.set(fieldName, value);
	}

	public String get(String fieldName)
	{
		if(isStandardField(fieldName))
			return fieldData.get(fieldName);
		else
			return privateFieldData.get(fieldName);
	}

	public void addPublicAttachment(AttachmentProxy a) throws 
		IOException,
		MartusCrypto.EncryptionException
	{
		BulletinHeaderPacket bhp = getBulletinHeaderPacket();
		File rawFile = a.getFile();
		if(rawFile != null)
		{
			byte[] sessionKeyBytes = getStore().getSignatureGenerator().createSessionKey();
			AttachmentPacket ap = new AttachmentPacket(getAccount(), sessionKeyBytes, rawFile, store.getSignatureVerifier());
			bhp.addPublicAttachmentLocalId(ap.getLocalId());
			pendingPublicAttachments.add(ap);
			a.setUniversalIdAndSessionKey(ap.getUniversalId(), sessionKeyBytes);
		}
		else
		{
			bhp.addPublicAttachmentLocalId(a.getUniversalId().getLocalId());
		}
				
		getFieldDataPacket().addAttachment(a);
	}
	
	public void addPrivateAttachment(AttachmentProxy a) throws 
		IOException,
		MartusCrypto.EncryptionException
	{
		BulletinHeaderPacket bhp = getBulletinHeaderPacket();
		File rawFile = a.getFile();
		if(rawFile != null)
		{
			byte[] sessionKeyBytes = getStore().getSignatureGenerator().createSessionKey();
			AttachmentPacket ap = new AttachmentPacket(getAccount(), sessionKeyBytes, rawFile, store.getSignatureVerifier());
			bhp.addPrivateAttachmentLocalId(ap.getLocalId());
			pendingPrivateAttachments.add(ap);
			a.setUniversalIdAndSessionKey(ap.getUniversalId(), sessionKeyBytes);
		}
		else
		{
			bhp.addPrivateAttachmentLocalId(a.getUniversalId().getLocalId());
		}
				
		getPrivateFieldDataPacket().addAttachment(a);
	}

	public AttachmentProxy[] getPublicAttachments()
	{
		return getFieldDataPacket().getAttachments();
	}

	public AttachmentProxy[] getPrivateAttachments()
	{
		return getPrivateFieldDataPacket().getAttachments();
	}
	
	public void extractAttachmentToFile(AttachmentProxy a, MartusCrypto verifier, File destFile) throws
		IOException, 
		Base64.InvalidBase64Exception, 
		InvalidPacketException,
		SignatureVerificationException,
		WrongPacketTypeException,
		MartusCrypto.CryptoException
	{
		UniversalId uid = a.getUniversalId();
		byte[] sessionKeyBytes = a.getSessionKeyBytes();
		DatabaseKey key = new DatabaseKey(uid);
		InputStream xmlIn = getDatabase().openInputStream(key, verifier);
		AttachmentPacket.exportRawFileFromXml(xmlIn, sessionKeyBytes, verifier, destFile);
	}

	public void clear()
	{
		getBulletinHeaderPacket().clearAttachments();
		getFieldDataPacket().clearAll();
		getPrivateFieldDataPacket().clearAll();
		pendingPublicAttachments.clear();
		pendingPrivateAttachments.clear();
		set(TAGENTRYDATE, getToday());
		set(TAGEVENTDATE, getFirstOfThisYear());
		setDraft();
	}
	
	public void clearPublicAttachments()
	{
		getBulletinHeaderPacket().removeAllPublicAttachments();
		getFieldDataPacket().clearAttachments();
	}

	public void clearPrivateAttachments()
	{
		getBulletinHeaderPacket().removeAllPrivateAttachments();
		getPrivateFieldDataPacket().clearAttachments();
	}

	public boolean matches(SearchTreeNode node)
	{
		if(node.getOperation() == node.VALUE)
			return contains(node.getValue());

		if(node.getOperation() == node.AND)
			return matches(node.getLeft()) && matches(node.getRight());

		if(node.getOperation() == node.OR)
			return matches(node.getLeft()) || matches(node.getRight());

		return false;
	}

	private boolean contains(String lookFor)
	{
		String fields[] = fieldData.getFieldTags();
		String lookForLowerCase = lookFor.toLowerCase();
		for(int f = 0; f < fields.length; ++f)
		{
			String contents = get(fields[f]).toLowerCase();
			if(contents.indexOf(lookForLowerCase) >= 0)
				return true;
		}
		return false;
	}

	public void save()
	{
		store.saveBulletin(this);
	}
	
	protected void saveToDatabase(Database db) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		MartusCrypto signer = store.getSignatureGenerator();
		
		UniversalId uid = getUniversalId();
		BulletinHeaderPacket oldBhp = new BulletinHeaderPacket(uid);
		DatabaseKey key = new DatabaseKey(uid);
		boolean bulletinAlreadyExisted = false;
		try
		{
			if(db.doesRecordExist(key))
			{
				InputStream in = db.openInputStream(key, signer);
				oldBhp.loadFromXml(in, signer);
				bulletinAlreadyExisted = true;
			}
		}
		catch(Exception ignoreItBecauseWeCantDoAnythingAnyway)
		{
			//e.printStackTrace();
			//System.out.println("Bulletin.saveToDatabase: " + e);
		}
		
		BulletinHeaderPacket bhp = getBulletinHeaderPacket();

		FieldDataPacket publicDataPacket = getFieldDataPacket();
		boolean shouldEncryptPublicData = (isDraft() || bhp.isAllPrivate());
		publicDataPacket.setEncrypted(shouldEncryptPublicData);
			
		byte[] dataSig = writePacketToDatabase(publicDataPacket, db, signer);
		bhp.setFieldDataSignature(dataSig);

		byte[] privateDataSig = writePacketToDatabase(getPrivateFieldDataPacket(), db, signer);
		bhp.setPrivateFieldDataSignature(privateDataSig);

		for(int i = 0; i < pendingPublicAttachments.size(); ++i)
		{
			// TODO: Should the bhp also remember attachment sigs?
			Packet packet = (Packet)pendingPublicAttachments.get(i);
			writePacketToDatabase(packet, db, signer);
		}

		for(int i = 0; i < pendingPrivateAttachments.size(); ++i)
		{
			// TODO: Should the bhp also remember attachment sigs?
			Packet packet = (Packet)pendingPrivateAttachments.get(i);
			writePacketToDatabase(packet, db, signer);
		}

		bhp.updateLastSavedTime();
		writePacketToDatabase(bhp, db, signer);

		if(bulletinAlreadyExisted)
		{
			
			String[] oldPublicAttachmentIds = oldBhp.getPublicAttachmentIds();
			String[] newPublicAttachmentIds = bhp.getPublicAttachmentIds();
			deleteRemovedPackets(db, oldPublicAttachmentIds, newPublicAttachmentIds);

			String[] oldPrivateAttachmentIds = oldBhp.getPrivateAttachmentIds();
			String[] newPrivateAttachmentIds = bhp.getPrivateAttachmentIds();
			deleteRemovedPackets(db, oldPrivateAttachmentIds, newPrivateAttachmentIds);
		}
	}

	protected void deleteRemovedPackets(Database db, String[] oldIds, String[] newIds) 
	{
		for(int oldIndex = 0; oldIndex < oldIds.length; ++oldIndex)
		{
			String oldLocalId = oldIds[oldIndex];
			if(!isStringInArray(newIds, oldLocalId))
			{
				UniversalId auid = UniversalId.createFromAccountAndLocalId(getAccount(), oldLocalId);
				db.discardRecord(new DatabaseKey(auid));
			}
		}
	}

	public String getHQPublicKey()
	{
		return getBulletinHeaderPacket().getHQPublicKey();	
	}

	public void setHQPublicKey(String key)
	{
		getBulletinHeaderPacket().setHQPublicKey(key);	
		getFieldDataPacket().setHQPublicKey(key);
		getPrivateFieldDataPacket().setHQPublicKey(key);
	}
	
	protected static boolean isStringInArray(String[] array, String lookFor) 
	{
		for(int newIndex = 0; newIndex < array.length; ++newIndex)
		{
			if(lookFor.equals(array[newIndex]))
				return true;
		}
		
		return false;
	}

	byte[] writePacketToDatabase(Packet packet, Database db, MartusCrypto signer) throws 
			IOException,
			MartusCrypto.CryptoException
	{
		return packet.writeXmlToDatabase(db, mustEncryptPublicData(), signer);
	}
	
	void removeBulletinFromDatabase(Database db, MartusCrypto crypto)
	{
		DatabaseKey key = new DatabaseKey(getUniversalId());
		if(!db.doesRecordExist(key))
			return;
			
		try
		{
			String xml = db.readRecord(key, crypto);
			byte[] bytes = xml.getBytes("UTF-8");
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
	
			BulletinHeaderPacket oldHeader = new BulletinHeaderPacket("");
			oldHeader.loadFromXml(in, store.getSignatureVerifier());
			
			String oldDataId = oldHeader.getFieldDataPacketId();
			if(oldDataId != null)
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(getAccount(), oldDataId);
				db.discardRecord(new DatabaseKey(uid));
			}
				
			String oldPrivateDataId = oldHeader.getPrivateFieldDataPacketId();
			if(oldPrivateDataId != null)
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(getAccount(), oldPrivateDataId);
				db.discardRecord(new DatabaseKey(uid));
			}
			AttachmentProxy[] attachments = getPublicAttachments();
			for(int i = 0 ; i < attachments.length ; ++i)
			{
				UniversalId uid = attachments[i].getUniversalId();
				db.discardRecord(new DatabaseKey(uid));
			}

			db.discardRecord(key);
		}
		catch(Exception e)
		{
			//System.out.println("removeBulletinFromDatabase: " + e);
		}
	}

	public static Bulletin loadFromDatabase(BulletinStore store, DatabaseKey key) throws 
		IOException,
		DamagedBulletinException,
		MartusCrypto.NoKeyPairException
	{
		return loadFromDatabase(store, key, store.getSignatureVerifier());
	}

	protected static Bulletin loadFromDatabase(BulletinStore store, DatabaseKey key, MartusCrypto verifier) throws 
			IOException,
			DamagedBulletinException,
			NoKeyPairException 
	{
		Bulletin b = new Bulletin(store);
		b.clear();
		b.isValidFlag = true;
		b.db = store.getDatabase();
		
		boolean isHeaderValid = true;

		BulletinHeaderPacket headerPacket = b.getBulletinHeaderPacket();
		DatabaseKey headerKey = key;
		b.loadAnotherPacket(headerPacket, headerKey, null, verifier);
		if(!b.isValid())
			isHeaderValid = false;
		
		if(isHeaderValid)
		{
			FieldDataPacket dataPacket = b.getFieldDataPacket();
			FieldDataPacket privateDataPacket = b.getPrivateFieldDataPacket();
		
			DatabaseKey dataKey = b.getDatabaseKeyForLocalId(headerPacket.getFieldDataPacketId());
	
			byte[] dataSig = headerPacket.getFieldDataSignature();
			b.loadAnotherPacket(dataPacket, dataKey, dataSig, verifier);
		
			DatabaseKey privateDataKey = b.getDatabaseKeyForLocalId(headerPacket.getPrivateFieldDataPacketId());
			byte[] privateDataSig = headerPacket.getPrivateFieldDataSignature();
			b.loadAnotherPacket(privateDataPacket, privateDataKey, privateDataSig, verifier);

		}

		if(b.isValid())
		{
			b.setHQPublicKey(headerPacket.getHQPublicKey());
		}
		else
		{
			b.setHQPublicKey("");
			if(!isHeaderValid)
			{
				//System.out.println("Bulletin.loadFromDatabase: Header invalid");
				throw new DamagedBulletinException();
			}
		}

		return b;
	}

	void loadAnotherPacket(Packet packet, DatabaseKey key, byte[] expectedSig, MartusCrypto verifier) throws 
			IOException, 
			NoKeyPairException 
	{
		packet.setUniversalId(key.getUniversalId());
		try
		{
			InputStream in = db.openInputStream(key, verifier);
			if(in == null)
			{
				isValidFlag = false;
				throw new IOException("Packet not found");
			}
			packet.loadFromXml(in, expectedSig, verifier);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			isValidFlag = false;
			throw e;
		}
		catch(NoKeyPairException e)
		{
			isValidFlag = false;
			throw e;
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			isValidFlag = false;
		}
	}

	DatabaseKey getDatabaseKeyForLocalId(String localId) 
	{
		UniversalId uidFdp = UniversalId.createFromAccountAndLocalId(getAccount(), localId);
		return new DatabaseKey(uidFdp);
	}

	static void importZipFileToStoreWithSameUids(ZipFile zip, BulletinStore store) throws 
		IOException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException, 
		WrongAccountException, 
		DecryptionException
	{
		importFileToDatabaseWithSameUids(zip, store.getDatabase(), store.getSignatureVerifier());
	}

	static UniversalId importZipFileToStoreWithNewUids(File inputFile, BulletinStore store) throws 
		IOException, 
		EncryptionException, 
		CryptoException 
	{
		Database db = store.getDatabase();
		MartusCrypto verifier = store.getSignatureVerifier();

		Bulletin original = store.createEmptyBulletin();
		original.loadFromFile(inputFile, verifier);
		Bulletin imported = store.createEmptyBulletin();
		imported.pullDataFrom(original);
		imported.saveToDatabase(db);
		return imported.getUniversalId();
	}

	static void verifyAllPacketsInZip(ZipFile zip, MartusCrypto verifier) throws 
		IOException, 
		InvalidPacketException, 
		SignatureVerificationException, 
		DecryptionException, 
		WrongAccountException 
	{
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, verifier);
		String dataLocalId = header.getFieldDataPacketId();
		String privateDataLocalId = header.getPrivateFieldDataPacketId();
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			byte[] expectedSig = null;
			ZipEntry entry = (ZipEntry)entries.nextElement();
			String localId = entry.getName();
			if(localId.equals(dataLocalId))
				expectedSig = header.getFieldDataSignature();
			else if(localId.equals(privateDataLocalId))
				expectedSig = header.getPrivateFieldDataSignature();
				
			ZipEntryInputStream in = new ZipEntryInputStream(zip, entry);
			//TODO: should also pass in expectedsig!
			Packet.validateXml(in, header.getAccountId(), entry.getName(), expectedSig, verifier);
		}
	}

	static void importFileToDatabaseWithSameUids(ZipFile zip, Database db, MartusCrypto verifier) throws 
		IOException, 
		InvalidPacketException, 
		SignatureVerificationException, 
		WrongAccountException, 
		DecryptionException 
	{
		verifyAllPacketsInZip(zip, verifier);
		Enumeration entries = zip.entries();
		entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			InputStream in = new ZipEntryInputStream(zip, entry);
			String oldAccountId = BulletinHeaderPacket.loadFromZipFile(zip, verifier).getAccountId();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(oldAccountId, entry.getName());
			db.writeRecord(new DatabaseKey(uid), in);
		}
	}

	public void loadFromFile(File inputFile, MartusCrypto verifier) throws IOException
	{
		clear();
		ZipFile zip = new ZipFile(inputFile);
		try
		{
	
			BulletinHeaderPacket header = getBulletinHeaderPacket();
	
			ZipEntry headerEntry = null;
			Enumeration entries = zip.entries();
			while(entries.hasMoreElements())
				headerEntry = (ZipEntry)entries.nextElement();
			InputStream headerIn = new ZipEntryInputStream(zip, headerEntry);
			try
			{
				header.loadFromXml(headerIn, verifier);
				if(!header.getLocalId().equals(headerEntry.getName()))
					throw new IOException("Misnamed header entry");
			}
			catch(Exception e)
			{
				throw new IOException(e.getMessage());
			}
			finally
			{
				headerIn.close();
			}
			
			FieldDataPacket data = getFieldDataPacket();
	
			entries = zip.entries();
			ZipEntry dataEntry = zip.getEntry(header.getFieldDataPacketId());
			if(dataEntry == null)
				throw new IOException("Data packet not found");
			InputStream dataIn = new ZipEntryInputStream(zip, dataEntry);
			try
			{
				data.loadFromXml(dataIn, header.getFieldDataSignature(), verifier);
			}
			catch(DecryptionException e)
			{
				//TODO mark bulletin as not complete	
			}
			catch(Exception e)
			{
				throw new IOException(e.getMessage());
			}
			finally
			{
				dataIn.close();
			}
			
			FieldDataPacket privateData = getPrivateFieldDataPacket();
	
			entries = zip.entries();
			ZipEntry privateDataEntry = zip.getEntry(header.getPrivateFieldDataPacketId());
			if(privateDataEntry == null)
				throw new IOException("Private data packet not found");
			InputStream privateDataIn = new ZipEntryInputStream(zip, privateDataEntry);
			try
			{
				privateData.loadFromXml(privateDataIn, header.getPrivateFieldDataSignature(), verifier);
			}
			catch(DecryptionException e)
			{
				//TODO Mark bulletin as not complete
			}
			catch(Exception e)
			{
				System.out.println(e);
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
			finally
			{
				privateDataIn.close();
			}
			
			AttachmentProxy[] attachments = getPublicAttachments();
			clearPublicAttachments();
			for(int i=0; i < attachments.length; ++i)
				addPublicAttachment(extractZipAttachmentToFileProxy(verifier, zip, attachments[i]));

			AttachmentProxy[] attachmentsPrivate = getPrivateAttachments();
			clearPrivateAttachments();
			for(int i=0; i < attachmentsPrivate.length; ++i)
				addPrivateAttachment(extractZipAttachmentToFileProxy(verifier, zip, attachmentsPrivate[i]));
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			zip.close();
		}
		setHQPublicKey(header.getHQPublicKey());
	}

	public AttachmentProxy extractZipAttachmentToFileProxy(MartusCrypto verifier, ZipFile zip, AttachmentProxy attachment) throws 
	IOException 
	{
		String localId = attachment.getUniversalId().getLocalId();
		byte[] sessionKeyBytes = attachment.getSessionKeyBytes();
		ZipEntry attachmentEntry = zip.getEntry(localId);
		if(attachmentEntry == null)
			throw new IOException("Attachment packet not found: " + localId);
		InputStream attachmentIn = new ZipEntryInputStream(zip, attachmentEntry);
		try
		{
			File tempFile = File.createTempFile("$$$MartusImportAttachment", null);
			tempFile.deleteOnExit();
			AttachmentPacket.exportRawFileFromXml(attachmentIn, sessionKeyBytes, verifier, tempFile);
			AttachmentProxy ap = new AttachmentProxy(tempFile);
			return ap;
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			attachmentIn.close();
		}
	}

	public void extractZipAttachmentsToFileProxies(MartusCrypto verifier, ZipFile zip, AttachmentProxy[] attachments) throws IOException {
		for(int i=0; i < attachments.length; ++i)
		{
			String localId = attachments[i].getUniversalId().getLocalId();
			byte[] sessionKeyBytes = attachments[i].getSessionKeyBytes();
			ZipEntry attachmentEntry = zip.getEntry(localId);
			if(attachmentEntry == null)
				throw new IOException("Attachment packet not found: " + localId);
			InputStream attachmentIn = new ZipEntryInputStream(zip, attachmentEntry);
			try
			{
				File tempFile = File.createTempFile("$$$MartusImportAttachment", null);
				tempFile.deleteOnExit();
				AttachmentPacket.exportRawFileFromXml(attachmentIn, sessionKeyBytes, verifier, tempFile);
				AttachmentProxy ap = new AttachmentProxy(tempFile);
				addPublicAttachment(ap);
			}
			catch(Exception e)
			{
				throw new IOException(e.getMessage());
			}
			finally
			{
				attachmentIn.close();
			}
		}
		
	}

	public void loadFromZipString(String zipString) throws IOException, Base64.InvalidBase64Exception
	{
		File tempFile = null;
		try
		{
			tempFile = Base64.decodeToTempFile(zipString);
			loadFromFile(tempFile, store.getSignatureVerifier());
		}
		finally
		{
			if(tempFile != null)
				tempFile.delete();
		}
	}
	
	public boolean isStandardField(String fieldName)
	{
		return getFieldDataPacket().fieldExists(fieldName);
	}

	public boolean isPrivateField(String fieldName)
	{
		return getPrivateFieldDataPacket().fieldExists(fieldName);
	}

	public int getFieldCount()
	{
		return fieldData.getFieldCount();
	}

	public static String[] getStandardFieldNames()
	{
		return new String[]
		{
			TAGLANGUAGE, 
			
			TAGAUTHOR, TAGTITLE, TAGLOCATION, TAGKEYWORDS,
			TAGEVENTDATE, TAGENTRYDATE, 
			TAGSUMMARY, TAGPUBLICINFO, 
		};
	}

	public static String[] getPrivateFieldNames()
	{
		return new String[]
		{
			TAGPRIVATEINFO,
		};
	}

	public static int getFieldType(String fieldName)
	{
		String lookFor = fieldName.toLowerCase();

		if(lookFor.equals(TAGSUMMARY) ||
				lookFor.equals(TAGPUBLICINFO) ||
				lookFor.equals(TAGPRIVATEINFO) )
			return MULTILINE;

		if(lookFor.equals(TAGEVENTDATE) ||
				lookFor.equals(TAGENTRYDATE) )
			return DATE;

		if(lookFor.equals(TAGLANGUAGE))
			return CHOICE;

		return NORMAL;
	}

	public static boolean isFieldEncrypted(String fieldName)
	{
		String lookFor = fieldName.toLowerCase();

		if(lookFor.equals(TAGPRIVATEINFO))
			return true;

		return false;
	}

	public boolean isAllPrivate()
	{
		return getBulletinHeaderPacket().isAllPrivate();
	}
	
	public void setAllPrivate(boolean newValue)
	{
		getBulletinHeaderPacket().setAllPrivate(newValue);
	}

	public void pullDataFrom(Bulletin other) throws 
		IOException,
		MartusCrypto.EncryptionException
	{
		this.clear();
		
		setStatus(other.getStatus());
		setAllPrivate(other.isAllPrivate());
		
		{
			String fields[] = fieldData.getFieldTags();
			for(int f = 0; f < fields.length; ++f)
			{
				set(fields[f], other.get(fields[f]));
			}
		}
		
		{
			String privateFields[] = privateFieldData.getFieldTags();
			for(int f = 0; f < privateFields.length; ++f)
			{
				set(privateFields[f], other.get(privateFields[f]));
			}
		}

		AttachmentProxy[] attachmentPublicProxies = other.getPublicAttachments();
		for(int aIndex = 0; aIndex < attachmentPublicProxies.length; ++aIndex)
		{
			addPublicAttachment(attachmentPublicProxies[aIndex]);
		}

		AttachmentProxy[] attachmentPrivateProxies = other.getPrivateAttachments();
		for(int aIndex = 0; aIndex < attachmentPrivateProxies.length; ++aIndex)
		{
			addPrivateAttachment(attachmentPrivateProxies[aIndex]);
		}

		pendingPublicAttachments.addAll(other.pendingPublicAttachments);
		pendingPrivateAttachments.addAll(other.pendingPrivateAttachments);
	}

	private String getFirstOfThisYear()
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.MONTH, 0);
		cal.set(GregorianCalendar.DATE, 1);
		DateFormat df = getStoredDateFormat();
		return df.format(cal.getTime());
	}

	public static String getToday()
	{
		DateFormat df = getStoredDateFormat();
		return df.format(new Date());
	}

	public static DateFormat getStoredDateFormat()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setLenient(false);
		return df;
	}
	
	public BulletinHeaderPacket getBulletinHeaderPacket()
	{
		return header;
	}
	
	public FieldDataPacket getFieldDataPacket()
	{
		return fieldData;
	}

	public FieldDataPacket getPrivateFieldDataPacket()
	{
		return privateFieldData;
	}

	private void createMemberVariables(UniversalId headerUid, UniversalId dataUid, UniversalId privateDataUid) 
	{
		isValidFlag = true;
		fieldData = new FieldDataPacket(dataUid, getStandardFieldNames());
		privateFieldData = new FieldDataPacket(privateDataUid, getPrivateFieldNames());
		privateFieldData.setEncrypted(true);
		header = new BulletinHeaderPacket(headerUid);
		header.setFieldDataPacketId(dataUid.getLocalId());
		header.setPrivateFieldDataPacketId(privateDataUid.getLocalId());
		pendingPublicAttachments = new Vector();
		pendingPrivateAttachments = new Vector();
	}
	
	private Database db;
	private boolean encryptedFlag;
	private boolean isFromDatabase;
	private boolean isValidFlag;
	private BulletinStore store;
	private BulletinHeaderPacket header;
	private FieldDataPacket fieldData;
	private FieldDataPacket privateFieldData;
	private Vector pendingPublicAttachments;
	private Vector pendingPrivateAttachments;
}
