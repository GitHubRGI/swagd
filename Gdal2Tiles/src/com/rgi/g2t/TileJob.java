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

package com.rgi.g2t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.IntFunction;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import utility.GdalError;
import utility.GdalUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
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
    private final File                file;
    private final CrsProfile          crsProfile;
    private final Color               noDataColor;
    private final Dimensions<Integer> tileDimensions;

    //private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //private List<Future<?>> tasks = new ArrayList<>();

    private final TileStoreReader tileStoreReader;
    private final TileStoreWriter tileStoreWriter;

    @SuppressWarnings("unused")
    private final TaskMonitor monitor;
    //private double workTotal = 0;
    //private int workUnits = 0;

    /**
     * Constructor
     *
     * @param file
     *             Source image
     * @param tileStoreWriter
     *             Destination tile store
     * @param tileDimensions
     *             Desired tile pixel width and height
     * @param noDataColor
     *             Default tile color
     * @param monitor
     *             Process monitor
     */
    public TileJob(final File                file,
                   //final TileStoreReader     tileStoreReader,
                   final TileStoreWriter     tileStoreWriter,
                   final Dimensions<Integer> tileDimensions,
                   final Color               noDataColor,
                   final TaskMonitor         monitor)
    {
        this.file            = file;
        this.tileStoreReader = null;//tileStoreReader;
        this.tileStoreWriter = tileStoreWriter;
        this.tileDimensions  = tileDimensions;
        this.monitor         = monitor;
        this.noDataColor     = noDataColor;
        this.crsProfile      = CrsProfileFactory.create(tileStoreWriter.getCoordinateReferenceSystem());
    }

    @Override
    public void run()
    {
        osr.UseExceptions();
        gdal.AllRegister();

        try
        {
            this.tile();
            gdal.GDALDestroyDriverManager();
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
        final Dataset dataset = getTransformedDataset(getDataset(this.file),
                                                      this.crsProfile.getCoordinateReferenceSystem().getIdentifier(),
                                                      gdalconstConstants.GRA_Bilinear);            // TODO: get user preference resample quality

        final GeoTransformation geoTransform = new GeoTransformation(dataset.GetGeoTransform());

        final Dimensions<Integer> rasterDimensions = new Dimensions<>(dataset.getRasterXSize(),
                                                                      dataset.getRasterYSize());

        final BoundingBox imageCrsBounds = geoTransform.getBounds(dataset);

        final int maxZoom = this.getMaxZoom(this.crsProfile.getBounds(), geoTransform.getPixelDimensions());

        final int minZoom = this.getMinZoom(geoTransform,
                                            rasterDimensions,
                                            imageCrsBounds);

        final BufferedImage source = GdalUtility.convert(dataset);

        for(int zoomLevel = maxZoom; zoomLevel >= minZoom; --zoomLevel)
        {
            final TileMatrixDimensions tileMatrixDimensions = this.tileStoreWriter.getTileScheme().dimensions(zoomLevel);

            final Coordinate<Integer> upperLeftTileCoordinate  = this.crsToTileCoordinate(imageCrsBounds.getTopLeft(),     this.crsProfile.getBounds(), tileMatrixDimensions);
            final Coordinate<Integer> lowerRightTileCoordinate = this.crsToTileCoordinate(imageCrsBounds.getBottomRight(), this.crsProfile.getBounds(), tileMatrixDimensions);

            final int numTilesWidth  = Math.abs(lowerRightTileCoordinate.getX() - upperLeftTileCoordinate.getX()) + 1;
            final int numTilesHeight = Math.abs(lowerRightTileCoordinate.getY() - upperLeftTileCoordinate.getY()) + 1;

            for(int x = 0; x < numTilesWidth; ++x)
            {
                for(int y = 0; y < numTilesHeight; ++y)
                {
                    final int tileX = 0;//upperLeftTileCoordinate.getX() + (x * this.tileScheme.origin().getDeltaX()); // TODO
                    final int tileY = 0;//upperLeftTileCoordinate.getY() + (y * this.tileScheme.origin().getDeltaY()); // TODO

                    final BufferedImage tileImage = new BufferedImage(this.tileDimensions.getWidth(),
                                                                      this.tileDimensions.getHeight(),
                                                                      BufferedImage.TYPE_INT_ARGB);

                    final Graphics2D graphic = tileImage.createGraphics();

                    graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    if(zoomLevel == maxZoom)
                    {
                        // CRS units (e.g. meters) per pixel = (world size / num tiles) / pixels per tileS
                        final double rx = (this.crsProfile.getBounds().getWidth()  / tileMatrixDimensions.getWidth())  / this.tileDimensions.getWidth();
                        final double ry = (this.crsProfile.getBounds().getHeight() / tileMatrixDimensions.getHeight()) / this.tileDimensions.getHeight();

                        final double tileWidth  = this.crsProfile.getBounds().getHeight() / tileMatrixDimensions.getHeight();
                        final double tileHeight = this.crsProfile.getBounds().getWidth()  / tileMatrixDimensions.getWidth();

                        // pixels = (pixels * meters per pixel) / meters per pixel
                        // w' = (w * r) / r'
                        final int scaledHeight = (int)((dataset.getRasterYSize() * geoTransform.getPixelDimensions().getHeight()) / ry);
                        final int scaledWidth  = (int)((dataset.getRasterXSize() * geoTransform.getPixelDimensions().getWidth())  / rx);

                        // pixels = (meters - meters) / meters per pixel
                        final int offsetY = (int)((geoTransform.getTopLeft().getY() - tileHeight) / ry);
                        final int offsetX = (int)((tileWidth - geoTransform.getTopLeft().getX())  / rx);

                        // generate base tile set
                        graphic.setColor(this.noDataColor);
                        graphic.fillRect(0,
                                         0,
                                         this.tileDimensions.getWidth(),
                                         this.tileDimensions.getHeight());

                        graphic.drawImage(source,
                                          offsetX - (x * this.tileDimensions.getWidth()),
                                          offsetY - (y * this.tileDimensions.getHeight()),
                                          scaledWidth,
                                          scaledHeight,
                                          null);

//                        try
//                        {
//                            ImageIO.write(tileImage, "png", new File("c:/users/corp/desktop/bar.png"));
//                            return;
//                        }
//                        catch(final IOException ex)
//                        {
//                            // TODO Auto-generated catch block
//                            ex.printStackTrace();
//                        }
                    }
                    else
                    {
                        // Generate tile using next highest zoom level's tiles.
                        final BufferedImage preScaled = new BufferedImage(2 * this.tileDimensions.getWidth(),
                                                                          2 * this.tileDimensions.getHeight(),
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
                                    final CrsCoordinate crsCoordinate = this.tileToCrsCoordinate(absTileY,
                                                                                                 absTileX,
                                                                                                 imageCrsBounds,
                                                                                                 zoomLevel);

                                    final BufferedImage upperTile = this.tileStoreReader.getTile(crsCoordinate, zoomLevel);

                                    if(upperTile == null)
                                    {
                                        g_scaled.setColor(this.noDataColor);
                                        g_scaled.fillRect(zx * this.tileDimensions.getWidth(),
                                                          zy * this.tileDimensions.getHeight(),
                                                          this.tileDimensions.getWidth(),
                                                          this.tileDimensions.getHeight());
                                    }
                                    else
                                    {
                                        g_scaled.drawImage(upperTile,
                                                           zx * this.tileDimensions.getWidth(),
                                                           zy * this.tileDimensions.getHeight(),
                                                           this.tileDimensions.getWidth(),
                                                           this.tileDimensions.getHeight(),
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
                                          this.tileDimensions.getWidth(),
                                          this.tileDimensions.getHeight(),
                                          null);
                    }

                    try
                    {
                        this.tileStoreWriter.addTile(this.tileToCrsCoordinate(tileY, tileX, imageCrsBounds, zoomLevel),
                                                     zoomLevel,
                                                     tileImage);
                    }
                    catch(final TileStoreException ex)
                    {
                        throw new TilingException("Unable to add tile", ex);
                    }
                }
            }
        }

        dataset.delete();
    }

    private static Dataset getDataset(final File file) throws TilingException
    {
        final Dataset dataset = gdal.Open(file.getAbsolutePath(),
                                          gdalconstConstants.GA_ReadOnly);

        if(dataset == null)
        {
            throw new TilingException(String.format("GDALOpen failed: %s", GdalError.lastError()));
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
            throw new TilingException(String.format("Failed to create a reprojected data set: AutoCreateWarpedVRT returned null:  %s",
                                                    GdalError.lastError()));
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

    private int getMaxZoom(final BoundingBox world, final Dimensions<Double> pixelDimensions)
    {
        final int xMaxZoom = getMaxZoom(world.getWidth(),  this.tileDimensions.getWidth(),  pixelDimensions.getWidth(),  (zoomLevel -> TileJob.this.tileStoreWriter.getTileScheme().dimensions(zoomLevel).getWidth()));
        final int yMaxZoom = getMaxZoom(world.getHeight(), this.tileDimensions.getHeight(), pixelDimensions.getHeight(), (zoomLevel -> TileJob.this.tileStoreWriter.getTileScheme().dimensions(zoomLevel).getHeight()));

        if(yMaxZoom == xMaxZoom)
        {
            return yMaxZoom;
        }

        final double zoomXResolution = world.getWidth()  / (this.tileStoreWriter.getTileScheme().dimensions(xMaxZoom).getWidth()  * this.tileDimensions.getWidth());
        final double zoomYResolution = world.getHeight() / (this.tileStoreWriter.getTileScheme().dimensions(yMaxZoom).getHeight() * this.tileDimensions.getHeight());

        // crsDistance / (Math.pow(2, zoomFloor)   * TILESIZE) is the resolution for that zoom level
        return Math.abs(pixelDimensions.getHeight() - zoomYResolution) <
               Math.abs(pixelDimensions.getWidth()  - zoomXResolution) ? yMaxZoom
                                                                       : xMaxZoom;
    }

    /**
     * @param crsDistance
     *             Height or width of the world's bounding box in CRS units
     */
    private static int getMaxZoom(final double crsDistance, final int tileSize, final double pixelDistance, final IntFunction<Integer> tileCount)
    {
        // The resolution in CRS units per pixel of the reprojected source image
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
        final double fractionalZoom = log2(crsDistance / (pixelDistance * tileSize));

        // Now the 2 closest zoom levels will be the ceiling and floor of our
        // fractional zoom level. We'll calculate the resolution for each zoom
        // level, and the difference between the zoom level resolution and
        // our source image resolution. Whichever zoom level has the smallest
        // difference in resolution from our source resolution will become our
        // max zoom level.
        // if all resolutions are identical, default to zooming larger

        final int zoomCeiling = (int)Math.ceil (fractionalZoom);
        final int zoomFloor   = (int)Math.floor(fractionalZoom);

        final double zoomCeilingResolution = crsDistance / (tileCount.apply(zoomCeiling) * tileSize);
        final double zoomFloorResolution   = crsDistance / (tileCount.apply(zoomFloor)   * tileSize);

        // crsDistance / (Math.pow(2, zoomFloor)   * TILESIZE) is the resolution for that zoom level
        return Math.abs(pixelDistance - zoomFloorResolution) <
               Math.abs(pixelDistance - zoomCeilingResolution) ? zoomFloor
                                                               : zoomCeiling;
    }

    private int getMinZoom(final GeoTransformation   geoTransformation,
                           final Dimensions<Integer> rasterDimensions,
                           final BoundingBox         imageCrsBounds)
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
            final TileMatrixDimensions tileMatrixDimensions = this.tileStoreWriter.getTileScheme().dimensions(minZoom);

            final Coordinate<Integer> minZoomUpperLeftTileCoordinate  = this.crsToTileCoordinate(imageCrsBounds.getTopLeft(),     this.crsProfile.getBounds(), tileMatrixDimensions); // TODO should I be using world bounds here?
            final Coordinate<Integer> minZoomLowerRightTileCoordinate = this.crsToTileCoordinate(imageCrsBounds.getBottomRight(), this.crsProfile.getBounds(), tileMatrixDimensions); // TODO should I be using world bounds here?

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
//    private static int f(final int z, final int d)
//    {
//        return d > 0 ? z
//                     : z > 0 ? 0
//                             : 1;
//    }

    private static double log2(final double value)
    {
        final double log2 = Math.log(2);

        return Math.log10(value) / log2;
    }

    private Coordinate<Integer> crsToTileCoordinate(final Coordinate<Double> coordinate, final BoundingBox bounds, final TileMatrixDimensions matrix)
    {
        return this.crsProfile.crsToTileCoordinate(new CrsCoordinate(coordinate, this.crsProfile.getCoordinateReferenceSystem()),
                                                   bounds,
                                                   matrix,
                                                   this.tileStoreWriter.getTileOrigin());
    }

    private CrsCoordinate tileToCrsCoordinate(final int tileY, final int tileX, final BoundingBox bounds, final int zoomLevel)
    {
        return this.crsProfile.tileToCrsCoordinate(tileX,
                                                   tileY,
                                                   bounds,
                                                   this.tileStoreWriter.getTileScheme().dimensions(zoomLevel),
                                                   this.tileStoreWriter.getTileOrigin());
    }
}
