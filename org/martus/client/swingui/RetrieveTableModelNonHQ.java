/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003, Beneficent
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

import org.martus.client.core.Bulletin;
import org.martus.client.core.BulletinSummary;
import org.martus.client.core.MartusApp;



abstract public class RetrieveTableModelNonHQ extends RetrieveTableModel {

	public RetrieveTableModelNonHQ(MartusApp appToUse)
	{
		super(appToUse);
	}

	public String getColumnName(int column)
	{
		switch(column)
		{
		case 0:
			return app.getFieldLabel("retrieveflag");
		case 1:
			return app.getFieldLabel(Bulletin.TAGTITLE);
		case 2:
		default:
			return app.getFieldLabel("BulletinSize");
		}
	}

	public int getColumnCount()
	{
		return 3;
	}

	public Object getValueAt(int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		switch(column)
		{
		case 0:
			return new Boolean(summary.isChecked());
		case 1:
			return summary.getTitle();
		case 2:
				return getSizeInKbytes(summary.getSize());
		default:
			return "";
		}
	}

	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == 0)
			summary.setChecked(((Boolean)value).booleanValue());
	}

	public Class getColumnClass(int column)
	{
		switch(column)
		{
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		default:
			return null;
		}
	}
}
