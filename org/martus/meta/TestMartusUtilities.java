package org.martus.meta;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipFile;

import org.martus.client.Bulletin;
import org.martus.client.BulletinStore;
import org.martus.common.Database;
import org.martus.common.DatabaseKey;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockMartusSecurity;
import org.martus.common.Packet;
import org.martus.common.StringInputStream;
import org.martus.common.TestCaseEnhanced;

public class TestMartusUtilities extends TestCaseEnhanced 
{
	public TestMartusUtilities(String name) 
	{
		super(name);
	}

	// TODO: create tests for all the MartusUtilities methods
	
	public void testThreadedPacketWriting() throws Exception
	{
		final int threadCount = 10;
		final int iterations = 10;
		ThreadFactory factory = new PacketWriteThreadFactory();
		launchTestThreads(factory, threadCount, iterations);
	}
	
	public void testThreadedExporting() throws Exception
	{
		final int threadCount = 10;
		final int iterations = 10;
		ThreadFactory factory = new ExportThreadFactory();
		launchTestThreads(factory, threadCount, iterations);
	}

	public void testThreadedimporting() throws Exception
	{
		final int threadCount = 10;
		final int iterations = 10;
		ThreadFactory factory = new ImportThreadFactory();
		launchTestThreads(factory, threadCount, iterations);
	}
	
	

	private void launchTestThreads(ThreadFactory factory, int threadCount, int iterations) throws Exception
	{
		TestingThread[] threads = new TestingThread[threadCount];
		for (int i = 0; i < threads.length; i++) 
		{
			threads[i] = factory.createThread(iterations);
		}
		
		for (int i = 0; i < threads.length; i++) 
		{
			threads[i].start();
		}
		
		for (int i = 0; i < threads.length; i++) 
		{
			threads[i].join();
			if(threads[i].getResult() != null)
				throw threads[i].getResult();
		}
	}
	
	abstract class ThreadFactory
	{
		abstract TestingThread createThread(int copies) throws Exception;
	}
	
	class ExportThreadFactory extends ThreadFactory
	{
		ExportThreadFactory() throws Exception
		{
			BulletinStore store = new BulletinStore(new MockClientDatabase());
			MockMartusSecurity security = new MockMartusSecurity();
			security.createKeyPair();
			
			store.setSignatureGenerator(security);
			b = store.createEmptyBulletin();
			b.save();
		}
		
		TestingThread createThread(int copies) throws Exception
		{
			return new Exporter(b, copies);
		}
		
		Bulletin b;
	}
	
	class ImportThreadFactory extends ThreadFactory
	{
		ImportThreadFactory() throws Exception
		{
			store = new BulletinStore(new MockClientDatabase());
			MockMartusSecurity security = new MockMartusSecurity();
			security.createKeyPair();
			store.setSignatureGenerator(security);
		}
		
		TestingThread createThread(int copies) throws Exception
		{
			return new Importer(store, copies);
		}

		BulletinStore store;		
	}
	
	class PacketWriteThreadFactory extends ThreadFactory
	{
		PacketWriteThreadFactory() throws Exception
		{
			store = new BulletinStore(new MockClientDatabase());

			MockMartusSecurity security = new MockMartusSecurity();
			security.createKeyPair();
			store.setSignatureGenerator(security);

		}
		
		TestingThread createThread(int copies) throws Exception
		{
			return new PacketWriter(store, copies);
		}

		BulletinStore store;		
	}
	
	abstract class TestingThread extends Thread
	{
		Exception getResult()
		{
			return result;
		}

		Exception result;
	}

	class Exporter extends TestingThread
	{
		Exporter(Bulletin bulletinToExport, int copiesToExport) throws Exception
		{
			bulletin = bulletinToExport;
			file = createTempFile();
			copies = copiesToExport;
			db = bulletin.getStore().getDatabase();
			security = bulletin.getStore().getSignatureVerifier();
			headerKey = DatabaseKey.createKey(bulletin.getUniversalId(), bulletin.getStatus());
		}
		
		public void run()
		{
			try 
			{
				for(int i=0; i < copies; ++i)
					MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, file, security);
				} 
			catch (Exception e) 
			{
				result = e;
			}
		}
		
		Bulletin bulletin;
		File file;
		int copies;
		Database db;
		MartusCrypto security;
		DatabaseKey headerKey;
	}

	class Importer extends TestingThread
	{
		Importer(BulletinStore storeToUse, int copiesToDo) throws Exception
		{
			copies = copiesToDo;
			store = storeToUse;

			file = createTempFile();
			db = store.getDatabase();
			security = store.getSignatureVerifier();

			Bulletin b = store.createEmptyBulletin();
			b.save();
			Database db = b.getStore().getDatabase();
			headerKey = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());
			MartusUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, file, security);
			store.destroyBulletin(b);
		}
		
		public void run()
		{
			try 
			{
				for(int i=0; i < copies; ++i)
				{
					ZipFile zip = new ZipFile(file);
					MartusUtilities.importBulletinPacketsFromZipFileToDatabase(db, null, zip, security);
					zip.close();

					Bulletin b = store.findBulletinByUniversalId(headerKey.getUniversalId());
					assertNotNull("import didn't work?", b);
					store.destroyBulletin(b);
				}
			} 
			catch (Exception e) 
			{
				result = e;
			}
		}
		
		BulletinStore store;
		File file;
		int copies;
		Database db;
		MartusCrypto security;
		DatabaseKey headerKey;
	}

	class PacketWriter extends TestingThread
	{
		PacketWriter(BulletinStore storeToUse, int copiesToDo) throws Exception
		{
			
			copies = copiesToDo;
			db = storeToUse.getDatabase();
			bulletin = storeToUse.createEmptyBulletin();
			security = storeToUse.getSignatureGenerator();
		}
		
		public void run()
		{
			try 
			{
				for(int i=0; i < copies; ++i)
				{
					Writer writer = new StringWriter();
					bulletin.getBulletinHeaderPacket().writeXml(writer, security);
					InputStreamWithSeek in = new StringInputStream(writer.toString());
					Packet.validateXml(in, bulletin.getAccount(), bulletin.getLocalId(), null, security);
				}
			} 
			catch (Exception e) 
			{
				result = e;
			}
		}
		
		Bulletin bulletin;
		Database db;
		MartusCrypto security;
		int copies;
	}
}
