package org.martus.common;

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
		assertEquals("<a b='c' d='e'>", MartusXml.getTagStart("a", "b", "c", "d", "e"));
		assertEquals("</a>\n", MartusXml.getTagEnd("a"));
	}
}
