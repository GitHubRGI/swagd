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

import java.io.File;
import java.nio.file.Path;

import com.rgi.common.coordinates.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.TileOrigin;

/**
 * @author Luke Lambert
 *
 * TODO This implementation is incomplete, and not ready for use
 *
 * TODO give attribution for this code "Tile-Based Geospatial Information Systems" by John T. Sample and Elias Ioup, Chapter 8 and specifically Listing 8.3 "Tile Clusters implementation" i.e. ClusteredTileStream
 *
 */
abstract class TileCluster
{
    public TileCluster(final Path location, final String setName, final int levels, final int breakPoint, final CrsProfile crsProfile)
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
        this.crsProfile = crsProfile;
    }

    protected ClusterAddress getClusterAddress(final int row, final int column, final int zoomLevel)
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
        return new File(String.format("%s/%s-%d-%d-%d.cluster",
                                      this.location,
                                      this.setName,
                                      clusterAddress.startlevel,
                                      clusterAddress.row,
                                      clusterAddress.column));
    }

    protected long getIndexPosition(final int row, final int column, final int zoomLevel)
    {
        final ClusterAddress clusterAddress = this.getClusterAddress(row, column, zoomLevel); // compute the local address that's the relative address of the tile in the cluster

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

    private static final int IndexLocationByteSize    = 8;  // size of long
    private static final int IndexSizeByteSize        = 4;  // size of int

    private static final int IndexTileAddressByteSize = IndexLocationByteSize + IndexSizeByteSize;

    protected static final TileOrigin Origin      = TileOrigin.LowerLeft; // TODO WARNING WARNING, THIS IS JUST A BLIND GUESS

    protected static final long NoDataLong  = -1L;
    protected static final int  NoDataInt   = -1;

    protected final Path        location;
    protected final String      setName;
    protected final int         zoomLevels;
    protected final int         breakPoint;
    protected final CrsProfile crsProfile;

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
}
