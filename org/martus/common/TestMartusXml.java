/* $Id: TestMartusXml.java,v 1.4 2002/09/25 18:29:14 charles Exp $ */
package org.martus.common;

import java.util.*;
import java.text.*;

import org.martus.common.*;

public class TestMartusXml extends TestCaseEnhanced
{
    public TestMartusXml(String name)
	{
        super(name);

    }

	public void testBasics()
	{
		assertEquals("<a>", MartusXml.getTagStart("a"));
		assertEquals("<a b='c'>", MartusXml.getTagStart("a", "b", "c"));
		assertEquals("</a>\n", MartusXml.getTagEnd("a"));
	}
}
