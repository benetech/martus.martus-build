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

package org.martus.client.test;

import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.martus.client.core.ChoiceItem;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiLocalization;
import org.martus.util.StringInputStream;
import org.martus.util.UnicodeStringWriter;

public class TestMartusLocalization extends TestCase
{
    public TestMartusLocalization(String name)
	{
		super(name);
	}

	public void setUp()
	{
		if(bd == null)
			bd = new UiLocalization(MartusApp.getTranslationsDirectory());
 	}

	public void testLanguagess()
	{
		ChoiceItem[] languages = bd.getUiLanguages();
		assertTrue("Should have multiple languages", languages.length > 1);

		boolean foundEnglish = false;
		for(int i = 0; i < languages.length; ++i)
		{
			String code = languages[i].getCode();
			if(code.equals("en"))
			{
				assertEquals("English", true, bd.isLanguageLoaded("en"));
				foundEnglish = true;
			}
			else
			{
				assertEquals(code, false, bd.isLanguageLoaded(code));
			}
		}

		assertTrue("must have english", foundEnglish);
	}

	public void testLanguageCodeFromFilename()
	{
		assertEquals("", bd.getLanguageCodeFromFilename("Martus.mtf"));
		assertEquals("", bd.getLanguageCodeFromFilename("Martus-es.xyz"));
		assertEquals("es", bd.getLanguageCodeFromFilename("Martus-es.mtf"));
	}

	public void testTranslations()
	{
		bd.loadTranslationFile("es");

		assertEquals("Print", bd.getLabel("en", "button", "print", null));
		assertNull("No translation found", bd.getLabel("en", "whatever", "not in the map", null));
		assertEquals("xyz", bd.getLabel("en", "whatever", "not in the map", "xyz"));

		assertEquals("Imprimir", bd.getLabel("es", "button", "print", null));

		assertEquals("?", bd.getLabel("en", "category", "sillytag", "?"));
		assertEquals("<?>", bd.getLabel("es", "category", "sillytag", "?"));
		bd.addTranslation("en", "category:sillytag=something");
		assertEquals("<something>", bd.getLabel("es", "category", "sillytag", "?"));
		bd.addTranslation("es", "category:sillytag=es/something");
		assertEquals("es/something", bd.getLabel("es", "category", "sillytag", "?"));

		assertEquals("<Print>", bd.getLabel("xx", "button", "print", null));
	}
	
	public void testLoadTranslations() throws Exception
	{
		String sampleFileContents = "# This is a comment with =\na:b=c\nd:e=f";
		StringInputStream in = new StringInputStream(sampleFileContents);
		bd.loadTranslations("qq", in);
		assertEquals("c", bd.getLabel("qq", "a", "b", "default"));
		Map qq = bd.getStringMap("qq");
		assertEquals("not 2?", 2, qq.size());
	}
	
	public void testExportTranslations() throws Exception
	{
		UnicodeStringWriter writer = UnicodeStringWriter.create();
		bd.exportTranslations("en", writer);
		String result = writer.toString();
		assertEquals("no leading comment?", 0, result.indexOf("#"));
	}

	public void testAddTranslation()
	{
		// must call getLabel first, to initialize the hash (this should go away soon!)
		assertEquals("<d>", bd.getLabel("xx", "b", "c", "d"));
		bd.addTranslation("en", "b:c=bc");
		assertEquals("<bc>", bd.getLabel("xx", "b", "c", "d"));
		bd.addTranslation("a", "invalid=because-bad-language");
		bd.addTranslation("en", null);
		bd.addTranslation("en", "invalid-because-no-equals");
		bd.addTranslation("en", "b:c=new\\nline");
		assertEquals("new\nline", bd.getLabel("en", "b", "c", "default"));
	}

/*TODO: Evaluate whether any of these tests are still useful
 * because they are not already covered elsewhere
 *
	public void testLoadTranslations()
	{
		try
		{
			assertEquals("xx shouldn't exist yet", false, bd.isLanguageLoaded("xx"));
			bd..loadTranslationFile("xx", "@#<>%#$%#");
			assertEquals("xx should exist now", true, bd.isLanguageLoaded("xx"));

			File file = createTempFileFromName("$$$MartusTestLoadTranslations");
			UnicodeWriter writer = new UnicodeWriter(file);
			writer.write("f:g=fg\n");
			writer.write("j:k=jk\n");
			writer.close();
			assertEquals("<h>", bd.getLabel("xx", "f", "g", "h"));
			assertEquals("<l>", bd.getLabel("xx", "j", "k", "l"));
			bd.loadTranslationFile("xx", file.getCanonicalPath());
			assertEquals("fg", bd.getLabel("xx", "f", "g", "h"));
			assertEquals("jk", bd.getLabel("xx", "j", "k", "l"));
		}
		catch (IOException e)
		{
			assertTrue(e.toString(), false);
		}

		file.delete();
	}
*/

	public void testGetAllTranslationStrings()
	{
		final String sillyEnglish = "a:b=c";
		final String sillyEsperanto = sillyEnglish + "x";

		Vector strings;
		strings = bd.getAllTranslationStrings("eo");
		assertNotNull("Null vector", strings);

		int count = strings.size();
		assertEquals("Should not contain english silly key yet", false, strings.contains(sillyEnglish));
		assertEquals("Should not contain esperanto silly key yet", false, strings.contains(sillyEsperanto));

		bd.addTranslation("eo", sillyEsperanto);
		strings = bd.getAllTranslationStrings("eo");
		assertEquals("Should not have added a string", count, strings.size());
		assertEquals("Still should not contain english silly key", false, strings.contains(sillyEnglish));
		assertEquals("Still should not contain esperanto silly key", false, strings.contains(sillyEsperanto));

		bd.addTranslation("en", sillyEnglish);
		strings = bd.getAllTranslationStrings("eo");
		assertEquals("Should have added one string", count+1, strings.size());
		assertEquals("Should now contain esperanto silly key", true, strings.contains(sillyEsperanto));
	}

	static UiLocalization bd;
}
