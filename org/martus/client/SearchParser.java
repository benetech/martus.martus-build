/* $Id: SearchParser.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;

import org.martus.client.*;

public class SearchParser
{
	public SearchParser(MartusApp appToUse)
	{
		app = appToUse;
	}

	public SearchTreeNode parse(String expression)
	{
		SearchTreeNode rootNode = new SearchTreeNode(expression);
		recursiveParse(rootNode);
		return rootNode;
	}

	private void recursiveParse(SearchTreeNode node)
	{
		final String orString = " " + app.getKeyword("or") + " ";
		final int orLen = orString.length();

		final String andString = " " + app.getKeyword("and") + " ";
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

	MartusApp app;
}
