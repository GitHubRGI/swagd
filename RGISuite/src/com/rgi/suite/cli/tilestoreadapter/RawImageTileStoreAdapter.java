package com.rgi.suite.cli.tilestoreadapter;

import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.suite.cli.HeadlessOptions;

import java.awt.*;

/**
 * Created by matthew.moran on 7/13/15.
 */
public class RawImageTileStoreAdapter implements HeadlessTileStoreAdapter
{
	@Override
	public TileStoreReader getReader(final HeadlessOptions opts) throws TileStoreException
	{
		final CoordinateReferenceSystem crsout = new CoordinateReferenceSystem("EPSG",
																			   opts.getOutputSrs());
		final Dimensions<Integer> tileDimensions = new Dimensions<>(opts.getTileWidth(),
																	opts.getTileHeight());
		final Color noDataColor = new Color(0, 0, 0, 0);
		return new RawImageTileReader(opts.getInputFile(),
									  tileDimensions,
									  noDataColor,
									  crsout);
	}

	@SuppressWarnings("ReturnOfNull")
	@Override
	public TileStoreWriter getWriter(final HeadlessOptions opts, final TileStoreReader reader)
	{
		return null;
	}
}
