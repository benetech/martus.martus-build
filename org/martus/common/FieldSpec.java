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

package org.martus.common;

import java.util.Vector;

import org.martus.common.bulletin.BulletinConstants;

public class FieldSpec
{
	public FieldSpec(String thisFieldDescription, int typeToUse)
	{
		initializeFromDescription(thisFieldDescription);

		type = typeToUse;
	}

	public FieldSpec(String thisFieldDescription)
	{
		initializeFromDescription(thisFieldDescription);

		type = getStandardType(tag);
		if(type == TYPE_UNKNOWN && !hasUnknownStuff())
			type = TYPE_NORMAL;
	}

	private void initializeFromDescription(String thisFieldDescription)
	{
		tag = extractFieldSpecElement(thisFieldDescription, TAG_ELEMENT_NUMBER);
		label = extractFieldSpecElement(thisFieldDescription, LABEL_ELEMENT_NUMBER);
		String unknownStuff = extractFieldSpecElement(thisFieldDescription, UNKNOWN_ELEMENT_NUMBER);
		if(!unknownStuff.equals(""))
		{
			//System.out.println("FieldSpec.initializeFromDescription unknown: " + tag + ": " + unknownStuff);
			hasUnknown = true;
		}
	}
	
	public String getTag()
	{
		return tag;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public int getType()
	{
		return type;
	}
	
	public boolean hasUnknownStuff()
	{
		return hasUnknown;
	}

	static public FieldSpec[] addFieldSpec(FieldSpec[] existingFieldSpecs, FieldSpec newFieldSpec)
	{
		int oldCount = existingFieldSpecs.length;
		FieldSpec[] tempFieldTags = new FieldSpec[oldCount + 1];
		System.arraycopy(existingFieldSpecs, 0, tempFieldTags, 0, oldCount);
		tempFieldTags[oldCount] = newFieldSpec;
		return tempFieldTags;
	}

	private static String extractFieldSpecElement(String fieldDescription, int elementNumber)
	{
		int elementStart = 0;
		for(int i = 0; i < elementNumber; ++i)
		{
			int comma = fieldDescription.indexOf(FIELD_SPEC_ELEMENT_DELIMITER, elementStart);
			if(comma < 0)
				return "";
			elementStart = comma + 1;
		}
		
		int trailingComma = fieldDescription.indexOf(FIELD_SPEC_ELEMENT_DELIMITER, elementStart);
		if(trailingComma < 0)
			trailingComma = fieldDescription.length();
		return fieldDescription.substring(elementStart, trailingComma);
	}

	static public FieldSpec[] parseFieldSpecsFromString(String delimitedTags)
	{
		FieldSpec[] newFieldSpecs = new FieldSpec[0];
		int tagStart = 0;
		while(tagStart >= 0 && tagStart < delimitedTags.length())
		{
			int delimiter = delimitedTags.indexOf(FIELD_SPEC_DELIMITER, tagStart);
			if(delimiter < 0)
				delimiter = delimitedTags.length();
			String thisFieldDescription = delimitedTags.substring(tagStart, delimiter);
			FieldSpec newFieldSpec = new FieldSpec(thisFieldDescription);
	
			newFieldSpecs = FieldSpec.addFieldSpec(newFieldSpecs, newFieldSpec);
			tagStart = delimiter + 1;
		}
		return newFieldSpecs;
	}

	static public String buildFieldListString(FieldSpec[] fieldSpecs)
	{
		String fieldList = "";
		for(int i = 0; i < fieldSpecs.length; ++i)
		{
			if(i > 0)
				fieldList += FIELD_SPEC_DELIMITER;
			FieldSpec spec = fieldSpecs[i];
			fieldList += spec.getTag();
			if(spec.getLabel().length() != 0)
				fieldList += FIELD_SPEC_ELEMENT_DELIMITER + spec.getLabel();
		}
		return fieldList;
	}
	
	public static FieldSpec[] getDefaultPublicFieldSpecs()
	{
		if(defaultPublicFieldSpecs == null)
		{
			defaultPublicFieldSpecs = new FieldSpec[] 
			{
				new FieldSpec(BulletinConstants.TAGLANGUAGE, FieldSpec.TYPE_CHOICE),
				new FieldSpec(BulletinConstants.TAGAUTHOR, FieldSpec.TYPE_NORMAL),
				new FieldSpec(BulletinConstants.TAGORGANIZATION, FieldSpec.TYPE_NORMAL),
				new FieldSpec(BulletinConstants.TAGTITLE, FieldSpec.TYPE_NORMAL),
				new FieldSpec(BulletinConstants.TAGLOCATION, FieldSpec.TYPE_NORMAL), 
				new FieldSpec(BulletinConstants.TAGKEYWORDS, FieldSpec.TYPE_NORMAL),
				new FieldSpec(BulletinConstants.TAGEVENTDATE, FieldSpec.TYPE_DATERANGE),
				new FieldSpec(BulletinConstants.TAGENTRYDATE, FieldSpec.TYPE_DATE),
				new FieldSpec(BulletinConstants.TAGSUMMARY, FieldSpec.TYPE_MULTILINE),
				new FieldSpec(BulletinConstants.TAGPUBLICINFO, FieldSpec.TYPE_MULTILINE),
			};
		}
		
		return defaultPublicFieldSpecs;
	}

	public static FieldSpec[] getDefaultPrivateFieldSpecs()
	{
		if(defaultPrivateFieldSpecs == null)
		{
			defaultPrivateFieldSpecs = new FieldSpec[]
			{
				new FieldSpec(BulletinConstants.TAGPRIVATEINFO, FieldSpec.TYPE_MULTILINE),
			};
		}
		
		return defaultPrivateFieldSpecs;
	}

	public static Vector getDefaultPublicFieldTags()
	{
		Vector tags = new Vector();
		FieldSpec[] defaultFields = FieldSpec.getDefaultPublicFieldSpecs();
		for (int i = 0; i < defaultFields.length; i++)
			tags.add(defaultFields[i].getTag());
		return tags;
	}

	public static boolean isCustomFieldTag(String tag)
	{
		FieldSpec[] publicSpecs = getDefaultPublicFieldSpecs();
		for(int i=0; i < publicSpecs.length; ++i)
			if(publicSpecs[i].getTag().equals(tag))
				return false;
				
		FieldSpec[] privateSpecs = getDefaultPrivateFieldSpecs();
		for(int i=0; i < privateSpecs.length; ++i)
			if(privateSpecs[i].getTag().equals(tag))
				return false;
				
		return true;
	}
	
	public static int getStandardType(String tag)
	{
		FieldSpec[] publicSpecs = getDefaultPublicFieldSpecs();
		for(int i=0; i < publicSpecs.length; ++i)
			if(publicSpecs[i].getTag().equals(tag))
				return publicSpecs[i].getType();
				
		FieldSpec[] privateSpecs = getDefaultPrivateFieldSpecs();
		for(int i=0; i < privateSpecs.length; ++i)
			if(privateSpecs[i].getTag().equals(tag))
				return privateSpecs[i].getType();
				
		return TYPE_UNKNOWN;
	}

	String tag;
	int type;
	String label;
	boolean hasUnknown;

	private static final char FIELD_SPEC_DELIMITER = ';';
	private static final char FIELD_SPEC_ELEMENT_DELIMITER = ',';
	private static final int TAG_ELEMENT_NUMBER = 0;
	private static final int LABEL_ELEMENT_NUMBER = 1;
	private static final int UNKNOWN_ELEMENT_NUMBER = 2;

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_MULTILINE = 1;
	public static final int TYPE_DATE = 2;
	public static final int TYPE_CHOICE = 4;
	public static final int TYPE_DATERANGE = 5;
	public static final int TYPE_BOOLEAN = 6;
	public static final int TYPE_UNKNOWN = 99;

	private static FieldSpec[] defaultPublicFieldSpecs;
	private static FieldSpec[] defaultPrivateFieldSpecs;

}
