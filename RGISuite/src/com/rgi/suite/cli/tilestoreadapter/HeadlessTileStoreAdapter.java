package com.rgi.suite.cli.tilestoreadapter;

import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.suite.cli.HeadlessOptions;

/**
 * Created by matthew.moran on 7/13/15.
 */
public interface HeadlessTileStoreAdapter
{
	/**
	 * returns a tile store reader
	 * @param opts
	 * @return Tile store reader
	 */
	TileStoreReader getReader(final HeadlessOptions opts) throws TileStoreException;

	/**
	 * returns a tile store writer
	 * @param opts options for the writer
	 * @param reader reader to base the writer off of
	 * @return tile store writer
	 */
	TileStoreWriter getWriter(final HeadlessOptions opts, final TileStoreReader reader) throws TileStoreException;
}
