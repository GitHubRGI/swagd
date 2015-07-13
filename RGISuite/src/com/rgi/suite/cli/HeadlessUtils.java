package com.rgi.suite.cli;

import com.rgi.common.Range;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by matthew.moran on 7/10/15.
 */
public class HeadlessUtils
{
	/**
	 * returns an image writer for the supplied image type.
	 *
	 * @return
	 */
	private static ImageWriter getImageWriter(MimeType imageFormat)
	{
		return ImageIO.getImageWritersByMIMEType( imageFormat.toString() )
					  .next();
	}

	/**
	 * created an image writer parameter object.
	 *
	 * @return
	 */
	protected static ImageWriteParam getImageWriteParameter(int quality, String compressionType, MimeType imageFormat)
	{
		final ImageWriteParam imageWriteParameter = getImageWriter( imageFormat )
															.getDefaultWriteParam();
		final Float compressionQualityValue = (float) ( ( quality ) / 100.00 );
		if( compressionType != null
			&& imageWriteParameter.canWriteCompressed() )
		{
			imageWriteParameter
					.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
			imageWriteParameter.setCompressionType( compressionType.toUpperCase() );

			if( compressionQualityValue != null )
			{
				imageWriteParameter
						.setCompressionQuality( compressionQualityValue );
			}
			return imageWriteParameter;
		}
		return null;
	}

	/**
	 * @param tileStoreReader
	 * @return
	 * @throws TileStoreException
	 */
	protected static TileScheme getRelativeZoomTimesTwoTileScheme(final TileStoreReader tileStoreReader) throws TileStoreException
	{
		final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();
		if( zoomLevels.size() == 0 )
		{
			throw new TileStoreException(
												"Input tile store contains no zoom levels" );
		}
		final Range<Integer>   zoomLevelRange = new Range<>( zoomLevels, Integer::compare );
		final List<TileHandle> tiles          =
				tileStoreReader.stream( zoomLevelRange.getMinimum() ).collect( Collectors.toList() );
		final Range<Integer> columnRange = new Range<>( tiles,
														tile -> tile.getColumn(), Integer::compare );
		final Range<Integer> rowRange = new Range<>( tiles,
													 tile -> tile.getRow(), Integer::compare );
		final int minZoomLevelMatrixWidth = columnRange.getMaximum()
											- columnRange.getMinimum() + 1;
		final int minZoomLevelMatrixHeight = rowRange.getMaximum()
											 - rowRange.getMinimum() + 1;
		return new ZoomTimesTwo( zoomLevelRange.getMinimum(),
								 zoomLevelRange.getMaximum(), minZoomLevelMatrixWidth,
								 minZoomLevelMatrixHeight );
	}
}
