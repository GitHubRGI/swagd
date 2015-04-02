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
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
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
import com.rgi.g2t.TilingException;

/**
 * @author Luke Lambert
 * @author Steven D. Lander
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
     * @throws DataFormatException  Thrown when GDAL has exhausted all means to get the WKT and failed.
     */
    public static SpatialReference getDatasetSpatialReference(final Dataset dataset) throws DataFormatException
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Dataset may not be null.");
        }

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
     * Get the {@link SpatialReference} from an image file
     *
     * @param file
     *             Image file
     * @return The {@link SpatialReference} of the image file
     * @throws DataFormatException
     *             when {@link GdalUtility#getDatasetSrs(Dataset)} throws
     */
    public static SpatialReference getDatasetSpatialReference(final File file) throws DataFormatException
    {
        if(file == null || !file.canRead())
        {
            throw new IllegalArgumentException("File may not be null, and must be readable");
        }

        osr.UseExceptions();
        // Register gdal for use
        gdal.AllRegister();

        final Dataset dataset = gdal.Open(file.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        try
        {
            return GdalUtility.getDatasetSpatialReference(dataset);
        }
        finally
        {
            dataset.delete();
            //gdal.GDALDestroyDriverManager(); Causes fatal error: exception access violation
        }
    }

    /**
     * Given a {@link CoordinateReferenceSystem}, return a {@link SpatialReference}
     *
     * @param crs An input {@link CoordinateReferenceSystem}
     * @return A {@link SpatialReference} built from the input CRS WKT
     */
    public static SpatialReference getSpatialReferenceFromCrs(final CoordinateReferenceSystem crs)
    {
        final SpatialReference srs = new SpatialReference();
        srs.ImportFromWkt(CrsProfileFactory.create(crs).getWellKnownText());
        return srs;
    }
    
    /**
     * @param crsProfile
     * @return
     */
    public static SpatialReference getSpatialReferenceFromCrsProfile(final CrsProfile crsProfile)
    {
    	return GdalUtility.getSpatialReferenceFromCrs(crsProfile.getCoordinateReferenceSystem());
    }

    /**
     * Determine if an input dataset has a georeference
     *
     * @param dataset An input {@link Dataset}
     * @return A boolean where true means the dataset has a georeference and false otherwise
     */
    public static boolean datasetHasGeoReference(final Dataset dataset)
    {
        final double[] identityTransform = { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
        // Compare dataset geotransform to an empty identity transform and ensure there are no GCPs
        // below is the negation of HasGeoReference
        //return Arrays.equals(dataset.GetGeoTransform(), emptyGeoReference) && dataset.GetGCPCount() == 0;
        return !Arrays.equals(dataset.GetGeoTransform(), identityTransform) || dataset.GetGCPCount() != 0;
    }

    /**
     * Get the bounding box for an input {@link Dataset}
     *
     * @param dataset An input {@link Dataset}
     * @return A {@link BoundingBox} built from the bounds of the input {@link Dataset}
     * @throws DataFormatException Thrown when the input dataset contains rotation or skew.  Fix
     *                                the input raster with the gdalwarp utility manually.
     */
    public static BoundingBox getBoundsForDataset(final Dataset dataset) throws DataFormatException
    {
        final double[] outputGeotransform = dataset.GetGeoTransform();
        // Report error in case rotation/skew is in geotransform (only for raster profile)
        // gdal2tiles.py only checks for (ogt[2], ogt[4]) != (0,0), it does not seem to care if only
        // one has a value
        if (outputGeotransform[2] != 0 || outputGeotransform[4] != 0)
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

    /**
     * Build a {@link CoordinateReferenceSystem} from an input {@link SpatialReference}
     *
     * @param srs An input {@link SpatialReference} from which a {@link CoordinateReferenceSystem} will be built
     * @return A {@link CoordinateReferenceSystem} built from the input {@link SpatialReference} using the
     *            authority name and code.
     */
    public static CoordinateReferenceSystem getCoordinateReferenceSystemFromSpatialReference(final SpatialReference srs)
    {
        // Passing null to GetAuthorityName and Code will query the root node of the WKT, not
        // sure if this is what we want
        final String authority  = srs.GetAuthorityName(null);
        final String identifier = srs.GetAuthorityCode(null);

//        final String attributePath = "PROJCS|GEOGCS|AUTHORITY";   // https://gis.stackexchange.com/questions/20298/
//
//        final String authority  = srs.GetAttrValue(attributePath, 0);
//        final String identifier = srs.GetAttrValue(attributePath, 1);

        if(authority  == null || authority .isEmpty() ||
           identifier == null || identifier.isEmpty())
        {
            return null;
        }

        return new CoordinateReferenceSystem(getName(srs), authority, Integer.valueOf(identifier));
    }

    /**
     * Gets the name of a {@link SpatialReference}
     *
     * @param spatialReference
     *             Spatial reference
     * @return
     *        The name of a {@link SpatialReference}. This is the first
     *        attribute after by a top level entry in the WKT. By the <a
     *        href="http://portal.opengeospatial.org/files/?artifact_id=25355">
     *        WKT specification</a>, the only valid top level entries are:
     *        "PROJCS", "GEOGCS", "GEOCCS" and each of their first attributes
     *        must be the name of the spatial reference system.
     */
    public static String getName(final SpatialReference spatialReference)
    {
        return Arrays.asList("PROJCS", "GEOGCS", "GEOCCS")  // These are all of the top level strings according to http://portal.opengeospatial.org/files/?artifact_id=25355.  They must all be followed by a name attribute.
                     .stream()
                     .map(srsType -> spatialReference.GetAttrValue(srsType, 0))
                     .filter(Objects::nonNull)
                     .findFirst()
                     .orElse(null);
    }

    /**
     * Build a {@link CrsProfile} from an input {@link Dataset}
     *
     * @param dataset An input {@link Dataset}
     * @return A {@link CrsProfile} built from the input {@link Dataset}
     * @throws TileStoreException Thrown when a {@link CrsProfile} could not be built from the input {@link Dataset}
     */
    public static CrsProfile getCrsProfileForDataset(final Dataset dataset) throws TileStoreException
    {
        try
        {
            return CrsProfileFactory.create(GdalUtility.getCoordinateReferenceSystemFromSpatialReference(GdalUtility.getDatasetSpatialReference(dataset)));
        }
        catch (final DataFormatException dfe)
        {
            throw new TileStoreException(dfe);
        }
    }

    /**
     * Calculate all the tile ranges for the data in the input {@link BoundingBox} for all zoom levels.
     *
     * @param bounds A {@link BoundingBox} describing the data area
     * @param crsProfile A {@link CrsProfile} for the input area
     * @param tileScheme A {@link TileScheme} representing the way in which the tiles should be arranged
     * @param tileOrigin A {@link TileOrigin} that represents which corner tiling begins from
     * @return A list of zoom-level-numbered indices containing tile coordinate info for the topLeft and bottomRight
     *            corners of the grid
     */
    public static List<Range<Coordinate<Integer>>> calculateTileRangesForAllZooms(final BoundingBox bounds,
                                                                                  final CrsProfile crsProfile,
                                                                                  final TileScheme tileScheme,
                                                                                  final TileOrigin tileOrigin)
    {
        final List<Range<Coordinate<Integer>>> tileRangesByZoom = new ArrayList<>();

        // Get the crs coordinates of the bounds
        final CrsCoordinate topLeft = new CrsCoordinate(bounds.getTopLeft(), crsProfile.getCoordinateReferenceSystem());
        final CrsCoordinate bottomRight = new CrsCoordinate(bounds.getBottomRight(), crsProfile.getCoordinateReferenceSystem());
        //final CrsCoordinate topLeft = new CrsCoordinate(bounds.getBottomLeft(), crsProfile.getCoordinateReferenceSystem());
        //final CrsCoordinate bottomRight = new CrsCoordinate(bounds.getTopRight(), crsProfile.getCoordinateReferenceSystem());
        IntStream.range(0, 32).forEach(zoom ->
        {
            final TileMatrixDimensions tileMatrixDimensions = tileScheme.dimensions(zoom);
            final Coordinate<Integer> topLeftTile = crsProfile.crsToTileCoordinate(topLeft, crsProfile.getBounds(), tileMatrixDimensions, tileOrigin);
            final Coordinate<Integer> bottomRightTile = crsProfile.crsToTileCoordinate(bottomRight, crsProfile.getBounds(), tileMatrixDimensions, tileOrigin);
            tileRangesByZoom.add(zoom, new Range<>(topLeftTile, bottomRightTile));
        });

        return tileRangesByZoom;
    }

    /**
     * Get the lowest-integer zoom level for the input {@link Dataset}
     *
     * @param dataset An input {@link Dataset}
     * @param tileRanges The calculated list of tile numbers and zooms
     * @param tileOrigin The {@link TileOrigin} of the tile grid
     * @param tileScheme The {@link TileScheme} of the tile grid
     * @param tileSize A {@link Dimensions} Integer object that describes what the tiles should look like
     * @return The zoom level for this dataset that produces only one tile.  Defaults to 0 if
     *            an error occurs
     */
    public static int minimalZoomForDataset(final Dataset dataset,
                                            final List<Range<Coordinate<Integer>>> tileRanges,
                                            final TileOrigin tileOrigin,
                                            final TileScheme tileScheme,
                                            final Dimensions<Integer> tileSize)
    {
        final double pixelSize = dataset.GetGeoTransform()[1];
        final double zoomPixelSize;
        if (tileSize.getWidth() == tileSize.getHeight())
        {
            zoomPixelSize = (pixelSize * Math.max(dataset.GetRasterXSize(), dataset.GetRasterYSize()) / tileSize.getWidth());
        }
        else
        {
            // Pixel resolution of the dimension with the most units per pixel will be used to calculate the zoom
            final int largestResolution = tileSize.getWidth() > tileSize.getHeight() ? tileSize.getWidth() : tileSize.getHeight();
            zoomPixelSize = (pixelSize * Math.max(dataset.GetRasterXSize(), dataset.GetRasterYSize()) / largestResolution);
        }
        try
        {
            final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(dataset);
            final int zoom = GdalUtility.zoomLevelForPixelSize(zoomPixelSize, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
            // TODO: We could probably come up with a better way of doing this
            // The resolution returned ensures that a raster could exist within a single tile, but that raster could still produce
            // 4 tiles at the lowest-integer-zoom if it was on tile boundaries.  Scale up *2* levels to ensure this does not happen.
            return zoom == 0 || zoom == 1 ? 0 : zoom - 2; 
        }
        catch(final TileStoreException e)
        {
            System.out.println("Could not determine minimal zoom, defaulting to 0.");
        }
        // Worst case scenario, return zoom level 0
        return 0;
    }

    /**
     * Get the highest-integer zoom level for the input {@link Dataset}
     *
     * @param dataset An input {@link Dataset}
     * @param tileRanges The calculated list of tile numbers and zooms
     * @param tileOrigin The {@link TileOrigin} of the tile grid
     * @param tileScheme The {@link TileScheme} of the tile grid
     * @param tileSize A {@link Dimensions} Integer object that describes what the tiles should look like
     * @return The zoom level for this dataset that is closest to the actual resolution
     * @throws TileStoreException Thrown when the
     */
    public static int maximalZoomForDataset(final Dataset dataset,
                                            final List<Range<Coordinate<Integer>>> tileRanges,
                                            final TileOrigin tileOrigin,
                                            final TileScheme tileScheme,
                                            final Dimensions<Integer> tileSize) throws TileStoreException
    {
        final double zoomPixelSize = dataset.GetGeoTransform()[1];
        try
        {
            final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(dataset);
            return GdalUtility.zoomLevelForPixelSize(zoomPixelSize, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        }
        catch (final TileStoreException e)
        {
            throw new TileStoreException("Could not determine maximal zoom for input raster pixel size (dataset.GetGeoTransform()[1]).");
        }
    }

    /**
     * Return a {@link Set} of all the zoom levels in the input {@link Dataset}
     *
     * @param dataset An input {@link Dataset}
     * @param tileOrigin The {@link TileOrigin} of the tile grid
     * @param tileSize A {@link Dimensions} Integer object that describes what the tiles should look like
     * @return A set of integers for all the zoom levels in the input dataset
     * @throws TileStoreException Thrown if the input dataset bounds could not be retrieved
     */
    public static Set<Integer> getZoomLevelsForDataset(final Dataset dataset, final TileOrigin tileOrigin, final Dimensions<Integer> tileSize) throws TileStoreException
    {
        // World extent tile scheme
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        try
        {
            final BoundingBox datasetBounds = GdalUtility.getBoundsForDataset(dataset);
            final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(dataset);
            final List<Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRangesForAllZooms(datasetBounds, crsProfile, tileScheme, tileOrigin);
            final int minZoom = GdalUtility.minimalZoomForDataset(dataset, tileRanges, tileOrigin, tileScheme, tileSize);
            final int maxZoom = GdalUtility.maximalZoomForDataset(dataset, tileRanges, tileOrigin, tileScheme, tileSize);
            return IntStream.rangeClosed(minZoom, maxZoom).boxed().collect(Collectors.toSet());
        }
        catch (final DataFormatException dfe)
        {
            throw new TileStoreException(dfe);
        }
    }

    /**
     * Return the appropriate zoom level based on the input pixel resolution
     *
     * @param zoomPixelSize The pixel resolution of the zoom level
     * @param tileRanges The calculated list of tile numbers and zooms
     * @param dataset An input {@link Dataset}
     * @param crsProfile A {@link CrsProfile} for the input area
     * @param tileScheme The {@link TileScheme} of the tile grid
     * @param tileOrigin The {@link TileOrigin} of the tile grid
     * @param tileSize A {@link Dimensions} Integer object that describes what the tiles should look like
     * @return The integer zoom matched to the pixel resolution
     * @throws TileStoreException Thrown when the bounds of the dataset could not be determined
     */
    public static int zoomLevelForPixelSize(final double zoomPixelSize,
                                            final List<Range<Coordinate<Integer>>> tileRanges,
                                            final Dataset dataset,
                                            final CrsProfile crsProfile,
                                            final TileScheme tileScheme,
                                            final TileOrigin tileOrigin,
                                            final Dimensions<Integer> tileSize) throws TileStoreException
    {
        try
        {
            final BoundingBox boundingBox = GdalUtility.getBoundsForDataset(dataset);
            final int[] zooms = IntStream.range(0, 32).toArray();
            for(final int zoom : zooms)
            {
                final TileMatrixDimensions tileMatrixDimensions = tileScheme.dimensions(zoom);
                // Get the tile coordinates of the top-left and bottom-right tiles
                final Coordinate<Integer> topLeftTile = crsProfile.crsToTileCoordinate(new CrsCoordinate(boundingBox.getTopLeft(), crsProfile.getCoordinateReferenceSystem()),
                                                                                          crsProfile.getBounds(), //boundingBox, Use bounds of the world here
                                                                                          tileMatrixDimensions,
                                                                                          tileOrigin);
                final Coordinate<Integer> bottomRightTile = crsProfile.crsToTileCoordinate(new CrsCoordinate(boundingBox.getBottomRight(), crsProfile.getCoordinateReferenceSystem()),
                                                                                              crsProfile.getBounds(), //boundingBox, Use bounds of the world here
                                                                                              tileMatrixDimensions,
                                                                                              tileOrigin);
                // Convert tile coordinates to crs coordinates: this will give us correct units-of-measure-per-pixel
                // This is tile data *plus* padding to the full tile grid
                final Coordinate<Integer> topLeftCoord = tileOrigin.transform(TileOrigin.UpperLeft, topLeftTile.getX(), topLeftTile.getY(), tileMatrixDimensions);
                final Coordinate<Integer> bottomRightCoord = tileOrigin.transform(TileOrigin.LowerRight, bottomRightTile.getX(), bottomRightTile.getY(), tileMatrixDimensions);
                final CrsCoordinate topLeftCrsFull = crsProfile.tileToCrsCoordinate(topLeftCoord.getX(),
                                                                                    topLeftCoord.getY(),
                                                                                    crsProfile.getBounds(),
                                                                                    tileMatrixDimensions,
                                                                                    TileOrigin.UpperLeft);
                final CrsCoordinate bottomRightCrsFull = crsProfile.tileToCrsCoordinate(bottomRightCoord.getX(),
                                                                                        bottomRightCoord.getY(),
                                                                                        crsProfile.getBounds(),
                                                                                        tileMatrixDimensions,
                                                                                        TileOrigin.LowerRight);
                // get how many tiles wide this zoom will be so that number can be multiplied by tile size
                final int zoomTilesWide = tileRanges.get(zoom).getMaximum().getX() - tileRanges.get(zoom).getMinimum().getX() + 1;
                final double zoomResolution;
                if (tileSize.getWidth() == tileSize.getHeight() || tileSize.getWidth() > tileSize.getHeight())
                {
                    //final double width = (new BoundingBox(topLeftCrsFull.getX(), bottomRightCrsFull.getY(), bottomRightCrsFull.getX(), topLeftCrsFull.getY())).getWidth();
                    final double width = bottomRightCrsFull.getX() - topLeftCrsFull.getX();
                    zoomResolution = width / (zoomTilesWide * tileSize.getWidth());
                }
                else
                {
                    final double height = topLeftCrsFull.getY() - bottomRightCrsFull.getY();
                    zoomResolution = height / (zoomTilesWide * tileSize.getHeight());
                }
                // bounding box is made with minx, miny, maxx, maxy
                if (zoomPixelSize > zoomResolution)
                {
                    // It could be that this only figures out the size of the image does not
                    // check if the image lies on tile boundaries.  In that case, a small image could
                    // produce two tiles if it lies on the bounds of the tile grid.
                    // Return two zoom levels up to be sure?
                    return zoom == 0 ? 0 : zoom - 1;
                }
        }
            throw new NumberFormatException("Could not determine zoom level for pixel size: " + String.valueOf(zoomPixelSize));
        }
        catch(final DataFormatException dfe)
        {
            throw new TileStoreException(dfe);
        }
       }

    /**
     * Warp an input {@link Dataset} into a different spatial reference system. Does
     * not correct for NODATA values.
     *
     * @param dataset An input {@link Dataset}
     * @param fromSrs 
     * @param toSrs 
     * @return A {@link Dataset} in the input {@link SpatialReference} requested
     * @throws DataFormatException Thrown when the AutoCreateWarpedVRT method returns null
     */
    public static Dataset warpDatasetToSrs(final Dataset dataset, final SpatialReference fromSrs, final SpatialReference toSrs) throws DataFormatException
    {
        final Dataset output = gdal.AutoCreateWarpedVRT(dataset, fromSrs.ExportToWkt(), toSrs.ExportToWkt(), gdalconstConstants.GRA_Average);
        if (output == null)
        {
            throw new DataFormatException();
        }
        return output;
    }

    /**
     * @param queryDataset
     * @param dimensions 
     * @param tileDataInMemory
     * @return
     * @throws TilingException
     */
    public static Dataset scaleQueryToTileSize(final Dataset queryDataset,
    										   final Dimensions<Integer> dimensions) throws TilingException
    {
    	// TODO: This just handles average resampling, it should be adjusted for other
    	// resampling types
    	final Dataset tileDataInMemory = gdal.GetDriverByName("MEM").Create("", dimensions.getWidth(), dimensions.getHeight(), queryDataset.GetRasterCount());
    	for (final int band : IntStream.range(1, queryDataset.GetRasterCount() + 1).toArray())
    	{
    		try
    		{
    			final int resolution = gdal.RegenerateOverview(queryDataset.GetRasterBand(band),
    														   tileDataInMemory.GetRasterBand(band),
    														   "average");
    			// This could possibly be replaced with (resolution != gdalconstConstants.CE_None)
    			if (resolution != 0)
    			{
    				throw new TilingException("Could not RegenerateOverview on band: "
    										  + String.valueOf(band));
    			}
    		}
    		catch (IllegalArgumentException iae)
    		{
    			iae.printStackTrace();
    		}
    	}
    	return tileDataInMemory;
    }
    
    /**
     * @param dataset
     * @return
     */
    public static Double[] getDatasetNoDataValues(final Dataset dataset)
    {
        // Initialize a new double array of size 3
        final Double[] noDataValues = new Double[3];
        // Get the nodata value for each band
        IntStream.range(1,  dataset.GetRasterCount() + 1).forEach(band ->
        {
            final Double[] noDataValue = new Double[1];
            dataset.GetRasterBand(band).GetNoDataValue(noDataValue);
            if (noDataValue.length != 0 && noDataValue[0] != null)
            {
                // Assumes only one value coming back from the band
                noDataValues[band-1] = noDataValue[0];
            }
        });
        // Is array still using the initialized values?
        if (noDataValues[0] == null && noDataValues[1] == null && noDataValues[2] == null)
        {
            return new Double[0];
        }
        // TODO: Is it possible to see a raster from GDAL with 2 bands? I think
        // only Mono and RGB options are possible
        if (noDataValues[0] != null)
        {
            noDataValues[1] = noDataValues[0];
            noDataValues[2] = noDataValues[0];
        }
        return noDataValues;
    }
    
    /**
     * @param dataset
     * @param alphaBand
     * @return
     */
    public static int getRasterBandCount(final Dataset dataset, final Band alphaBand)
    {
        // TODO: The bitwise calc functionality needs to be verified from the python functionality
        final boolean bitwiseAlpha = (alphaBand.GetMaskFlags() & gdalconstConstants.GMF_ALPHA) != 0;
        if (bitwiseAlpha || dataset.GetRasterCount() == 4 || dataset.GetRasterCount() == 2)
        {
            return dataset.GetRasterCount() - 1;
        }
        return dataset.GetRasterCount();
    }
    
    /**
     * @param dataset
     * @return
     * @throws TileStoreException
     */
    public static int getAlphaBandIndex(final Dataset dataset) throws TileStoreException
    {
        final int[] bands = IntStream.range(1, dataset.GetRasterCount()).toArray();
        for (final int nBand : bands)
        {
            final Band band = dataset.GetRasterBand(nBand);
            if (band.GetColorInterpretation() == gdalconstConstants.GCI_AlphaBand)
            {
                return nBand;
            }
        }
        throw new TileStoreException("No Alpha band detected.  Call getAlphaBandIndex after correctNoDataSimple");
    }
    
    /**
     * @param dataset
     * @return
     */
    public static Dataset correctNoDataSimple(final Dataset dataset)
    {
        boolean datasetHasAlphaBand = false;
        // Iterate through the bands to see if any are tagged alpha
        final int[] bands = IntStream.range(1, dataset.GetRasterCount() + 1).toArray();
        for (final int nBand : bands)
        {
            final Band band = dataset.GetRasterBand(nBand);
            if (band.GetColorInterpretation() == gdalconstConstants.GCI_AlphaBand)
            {
                datasetHasAlphaBand = true;
            }
        }
        // If the dataset actually has an alpha band, return it
        if (datasetHasAlphaBand)
        {
            return dataset;
        }
        // Dataset has no alpha and is NOT a VRT
        if (!dataset.GetDriver().getShortName().equalsIgnoreCase("VRT"))
        {
            // Create a vrt of this dataset
            final Dataset vrtCopy = gdal.AutoCreateWarpedVRT(dataset);
            // Add an alpha band
            vrtCopy.AddBand(gdalconstConstants.GDT_Byte);
            // A new band added is always the last, per docs
            vrtCopy.GetRasterBand(vrtCopy.GetRasterCount() + 1).SetColorInterpretation(gdalconstConstants.GCI_AlphaBand);
            return vrtCopy;
        }
        // Dataset has no alpha and IS a VRT
        dataset.AddBand(gdalconstConstants.GDT_Byte);
        dataset.GetRasterBand(dataset.GetRasterCount() + 1).SetColorInterpretation(gdalconstConstants.GCI_AlphaBand);
        return dataset;
    }
    
    /**
     * @param dataset
     * @return
     */
    public static boolean datasetHasAlpha(final Dataset dataset)
    {
    	boolean datasetHasAlpha = false;
    	final int[] bands = IntStream.range(1, dataset.GetRasterCount() + 1).toArray();
    	for (final int nBand : bands)
    	{
    		final Band band = dataset.GetRasterBand(nBand);
    		if (band.GetColorInterpretation() == gdalconstConstants.GCI_AlphaBand)
    		{
    			datasetHasAlpha = true;
    			break;
    		}
    	}
    	return datasetHasAlpha;
    }
    
    /**
     * @param geoTransform
     * @param boundingBox
     * @param dimensions 
     * @param dataset 
     * @return
     * @throws TilingException 
     */
    public static GdalRasterParameters getGdalRasterParameters(final double[] geoTransform,
    														   final BoundingBox boundingBox,
    														   final Dimensions<Integer> dimensions,
    														   final Dataset dataset) throws TilingException
    {
    	int readX = (int)((boundingBox.getMinX() - geoTransform[0]) / geoTransform[1] + 0.001);
  		int readY = (int)((boundingBox.getMaxY() - geoTransform[3]) / geoTransform[5] + 0.001);
  		int readXSize = (int)((boundingBox.getMaxX() - boundingBox.getMinX()) / geoTransform[1] + 0.5);
  		int readYSize = (int)((boundingBox.getMinY() - boundingBox.getMaxY()) / geoTransform[5] + 0.5);
  		return new GdalRasterParameters(readX, readY, readXSize, readYSize, dimensions, dataset);
    }
    
    /**
     * @param params
     * @param write
     * @param dataset
     * @return
     * @throws TilingException
     */
    public static byte[] readRaster(final GdalRasterParameters params, final Dataset dataset) throws TilingException
    {
    	final int bandCount = dataset.GetRasterCount(); // correctNoDataSimple should have added an alpha band
    	final byte[] imageData = new byte[params.getWriteXSize() * params.getWriteYSize() * bandCount];
    	final int result = dataset.ReadRaster(params.getReadX(), // xOffset
    										  params.getReadY(), // yOffset
    										  params.getReadXSize(), // xSize
    										  params.getReadYSize(), // ySize
    										  params.getWriteXSize(), // buffer_xSize
    										  params.getWriteYSize(), // buffer_ySize
    										  gdalconstConstants.GDT_Byte, // buffer type
    										  imageData, // array into which the data will be written, must
    										  			 // contain at least buffer_xSize * buffer_ySize * nBandCount
    										  null); // Per documentation, will select the first nBandCount bands
    	if (result != gdalconstConstants.CE_None)
    	{
    		throw new TilingException("Failure reported by ReadRaster call in GdalUtility.");
    	}
    	return imageData;
    }
    
    /**
     * @param params
     * @param dataset
     * @return
     * @throws TilingException
     */
    public static ByteBuffer readRasterDirect(final GdalRasterParameters params, final Dataset dataset) throws TilingException
    {
    	final int bandCount = dataset.GetRasterCount(); // correctNoDataSimple should have added an alpha band
    	//final byte[] backingArray = new byte[params.getWriteXSize() * params.getWriteYSize() * bandCount];
    	ByteBuffer imageData = ByteBuffer.allocateDirect(params.getWriteXSize() * params.getWriteYSize() * bandCount);   	
    	final int[] bandList = IntStream.range(1, bandCount + 1).toArray();
    	final int result = dataset.ReadRaster_Direct(params.getReadX(),
    										  		 params.getReadY(),
    										  		 params.getReadXSize(),
    										  		 params.getReadYSize(),
    										  		 params.getWriteXSize(),
    										  		 params.getWriteYSize(),
    										  		 gdalconstConstants.GDT_Byte,
    										  		 imageData,
    										  		 null); // Per documentation, will select the first nBandCount bands
    	if (result != gdalconstConstants.CE_None)
    	{
    		throw new TilingException("Failure reported by ReadRaster call in GdalUtility.");
    	}
    	return imageData;
    }
    
    /**
     * @param params
     * @param imageData
     * @param bandCount
     * @return
     * @throws TilingException
     */
    public static Dataset writeRaster(final GdalRasterParameters params, final byte[] imageData, final int bandCount) throws TilingException
    {
    	final Dataset querySizeDatasetInMemory = gdal.GetDriverByName("MEM").Create("", params.getQueryXSize(), params.getQueryYSize(), bandCount);
    	final int result = querySizeDatasetInMemory.WriteRaster(params.getWriteX(),
    															params.getWriteY(),
    															params.getWriteXSize(),
    															params.getWriteYSize(),
    															params.getWriteXSize(),
    															params.getWriteYSize(),
    															gdalconstConstants.GDT_Byte,
    															imageData,
    															null); // Per documentation, will select the first nBandCount bands
    	if (result != gdalconstConstants.CE_None)
    	{
    		throw new TilingException("Failure reported by WriteRaster call in GdalUtility.");
    	}
    	return querySizeDatasetInMemory;
    }
    
    /**
     * @param params
     * @param imageData
     * @param bandCount
     * @return
     * @throws TilingException
     */
    public static Dataset writeRasterDirect(final GdalRasterParameters params, final ByteBuffer imageData, final int bandCount) throws TilingException
    {
    	Dataset querySizeDatasetInMemory = gdal.GetDriverByName("MEM").Create("", params.getQueryXSize(), params.getQueryYSize(), bandCount);
    	final int result = querySizeDatasetInMemory.WriteRaster_Direct(params.getWriteX(),
    																   params.getWriteY(),
    																   params.getWriteXSize(),
    																   params.getWriteYSize(),
    																   params.getWriteXSize(),
    																   params.getWriteYSize(),
    																   gdalconstConstants.GDT_Byte,
    																   imageData,
    																   null); // Per documentation, will select the first nBandCount bands
    	if (result != gdalconstConstants.CE_None)
    	{
    		throw new TilingException("Failure reported by WriteRasterDirect call in GdalUtility.");
    	}
    	return querySizeDatasetInMemory;
    }
    
    /**
     * @author Steven D. Lander
     *
     */
    public static class GdalRasterParameters
    {
    	private int readX;
    	private int readY;
    	private int readXSize;
    	private int readYSize;
    	private int writeX;
    	private int writeY;
    	private int writeXSize;
    	private int writeYSize;
    	private int queryXSize;
    	private int queryYSize;
    	
    	/**
    	 * @param readX
    	 * @param readY
    	 * @param readXSize
    	 * @param readYSize
    	 * @param dimensions 
    	 * @param dataset 
    	 * @throws TilingException 
    	 */
    	public GdalRasterParameters(final int readX,
    								final int readY,
    								final int readXSize,
    								final int readYSize,
    								final Dimensions<Integer> dimensions,
    								final Dataset dataset) throws TilingException
    	{
    		if (dimensions == null)
    		{
    			throw new TilingException("Dimensions of the tile system cannot be null.");
    		}
    		if (dataset == null)
    		{
    			throw new TilingException("Input dataset must be supplied to GdalRasterParameters.");
    		}
    		this.readX = readX;
    		this.readY = readY;
    		this.readXSize = readXSize;
    		this.readYSize = readYSize;
    		this.writeX = 0;
    		this.writeY = 0;
    		this.queryXSize = 4 * dimensions.getWidth();
    		this.queryYSize = 4 * dimensions.getHeight();
    		this.writeXSize = this.queryXSize;
    		this.writeYSize = this.queryYSize;
    		this.adjust(dataset);
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getReadX()
    	{
    		return this.readX;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getReadY()
    	{
    		return this.readY;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getReadXSize()
    	{
    		return this.readXSize;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getReadYSize()
    	{
    		return this.readYSize;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getWriteX()
    	{
    		return this.writeX;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getWriteY()
    	{
    		return this.writeY;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getWriteXSize()
    	{
    		return this.writeXSize;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getWriteYSize()
    	{
    		return this.writeYSize;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getQueryXSize()
    	{
    		return this.queryXSize;
    	}
    	
    	/**
    	 * @return
    	 */
    	public int getQueryYSize()
    	{
    		return this.queryYSize;
    	}
    	
    	/**
    	 * @param dataset
    	 */
    	private void adjust(final Dataset dataset)
    	{
	        if (this.readX < 0)
	        {
	            final int readXShift = Math.abs(this.readX);
	            this.writeX = (int)(this.writeXSize * ((float)readXShift / this.readXSize));
	            this.writeXSize -= this.writeX;
	            this.readXSize -= (int)(this.readXSize * ((float)readXShift) / this.readXSize);
	            this.readX = 0;
	        }
	        if (this.readX + this.readXSize > dataset.GetRasterXSize())
	        {
	            this.writeXSize = (int)(this.writeXSize * ((float)(dataset.GetRasterXSize() - this.readX) / this.readXSize));
	            this.readXSize = dataset.GetRasterXSize() - this.readX;
	        }
	        if (this.readY < 0)
	        {
	            final int readYShift = Math.abs(this.readY);
	            this.writeY = (int)(this.writeYSize * ((float)readYShift / this.readYSize));
	            this.writeYSize -= this.writeY;
	            this.readYSize -= (int)(this.readYSize * ((float)readYShift / this.readYSize));
	            this.readY = 0;
	        }
	        if (this.readY + this.readYSize > dataset.GetRasterYSize())
	        {
	            this.writeYSize = (int)(this.writeYSize * ((float)(dataset.GetRasterYSize() - this.readY) / this.readYSize));
	            this.readYSize = dataset.GetRasterYSize() - this.readY;
	        }
    	}
    }
}