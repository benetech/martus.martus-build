/* $Id: TestChoiceItem.java,v 1.4 2002/09/25 22:07:14 kevins Exp $ */
package org.martus.client;

import junit.framework.*;
import java.util.*;
import java.text.*;

import org.martus.client.*;

public class TestChoiceItem extends TestCase
{
    public TestChoiceItem(String name)
	{
		super(name);
	}

	public void testBasics()
	{
		ChoiceItem item = new ChoiceItem("a", "b");
		assertEquals("a", item.getCode());
		assertEquals("b", item.toString());
	}
}
