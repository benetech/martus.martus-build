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

package org.martus.client.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Vector;

import org.martus.common.MartusCrypto;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusCrypto.MartusSignatureException;

public class ConfigInfo implements Serializable
{
	public ConfigInfo()
	{
		clear();
	}

	public boolean hasContactInfo()
	{
		if(author != null && author.length() > 0)
			return true;

		if(organization != null && organization.length() > 0)
			return true;

		return false;
	}

	public void setAuthor(String newSource)		{ author = newSource; }
	public void setOrganization(String newOrg)		{ organization = newOrg; }
	public void setEmail(String newEmail)			{ email = newEmail; }
	public void setWebPage(String newWebPage)		{ webPage = newWebPage; }
	public void setPhone(String newPhone)			{ phone = newPhone; }
	public void setAddress(String newAddress)		{ address = newAddress; }
	public void setServerName(String newServerName){ serverName = newServerName; }
	public void setServerPublicKey(String newServerPublicKey){serverPublicKey = newServerPublicKey; }
	public void setTemplateDetails(String newTemplateDetails){ templateDetails = newTemplateDetails; }
	public void setHQKey(String newHQKey)			{ hqKey = newHQKey; }
	public void setSendContactInfoToServer(boolean newSendContactInfoToServer) {sendContactInfoToServer = newSendContactInfoToServer; }
	public void clearHQKey()						{ hqKey = ""; }
	public void clearPromptUserRequestSendToServer() { mustAskUserToSendToServer = false; }

	public short getVersion()			{ return version; }
	public String getAuthor()			{ return author; }
	public String getOrganization()	{ return organization; }
	public String getEmail()			{ return email; }
	public String getWebPage()			{ return webPage; }
	public String getPhone()			{ return phone; }
	public String getAddress()			{ return address; }
	public String getServerName()		{ return serverName; }
	public String getServerPublicKey()	{ return serverPublicKey; }
	public String getTemplateDetails() { return templateDetails; }
	public String getHQKey() 			{ return hqKey; }
	public boolean shouldContactInfoBeSentToServer() { return sendContactInfoToServer; }
	public boolean promptUserRequestSendToServer() { return mustAskUserToSendToServer; }

	public void clear()
	{
		version = VERSION;
		author = "";
		organization = "";
		email = "";
		webPage = "";
		phone = "";
		address = "";
		serverName = "";
		serverPublicKey="";
		templateDetails = "";
		hqKey = "";
		sendContactInfoToServer = false;
		mustAskUserToSendToServer = false;
	}

	public Vector getContactInfo(MartusCrypto signer) throws
		MartusSignatureException
	{
		Vector contactInfo = new Vector();
		contactInfo.add(signer.getPublicKeyString());
		contactInfo.add(new Integer(6));
		contactInfo.add(author);
		contactInfo.add(organization);
		contactInfo.add(email);
		contactInfo.add(webPage);
		contactInfo.add(phone);
		contactInfo.add(address);
		String signature = MartusUtilities.sign(contactInfo, signer);
		contactInfo.add(signature);
		return contactInfo;
	}

	public static ConfigInfo load(InputStream inputStream)
	{
		ConfigInfo loaded =  new ConfigInfo();
		try
		{
			DataInputStream in = new DataInputStream(inputStream);
			loaded.version = in.readShort();
			loaded.author = in.readUTF();
			loaded.organization = in.readUTF();
			loaded.email = in.readUTF();
			loaded.webPage = in.readUTF();
			loaded.phone = in.readUTF();
			loaded.address = in.readUTF();
			loaded.serverName = in.readUTF();
			loaded.templateDetails = in.readUTF();
			loaded.hqKey = in.readUTF();
			loaded.serverPublicKey = in.readUTF();
			if(loaded.version > 1)
				loaded.sendContactInfoToServer = in.readBoolean();
			else
				loaded.mustAskUserToSendToServer = true;
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
			out.writeUTF(author);
			out.writeUTF(organization);
			out.writeUTF(email);
			out.writeUTF(webPage);
			out.writeUTF(phone);
			out.writeUTF(address);
			out.writeUTF(serverName);
			out.writeUTF(templateDetails);
			out.writeUTF(hqKey);
			out.writeUTF(serverPublicKey);
			out.writeBoolean(sendContactInfoToServer);
			out.close();
		}
		catch(Exception e)
		{
			System.out.println("ConfigInfo.save error: " + e);
		}
	}

	private boolean mustAskUserToSendToServer;

	final short VERSION = 3;
	//Version 1
	private short version;
	private String author;
	private String organization;
	private String email;
	private String webPage;
	private String phone;
	private String address;
	private String serverName;
	private String serverPublicKey;
	private String templateDetails;
	private String hqKey;
	//Version 2
	private boolean sendContactInfoToServer;
	//Version 3 flag to indicate AccountMap.txt is signed.

}
