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

package org.martus.client.test;

import java.io.File;
import java.io.IOException;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.common.Bulletin;
import org.martus.common.MartusCrypto;
import org.martus.common.MockClientDatabase;
import org.martus.common.MockMartusSecurity;

public class MockMartusApp extends MartusApp
{
	public static MockMartusApp create(MartusCrypto crypto) throws IOException, MartusAppInitializationException
	{
		File fakeDataDirectory = File.createTempFile("$$$MartusTestApp", null);
		fakeDataDirectory.deleteOnExit();
		fakeDataDirectory.delete();
		fakeDataDirectory.mkdir();

		MockMartusApp app = new MockMartusApp(crypto, fakeDataDirectory);
		app.dataDirectory = fakeDataDirectory.getPath() + "/";

		app.store = new BulletinStore(new MockClientDatabase());
		app.store.setSignatureGenerator(crypto);

		return app;
	}

	public static MockMartusApp create() throws Exception
	{
		return create(new MockMartusSecurity());
	}

	MockMartusApp(MartusCrypto crypto, File dataDirectoryToUse) throws MartusAppInitializationException
	{
		super(crypto, dataDirectoryToUse);
	}

	public void deleteAllFiles() throws Exception
	{
		getStore().deleteAllData();

		getUploadInfoFile().delete();
		if(getUploadInfoFile().exists())
			throw new IOException("uploadInfoFile");

		getKeyPairFile().delete();
		if(getKeyPairFile().exists())
			throw new IOException("getKeyPairFile");

		getBackupFile(getKeyPairFile()).delete();
		if(getBackupFile(getKeyPairFile()).exists())
			throw new IOException("getBackupFile");

		File configInfo = new File(getConfigInfoFilename());
		configInfo.delete();
		if(configInfo.exists())
			throw new IOException("configInfo");

		File sigFile = new File(getConfigInfoSignatureFilename());
		sigFile.delete();
		if(sigFile.exists())
			throw new IOException("sigFile");

		File logFile = new File(getUploadLogFilename());
		logFile.delete();
		if(logFile.exists())
			throw new IOException("logFile");

		File dir = new File(getDataDirectory());
		dir.delete();
		if(dir.exists())
			throw new IOException("dataDirectory");

	}

	public void loadSampleData()
	{
		BulletinFolder f = this.getFolderSent();
		{
			Bulletin b = createBulletin();
			b.set("language",	"es");
			b.set("author",		"Comisión para el Esclarecimiento Histórico");
			b.set("title",		"Santa Rosa Xeputul Bombardeo");
			b.set("location",	"Santa Rosa Xeputul, Guatemala");
			b.set("eventdate",	"1999-01-19");
			b.set("entrydate",	"1999-02-17");
			b.set("keywords",	"ejecución arbitraria, bombardeo");
			b.set("summary",	"El día 19 de diciembre de 1989, un avión Cessna A-37B, " +
								"de la Fuerza Aérea Guatemalteca sobrevoló la comunidad " +
								"Santa Rosa Xeputul de la CPR de la Sierra... a las 11 de " +
								"la mañana la unidad aérea lanzó una bomba de 550 libras " +
								"... murieron cuatro civiles, mujeres.  Otros dos " +
								"fallecieron ese mismo día en Chaxá, aldea cercana a Santa " +
								"Rosa, a causa de otra bomba arrojada por el avión.");
			b.set("publicinfo",	"");
			b.set("privateinfo",	"");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.setAllPrivate(true);
			b.set("author",		"Truth and Reconciliation Commission, South Africa");
			b.set("title",		"GO/0092 Johannesburg");
			b.set("location",	"Moroka");
			b.set("eventdate",	"1969-03-10");
			b.set("entrydate",	"1996-04-29");
			b.set("keywords",	"Torture, Murder, Police, Abduction");
			b.set("summary",	"Emmanuel Lenkoe was detained by police on 5 March 1969.  He " +
								"died on 10 March 1969 while in custody.  The police statement " +
								"was that he had hanged himself with his own belt.  The body " +
								"was not released to the family until one month after death.  An " +
								"autopsy performed at the family's request showed signs of " +
								"torture and burn marks.");
			b.set("publicinfo",	"The police said they were from Moroka Station, but actually " +
								"were from Pretoria where they detained Lenkoe. They had a law " +
								"that allowed them to detain without trial for 180 days, extended " +
								"from 90. The police claimed Emmanuel Lenkoe hung himself with " +
								"his belt in his cell");
			b.set("privateinfo","");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Radiz");
			b.set("eventdate", "2001-04-26");
			b.set("title", "Kidnapping in town square");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Molanna");
			b.set("eventdate", "1999-02-15");
			b.set("title", "Missing child near river");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Ullan");
			b.set("eventdate", "2000-12-19");
			b.set("title", "Child with broken arm");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Cambodian Land Rights Organization");
			b.set("title", "Confiscation of Land");
			b.set("location", "Siem Reap, Cambodia");
			b.set("eventdate", "2001-08-09");
			b.set("entrydate", "2001-10-05");
			b.set("keywords", "land confiscation, land rights, confiscation");
			b.set("summary", "A small business owner in the town of Siem Reap had " +
							"his shop taken from him when developers claimed they " +
							"purchased the land.");
			b.set("publicinfo", "The small business owner has been using the land " +
							"for over ten years.  It has been in his family for as " +
							"long as he can remember.  He is not sure who the " +
							"developers purchased the land from and does not know " +
							"who in the government to seek assistance from.  His " +
							"shop is in the tourist  district, and has been confiscated.");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.set("author", "Sri Lanka Peace Institute");
			b.set("title", "Firebombing of NGO Office");
			b.set("location", "Colombo");
			b.set("eventdate", "2001-02-03");
			b.set("entrydate", "2001-02-05");
			b.set("keywords", "firebomb, explosion, NGO");
			b.set("summary", "The Oxfam NGO office in downtown Colombo was attacked by " +
								"men who threw a firebomb in the front windows.");
			b.set("publicinfo", "The Oxfam NGO office in downtown Colombo was attacked " +
								"by men who threw a firebomb in the front windows.  The " +
								"explosion did not hurt anyone, staff members were in " +
								"the back of the office during the attack.");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.set("author", "Guatemala Legal Defense");
			b.set("title", "Stolen computers");
			b.set("location", "Guatemala City");
			b.set("eventdate", "2001-03-07");
			b.set("entrydate", "2001-04-14");
			b.set("keywords", "theft, computers, legal research, war crimes tribunal");
			b.set("summary", "The Guatemala Legal Defence offices were targeted for theft of computers.  All of the legal office PCs with information pertaining to the upcoming War Crimes Tribunal were stolen.");
			b.set("publicinfo", "The legal research to be used in the upcoming War Crimes Tribunal was lost when computers with the research and strategy were stolen in March.  Government officials claim that the computers loss was due to petty theft, but many in the NGO community believe that it is a targeted campaign to thwart justice in the upcoming tribunal.");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.set("author", "Environmental Watch Group");
			b.set("title", "toxic storage leaks outside Saint Petersburg");
			b.set("location", "St. Petersburg, Russia");
			b.set("eventdate", "2001-07-19");
			b.set("entrydate", "2001-07-20");
			b.set("keywords", "toxic storage, leaks, environment");
			b.set("summary", "The chemical plant facilities south of St. Petersburg are using faulty storage mechanisms for storing toxic chemicals that it no longer uses.");
			b.set("publicinfo", "Toxic storage tanks used by the chemical plant facilities south of St. Petersburg are not meeting international standards.  Workers fear that contamination of air and groundwater could ensue.");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.setAllPrivate(true);
			b.set("author", "The Martus Project");
			b.set("title", "woman with broken arm");
			b.set("location", "Colombo");
			b.set("eventdate", "2001-01-29");
			b.set("entrydate", "2002-04-17");
			b.set("keywords", "kidnapping, battery");
			b.set("summary", "Woman's arm was broken when she tried to stop her husband from being kidnapped.");
			b.set("publicinfo", "Two men arrived in the middle of the night and pounded on the door.  The men abducted the husband and the woman tried to stop them.  They broke her arm and she has not seen her husband since.");
			b.setSealed();
			store.saveBulletin(b);
			f.add(b);
		}
	}

	String testDataDirectory;

}
