/* $Id: EncryptionChangeListener.java,v 1.3 2002/04/18 20:42:55 charles Exp $ */
package org.martus.client;

import java.util.EventListener;
import javax.swing.event.*;

public interface EncryptionChangeListener extends EventListener
{
	void encryptionChanged(boolean newState);
}
