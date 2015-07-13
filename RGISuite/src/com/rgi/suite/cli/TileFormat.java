package com.rgi.suite.cli;

import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.store.tiles.geopackage.GeoPackageReader;
import com.rgi.store.tiles.geopackage.GeoPackageWriter;
import com.rgi.store.tiles.tms.TmsReader;
import com.rgi.store.tiles.tms.TmsWriter;

import java.awt.*;

/**
 * Enumeration for headless tile input/output formats
 * @author matthew.moran
 *
 */
public enum TileFormat
{
	RAW{
		@Override
		public TileStoreReader getReader(final HeadlessOptions opts) throws Exception
		{
			final CoordinateReferenceSystem crsout = new CoordinateReferenceSystem(
					"EPSG", opts.getOutputSrs());
			final Dimensions<Integer> tileDimensions = new Dimensions<>(
					opts.getTileWidth(), opts.getTileHeight());
			final Color noDataColor = new Color(0, 0, 0, 0);
			return new RawImageTileReader(opts.getInputFile(),
					tileDimensions,
					noDataColor,
					crsout);
		}
		@Override
		public TileStoreWriter getWriter( final HeadlessOptions opts,TileStoreReader reader) throws Exception
		{
			return null;
		}
	}, // raw image
	TMS{

		@Override
		public TileStoreReader getReader(final HeadlessOptions opts) throws Exception
		{
			final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", opts.getInputSrs());
			return new TmsReader(crs, opts.getInputFile().toPath());
		}
		@Override
		public TileStoreWriter getWriter( final HeadlessOptions opts,TileStoreReader reader) throws Exception
		{
			final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", opts.getOutputSrs());
			return new TmsWriter(crs,
					opts.getOutputFile().toPath(),
					opts.getImageFormat(),
					HeadlessUtils.getImageWriteParameter(opts.getCompressionQuality(),
							opts.getCompressionType(), opts.getImageFormat()));

		}
	}, // tms store
	GPKG{
		@Override
		public TileStoreReader getReader(final HeadlessOptions opts) throws Exception
		{
			return new GeoPackageReader(opts.getInputFile(),opts.getTileSetNameIn());
		}
		@Override
		public TileStoreWriter getWriter( final HeadlessOptions opts,TileStoreReader reader) throws Exception
		{
			final CoordinateReferenceSystem crs = new CoordinateReferenceSystem(
					"EPSG", opts.getOutputSrs());
			return new GeoPackageWriter(opts.getOutputFile(),
					crs,
					opts.getTileSetNameOut(),
					opts.getTileSetNameOut(),
					opts.getTileSetDescription(),
					reader.getBounds(),//always whole world (lame)
					HeadlessUtils.getRelativeZoomTimesTwoTileScheme(reader),
					opts.getImageFormat(),
					HeadlessUtils.getImageWriteParameter(opts.getCompressionQuality(),
							opts.getCompressionType(), opts.getImageFormat()));
		}
	}, // gpkg
	ERR{
		@Override
		public TileStoreReader getReader(final HeadlessOptions opts) throws Exception
		{
			return null;
		}
		@Override
		public TileStoreWriter getWriter( final HeadlessOptions opts,TileStoreReader reader) throws Exception
		{
			return null;
		}
	}; // error

	/**
	 * returns a tile store reader
	 * @param opts
	 * @return Tile store reader
	 * @throws Exception
	 */
	public abstract TileStoreReader getReader(final HeadlessOptions opts) throws Exception;

	/**
	 * returns a tile store writer
	 * @param opts options for the writer
	 * @param reader reader to base the writer off of
	 * @return tile store writer
	 * @throws Exception
	 */
	public abstract TileStoreWriter getWriter( final HeadlessOptions opts,TileStoreReader reader) throws Exception;
}
