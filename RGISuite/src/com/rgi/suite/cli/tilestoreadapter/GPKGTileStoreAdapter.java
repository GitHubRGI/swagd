package com.rgi.suite.cli.tilestoreadapter;

import com.rgi.common.Range;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.store.tiles.geopackage.GeoPackageReader;
import com.rgi.store.tiles.geopackage.GeoPackageWriter;
import com.rgi.suite.cli.HeadlessOptions;
import com.rgi.suite.cli.HeadlessUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by matthew.moran on 7/13/15.
 */
public class GPKGTileStoreAdapter implements HeadlessTileStoreAdapter
{
	/**
	 * @param tileStoreReader - tile store reader to get the zoom scheme from
	 * @return tilescheme from the reader
	 * @throws TileStoreException
	 */
	private TileScheme getRelativeZoomTimesTwoTileScheme(final TileStoreReader tileStoreReader) throws
																								TileStoreException
	{
		final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();
		if(zoomLevels.isEmpty())
		{
			throw new TileStoreException(
												"Input tile store contains no zoom levels");
		}
		final Range<Integer> zoomLevelRange = new Range<>(zoomLevels, Integer::compare);
		final List<TileHandle> tiles =
				tileStoreReader.stream(zoomLevelRange.getMinimum()).collect(Collectors.toList());
		final Range<Integer> columnRange = new Range<>(tiles,
													   tile -> tile.getColumn(), Integer::compare);
		final Range<Integer> rowRange = new Range<>(tiles,
													tile -> tile.getRow(), Integer::compare);
		final int minZoomLevelMatrixWidth = columnRange.getMaximum()
											- columnRange.getMinimum() + 1;
		final int minZoomLevelMatrixHeight = rowRange.getMaximum()
											 - rowRange.getMinimum() + 1;
		return new ZoomTimesTwo(zoomLevelRange.getMinimum(),
								zoomLevelRange.getMaximum(), minZoomLevelMatrixWidth,
								minZoomLevelMatrixHeight);
	}

	@Override
	public TileStoreReader getReader(final HeadlessOptions opts) throws TileStoreException
	{
		return new GeoPackageReader(opts.getInputFile(), opts.getTileSetNameIn());
	}

	@Override
	public TileStoreWriter getWriter(final HeadlessOptions opts, final TileStoreReader reader) throws
																							   TileStoreException
	{
		final CoordinateReferenceSystem crs = new CoordinateReferenceSystem(
																				   "EPSG",
																				   opts.getOutputSrs());
		return new GeoPackageWriter(opts.getOutputFile(),
									crs,
									opts.getTileSetNameOut(),
									opts.getTileSetNameOut(),
									opts.getTileSetDescription(),
									reader.getBounds(),//always whole world (lame)
									this.getRelativeZoomTimesTwoTileScheme(reader),
									opts.getImageFormat(),
									HeadlessUtils.getImageWriteParameter(opts.getCompressionQuality(),
																		 opts.getCompressionType(),
																		 opts.getImageFormat()));
	}
}
