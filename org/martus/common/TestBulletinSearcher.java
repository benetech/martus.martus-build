/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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
GPL license for more details on the required d a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.common;


public class TestBulletinSearcher extends TestCaseEnhanced
{

	public TestBulletinSearcher(String name)
	{
		super(name);
	}

	public void testDoesMatch() throws Exception
	{
		MartusCrypto security = new MockMartusSecurity();
		Bulletin b = new Bulletin(security);
		b.set("author", "hello");
		b.set("summary", "summary");
		b.set("title", "Josée");
		b.set(Bulletin.TAGEVENTDATE, "2002-04-04");
		b.set(Bulletin.TAGENTRYDATE, "2002-10-15");

		String beginDate ="1900-01-01";
		String endDate = "2099-12-31";

		BulletinSearcher helloWithAnyDate = new BulletinSearcher(new SearchTreeNode("hello"), beginDate, endDate);
		assertEquals("hello", true, helloWithAnyDate.doesMatch(b));

		// field names should not be searched
		BulletinSearcher fieldTagWithAnyDate = new BulletinSearcher(new SearchTreeNode("author"), beginDate, endDate);
		assertEquals("author", false, fieldTagWithAnyDate.doesMatch(b));
		// id should not be searched
		BulletinSearcher localIdWithAnyDate = new BulletinSearcher(new SearchTreeNode(b.getLocalId()), beginDate, endDate);
		assertEquals("getLocalId()", false, localIdWithAnyDate.doesMatch(b));

		BulletinSearcher noText = new BulletinSearcher(new SearchTreeNode(""), beginDate, endDate);
		assertEquals("Blank must match", true, noText.doesMatch(b));

		BulletinSearcher allCaps = new BulletinSearcher(new SearchTreeNode("HELLO"), beginDate, endDate);
		assertEquals("HELLO", true, allCaps.doesMatch(b));
		BulletinSearcher utf8 = new BulletinSearcher(new SearchTreeNode("josée"), beginDate, endDate);
		assertEquals("josée", true, utf8.doesMatch(b));
		BulletinSearcher utf8MixedCase = new BulletinSearcher(new SearchTreeNode("josÉe"), beginDate, endDate);
		assertEquals("josÉe", true, utf8MixedCase.doesMatch(b));
		BulletinSearcher nonUtf8 = new BulletinSearcher(new SearchTreeNode("josee"), beginDate, endDate);
		assertEquals("josee", false, nonUtf8.doesMatch(b));

		SearchParser parser = SearchParser.createEnglishParser();
		BulletinSearcher andRightFalse = new BulletinSearcher(parser.parse("hello and goodbye"), beginDate, endDate);
		assertEquals("right false and", false, andRightFalse.doesMatch(b));
		BulletinSearcher andLeftFalse = new BulletinSearcher(parser.parse("goodbye and hello"), beginDate, endDate);
		assertEquals("left false and", false, andLeftFalse.doesMatch(b));
		BulletinSearcher andBothTrue = new BulletinSearcher(parser.parse("Hello and Summary"), beginDate, endDate);
		assertEquals("true and", true, andBothTrue.doesMatch(b));

		BulletinSearcher orBothFalse = new BulletinSearcher(parser.parse("swinging and swaying"), beginDate, endDate);
		assertEquals("false or", false, orBothFalse.doesMatch(b));
		BulletinSearcher orRightFalse = new BulletinSearcher(parser.parse("hello or goodbye"), beginDate, endDate);
		assertEquals("left true or", true, orRightFalse.doesMatch(b));
		BulletinSearcher orLeftFalse = new BulletinSearcher(parser.parse("goodbye or hello"), beginDate, endDate);
		assertEquals("right true or", true, orLeftFalse.doesMatch(b));
		BulletinSearcher orBothTrue = new BulletinSearcher(parser.parse("hello or summary"), beginDate, endDate);
		assertEquals("both true or", true, orBothTrue.doesMatch(b));
	}

	public void testDateMatches() throws Exception
	{
		Bulletin b = new Bulletin(new MockMartusSecurity());
		b.set("author", "Dave");
		b.set("summary", "summary");
		b.set("title", "cool day");
		b.set(Bulletin.TAGEVENTDATE, "2002-04-04");
		b.set(Bulletin.TAGENTRYDATE, "2002-10-15");

		String outOfRangeBeginDate ="2003-01-01";
		String outOfRangeEndDate = "2006-12-31";
		String bothInRangeBeginDate ="2002-01-01";
		String bothInRangeEndDate = "2002-12-31";
		String eventInRangeBeginDate ="2002-01-01";
		String eventInRangeEndDate = "2002-04-04";
		String entryInRangeBeginDate ="2002-10-15";
		String entryInRangeEndDate = "2002-10-16";

		BulletinSearcher emptySearch = new BulletinSearcher(new SearchTreeNode(""), outOfRangeBeginDate, outOfRangeEndDate);
		assertEquals("out of range", false, emptySearch.doesMatch(b));

		BulletinSearcher noStringAndBothDatesMatch = new BulletinSearcher(new SearchTreeNode(""), bothInRangeBeginDate, bothInRangeEndDate);
		assertEquals("both event and entry in range", true, noStringAndBothDatesMatch.doesMatch(b));

		BulletinSearcher noStringAndEventMatches = new BulletinSearcher(new SearchTreeNode(""), eventInRangeBeginDate, eventInRangeEndDate);
		assertEquals("event only in range", true, noStringAndEventMatches.doesMatch(b));

		BulletinSearcher noStringAndEntryMatches = new BulletinSearcher(new SearchTreeNode(""), entryInRangeBeginDate, entryInRangeEndDate);
		assertEquals("entry only in range", true, noStringAndEntryMatches.doesMatch(b));

		BulletinSearcher wrongStringAndBothDatesMatch = new BulletinSearcher(new SearchTreeNode("hello"), bothInRangeBeginDate, bothInRangeEndDate);
		assertEquals("both event and entry in range but string doesn't match", false, wrongStringAndBothDatesMatch.doesMatch(b));

		BulletinSearcher stringMatchesAndBothDatesMatch = new BulletinSearcher(new SearchTreeNode("Dave"), bothInRangeBeginDate, bothInRangeEndDate);
		assertEquals("both event and entry in range and string matchs", true, stringMatchesAndBothDatesMatch.doesMatch(b));

	}
}
