/* $Id: TestSearchTreeNode.java,v 1.4 2002/09/25 18:29:14 charles Exp $ */
package org.martus.client;

import java.util.*;
import java.text.*;

import org.martus.common.*;

public class TestSearchTreeNode extends TestCaseEnhanced
{
    public TestSearchTreeNode(String name)
	{
        super(name);
    }

    public void setUp()
    {
    }

    public void testValueNode()
    {
		SearchTreeNode node = new SearchTreeNode("text");
		assertEquals("text", node.getValue());
		assertEquals(SearchTreeNode.VALUE, node.getOperation());

		node = new SearchTreeNode(" stripped ");
		assertEquals("stripped", node.getValue());
    }

    public void testOrNode()
    {
		SearchTreeNode node = new SearchTreeNode("text");

		assertEquals("text", node.getValue());
		node.convertToOr("left", "right");
		assertEquals(SearchTreeNode.OR, node.getOperation());
		assertNull("Or clears value", node.getValue());

		assertNotNull("Left", node.getLeft());
		assertNotNull("Right", node.getRight());

		assertEquals("Left", "left", node.getLeft().getValue());
		assertEquals("Right", "right", node.getRight().getValue());
	}

    public void testAndNode()
    {
		SearchTreeNode node = new SearchTreeNode("text");

		assertEquals("text", node.getValue());
		node.convertToAnd("left", "right");
		assertEquals(SearchTreeNode.AND, node.getOperation());
		assertNull("Or clears value", node.getValue());

		assertNotNull("Left", node.getLeft());
		assertNotNull("Right", node.getRight());

		assertEquals("Left", "left", node.getLeft().getValue());
		assertEquals("Right", "right", node.getRight().getValue());
	}

}
