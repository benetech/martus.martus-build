package org.martus.client;

import java.util.EventListener;

public interface EncryptionChangeListener extends EventListener
{
	void encryptionChanged(boolean newState);
}
