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

package org.martus.client.swingui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.martus.client.core.ChoiceItem;
import org.martus.client.core.DateUtilities;
import org.martus.client.core.Localization;
import org.martus.client.core.MartusApp;
import org.martus.common.MartusUtilities;
import org.martus.util.UnicodeWriter;


public class UiLocalization extends Localization
{
	public static final String[] AVAILABLE_MTF_LANGUAGE_RESOURCES = {"es", "ru"};
    public static void main (String args[])
	{
		if(args.length != 2)
		{
			System.out.println("If you specify a language code and output filename, " +
								"this will write out a file");
			System.out.println("that contains all the existing translations " +
								"for language xx, plus placeholder");
			System.out.println("tags for all the untranslated strings.");
			System.out.println("Example of a language code: es = Spanish");
			System.exit(1);
		}

		String languageCode = args[0].toLowerCase();
		if(languageCode.length() != 2 ||
			!Character.isLetter(languageCode.charAt(0)) ||
			!Character.isLetter(languageCode.charAt(1)))
		{
			System.out.println("Invalid language code. Must be two letters (e.g. 'es')");
			System.exit(2);
		}

		System.out.println("Exporting translations for: " + languageCode);
		UiLocalization bd = new UiLocalization(MartusApp.getTranslationsDirectory());
		bd.loadTranslationFile(languageCode);
		File outputFile = new File(args[1]);

		try
		{
			UnicodeWriter writer = new UnicodeWriter(outputFile);
			bd.exportTranslations(languageCode, writer);

			writer.close();
			System.out.println("Success");
		}
		catch(Exception e)
		{
			System.out.println("FAILED: " + e);
			System.exit(3);
		}

    }

	public void exportTranslations(String languageCode, UnicodeWriter writer)
		throws IOException 
	{
		setCurrentLanguageCode("en");
		writer.writeln("# Martus Client Translation File");
		writer.writeln("# Language code:  " + languageCode);
		writer.writeln("# Language name:  " + getLanguageName(languageCode));
		writer.writeln("# Exported date:  " + new Date().toString());
		writer.writeln("# Client version: " + UiConstants.versionLabel);
		writer.writeln("# Client build:   " + MartusUtilities.getVersionDate());
		writer.writeln("#");
		writer.writeln("# Lines beginning with # are comments and are ignored by Martus");
		writer.writeln("# Each entry consists of: KEY=VALUE");
		writer.writeln("# Do not modify any KEY. Do localize every VALUE.");
		writer.writeln("# Each untranslated VALUE has <> around it. As you translate each ");
		writer.writeln("#    VALUE, remove its <>");
		writer.writeln("# The file MUST be saved in UTF-8 format!");
		writer.writeln("# If you name it Martus-xx.mtf (where xx is the correct language code) and");
		writer.writeln("#    put the file in c:\\Martus, then Martus will automatically read it.");
		writer.writeln("# In Martus, to choose a language, go to Options/Preferences.");
		writer.writeln("#");
		Vector keys = getAllTranslationStrings(languageCode);
		for(int i = 0; i < keys.size(); ++i)
		{
			String thisString = (String)keys.get(i);
			writeWithNewlinesEncoded(writer, thisString);
		}
	}

	public static void writeWithNewlinesEncoded(UnicodeWriter writer, String thisString)
		throws IOException
	{
		final String NEWLINE = System.getProperty("line.separator");
		BufferedReader reader = new BufferedReader(new StringReader(thisString));
		boolean additionalLine = false;
		while(true)
		{
			String thisLine = reader.readLine();
			if(thisLine == null)
				break;
			if(additionalLine)
				writer.write("\\n");
			additionalLine = true;
			writer.write(thisLine);
		}
		writer.write(NEWLINE);
		reader.close();
	}

    public UiLocalization(File directoryToUse)
    {
    	super(directoryToUse);
		loadEnglishTranslations();
		setCurrentDateFormatCode(DateUtilities.MDY_SLASH.getCode());
	}
	
	public String getLabel(String languageCode, String category, String tag, String defaultResult)
	{
		return getLabel(languageCode, category + ":" + tag, defaultResult);
	}

	private String getLabel(String languageCode, String key, String defaultResult)
	{
		String result = null;
		Map stringMap = getStringMap(languageCode);
		if(stringMap != null)
			result = (String)stringMap.get(key);
		if(result == null && !languageCode.equals(ENGLISH))
			result = "<" + getLabel(ENGLISH, key, defaultResult) + ">";
		if(result == null)
			result = defaultResult;
		return result;
	}

	private ChoiceItem getLanguageChoiceItem(String filename)
	{
		String code = getLanguageCodeFromFilename(filename);
		String name = getLabel(ENGLISH, "language", code, "Unknown: " + code);
		return new ChoiceItem(code, name);
	}

	public ChoiceItem[] getUiLanguages()
	{
		Vector languages = new Vector();
		languages.addElement(new ChoiceItem(ENGLISH, getLabel(ENGLISH, "language", ENGLISH, "English")));
		
		addCompiledLanguageResources(languages);

		String[] languageFiles = directory.list(new LanguageFilenameFilter());

		for(int i=0;i<languageFiles.length;++i)
		{
			languages.addElement(getLanguageChoiceItem(languageFiles[i]));
		}

		return (ChoiceItem[])(languages.toArray((Object[])(new ChoiceItem[0])));
	}
	
	Vector addCompiledLanguageResources(Vector languages)
	{
		String filename = null;
		for(int i = 0; i < AVAILABLE_MTF_LANGUAGE_RESOURCES.length; ++i)
		{
			filename = MARTUS_LANGUAGE_FILE_PREFIX + AVAILABLE_MTF_LANGUAGE_RESOURCES[i] + MARTUS_LANGUAGE_FILE_SUFFIX;
			if(getClass().getResource(filename) != null)
			{
				languages.addElement(getLanguageChoiceItem(filename));
			}
		}
		return languages;
	}

	public String getLocalizedFolderName(String folderName)
	{
		return getLabel(getCurrentLanguageCode(), "folder", folderName, "");
	}

	public String getFieldLabel(String fieldName)
	{
		return getLabel(getCurrentLanguageCode(), "field", fieldName, "");
	}

	public String getLanguageName(String code)
	{
		return getLabel(getCurrentLanguageCode(), "language", code, "Unknown");
	}

	public String getWindowTitle(String code)
	{
		return getLabel(getCurrentLanguageCode(), "wintitle", code, "???");
	}

	public String getButtonLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "button", code, "???");
	}

	public String getMenuLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "menu", code, "???");
	}

	public String getStatusLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "status", code, "???");
	}

	public String getKeyword(String code)
	{
		return getLabel(getCurrentLanguageCode(), "keyword", code, "???");
	}

	public String getMonthLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "month", code, "???");
	}

	public String[] getMonthLabels()
	{
		final String[] tags = {"jan","feb","mar","apr","may","jun",
							"jul","aug","sep","oct","nov","dec"};

		String[] labels = new String[tags.length];
		for(int i = 0; i < labels.length; ++i)
		{
			labels[i] = getMonthLabel(tags[i]);
		}

		return labels;
	}

	public ChoiceItem[] getLanguageNameChoices()
	{
		return getLanguageNameChoices(ALL_LANGUAGE_CODES);
	}

	public ChoiceItem[] getLanguageNameChoices(String[] languageCodes)
	{
		if(languageCodes == null)
			return null;
		ChoiceItem[] tempChoicesArray = new ChoiceItem[languageCodes.length];
		for(int i = 0; i < languageCodes.length; i++)
		{
			tempChoicesArray[i] =
				new ChoiceItem(languageCodes[i], getLanguageName(languageCodes[i]));
		}
		Arrays.sort(tempChoicesArray);
		return tempChoicesArray;
	}

	public Vector getAllTranslationStrings(String languageCode)
	{
		createStringMap(languageCode);
	
		Vector strings = new Vector();
		Map englishMap = getStringMap(ENGLISH);
		Set englishKeys = englishMap.keySet();
		SortedSet sorted = new TreeSet(englishKeys);
		Iterator it = sorted.iterator();
		while(it.hasNext())
		{
			String key = (String)it.next();
			String value = getLabel(languageCode, key, "???");
			strings.add(key + "=" + value);
		}
		return strings;
	}

	public void loadEnglishTranslations()
	{
		createStringMap(ENGLISH);
		for(int i=0; i < EnglishStrings.strings.length; ++i)
		{
			addTranslation(ENGLISH, EnglishStrings.strings[i]);
		}
	}

	public static class LanguageFilenameFilter implements FilenameFilter
	{
		public boolean accept(File dir, String name)
		{
			return UiLocalization.isLanguageFile(name);
		}
	}



}
