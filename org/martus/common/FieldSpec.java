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

public class FieldSpec
{
	public FieldSpec(String thisFieldDescription, int typeToUse)
	{
		this(thisFieldDescription);
		type = typeToUse;
	}

	public FieldSpec(String thisFieldDescription)
	{
		tag = extractFieldSpecElement(thisFieldDescription, TAG_ELEMENT_NUMBER);
		label = extractFieldSpecElement(thisFieldDescription, LABEL_ELEMENT_NUMBER);
		String unknownStuff = extractFieldSpecElement(thisFieldDescription, UNKNOWN_ELEMENT_NUMBER);
		if(!unknownStuff.equals(""))
			hasUnknown = true;
	}
	
	public String getTag()
	{
		return tag;
	}
	
	public String getLabel()
	{
		return label;
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
	public static final int TYPE_UNKNOWN = 6;

}
