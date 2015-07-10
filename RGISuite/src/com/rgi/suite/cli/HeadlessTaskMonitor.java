/**
 *
 */
package com.rgi.suite.cli;

import com.rgi.common.TaskMonitor;

/**
 * TaskMonitor for headless operation, outputs progress bar to the command line for tracking status.
 * @author matthew.moran
 *
 */
public class HeadlessTaskMonitor implements TaskMonitor
{

	private int maximum = 0;
	private int step = 1;
	private int last = 0;

	/**
	 * @see com.rgi.common.TaskMonitor#setMaximum(int)
	 */
	@Override
	public void setMaximum(final int maximum)
	{
		this.maximum = maximum;
		if (maximum >= 25)
		{
			this.step = maximum / 25;
		}
	}

	/**
	 * @see com.rgi.common.TaskMonitor#setProgress(int)
	 */
	@Override
	public void setProgress(final int value)
	{
		final int percent = (int) ((value) / (float) (this.maximum) * 100.00);
		if (value > (this.last + this.step))
		{
			final StringBuilder bar = new StringBuilder("[");
			for (int i = 0; i < 25; i++)
			{
				if (i < (percent / 4))
				{
					bar.append("=");
				} else if (i == (percent / 4))
				{
					bar.append(">");
				} else
				{
					bar.append(" ");
				}
			}
			bar.append(String.format("]  - %d / %d ", value, this.maximum));
			// return carriage and write over progress bar.
			System.out.print("\r" + bar.toString());
			this.last = value;
		}
		if(value == this.maximum)
		{
			System.out.println(String.format("\r[=========================] %d / %d",value,this.maximum));
			System.out.println("Tiling Complete!");
		}

	}

}
