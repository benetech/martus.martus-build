/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002,2003, Beneficent
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

package org.martus.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;


public class LoggerToConsole implements LoggerInterface
{
	public LoggerToConsole()
	{
	}
	
	public void log(String message)
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat formatDate = new SimpleDateFormat("EE MM/dd HH:mm:ss z");
		String threadId = getCurrentClientAddress();
		if(threadId == null)
			threadId = "";
		else
			threadId = threadId + ": ";
		String logEntry = formatDate.format(stamp) + " " + threadId + message;
		System.out.println(logEntry);
	}

	protected String getCurrentClientAddress()
	{
		Thread currThread = Thread.currentThread();
		if( XmlRpcThread.class.getName() == currThread.getClass().getName() )
		{
			String ip = ((XmlRpcThread) Thread.currentThread()).getClientAddress();
			return ip;
		}
		return null;
	}
}
