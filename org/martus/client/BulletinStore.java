package org.martus.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.martus.client.*;
import org.martus.common.BulletinHeaderPacket;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.FileDatabase;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusXml;
import org.martus.common.Packet;
import org.martus.common.UniversalId;
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
	public BulletinStore(File baseDirectory) throws FileDatabase.MissingAccountMapException
	{
		File dbDirectory = new File(baseDirectory, "packets");
		Database db = new ClientFileDatabase(dbDirectory);
		initialize(baseDirectory, db);
	}
	
	public BulletinStore(Database db)
	{
		try {
			File tempFile = File.createTempFile("$$$MartusBulletinStore", null);
			File baseDirectory = tempFile.getParentFile();
			tempFile.delete();
			initialize(baseDirectory, db);
		} 
		catch(IOException e) 
		{
			System.out.println("BulletinStore: " + e);
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
	
	public void setEncryptPublicData(boolean encrypt)
	{
		encryptPublicDataFlag = encrypt;
	}
	
	public boolean mustEncryptPublicData()
	{
		return encryptPublicDataFlag;
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
	
	public Set getSetOfBulletinUniversalIdsInFolders()
	{
		Set setOfUniversalIds = new HashSet();
		
		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			for(int b = 0; b < folder.getBulletinCount(); ++b)
			{
				UniversalId uid = folder.getBulletinUniversalId(b);
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

	public void destroyBulletin(Bulletin b)
	{
		UniversalId id = b.getUniversalId();

		for(int f = 0; f < getFolderCount(); ++f)
		{
			removeBulletinFromFolder(b, getFolder(f));
		}

		removeBulletinFromStore(id);
	}
	
	public void removeBulletinFromStore(UniversalId uid)
	{
		Bulletin foundBulletin = findBulletinByUniversalId(uid);
		foundBulletin.removeBulletinFromDatabase(database, getSignatureVerifier());
		bulletinCache.remove(uid);
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
			Bulletin b = Bulletin.loadFromDatabase(this, key);
			addToCache(b);
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
			return null;
		}
	}

	public void saveBulletin(Bulletin b)
	{
		bulletinCache.remove(b.getUniversalId());
		try
		{
			b.setStore(this);
			b.saveToDatabase(database);
		}
		catch(Exception e)
		{
			//TODO: Better error handling!
			System.out.println("BulletinStore.saveBulletin: " + e);
			System.out.println("BulletinStore.saveBulletin: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void discardBulletin(BulletinFolder f, Bulletin b)
	{
		getFolderDiscarded().add(b);
		removeBulletinFromFolder(b, f);
		if(isOrphan(b))
			destroyBulletin(b);
	}

	public BulletinFolder createFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		return folder;
	}

	public boolean renameFolder(String oldName, String newName)
	{
		if(!BulletinFolder.isNameVisible(newName))
			return false;

		if(newName.charAt(0) < '0')
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

	public boolean deleteFolder(String name)
	{
		BulletinFolder folder = findFolder(name);
		if(folder == null)
			return false;

		if(!folder.canDelete())
			return false;

		BulletinFolder discarded = getFolderDiscarded();

		while(folder.getBulletinCount() > 0)
		{
			Bulletin b = folder.getBulletin(0);
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

	public int getFolderCount()
	{
		return folders.size();
	}

	public BulletinFolder getFolder(int index)
	{
		if(index < 0 || index >= folders.size())
			return null;

		return (BulletinFolder)folders.get(index);
	}

	public BulletinFolder findFolder(String name)
	{
		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			if(name.equals(folder.getName()))
				return folder;
		}
		return null;
	}

	public String getSearchFolderName()
	{
		return "Search Results";
	}
	
	public String getOrphanFolderName()
	{
		return "Recovered Bulletins";
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
		folderOutbox = createSystemFolder("Outbox");
		folderOutbox.setStatusAllowed(Bulletin.STATUSSEALED);
		folderSent = createSystemFolder("Sent Bulletins");
		folderSent.setStatusAllowed(Bulletin.STATUSSEALED);
		folderDrafts = createSystemFolder("Draft Bulletins");
		folderDrafts.setStatusAllowed(Bulletin.STATUSDRAFT);
		folderDiscarded = createSystemFolder("Discarded Bulletins");
		folderDraftOutbox = createSystemFolder("*DraftOutbox");
		folderDraftOutbox.setStatusAllowed(Bulletin.STATUSDRAFT);
	}

	public BulletinFolder createSystemFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		folder.preventRename();
		folder.preventDelete();
		return folder;
	}

	public void moveBulletin(Bulletin b, BulletinFolder from, BulletinFolder to)
	{
		if(from.equals(to))
			return;
		to.add(b);
		removeBulletinFromFolder(b, from);
	}

	public void removeBulletinFromFolder(Bulletin b, BulletinFolder from)
	{
		from.remove(b.getUniversalId());
		saveFolders();
	}

	public void deleteAllData()
	{
		database.deleteAllData();
		getFoldersFile().delete();
		initialize(dir, database);
	}

	public Database getDatabase()
	{
		return database;
	}
	
	public void setDatabase(Database toUse)
	{
		database = toUse;	
	}

	public void loadFolders()
	{
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = new BufferedInputStream(new FileInputStream(getFoldersFile()));
			getSignatureVerifier().decrypt(in, out);
			in.close();
			
			String folderXml = new String(out.toByteArray(), "UTF-8");
			if(folderXml != null)
				loadFolders(new StringReader(folderXml));
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
			// TODO: Improve error handling!!!
			System.out.println("BulletinStore.saveFolders: " + e);
		}
	}
	
	File getFoldersFile()
	{
		return new File(dir, "MartusFolders.dat");
	}

	public Bulletin createEmptyBulletin()
	{
		Bulletin b = new Bulletin(this);
		return b;
	}

	public BulletinFolder createOrFindFolder(String name)
	{
		BulletinFolder result = findFolder(name);
		if(result != null)
			return result;
		return createFolder(name);
	}

	public void addBulletinToFolder(UniversalId uId, BulletinFolder folder)
	{
		Bulletin b = findBulletinByUniversalId(uId);
		if(b == null)
			return;

		folder.add(b);
	}

	private void initialize(File baseDirectory, Database db)
	{
		dir = baseDirectory;
		database = db;
		account = "";
		bulletinCache = new TreeMap();
		folders = new Vector();

		createSystemFolders();
	}

	public int quarantineUnreadableBulletins()
	{
		class Quarantiner implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					InputStream in = database.openInputStream(key, getSignatureVerifier());
					Packet.validateXml(in, key.getAccountId(), key.getLocalId(), null, getSignatureVerifier());
				}
				catch(Exception e)
				{
					++quarantinedCount;
					database.moveRecordToQuarantine(key);
				}
			}
		
			int quarantinedCount;
		}
		
		Quarantiner visitor = new Quarantiner();
		visitAllBulletins(visitor);
		return visitor.quarantinedCount;
	}
	
	boolean isOrphan(Bulletin b)
	{
		for(int f=0; f < folders.size(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			if(folder.isVisible() && folder.contains(b))
				return false;
		}

		return true;
	}

	private BulletinFolder rawCreateFolder(String name)
	{
		if(findFolder(name) != null)
			return null;

		BulletinFolder folder = new BulletinFolder(this, name);
		folders.add(folder);
		return folder;
	}

	public String foldersToXml()
	{
		String xml = MartusXml.getFolderListTagStart();

		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			xml += folderToXml(folder);
		}

		xml += MartusXml.getFolderListTagEnd();
		return xml;
	}

	public String folderToXml(BulletinFolder folder)
	{
		String xml = MartusXml.getFolderTagStart(folder.getName());
		for(int index=0; index < folder.getBulletinCount(); ++index)
		{
			Bulletin b = folder.getBulletin(index);
			xml += MartusXml.getIdTag(b.getUniversalIdString());
		}
		xml += MartusXml.getFolderTagEnd();
		return xml;
	}

	private static class FolderXmlHandler extends DefaultHandler
	{
		public FolderXmlHandler(BulletinStore storeToUse)
		{
			store = storeToUse;
		}

		public void startElement(String namespaceURI, String sName, String qName,
				Attributes attrs) throws SAXException
		{
			if(qName.equals(MartusXml.tagFolder))
			{
				String name = attrs.getValue(MartusXml.attrFolder);
				currentFolder = store.createOrFindFolder(name);
			}

			buffer = "";
		}

		public void endElement(String namespaceURI, String sName, String qName)
		{
			if(qName.equals(MartusXml.tagFolder))
			{
				currentFolder = null;
			}
			else if(qName.equals(MartusXml.tagId))
			{
				try 
				{
					UniversalId bId = UniversalId.createFromString(buffer);
					store.addBulletinToFolder(bId, currentFolder);
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

		BulletinStore store;
		BulletinFolder currentFolder;
		String buffer = "";
	}

	public void loadFolders(Reader xml)
	{
		folders.clear();
		createSystemFolders();
		FolderXmlHandler handler = new FolderXmlHandler(this);
		MartusXml.loadXml(xml, handler);
	}

	private String createUniqueId()
	{
		return new UID().toString();
	}
	
	public class StatusNotAllowedException extends Exception {}
	
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
				importZipFileToStoreWithSameUids(zipFile);
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
		final MartusCrypto verifier = getSignatureVerifier();

		ZipFile zip = new ZipFile(inputFile);
		try
		{
			Bulletin.importZipFileToStoreWithSameUids(zip, this);
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

	void addToCache(Bulletin b) 
	{
		if(bulletinCache.size() >= maxCachedBulletinCount)
			bulletinCache.clear();
			
		bulletinCache.put(b.getUniversalId(), b);
	}

	static final int maxCachedBulletinCount = 100;
	
	private MartusCrypto signer;
	private String account;
	private File dir;
	private Database database;
	private Vector bulletins;
	private Vector folders;
	private BulletinFolder folderOutbox;
	private BulletinFolder folderSent;
	private BulletinFolder folderDrafts;
	private BulletinFolder folderDiscarded;
	private BulletinFolder folderDraftOutbox;
	Map bulletinCache;
	private boolean encryptPublicDataFlag;
}
