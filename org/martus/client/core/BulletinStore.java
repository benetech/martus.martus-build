/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.rmi.server.UID;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FileInputStreamWithSeek;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusXml;
import org.martus.common.Packet;
import org.martus.common.UniversalId;
import org.martus.common.FileDatabase.MissingAccountMapException;
import org.martus.common.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.Packet.WrongAccountException;
import org.martus.common.UniversalId.NotUniversalIdException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
	This class represents a collection of bulletins
	(and also a collection of folders) stored on the
	client pc.

	It is responsible for managing the lifetimes of
	both bulletins and folders, including saving and
	loading them to/from disk.
*/
public class BulletinStore
{
	public BulletinStore(File baseDirectory, MartusCrypto cryptoToUse)
	{
		setSignatureGenerator(cryptoToUse);
		File dbDirectory = new File(baseDirectory, "packets");
		Database db = new ClientFileDatabase(dbDirectory, signer);
		setUpStore(baseDirectory, db);
	}

	public BulletinStore(Database db)
	{
		try
		{
			File tempFile = File.createTempFile("$$$MartusBulletinStore", null);
			File baseDirectory = tempFile.getParentFile();
			tempFile.delete();
			setUpStore(baseDirectory, db);
		}
		catch(IOException e)
		{
			System.out.println("BulletinStore: " + e);
		}
	}

	public void doAfterSigninInitalization() throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		database.initialize();
		loadCacheOfSortableFields();
	}

	public void prepareToExit()
	{
		try
		{
			FileOutputStream out = new FileOutputStream(cacheOfSortableFieldsFile);
			cacheOfSortableFields.save(out, getSignatureGenerator());
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			cacheOfSortableFieldsFile.delete();
		}
	}

	public String getAccountId()
	{
		return signer.getPublicKeyString();
	}

	public void setSignatureGenerator(MartusCrypto signerToUse)
	{
		signer = signerToUse;
	}

	public MartusCrypto getSignatureGenerator()
	{
		return signer;
	}

	public MartusCrypto getSignatureVerifier()
	{
		return signer;
	}

	public boolean mustEncryptPublicData()
	{
		return getDatabase().mustEncryptLocalData();
	}

	public int getBulletinCount()
	{
		class BulletinCounter implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				++count;
			}

			int count = 0;
		}

		BulletinCounter counter = new BulletinCounter();
		visitAllBulletins(counter);
		return counter.count;
	}

	public Vector getAllBulletinUids()
	{
		class UidCollector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				uidList.add(key.getUniversalId());
			}
			Vector uidList = new Vector();
		}

		UidCollector uidCollector = new UidCollector();
		visitAllBulletins(uidCollector);
		return uidCollector.uidList;
	}

	public void visitAllBulletins(Database.PacketVisitor visitor)
	{
		class BulletinKeyFilter implements Database.PacketVisitor
		{
			BulletinKeyFilter(Database db, Database.PacketVisitor visitorToUse)
			{
				visitor = visitorToUse;
				db.visitAllRecords(this);
			}

			public void visit(DatabaseKey key)
			{
				if(BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				{
					++count;
					visitor.visit(key);
				}
			}
			Database.PacketVisitor visitor;
			int count;
		}

		new BulletinKeyFilter(getDatabase(), visitor);
	}

	public Set getSetOfAllBulletinUniversalIds()
	{
		class Visitor implements Database.PacketVisitor
		{
			Visitor()
			{
				setOfUniversalIds = new HashSet();
			}

			public void visit(DatabaseKey key)
			{
				setOfUniversalIds.add(key.getUniversalId());
			}

			Set setOfUniversalIds;
		}

		Visitor visitor = new Visitor();
		visitAllBulletins(visitor);
		return visitor.setOfUniversalIds;
	}

	public synchronized Set getSetOfBulletinUniversalIdsInFolders()
	{
		Set setOfUniversalIds = new HashSet();

		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			for(int b = 0; b < folder.getBulletinCount(); ++b)
			{
				UniversalId uid = folder.getBulletinUniversalIdUnsorted(b);
				setOfUniversalIds.add(uid);
			}
		}

		return setOfUniversalIds;
	}

	public Set getSetOfOrphanedBulletinUniversalIds()
	{
		Set possibleOrphans = getSetOfAllBulletinUniversalIds();
		Set inFolders = getSetOfBulletinUniversalIdsInFolders();
		possibleOrphans.removeAll(inFolders);
		return possibleOrphans;
	}

	public synchronized void destroyBulletin(Bulletin b)
	{
		UniversalId id = b.getUniversalId();

		for(int f = 0; f < getFolderCount(); ++f)
		{
			removeBulletinFromFolder(b, getFolder(f));
		}

		removeBulletinFromStore(id);
	}

	public synchronized void removeBulletinFromStore(UniversalId uid)
	{
		Bulletin foundBulletin = findBulletinByUniversalId(uid);
		MartusCrypto crypto = getSignatureVerifier();
		try
		{
			MartusUtilities.deleteBulletinFromDatabase(foundBulletin.getBulletinHeaderPacket(), database, crypto);
		}
		catch(Exception e)
		{
			//TODO: NEED BETTER ERROR HANDLING HERE!!!
			//System.out.println("removeBulletinFromDatabase: " + e);
		}
		bulletinCache.remove(uid);
		cacheOfSortableFields.removeFieldData(uid);
	}

	public Bulletin findBulletinByUniversalId(UniversalId uid)
	{

		if(bulletinCache.containsKey(uid))
			return (Bulletin)bulletinCache.get(uid);

		Database db = getDatabase();

		DatabaseKey key = new DatabaseKey(uid);
		if(!db.doesRecordExist(key))
		{
			//System.out.println("BulletinStore.findBulletinByUniversalId: !doesRecordExist");
			return null;
		}

		try
		{
			Bulletin b = BulletinLoader.loadFromDatabase(this, key);
			addToCaches(b);
			return b;
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
			//TODO: Better error handling
			System.out.println("BulletinStore.findBulletinByUniversalId: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public String getFieldData(UniversalId uid, String fieldTag)
	{
		String data = cacheOfSortableFields.getFieldData(uid, fieldTag);
		if(data != null)
			return data;
		Bulletin b = findBulletinByUniversalId(uid);
		return b.get(fieldTag);
	}

	public void saveBulletin(Bulletin b)
	{
		bulletinCache.remove(b.getUniversalId());
		try
		{
			b.setStore(this);
			BulletinSaver.saveToDatabase(b, database, mustEncryptPublicData());
			//We don't call addToCaches here because we are not sure
			//that this bulletin object is still usable -- maybe
			//attachment proxies still point to disk files?
			cacheOfSortableFields.setFieldData(b);
		}
		catch(Exception e)
		{
			//TODO: Better error handling!
			System.out.println("BulletinStore.saveBulletin: " + e);
			System.out.println("BulletinStore.saveBulletin: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public synchronized void discardBulletin(BulletinFolder f, Bulletin b)
	{
		getFolderDiscarded().add(b);
		removeBulletinFromFolder(b, f);
		if(isOrphan(b))
			destroyBulletin(b);
	}

	public synchronized BulletinFolder createFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		return folder;
	}

	public synchronized boolean renameFolder(String oldName, String newName)
	{
		if(!BulletinFolder.isNameVisible(newName))
			return false;

		if(newName.length() == 0 || newName.charAt(0) < '0')
			return false;

		if(findFolder(newName) != null)
			return false;

		BulletinFolder folder = findFolder(oldName);
		if(folder == null)
			return false;

		folder.setName(newName);
		saveFolders();
		return true;
	}

	public synchronized boolean deleteFolder(String name)
	{
		BulletinFolder folder = findFolder(name);
		if(folder == null)
			return false;

		if(!folder.canDelete())
			return false;

		BulletinFolder discarded = getFolderDiscarded();

		while(folder.getBulletinCount() > 0)
		{
			Bulletin b = folder.getBulletinSorted(0);
			discarded.add(b);
			folder.remove(b.getUniversalId());
		}

		folders.remove(folder);
		saveFolders();
		return true;
	}

	public void clearFolder(String folderName)
	{
		BulletinFolder folder = findFolder(folderName);
		if(folder == null)
			return;

		folder.removeAll();
		saveFolders();
	}

	public synchronized int getFolderCount()
	{
		return folders.size();
	}

	private synchronized BulletinFolder getFolder(int index)
	{
		if(index < 0 || index >= folders.size())
			return null;

		return (BulletinFolder)folders.get(index);
	}

	public synchronized BulletinFolder findFolder(String name)
	{
		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			if(name.equals(folder.getName()))
				return folder;
		}
		return null;
	}

	public synchronized Vector getAllFolderNames()
	{
		Vector names = new Vector();
		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			String folderName = folder.getName();
			names.add(folderName);
		}
		return names;
	}

	public synchronized Vector getVisibleFolderNames()
	{
		Vector names = new Vector();
		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			String folderName = folder.getName();
			if(BulletinFolder.isNameVisible(folderName))
				names.add(folderName);
		}
		return names;
	}

	public String getSearchFolderName()
	{
		return SEARCH_RESULTS_BULLETIN_FOLDER;
	}

	public String getOrphanFolderName()
	{
		return RECOVERED_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedSealed()
	{
		return RETRIEVE_SEALED_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedDraft()
	{
		return RETRIEVE_DRAFT_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedFieldOfficeSealed()
	{
		return RETRIEVE_SEALED_FIELD_OFFICE_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedFieldOfficeDraft()
	{
		return RETRIEVE_DRAFT_FIELD_OFFICE_BULLETIN_FOLDER;
	}

	public String getNameOfFolderDamaged()
	{
		return DAMAGED_BULLETIN_FOLDER;
	}


	public BulletinFolder getFolderOutbox()
	{
		return folderOutbox;
	}

	public BulletinFolder getFolderDiscarded()
	{
		return folderDiscarded;
	}

	public BulletinFolder getFolderSent()
	{
		return folderSent;
	}

	public BulletinFolder getFolderDrafts()
	{
		return folderDrafts;
	}

	public BulletinFolder getFolderDraftOutbox()
	{
		return folderDraftOutbox;
	}

	public void createSystemFolders()
	{
		folderOutbox = createSystemFolder(OUTBOX_FOLDER);
		folderOutbox.setStatusAllowed(Bulletin.STATUSSEALED);
		folderSent = createSystemFolder(SENT_FOLDER);
		folderSent.setStatusAllowed(Bulletin.STATUSSEALED);
		folderDrafts = createSystemFolder(DRAFT_FOLDER);
		folderDrafts.setStatusAllowed(Bulletin.STATUSDRAFT);
		folderDiscarded = createSystemFolder(DISCARDED_FOLDER);
		folderDraftOutbox = createSystemFolder(DRAFT_OUTBOX);
		folderDraftOutbox.setStatusAllowed(Bulletin.STATUSDRAFT);
	}

	public BulletinFolder createSystemFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		folder.preventRename();
		folder.preventDelete();
		return folder;
	}

	public synchronized void moveBulletin(Bulletin b, BulletinFolder from, BulletinFolder to)
	{
		if(from.equals(to))
			return;
		to.add(b);
		removeBulletinFromFolder(b, from);
	}

	public synchronized void removeBulletinFromFolder(Bulletin b, BulletinFolder from)
	{
		from.remove(b.getUniversalId());
		saveFolders();
	}

	public Vector findBulletinInAllVisibleFolders(Bulletin b)
	{
		Vector allFolders= getVisibleFolderNames();
		Vector foldersContainingBulletin = new Vector();
		for(int i = 0; i < allFolders.size(); ++i)
		{
			BulletinFolder folder = findFolder((String)allFolders.get(i));
			if(folder != null && folder.contains(b))
				foldersContainingBulletin.add(folder);
		}
		return foldersContainingBulletin;
	}

	public void deleteAllData() throws Exception
	{
		database.deleteAllData();
		getFoldersFile().delete();
		setUpStore(dir, database);
	}

	public Database getDatabase()
	{
		return database;
	}

	public void setDatabase(Database toUse)
	{
		database = toUse;
	}

	public synchronized void loadFolders()
	{
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStreamWithSeek in = new FileInputStreamWithSeek(getFoldersFile());
			getSignatureVerifier().decrypt(in, out);
			in.close();

			String folderXml = new String(out.toByteArray(), "UTF-8");
			if(folderXml != null)
				if(loadFolders(new StringReader(folderXml)))
					saveFolders();
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("BulletinStore.loadFolders: " + e);
		}
		catch(FileNotFoundException expectedIfFoldersDontExistYet)
		{
		}
		catch(Exception e)
		{
			// TODO: Improve error handling!!!
			System.out.println("BulletinStore.loadFolders: " + e);
		}
	}

	public synchronized void saveFolders()
	{
		try
		{
			String xml = foldersToXml();
			byte[] bytes = xml.getBytes("UTF-8");
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);

			FileOutputStream out = new FileOutputStream(getFoldersFile());
			if(getSignatureGenerator() == null)
				return;
			getSignatureGenerator().encrypt(in, out);
			out.close();
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("BulletinStore.saveFolders: " + e);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("BulletinStore.saveFolders: " + e);
		}
	}

	public File getFoldersFile()
	{
		return new File(dir, "MartusFolders.dat");
	}

	public Bulletin createEmptyBulletin()
	{
		Bulletin b = new Bulletin(this);
		return b;
	}

	public synchronized BulletinFolder createOrFindFolder(String name)
	{
		BulletinFolder result = findFolder(name);
		if(result != null)
			return result;
		return createFolder(name);
	}

	public synchronized void addBulletinToFolder(UniversalId uId, BulletinFolder folder)
	{
		Bulletin b = findBulletinByUniversalId(uId);
		if(b == null)
			return;

		folder.add(b);
	}

	private void setUpStore(File baseDirectory, Database db)
	{
		dir = baseDirectory;
		cacheOfSortableFieldsFile = new File(dir, CACHE_FILE_NAME);

		database = db;
		account = "";
		bulletinCache = new TreeMap();
		cacheOfSortableFields = new CacheOfSortableFields();
		folders = new Vector();

		createSystemFolders();
	}

	private void loadCacheOfSortableFields()
	{
		if(!cacheOfSortableFieldsFile.exists())
			return;

		FileInputStreamWithSeek in = null;
		try
		{
			in = new FileInputStreamWithSeek(cacheOfSortableFieldsFile);
			cacheOfSortableFields.load(in, getSignatureGenerator());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(in != null)
					in.close();
			}
			catch (IOException nothingWeCanDo)
			{
			}
		}
		cacheOfSortableFieldsFile.delete();
	}

	public int quarantineUnreadableBulletins()
	{
		class Quarantiner implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				InputStreamWithSeek in = null;
				try
				{
					in = database.openInputStream(key, getSignatureVerifier());
					Packet.validateXml(in, key.getAccountId(), key.getLocalId(), null, getSignatureVerifier());
					in.close();
				}
				catch(Exception e)
				{
					++quarantinedCount;
					if(in != null)
					{
						try { in.close(); } catch(Exception ignore) {}
					}
					database.moveRecordToQuarantine(key);
				}
			}

			int quarantinedCount;
		}

		Quarantiner visitor = new Quarantiner();
		visitAllBulletins(visitor);
		return visitor.quarantinedCount;
	}

	public synchronized boolean isOrphan(Bulletin b)
	{
		Vector allFolders= getVisibleFolderNames();
		for(int i = 0; i < allFolders.size(); ++i)
		{
			BulletinFolder folder = findFolder((String)allFolders.get(i));
			if(folder != null && folder.contains(b))
				return false;
		}

		return true;
	}

	private synchronized BulletinFolder rawCreateFolder(String name)
	{
		if(findFolder(name) != null)
			return null;

		BulletinFolder folder = new BulletinFolder(this, name);
		folders.add(folder);
		return folder;
	}

	public synchronized String foldersToXml()
	{
		String xml = MartusClientXml.getFolderListTagStart();

		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			xml += folderToXml(folder);
		}

		xml += MartusClientXml.getFolderListTagEnd();
		return xml;
	}

	public String folderToXml(BulletinFolder folder)
	{
		String xml = MartusClientXml.getFolderTagStart(folder.getName());
		for(int index=0; index < folder.getBulletinCount(); ++index)
		{
			UniversalId uid = folder.getBulletinUniversalIdSorted(index);
			xml += MartusXml.getIdTag(uid.toString());
		}
		xml += MartusClientXml.getFolderTagEnd();
		return xml;
	}

	private static class FolderXmlHandler extends DefaultHandler
	{
		public FolderXmlHandler(BulletinStore storeToUse)
		{
			store = storeToUse;
			startLoadingFolders = true;
		}

		public void startElement(String namespaceURI, String sName, String qName,
				Attributes attrs) throws SAXException
		{
			if(qName.equals(MartusClientXml.tagFolder))
			{
				handleFolderStartTag(attrs);
			}

			buffer = "";
		}

		public void endElement(String namespaceURI, String sName, String qName)
		{
			if(qName.equals(MartusClientXml.tagFolder))
			{
				currentFolder = null;
			}
			else if(qName.equals(MartusClientXml.tagId))
			{
				try
				{
					UniversalId bId = UniversalId.createFromString(buffer);
					currentFolder.add(bId);
				}
				catch(NotUniversalIdException e)
				{
					System.out.println("BulletinStore::endElement : " + e);
				}
			}

			buffer = "";
		}

		public void characters(char buf[], int offset, int len) throws SAXException
		{
			buffer += new String(buf, offset, len);
		}

		void handleFolderStartTag(Attributes attrs)
		{
			String name = attrs.getValue(MartusClientXml.attrFolder);
			if(startLoadingFolders)
			{
				startLoadingFolders = false;
				if(name.equals("Outbox"))
					legacyFolders = true;
			}
			if(legacyFolders)
			{
				if(name.equals("Outbox"))
					name = OUTBOX_FOLDER;
				if(name.equals("Sent Bulletins"))
					name = SENT_FOLDER;
				if(name.equals("Draft Bulletins"))
					name = DRAFT_FOLDER;
				if(name.equals("Discarded Bulletins"))
					name = DISCARDED_FOLDER;
			}
			currentFolder = store.createOrFindFolder(name);
		}

		BulletinStore store;
		BulletinFolder currentFolder;
		String buffer = "";

		boolean startLoadingFolders;
		public boolean legacyFolders;
	}

	public synchronized boolean loadFolders(Reader xml)
	{
		folders.clear();
		createSystemFolders();
		FolderXmlHandler handler = new FolderXmlHandler(this);
		MartusXml.loadXml(xml, handler);
		return handler.legacyFolders;
	}

	private String createUniqueId()
	{
		return new UID().toString();
	}

	public static class StatusNotAllowedException extends Exception {}

	public void importZipFileBulletin(File zipFile, BulletinFolder toFolder, boolean forceSameUids) throws
			IOException,
			StatusNotAllowedException,
			MartusCrypto.CryptoException,
			Packet.InvalidPacketException,
			Packet.SignatureVerificationException
	{
		ZipFile zip = new ZipFile(zipFile);
		try
		{
			BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, getSignatureVerifier());
			if(!canPutBulletinInFolder(toFolder, bhp.getAccountId(), bhp.getStatus()))
			{
				throw new StatusNotAllowedException();
			}
			UniversalId uid = bhp.getUniversalId();

			boolean isSealed = bhp.getStatus().equals(Bulletin.STATUSSEALED);
			boolean isMine = getAccountId().equals(bhp.getAccountId());
			if(forceSameUids || !isMine || isSealed)
			{
				importZipFileToStoreWithSameUids(zipFile);
			}
			else
				uid = importZipFileToStoreWithNewUids(zipFile);

			addBulletinToFolder(uid, toFolder);
		}
		finally
		{
			zip.close();
		}

		saveFolders();
	}

	public void importZipFileToStoreWithSameUids(File inputFile) throws
		IOException,
		MartusCrypto.CryptoException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException
	{
		ZipFile zip = new ZipFile(inputFile);
		try
		{
			MartusUtilities.importBulletinPacketsFromZipFileToDatabase(getDatabase(), null, zip, getSignatureVerifier());
		}
		catch(WrongAccountException shouldBeImpossible)
		{
			throw new Packet.InvalidPacketException("Wrong account???");
		}
		finally
		{
			zip.close();
		}
	}

	public UniversalId importZipFileToStoreWithNewUids(File inputFile) throws
			IOException,
			MartusCrypto.CryptoException,
			Packet.InvalidPacketException,
			Packet.SignatureVerificationException
	{
		return Bulletin.importZipFileToStoreWithNewUids(inputFile, this);
	}

	public boolean canPutBulletinInFolder(BulletinFolder folder, String bulletinAuthorAccount, String bulletinStatus)
	{
		if(!folder.canAdd(bulletinStatus))
			return false;
		if(folder.equals(getFolderOutbox()) && !bulletinAuthorAccount.equals(getAccountId()))
			return false;
		return true;
	}

	synchronized void addToCaches(Bulletin b)
	{
		if(bulletinCache.size() >= maxCachedBulletinCount)
			bulletinCache.clear();

		bulletinCache.put(b.getUniversalId(), b);
		cacheOfSortableFields.setFieldData(b);
	}

	public static int maxCachedBulletinCount = 100;

	public static final String OUTBOX_FOLDER = "%OutBox";
	public static final String SENT_FOLDER = "%Sent";
	public static final String DRAFT_FOLDER = "%Draft";
	public static final String DISCARDED_FOLDER = "%Discarded";
	public static final String SEARCH_RESULTS_BULLETIN_FOLDER = "%SearchResults";
	public static final String RECOVERED_BULLETIN_FOLDER = "%RecoveredBulletins";
	public static final String RETRIEVE_SEALED_BULLETIN_FOLDER = "%RetrievedMyBulletin";
	public static final String RETRIEVE_SEALED_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletin";
	public static final String RETRIEVE_DRAFT_BULLETIN_FOLDER = "%RetrievedMyBulletinDraft";
	public static final String RETRIEVE_DRAFT_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletinDraft";
	public static final String DAMAGED_BULLETIN_FOLDER = "%DamagedBulletins";
	private static final String DRAFT_OUTBOX = "*DraftOutbox";

	private static final String CACHE_FILE_NAME = "sfcache.dat";
	private MartusCrypto signer;
	private String account;
	private File dir;
	private Database database;
	private Vector folders;
	private BulletinFolder folderOutbox;
	private BulletinFolder folderSent;
	private BulletinFolder folderDrafts;
	private BulletinFolder folderDiscarded;
	private BulletinFolder folderDraftOutbox;
	public Map bulletinCache;
	CacheOfSortableFields cacheOfSortableFields;
	File cacheOfSortableFieldsFile;
	private boolean encryptPublicDataFlag;
}
