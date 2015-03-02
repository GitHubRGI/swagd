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

package com.rgi.common.tile.store.tilecluster;

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
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.util.ImageUtility;

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
