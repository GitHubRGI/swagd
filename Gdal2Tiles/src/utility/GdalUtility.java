/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package utility;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;

import com.rgi.common.BoundingBox;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileStoreException;

/**
 * @author Luke Lambert
 *
 */
public class GdalUtility
{
    /**
     * Converts a GDAL {@link Dataset} into a {@link BufferedImage} <br>
     * <br>
     * Code based on GDAL/SWIG example found <a href=
     * "https://svn.osgeo.org/gdal/trunk/gdal/swig/java/apps/GDALtest.java"
     * >here</a>.
     *
     * @param dataset
     *            A GDAL {@link Dataset}
     * @return Returns a {@link BufferedImage} who's contents matches that of
     *         the input GDAL {@link Dataset}
     */
    public static BufferedImage convert(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Dataset may not be null");
        }

        final int bandCount = dataset.getRasterCount();

        if(bandCount <= 0)
        {
            throw new RuntimeException("Raster contained no bands");
        }

        final int rasterWidth  = dataset.getRasterXSize();
        final int rasterHeight = dataset.getRasterYSize();

        final int pixelCount = rasterWidth * rasterHeight;

        final Band band = dataset.GetRasterBand(1);   // Bands are 1-base indexed

        if(band == null)
        {
            throw new RuntimeException("GDAL returned a null raster band");
        }

        final int bandDataType = band.getDataType();

        if(bandCount == 1)
        {
            if(band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex && band.GetRasterColorTable() != null)
            {
                final ByteBuffer byteBuffer = band.ReadRaster_Direct(0, 0, band.getXSize(), band.getYSize(), band.getDataType());

                final DataBuffer  dataBuffer     = getDataBuffer(band.getDataType(), bandCount, pixelCount, new ByteBuffer[]{ byteBuffer });
                final int         dataBufferType = getDataBufferType(bandDataType);
                final SampleModel sampleModel    = new BandedSampleModel(dataBufferType, rasterWidth, rasterHeight, bandCount);

                final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

                //bufferedImageDataType = BufferedImage.TYPE_BYTE_INDEXED; // This assignment had no effect
                return new BufferedImage(band.GetRasterColorTable()
                                             .getIndexColorModel(gdal.GetDataTypeSize(bandDataType)),
                                         raster,
                                         false,
                                         null);
            }
        }

        final ByteBuffer[] bands = IntStream.range(0, bandCount)
                                            .mapToObj(bandIndex -> { final Band currentBand = dataset.GetRasterBand(bandIndex + 1);
                                                                     return currentBand.ReadRaster_Direct(0, 0, currentBand.getXSize(), currentBand.getYSize(), currentBand.getDataType());
                                                                   })
                                            .toArray(ByteBuffer[]::new);

        final DataBuffer  dataBuffer     = getDataBuffer(bandDataType, bandCount, pixelCount, bands);
        final int         dataBufferType = getDataBufferType(bandDataType);
        final SampleModel sampleModel    = new BandedSampleModel(dataBufferType, rasterWidth, rasterHeight, bandCount);

        final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        if(bandCount > 2)
        {
            return new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                                             bandCount == 4,
                                                             false,
                                                             bandCount == 4 ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
                                                             dataBufferType),
                                     raster,
                                     true,
                                     null);
        }


        final BufferedImage bufferedImage = new BufferedImage(rasterWidth, rasterHeight, getBufferedImageDataType(bandDataType));
        bufferedImage.setData(raster);

        return bufferedImage;
    }

    private static DataBuffer getDataBuffer(final int bandDataType,
    										final int bandCount,
    										final int pixelCount,
    										final ByteBuffer[] bands)
    {
        if(bandDataType == gdalconstConstants.GDT_Byte)
        {
            final byte[][] bytes = new byte[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                bytes[i] = new byte[pixelCount];
                bands[i].get(bytes[i]);
            }

            return new DataBufferByte(bytes, pixelCount);
        }
        else if(bandDataType == gdalconstConstants.GDT_Int16)
        {
            final short[][] shorts = new short[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                shorts[i] = new short[pixelCount];
                bands[i].asShortBuffer().get(shorts[i]);
            }

            return new DataBufferShort(shorts, pixelCount);
        }
        else if(bandDataType == gdalconstConstants.GDT_Int32)
        {
            final int[][] ints = new int[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                ints[i] = new int[pixelCount];
                bands[i].asIntBuffer().get(ints[i]);
            }

            return new DataBufferInt(ints, pixelCount);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported band data type");
        }
    }

    private static int getDataBufferType(final int bandDataType)
    {
        if(bandDataType == gdalconstConstants.GDT_Byte)
        {
            return DataBuffer.TYPE_BYTE;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int16)
        {
            return DataBuffer.TYPE_USHORT;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int32)
        {
            return DataBuffer.TYPE_INT;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported band data type");
        }
    }

    private static int getBufferedImageDataType(final int bandDataType)
    {
        if(bandDataType == gdalconstConstants.GDT_Byte)
        {
            return BufferedImage.TYPE_BYTE_GRAY;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int16)
        {
            return BufferedImage.TYPE_USHORT_GRAY;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int32)
        {
            return BufferedImage.TYPE_CUSTOM;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported band data type");
        }
    }
    
    /**
     * Gets the GDAL {@link SpatialReference} from the input {@link Dataset}
     * 
     * @param dataset A GDAL {@link Dataset}
     * @return Returns a GDAL {@link SpatialReference} that the input dataset has
     * @throws DataFormatException 
     */
    public static SpatialReference getDatasetSrs(final Dataset dataset) throws DataFormatException
    {
		final SpatialReference srs = new SpatialReference();
		// Get the well-known-text of this dataset
		String wkt = dataset.GetProjection();
		if (wkt.isEmpty() && dataset.GetGCPCount() != 0)
		{
			// If the wkt is empty and there are GCPs...
			wkt = dataset.GetGCPProjection();
		}
		if (!wkt.isEmpty())
		{
			// Initialize the srs from the non-empt wkt string
			srs.ImportFromWkt(wkt);
			return srs;
		}
		throw new DataFormatException("Cannot get source file spatial reference system.");
    }
    
    /**
     * @param crs
     * @return
     */
    public static SpatialReference getSpatialReferenceFromCrs(final CoordinateReferenceSystem crs)
    {
    	final SpatialReference srs = new SpatialReference();
    	srs.ImportFromEPSG(crs.getIdentifier());
    	return srs;
    }
    
    /**
     * @param dataset
     * @return
     */
    public static boolean datasetHasGeoReference(final Dataset dataset)
    {
		final double[] emptyGeoReference = { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
		// Compare dataset geotransform to an empty geotransform and ensure there are no GCPs
		//return Arrays.equals(dataset.GetGeoTransform(), emptyGeoReference) && dataset.GetGCPCount() == 0;
		return Arrays.equals(dataset.GetGeoTransform(), emptyGeoReference) || dataset.GetGCPCount() != 0;
    }
    
    /**
     * @param dataset
     * @return
     * @throws DataFormatException
     */
    public static BoundingBox getBoundsForDataset(final Dataset dataset) throws DataFormatException
    {
		final double[] outputGeotransform = dataset.GetGeoTransform();
		// Report error in case rotation/skew is in geotransform (only for raster profile)
		if (outputGeotransform[2] != 0 && outputGeotransform[4] != 0)
		{
			throw new DataFormatException("Georeference of the raster contains rotation or skew. " +
										  "Such raster is not supported.  Please use gdalwarp first.");
		}
		final double minX = outputGeotransform[0];
		final double maxX = outputGeotransform[0] + dataset.GetRasterXSize() * outputGeotransform[1];
		final double maxY = outputGeotransform[3];
		final double minY = outputGeotransform[3] - dataset.GetRasterYSize() * outputGeotransform[1];
		return new BoundingBox(minX, minY, maxX, maxY);
    }
    
    public static CoordinateReferenceSystem getCoordinateReferenceSystemFromSpatialReference(final SpatialReference srs)
    {
        // Passing null to GetAuthorityName and Code will query the root node of the WKT, not
        // sure if this is what we want
        final String authority = srs.GetAuthorityName(null);
        final String identifier = srs.GetAuthorityCode(null);
        return new CoordinateReferenceSystem(authority, Integer.valueOf(identifier));
    }
    
    /**
     * @param dataset
     * @return
     * @throws TileStoreException
     */
    public static CrsProfile getCrsProfileForDataset(final Dataset dataset) throws TileStoreException
    {
    	try
    	{
    		return CrsProfileFactory.create(GdalUtility.getCoordinateReferenceSystemFromSpatialReference(GdalUtility.getDatasetSrs(dataset)));
    	}
    	catch (DataFormatException dfe)
    	{
    		throw new TileStoreException(dfe);
    	}
    }
    
    /**
     * @param bounds
     * @param crsProfile
     * @param tileScheme
     * @param tileOrigin
     * @return
     */
    public static List<Range<Coordinate<Integer>>> calculateTileRangesForAllZooms(final BoundingBox bounds,
    																			  final CrsProfile crsProfile,
    																			  final TileScheme tileScheme,
    																			  final TileOrigin tileOrigin)
    {
    	List<Range<Coordinate<Integer>>> tileRangesByZoom = new ArrayList<Range<Coordinate<Integer>>>();
    	// Get the crs coordinates of the bounds
    	final CrsCoordinate topLeft = new CrsCoordinate(bounds.getTopLeft(), crsProfile.getCoordinateReferenceSystem());
    	final CrsCoordinate bottomRight = new CrsCoordinate(bounds.getBottomRight(), crsProfile.getCoordinateReferenceSystem());
    	IntStream.range(0, 32).forEach(zoom ->
    	{
    		final TileMatrixDimensions tileMatrixDimensions = tileScheme.dimensions(zoom);
			final Coordinate<Integer> topLeftTile = crsProfile.crsToTileCoordinate(topLeft, crsProfile.getBounds(), tileMatrixDimensions, tileOrigin);
			final Coordinate<Integer> bottomRightTile = crsProfile.crsToTileCoordinate(bottomRight, crsProfile.getBounds(), tileMatrixDimensions, tileOrigin);
			tileRangesByZoom.add(zoom, new Range<Coordinate<Integer>>(topLeftTile, bottomRightTile));
    	});
    	return tileRangesByZoom;
    }
    
    /**
     * @param dataset
     * @param tileRanges
     * @param tileOrigin
     * @param tileScheme
     * @param tileSize
     * @return
     * @throws TileStoreException
     */
    public static int minimalZoomForDataset(final Dataset dataset,
    										  final List<Range<Coordinate<Integer>>> tileRanges,
    										  final TileOrigin tileOrigin,
    										  final TileScheme tileScheme,
    										  final int tileSize) throws TileStoreException
    {
    	final double pixelSize = dataset.GetGeoTransform()[1];
    	final double zoomPixelSize = (pixelSize * Math.max(dataset.GetRasterXSize(), dataset.GetRasterYSize()) / tileSize);
    	try
    	{
    		final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(dataset);
    		return GdalUtility.zoomLevelForPixelSize(zoomPixelSize, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
    	}
    	catch(TileStoreException e)
    	{
    		System.out.println("Could not determine minimal zoom, defaulting to 0.");
    	}
    	// Worst case scenario, return zoom level 0
    	return 0;
    }
    
    /**
     * @param dataset
     * @param tileRanges
     * @param tileOrigin
     * @param tileScheme
     * @param tileSize
     * @return
     * @throws TileStoreException
     */
    public static int maximalZoomForDataset(final Dataset dataset,
    										final List<Range<Coordinate<Integer>>> tileRanges,
    										final TileOrigin tileOrigin,
    										final TileScheme tileScheme,
    										final int tileSize) throws TileStoreException
    {
    	final double zoomPixelSize = dataset.GetGeoTransform()[1];
    	try
    	{
    		final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(dataset);
    		return GdalUtility.zoomLevelForPixelSize(zoomPixelSize, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
    	}
    	catch (TileStoreException e)
    	{
    		throw new TileStoreException("Could not determine minimal zoom, defaulting to 0.");
    	}
    }
    
    /**
     * @param dataset
     * @param tileOrigin
     * @param tileSize
     * @return
     * @throws TileStoreException
     */
    public static Set<Integer> getZoomLevelsForDataset(final Dataset dataset, final TileOrigin tileOrigin, final int tileSize) throws TileStoreException
    {
    	// World extent tile scheme
    	final TileScheme tileScheme = new ZoomTimesTwo(0, 32, 1, 1);
    	try
    	{
			final BoundingBox datasetBounds = GdalUtility.getBoundsForDataset(dataset);
			final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(dataset);
			final List<Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRangesForAllZooms(datasetBounds, crsProfile, tileScheme, tileOrigin);
			final int minZoom = GdalUtility.minimalZoomForDataset(dataset, tileRanges, tileOrigin, tileScheme, tileSize);
			final int maxZoom = GdalUtility.maximalZoomForDataset(dataset, tileRanges, tileOrigin, tileScheme, tileSize);
			return IntStream.rangeClosed(minZoom, maxZoom).boxed().collect(Collectors.toSet());
		}
    	catch (DataFormatException dfe)
    	{
    		throw new TileStoreException(dfe);
		}
    }
    
    /**
     * @param zoomPixelSize
     * @param tileRanges
     * @param dataset
     * @param crsProfile
     * @param tileScheme
     * @param tileOrigin
     * @param tileSize
     * @return
     * @throws TileStoreException
     */
    public static int zoomLevelForPixelSize(final double zoomPixelSize,
    										final List<Range<Coordinate<Integer>>> tileRanges,
    										final Dataset dataset,
    										final CrsProfile crsProfile,
    										final TileScheme tileScheme,
    										final TileOrigin tileOrigin,
    										final int tileSize) throws TileStoreException
    {
    	try
    	{
    		final BoundingBox boundingBox = GdalUtility.getBoundsForDataset(dataset);
    		int[] zooms = IntStream.range(0, 31).toArray();
            for(int zoom : zooms)
            {
               	final TileMatrixDimensions tileMatrixDimensions = tileScheme.dimensions(zoom);
               	// Get the tile coordinates of the top-left and bottom-right tiles
               	final Coordinate<Integer> topLeftTile = crsProfile.crsToTileCoordinate(new CrsCoordinate(boundingBox.getTopLeft(), crsProfile.getCoordinateReferenceSystem()),
               																		   boundingBox,
               																		   tileMatrixDimensions,
               																		   tileOrigin);
               	final Coordinate<Integer> bottomRightTile = crsProfile.crsToTileCoordinate(new CrsCoordinate(boundingBox.getBottomRight(), crsProfile.getCoordinateReferenceSystem()),
               																			   boundingBox,
               																			   tileMatrixDimensions,
               																			   tileOrigin);
               	// Convert tile coordinates to crs coordinates: this will give us correct units-of-measure-per-pixel
               	// This is tile data *plus* padding to the full tile grid
               	final CrsCoordinate topLeftCrsFull = crsProfile.tileToCrsCoordinate(topLeftTile.getX(),
                                                                                    topLeftTile.getY() + 1,
                                                                                    crsProfile.getBounds(),
                                                                                    tileMatrixDimensions,
                                                                                    tileOrigin);
                final CrsCoordinate bottomRightCrsFull = crsProfile.tileToCrsCoordinate(bottomRightTile.getX() + 1,
                                                                                   		bottomRightTile.getY(),
                                                                                        crsProfile.getBounds(),
                                                                                        tileMatrixDimensions,
                                                                                        tileOrigin);
                // bounding box is made with minx, miny, maxx, maxy
                final double width = (new BoundingBox(topLeftCrsFull.getX(), bottomRightCrsFull.getY(), bottomRightCrsFull.getX(), topLeftCrsFull.getY())).getWidth();
                // get how many tiles wide this zoom will be so that number can be multiplied by tile size
                int zoomTilesWide = tileRanges.get(zoom).getMaximum().getX() - tileRanges.get(zoom).getMinimum().getX() + 1;
                double zoomResolution = width / (zoomTilesWide * tileSize);
                if (zoomPixelSize > zoomResolution)
                {
                	// TODO: It could be that this only figures out the size of the image does not
                	// check if the image lies on tile boundaries.  In that case, a small image could
                	// produce two tiles if it lies on the bounds of the tile grid.
                	// Possibly return two zoom levels up to be sure?
                	return zoom == 0 ? 0 : zoom - 1;
                }
        }
            throw new NumberFormatException("Could not determine zoom level for pixel size: " + String.valueOf(zoomPixelSize));
    	}
    	catch(DataFormatException dfe)
    	{
    		throw new TileStoreException(dfe);
    	}
   	}
}