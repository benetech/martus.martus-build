package org.martus.server.tools;

import java.io.File;

import org.martus.common.FileInputStreamWithSeek;
import org.martus.common.InputStreamWithSeek;
import org.martus.common.MartusCrypto;
import org.martus.common.MartusSecurity;
import org.martus.common.Packet;

public class VerifyPacket
{
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.out.println("VerifyPacket <packet filename>");
			System.exit(1);
		}

		String localId = args[0];

		try
		{
			InputStreamWithSeek in = new FileInputStreamWithSeek(new File(localId));
			MartusCrypto security = new MartusSecurity();
			Packet.verifyPacketSignature(in, null, security);
			System.out.println("Signature OK!");
		}
		catch(Exception e)
		{
			System.out.println("Exception: " + e);
			System.out.println("           " + e.getMessage());
		}
	}
}
