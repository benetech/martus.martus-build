package org.martus.client;

import org.martus.common.BulletinConstants;
import org.martus.common.MartusXml;

public class MartusClientXml 
{

	public static String getFolderListTagStart()
	{
		return MartusXml.getTagStart(MartusXml.tagFolderList);
	}

	public static String getFolderListTagEnd()
	{
		return MartusXml.getTagEnd(MartusXml.tagFolderList);
	}

	public static String getBulletinTagStart(Bulletin b)
	{
		return MartusXml.getTagStart(
			MartusXml.tagBulletin,
			MartusXml.attrBulletinId,
			b.getUniversalIdString(),
			MartusXml.attrBulletinEventDate,
			b.get(BulletinConstants.TAGEVENTDATE));
	}

	public static String getBulletinTagEnd()
	{
		return MartusXml.getTagEnd(MartusXml.tagBulletin);
	}

	public static String getFolderTagStart(String name)
	{
		return MartusXml.getTagStart(
			MartusXml.tagFolder,
			MartusXml.attrFolder,
			name);
	}

	public static String getFolderTagEnd()
	{
		return MartusXml.getTagEnd(MartusXml.tagFolder);
	}
}
