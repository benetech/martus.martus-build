package org.martus.common;

public class Version 
{
	public static void main(String[] args) 
	{
		MartusUtilities utilities = new MartusUtilities();
		String date = utilities.getVersionDate(utilities.getClass());
		System.out.println(formatDateVersion(date));
	}
	
	static String formatDateVersion(String dateVersion)
	{
		if(dateVersion.length() != 8)
			return dateVersion;
		return dateVersion.substring(0,4) + "-" + dateVersion.substring(4,6) + "-" + dateVersion.substring(6);
	}
}
