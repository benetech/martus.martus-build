package org.martus.client;

import junit.framework.TestCase;

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
