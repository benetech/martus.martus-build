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

package org.martus.client.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.client.core.ConfigInfo;
import org.martus.common.MartusSecurity;
import org.martus.common.MartusUtilities;
import org.martus.common.TestCaseEnhanced;

public class TestConfigInfo extends TestCaseEnhanced
{
    public TestConfigInfo(String name) throws IOException
	{
        super(name);
		configFile = File.createTempFile("$$testconfig", null);
    }

    public void setUp()
    {
    }

    public void tearDown()
    {
		configFile.delete();
	}

	public void testConstructor()
	{
		ConfigInfo info = new ConfigInfo();
		assertNotNull("source null", info.getAuthor());
		assertNotNull("template null", info.getTemplateDetails());
	}

	public void testBeforeLoad()
	{
		ConfigInfo info = new ConfigInfo();

		assertEquals("No contact info", false, info.hasContactInfo());

		info.setAuthor("fred");
		assertEquals("fred", info.getAuthor());
	}

	public void testHasContactInfo() throws Exception
	{
		ConfigInfo info = new ConfigInfo();
		info.setAuthor("fred");
		assertEquals("author isn't enough contact info?", true, info.hasContactInfo());
		info.setAuthor("");
		info.setOrganization("whatever");
		assertEquals("organization isn't enough contact info?", true, info.hasContactInfo());

	}

	public void testSaveAndLoadFullFile() throws Exception
	{
		ConfigInfo info = new ConfigInfo();
		verifyEmptyInfo(info, "constructor");

		info.setAuthor(sampleAuthor);
		info.setOrganization(sampleOrg);
		info.setEmail(sampleEmail);
		info.setWebPage(sampleWebPage);
		info.setPhone(samplePhone);
		info.setAddress(sampleAddress);
		info.setServerName(sampleServerName);
		info.setServerPublicKey(sampleServerKey);
		info.setTemplateDetails(sampleTemplateDetails);
		info.setHQKey(sampleHQKey);
		info.setSendContactInfoToServer(sampleSendContactInfoToServer);
		verifySampleInfo(info, "afterSet");

		FileOutputStream outputStream = new FileOutputStream(configFile);
		info.save(outputStream);
		outputStream.close();

		info.clear();
		verifyEmptyInfo(info, "clear");

		FileInputStream inputStream = new FileInputStream(configFile);
		info = ConfigInfo.load(inputStream);
		inputStream.close();

		verifySampleInfo(info, "afterLoad");
	}

	public void verifySampleInfo(ConfigInfo info, String label)
	{
		assertEquals(label + ": Full has contact info", true, info.hasContactInfo());
		assertEquals(label + ": sampleSource", sampleAuthor, info.getAuthor());
		assertEquals(label + ": sampleOrg", sampleOrg, info.getOrganization());
		assertEquals(label + ": sampleEmail", sampleEmail, info.getEmail());
		assertEquals(label + ": sampleWebPage", sampleWebPage, info.getWebPage());
		assertEquals(label + ": samplePhone", samplePhone, info.getPhone());
		assertEquals(label + ": sampleAddress", sampleAddress, info.getAddress());
		assertEquals(label + ": sampleServerName", sampleServerName, info.getServerName());
		assertEquals(label + ": sampleServerKey", sampleServerKey, info.getServerPublicKey());
		assertEquals(label + ": sampleTemplateDetails", sampleTemplateDetails, info.getTemplateDetails());
		assertEquals(label + ": sampleHQKey", sampleHQKey, info.getHQKey());
		assertEquals(label + ": sampleSendContactInfoToServer", sampleSendContactInfoToServer, info.shouldContactInfoBeSentToServer());

	}

	public void verifyEmptyInfo(ConfigInfo info, String label)
	{
		assertEquals(label + ": Full has contact info", false, info.hasContactInfo());
		assertEquals(label + ": sampleSource", "", info.getAuthor());
		assertEquals(label + ": sampleOrg", "", info.getOrganization());
		assertEquals(label + ": sampleEmail", "", info.getEmail());
		assertEquals(label + ": sampleWebPage", "", info.getWebPage());
		assertEquals(label + ": samplePhone", "", info.getPhone());
		assertEquals(label + ": sampleAddress", "", info.getAddress());
		assertEquals(label + ": sampleServerName", "", info.getServerName());
		assertEquals(label + ": sampleServerKey", "", info.getServerPublicKey());
		assertEquals(label + ": sampleTemplateDetails", "", info.getTemplateDetails());
		assertEquals(label + ": sampleHQKey", "", info.getHQKey());
		assertEquals(label + ": sampleSendContactInfoToServer", false, info.shouldContactInfoBeSentToServer());
	}

	public void testGetContactInfo() throws Exception
	{
		ConfigInfo newInfo = new ConfigInfo();
		newInfo.setAuthor(sampleAuthor);
		newInfo.setAddress(sampleAddress);
		newInfo.setPhone(samplePhone);
		MartusSecurity signer = new MartusSecurity();
		signer.createKeyPair(512);
		Vector contactInfo = newInfo.getContactInfo(signer);
		String publicKey = (String)contactInfo.get(0);

		assertEquals("Not the publicKey?", signer.getPublicKeyString(), publicKey);
		int contentSize = ((Integer)(contactInfo.get(1))).intValue();
		assertEquals("Not the correct size?", contentSize + 3, contactInfo.size());
		assertEquals("Author not correct?", sampleAuthor, contactInfo.get(2));
		assertEquals("Address not correct?", sampleAddress, contactInfo.get(7));
		assertEquals("phone not correct?", samplePhone, contactInfo.get(6));
		String signature = (String)contactInfo.get(contactInfo.size()-1);
		contactInfo.remove(contactInfo.size()-1);
		assertTrue("Signature failed?", MartusUtilities.verifySignature(contactInfo, signer, publicKey, signature));
	}

	public void testStreamSaveAndLoadEmpty() throws Exception
	{
		ConfigInfo emptyInfo = new ConfigInfo();
		ByteArrayOutputStream emptyOutputStream = new ByteArrayOutputStream();
		emptyInfo.save(emptyOutputStream);

		emptyInfo.setAuthor("should go away");
		ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(emptyOutputStream.toByteArray());
		emptyInfo = ConfigInfo.load(emptyInputStream);
		assertEquals("should have cleared", "", emptyInfo.getAuthor());
	}

	public void testStreamSaveAndLoadNonEmpty() throws Exception
	{
		ConfigInfo info = new ConfigInfo();
		String server = "server";
		info.setServerName(server);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		info.save(outputStream);
		info.setServerName("should be reverted");

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		info = ConfigInfo.load(inputStream);
		assertEquals("should have reverted", server, info.getServerName());
	}

	public void testRemoveHQKey() throws Exception
	{
		ConfigInfo info = new ConfigInfo();
		String hqKey = "HQKey";
		info.setHQKey(hqKey);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		info.save(outputStream);
		info.clearHQKey();
		assertEquals("HQ Key Should be cleared", "", info.getHQKey());

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		info = ConfigInfo.load(inputStream);
		assertEquals("HQ key should have reverted", hqKey, info.getHQKey());
	}


	public void testTemplateDetails() throws Exception
	{
		ConfigInfo info = new ConfigInfo();
		info.setTemplateDetails(sampleTemplateDetails);
		assertEquals("Details not set?", sampleTemplateDetails, info.getTemplateDetails());
	}

	File configFile;
	final String sampleAuthor = "author";
	final String sampleOrg = "org";
	final String sampleEmail = "email";
	final String sampleWebPage = "web";
	final String samplePhone = "phone";
	final String sampleAddress = "address\nline2";
	final String sampleServerName = "server name";
	final String sampleServerKey = "server pub key";
	final String sampleTemplateDetails = "details\ndetail2";
	final String sampleHQKey = "1234324234";
	final boolean sampleSendContactInfoToServer = true;
}
