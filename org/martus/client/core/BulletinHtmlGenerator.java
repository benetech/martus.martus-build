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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.martus.client.swingui.UiLocalization;
import org.martus.common.FieldSpec;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.FieldDataPacket;

public class BulletinHtmlGenerator
{
	public BulletinHtmlGenerator(int widthToUse, UiLocalization localizationToUse)
	{
		width = widthToUse;
		localization = localizationToUse;
	}

	public String getHtmlString(Bulletin b)
	{
		StringBuffer html = new StringBuffer(1000);
		html.append("<html>");
		html.append("<table width='");
		html.append(Integer.toString(width));
		html.append("'>");

		String publicSectionTitle = localization.getFieldLabel("publicsection");
		html.append("<tr><td colspan='2'><u><b>");
		html.append(publicSectionTitle);
		html.append("</b></u></td></tr>");
		html.append("\n");

		String allPrivateValueTag = "no";
		if(b.isAllPrivate())
			allPrivateValueTag = "yes";
		html.append(getFieldHtmlString("allprivate", localization.getButtonLabel(allPrivateValueTag)));

		FieldSpec[] standardFieldTags = b.getPublicFieldSpecs();
		html.append(getSectionHtmlString(b, standardFieldTags));
		html.append(getAttachmentsHtmlString(b.getFieldDataPacket()));

		html.append("<tr></tr>");
		String privateSectionTitle = localization.getFieldLabel("privatesection");
		html.append("<tr><td colspan='2'><u><b>");
		html.append(privateSectionTitle);
		html.append("</b></u></td></tr>");
		html.append("\n");
		FieldSpec[] privateFieldTags = b.getPrivateFieldSpecs();
		html.append(getSectionHtmlString(b, privateFieldTags));
		html.append(getAttachmentsHtmlString(b.getPrivateFieldDataPacket()));

		html.append("</table>");
		html.append("</html>");
		return new String(html);
	}

	private String getSectionHtmlString(Bulletin b, FieldSpec[] standardFieldTags)
	{
		String sectionHtml = "";
		for(int fieldNum = 0; fieldNum < standardFieldTags.length; ++fieldNum)
		{
			String tag = standardFieldTags[fieldNum].getTag();
			String value = MartusUtilities.getXmlEncoded(b.get(tag));
			if(Bulletin.getFieldType(tag) == Bulletin.DATE)
				value = localization.convertStoredDateToDisplay(value);
			else if(Bulletin.getFieldType(tag) == Bulletin.CHOICE)
				value = localization.getLanguageName(value);
			else if(Bulletin.getFieldType(tag) == Bulletin.MULTILINE)
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
		fieldHtml.append(localization.getFieldLabel(tag));
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
	UiLocalization localization;
}
