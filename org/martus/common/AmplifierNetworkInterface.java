package org.martus.common;


import java.util.Vector;

/**
 * @author skoneru
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface AmplifierNetworkInterface
{
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature);
	public Vector getAccountUniversalIds(String myAccountId, Vector parameters, String signature);
	//public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature);
	public Vector getAmplifierBulletinChunk(String myAccountId, Vector parameters, String signature);
}
