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

package org.martus.common.bulletin;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.martus.common.FieldSpec;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.Base64.InvalidBase64Exception;

public class Bulletin implements BulletinConstants
{

	public static class DamagedBulletinException extends Exception
	{
	}

	public Bulletin(MartusCrypto securityToUse)
	{
		this(securityToUse, FieldSpec.getDefaultPublicFieldSpecs(), FieldSpec.getDefaultPrivateFieldSpecs());
	}
	
	public Bulletin(MartusCrypto securityToUse, FieldSpec[] publicFieldSpecs, FieldSpec[] privateFieldSpecs)
	{
		security = securityToUse;
		String accountId = security.getPublicKeyString();
		UniversalId headerUid = BulletinHeaderPacket.createUniversalId(accountId);
		UniversalId dataUid = FieldDataPacket.createUniversalId(accountId);
		UniversalId privateDataUid = FieldDataPacket.createUniversalId(accountId);

		isValidFlag = true;
		fieldData = createPublicFieldDataPacket(dataUid, publicFieldSpecs);
		fieldData.setEncrypted(true);
		privateFieldData = createPrivateFieldDataPacket(privateDataUid, privateFieldSpecs);
		privateFieldData.setEncrypted(true);
		header = createHeaderPacket(headerUid);
		header.setFieldDataPacketId(dataUid.getLocalId());
		header.setPrivateFieldDataPacketId(privateDataUid.getLocalId());
		setPendingPublicAttachments(new Vector());
		setPendingPrivateAttachments(new Vector());

		clear();
	}

	public MartusCrypto getSignatureGenerator()
	{
		return security;
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

	public void setIsValid(boolean isValid)
	{
		isValidFlag = isValid;
	}

	public boolean isValid()
	{
		return isValidFlag;
	}
	
	public boolean hasUnknownTags()
	{
		if(getBulletinHeaderPacket().hasUnknownTags())
			return true;
		if(getFieldDataPacket().hasUnknownTags())
			return true;
		if(getPrivateFieldDataPacket().hasUnknownTags())
			return true;
		return false;
	}
	
	public boolean hasUnknownCustomField()
	{
		FieldDataPacket fdp = getFieldDataPacket();
		FieldSpec[] specs = fdp.getFieldSpecs();
		for(int i=0; i < specs.length; ++i)
		{
			if(specs[i].hasUnknownStuff())
				return true;
		}
		
		return false;
	}

	public long getLastSavedTime()
	{
		return getBulletinHeaderPacket().getLastSavedTime();
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
	
	public FieldSpec[] getPublicFieldSpecs()
	{
		return fieldData.getFieldSpecs();
	}
	
	public FieldSpec[] getPrivateFieldSpecs()
	{
		return privateFieldData.getFieldSpecs();
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
		if(fieldName.equals(Bulletin.TAGSTATUS))
		{
			if(isDraft())
				return BulletinConstants.STATUSDRAFT;
			return BulletinConstants.STATUSSEALED;
		}
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
			byte[] sessionKeyBytes = getSignatureGenerator().createSessionKey();
			AttachmentPacket ap = new AttachmentPacket(getAccount(), sessionKeyBytes, rawFile, getSignatureGenerator());
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
			byte[] sessionKeyBytes = getSignatureGenerator().createSessionKey();
			AttachmentPacket ap = new AttachmentPacket(getAccount(), sessionKeyBytes, rawFile, getSignatureGenerator());
			bhp.addPrivateAttachmentLocalId(ap.getLocalId());
			getPendingPrivateAttachments().add(ap);
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

	public void clear()
	{
		getBulletinHeaderPacket().clearAttachments();
		getFieldDataPacket().clearAll();
		getPrivateFieldDataPacket().clearAll();
		pendingPublicAttachments.clear();
		getPendingPrivateAttachments().clear();
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

	public boolean contains(String lookFor)
	{
		FieldSpec fields[] = fieldData.getFieldSpecs();
		String lookForLowerCase = lookFor.toLowerCase();
		for(int f = 0; f < fields.length; ++f)
		{
			String contents = get(fields[f].getTag()).toLowerCase();
			if(contents.indexOf(lookForLowerCase) >= 0)
				return true;
		}
		return false;
	}

	public boolean withinDates(String beginDate, String endDate)
	{
		String eventDate = fieldData.get(Bulletin.TAGEVENTDATE);
		String entryDate = fieldData.get(Bulletin.TAGENTRYDATE);
		
		if(eventDate.compareTo(beginDate) >= 0 && eventDate.compareTo(endDate) <= 0)
			return true;
		if(entryDate.compareTo(beginDate) >= 0 && entryDate.compareTo(endDate) <= 0)
			return true;
			
		int comma = eventDate.indexOf(MartusFlexidate.DATE_RANGE_SEPARATER);		
		if (comma > 0 && isWithinFlexiDates(eventDate.substring(comma+1), beginDate, endDate))
			return true;

		return false;
	}
	
	private boolean isWithinFlexiDates(String flexiDate, String searchBeginDate, String searchEndDate)
	{		
		if (flexiDate.indexOf(MartusFlexidate.FLEXIDATE_RANGE_DELIMITER) < 0)
			return false;
					
		MartusFlexidate mf = new MartusFlexidate(flexiDate);		
		DateFormat df = Bulletin.getStoredDateFormat();						
		String beginDate = df.format(mf.getBeginDate());
		String endDate = df.format(mf.getEndDate());

		if (beginDate.compareTo(searchEndDate) > 0 ||
			endDate.compareTo(searchBeginDate) < 0)
			return false;		
		return true;
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

	public static DateFormat getStoredDateFormat()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setLenient(false);
		return df;
	}

	public DatabaseKey getDatabaseKeyForLocalId(String localId)
	{
		UniversalId uidFdp = UniversalId.createFromAccountAndLocalId(getAccount(), localId);
		return new DatabaseKey(uidFdp);
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

	public boolean isAllPrivate()
	{

		BulletinHeaderPacket bhp = getBulletinHeaderPacket();
		if(!bhp.hasAllPrivateFlag())
		{
			FieldDataPacket fdp = getFieldDataPacket();
			bhp.setAllPrivate(fdp.isEncrypted());
		}
		return bhp.isAllPrivate();
	}

	public void setAllPrivate(boolean newValue)
	{
		getBulletinHeaderPacket().setAllPrivate(newValue);
	}

	public void createDraftCopyOf(Bulletin other, Database otherDatabase) throws
		CryptoException, 
		InvalidPacketException, 
		SignatureVerificationException, 
		WrongPacketTypeException, 
		IOException, 
		InvalidBase64Exception
	{
		this.clear();

		setDraft();
		setAllPrivate(other.isAllPrivate());

		{
			FieldSpec fields[] = fieldData.getFieldSpecs();
			for(int f = 0; f < fields.length; ++f)
			{
				set(fields[f].getTag(), other.get(fields[f].getTag()));
			}
		}

		{
			FieldSpec privateFields[] = privateFieldData.getFieldSpecs();
			for(int f = 0; f < privateFields.length; ++f)
			{
				set(privateFields[f].getTag(), other.get(privateFields[f].getTag()));
			}
		}

		MartusCrypto security = getSignatureGenerator();
		AttachmentProxy[] attachmentPublicProxies = other.getPublicAttachments();
		for(int aIndex = 0; aIndex < attachmentPublicProxies.length; ++aIndex)
		{
			AttachmentProxy ap = attachmentPublicProxies[aIndex];
			ap = getAsFileProxy(ap, otherDatabase, Bulletin.STATUSDRAFT, security);
			addPublicAttachment(ap);
		}

		AttachmentProxy[] attachmentPrivateProxies = other.getPrivateAttachments();
		for(int aIndex = 0; aIndex < attachmentPrivateProxies.length; ++aIndex)
		{
			AttachmentProxy ap = attachmentPrivateProxies[aIndex];
			ap = getAsFileProxy(ap, otherDatabase, Bulletin.STATUSDRAFT, security);
			addPrivateAttachment(ap);
		}

		pendingPublicAttachments.addAll(other.pendingPublicAttachments);
		getPendingPrivateAttachments().addAll(other.getPendingPrivateAttachments());
	}

	public AttachmentProxy getAsFileProxy(AttachmentProxy ap, Database otherDatabase, String status, MartusCrypto security)
		throws
			IOException,
			CryptoException,
			InvalidPacketException,
			SignatureVerificationException,
			WrongPacketTypeException,
			InvalidBase64Exception
	{
		if(ap.getFile() != null) 
			return ap;
		if(otherDatabase == null)
			return ap;

		DatabaseKey key = DatabaseKey.createKey(ap.getUniversalId(),status);
		InputStreamWithSeek packetIn = otherDatabase.openInputStream(key, security);
		if(packetIn == null)
			return ap;

		try
		{
			return AttachmentProxy.createFileProxyFromAttachmentPacket(packetIn, ap, security);
		}
		finally
		{
			packetIn.close();
		}
	}

	static String getFirstOfThisYear()
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.MONTH, 0);
		cal.set(GregorianCalendar.DATE, 1);
		DateFormat df = getStoredDateFormat();
		return df.format(cal.getTime());
	}

	public static String getLastDayOfThisYear()
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.MONTH, 11);
		cal.set(GregorianCalendar.DATE, 31);
		DateFormat df = getStoredDateFormat();
		return df.format(cal.getTime());
	}

	public static String getToday()
	{
		DateFormat df = getStoredDateFormat();
		return df.format(new Date());
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

	private void setPendingPublicAttachments(Vector pendingPublicAttachments)
	{
		this.pendingPublicAttachments = pendingPublicAttachments;
	}

	public Vector getPendingPublicAttachments()
	{
		return pendingPublicAttachments;
	}

	private void setPendingPrivateAttachments(Vector pendingPrivateAttachments)
	{
		this.pendingPrivateAttachments = pendingPrivateAttachments;
	}

	public Vector getPendingPrivateAttachments()
	{
		return pendingPrivateAttachments;
	}
	
	protected BulletinHeaderPacket createHeaderPacket(UniversalId headerUid)
	{
		return new BulletinHeaderPacket(headerUid);
	}

	protected FieldDataPacket createPrivateFieldDataPacket(
		UniversalId privateDataUid,
		FieldSpec[] privateFieldSpecs)
	{
		return createFieldDataPacket(privateDataUid, privateFieldSpecs);
	}

	protected FieldDataPacket createPublicFieldDataPacket(
		UniversalId dataUid,
		FieldSpec[] publicFieldSpecs)
	{
		return createFieldDataPacket(dataUid, publicFieldSpecs);
	}

	protected FieldDataPacket createFieldDataPacket(
		UniversalId dataUid,
		FieldSpec[] publicFieldSpecs)
	{
		return new FieldDataPacket(dataUid, publicFieldSpecs);
	}

	private boolean isValidFlag;
	private MartusCrypto security;
	private BulletinHeaderPacket header;
	private FieldDataPacket fieldData;
	private FieldDataPacket privateFieldData;
	private Vector pendingPublicAttachments;
	private Vector pendingPrivateAttachments;
}
