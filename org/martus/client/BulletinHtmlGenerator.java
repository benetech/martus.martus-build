package org.martus.client;

import org.martus.common.AttachmentProxy;
import org.martus.common.FieldDataPacket;
import org.martus.common.MartusUtilities;

public class BulletinHtmlGenerator
{
	BulletinHtmlGenerator(int widthToUse, MartusApp appToUse)
	{
		width = widthToUse;
		app = appToUse;
	}
	
	public String getHtmlString(Bulletin b)
	{
		String html = "<html>";
		html += "<table width='" + width + "'>";

		String publicSectionTitle = app.getFieldLabel("publicsection");
		html += "<tr><td colspan='2'><u><b>" + publicSectionTitle + "</b></u></td></tr>";

		String allPrivateValueTag = "no";
		if(b.isAllPrivate())
			allPrivateValueTag = "yes";
		html += getFieldHtmlString("allprivate", app.getButtonLabel(allPrivateValueTag));

		String[] standardFieldTags = Bulletin.getStandardFieldNames();
		html += getSectionHtmlString(b, standardFieldTags);
		html += getAttachmentsHtmlString(b.getFieldDataPacket());

		html += "<tr></tr>";
		String privateSectionTitle = app.getFieldLabel("privatesection");
		html += "<tr><td colspan='2'><u><b>" + privateSectionTitle + "</b></u></td></tr>";
		String[] privateFieldTags = Bulletin.getPrivateFieldNames();
		html += getSectionHtmlString(b, privateFieldTags);
		html += getAttachmentsHtmlString(b.getPrivateFieldDataPacket());

		html += "</table>";
		html += "</html>";
		return html;
	}

	private String getSectionHtmlString(Bulletin b, String[] standardFieldTags)
	{
		String sectionHtml = "";
		for(int fieldNum = 0; fieldNum < standardFieldTags.length; ++fieldNum)
		{
			String tag = standardFieldTags[fieldNum];
			String value = MartusUtilities.getXmlEncoded(b.get(tag));
			if(b.getFieldType(tag) == b.DATE)
				value = app.convertStoredToDisplay(value);
			else if(b.getFieldType(tag) == b.CHOICE)
				value = app.getLanguageName(value);
				
			String fieldHtml = getFieldHtmlString(tag, value);
			sectionHtml += fieldHtml;
		}
		return sectionHtml;
	}

	private String getAttachmentsHtmlString(FieldDataPacket fdp)
	{
		String attachmentList = "";
		AttachmentProxy[] attachments = fdp.getAttachments();
		for(int i = 0 ; i < attachments.length ; ++i)
		{
			String label = attachments[i].getLabel();
			attachmentList += "<p>" + label + "</p>";
		}
		return getFieldHtmlString("attachments", attachmentList);
	}

	private String getFieldHtmlString(String tag, String value)
	{
		String fieldHtml = "<tr><td width='15%' align='right' valign='top'>" + 
						app.getFieldLabel(tag) + 
						"</td><td valign='top'>" + value + "</td></tr>";
		return fieldHtml;
	}
	
	int width;
	MartusApp app;
}
