/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.swingui;

import javax.swing.UIManager;

class Martus
{
    public static void main (String args[])
	{
		if(args.length >0)
		{
			if(args.length == 1 && args[0].compareToIgnoreCase("-testall")==0)
			{
				org.martus.common.TestCommon.runTests();
				org.martus.client.test.TestAll.runTests();
				System.exit(0);
			}
			else
			{
				System.out.println("Incorrect command line parameter");
				System.out.println("The only valid optional commands is:");
				System.out.println("-testall");
				System.exit(1);
			}
		}
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			System.out.println(e);
			//e.printStatckTrace(System.out);
		}

        UiMainWindow window = new UiMainWindow();
        if(!window.run())
        	System.exit(0);
    }
}
