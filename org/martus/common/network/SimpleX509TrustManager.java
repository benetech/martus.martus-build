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

package org.martus.common.network;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.martus.common.crypto.*;


public class SimpleX509TrustManager implements X509TrustManager
{

	public SimpleX509TrustManager()
	{
		super();
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType)
		throws CertificateException
	{
		// WORKAROUND for a bug in Sun JSSE that shipped with 1.4.1_01 and earlier
		// where it would invoke this method instead of checkServerTrusted!
		checkServerTrusted(chain, authType);
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
		throws CertificateException
	{
		if(!authType.equals("RSA"))
			throw new CertificateException("Only RSA supported");
		if(chain.length != 3)
			throw new CertificateException("Need three certificates");
		X509Certificate cert0 = chain[0];
		X509Certificate cert1 = chain[1];
		X509Certificate cert2 = chain[2];
		try
		{
			cert0.verify(cert0.getPublicKey());
			cert1.verify(cert2.getPublicKey());
			cert2.verify(cert2.getPublicKey());

			PublicKey tryPublicKey = expectedPublicKey;
			if(tryPublicKey == null)
			{
				String certPublicKeyString = MartusSecurity.getKeyString(cert2.getPublicKey());
				String certPublicCode = MartusCrypto.computePublicCode(certPublicKeyString);
				if(expectedPublicCode.equals(certPublicCode))
					tryPublicKey = cert2.getPublicKey();
			}
			cert1.verify(tryPublicKey);
			setExpectedPublicKey(MartusSecurity.getKeyString(tryPublicKey));
		}
		catch (Exception e)
		{
			//Tests will cause this to fire
			//System.out.println("checkServerTrusted: " + e);
			throw new CertificateException(e.toString());
		}
	}

	public X509Certificate[] getAcceptedIssuers()
	{
		return null;
	}

	public void setExpectedPublicCode(String expectedPublicCodeToUse)
	{
		expectedPublicCode = expectedPublicCodeToUse;
		expectedPublicKey = null;
	}

	public void setExpectedPublicKey(String expectedPublicKeyToUse)
	{
		expectedPublicKey = MartusSecurity.extractPublicKey(expectedPublicKeyToUse);
		expectedPublicCode = null;
	}

	private PublicKey expectedPublicKey;
	private String expectedPublicCode;

}
