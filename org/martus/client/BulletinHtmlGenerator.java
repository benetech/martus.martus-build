package org.martus.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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
		StringBuffer html = new StringBuffer(1000);
		html.append("<html>");
		html.append("<table width='");
		html.append(Integer.toString(width));
		html.append("'>");

		String publicSectionTitle = app.getFieldLabel("publicsection");
		html.append("<tr><td colspan='2'><u><b>");
		html.append(publicSectionTitle);
		html.append("</b></u></td></tr>");
		html.append("\n");

		String allPrivateValueTag = "no";
		if(b.isAllPrivate())
			allPrivateValueTag = "yes";
		html.append(getFieldHtmlString("allprivate", app.getButtonLabel(allPrivateValueTag)));

		String[] standardFieldTags = Bulletin.getStandardFieldNames();
		html.append(getSectionHtmlString(b, standardFieldTags));
		html.append(getAttachmentsHtmlString(b.getFieldDataPacket()));

		html.append("<tr></tr>");
		String privateSectionTitle = app.getFieldLabel("privatesection");
		html.append("<tr><td colspan='2'><u><b>");
		html.append(privateSectionTitle);
		html.append("</b></u></td></tr>");
		html.append("\n");
		String[] privateFieldTags = Bulletin.getPrivateFieldNames();
		html.append(getSectionHtmlString(b, privateFieldTags));
		html.append(getAttachmentsHtmlString(b.getPrivateFieldDataPacket()));

		html.append("</table>");
		html.append("</html>");
		return new String(html);
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
			else if(b.getFieldType(tag) == b.MULTILINE)
				value = insertNewlines(value);
				
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
		StringBuffer fieldHtml = new StringBuffer(value.length() + 100);
		fieldHtml.append("<tr><td width='15%' align='right' valign='top'>");
		fieldHtml.append(app.getFieldLabel(tag));
		fieldHtml.append("</td>");
		fieldHtml.append("<td valign='top'>");
		fieldHtml.append(value);
		fieldHtml.append("</td></tr>");
		fieldHtml.append("\n");
		return new String(fieldHtml);
	}
	
	private String insertNewlines(String value)
	{
		final String P_TAG_BEGIN = "<p>";
		final String P_TAG_END = "</p>";
		StringBuffer html = new StringBuffer(value.length() + 100);
		html.append(P_TAG_BEGIN);

		try
		{
			BufferedReader reader = new BufferedReader(new StringReader(value));
			String thisParagraph = null;
			while((thisParagraph = reader.readLine()) != null)
			{
				html.append(thisParagraph);
				html.append(P_TAG_END);
				html.append(P_TAG_BEGIN);
			}
		}
		catch (IOException e)
		{
			html.append("...?");
		}

		html.append(P_TAG_END);
		return new String(html);
	}
	
	int width;
	MartusApp app;
}
