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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.martus.client.core.ChoiceItem;
import org.martus.client.core.MartusApp;
import org.martus.common.UnicodeReader;
import org.martus.common.UnicodeWriter;


public class MartusLocalization
{
    public static void main (String args[])
	{
		if(args.length != 2)
		{
			System.out.println("If you specify a language code and output filename, " +
								"this will write out a file");
			System.out.println("that contains all the existing translations " +
								"for language xx, plus placeholder");
			System.out.println("tags for all the untranslated strings.");
			System.out.println("Example of a language codes: es = Spanish");
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
		MartusLocalization bd = new MartusLocalization();
		bd.loadTranslationFile(languageCode);
		Vector keys = bd.getAllTranslationStrings(languageCode);

		final String NEWLINE = System.getProperty("line.separator");
		try
		{
			UnicodeWriter writer = new UnicodeWriter(new File(args[1]));
			for(int i = 0; i < keys.size(); ++i)
				writer.write(keys.get(i) + NEWLINE);

			writer.close();
			System.out.println("Success");
		}
		catch(Exception e)
		{
			System.out.println("FAILED: " + e);
			System.exit(3);
		}

    }

    public MartusLocalization()
    {
		languageTranslationsMap = new TreeMap();
		loadEnglishTranslations();
	}

	public void loadEnglishTranslations()
	{
		createStringMap(ENGLISH);
		for(int i=0; i < EnglishStrings.strings.length; ++i)
		{
			addTranslation(ENGLISH, EnglishStrings.strings[i]);
		}
	}

	public boolean isLanguageLoaded(String languageCode)
	{
		if(getStringMap(languageCode) == null)
			return false;

		return true;
	}

	public Map getStringMap(String languageCode)
	{
		return (Map)languageTranslationsMap.get(languageCode);
	}

	public void addTranslation(String languageCode, String translation)
	{
		if(translation == null)
			return;

		Map stringMap = getStringMap(languageCode);
		if(stringMap == null)
			return;

		int endKey = translation.indexOf('=');
		if(endKey < 0)
			return;

		String key = translation.substring(0,endKey);
		String value = translation.substring(endKey + 1, translation.length());
		value = value.replaceAll("\\\\n", "\n");
		stringMap.put(key, value);
	}

	public Map createStringMap(String languageCode)
	{
		if(!isLanguageLoaded(languageCode))
			languageTranslationsMap.put(languageCode, new TreeMap());

		return getStringMap(languageCode);
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

	public ChoiceItem[] getUiLanguages(String translationsDirectory)
	{
		Vector languages = new Vector();
		languages.addElement(new ChoiceItem(ENGLISH, getLabel(ENGLISH, "language", ENGLISH, "English")));
		String spanishFileName = "Martus-es.mtf";
		if(getClass().getResource(spanishFileName) != null)
		{
			languages.addElement(getLanguageChoiceItem(spanishFileName));
		}
		File dir = new File(translationsDirectory);
		String[] languageFiles = dir.list(new LanguageFilenameFilter());

		for(int i=0;i<languageFiles.length;++i)
		{
			languages.addElement(getLanguageChoiceItem(languageFiles[i]));
		}

		return (ChoiceItem[])(languages.toArray((Object[])(new ChoiceItem[0])));
	}

	public String getLanguageCodeFromFilename(String filename)
	{
		if(!isLanguageFile(filename))
			return "";

		int codeStart = filename.indexOf('-') + 1;
		int codeEnd = filename.indexOf('.');
		return filename.substring(codeStart, codeEnd);
	}

	private static boolean isLanguageFile(String filename)
	{
		return (filename.startsWith("Martus-") && filename.endsWith(".mtf"));
	}

	private static class LanguageFilenameFilter implements FilenameFilter
	{
		public boolean accept(File dir, String name)
		{
			return MartusLocalization.isLanguageFile(name);
		}
	}

	public static String getDefaultUiLanguage()
	{
		return ENGLISH;
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

	public void loadTranslationFile(String languageCode)
	{
		InputStream transStream = null;
		String fileShortName = "Martus-" + languageCode + ".mtf";
		File file = new File(MartusApp.getTranslationsDirectory(), fileShortName);
		try
		{
			if(file.exists())
			{
				transStream = new FileInputStream(file);
			}
			else
			{
				transStream = getClass().getResourceAsStream(fileShortName);
			}
			if(transStream == null)
			{
				return;
			}
			loadTranslations(languageCode, transStream);
		}

		catch (IOException e)
		{
			System.out.println("BulletinDisplay.loadTranslationFile " + e);
		}
	}

	public void loadTranslations(String languageCode, InputStream inputStream)
	{
		createStringMap(languageCode);
		try
		{
			UnicodeReader reader = new UnicodeReader(inputStream);
			String translation;
			while(true)
			{
				translation = reader.readLine();
				if(translation == null)
					break;
				addTranslation(languageCode, translation);
			}
			reader.close();
		}
		catch (IOException e)
		{
			System.out.println("BulletinDisplay.loadTranslations " + e);
		}
	}

	public String getCurrentLanguageCode()
	{
		return currentLanguageCode;
	}

	public void setCurrentLanguageCode(String newLanguageCode)
	{
		currentLanguageCode = newLanguageCode;
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

	public String getMonthLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "month", code, "???");
	}

	public String getMessageLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "message", code, "???");
	}

	public String getStatusLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "status", code, "???");
	}

	public String getKeyword(String code)
	{
		return getLabel(getCurrentLanguageCode(), "keyword", code, "???");
	}


	private Map languageTranslationsMap;
	private String currentLanguageCode;

	private static final String ENGLISH = "en";
	public static final String[] ALL_LANGUAGE_CODES = {
				"?", "en", "ar",
				"az", "bn", "my","zh", "nl", "eo", "fr", "de","gu","ha","he","hi","hu",
				"it", "ja","jv","kn","ko","ml","mr","or","pa","pl","pt","ro","ru","sr",
				"sr", "sd","si","es","ta","te", "th","tr","uk","ur","vi"};
}
