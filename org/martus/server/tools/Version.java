package org.martus.server.tools;

import org.martus.common.MartusUtilities;
import org.martus.server.core.ServerConstants;

public class Version
{
	public static void main(String[] args)
	{
		String version = ServerConstants.version;
		String build = MartusUtilities.getVersionDate();
		System.out.println(version + " " + build);
	}
}
