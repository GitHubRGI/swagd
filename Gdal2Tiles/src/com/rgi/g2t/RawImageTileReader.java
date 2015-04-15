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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import javax.naming.OperationNotSupportedException;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import utility.GdalUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.util.FileUtility;

/**
 * Not an actual {@link TileStoreReader} per se, but has some attributes that
 * make GUI building easier. Only {@link #getCoordinateReferenceSystem},
 * {@link #getZoomLevels}, {@link #getBounds}, and {@link #stream(int)} are
 * implemented.
 *
 * @author Steven D. Lander
 *
 */
public class RawImageTileReader implements TileStoreReader
{
    private static final TileOrigin tileOrigin = TileOrigin.LowerLeft;

    private final File                      rawImage;
    private final CoordinateReferenceSystem coordinateReferenceSystem;
    private final Dimensions<Integer>       tileSize;
    private final Dataset                   dataset;

    /**
     * Constructor
     *
     * @param rawImage A raster image
     * @param tileSize A {@link Dimensions} that describes what an individual tile looks like
     * @throws TileStoreException Thrown when GDAL could not get the correct {@link CoordinateReferenceSystem}
     *                               of the input raster OR if the raw image could not be loaded as a {@link Dataset}
     */
    public RawImageTileReader(final File rawImage, final Dimensions<Integer> tileSize) throws TileStoreException
    {
        this(rawImage,
             tileSize,
             null);
    }

    /**
     * Constructor
     * @param rawImage A raster image {@link File}
     * @param tileSize A {@link Dimensions} that describes what an individual tile looks like
     * @param coordinateReferenceSystem The {@link CoordinateReferenceSystem} the tiles should be output in
     *
     * @throws TileStoreException Thrown when GDAL could not get the correct {@link CoordinateReferenceSystem}
     *                               of the input raster OR if the raw image could not be loaded as a {@link Dataset}
     */
    public RawImageTileReader(final File rawImage, final Dimensions<Integer> tileSize, final CoordinateReferenceSystem coordinateReferenceSystem) throws TileStoreException
    {
        if(rawImage == null || !rawImage.canRead())
        {
            throw new IllegalArgumentException("Raw image may not be null, and must represent a valid file on disk.");
        }

        if(tileSize == null)
        {
            throw new IllegalArgumentException("Tile size may not be null.");
        }

        this.rawImage = rawImage;
        this.tileSize = tileSize;

        osr.UseExceptions();
        gdal.AllRegister(); // Register GDAL extensions

        final Dataset inputDataset = gdal.Open(this.rawImage.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        if(inputDataset == null)
        {
            this.close();
            throw new TileStoreException("Could not open the raw image as a dataset in GDAL.");
        }

        try
        {
            if(coordinateReferenceSystem == null)
            {
                this.dataset = inputDataset;
                this.coordinateReferenceSystem = GdalUtility.getCoordinateReferenceSystemFromSpatialReference(GdalUtility.getDatasetSpatialReference(this.dataset));

                if(this.coordinateReferenceSystem == null)
                {
                    throw new IllegalArgumentException("Image file is not in a recognized coordinate reference system");
                }
            }
            else
            {
                this.coordinateReferenceSystem = coordinateReferenceSystem;
                final SpatialReference inputSrs = GdalUtility.getDatasetSpatialReference(inputDataset);
                this.dataset = GdalUtility.warpDatasetToSrs(inputDataset, inputSrs, GdalUtility.getSpatialReferenceFromCrs(this.coordinateReferenceSystem));
            }
        }
        catch(final DataFormatException dfe)
        {
            this.close();
            throw new TileStoreException(dfe);
        }
    }

    @Override
    public void close()
    {
        if(this.dataset != null)
        {
            this.dataset.delete();
        }

        //gdal.GDALDestroyDriverManager();
    }

    @Override
    public BoundingBox getBounds() throws TileStoreException
    {
        // Input dataset should be in the SRS the user wants
        try
        {
            return GdalUtility.getBoundsForDataset(this.dataset);
        }
        catch(final DataFormatException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public long countTiles() throws TileStoreException {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public long getByteSize() throws TileStoreException {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public BufferedImage getTile(final int column, final int row, final int zoomLevel)
            throws TileStoreException {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel)
            throws TileStoreException {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        // Return the Set of zoom levels, with a LowerLeft origin
        return GdalUtility.getZoomLevels(this.dataset,
                                                   RawImageTileReader.tileOrigin,
                                                   this.tileSize);
    }

    @Override
    public Stream<TileHandle> stream() throws TileStoreException {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public Stream<TileHandle> stream(final int zoomLevel) throws TileStoreException {
        // Create a new tile scheme
        // zoomLevel should be the minZoom of the raw image (lowest integer zoom)
        // this zoom should only have one tile
        final ZoomTimesTwo tileScheme = new ZoomTimesTwo(zoomLevel, zoomLevel, 1, 1);
        final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(this.dataset);
        // Calculate the tile ranges for all the zoom levels (0-31)
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    this.getBounds(),
                                                                                                    null,   // TODO !!WARNING!! null is a placeholder
                                                                                                    crsProfile,
                                                                                                    TileOrigin.LowerLeft);
        // Pick out the zoom level range for this particular zoom
        final Range<Coordinate<Integer>> zoomInfo = tileRanges.get(zoomLevel);

        // Get the coordinate information
        final Coordinate<Integer> topLeftCoordinate = zoomInfo.getMinimum();
        final Coordinate<Integer> bottomRightCoordinate = zoomInfo.getMaximum();

        // Parse each coordinate into min/max tiles for X/Y
        final int zoomMinXTile = topLeftCoordinate.getX();
        final int zoomMaxXTile = bottomRightCoordinate.getX();
        final int zoomMinYTile = bottomRightCoordinate.getY();
        final int zoomMaxYTile = topLeftCoordinate.getY();

        // Create a tile handle list that we can append to
        final List<TileHandle> tileHandles = new ArrayList<>();

        for (int tileY = zoomMaxYTile; tileY >= zoomMinYTile; --tileY)
        {
            // Iterate through all Y's
            for (int tileX = zoomMinXTile; tileX <= zoomMaxXTile; ++tileX)
            {
                // Iterate through all X's
                // Create a raw image handle with the z/x/y
                final RawImageTileHandle rawImageTileHandle = new RawImageTileHandle(zoomLevel, tileX, tileY);
                // Add to the list
                tileHandles.add(rawImageTileHandle);
            }
        }
        // Return the entire tile handle list as a stream
        return tileHandles.stream();
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() throws TileStoreException
    {
        return this.coordinateReferenceSystem;
    }

    @Override
    public String getName() {
        return FileUtility.nameWithoutExtension(this.rawImage);
    }

    @Override
    public String getImageType() throws TileStoreException {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public Dimensions<Integer> getImageDimensions() throws TileStoreException {
        return this.tileSize;
    }

    @Override
    public TileScheme getTileScheme() throws TileStoreException {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    private class RawImageTileHandle implements TileHandle
    {
        private final int zoom;
        private final int column;
        private final int row;

        RawImageTileHandle(final int zoom, final int column, final int row)
        {
            this.zoom = zoom;
            this.column = column;
            this.row = row;
        }

        @Override
        public int getZoomLevel() {
            return this.zoom;
        }

        @Override
        public int getColumn() {
            return this.column;
        }

        @Override
        public int getRow() {
            return this.row;
        }

        @Override
        public TileMatrixDimensions getMatrix() throws TileStoreException {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public CrsCoordinate getCrsCoordinate() throws TileStoreException {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public CrsCoordinate getCrsCoordinate(final TileOrigin corner)
                throws TileStoreException {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public BoundingBox getBounds() throws TileStoreException {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public BufferedImage getImage() throws TileStoreException {
            throw new TileStoreException(new OperationNotSupportedException());
        }
    }

    @Override
    public TileOrigin getTileOrigin()
    {
        return tileOrigin;
    }
}
