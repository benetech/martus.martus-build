/* $Id:  $ */
package org.martus.common;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;


public class TestXmlWriterFilter extends TestCaseEnhanced
{
	public TestXmlWriterFilter(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		StringWriter stringWriter = new StringWriter();
		XmlWriterFilter filter = new XmlWriterFilter(stringWriter);
		filter.writeStartTag("z");
		filter.writeEndTag("y");
		filter.writeDirect("<&a>");
		filter.writeStartTag(" <&a> ");
		filter.writeEncoded(" <&b ");
		stringWriter.close();
		
		String result = stringWriter.toString();
		assertEquals("<z></y>\n<&a>< <&a> > &lt;&amp;b ", result);
	}
	
	public void testSigningGood() throws Exception
	{
		int SHORTEST_LEGAL_KEY_SIZE = 512;
		MartusSecurity security = new MartusSecurity();
		security.createKeyPair(SHORTEST_LEGAL_KEY_SIZE);

		String expectedText = "<a>\r\ncd\n</a>\n";
		byte[] expectedBytes = expectedText.getBytes();
		ByteArrayInputStream expectedIn = new ByteArrayInputStream(expectedBytes);
		byte[] expectedSig = security.createSignature(expectedIn);
		expectedIn.close();
		
		StringWriter stringWriter = new StringWriter();
		XmlWriterFilter filter = new XmlWriterFilter(stringWriter);
		filter.writeDirect("<!--comment-->\n");
		filter.startSignature(security);
		filter.writeStartTag("a");
		filter.writeEncoded("\r\ncd\n");
		filter.writeEndTag("a");
		byte[] sig = filter.getSignature();
		assertNotNull("null sig?", sig);
		assertEquals("bad sig?", true, Arrays.equals(expectedSig, sig));
		filter.writeDirect("more stuff that isn't signed");
		
		try
		{
			filter.getSignature();
			fail("Should have thrown");
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			// expected exception
		}

	}
	
	public void testSigningNotInitialized()
	{
		try
		{
			XmlWriterFilter filter = new XmlWriterFilter(new StringWriter());
			filter.getSignature();
			fail("Should have thrown");
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			// expected exception
		}

	}
	
}
