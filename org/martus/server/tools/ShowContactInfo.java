package org.martus.server.tools;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ShowContactInfo
{
	public static void main(String[] args)
	{
		File contactInfoFile = null;
		
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--field-names"))
			{
				System.out.println("author\torganization\temail\twebpage\tphone\taddress");
				System.out.flush();
				System.exit(0);
			}
			
			if(args[i].startsWith("--file"))
			{
				contactInfoFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
		}
		
		if(contactInfoFile == null)
		{
			System.err.println("\nUsage:\n ShowContactInfo --file=<pathToContactFile.dat>");
			System.exit(2);
		}
		
		if(!contactInfoFile.isFile() || !contactInfoFile.exists())
		{
			System.err.println("Error: " + contactInfoFile.getAbsolutePath() + " is not a file" );
			System.err.flush();
			System.exit(3);
		}
		
		StringBuffer buffer = new StringBuffer();
		FileInputStream inputStream = null;
		DataInputStream in = null;
		try
		{
			inputStream = new FileInputStream(contactInfoFile);

			in = new DataInputStream(inputStream);
			
			in.readUTF(); //ignored
			int dataSize = in.readInt();
			int count = 0;
			
			while(count < dataSize)
			{
				buffer.append( in.readUTF().replaceAll("\n", "|") + "\t");
				count++;
			}
		}
		catch(EOFException ignored)
		{
			;
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e );
			System.err.flush();
			System.exit(3);
		}
		finally
		{
			try
			{
				if(in != null )
					in.close();
					
				if(inputStream != null )
					inputStream.close();
			}
			catch(IOException ignored)
			{
				;
			}
		}
		
		System.out.println(buffer);
	}
}
