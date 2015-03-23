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

package com.rgi.common.tile.store.tilecluster;

import java.io.File;
import java.nio.file.Path;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;

/**
 * Abstract base class for a tile store reader and writer of the "Tile Cluster"
 * method of storing tiles
 *
 * @author Luke Lambert
 *
 * TODO This implementation is incomplete, and not ready for use
 *
 * TODO give attribution for this code "Tile-Based Geospatial Information Systems" by John T. Sample and Elias Ioup, Chapter 8 and specifically Listing 8.3 "Tile Clusters implementation" i.e. ClusteredTileStream
 *
 */
@SuppressWarnings("javadoc")
abstract class TileCluster
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
    public TileCluster(final Path       location,
                       final String     setName,
                       final int        levels,
                       final int        breakPoint,
                       final CrsProfile crsProfile)
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

        this.location   = location;
        this.setName    = setName;
        this.zoomLevels = levels;
        this.breakPoint = breakPoint;
        this.crsProfile = crsProfile;

        this.tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
    }

    public void close()
    {
        // Nothing to do here.  This method exists for child classes that need to implement AutoClosable
    }

    @SuppressWarnings("static-method")  // Needs to be used as an override in child classes that implement TileStoreReader/TileStoreWriter
    public BoundingBox getBounds()
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    public String getName()
    {
        return String.format("%s%c%s",
                             this.location,
                             File.separatorChar,
                             this.setName);
    }

    @SuppressWarnings("static-method")  // Needs to be used as an override in child classes that implement TileStoreReader/TileStoreWriter
    public TileOrigin getTileOrigin()
    {
        return TileCluster.Origin;
    }

    public TileScheme getTileScheme()
    {
        return this.tileScheme;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.crsProfile.getCoordinateReferenceSystem();
    }

    protected ClusterAddress getClusterAddress(final int column, final int row, final int zoomLevel)
    {
        if(zoomLevel <= 0 || zoomLevel > this.zoomLevels)
        {
            throw new IllegalArgumentException(String.format("Level must be between [0, %d]",
                                                             this.zoomLevels));
        }

        int targetZoomLevel = 0;
        int    endZoomLevel = 0;

        if(zoomLevel < this.breakPoint)
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

        final int    zoomLevelDifference = zoomLevel - targetZoomLevel;       // Compute the difference between the target cluster zoom level and the tile zoom level
        final double levelFactor         = Math.pow(2, zoomLevelDifference);  // Level factor is the number of tiles at zoom level "coordinate.getZoomLevel()" for a cluster that starts at "target zoom level"

        // Divide the row and column by the coordinate.getZoomLevel() factor to get the row and column address of the cluster we are using
        final long clusterRow    = (long)Math.floor(row    / levelFactor);
        final long clusterColumn = (long)Math.floor(column / levelFactor);

        return new ClusterAddress(clusterRow,
                                  clusterColumn,
                                  targetZoomLevel,
                                  endZoomLevel);
    }

    protected File getClusterFile(final ClusterAddress clusterAddress)
    {
        return new File(String.format("%s%c%s-%d-%d-%d.cluster",
                                      this.location,
                                      File.separatorChar,
                                      this.setName,
                                      clusterAddress.startlevel,
                                      clusterAddress.row,
                                      clusterAddress.column));
    }

    protected long getIndexPosition(final int row, final int column, final int zoomLevel)
    {
        final ClusterAddress clusterAddress = this.getClusterAddress(column, row, zoomLevel); // compute the local address that's the relative address of the tile in the cluster

        final int localZoomLevel = zoomLevel - clusterAddress.startlevel;

        final long localRow    = (long)(row    - (Math.pow(2 , localZoomLevel) * clusterAddress.row));
        final long localColumn = (long)(column - (Math.pow(2 , localZoomLevel) * clusterAddress.column));

        final int columnsAtLocallevel = (int)Math.pow(2 , localZoomLevel);

        final long indexPosition = TileCluster.getCumulativeTileCount(localZoomLevel - 1) + localRow * columnsAtLocallevel + localColumn;

        return indexPosition * IndexTileAddressByteSize; // multiply index position times byte size of a tile address
    }

    // TODO convert this into a normal equation ::math::
    protected static int getCumulativeTileCount(final int finalZoomLevel)
    {
        int count = 0;

        for(int i = 1; i <= finalZoomLevel; ++i)
        {
            count += (int)(Math.pow(2, 2 * i - 2));
        }

        return count;
    }

    protected static class ClusterAddress
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

    protected static final TileOrigin Origin = TileOrigin.LowerLeft; // TODO WARNING WARNING, THIS IS JUST A BLIND GUESS

    protected static final long NoDataLong = -1L;
    protected static final int  NoDataInt  = -1;

    protected final Path       location;
    protected final String     setName;
    protected final int        zoomLevels;
    protected final int        breakPoint;
    protected final CrsProfile crsProfile;
    protected final TileScheme tileScheme;

    private static final int IndexLocationByteSize = 8;  // size of long
    private static final int IndexSizeByteSize     = 4;  // size of int

    private static final int IndexTileAddressByteSize = IndexLocationByteSize + IndexSizeByteSize;
}
