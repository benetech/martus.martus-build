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

/* $Id$ */
package org.martus.common.search;


public class SearchParser
{
	public static SearchParser createEnglishParser()
	{
		return new SearchParser("and", "or");
	}

	public SearchParser(String andKeyword, String orKeyword)
	{
		andString = " " + andKeyword + " ";
		orString = " " + orKeyword + " ";
	}

	public SearchTreeNode parse(String expression)
	{
		SearchTreeNode rootNode = new SearchTreeNode(expression);
		recursiveParse(rootNode);
		return rootNode;
	}

	private void recursiveParse(SearchTreeNode node)
	{
		final int orLen = orString.length();

		final int andLen = andString.length();

		String lowerText = node.getValue().toLowerCase();
		int orAt = lowerText.indexOf(orString);
		int andAt = lowerText.indexOf(andString);
		if(orAt > 0)
		{
			String text = node.getValue();
			String left = text.substring(0, orAt);
			String right = text.substring(orAt + orLen, text.length());
			node.convertToOr(left, right);
			recursiveParse(node.getLeft());
			recursiveParse(node.getRight());
		}
		else if(andAt > 0)
		{
			String text = node.getValue();
			String left = text.substring(0, andAt);
			String right = text.substring(andAt + andLen, text.length());
			node.convertToAnd(left, right);
			recursiveParse(node.getLeft());
			recursiveParse(node.getRight());
		}
	}

	String andString;
	String orString;
}
