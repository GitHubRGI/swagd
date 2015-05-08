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

package com.rgi.store.tiles.tilecluster;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.util.ImageUtility;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;

/**
 * @author Luke Lambert
 *
 * TODO This implementation is incomplete, and not ready for use
 *
 * TODO give attribution for this code "Tile-Based Geospatial Information Systems" by John T. Sample and Elias Ioup, Chapter 8 and specifically Listing 8.3 "Tile Clusters implementation" i.e. ClusteredTileStream
 *
 */
@SuppressWarnings("javadoc")
public class TileClusterReader extends TileCluster implements TileStoreReader
{
    /**
     * Constructor
     *
     * @param location
     * @param setName
     * @param levels
     * @param breakPoint
     * @param crsProfile
     */
    public TileClusterReader(final Path       location,
                             final String     setName,
                             final int        levels,
                             final int        breakPoint,
                             final CrsProfile crsProfile)
    {
        super(location, setName, levels, breakPoint, crsProfile);

        if(!location.toFile().canRead())
        {
            throw new IllegalArgumentException("Specified location cannot be read from");
        }
    }

    @Override
    public long getByteSize()
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public long countTiles() throws TileStoreException
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public BufferedImage getTile(final int column, final int row, final int zoomLevel) throws TileStoreException
    {
        final ClusterAddress clusterAddress = this.getClusterAddress(column, row, zoomLevel);
        final File           clusterFile    = this.getClusterFile(clusterAddress);

        if(!clusterFile.canRead())
        {
            return null;
        }

        try(final RandomAccessFile randomAccessFile = new RandomAccessFile(clusterFile, "r"))
        {
            final long indexPosition = this.getIndexPosition(row, column, zoomLevel);

            randomAccessFile.seek(indexPosition);

            final long tilePosition = randomAccessFile.readLong();

            if(tilePosition == TileCluster.NoDataLong)
            {
                return null;
            }

            final int    tileSize  = randomAccessFile.readInt();
            final byte[] imageData = new byte[tileSize];

            final long tilePositionOffset = tilePosition + TileHeaderByteSize;

            randomAccessFile.seek(tilePositionOffset);
            randomAccessFile.readFully(imageData);

            return ImageUtility.bytesToBufferedImage(imageData);
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.crsProfile.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        // First determine the cluster that will hold the data
        final Coordinate<Integer> clusterCoordinate = this.crsProfile.crsToTileCoordinate(coordinate,
                                                                                          this.getBounds(),
                                                                                          this.tileScheme.dimensions(zoomLevel),
                                                                                          TileCluster.Origin);

        return this.getTile(clusterCoordinate.getX(),
                            clusterCoordinate.getY(),
                            zoomLevel);
    }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Stream<TileHandle> stream()
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Stream<TileHandle> stream(final int zoomLevel)
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getImageType()
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Dimensions<Integer> getImageDimensions()
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public TileScheme getTileScheme()
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static final int MagicNumberByteSize = 8; // size of long
    private static final int ColumnByteSize      = 8; // size of long
    private static final int RowByteSize         = 8; // size of long
    private static final int LengthByteSize      = 4; // size of long

    private static final int TileHeaderByteSize = MagicNumberByteSize +
                                                  MagicNumberByteSize + // The magic number is written twice
                                                  ColumnByteSize      +
                                                  RowByteSize         +
                                                  LengthByteSize;
}
