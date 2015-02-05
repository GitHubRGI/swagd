/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.g2t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import utility.GdalUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.task.Settings;
import com.rgi.common.task.Settings.Profile;
import com.rgi.common.task.Settings.Setting;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Duff Means
 * @author Luke Lambert
 *
 */
public class TileJob implements Runnable
{
    private static final int TILESIZE = 256;
    //private static final String TILEEXT = "png";

    private final File       file;
    private final CrsProfile crsProfile;
    private final TileScheme tileScheme;
    private final Color      noDataColor;

    private Dataset          outputDataset;


    //private double[] inputGT;
    //private double[] outputGT;

    //private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //private List<Future<?>> tasks = new ArrayList<>();

    private final TileStoreReader tileStoreReader;
    private final TileStoreWriter tileStoreWriter;

    @SuppressWarnings("unused")
    private final TaskMonitor monitor;
    //private double workTotal = 0;
    //private int workUnits = 0;

    public TileJob(final File file,
                   final TileStoreReader tileStoreReader,
                   final TileStoreWriter tileStoreWriter,
                   final Settings settings,
                   final TaskMonitor monitor)
    {
        this.file            = file;
        this.tileStoreReader = tileStoreReader;
        this.tileStoreWriter = tileStoreWriter;
        this.monitor         = monitor;

        this.tileScheme = new ZoomTimesTwo(0, 31, 1, 1, TileOrigin.valueOf(settings.get(Setting.TileOrigin)));

        this.noDataColor = settings.getColor(Setting.NoDataColor);

        final Profile profileSetting = Settings.Profile.valueOf(settings.get(Setting.CrsProfile));

        this.crsProfile = CrsProfileFactory.create(profileSetting.getAuthority(),
                                                   profileSetting.getID());
    }

    @Override
    public void run()
    {
        osr.UseExceptions();
        gdal.AllRegister();

        try
        {
            this.tile();
            System.out.println("Done.");
        }
        catch(final TilingException ex)
        {
            System.err.println("Unable to complete tiling job: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void tile() throws TilingException
    {
        this.outputDataset = getTransformedDataset(getDataset(this.file),
                                                   this.crsProfile.getCoordinateReferenceSystem().getIdentifier(),
                                                   gdalconstConstants.GRA_Bilinear);            // TODO: get user preference resample quality

        final GeoTransformation outputGeoTransform = new GeoTransformation(this.outputDataset.GetGeoTransform());

        final Dimensions outputDatasetRasterDimensions = new Dimensions(this.outputDataset.getRasterYSize(),
                                                                        this.outputDataset.getRasterXSize());

        // Image georeference points
        final CrsCoordinate imageUpperLeft  = new CrsCoordinate(outputGeoTransform.getTopLeft(), this.crsProfile.getCoordinateReferenceSystem());
        final CrsCoordinate imageLowerRight = new CrsCoordinate(outputGeoTransform.getBottomRight(this.outputDataset.getRasterYSize(),
                                                                                                  this.outputDataset.getRasterXSize()),
                                                                this.crsProfile.getCoordinateReferenceSystem());

        final int maxZoom = getMaxZoom(this.crsProfile.getBounds(), outputGeoTransform.getPixelDimensions());

        final int minZoom = this.getMinZoom(outputGeoTransform,
                                            outputDatasetRasterDimensions,
                                            imageUpperLeft,
                                            imageLowerRight);



        // generate base tile set
        final BufferedImage source = GdalUtility.convert(this.outputDataset);

        Coordinate<Integer> upperLeftTileCoordinate  = this.crsToTileCoordinate(imageUpperLeft,  maxZoom);
        Coordinate<Integer> lowerRightTileCoordinate = this.crsToTileCoordinate(imageLowerRight, maxZoom);

        int numTilesWidth  = Math.abs(lowerRightTileCoordinate.getX() - upperLeftTileCoordinate.getX()) + 1;
        int numTilesHeight = Math.abs(lowerRightTileCoordinate.getY() - upperLeftTileCoordinate.getY()) + 1;

        int zoomLevelTiles = (int)Math.pow(2, maxZoom);

        // srs units (e.g. meters) per pixel = (world size / num tiles) / pixels per tile
        double rx = (this.crsProfile.getBounds().getWidth()  / zoomLevelTiles) / TILESIZE;
        double ry = (this.crsProfile.getBounds().getHeight() / zoomLevelTiles) / TILESIZE;

        // pixels = (pixels * meters per pixel) / meters per pixel
        // w' = (w * r) / r'
        int scaledHeight = (int)((this.outputDataset.getRasterYSize() * outputGeoTransform.getPixelDimensions().getHeight()) / ry);
        int scaledWidth =  (int)((this.outputDataset.getRasterXSize() * outputGeoTransform.getPixelDimensions().getWidth())  / rx);

        Dimensions tileBounds = this.crsProfile.getTileDimensions(this.tileScheme.dimensions(maxZoom));

        // pixels = (meters - meters) / meters per pixel
        int offsetY = (int)((tileBounds.getHeight() - outputGeoTransform.getTopLeft().getY()) / ry);
        int offsetX = (int)((outputGeoTransform.getTopLeft().getX() - tileBounds.getWidth())  / rx);

        for(int x = 0; x < numTilesWidth; ++x)
        {
            final int tileX = 0;//upperLeftTileCoordinate.getX() + (x * this.tileScheme.origin().getDeltaX()); // TODO

            for(int y = 0; y < numTilesHeight; ++y)
            {
                final int tileY = 0;//upperLeftTileCoordinate.getY() + (y * this.tileScheme.origin().getDeltaY()); // TODO

                final BufferedImage tileImage = new BufferedImage(TILESIZE,
                                                                  TILESIZE,
                                                                  BufferedImage.TYPE_INT_ARGB);

                final Graphics2D graphic = tileImage.createGraphics();

                graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                         RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                graphic.setColor(this.noDataColor);
                graphic.fillRect(0, 0, TILESIZE, TILESIZE);

                graphic.drawImage(source,
                                  offsetX - (x * TILESIZE),
                                  offsetY - (y * TILESIZE),
                                  scaledWidth,
                                  scaledHeight,
                                  null);

                try
                {
                    this.tileStoreWriter.addTile(this.crsProfile.tileToCrsCoordinate(tileY,
                                                                                     tileX,
                                                                                     this.tileScheme.dimensions(maxZoom),
                                                                                     this.tileScheme.getOrigin()),
                                                 maxZoom,
                                                 tileImage);
                }
                catch(final TileStoreException ex)
                {
                    throw new TilingException("Unable to add tile", ex);
                }
            }
        }

        for(int z = maxZoom - 1; z >= minZoom; --z)
        {
            upperLeftTileCoordinate  = this.crsToTileCoordinate(imageUpperLeft,  z);
            lowerRightTileCoordinate = this.crsToTileCoordinate(imageLowerRight, z);

            numTilesWidth  = Math.abs(lowerRightTileCoordinate.getX() - upperLeftTileCoordinate.getX()) + 1;
            numTilesHeight = Math.abs(lowerRightTileCoordinate.getY() - upperLeftTileCoordinate.getY()) + 1;

            zoomLevelTiles = (int)Math.pow(2, z);

            // srs units (e.g. meters) per pixel = (world size / num tiles) / pixels per tile
            rx = (this.crsProfile.getBounds().getWidth()  / zoomLevelTiles) / TILESIZE;
            ry = (this.crsProfile.getBounds().getHeight() / zoomLevelTiles) / TILESIZE;

            // pixels = (pixels * meters per pixel) / meters per pixel
            // w' = (w * r) / r'
            scaledHeight = (int)((this.outputDataset.getRasterYSize() * outputGeoTransform.getPixelDimensions().getHeight()) / ry);
            scaledWidth  = (int)((this.outputDataset.getRasterXSize() * outputGeoTransform.getPixelDimensions().getWidth())  / rx);

            tileBounds = this.crsProfile.getTileDimensions(this.tileScheme.dimensions(maxZoom));

            // pixels = (meters - meters) / meters per pixel
            offsetX = (int)((outputGeoTransform.getTopLeft().getX() - tileBounds.getWidth())  / rx);
            offsetY = (int)((tileBounds.getHeight() - outputGeoTransform.getTopLeft().getY()) / ry);

            for(int x = 0; x < numTilesWidth; ++x)
            {
                final int tileX = 0;//upperLeftTileCoordinate.getX() + (x * this.tileScheme.origin().getDeltaX()); // TODO

                for(int y = 0; y < numTilesHeight; ++y)
                {
                    final int tileY = 0;//upperLeftTileCoordinate.getY() + (y * this.tileScheme.origin().getDeltaY());// TODO

                    final BufferedImage tileImage = new BufferedImage(TILESIZE,
                                                                      TILESIZE,
                                                                      BufferedImage.TYPE_INT_ARGB);

                    final Graphics2D graphic = tileImage.createGraphics();

                    graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    // Generate tile using next highest zoom level's tiles.
                    final BufferedImage preScaled = new BufferedImage(2 * TILESIZE,
                                                                      2 * TILESIZE,
                                                                      BufferedImage.TYPE_INT_ARGB);

                    final Graphics2D g_scaled = preScaled.createGraphics();

                    for(int zx = 0; zx < 2; ++zx)
                    {
                        for(int zy = 0; zy < 2; ++zy)
                        {
                            // The coordinate of the tile that we need to stitch into our new tile is
                            // double our current coordinate, plus the appropriate offset based on the
                            // iterator and origin.
                            final int absTileX = 0;//(2 * tileX) + f(zx, this.tileScheme.origin().getDeltaX()); // TODO
                            final int absTileY = 0;//(2 * tileY) + f(zy, this.tileScheme.origin().getDeltaY()); // TODO

                            try
                            {
                                final CrsCoordinate crsCoordinate = this.crsProfile.tileToCrsCoordinate(absTileY,
                                                                                                        absTileX,
                                                                                                        this.tileScheme.dimensions(z),
                                                                                                        this.tileScheme.getOrigin());

                                final BufferedImage upperTile = this.tileStoreReader.getTile(crsCoordinate, z);

                                if(upperTile == null)
                                {
                                    g_scaled.setColor(this.noDataColor);
                                    g_scaled.fillRect(zx * TILESIZE,
                                                      zy * TILESIZE,
                                                      TILESIZE,
                                                      TILESIZE);
                                }
                                else
                                {
                                    g_scaled.drawImage(upperTile,
                                                       zx * TILESIZE,
                                                       zy * TILESIZE,
                                                       TILESIZE,
                                                       TILESIZE,
                                                       null);
                                }
                            }
                            catch(final Exception e)
                            {
                                throw new TilingException("Problem getting tile", e);
                            }
                        }
                    }

                    graphic.drawImage(preScaled,
                                      0,
                                      0,
                                      TILESIZE,
                                      TILESIZE,
                                      null);

                    try
                    {
                        this.tileStoreWriter.addTile(this.crsProfile.tileToCrsCoordinate(tileY,
                                                                                         tileX,
                                                                                         this.tileScheme.dimensions(z),
                                                                                         this.tileScheme.getOrigin()),
                                                     z,
                                                     tileImage);
                    }
                    catch(final TileStoreException ex)
                    {
                        throw new TilingException("Unable to add adding tile", ex);
                    }
                }
            }
        }
    }

    private static Dataset getDataset(final File file) throws TilingException
    {
        final Dataset dataset = gdal.Open(file.getAbsolutePath(),
                                          gdalconstConstants.GA_ReadOnly);

        if(dataset == null)
        {
            throw new TilingException(String.format("GDALOpen failed: <%d> %s",
                                                    gdal.GetLastErrorNo(),
                                                    gdal.GetLastErrorMsg()));
        }

        return dataset;
    }

    /**
     * @param inputDataset
     *             Dataset to be transformed
     * @param requestedEpsgCrsIdentifier
     *             Coordinate Reference System (CRS) identifier, as defined by the EPSG
     * @param resampleAlgorithm
     *             Must be one of the gdalconstConstants.GRA_* defines
     * @return
     * @throws TilingException
     */
    private static Dataset getTransformedDataset(final Dataset inputDataset, final int requestedEpsgCrsIdentifier, final int resampleAlgorithm) throws TilingException
    {
        if(inputDataset.getRasterCount() == 0)
        {
            throw new TilingException("Input file contains no rasters");
        }

        final GeoTransformation inputGeoTransform = new GeoTransformation(inputDataset.GetGeoTransform());

        if(!inputGeoTransform.isNorthUp())
        {
            throw new IllegalArgumentException("The geotransformation of the input file contains unsupported rotation or skew.");
        }

        if(inputGeoTransform.equals(GeoTransformation.Identity) && inputDataset.GetGCPCount() == 0)    // GCP == Ground Control Point -> http://www.gdal.org/structGDAL__GCP.html#_details
        {
            throw new IllegalArgumentException("Unable to generate non-raster tiles from non-georeferenced input");
        }

        final String inputWkt = getWellKnownText(inputDataset);

        // If there's no input projection, there's nothing to reproject
        if(inputWkt == null)
        {
            return inputDataset;
        }

        final SpatialReference inputSrs = new SpatialReference(inputWkt);

        final SpatialReference outputSrs = new SpatialReference();
        outputSrs.ImportFromEPSG(requestedEpsgCrsIdentifier);

        // If the input projection matches output projection there's no need to reproject
        if(inputSrs.ExportToProj4().equals(outputSrs.ExportToProj4()))
        {
            return inputDataset;
        }

        final Dataset outputDataset = gdal.AutoCreateWarpedVRT(inputDataset,
                                                               inputWkt,
                                                               outputSrs.ExportToWkt(),
                                                               resampleAlgorithm);
        if(outputDataset == null)
        {
            throw new TilingException(String.format("Failed to create a reprojected data set: AutoCreateWarpedVRT returned null: <%d> %s",
                                                   gdal.GetLastErrorNo(),
                                                   gdal.GetLastErrorMsg()));
        }

        return outputDataset;
    }

    private static String getWellKnownText(final Dataset dataset)
    {
        final String wellKnownText = dataset.GetProjection();
        if(wellKnownText == null && dataset.GetGCPCount() != 0)
        {
            return dataset.GetGCPProjection();
        }

        return wellKnownText;
    }

    private static int getMaxZoom(final BoundingBox world, final Dimensions transformPixelDimensions)
    {
        final int yMaxZoom = getMaxZoom(world.getHeight(), transformPixelDimensions.getHeight());
        final int xMaxZoom = getMaxZoom(world.getWidth(),  transformPixelDimensions.getWidth());

        final double zoomYResolution     = world.getHeight() / (Math.pow(2, yMaxZoom) * TILESIZE);
        final double zoomXloorResolution = world.getWidth()  / (Math.pow(2, xMaxZoom) * TILESIZE);

        // crsDistance / (Math.pow(2, zoomFloor)   * TILESIZE) is the resolution for that zoom level
        return Math.abs(transformPixelDimensions.getHeight() - zoomYResolution) <
               Math.abs(transformPixelDimensions.getWidth()  - zoomXloorResolution) ? yMaxZoom
                                                                                    : xMaxZoom;
    }

    /**
     * @param crsDistance
     *             Height or width of the world's bounding box in CRS units
     */
    private static int getMaxZoom(final double crsDistance, final double pixelDistance)
    {
        // The resolution in srs units per pixel of the reprojected source image
        // probably won't perfectly match the resolution of one of our zoom levels,
        // so we need to determine which zoom level it's closest to, so we can
        // scale down or up with the least loss or addition of data.
        // Since zoom level resolutions will always be related to the number of
        // tiles at a zoom level, and number of tiles will always be a power of 2,
        // we can reverse the calculation to get a fractional zoom level.
        // Resolution = Size of the World / ( Number of Tiles * Pixels per Tile )
        // and Number of Tiles(z) = 2 ^ z
        // so
        // z = log2( Size of the World / (Resolution * Pixels per Tile) )
        final double fractionalZoom = log2(crsDistance / (pixelDistance * TILESIZE));

        // Now the 2 closest zoom levels will be the ceiling and floor of our
        // fractional zoom level. We'll calculate the resolution for each zoom
        // level, and the difference between the zoom level resolution and
        // our source image resolution. Whichever zoom level has the smallest
        // difference in resolution from our source resolution will become our
        // max zoom level.
        // if all resolutions are identical, default to zooming larger

        final int zoomCeiling = (int)Math.ceil (fractionalZoom);
        final int zoomFloor   = (int)Math.floor(fractionalZoom);

        final double zoomCeilingResolution = crsDistance / (Math.pow(2, zoomCeiling) * TILESIZE);
        final double zoomFloorResolution   = crsDistance / (Math.pow(2, zoomFloor)   * TILESIZE);

        // crsDistance / (Math.pow(2, zoomFloor)   * TILESIZE) is the resolution for that zoom level
        return Math.abs(pixelDistance - zoomCeilingResolution) <
               Math.abs(pixelDistance - zoomFloorResolution) ? zoomFloor
                                                             : zoomCeiling;
    }

    private int getMinZoom(final GeoTransformation geoTransformation,
                           final Dimensions        rasterDimensions,
                           final CrsCoordinate     imageUpperLeft,
                           final CrsCoordinate     imageLowerRight)
    {
        final BoundingBox worldBounds = this.crsProfile.getBounds();

        // Next, find the zoom level where the entire image fits one tile.
        // similar to the above calculation for closest zoom level, but
        // this time instead of choosing between ceiling and floor
        // zoom levels, we'll always choose the floor. As before, the
        // calculation for fractional zoom level is:
        // Resolution = Size of the World / ( Number of Tiles * Pixels per Tile )
        // where, again, Number of Tiles is 2 ^ z
        // This time Resolution = Size of the Image / Pixels per Tile
        // Plugging in this resolution, Pixels per Tile multiplies out,
        // and the calculation reduces to:
        // z = log2( Size of the World / Size of the Image )
        // again, do it once for width and once for height
        int minZoom = Math.min((int)Math.floor(log2(worldBounds.getWidth()  / (geoTransformation.getPixelDimensions().getWidth()  * rasterDimensions.getWidth()))),
                               (int)Math.floor(log2(worldBounds.getHeight() / (geoTransformation.getPixelDimensions().getHeight() * rasterDimensions.getHeight()))));

        // We know our minimum zoom is of sufficient resolution that our source
        // image will fit entirely within the tile size, but it  might not be
        // aligned appropriately to actually fall entirely within the bounds of
        // a single tile. As such, we need to decrease the minimum zoom until
        // the entire image fits entirely within the bounds of a single tile.
        // If we've done our work right this should happen no more than once,
        // but just in case, we'll put it in a loop.
        while(minZoom > 0)
        {
            final Coordinate<Integer> minZoomUpperLeftTileCoordinate  = this.crsToTileCoordinate(imageUpperLeft,  minZoom);
            final Coordinate<Integer> minZoomLowerRightTileCoordinate = this.crsToTileCoordinate(imageLowerRight, minZoom);

            // Calculate the number of tiles these coordinates span.
            // We take the absolute value so we don't have to care about
            // origin, and whether the coordinates increase or decrease left to
            // right and top to bottom. Add one because the max coordinate
            // should be inclusive of its tile, not exclusive
            final int numTilesWidth  = Math.abs(minZoomUpperLeftTileCoordinate.getX() - minZoomLowerRightTileCoordinate.getX()) + 1;
            final int numTilesHeight = Math.abs(minZoomUpperLeftTileCoordinate.getY() - minZoomLowerRightTileCoordinate.getY()) + 1;

            // If the number of tiles needed for either height or width
            // exceeds 1, decrease the minimum zoom level and try again.
            if(numTilesWidth <= 1 && numTilesHeight <= 1)
            {
                break;
            }
            --minZoom;
        }

        return minZoom;
    }

    // Given a z value of 0 or 1, and a d value of -1 or 1,
    // inverts z if d is -1.
    // the long answer:
    // When stitching tiles from a higher zoom level
    // together to create a tile for this zoom level,
    // we need to get the correct tile coordinates
    // based on the tile set's origin. The origin contains
    // a delta value (d) for the x and y directions, to inform
    // us whether it is a left-to-right or right-to-left
    // system, and top-to-bottom or bottom-to-top system.
    // we always iterate left-to-right and top-to-bottom (i.e.
    // upper left origin) but if our origin is lower right (dx=-1, dy=-1),
    // then we need to transform our iterators like so:
    // +---+---+     +---+---+
    // |0,0|0,1|     |1,1|1,0|
    // +---+---+ --> +---+---+
    // |1,0|1,1|     |0,1|0,0|
    // +---+---+     +---+---+
    // e.g.: getting source tiles to build tile 4,4:
    // z=n             z=n+1         z=n+1
    //               upper left    lower right
    // +-------+     +---+---+     +---+---+
    // |       |     |8,8|9,8|     |9,9|8,9|
    // |  4,4  |     +---+---+     +---+---+
    // |       |     |8,9|9,9|     |9,8|8,8|
    // +-------+     +---+---+     +---+---+
    /**
     * given a z value of 0 or 1, and a d value of -1 or 1,
     * inverts z if d is -1.
     * @param z the iterator, 0 or 1
     * @param d the origin delta, -1 or 1
     * @return the transformed z value
     */
    private static int f(final int z, final int d)
    {
        return d > 0 ? z
                     : z > 0 ? 0
                             : 1;
    }

//    public BufferedImage convert(final Dataset dataset) throws TilingException
//    {
//        Band band = null;
//
//        final int          bandCount = dataset.getRasterCount();
//        final ByteBuffer[] bands     = new ByteBuffer[bandCount];
//        final int[]        banks     = new int       [bandCount];
//        final int[]        offsets   = new int       [bandCount];
//
//        final int xsize = dataset.getRasterXSize();
//        final int ysize = dataset.getRasterYSize();
//        final int pixels = xsize * ysize;
//
//        int bandBufferType = 0;
//
//        for(int bandIndex = 0; bandIndex < bandCount; ++bandIndex)
//        {
//            // Bands are not 0-base indexed, so we must add 1
//            band = dataset.GetRasterBand(bandIndex + 1);
//
//            bandBufferType = band.getDataType();
//            final int bandBufferSize = pixels * (gdal.GetDataTypeSize(bandBufferType) / 8);
//
//            final ByteBuffer data = ByteBuffer.allocateDirect(bandBufferSize);
//            data.order(ByteOrder.nativeOrder());
//
//            if(band.ReadRaster_Direct(0,
//                                      0,
//                                      band.getXSize(),
//                                      band.getYSize(),
//                                      xsize,
//                                      ysize,
//                                      bandBufferType,
//                                      data) != gdalconstConstants.CE_None)
//            {
//                throw new TilingException(String.format("Reading a raster band of the image failed: <%d> %s",
//                                                        gdal.GetLastErrorNo(),
//                                                        gdal.GetLastErrorMsg()));
//            }
//
//            bands  [bandIndex] = data;
//            banks  [bandIndex] = bandIndex;
//            offsets[bandIndex] = 0;
//        }
//
//        if(band == null)
//        {
//            throw new RuntimeException("The GDAL dataset returned null for a raster band");
//        }
//
//        final ImageProperties imageProperties = this.getImageProperties(band, bandCount, pixels, bands);
//        final SampleModel     sampleModel     = new BandedSampleModel(imageProperties.bufferType, xsize, ysize, xsize, banks, offsets);
//        final WritableRaster  raster          = Raster.createWritableRaster(sampleModel, imageProperties.imageBuffer, null);
//
//        BufferedImage img = null;
//
//        if(band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex)
//        {
//            img = new BufferedImage(band.GetRasterColorTable().getIndexColorModel(gdal.GetDataTypeSize(bandBufferType)),
//                                    raster,
//                                    false,
//                                    null);
//        }
//        else
//        {
//            if(bandCount > 2)
//            {
//                final ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
//                                                                      bandCount == 4,
//                                                                      false,
//                                                                      bandCount == 4 ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
//                                                                      imageProperties.bufferType);
//
//                img = new BufferedImage(colorModel, raster, true, null);
//            }
//            else
//            {
//                img = new BufferedImage(xsize, ysize, imageProperties.dataType);
//                img.setData(raster);
//            }
//        }
//
//        return img;
//    }

    private Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate crsCoordinate, final int zoomLevel)
    {
        return this.crsProfile.crsToTileCoordinate(crsCoordinate,
                                                   this.tileScheme.dimensions(zoomLevel),
                                                   this.tileScheme.getOrigin());
    }

    private static double log2(final double value)
    {
        final double log2 = Math.log(2);

        return Math.log10(value) / log2;
    }

//    private ImageProperties getImageProperties(final Band band, final int bandCount, final int pixels, final ByteBuffer[] bands) throws TilingException
//    {
//        final ImageProperties imageProperties = this.new ImageProperties();
//
//        final int bandBufferType = band.getDataType();
//
//        if(bandBufferType == gdalconstConstants.GDT_Byte)
//        {
//            final byte[][] bytes = new byte[bandCount][];
//            for(int i = 0; i < bandCount; i++)
//            {
//                bytes[i] = new byte[pixels];
//                bands[i].get(bytes[i]);
//            }
//
//            imageProperties.imageBuffer = new DataBufferByte(bytes, pixels);
//            imageProperties.bufferType  = DataBuffer.TYPE_BYTE;
//            imageProperties.dataType    = (band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex) ? BufferedImage.TYPE_BYTE_INDEXED
//                                                                                                                       : BufferedImage.TYPE_BYTE_GRAY;
//        }
//        else if(bandBufferType == gdalconstConstants.GDT_Int16)
//        {
//            final short[][] shorts = new short[bandCount][];
//            for(int i = 0; i < bandCount; i++)
//            {
//                shorts[i] = new short[pixels];
//                bands[i].asShortBuffer().get(shorts[i]);
//            }
//            imageProperties.imageBuffer = new DataBufferShort(shorts, pixels);
//            imageProperties.bufferType  = DataBuffer.TYPE_USHORT;
//            imageProperties.dataType    = BufferedImage.TYPE_USHORT_GRAY;
//        }
//        else if(bandBufferType == gdalconstConstants.GDT_Int32)
//        {
//            final int[][] ints = new int[bandCount][];
//            for(int i = 0; i < bandCount; i++)
//            {
//                ints[i] = new int[pixels];
//                bands[i].asIntBuffer().get(ints[i]);
//            }
//
//            imageProperties.imageBuffer = new DataBufferInt(ints, pixels);
//            imageProperties.bufferType  = DataBuffer.TYPE_INT;
//            imageProperties.dataType    = BufferedImage.TYPE_CUSTOM;
//        }
//        else
//        {
//            throw new TilingException("Unhandled image band data type");
//        }
//
//        return imageProperties;
//    }
//
//    private class ImageProperties
//    {
//        DataBuffer imageBuffer;
//        int        dataType;
//        int        bufferType;
//    }
}
