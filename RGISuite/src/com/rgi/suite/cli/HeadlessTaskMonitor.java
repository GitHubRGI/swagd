/**
 *
 */
package com.rgi.suite.cli;

import com.rgi.common.TaskMonitor;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * TaskMonitor for headless operation, outputs progress bar to the command line for tracking status.
 *
 * @author matthew.moran
 */
public class HeadlessTaskMonitor implements TaskMonitor
{

	public static final int PROGRESS_BAR_LENGTH = 25;
	private int maximum;
	private int step = 1;
	private       int    last;
	private final Logger logger;

	HeadlessTaskMonitor(final Logger logger)
	{
		this.logger = logger;
	}

	@Override
	public void setMaximum(final int maximum)
	{
		this.maximum = maximum;
		if( maximum >= HeadlessTaskMonitor.PROGRESS_BAR_LENGTH )
		{
			this.step = maximum / HeadlessTaskMonitor.PROGRESS_BAR_LENGTH;
		}
	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	@Override
	public void setProgress(final int value)
	{
		final int percent = value * 100 / this.maximum;
		if( value > this.last + this.step )
		{
			final StringBuilder builder = new StringBuilder( "[" );
			//25 works well with console progress bars, the value is fairly arbitrary though.
			IntStream.rangeClosed( 1, HeadlessTaskMonitor.PROGRESS_BAR_LENGTH ).forEach( index -> {
				if( index < percent / 4 )
				{
					builder.append( '=' );
				}
				else if( index == percent / 4 )
				{
					builder.append( '>' );
				}
				else
				{
					builder.append( ' ' );
				}
			} );
			builder.append( String.format( "]  - %d / %d ", value, this.maximum ) );
			// return carriage and write over progress bar.
			//noinspection HardcodedLineSeparator,UseOfSystemOutOrSystemErr
			System.out.print( "\r" + builder );
			this.last = value;
		}
		if( value == this.maximum )
		{
			this.logger.log( Level.INFO,
							 String.format( "\rGenerated %d/%d tiles successfully!", value, this.maximum ) );
			//noinspection HardcodedLineSeparator,UseOfSystemOutOrSystemErr
			System.out.println( String.format( "\r[=========================] %d / %d", value, this.maximum ) );

		}

	}

}
