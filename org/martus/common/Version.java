package org.martus.common;

public class Version 
{
	public static void main(String[] args) 
	{
		MartusUtilities utilities = new MartusUtilities();
		System.out.println(utilities.getVersionDate(utilities.getClass()));
	}
}
