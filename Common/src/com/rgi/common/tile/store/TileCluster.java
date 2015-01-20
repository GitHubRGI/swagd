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

package com.rgi.common.tile.store;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Set;

import javax.imageio.ImageIO;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.scheme.TileScheme;

/**
 * @author Luke Lambert
 *
 * TODO This implementation is incomplete, and not ready for use
 *
 * TODO give attribution for this code "Tile-Based Geospatial Information Systems" by John T. Sample and Elias Ioup, Chapter 8 and specifically Listing 8.3 "Tile Clusters implementation" e.i. ClusteredTileStream
 *
 */
public class TileCluster implements TileStore
{
    public TileCluster(final Path location, final String setName, final int levels, final int breakPoint, final TileProfile tileProfile)
    {
        if(location == null)
        {
            throw new IllegalArgumentException("Location may not be null");
        }

        if(setName == null || setName.isEmpty())    // TODO How can we check if location + setName is valid?
        {
            throw new IllegalArgumentException("Set name may not be null or empty");
        }

        if(levels <= 0)
        {
            throw new IllegalArgumentException("Levels must be greater than 0");
        }

        if(levels <= breakPoint)
        {
            throw new IllegalArgumentException("Break point must be less than zoomLevels");
        }

        this.location    = location;
        this.setName     = setName;
        this.zoomLevels  = levels;
        this.breakPoint  = breakPoint;
        this.tileProfile = tileProfile;
        this.tileScheme  = null;    // TODO
    }

    @Override
    public BoundingBox calculateBounds()
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public long calculateSize()
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
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        // First determine the cluster that will hold the data
        final AbsoluteTileCoordinate clusterCoordinate = this.tileProfile.crsToAbsoluteTileCoordinate(coordinate,
                                                                                                      zoomLevel,
                                                                                                      TileCluster.Origin);

        final ClusterAddress clusterAddress = this.getClusterAddress(clusterCoordinate);
        final File           clusterFile    = this.getClusterFile(clusterAddress);

        if(!clusterFile.canRead())
        {
            return null;
        }

        try(final RandomAccessFile randomAccessFile = new RandomAccessFile(clusterFile, "r"))
        {
            final long indexPosition = this.getIndexPosition(clusterCoordinate);

            randomAccessFile.seek(indexPosition);

            final long tilePosition = randomAccessFile.readLong();

            if(tilePosition == TileCluster.NoDataLong)
            {
                return null;
            }

            final int    tileSize  = randomAccessFile.readInt();
            final byte[] imageData = new byte[tileSize];

            final long tilePositionOffset = tilePosition + TileCluster.TileHeaderByteSize;

            randomAccessFile.seek(tilePositionOffset);
            randomAccessFile.readFully(imageData);

            try(ByteArrayInputStream imageInputStream = new ByteArrayInputStream(imageData))
            {
                return ImageIO.read(imageInputStream);
            }
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(image == null)
        {
            throw new IllegalArgumentException("Image may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile profile's coordinate reference system");
        }

        final AbsoluteTileCoordinate clusterCoordinate = this.tileProfile.crsToAbsoluteTileCoordinate(coordinate,
                                                                                                      zoomLevel,
                                                                                                      TileCluster.Origin);
        final ClusterAddress clusterAddress = this.getClusterAddress(clusterCoordinate);
        final File           clusterFile    = this.getClusterFile(clusterAddress);

        // If the file doesn't exist, set up an empty cluster file
        if(!clusterFile.exists())
        {
            try
            {
                TileCluster.createNewClusterFile(clusterFile, clusterAddress.endlevel - clusterAddress.startlevel + 1);
            }
            catch(final IOException ex)
            {
                throw new TileStoreException(ex);
            }
        }

        final String outputFormat = "PNG";  // TODO how do we want to pick this ?

        try(final RandomAccessFile      randomAccessFile  = new RandomAccessFile(clusterFile, "rw");
            @SuppressWarnings("resource")
            final ByteArrayOutputStream outputStream      = new ByteArrayOutputStream())
        {
            if(!ImageIO.write(image, outputFormat, outputStream))
            {
                throw new TileStoreException(String.format("No appropriate image writer found for format '%s'", outputFormat));
            }

            final byte[] imageData = outputStream.toByteArray();

            // Write the data at the end of the tile file
            final long tilePosition = randomAccessFile.length();

            randomAccessFile.seek(tilePosition);

            randomAccessFile.writeLong(TileCluster.MagicNumber);
            randomAccessFile.writeLong(TileCluster.MagicNumber);
            randomAccessFile.writeLong(clusterCoordinate.getColumn());
            randomAccessFile.writeLong(clusterCoordinate.getRow());
            randomAccessFile.writeInt (imageData.length);
            randomAccessFile.write    (imageData);

            final long indexPosition = this.getIndexPosition(clusterCoordinate);

            randomAccessFile.seek(indexPosition);

            // write the tile position and size in the index
            randomAccessFile.writeLong(tilePosition);
            randomAccessFile.writeInt(imageData.length);
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.tileProfile.getCoordinateReferenceSystem();
    }

    @Override
    public TileScheme getTileScheme()
    {
        return this.tileScheme;
    }

    private ClusterAddress getClusterAddress(final AbsoluteTileCoordinate coordinate)
    {
        if(coordinate.getZoomLevel() <= 0 || coordinate.getZoomLevel() > this.zoomLevels)
        {
            throw new IllegalArgumentException(String.format("Level must be between [0, %d]",
                                                             this.zoomLevels));
        }

        int targetZoomLevel = 0;
        int    endZoomLevel = 0;

        if(coordinate.getZoomLevel() < this.breakPoint)
        {
            // Tile goes in one of top two clusters
            targetZoomLevel = 1;
               endZoomLevel = this.breakPoint - 1;
        }
        else
        {
            // Tile goes in bottom cluster
            targetZoomLevel = this.breakPoint;
               endZoomLevel = this.zoomLevels;
        }

        final int    zoomLevelDifference = coordinate.getZoomLevel() - targetZoomLevel; // Compute the difference between the target cluster zoom level and the tile zoom level
        final double levelFactor         = Math.pow(2, zoomLevelDifference);            // Level factor is the number of tiles at zoom level "coordinate.getZoomLevel()" for a cluster that starts at "target zoom level"

        // Divide the row and column by the coordinate.getZoomLevel() factor to get the row and column address of the cluster we are using
        final long clusterRow    = (long)Math.floor(coordinate.getRow()    / levelFactor);
        final long clusterColumn = (long)Math.floor(coordinate.getColumn() / levelFactor);

        return new ClusterAddress(clusterRow,
                                  clusterColumn,
                                  targetZoomLevel,
                                  endZoomLevel);
    }

    private File getClusterFile(final ClusterAddress clusterAddress)
    {
        return new File(String.format("%s/%s-%d-%d-%d.cluster",
                                      this.location,
                                      this.setName,
                                      clusterAddress.startlevel,
                                      clusterAddress.row,
                                      clusterAddress.column));
    }

    /**
     * Create an empty file and fills the index with NoDataLong values
     *
     * @param file
     * @param numberOfzoomLevels
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void createNewClusterFile(final File file, final int numberOfzoomLevels) throws FileNotFoundException, IOException
    {
        try(final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"))
        {
            randomAccessFile.seek(0);

            final long tiles = TileCluster.getCumulativeTileCount(numberOfzoomLevels);

            for(long i = 0; i < tiles; ++i)
            {
                randomAccessFile.writeLong(TileCluster.NoDataLong);  // NoData position of tile
                randomAccessFile.writeInt (TileCluster.NoDataInt);   // NoData size of tile
            }
        }
    }

    // TODO convert this into a normal equation ::math::
    private static int getCumulativeTileCount(final int finalZoomLevel)
    {
        int count = 0;

        for(int i = 1; i <= finalZoomLevel; ++i)
        {
            count += (int)(Math.pow(2, 2 * i - 2));
        }

        return count;
    }

    private long getIndexPosition(final AbsoluteTileCoordinate coordinate)
    {
        final ClusterAddress clusterAddress = this.getClusterAddress(coordinate); // compute the local address that's the relative address of the tile in the cluster

        final int localZoomLevel = coordinate.getZoomLevel() - clusterAddress.startlevel;

        final long localRow    = (long)(coordinate.getRow()    - (Math.pow(2 , localZoomLevel) * clusterAddress.row));
        final long localColumn = (long)(coordinate.getColumn() - (Math.pow(2 , localZoomLevel) * clusterAddress.column));

        final int columnsAtLocallevel = (int)Math.pow(2 , localZoomLevel);

        final long indexPosition = TileCluster.getCumulativeTileCount(localZoomLevel - 1) + localRow * columnsAtLocallevel + localColumn;

        return indexPosition * IndexTileAddressByteSize; // multiply index position times byte size of a tile address
    }

    private static final int IndexLocationByteSize    = 8;  // size of long
    private static final int IndexSizeByteSize        = 4;  // size of int

    private static final int IndexTileAddressByteSize = IndexLocationByteSize + IndexSizeByteSize;

    private static final TileOrigin Origin      = TileOrigin.LowerLeft; // TODO WARNING WARNING, THIS IS JUST A BLIND GUESS
    private static final long       MagicNumber = 0x772211ee;
    private static final long       NoDataLong  = -1L;
    private static final int        NoDataInt   = -1;

    private static final int MagicNumberByteSize = 8; // size of long
    private static final int ColumnByteSize      = 8; // size of long
    private static final int RowByteSize         = 8; // size of long
    private static final int LengthByteSize      = 4; // size of long

    private static final int TileHeaderByteSize = TileCluster.MagicNumberByteSize +
                                                  TileCluster.MagicNumberByteSize + // The magic number is written twice
                                                  TileCluster.ColumnByteSize      +
                                                  TileCluster.RowByteSize         +
                                                  TileCluster.LengthByteSize;

    private final Path        location;
    private final String      setName;
    private final int         zoomLevels;
    private final int         breakPoint;
    private final TileProfile tileProfile;
    private final TileScheme  tileScheme;

    private class ClusterAddress
    {
        final long row;
        final long column;
        final int  startlevel;
        final int  endlevel;

        public ClusterAddress(final long row, final long column, final int startlevel, final int endlevel)
        {
            this.row        = row;
            this.column     = column;
            this.startlevel = startlevel;
            this.endlevel   = endlevel;
        }
    }
}
