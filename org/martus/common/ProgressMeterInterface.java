package org.martus.common;
public interface ProgressMeterInterface
{
	public void setStatusMessageAndHideMeter(String message);
	public void setStatusMessage(String message);
	public void updateProgressMeter(String message, int currentValue, int maxValue);
	public void hideProgressMeter();
	public boolean shouldExit();
}
