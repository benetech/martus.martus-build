/* $Id: ChoiceItem.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;



public class ChoiceItem
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

	private String code;
	private String display;
}

