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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.martus.common.bulletin.Bulletin;
import org.martus.util.UnicodeReader;

public class Localization
{
	public Localization(File directoryToUse)
	{
		directory = directoryToUse;
		languageTranslationsMap = new TreeMap();
	}
	


	public File directory;
	public Map languageTranslationsMap;
	public String currentLanguageCode;
	public String currentDateFormat;

	public static final String ENGLISH = "en";
	public static final String[] ALL_LANGUAGE_CODES = {
				"?", "en", "ar",
				"az", "bn", "my","zh", "nl", "eo", "fr", "de","gu","ha","he","hi","hu",
				"it", "ja","jv","kn","ko","ml","mr","or","pa","pl","pt","ro","ru","sr",
				"sr", "sd","si","es","ta","te", "th","tr","uk","ur","vi"};
	public String getCurrentDateFormatCode()
	{
		return currentDateFormat;
	}



	public void setCurrentDateFormatCode(String code)
	{
		currentDateFormat = code;
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
			
		if(translation.startsWith("#"))
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



	public String getLanguageCodeFromFilename(String filename)
	{
		if(!isLanguageFile(filename))
			return "";
	
		int codeStart = filename.indexOf('-') + 1;
		int codeEnd = filename.indexOf('.');
		return filename.substring(codeStart, codeEnd);
	}



	protected static boolean isLanguageFile(String filename)
	{
		return (filename.startsWith("Martus-") && filename.endsWith(".mtf"));
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
		loadTranslationFile(newLanguageCode);
		currentLanguageCode = newLanguageCode;
	}



	public String convertStoredDateToDisplay(String storedDate)
	{
		DateFormat dfStored = Bulletin.getStoredDateFormat();
		DateFormat dfDisplay = new SimpleDateFormat(getCurrentDateFormatCode());
		String result = "";
		try
		{
			Date d = dfStored.parse(storedDate);
			result = dfDisplay.format(d);
		}
		catch(ParseException e)
		{
			// unparsable dates simply become blank strings,
			// so we don't want to do anything for this exception
			//System.out.println(e);
		}
	
		return result;
	}
}
