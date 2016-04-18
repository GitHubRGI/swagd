package com.rgi.suite.cli.tilestoreadapter;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.store.tiles.tms.TmsReader;
import com.rgi.store.tiles.tms.TmsWriter;
import com.rgi.suite.cli.HeadlessOptions;
import com.rgi.suite.cli.HeadlessUtils;

/**
 * Created by matthew.moran on 7/13/15.
 */
public class TMSTileStoreAdapter implements HeadlessTileStoreAdapter
{
	@Override
	public TileStoreReader getReader(final HeadlessOptions opts)
	{
		final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", opts.getInputSrs());
		return new TmsReader(crs, opts.getInputFile().toPath());
	}

	@Override
	public TileStoreWriter getWriter(final HeadlessOptions opts, final TileStoreReader reader)
	{
		final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", opts.getOutputSrs());
		return new TmsWriter(crs,
							 opts.getOutputFile().toPath(),
							 opts.getImageFormat(),
							 HeadlessUtils.getImageWriteParameter(opts.getCompressionQuality(),
																  opts.getCompressionType(),
																  opts.getImageFormat()));

	}
}
