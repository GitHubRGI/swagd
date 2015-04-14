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

public class RawImageTileReader2 implements TileStoreReader
{
    private static final TileOrigin         tileOrigin = TileOrigin.LowerLeft;

    private final File                      rawImage;
    private final CoordinateReferenceSystem coordinateReferenceSystem;
    private final Dimensions<Integer>       tileSize;
    private final Dataset                   dataset;
    private final BoundingBox               bounds;
    private final Set<Integer>              zoomLevels;

    static
    {
        osr.UseExceptions();
        gdal.AllRegister(); // Register GDAL extensions
    }

    /**
     * Constructor
     *
     * @param rawImage
     *            A raster image
     * @param tileSize
     *            A {@link Dimensions} that describes what an individual tile
     *            looks like
     * @throws TileStoreException
     *             Thrown when GDAL could not get the correct
     *             {@link CoordinateReferenceSystem} of the input raster OR if
     *             the raw image could not be loaded as a {@link Dataset}
     */
    public RawImageTileReader2(final File rawImage, final Dimensions<Integer> tileSize) throws TileStoreException
    {
        this(rawImage, tileSize, null);
    }

    /**
     * Constructor
     *
     * @param rawImage
     *            A raster image {@link File}
     * @param tileSize
     *            A {@link Dimensions} that describes what an individual tile
     *            looks like
     * @param coordinateReferenceSystem
     *            The {@link CoordinateReferenceSystem} the tiles should be
     *            output in
     *
     * @throws TileStoreException
     *             Thrown when GDAL could not get the correct
     *             {@link CoordinateReferenceSystem} of the input raster OR if
     *             the raw image could not be loaded as a {@link Dataset}
     */
    public RawImageTileReader2(final File rawImage, final Dimensions<Integer> tileSize, final CoordinateReferenceSystem coordinateReferenceSystem) throws TileStoreException
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

            this.bounds = GdalUtility.getBoundsForDataset(this.dataset);

            GdalUtility.getZoomLevelsForDataset(this.dataset, RawImageTileReader2.tileOrigin, this.tileSize);
        }
        catch(final DataFormatException dfe)
        {
            this.close();
            throw new TileStoreException(dfe);
        }
        finally
        {
            inputDataset.delete();
        }

        this.zoomLevels = GdalUtility.getZoomLevelsForDataset(this.dataset, RawImageTileReader2.tileOrigin, this.tileSize);
    }

    @Override
    public void close()
    {
        if(this.dataset != null)
        {
            this.dataset.delete();
        }
    }

    @Override
    public BoundingBox getBounds() throws TileStoreException
    {
        return this.bounds;
    }

    @Override
    public long countTiles() throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public long getByteSize() throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public BufferedImage getTile(final int column, final int row, final int zoomLevel) throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        return this.zoomLevels;
    }

    @Override
    public Stream<TileHandle> stream() throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public Stream<TileHandle> stream(final int zoomLevel) throws TileStoreException
    {
        // Create a new tile scheme
        // zoomLevel should be the minZoom of the raw image (lowest integer zoom)
        // this zoom should only have one tile
        final ZoomTimesTwo tileScheme = new ZoomTimesTwo(zoomLevel, zoomLevel, 1, 1);
        final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(this.dataset);
        // Calculate the tile ranges for all the zoom levels (0-31)
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme, this.getBounds(), crsProfile, TileOrigin.LowerLeft);
        // Pick out the zoom level range for this particular zoom
        final Range<Coordinate<Integer>> zoomInfo = tileRanges.get(zoomLevel);

        // Get the coordinate information
        final Coordinate<Integer> topLeftCoordinate     = zoomInfo.getMinimum();
        final Coordinate<Integer> bottomRightCoordinate = zoomInfo.getMaximum();

        // Parse each coordinate into min/max tiles for X/Y
        final int zoomMinXTile = topLeftCoordinate.getX();
        final int zoomMaxXTile = bottomRightCoordinate.getX();
        final int zoomMinYTile = bottomRightCoordinate.getY();
        final int zoomMaxYTile = topLeftCoordinate.getY();

        // Create a tile handle list that we can append to
        final List<TileHandle> tileHandles = new ArrayList<>();

        for(int tileY = zoomMaxYTile; tileY >= zoomMinYTile; --tileY)
        {
            // Iterate through all Y's
            for(int tileX = zoomMinXTile; tileX <= zoomMaxXTile; ++tileX)
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
    public String getName()
    {
        return FileUtility.nameWithoutExtension(this.rawImage);
    }

    @Override
    public String getImageType() throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    @Override
    public Dimensions<Integer> getImageDimensions() throws TileStoreException
    {
        return this.tileSize;
    }

    @Override
    public TileScheme getTileScheme() throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException());
    }

    private class RawImageTileHandle implements TileHandle
    {
        private final int zoom;
        private final int column;
        private final int row;

        RawImageTileHandle(final int zoom, final int column, final int row)
        {
            this.zoom   = zoom;
            this.column = column;
            this.row    = row;
        }

        @Override
        public int getZoomLevel()
        {
            return this.zoom;
        }

        @Override
        public int getColumn()
        {
            return this.column;
        }

        @Override
        public int getRow()
        {
            return this.row;
        }

        @Override
        public TileMatrixDimensions getMatrix() throws TileStoreException
        {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public CrsCoordinate getCrsCoordinate() throws TileStoreException
        {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public CrsCoordinate getCrsCoordinate(final TileOrigin corner) throws TileStoreException
        {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public BoundingBox getBounds() throws TileStoreException
        {
            throw new TileStoreException(new OperationNotSupportedException());
        }

        @Override
        public BufferedImage getImage() throws TileStoreException
        {
            throw new TileStoreException(new OperationNotSupportedException());
        }
    }

    @Override
    public TileOrigin getTileOrigin()
    {
        return tileOrigin;
    }
}
