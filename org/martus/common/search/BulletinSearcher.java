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

import org.martus.common.bulletin.Bulletin;

public class BulletinSearcher
{
	public BulletinSearcher(SearchTreeNode nodeToMatch, String beginDateToMatch, String endDateToMatch)
	{
		node = nodeToMatch;
		beginDate = beginDateToMatch;
		endDate = endDateToMatch;
	}

	public boolean doesMatch(Bulletin b)
	{
		if(node.getOperation() == SearchTreeNode.VALUE)
		{
			if(b.contains(node.getValue()))
			{
				return b.withinDates(beginDate, endDate);
			}
			return false;
		}

		BulletinSearcher left = new BulletinSearcher(node.getLeft(), beginDate, endDate);
		BulletinSearcher right = new BulletinSearcher(node.getRight(), beginDate, endDate);

		if(node.getOperation() == SearchTreeNode.AND)
			return left.doesMatch(b) && right.doesMatch(b);

		if(node.getOperation() == SearchTreeNode.OR)
			return left.doesMatch(b) || right.doesMatch(b);

		return false;
	}

	SearchTreeNode node;
	String beginDate;
	String endDate;
}
