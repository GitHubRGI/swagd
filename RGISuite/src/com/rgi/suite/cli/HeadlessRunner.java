/**
 *
 */
package com.rgi.suite.cli;

import com.rgi.common.TaskMonitor;
import com.rgi.packager.Packager;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;

/**
 * @author matthew.moran
 *
 */
public class HeadlessRunner implements Runnable
{
	private final HeadlessOptions opts;
	public HeadlessRunner(final HeadlessOptions options)
	{
		this.opts = options;
	}

	/**
	 * Runs the logic for this object. (tiles/packages). ??invalidates itself after run so
	 * duplicate attempts are not supported.
	 */
	@Override
	public void run()
	{
		final TaskMonitor taskMonitor = new HeadlessTaskMonitor();

		try (final TileStoreReader tileStoreReader = this.opts.getInputType().getReader(this.opts);
		     final TileStoreWriter tileStoreWriter = this.opts.getOutputType().getWriter(this.opts,tileStoreReader))
		{
			new Packager(taskMonitor, tileStoreReader, tileStoreWriter).execute();
		} catch (final Exception ex)
		{
			System.err.println(ex.getMessage());
		}
	}





}
