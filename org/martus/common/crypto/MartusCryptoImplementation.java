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

package org.martus.common.crypto;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import org.martus.util.*;

public abstract class MartusCryptoImplementation extends MartusCrypto
{
	public synchronized String createSignatureOfVectorOfStrings(Vector dataToSign) throws
			MartusCrypto.MartusSignatureException
	{
		try
		{
			signatureInitializeSign();
			for(int element = 0; element < dataToSign.size(); ++element)
			{
				String thisElement = dataToSign.get(element).toString();
				byte[] bytesToSign = thisElement.getBytes("UTF-8");
				signatureDigestBytes(bytesToSign);
				signatureDigestByte((byte)0);
			}
			return Base64.encode(signatureGet());
		}
		catch(Exception e)
		{
			// TODO: Needs tests!
			e.printStackTrace();
			System.out.println("ServerProxy.sign: " + e);
			throw new MartusCrypto.MartusSignatureException();
		}
	}

	public synchronized boolean verifySignatureOfVectorOfStrings(Vector dataToTestWithSignature, String signedBy)
	{
		Vector dataToTest = (Vector)dataToTestWithSignature.clone();
		String sig = (String)dataToTest.remove(dataToTest.size() - 1);
		return verifySignatureOfVectorOfStrings(dataToTest, signedBy, sig);
	}
	
	public synchronized boolean verifySignatureOfVectorOfStrings(Vector dataToTest, String signedBy, String sig)
	{
		try
		{
			signatureInitializeVerify(signedBy);
			for(int element = 0; element < dataToTest.size(); ++element)
			{
				String thisElement = dataToTest.get(element).toString();
				byte[] bytesToSign = thisElement.getBytes("UTF-8");
				//TODO: might want to optimize this for speed
				for(int b = 0; b < bytesToSign.length; ++b)
					signatureDigestByte(bytesToSign[b]);
				signatureDigestByte((byte)0);
			}
			byte[] sigBytes = Base64.decode(sig);
			return signatureIsValid(sigBytes);
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public String getSignatureOfPublicKey()
		throws Base64.InvalidBase64Exception, MartusCrypto.MartusSignatureException
	{
		String publicKeyString = getPublicKeyString();
		byte[] publicKeyBytes = Base64.decode(publicKeyString);
		ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
		byte[] sigBytes = createSignatureOfStream(in);
		String sigString = Base64.encode(sigBytes);
		return sigString;
	}


}
