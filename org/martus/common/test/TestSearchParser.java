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

package org.martus.common.test;

import org.martus.common.search.SearchParser;
import org.martus.common.search.SearchTreeNode;

public class TestSearchParser extends TestCaseEnhanced
{
    public TestSearchParser(String name)
	{
        super(name);
    }

	public void testSimpleSearch()
	{
		SearchParser parser = SearchParser.createEnglishParser();
		SearchTreeNode rootNode = parser.parse("blah");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.VALUE, rootNode.getOperation());
	}

	public void testSimpleOr()
	{
		SearchParser parser = SearchParser.createEnglishParser();
		SearchTreeNode rootNode = parser.parse("this or that");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.OR, rootNode.getOperation());

		SearchTreeNode left = rootNode.getLeft();
		assertNotNull("Null left", left);
		assertEquals("this", left.getValue());

		SearchTreeNode right = rootNode.getRight();
		assertNotNull("Null right", right);
		assertEquals("that", right.getValue());
	}

	public void testSimpleAnd()
	{
		SearchParser parser = SearchParser.createEnglishParser();
		SearchTreeNode rootNode = parser.parse(" tweedledee  and  tweedledum ");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.AND, rootNode.getOperation());

		SearchTreeNode left = rootNode.getLeft();
		assertNotNull("Null left", left);
		assertEquals("tweedledee", left.getValue());

		SearchTreeNode right = rootNode.getRight();
		assertNotNull("Null right", right);
		assertEquals("tweedledum", right.getValue());
	}

	public void testComplex()
	{
		SearchParser parser = SearchParser.createEnglishParser();
		// a AND (b AND c)
		SearchTreeNode abc = parser.parse("a and b and c");
		assertNotNull("Null root", abc);
		assertEquals("rootNode", SearchTreeNode.AND, abc.getOperation());
		assertEquals("a", abc.getLeft().getValue());

		SearchTreeNode bc = abc.getRight();
		assertNotNull("root Null left", bc);
		assertEquals("bc", SearchTreeNode.AND, bc.getOperation());
		assertEquals("b", bc.getLeft().getValue());
		assertEquals("c", bc.getRight().getValue());
	}

	public void testReallyComplex()
	{
		SearchParser parser = SearchParser.createEnglishParser();
		// (a and b) OR ( ( (c AND (d and e) OR f)
		SearchTreeNode rootNode = parser.parse("a and b or c and d and e or f");
		assertNotNull("Null root", rootNode);
		assertEquals("rootNode", SearchTreeNode.OR, rootNode.getOperation());

		SearchTreeNode ab = rootNode.getLeft();
		assertNotNull("ab Null", ab);
		assertEquals("ab", SearchTreeNode.AND, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		SearchTreeNode cdef = rootNode.getRight();
		assertNotNull("cdef Null", cdef);
		assertEquals("cdef", SearchTreeNode.OR, cdef.getOperation());
		assertEquals("f", cdef.getRight().getValue());

		SearchTreeNode cde = cdef.getLeft();
		assertNotNull("cde Null", cde);
		assertEquals("cde", SearchTreeNode.AND, cde.getOperation());
		assertEquals("c", cde.getLeft().getValue());

		SearchTreeNode de = cde.getRight();
		assertNotNull("de Null", de);
		assertEquals("de", SearchTreeNode.AND, de.getOperation());
		assertEquals("d", de.getLeft().getValue());
		assertEquals("e", de.getRight().getValue());
	}

	public void testSpanish()
	{
		// a OR (b AND c)
		SearchParser parser = new SearchParser("y", "o");
		SearchTreeNode abc = parser.parse("a o b y c");
		assertNotNull("Null root", abc);
		assertEquals("rootNode", SearchTreeNode.OR, abc.getOperation());
		assertEquals("a", abc.getLeft().getValue());

		SearchTreeNode bc = abc.getRight();
		assertNotNull("root Null left", bc);
		assertEquals("bc", SearchTreeNode.AND, bc.getOperation());
		assertEquals("b", bc.getLeft().getValue());
		assertEquals("c", bc.getRight().getValue());
	}
}
