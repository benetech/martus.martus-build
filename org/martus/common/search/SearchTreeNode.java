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

package org.martus.common.search;

public class SearchTreeNode
{
	public final static int VALUE = 0;
	public final static int OR = 1;
	public final static int AND = 2;

	public SearchTreeNode(String value)
	{
		nodeOp = VALUE;
		nodeValue = value.trim();
	}

	public void convertToOr(String left, String right)
	{
		nodeOp = OR;
		nodeValue = null;
		createChildNodes(left, right);
	}

	public void convertToAnd(String left, String right)
	{
		nodeOp = AND;
		nodeValue = null;
		createChildNodes(left, right);
	}

	public String getValue()
	{
		return nodeValue;
	}

	public int getOperation()
	{
		return nodeOp;
	}

	public SearchTreeNode getLeft()
	{
		return nodeLeft;
	}

	public SearchTreeNode getRight()
	{
		return nodeRight;
	}

	private void createChildNodes(String left, String right)
	{
		nodeLeft = new SearchTreeNode(left);
		nodeRight = new SearchTreeNode(right);
	}

	private String nodeValue;
	private int nodeOp;
	private SearchTreeNode nodeLeft;
	private SearchTreeNode nodeRight;
}
