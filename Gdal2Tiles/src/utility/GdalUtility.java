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
import java.util.Arrays;
import java.util.Map;
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
import com.rgi.g2t.GeoTransformation;
import com.rgi.g2t.TilingException;

/**
 * Common functionality of the GDAL library made into helper functions
 *
 * @author Luke D. Lambert
 * @author Steven D. Lander
 */
public class GdalUtility
{
    static
    {
        // GDAL_DATA needs to be a valid path
        if(System.getenv("GDAL_DATA") == null)
        {
            throw new RuntimeException("Tiling will not work without GDAL_DATA environment variable.");
        }
        // Get the system path
        //String paths = System.getenv("PATH");
        // TODO
        // Parse the path entries
        // Check each path entry for the required dll's/so's
        // Throw an error if any of the required ones are missing

        osr.UseExceptions();
        gdal.AllRegister(); // Register GDAL extensions
    }

    /**
     * Opens an image file, and returns a {@link Dataset}
     *
     * @param rawImage
     *             A raster image {@link File}
     * @return A {@link Dataset} warped to the input coordinate reference system
     */
    public static Dataset open(final File rawImage)
    {
        return GdalUtility.open(rawImage, null);
    }

    /**
     * Opens an image file, and returns a {@link Dataset}
     *
     * @param rawImage
     *             A raster image {@link File}
     * @param coordinateReferenceSystem
     *             The {@link CoordinateReferenceSystem} the tiles should be
     *             output in
     * @return A {@link Dataset} warped to the input coordinate reference system
     */
    public static Dataset open(final File rawImage, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        final Dataset dataset = gdal.Open(rawImage.getAbsolutePath()); // Opening is read-only by default

        if(dataset == null)
        {
            throw new RuntimeException(new GdalError().getMessage());
        }

        if(coordinateReferenceSystem == null)
        {
            return dataset;
        }

        try
        {
            final SpatialReference inputSrs = GdalUtility.getSpatialReference(dataset);

            final Dataset warpedDataset = GdalUtility.warpDatasetToSrs(dataset, inputSrs, GdalUtility.getSpatialReference(coordinateReferenceSystem));

            if(warpedDataset == null)
            {
                throw new RuntimeException(new GdalError().getMessage());
            }

            return warpedDataset;
        }
        finally
        {
            dataset.delete();
        }
    }

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
     * @param dataset
     *             A GDAL {@link Dataset}
     * @return Returns the GDAL {@link SpatialReference} of the dataset
     */
    public static SpatialReference getSpatialReference(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Dataset may not be null.");
        }

        String wkt = dataset.GetProjection(); // Get the well-known-text of this dataset

        if(wkt.isEmpty() && dataset.GetGCPCount() != 0) // If the WKT is empty and there are GCPs...
        {
            wkt = dataset.GetGCPProjection();
        }

        final SpatialReference srs = new SpatialReference();
        srs.ImportFromWkt(wkt); // Returns 0 on success. Otherwise throws a RuntimeException() (or an error code if DontUseExceptions() has been called).
        return srs;
    }

    /**
     * Get the {@link SpatialReference} from an image file
     *
     * @param file
     *             Image file
     * @return The {@link SpatialReference} of the image file
     */
    public static SpatialReference getSpatialReference(final File file)
    {
        if(file == null || !file.canRead())
        {
            throw new IllegalArgumentException("File may not be null, and must be readable");
        }

        final Dataset dataset = GdalUtility.open(file);

        try
        {
            return GdalUtility.getSpatialReference(dataset);
        }
        finally
        {
            dataset.delete();
        }
    }

    /**
     * Given a {@link CoordinateReferenceSystem}, return a {@link SpatialReference}
     *
     * @param crs An input {@link CoordinateReferenceSystem}
     * @return A {@link SpatialReference} built from the input CRS WKT
     */
    public static SpatialReference getSpatialReference(final CoordinateReferenceSystem crs)
    {
        if(crs == null)
        {
            throw new IllegalArgumentException("Coordinate reference system cannot be null.");
        }
        final SpatialReference srs = new SpatialReference();
        srs.ImportFromWkt(CrsProfileFactory.create(crs).getWellKnownText());
        return srs;
    }

    /**
     * Provide a {@link SpatialReference} given an input {@link CrsProfile}
     *
     * @param crsProfile A {@link CrsProfile} from which a {@link SpatialReference} should be built
     * @return A {@link SpatialReference} built from the input CrsProfile
     */
    public static SpatialReference getSpatialReference(final CrsProfile crsProfile)
    {
        if(crsProfile == null)
        {
            throw new IllegalArgumentException("Crs Profile cannot be null.");
        }
        return GdalUtility.getSpatialReference(crsProfile.getCoordinateReferenceSystem());
    }

    /**
     * Determine if an input dataset has a georeference
     *
     * @param dataset An input {@link Dataset}
     * @return A boolean where true means the dataset has a georeference and false otherwise
     */
    public static boolean hasGeoReference(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        final double[] identityTransform = { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };

        // Compare the dataset's transform to the identity transform and ensure there are no GCPs
        return !Arrays.equals(dataset.GetGeoTransform(), identityTransform) || dataset.GetGCPCount() != 0;
    }

    /**
     * Get the bounding box for an input {@link Dataset}
     *
     * @param dataset
     *             An input {@link Dataset}
     * @return A {@link BoundingBox} built from the bounds of the input {@link Dataset}
     * @throws DataFormatException
     *             When the input dataset contains rotation or skew.  Fix the
     *             input raster with the <code>gdalwarp</code> utility
     *             manually.
     */
    public static BoundingBox getBounds(final Dataset dataset) throws DataFormatException
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        final double[] outputGeotransform = dataset.GetGeoTransform();
        if(outputGeotransform[2] != 0 || outputGeotransform[4] != 0)
        {
            throw new DataFormatException("Raster's georeference contains a rotation or skew, which is not supported.  Please use gdalwarp first.");
        }

        return new BoundingBox(outputGeotransform[0],
                               outputGeotransform[3] + dataset.GetRasterYSize() * outputGeotransform[5],
                               outputGeotransform[0] + dataset.GetRasterXSize() * outputGeotransform[1],
                               outputGeotransform[3]);
    }

    /**
     * Build a {@link CoordinateReferenceSystem} from an input {@link SpatialReference}
     *
     * @param srs An input {@link SpatialReference} from which a {@link CoordinateReferenceSystem} will be built
     * @return A {@link CoordinateReferenceSystem} built from the input {@link SpatialReference} using the
     *            authority name and code.
     */
    public static CoordinateReferenceSystem getCoordinateReferenceSystem(final SpatialReference srs)
    {
        if(srs == null)
        {
            throw new IllegalArgumentException("Input spatial reference system cannot be null.");
        }
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
        if(spatialReference == null)
        {
            throw new IllegalArgumentException("Input spatial reference cannot be null.");
        }
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
     * @param dataset
     *             An input {@link Dataset}
     * @return A {@link CrsProfile} built from the input {@link Dataset}
     */
    public static CrsProfile getCrsProfile(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }

        final SpatialReference          srs = GdalUtility.getSpatialReference(dataset);
        final CoordinateReferenceSystem crs = GdalUtility.getCoordinateReferenceSystem(srs);

        return CrsProfileFactory.create(crs);
    }

    /**
     * Calculate all the tile ranges for the data in the input {@link
     * BoundingBox} for the given zoom levels.
     *
     * @param tileScheme
     *             A {@link TileScheme} describing tile matrices for a set of
     *             zoom levels
     * @param datasetBounds
     *             A {@link BoundingBox} describing the data area
     * @param tileMatrixBounds
     *             A {@link BoundingBox} describing the area of the tile
     *             matrix.
     * @param crsProfile
     *             A {@link CrsProfile} for the input area
     * @param tileOrigin
     *             A {@link TileOrigin} that represents which corner tiling
     *             begins from
     *
     * @return A {@link Map} of zoom levels to tile coordinate info for the
     *             top left and bottom right corners of the matrix
     */
    public static Map<Integer, Range<Coordinate<Integer>>> calculateTileRanges(final TileScheme  tileScheme,
                                                                               final BoundingBox datasetBounds,
                                                                               final BoundingBox tileMatrixBounds,
                                                                               final CrsProfile  crsProfile,
                                                                               final TileOrigin  tileOrigin)
    {
        if(datasetBounds == null)
        {
            throw new IllegalArgumentException("Input bounds cannot be null.");
        }

        if(crsProfile == null)
        {
            throw new IllegalArgumentException("Input crs profile cannot be null.");
        }

        if(tileScheme == null)
        {
            throw new IllegalArgumentException("Input tile scheme cannot be null.");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Input tile origin cannot be null.");
        }

        // Get the CRS coordinates of the bounds
        final CrsCoordinate topLeft     = new CrsCoordinate(datasetBounds.getTopLeft(),     crsProfile.getCoordinateReferenceSystem());
        final CrsCoordinate bottomRight = new CrsCoordinate(datasetBounds.getBottomRight(), crsProfile.getCoordinateReferenceSystem());

        return tileScheme.getZoomLevels()
                         .stream()
                         .collect(Collectors.toMap(zoom -> zoom,
                                                   zoom -> { final TileMatrixDimensions tileMatrixDimensions = tileScheme.dimensions(zoom);

                                                             final Coordinate<Integer>     topLeftTile = crsProfile.crsToTileCoordinate(topLeft,     tileMatrixBounds, tileMatrixDimensions, tileOrigin);
                                                             final Coordinate<Integer> bottomRightTile = crsProfile.crsToTileCoordinate(bottomRight, tileMatrixBounds, tileMatrixDimensions, tileOrigin);

                                                             return new Range<>(topLeftTile, bottomRightTile);
                                                            }));
    }

    /**
     * Get the lowest-integer zoom level for the input {@link Dataset}
     *
     * @param dataset
     *             An input {@link Dataset}
     * @param tileRanges
     *             The calculated list of tile numbers and zooms
     * @param tileOrigin
     *             The {@link TileOrigin} of the tile grid
     * @param tileScheme
     *             The {@link TileScheme} of the tile grid
     * @param tileSize
     *             A {@link Dimensions} Integer object that describes what the tiles should look like
     * @return The zoom level for this dataset that produces only one tile.
     *            Defaults to 0 if an error occurs.
     */
    public static int getMinimalZoom(final Dataset                                  dataset,
                                     final Map<Integer, Range<Coordinate<Integer>>> tileRanges,
                                     final TileOrigin                               tileOrigin,
                                     final TileScheme                               tileScheme,
                                     final Dimensions<Integer>                      tileSize)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null");
        }

        if(tileRanges == null || tileRanges.isEmpty())
        {
            throw new IllegalArgumentException("Tile range list cannot be null or empty.");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Tile origin cannot be null.");
        }

        if(tileScheme == null)
        {
            throw new IllegalArgumentException("Tile scheme cannot be null.");
        }

        if(tileSize == null)
        {
            throw new IllegalArgumentException("Tile size cannot be null");
        }

        final Dimensions<Double> datasetPixelResolution = new GeoTransformation(dataset.GetGeoTransform()).getPixelResolution();
        final double zoomPixelSize;

        if(tileSize.getWidth() > tileSize.getHeight())
        {
            zoomPixelSize = (datasetPixelResolution.getWidth()  * dataset.GetRasterXSize()) / tileSize.getWidth();
        }
        else
        {
            zoomPixelSize = (datasetPixelResolution.getHeight() * dataset.GetRasterYSize()) / tileSize.getHeight();
        }

        try
        {
            final CrsProfile crsProfile = GdalUtility.getCrsProfile(dataset);
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
     * @param dataset
     *             An input {@link Dataset}
     * @param tileRanges
     *             The calculated list of tile numbers and zooms
     * @param tileOrigin
     *             The {@link TileOrigin} of the tile grid
     * @param tileScheme
     *             The {@link TileScheme} of the tile grid
     * @param tileSize
     *             A {@link Dimensions} Integer object that describes what the tiles should look like
     * @return The zoom level for this dataset that is closest to the actual
     *             resolution
     * @throws TileStoreException Thrown when a {@link GdalUtility#getCrsProfile(Dataset)} throws
     */
    public static int getMaximalZoom(final Dataset                                  dataset,
                                     final Map<Integer, Range<Coordinate<Integer>>> tileRanges,
                                     final TileOrigin                               tileOrigin,
                                     final TileScheme                               tileScheme,
                                     final Dimensions<Integer>                      tileSize) throws TileStoreException
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }

        if(tileRanges == null || tileRanges.isEmpty())
        {
            throw new IllegalArgumentException("Tile range list cannot be null or empty.");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Tile origin cannot be null.");
        }

        if(tileScheme == null)
        {
            throw new IllegalArgumentException("Tile scheme cannot be null.");
        }

        if(tileSize == null)
        {
            throw new IllegalArgumentException("Tile dimensions cannot be null.");
        }

        final double zoomPixelSize = dataset.GetGeoTransform()[1];

        try
        {
            final CrsProfile crsProfile = GdalUtility.getCrsProfile(dataset);
            return GdalUtility.zoomLevelForPixelSize(zoomPixelSize, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        }
        catch(final TileStoreException e)
        {
            throw new TileStoreException("Could not determine maximum zoom level.");
        }
    }

    /**
     * Return a {@link Set} of all the zoom levels in the input {@link Dataset}
     *
     * @param dataset
     *             An input {@link Dataset}
     * @param tileOrigin
     *             The {@link TileOrigin} of the tile grid
     * @param tileSize
     *              A {@link Dimensions} Integer object that describes what the
     *              tiles should look like
     * @return A set of integers for all the zoom levels in the input dataset
     * @throws TileStoreException Thrown if the input dataset bounds could not
     *             be retrieved
     */
    public static Set<Integer> getZoomLevels(final Dataset             dataset,
                                             final TileOrigin          tileOrigin,
                                             final Dimensions<Integer> tileSize) throws TileStoreException
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Tile origin cannot be null.");
        }

        if(tileSize == null)
        {
            throw new IllegalArgumentException("Tile dimensions cannot be null.");
        }

        // World extent tile scheme
        final ZoomTimesTwo tileScheme = new ZoomTimesTwo(0, 31, 1, 1);

        try
        {
            final BoundingBox datasetBounds = GdalUtility.getBounds    (dataset);
            final CrsProfile  crsProfile    = GdalUtility.getCrsProfile(dataset);

            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                        datasetBounds,
                                                                                                        crsProfile.getBounds(),
                                                                                                        crsProfile,
                                                                                                        tileOrigin);

            final int minZoom = GdalUtility.getMinimalZoom(dataset, tileRanges, tileOrigin, tileScheme, tileSize);
            final int maxZoom = GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, tileSize);

            return IntStream.rangeClosed(minZoom, maxZoom)
                            .boxed()
                            .collect(Collectors.toSet());
        }
        catch(final DataFormatException dfe)
        {
            throw new TileStoreException(dfe);
        }
    }

    /**
     * Return the appropriate zoom level based on the input pixel resolution
     *
     * @param zoomPixelSize
     *             The pixel resolution of the zoom level
     * @param tileRanges
     *             The calculated list of tile numbers and zooms
     * @param dataset
     *             An input {@link Dataset}
     * @param crsProfile
     *             A {@link CrsProfile} for the input area
     * @param tileScheme
     *             The {@link TileScheme} of the tile grid
     * @param tileOrigin
     *             The {@link TileOrigin} of the tile grid
     * @param tileSize
     *             A {@link Dimensions} Integer object that describes what the
     *             tiles should look like
     * @return The integer zoom matched to the pixel resolution
     * @throws TileStoreException
     *             When the bounds of the dataset could not be determined
     */
    public static int zoomLevelForPixelSize(final double                                   zoomPixelSize,
                                            final Map<Integer, Range<Coordinate<Integer>>> tileRanges,
                                            final Dataset                                  dataset,
                                            final CrsProfile                               crsProfile,
                                            final TileScheme                               tileScheme,
                                            final TileOrigin                               tileOrigin,
                                            final Dimensions<Integer>                      tileSize) throws TileStoreException
    {
        if(tileRanges == null || tileRanges.isEmpty())
        {
            throw new IllegalArgumentException("Tile range list cannot be null.");
        }

        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }

        if(crsProfile == null)
        {
            throw new IllegalArgumentException("Crs profile cannot be null.");
        }

        if(tileScheme == null)
        {
            throw new IllegalArgumentException("Tile scheme cannot be null.");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Tile origin cannot be null.");
        }

        if(tileSize == null)
        {
            throw new IllegalArgumentException("Tile dimensions cannot be null.");
        }

        try
        {
            final BoundingBox boundingBox = GdalUtility.getBounds(dataset);
            final int zoomLevelForPixelSize = tileRanges.entrySet()
                                                        .stream()
                                                        .filter(entrySet -> { return zoomLevelForPixelSize(tileScheme.dimensions(entrySet.getKey()),
                                                                                                           zoomPixelSize,
                                                                                                           entrySet.getValue(),
                                                                                                           crsProfile,
                                                                                                           tileScheme,
                                                                                                           tileOrigin,
                                                                                                           tileSize,
                                                                                                           boundingBox);
                                                                            })
                                                        .map(entrySet -> entrySet.getKey())
                                                        .findFirst()
                                                        .orElseThrow(() -> new NumberFormatException("Could not determine zoom level for pizel size: " + String.valueOf(zoomPixelSize)));

            return zoomLevelForPixelSize == 0 ? 0 : zoomLevelForPixelSize - 1;
        }
        catch(DataFormatException | NumberFormatException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    private static boolean zoomLevelForPixelSize(final TileMatrixDimensions       tileMatrixDimensions,
                                                 final double                     zoomPixelSize,
                                                 final Range<Coordinate<Integer>> tileRange,
                                                 final CrsProfile                 crsProfile,
                                                 final TileScheme                 tileScheme,
                                                 final TileOrigin                 tileOrigin,
                                                 final Dimensions<Integer>        tileSize,
                                                 final BoundingBox                boundingBox)
    {
        if(tileMatrixDimensions == null)
        {
            throw new IllegalArgumentException();
        }

        if(tileRange == null)
        {
            throw new IllegalArgumentException("Tile range cannot be null.");
        }

        if(crsProfile == null)
        {
            throw new IllegalArgumentException("Crs profile cannot be null.");
        }

        if(tileScheme == null)
        {
            throw new IllegalArgumentException("Tile scheme cannot be null.");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Tile origin cannot be null.");
        }

        if(tileSize == null)
        {
            throw new IllegalArgumentException("Tile dimensions cannot be null.");
        }

        // Get the tile coordinates of the top-left and bottom-right tiles
        final Coordinate<Integer> topLeftTile = crsProfile.crsToTileCoordinate(new CrsCoordinate(boundingBox.getTopLeft(),
                                                                               crsProfile.getCoordinateReferenceSystem()),
                                                                               crsProfile.getBounds(), // Use bounds of the world here
                                                                               tileMatrixDimensions,
                                                                               tileOrigin);

        final Coordinate<Integer> bottomRightTile = crsProfile.crsToTileCoordinate(new CrsCoordinate(boundingBox.getBottomRight(),
                                                                                   crsProfile.getCoordinateReferenceSystem()),
                                                                                   crsProfile.getBounds(), //boundingBox, Use bounds of the world here
                                                                                   tileMatrixDimensions,
                                                                                   tileOrigin);

        // Convert tile coordinates to crs coordinates: this will give us correct units-of-measure-per-pixel
        final Coordinate<Integer> topLeftCoord = tileOrigin.transform(TileOrigin.UpperLeft,
                                                                      topLeftTile.getX(),
                                                                      topLeftTile.getY(),
                                                                      tileMatrixDimensions);

        final Coordinate<Integer> bottomRightCoord = tileOrigin.transform(TileOrigin.LowerRight,
                                                                          bottomRightTile.getX(),
                                                                          bottomRightTile.getY(),
                                                                          tileMatrixDimensions);

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
        // TODO *WARNING* 'tiles wide' is used for both width and height calculations!
        final int zoomTilesWide = tileRange.getMaximum().getX() - tileRange.getMinimum().getX() + 1;
        final double zoomResolution;
        if(tileSize.getWidth() >= tileSize.getHeight())
        {
            final double width = bottomRightCrsFull.getX() - topLeftCrsFull.getX();
            zoomResolution = width / (zoomTilesWide * tileSize.getWidth());
        }
        else
        {
            final double height = topLeftCrsFull.getY() - bottomRightCrsFull.getY();
            zoomResolution = height / (zoomTilesWide * tileSize.getHeight());
        }

        return zoomPixelSize > zoomResolution;
    }

    /**
     * Warp an input {@link Dataset} into a different spatial reference system. Does
     * not correct for NODATA values.
     *
     * @param dataset
     *             An input {@link Dataset}
     * @param fromSrs
     *             Original spatial reference system of the <code>dataset</code>
     * @param toSrs
     *             Spatial reference system to warp the <code>dataset</code> to
     * @return A {@link Dataset} in the input {@link SpatialReference} requested
     */
    public static Dataset warpDatasetToSrs(final Dataset          dataset,
                                           final SpatialReference fromSrs,
                                           final SpatialReference toSrs)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }

        if(fromSrs == null)
        {
            throw new IllegalArgumentException("From-Srs cannot be null.");
        }

        if(toSrs == null)
        {
            throw new IllegalArgumentException("To-Srs cannot be null.");
        }

        final Dataset output = gdal.AutoCreateWarpedVRT(dataset,
                                                        fromSrs.ExportToWkt(),
                                                        toSrs.ExportToWkt(),
                                                        gdalconstConstants.GRA_Average);

        if(output == null)
        {
            throw new RuntimeException(new GdalError().getMessage());
        }

        return output;
    }

    /**
     * Scale a Dataset down into a smaller-sized Dataset using the average algorithm.
     *
     * @param queryDataset A {@link Dataset} that needs to be scaled down to a smaller size
     * @param dimensions  A {@link Dimensions} object containing the width and height
     *                       information for the output {@link Dataset}
     * @return A {@link Dataset} of the sizespecified in the width and height properties of
     *            the input {@link Dimensions} object
     * @throws TilingException Thrown when any band of the input query {@link Dataset} fails
     *                            to scale correctly with {@link gdal#RegenerateOverview(Band, Band, String)}
     */
    public static Dataset scaleQueryToTileSize(final Dataset queryDataset,
                                               final Dimensions<Integer> dimensions) throws TilingException
    {
        if(queryDataset == null)
        {
            throw new IllegalArgumentException("Query dataset cannot be null.");
        }
        if(dimensions == null)
        {
            throw new IllegalArgumentException("Tile dimensions cannot be null.");
        }
        // TODO: This just handles average resampling, it should be adjusted for other resampling types
        final Dataset tileDataInMemory = gdal.GetDriverByName("MEM").Create("",
                                                                            dimensions.getWidth(),
                                                                            dimensions.getHeight(),
                                                                            queryDataset.GetRasterCount());
        try
        {
            IntStream.rangeClosed(1, queryDataset.GetRasterCount())
                     .forEach(index -> {
                         final int resolution = gdal.RegenerateOverview(queryDataset.GetRasterBand(index),
                                                                         tileDataInMemory.GetRasterBand(index),
                                                                         "average");
                         if(resolution != 0)
                         {
                             throw new RuntimeException("Could not regenerate overview on band: " + String.valueOf(index));
                         }
                     });
        }
        catch(final RuntimeException ex)
        {
            throw new TilingException(ex);
        }
        return tileDataInMemory;
    }

    /**
     * Get the color values specified as NODATA in a Dataset.
     *
     * @param dataset An input {@link Dataset} that possibly has NODATA values
     * @return The NODATA values as a {@link Double} array
     */
    public static Double[] getNoDataValues(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        // Initialize a new double array of size 3
        final Double[] noDataValues = new Double[3];
        // Get the nodata value for each band
        IntStream.rangeClosed(1,  dataset.GetRasterCount())
                 .forEach(band -> {
                                    final Double[] noDataValue = new Double[1];
                                    dataset.GetRasterBand(band).GetNoDataValue(noDataValue);
                                    if(noDataValue.length != 0 && noDataValue[0] != null)
                                    {
                                        // Assumes only one value coming back from the band
                                        noDataValues[band-1] = noDataValue[0];
                                    }
                                   });
        // Is array still using the initialized values?
        if(noDataValues[0] == null && noDataValues[1] == null && noDataValues[2] == null)
        {
            return new Double[0];
        }
        // TODO: Is it possible to see a raster from GDAL with 2 bands? I think
        // only Mono and RGB options are possible
        if(noDataValues[0] != null)
        {
            noDataValues[1] = noDataValues[0];
            noDataValues[2] = noDataValues[0];
        }
        return noDataValues;
    }

    /**
     * Get the number of raster {@link Band}s in a Dataset.
     *
     * @param dataset An input {@link Dataset} containing a number of bands
     * @param alphaBand An alpha {@link Band}
     * @return The number of {@link Band}s in the input {@link Dataset}
     */
    public static int getRasterBandCount(final Dataset dataset,
                                         final Band alphaBand)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        if(alphaBand == null)
        {
            throw new IllegalArgumentException("Alpha band cannot be null.");
        }
        // TODO: The bitwise calc functionality needs to be verified from the python functionality
        final boolean bitwiseAlpha = (alphaBand.GetMaskFlags() & gdalconstConstants.GMF_ALPHA) != 0;
        return bitwiseAlpha || dataset.GetRasterCount() == 4 || dataset.GetRasterCount() == 2 ? dataset.GetRasterCount() - 1 : dataset.GetRasterCount();
    }

    /**
     * Get the index of the alpha {@link Band} of a Dataset, if any.
     *
     * @param dataset An input {@link Dataset} to search for an alpha {@link Band}
     * @return The index of the alpha band of the input Dataset if found
     * @throws TileStoreException Thrown when no alpha band could be detected.
     */
    public static int getAlphaBandIndex(final Dataset dataset) throws TileStoreException
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        return IntStream.rangeClosed(1, dataset.GetRasterCount())
                        .filter(index -> dataset.GetRasterBand(index).GetColorInterpretation() == gdalconstConstants.GCI_AlphaBand)
                        .findFirst()
                        .orElseThrow(() -> new TileStoreException("No Alpha band detected.  Call getAlphaBandIndex after correcting nodata color."));
    }

    /**
     * Correct an input raster {@link Dataset}s NODATA values to an alpha {@link Band}
     *
     * @param dataset An input {@link Dataset}
     * @return A dataset with an alpha band added that reflects the input Dataset's NODATA value
     */
    public static Dataset correctNoDataSimple(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        final boolean datasetHasAlphaBand = GdalUtility.hasAlpha(dataset);

        // If the dataset actually has an alpha band, return it
        if(datasetHasAlphaBand)
        {
            return dataset;
        }

        // Dataset has no alpha and is NOT a VRT
        if(!dataset.GetDriver().getShortName().equalsIgnoreCase("VRT"))
        {
            // Create a vrt of this dataset
            final Dataset vrtCopy = gdal.AutoCreateWarpedVRT(dataset);
            // Add an alpha band
            // TODO: This does not work even on VRT datasets.  Find out why.
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
     * Return whether or not the input Dataset has an alpha {@link Band}
     *
     * @param dataset An input {@link Dataset}
     * @return True if the input {@link Dataset} has an alpha {@link Band},
     *            false otherwise.
     */
    public static boolean hasAlpha(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        return IntStream.rangeClosed(1, dataset.GetRasterCount())
                        .anyMatch(index -> dataset.GetRasterBand(index).GetColorInterpretation() == gdalconstConstants.GCI_AlphaBand);
    }

    /**
     * Create a set of GDAL parameters for reading tile data and writing that
     * data to another Dataset.
     *
     * @param geoTransform An array of doubles representing the geotransform of the input dataset
     * @param boundingBox The {@link BoundingBox} of the tile query
     * @param dimensions The tile {@link Dimensions}
     * @param dataset The input {@link Dataset}
     * @return An object with all information necessary to perform GDAL ReadRaster and WriteRaster
     *            operations.
     */
    public static GdalRasterParameters getGdalRasterParameters(final double[] geoTransform,
                                                               final BoundingBox boundingBox,
                                                               final Dimensions<Integer> dimensions,
                                                               final Dataset dataset)
    {
        if(geoTransform.length == 0)
        {
            throw new IllegalArgumentException("Geotransform cannot be empty.");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be null.");
        }

        if(dimensions == null)
        {
            throw new IllegalArgumentException("Tile dimensions cannot be null.");
        }

        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }

        // This is sorcery of the darkest kind.  It works but it not fully understood.
        final int readX = (int)((boundingBox.getMinX() - geoTransform[0]) / geoTransform[1] + 0.001);
        final int readY = (int)((boundingBox.getMaxY() - geoTransform[3]) / geoTransform[5] + 0.001);

        final int readXSize = (int)(boundingBox.getWidth()  /  geoTransform[1] + 0.5);
        final int readYSize = (int)(boundingBox.getHeight() / -geoTransform[5] + 0.5);

        return new GdalRasterParameters(readX, readY, readXSize, readYSize, dimensions, dataset);
    }

    /**
     * Read a subset of image data from a raster {@link Dataset}.
     *
     * @param params A {@link GdalRasterParameters} object containing data on how the tile should be read from
     *                  the raster image
     * @param dataset The {@link Dataset} to read the tile data from
     * @return A {@link Byte} array of size @params.writeXSize() * @params.writeYSize() * @dataset.GetRasterCount()
     *            containing tile data for the area specified in @params
     * @throws TilingException Thrown when ReadRaster reports a failure
     */
    public static byte[] readRaster(final GdalRasterParameters params,
                                    final Dataset dataset) throws TilingException
    {
        if(params == null)
        {
            throw new IllegalArgumentException("GDAL parameters cannot be null.");
        }
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
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
        if(result != gdalconstConstants.CE_None)
        {
            throw new TilingException("Failure reported by ReadRaster call in GdalUtility.");
        }
        return imageData;
    }

    /**
     * Read a subset of image data from a raster {@link Dataset} directly using a {@link ByteBuffer}.
     *
     * @param params A GDAL parameters object containing data on how the tile should be read from
     *                  the raster image
     * @param dataset The {@link Dataset} to read the tile data from
     * @return A {@link Byte} array of size @params.writeXSize() * @params.writeYSize() * @dataset.GetRasterCount()
     *            containing tile data for the area specified in @params
     * @throws TilingException Thrown when ReadRaster_Direct reports a failure
     */
    public static ByteBuffer readRasterDirect(final GdalRasterParameters params,
                                              final Dataset dataset) throws TilingException
    {
        if(params == null)
        {
            throw new IllegalArgumentException("GDAL parameters cannot be null.");
        }
        if(dataset == null)
        {
            throw new IllegalArgumentException("Input dataset cannot be null.");
        }
        final int bandCount = dataset.GetRasterCount(); // correctNoDataSimple should have added an alpha band
        final ByteBuffer imageData = ByteBuffer.allocateDirect(params.getWriteXSize() * params.getWriteYSize() * bandCount);
        final int result = dataset.ReadRaster_Direct(params.getReadX(),
                                                     params.getReadY(),
                                                     params.getReadXSize(),
                                                     params.getReadYSize(),
                                                     params.getWriteXSize(),
                                                     params.getWriteYSize(),
                                                     gdalconstConstants.GDT_Byte,
                                                     imageData,
                                                     null); // Per documentation, will select the first nBandCount bands
        if(result != gdalconstConstants.CE_None)
        {
            throw new TilingException("Failure reported by ReadRaster call in GdalUtility.");
        }
        return imageData;
    }

    /**
     * Write tile data to an output {@link Dataset}.
     *
     * @param params The {@link GdalRasterParameters} containing data on how the tile should be
     *                  written to the returned {@link Dataset}
     * @param imageData A {@link Byte} array of size @params.writeXSize() * @params.writeYSize() * @dataset.GetRasterCount()
     *                        containing tile data for the area specified
     * @param bandCount The number of bands the output {@link Dataset} should have
     * @return A {@link Dataset} representing a tile image
     * @throws TilingException Thrown when WriteRaster reports a failure
     */
    public static Dataset writeRaster(final GdalRasterParameters params,
                                      final byte[] imageData,
                                      final int bandCount) throws TilingException
    {
        if(params == null)
        {
            throw new IllegalArgumentException("GDAL parameters cannot be null.");
        }
        if(imageData.length == 0)
        {
            throw new IllegalArgumentException("Image data must be non-zero length.");
        }
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
        if(result != gdalconstConstants.CE_None)
        {
            throw new TilingException("Failure reported by WriteRaster call in GdalUtility.");
        }
        return querySizeDatasetInMemory;
    }

    /**
     * Write tile data to an output {@link Dataset} directly using a {@link ByteBuffer}.
     * @param params The {@link GdalRasterParameters} containing data on how the tile should be
     *                  written to the returned {@link Dataset}
     * @param imageData A {@link Byte} array of size @params.writeXSize() * @params.writeYSize() * @dataset.GetRasterCount()
     *                        containing tile data for the area specified
     * @param bandCount The number of bands the output {@link Dataset} should have
     * @return A {@link Dataset} representing a tile image
     * @throws TilingException Thrown when WriteRaster_Direct reports a failure
     */
    public static Dataset writeRasterDirect(final GdalRasterParameters params,
                                            final ByteBuffer imageData,
                                            final int bandCount) throws TilingException
    {
        if(params == null)
        {
            throw new IllegalArgumentException("GDAL parameters cannot be null.");
        }
        if(imageData == null)
        {
            throw new IllegalArgumentException("Image data must be non-zero length.");
        }
        final Dataset querySizeDatasetInMemory = gdal.GetDriverByName("MEM").Create("", params.getQueryXSize(), params.getQueryYSize(), bandCount);
        final int result = querySizeDatasetInMemory.WriteRaster_Direct(params.getWriteX(),
                                                                       params.getWriteY(),
                                                                       params.getWriteXSize(),
                                                                       params.getWriteYSize(),
                                                                       params.getWriteXSize(),
                                                                       params.getWriteYSize(),
                                                                       gdalconstConstants.GDT_Byte,
                                                                       imageData,
                                                                       null); // Per documentation, will select the first nBandCount bands
        if(result != gdalconstConstants.CE_None)
        {
            throw new TilingException("Failure reported by WriteRasterDirect call in GdalUtility.");
        }
        return querySizeDatasetInMemory;
    }

    /**
     * An object containing all data necessary for GDAL ReadRaster and WriteRaster functions.
     *
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
        private final int queryXSize;
        private final int queryYSize;

        /**
         * @param readX The X-axis pixel location to start reading tile data from
         * @param readY The Y-axis pixel location to start reading tile data from
         * @param readXSize The amount of pixels to read in the X-axis
         * @param readYSize The amount of pixels to read in the Y-axis
         * @param dimensions The {@link Dimensions} of the tile grid
         * @param dataset The raster {@link Dataset} that is being manipulated
         */
        public GdalRasterParameters(final int readX,
                                    final int readY,
                                    final int readXSize,
                                    final int readYSize,
                                    final Dimensions<Integer> dimensions,
                                    final Dataset dataset)
        {
            if(dimensions == null)
            {
                throw new IllegalArgumentException("Dimensions of the tile system cannot be null.");
            }

            if(dataset == null)
            {
                throw new IllegalArgumentException("Input dataset must be supplied to GdalRasterParameters.");
            }

            // Points that dictate where a tile read occurs on the dataset
            this.readX = readX;
            this.readY = readY;

            // Size values that dictate how much data should be read from the dataset for
            // a tile read operation
            this.readXSize = readXSize;
            this.readYSize = readYSize;

            // Points on the write canvas that dictate where the read data should be written
            this.writeX = 0;
            this.writeY = 0;

            // Size values that indicate how large the tile query canvas should be
            // Hard coding the query to be larger size for later down-scaling
            this.queryXSize = 4 * dimensions.getWidth();
            this.queryYSize = 4 * dimensions.getHeight();

            // Size values that dictate how large the write canvas should be
            this.writeXSize = this.queryXSize;
            this.writeYSize = this.queryYSize;
            this.adjust(dataset);
        }

        /**
         * @return The point in the x axis where tile data should be read from
         */
        public int getReadX()
        {
            return this.readX;
        }

        /**
         * @return The point in the x axis where tile data should be read from
         */
        public int getReadY()
        {
            return this.readY;
        }

        /**
         * @return The raster read size for the x axis
         */
        public int getReadXSize()
        {
            return this.readXSize;
        }

        /**
         * @return The raster read size for the y axis
         */
        public int getReadYSize()
        {
            return this.readYSize;
        }

        /**
         * @return The point in the x axis where tile data should be written
         */
        public int getWriteX()
        {
            return this.writeX;
        }

        /**
         * @return The point in the y axis where tile data should be written
         */
        public int getWriteY()
        {
            return this.writeY;
        }

        /**
         * @return The raster canvas write size for the x axis
         */
        public int getWriteXSize()
        {
            return this.writeXSize;
        }

        /**
         * @return The raster canvas write size for the y axis
         */
        public int getWriteYSize()
        {
            return this.writeYSize;
        }

        /**
         * @return The size of the raster data query in the x axis
         */
        public int getQueryXSize()
        {
            return this.queryXSize;
        }

        /**
         * @return The size of the raster data query in the y axis
         */
        public int getQueryYSize()
        {
            return this.queryYSize;
        }

        /**
         * Adjust final read, write, and size parameters for a GDAL calls to ReadRaster and
         * Write Raster.
         *
         * @param dataset The input dataset with which all values will be adjusted from
         */
        private void adjust(final Dataset dataset)
        {
            if(this.readX < 0)
            {
                final int readXShift = Math.abs(this.readX);
                this.writeX = (int)(this.writeXSize * ((float)readXShift / this.readXSize));
                this.writeXSize -= this.writeX;
                this.readXSize -= (int)(this.readXSize * ((float)readXShift) / this.readXSize);
                this.readX = 0;
            }

            if(this.readX + this.readXSize > dataset.GetRasterXSize())
            {
                this.writeXSize = (int)(this.writeXSize * ((float)(dataset.GetRasterXSize() - this.readX) / this.readXSize));
                this.readXSize = dataset.GetRasterXSize() - this.readX;
            }

            if(this.readY < 0)
            {
                final int readYShift = Math.abs(this.readY);
                this.writeY = (int)(this.writeYSize * ((float)readYShift / this.readYSize));
                this.writeYSize -= this.writeY;
                this.readYSize -= (int)(this.readYSize * ((float)readYShift / this.readYSize));
                this.readY = 0;
            }

            if(this.readY + this.readYSize > dataset.GetRasterYSize())
            {
                this.writeYSize = (int)(this.writeYSize * ((float)(dataset.GetRasterYSize() - this.readY) / this.readYSize));
                this.readYSize = dataset.GetRasterYSize() - this.readY;
            }
        }
    }
}