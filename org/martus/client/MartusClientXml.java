package org.martus.client;

import org.martus.common.BulletinConstants;
import org.martus.common.MartusXml;

public class MartusClientXml 
{

	public static String getFolderListTagStart()
	{
		return MartusXml.getTagStart(MartusClientXml.tagFolderList);
	}

	public static String getFolderListTagEnd()
	{
		return MartusXml.getTagEnd(MartusClientXml.tagFolderList);
	}

	public static String getBulletinTag(Bulletin b)
	{
		
		String tagStart = MartusXml.getTagStart(MartusClientXml.tagBulletin,
					MartusClientXml.attrBulletinId, b.getUniversalIdString(),
					MartusClientXml.attrBulletinEventDate, b.get(BulletinConstants.TAGEVENTDATE));
		String tagEnd = MartusXml.getTagEnd(MartusClientXml.tagBulletin);

		return tagStart + tagEnd;
	}

	public static String getFolderTagStart(String name)
	{
		return MartusXml.getTagStart(
			MartusClientXml.tagFolder,
			MartusClientXml.attrFolder,
			name);
	}

	public static String getFolderTagEnd()
	{
		return MartusXml.getTagEnd(MartusClientXml.tagFolder);
	}

	public final static String tagBulletin = "Bulletin";
	public final static String attrBulletinId = "id";
	public final static String attrBulletinEventDate = "eventdate";

	public final static String tagFolderList = "FolderList";

	public final static String tagFolder = "Folder";
	public final static String attrFolder = "name";

	public final static String tagId = "Id";
}
