package org.martus.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class ConfigInfo implements Serializable
{
	public ConfigInfo()
	{
		clear();
	}

	public boolean hasContactInfo()
	{
		if(source == null)
			return false;
		if(source.length() == 0)
			return false;

		return true;
	}

	public void setSource(String newSource)		{ source = newSource; }
	public void setOrganization(String newOrg)		{ organization = newOrg; }
	public void setEmail(String newEmail)			{ email = newEmail; }
	public void setWebPage(String newWebPage)		{ webPage = newWebPage; }
	public void setPhone(String newPhone)			{ phone = newPhone; }
	public void setAddress(String newAddress)		{ address = newAddress; }
	public void setServerName(String newServerName){ serverName = newServerName; }
	public void setServerPublicKey(String newServerPublicKey){serverPublicKey = newServerPublicKey; }
	public void setTemplateDetails(String newTemplateDetails){ templateDetails = newTemplateDetails; }
	public void setHQKey(String newHQKey)			{ hqKey = newHQKey; }
	public void clearHQKey()						{ hqKey = ""; }

	public String getSource()			{ return source; }
	public String getOrganization()	{ return organization; }
	public String getEmail()			{ return email; }
	public String getWebPage()			{ return webPage; }
	public String getPhone()			{ return phone; }
	public String getAddress()			{ return address; }
	public String getServerName()		{ return serverName; }
	public String getServerPublicKey()	{ return serverPublicKey; }
	public String getTemplateDetails() { return templateDetails; }
	public String getHQKey() 			{ return hqKey; }

	public void clear()
	{
		source = "";
		organization = "";
		email = "";
		webPage = "";
		phone = "";
		address = "";
		serverName = "";
		serverPublicKey="";
		templateDetails = "";
		hqKey = "";
	}

	public static ConfigInfo load(InputStream inputStream)
	{
		ConfigInfo loaded =  new ConfigInfo();
		try
		{
			DataInputStream in = new DataInputStream(inputStream);
			short version = in.readShort();
			loaded.source = in.readUTF();
			loaded.organization = in.readUTF();
			loaded.email = in.readUTF();
			loaded.webPage = in.readUTF();
			loaded.phone = in.readUTF();
			loaded.address = in.readUTF();
			loaded.serverName = in.readUTF();
			loaded.templateDetails = in.readUTF();
			loaded.hqKey = in.readUTF();
			loaded.serverPublicKey = in.readUTF();
			in.close();
		}
		catch (Exception e)
		{
			System.out.println("ConfigInfo.load " + e);
		}
		return loaded;
	}

	public void save(OutputStream outputStream)
	{
		try
		{
			DataOutputStream out = new DataOutputStream(outputStream);
			out.writeShort(VERSION);
			out.writeUTF(source);
			out.writeUTF(organization);
			out.writeUTF(email);
			out.writeUTF(webPage);
			out.writeUTF(phone);
			out.writeUTF(address);
			out.writeUTF(serverName);
			out.writeUTF(templateDetails);
			out.writeUTF(hqKey);
			out.writeUTF(serverPublicKey);
			out.close();
		}
		catch(Exception e)
		{
			System.out.println("ConfigInfo.save error: " + e);
		}
	}

	private String source;
	private String organization;
	private String email;
	private String webPage;
	private String phone;
	private String address;
	private String serverName;
	private String serverPublicKey;
	private String templateDetails;
	private String hqKey;
	final short VERSION = 1;
}
