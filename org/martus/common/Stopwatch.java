package org.martus.common;

class Stopwatch
{
		Stopwatch()
		{
			start();
		}
		
		void start()
		{
			millis = System.currentTimeMillis();
		}
		
		long stop()
		{
			return System.currentTimeMillis() - millis;
		}
		
		long millis;
}
