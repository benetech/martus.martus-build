package org.martus.server.formirroring;

import org.martus.common.MockMartusSecurity;
import org.martus.common.TestCaseEnhanced;
import org.martus.server.forclients.MockMartusServer;

public class TestServerForMirroring extends TestCaseEnhanced
{
	public TestServerForMirroring(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createServer();
		MockMartusServer coreServer = new MockMartusServer();
		coreServer.setSecurity(security);
		new ServerForMirroring(coreServer);
	}

	protected void tearDown() throws Exception
	{
	}

	public void testBasics() throws Exception
	{
	}
}
