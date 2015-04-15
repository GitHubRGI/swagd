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
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
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

    private final File                                     rawImage;
    private final CoordinateReferenceSystem                coordinateReferenceSystem;
    private final Dimensions<Integer>                      tileSize;
    private final Dataset                                  dataset;
    private final BoundingBox                              bounds;
    private final Set<Integer>                             zoomLevels;
    private final ZoomTimesTwo                             tileScheme;
    private final CrsProfile                               profile;
    private final int                                      tileCount;
    private final Map<Integer, Range<Coordinate<Integer>>> tileRanges;

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
                this.dataset                   = inputDataset;
                this.coordinateReferenceSystem = GdalUtility.getCoordinateReferenceSystemFromSpatialReference(GdalUtility.getDatasetSpatialReference(this.dataset));

                if(this.coordinateReferenceSystem == null)
                {
                    throw new IllegalArgumentException("Image file is not in a recognized coordinate reference system");
                }
            }
            else
            {
                final SpatialReference inputSrs = GdalUtility.getDatasetSpatialReference(inputDataset);

                this.coordinateReferenceSystem = coordinateReferenceSystem;
                this.dataset                   = GdalUtility.warpDatasetToSrs(inputDataset, inputSrs, GdalUtility.getSpatialReferenceFromCrs(this.coordinateReferenceSystem));
            }

            this.bounds  = GdalUtility.getBoundsForDataset(this.dataset);
            this.profile = CrsProfileFactory.create(this.coordinateReferenceSystem);
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

        this.zoomLevels = GdalUtility.getZoomLevels(this.dataset, RawImageTileReader2.tileOrigin, this.tileSize);

        final Range<Integer> tileRange = new Range<>(this.zoomLevels, Integer::compare);

        this.tileScheme = new ZoomTimesTwo(tileRange.getMinimum(),
                                           tileRange.getMaximum(),
                                           1,
                                           1);

        this.tileRanges = GdalUtility.calculateTileRanges(this.tileScheme,
                                                          this.bounds,
                                                          need final bounds of tile set
                                                          this.profile,
                                                          RawImageTileReader2.tileOrigin);

        this.tileCount = ranges.entrySet()
                               .stream()
                               .map(entrySet -> entrySet.getValue())
                               .mapToInt(range -> { return (range.getMaximum().getX() - range.getMinimum().getX() + 1) *
                                                           (range.getMinimum().getY() - range.getMaximum().getY() + 1);
                                                  })
                               .sum();
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
        return this.tileCount;
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
        final Range<Coordinate<Integer>> zoomInfo = this.tileRanges.get(zoomLevel);

        // Get the coordinate information
        final Coordinate<Integer> topLeftCoordinate     = zoomInfo.getMinimum();
        final Coordinate<Integer> bottomRightCoordinate = zoomInfo.getMaximum();

        // Parse each coordinate into min/max tiles for X/Y
        final int zoomMinXTile =     topLeftCoordinate.getX();
        final int zoomMaxXTile = bottomRightCoordinate.getX();
        final int zoomMinYTile = bottomRightCoordinate.getY();
        final int zoomMaxYTile =     topLeftCoordinate.getY();

        // Create a tile handle list that we can append to
        final List<TileHandle> tileHandles = new ArrayList<>();

        for(int tileY = zoomMaxYTile; tileY >= zoomMinYTile; --tileY)
        {
            for(int tileX = zoomMinXTile; tileX <= zoomMaxXTile; ++tileX)
            {
                tileHandles.add(new RawImageTileHandle(zoomLevel, tileX, tileY));
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
        return null;
    }

    @Override
    public Dimensions<Integer> getImageDimensions() throws TileStoreException
    {
        return this.tileSize;
    }

    @Override
    public TileScheme getTileScheme() throws TileStoreException
    {
        return this.tileScheme;
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
