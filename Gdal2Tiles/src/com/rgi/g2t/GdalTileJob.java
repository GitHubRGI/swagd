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
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import utility.GdalUtility;
import utility.GdalUtility.GdalRasterParameters;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.TaskMonitor;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * A {@link Runnable} job that tiles input raster data.
 *
 * @author Steven D. Lander
 */
public class GdalTileJob implements Runnable
{
    private final TileStoreWriter     writer;
    private final CrsProfile          crsProfile;
    private final File                file;
    private final Dimensions<Integer> tileDimensions;
    private final Color               noDataColor;
    private final TaskMonitor         monitor;

    /**
     * Constructor
     *
     * @param file
     *             The {@link File} object of the raster data to be tiled
     * @param writer
     *             The {@link TileStoreWriter} object that will be used to create tiles on-disk
     * @param tileDimensions
     *             The {@link Dimensions} of the tile grid requested
     * @param noDataColor
     *             The {@link Color} of the NODATA fields within the raster image
     * @param monitor
     *             The {@link TaskMonitor} of the running job
     */
    public GdalTileJob(final File                file,
                       final TileStoreWriter     writer,
                       final Dimensions<Integer> tileDimensions,
                       final Color               noDataColor,
                       final TaskMonitor         monitor)
    {
        if (file == null)
        {
            throw new IllegalArgumentException("Raster file location cannot be null");
        }
        if (writer == null)
        {
            throw new IllegalArgumentException("Tile store writer cannot be null.");
        }
        if (tileDimensions == null)
        {
            throw new IllegalArgumentException("Tile matrix dimensions cannot be null.");
        }

        // TODO: Implement alpha support for no data color.

        if (monitor == null)
        {
            throw new IllegalArgumentException("Monitor cannot be null.");
        }
        this.file           = file;
        this.writer         = writer;
        this.tileDimensions = tileDimensions;
        this.monitor        = monitor;
        this.noDataColor    = noDataColor;
        this.crsProfile     = CrsProfileFactory.create(writer.getCoordinateReferenceSystem());
    }

    @Override
    public void run()
    {
        try
        {
            final Dataset inputDataset = this.openInput();
            final Dataset outputDataset = this.openOutput(inputDataset);

            final BoundingBox outputBounds = GdalUtility.getBoundsForDataset(outputDataset);

            // Calculate all the tiles in every zoom possible (0-31)
            final Map<Integer, Range<Coordinate<Integer>>> ranges = GdalUtility.calculateTileRanges(this.writer.getTileScheme(),
                                                                                                    outputBounds,
                                                                                                    this.crsProfile,
                                                                                                    this.writer.getTileOrigin());
            // Generate tiles
//            final int minZoom = GdalUtility.minimalZoomForDataset(outputDataset,
//                                                                  ranges,
//                                                                  this.writer.getTileOrigin(),
//                                                                  this.writer.getTileScheme(),
//                                                                  this.tileDimensions);
//
//            final int maxZoom = GdalUtility.maximalZoomForDataset(outputDataset,
//                                                                  ranges,
//                                                                  this.writer.getTileOrigin(),
//                                                                  this.writer.getTileScheme(),
//                                                                  this.tileDimensions);
//
//            final Range<Integer> range = new Range<>(this.writer.getTileScheme()
//                                                                .getZoomLevels()
//                                                                .stream(),
//                                                     Integer::compare);

            // Set the total tile count in monitor
            final int maxTiles = ranges.entrySet()
                                       .stream()
                                       //.filter(entrySet -> entrySet.getKey() >= minZoom && entrySet.getKey() <= maxZoom)    // Select the subset of zooms that will actually be tiled, so we can generate the total tile count from it
                                       .map(entrySet -> entrySet.getValue())
                                       .mapToInt(range -> { // range.getMinimum is the TopLeft corner of the bounding box and
                                                            // range.getMaximum is the BottomRight corner of the bounding box
                                                            final int totalX = range.getMaximum().getX() - range.getMinimum().getX() + 1;
                                                            final int totalY = range.getMinimum().getY() - range.getMaximum().getY() + 1;
                                                            return totalX * totalY;
                                                          })
                                       .sum();

            this.monitor.setMaximum(maxTiles);

            int tilesComplete = 0;

            // Tile all levels
            for(final Entry<Integer, Range<Coordinate<Integer>>> entry : ranges.entrySet()
                                                                               .stream()
                                                                               .sorted((a, b) -> (Integer.compare(a.getKey(), b.getKey()) * -1)) // sort max to min
                                                                               .collect(Collectors.toList()))
            {
                final int zoom = entry.getKey();
                final Range<Coordinate<Integer>> range = entry.getValue();

                tilesComplete = this.generateTiles(outputDataset, range, zoom, tilesComplete);
            }

            // Clean up the opened datasets
            inputDataset.delete();
            outputDataset.delete();
        }
        catch(final TilingException | DataFormatException/* | TileStoreException*/ ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Open a raster file for tiling.
     *
     * @return A {@link Dataset} representing the raster image on disk
     * @throws TilingException Thrown when the {@link Dataset} has no Georeference
     */
    private Dataset openInput() throws TilingException
    {
        osr.UseExceptions();
        // Register GDAL for use
        gdal.AllRegister();
        // TODO: Check memory driver in case GDAL is configured incorrectly?
        final Dataset dataset = gdal.Open(this.file.toPath().toString(), gdalconstConstants.GA_ReadOnly);
        // TODO: What happens if it cannot open this?
        if (dataset.GetRasterBand(1).GetColorTable() != null)
        {
            // TODO: make a temporary vrt with gdal_translate to expand this to RGB/RGBA
            System.out.println("expand this raster to RGB/RGBA");
        }
        if (dataset.GetRasterCount() == 0)
        {
            throw new TilingException("Input file has no raster band.");
        }
        // We cannot tile an image with no geo referencing information
        if (!GdalUtility.datasetHasGeoReference(dataset))
        {
            throw new TilingException("Input raster image has no georeference.");
        }

        return dataset;
    }

    /**
     * Open a {@link Dataset} warped to correct for differing {@link SpatialReference} systems.
     *
     * @param inputDataset A {@link Dataset} representing the input raster
     * @return A {@link Dataset} warped to the correct {@link SpatialReference}
     * @throws TilingException Thrown when an error occurs with either getting the
     *                            {@link SpatialReference} from the input raster OR when warping
     *                            the input dataset fails
     */
    private Dataset openOutput(final Dataset inputDataset) throws TilingException
    {
        try
        {
            final SpatialReference inputSrs = GdalUtility.getDatasetSpatialReference(inputDataset);
            final SpatialReference outputSrs = GdalUtility.getSpatialReferenceFromCrs(this.crsProfile.getCoordinateReferenceSystem());

            // If input srs and output srs are not the same, reproject by making a VRT
            if (!inputSrs.ExportToProj4().equals(outputSrs.ExportToProj4()) || inputDataset.GetGCPCount() == 0)
            {
                // Create a warped VRT
                //outputDataset = GdalUtility.warpDatasetToSrs(inputDataset, inputSrs, outputSrs);
                return GdalUtility.warpDatasetToSrs(inputDataset, inputSrs, outputSrs);
            }

            // The input and output projections are the same, no reprojection needed
            return inputDataset;
        }
        catch (final DataFormatException tse)
        {
            throw new TilingException(tse);
        }
    }

    /**
     * Generate tiles for all zoom levels requested.
     *
     * @param dataset A {@link Dataset} to generate tiles from
     * @param zoomRange A {@link Range} of zoom level X/Y coordinates to generate
     *                     tiles from
     * @param zoom The specific integer zoom level to tile
     * @param tilesComplete The amount of tiles created to-date
     * @return The current amount of tiles created to-date
     * @throws TilingException Thrown when the {@link TileStoreWriter} fails to
     *                            add a tile
     */
    private int generateTiles(final Dataset dataset,
                              final Range<Coordinate<Integer>> zoomRange,
                              final int zoom,
                              final int tilesComplete) throws TilingException
    {
        // Set the tile progress accumulator
        int tileProgress = tilesComplete;

        // Create a tile folder name
        final Coordinate<Integer> topLeftCoordinate = zoomRange.getMinimum();
        final Coordinate<Integer> bottomRightCoordinate = zoomRange.getMaximum();

        // Set x/y min/max values
        final int tileMinX = topLeftCoordinate.getX();
        final int tileMaxX = bottomRightCoordinate.getX();
        final int tileMinY = bottomRightCoordinate.getY();
        final int tileMaxY = topLeftCoordinate.getY();

        // Set the dimensions of this zoom
        final TileMatrixDimensions zoomDimensions = this.writer.getTileScheme().dimensions(zoom);

        // create a for loop for tile y, counting down
        for (int tileY = tileMaxY; tileY >= tileMinY; tileY--)
        {
            // Create a for loop for tile x, counting up
            // This makes queries start at top-left of the data
            for (int tileX = tileMinX; tileX <= tileMaxX; tileX++)
            {
                // Create coordinates for the bounding box corners
                final CrsCoordinate tileTopLeftCorner     = this.crsProfile.tileToCrsCoordinate(tileX,     tileY + 1, this.crsProfile.getBounds(), zoomDimensions, TileOrigin.LowerLeft);
                final CrsCoordinate tileBottomRightCorner = this.crsProfile.tileToCrsCoordinate(tileX + 1, tileY,     this.crsProfile.getBounds(), zoomDimensions, TileOrigin.LowerLeft);

                // Create a bounding box from the coordinates above
                final BoundingBox tileBBox = new BoundingBox(tileTopLeftCorner.getX(), tileBottomRightCorner.getY(), tileBottomRightCorner.getX(), tileTopLeftCorner.getY());

                // Build the parameters for GDAL read raster call
                final GdalRasterParameters params = GdalUtility.getGdalRasterParameters(dataset.GetGeoTransform(), tileBBox, this.tileDimensions, dataset);

                // Read image data directly from the raster
                final ByteBuffer imageData = GdalUtility.readRasterDirect(params, dataset);

                // TODO: logic goes here in the case that the querysize == tile size (gdalconstConstants.GRA_NearestNeighbour) (write directly)
                // Time to start writing the tile
                final Dataset querySizeImageCanvas = GdalUtility.writeRasterDirect(params, imageData, dataset.GetRasterCount());

                // Scale each band of tileDataInMemory down to the tile size (down from the query size)
                final Dataset tileDataInMemory = GdalUtility.scaleQueryToTileSize(querySizeImageCanvas, this.tileDimensions);
                try
                {
                    // Write the tile to the tile store
                    this.writer.addTile(tileX, tileY, zoom, GdalUtility.convert(tileDataInMemory));
                    // Iterate the tile progress bar
                    this.monitor.setProgress(++tileProgress);
                }
                catch (final TileStoreException tse)
                {
                    throw new TilingException(tse);
                }
                finally
                {
                    if (querySizeImageCanvas != null)
                    {
                        querySizeImageCanvas.delete();
                    }
                    if (tileDataInMemory != null)
                    {
                        tileDataInMemory.delete();
                    }
                }
            }
        }
        // Return the total tiles produced for this zoom
        return tileProgress;
    }
}