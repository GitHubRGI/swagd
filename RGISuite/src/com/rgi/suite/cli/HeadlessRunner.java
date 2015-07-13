package com.rgi.suite.cli;

import com.rgi.common.TaskMonitor;
import com.rgi.packager.Packager;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author matthew.moran
 */
public class HeadlessRunner implements Runnable
{
	private final HeadlessOptions opts;
	private final Logger          logger;

	public HeadlessRunner(final HeadlessOptions options, final Logger logger)
	{
		this.opts = options;
		this.logger = logger;
	}

	/**
	 * Runs the logic for this object. (tiles/packages). ??invalidates itself after run so
	 * duplicate attempts are not supported.
	 */
	@SuppressWarnings("OverlyBroadCatchBlock") //required for autoclosable resources
	@Override
	public void run()
	{
		final TaskMonitor taskMonitor = new HeadlessTaskMonitor( this.logger );

		try( final TileStoreReader tileStoreReader = this.opts.getInputAdapter().getReader( this.opts );
			 final TileStoreWriter tileStoreWriter = this.opts.getOutputAdapter().getWriter( this.opts,
																							 tileStoreReader ) )
		{
			new Packager( taskMonitor, tileStoreReader, tileStoreWriter ).execute();
		}
		catch( final Exception exception )
		{
			this.logger.log( Level.SEVERE, exception.getMessage() );
		}

	}

}
