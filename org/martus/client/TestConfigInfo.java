package org.martus.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

		info.setAuthor(sampleSource);
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
		assertEquals(label + ": sampleSource", sampleSource, info.getAuthor());
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
	final String sampleSource = "source";
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
