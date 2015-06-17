/**
 *
 */
package com.rgi.suite.cli;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

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
import com.rgi.store.tiles.geopackage.GeoPackageWriter;
import com.rgi.store.tiles.tms.TmsWriter;

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
		final Dimensions<Integer> tileDimensions = new Dimensions<>(
				this.opts.getTileWidth(), this.opts.getTileHeight());
		final Color noDataColor = new Color(0, 0, 0, 0);
		final CoordinateReferenceSystem crs = new CoordinateReferenceSystem(
				"EPSG", this.opts.getOutputSrs());

		try (	TileStoreWriter tileStoreWriter = null;
				final TileStoreReader tileStoreReader = new RawImageTileReader(
				this.opts.getInputFile(), tileDimensions, noDataColor, crs))
		{
			final MimeType imageType = new MimeType("image", this.opts.getImageFormat());
			switch (this.opts.getOutputType())
			{
			case TMS:
				tileStoreWriter = new TmsWriter(crs, this.opts.getOutputFile().toPath(),
						imageType);
				break;
			case GPKG:
				tileStoreWriter = new GeoPackageWriter(this.opts.getOutputFile(),
						tileStoreReader.getCoordinateReferenceSystem(),
						this.opts.getTileSetName(), this.opts.getTileSetName(),
						this.opts.getTileSetDescription(), tileStoreReader.getBounds(),
						getRelativeZoomTimesTwoTileScheme(tileStoreReader),
						imageType, this.getImageWriteParameter());
				break;
			default:
				throw new Exception("output Type must be TMS or GPKG!");
			}
			// kick of packager operation.
			new Packager(taskMonitor, tileStoreReader, tileStoreWriter)
					.execute();
		} catch (final Exception ex)
		{
			System.err.println(ex.getMessage());
		} finally
		{
				if (tileStoreWriter != null)
				{
					try
					{
						tileStoreWriter.close();
					} catch (final Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
	}


	/**
	 * returns an image writer for the supplied image type.
	 *
	 * @return
	 */
	private ImageWriter getImageWriter()
	{
		MimeType mimeType;
		try
		{
			mimeType = new MimeType("image", this.opts.getImageFormat());
			return ImageIO.getImageWritersByMIMEType(mimeType.toString())
					.next();

		} catch (final MimeTypeParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * created an image writer parameter object.
	 *
	 * @return
	 */
	private ImageWriteParam getImageWriteParameter()
	{
		final ImageWriteParam imageWriteParameter = this.opts.getImageWriter()
				.getDefaultWriteParam();

		final Float compressionQualityValue = (float) ((this.opts.getQuality()) / 100.00);

		if (this.opts.getCompressionType() != null
				&& imageWriteParameter.canWriteCompressed())
		{
			imageWriteParameter
					.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriteParameter.setCompressionType(this.opts.compressionType);

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
