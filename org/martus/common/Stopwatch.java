package org.martus.common;

public class Stopwatch
{
	public Stopwatch()
	{
		start();
	}
	
	public void start()
	{
		millis = System.currentTimeMillis();
	}
	
	public long stop()
	{
		return System.currentTimeMillis() - millis;
	}
	
	private long millis;
}
