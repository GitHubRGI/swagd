/**
 *
 */
package com.rgi.suite.cli;

import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.TaskMonitor;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.packager.Packager;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.store.tiles.geopackage.GeoPackageReader;
import com.rgi.store.tiles.geopackage.GeoPackageWriter;
import com.rgi.store.tiles.tms.TmsReader;
import com.rgi.store.tiles.tms.TmsWriter;

import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

		try (final TileStoreReader tileStoreReader = getTileStoreReader();
		     final TileStoreWriter tileStoreWriter = getTileStoreWriter(tileStoreReader);
		)
		{

			// kick of packager operation.
			new Packager(taskMonitor, tileStoreReader, tileStoreWriter)
					.execute();
		} catch (final Exception ex)
		{
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * returns a tileStore reader based on input from headless options class
	 * @return
	 * @throws TileStoreException
	 */
	private TileStoreReader getTileStoreReader() throws TileStoreException
	{
		final CoordinateReferenceSystem crsout = new CoordinateReferenceSystem(
				"EPSG", this.opts.getOutputSrs());
		switch(opts.getInputType())
		{
			case ERR:
				return null;
			case RAW:
				//pass in the output reference system so it will convert

				final Dimensions<Integer> tileDimensions = new Dimensions<>(
						this.opts.getTileWidth(), this.opts.getTileHeight());
				final Color noDataColor = new Color(0, 0, 0, 0);
				return new RawImageTileReader(this.opts.getInputFile(),
											tileDimensions,
											noDataColor,
											crsout);
			case GPKG:
				return new GeoPackageReader(opts.getInputFile(),opts.getTileSetNameIn());
			case TMS:
				return new TmsReader(crsout, opts.getInputFile().toPath());
			default:
				return null;
		}

	}

	/**
	 * returns a tileStore Writer based on output type from headless options classs
	 * @param reader
	 * @return
	 * @throws TileStoreException
	 * @throws MimeTypeParseException
	 */
	private TileStoreWriter getTileStoreWriter(TileStoreReader reader) throws TileStoreException, MimeTypeParseException
	{
		final CoordinateReferenceSystem crs = new CoordinateReferenceSystem(
				"EPSG", this.opts.getOutputSrs());
		switch(opts.getOutputType())
		{
			case ERR:
				return null;
			case RAW:
				return null; //cannot write to raw image
			case GPKG:
				return new GeoPackageWriter(opts.getOutputFile(),
											crs,
											opts.getTileSetNameOut(),
											opts.getTileSetNameOut(),
											opts.getTileSetDescription(),
											reader.getBounds(),//always whole world (lame)
											getRelativeZoomTimesTwoTileScheme(reader),
											opts.getImageFormat(),
											getImageWriteParameter());

			case TMS:
				return new TmsWriter(crs,
									opts.getOutputFile().toPath(),
									opts.getImageFormat(),
									getImageWriteParameter());
			default:
				return null;
		}
	}

	/**
	 * returns an image writer for the supplied image type.
	 *
	 * @return
	 */
	private ImageWriter getImageWriter()
	{
			return ImageIO.getImageWritersByMIMEType(this.opts.getImageFormat().toString())
					.next();
	}

	/**
	 * created an image writer parameter object.
	 *
	 * @return
	 */
	private ImageWriteParam getImageWriteParameter()
	{
		final ImageWriteParam imageWriteParameter = this.getImageWriter()
				.getDefaultWriteParam();
		final Float compressionQualityValue = (float) ((this.opts.getCompressionQuality()) / 100.00);
		if (this.opts.getCompressionType() != null
				&& imageWriteParameter.canWriteCompressed())
		{
			imageWriteParameter
					.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriteParameter.setCompressionType(this.opts.getCompressionType().toUpperCase());

			if (compressionQualityValue != null)
			{
				imageWriteParameter
						.setCompressionQuality(compressionQualityValue);
			}
			return imageWriteParameter;
		}
		return null;
	}

	/**
	 *
	 * @param tileStoreReader
	 * @return
	 * @throws TileStoreException
	 */
	private static TileScheme getRelativeZoomTimesTwoTileScheme(
			final TileStoreReader tileStoreReader) throws TileStoreException
	{
		final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();
		if (zoomLevels.size() == 0)
		{
			throw new TileStoreException(
					"Input tile store contains no zoom levels");
		}
		final Range<Integer> zoomLevelRange = new Range<>(zoomLevels,
				Integer::compare);
		final List<TileHandle> tiles = tileStoreReader.stream(
				zoomLevelRange.getMinimum()).collect(Collectors.toList());
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

}
