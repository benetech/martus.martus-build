package org.martus.client;

public class ChoiceItem implements Comparable
{
	public ChoiceItem(String codeToUse, String displayToUse)
	{
		code = codeToUse;
		display = displayToUse;
	}

	public String toString()
	{
		return display;
	}

	public String getCode()
	{
		return code;
	}

	public int compareTo(Object other)
	{
		return toString().compareTo(other.toString());
	}

	private String code;
	private String display;

}

